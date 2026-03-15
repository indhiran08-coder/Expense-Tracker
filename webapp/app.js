/* =============================================
   EXPENSE TRACKER – app.js  (Supabase Edition)
   Real PostgreSQL via Supabase · Chart.js · CSV
   ============================================= */

'use strict';

// ============================================================
// SUPABASE CONFIG
// ============================================================
const SUPABASE_URL = 'https://dwsddrfjybtcyxawqjty.supabase.co';
const SUPABASE_KEY = 'sb_publishable_ONfsyb4fKt87kZlaHMWXjA__actgifW';

const { createClient } = supabase;
const db = createClient(SUPABASE_URL, SUPABASE_KEY);

// ============================================================
// STATE
// ============================================================
let expenses      = [];   // local mirror of DB rows
let currentSalary = 0;
let editingId     = null; // null = add mode; number = edit mode
let deleteTargetId = null;
let spendingChart  = null;

const SAL_KEY = 'expenseTrackerSalary'; // salary stays in localStorage

// ============================================================
// INITIALISATION
// ============================================================
document.addEventListener('DOMContentLoaded', async () => {
  // Restore salary from localStorage
  currentSalary = parseFloat(localStorage.getItem(SAL_KEY) || '0') || 0;
  const salInput = document.getElementById('salaryInput');
  salInput.value = currentSalary > 0 ? currentSalary : '';

  // Salary change listener
  salInput.addEventListener('input', async () => {
    currentSalary = parseFloat(salInput.value) || 0;
    localStorage.setItem(SAL_KEY, String(currentSalary));
    recomputeAllBalances();
    await pushBalancesToDB();
    renderTable();
    renderChart();
    updateSummaryCards();
  });

  // Set today as default date
  document.getElementById('dateInput').value = todayStr();

  // Export button
  document.getElementById('exportBtn').addEventListener('click', exportCSV);

  // Load data from Supabase
  setLoading(true);
  await loadFromDB();
  setLoading(false);
});

// ============================================================
// LOADING STATE
// ============================================================
function setLoading(on) {
  const btn = document.getElementById('submitBtn');
  if (on) {
    btn.disabled = true;
    document.getElementById('submitBtnText').textContent = '⏳ Loading…';
  } else {
    btn.disabled = false;
    document.getElementById('submitBtnText').textContent =
      editingId !== null ? 'Save Changes' : '➕ Add Expense';
  }
}

// ============================================================
// LOAD FROM SUPABASE (replaces loadExpensesFromDB in Java)
// ============================================================
async function loadFromDB() {
  const { data, error } = await db
    .from('expenses')
    .select('*')
    .order('created_at', { ascending: true });

  if (error) {
    showToast('❌ Could not load expenses: ' + error.message, 'error');
    console.error(error);
    return;
  }

  expenses = data || [];
  recomputeAllBalances();
  renderTable();
  renderChart();
  updateSummaryCards();
  populateCategoryFilter();
}

// ============================================================
// BALANCE RECALCULATION (mirrors recomputeAllBalances() in Java)
// ============================================================
function recomputeAllBalances() {
  let running = currentSalary;
  expenses.forEach(ex => {
    running    -= ex.amount;
    ex.balance  = running;
  });
  updateSummaryCards();
}

async function pushBalancesToDB() {
  // Update balance column for all rows in Supabase
  const updates = expenses.map(ex =>
    db.from('expenses').update({ balance: ex.balance, salary: currentSalary }).eq('id', ex.id)
  );
  await Promise.all(updates);
}

// ============================================================
// ADD OR UPDATE EXPENSE
// ============================================================
async function submitExpense() {
  const category    = document.getElementById('categoryInput').value.trim();
  const amountRaw   = document.getElementById('amountInput').value.trim();
  const description = document.getElementById('descriptionInput').value.trim();
  const date        = document.getElementById('dateInput').value;

  if (!category || !amountRaw || !date) {
    showToast('⚠️ Please fill in Category, Amount, and Date.', 'error');
    return;
  }
  const amount = parseFloat(amountRaw);
  if (isNaN(amount) || amount <= 0) {
    showToast('⚠️ Enter a valid positive amount.', 'error');
    return;
  }

  setLoading(true);

  if (editingId !== null) {
    await updateExpenseInDB(editingId, { category, amount, description, date });
  } else {
    await addExpenseToDB({ category, amount, description, date });
  }

  setLoading(false);
}

// ---- INSERT ----
async function addExpenseToDB(fields) {
  const { data, error } = await db
    .from('expenses')
    .insert([{
      category:    fields.category,
      amount:      fields.amount,
      description: fields.description,
      date:        fields.date,
      balance:     0,
      salary:      currentSalary,
    }])
    .select()
    .single();

  if (error) {
    showToast('❌ Failed to add expense: ' + error.message, 'error');
    console.error(error);
    return;
  }

  expenses.push(data);
  recomputeAllBalances();
  await pushBalancesToDB();

  renderTable();
  renderChart();
  populateCategoryFilter();
  showToast('✅ Expense added!', 'success');
  clearForm();
}

// ---- UPDATE ----
async function updateExpenseInDB(id, fields) {
  const { data, error } = await db
    .from('expenses')
    .update({
      category:    fields.category,
      amount:      fields.amount,
      description: fields.description,
      date:        fields.date,
    })
    .eq('id', id)
    .select()
    .single();

  if (error) {
    showToast('❌ Update failed: ' + error.message, 'error');
    console.error(error);
    return;
  }

  const idx = expenses.findIndex(e => e.id === id);
  if (idx > -1) expenses[idx] = { ...expenses[idx], ...data };

  recomputeAllBalances();
  await pushBalancesToDB();

  renderTable();
  renderChart();
  populateCategoryFilter();
  showToast('✅ Expense updated!', 'success');
  cancelEdit();
}

// ============================================================
// EDIT MODE
// ============================================================
function startEdit(id) {
  const ex = expenses.find(e => e.id === id);
  if (!ex) return;

  editingId = id;

  document.getElementById('categoryInput').value    = ex.category;
  document.getElementById('amountInput').value      = ex.amount;
  document.getElementById('descriptionInput').value = ex.description || '';
  document.getElementById('dateInput').value        = ex.date ? ex.date.slice(0, 10) : '';

  document.getElementById('formTitle').textContent     = '✏️ Edit Expense';
  document.getElementById('submitBtnText').textContent = 'Save Changes';
  document.getElementById('cancelBtn').style.display   = 'block';

  document.querySelectorAll('tbody tr').forEach(tr => tr.classList.remove('selected-row'));
  const row = document.querySelector(`tr[data-id="${id}"]`);
  if (row) row.classList.add('selected-row');

  window.scrollTo({ top: 0, behavior: 'smooth' });
}

function cancelEdit() {
  editingId = null;
  clearForm();
  document.getElementById('formTitle').textContent     = '➕ Add Expense';
  document.getElementById('submitBtnText').textContent = '➕ Add Expense';
  document.getElementById('cancelBtn').style.display   = 'none';
  document.querySelectorAll('tbody tr').forEach(tr => tr.classList.remove('selected-row'));
}

// ============================================================
// DELETE
// ============================================================
function confirmDelete(id) {
  deleteTargetId = id;
  const ex = expenses.find(e => e.id === id);
  document.getElementById('deleteModalMsg').textContent =
    ex ? `Delete "${ex.category}" — ₹${formatINR(ex.amount)}?` : 'This action cannot be undone.';
  document.getElementById('deleteModal').style.display = 'flex';
  document.getElementById('confirmDeleteBtn').onclick  = doDelete;
}

async function doDelete() {
  closeModal();
  setLoading(true);

  const { error } = await db
    .from('expenses')
    .delete()
    .eq('id', deleteTargetId);

  if (error) {
    showToast('❌ Delete failed: ' + error.message, 'error');
    setLoading(false);
    return;
  }

  expenses = expenses.filter(e => e.id !== deleteTargetId);
  recomputeAllBalances();
  await pushBalancesToDB();

  renderTable();
  renderChart();
  populateCategoryFilter();
  showToast('🗑️ Expense deleted.', 'info');
  setLoading(false);
  deleteTargetId = null;
}

function closeModal() {
  document.getElementById('deleteModal').style.display = 'none';
}

// ============================================================
// TABLE RENDER
// ============================================================
function renderTable() {
  const searchVal  = document.getElementById('searchInput').value.toLowerCase();
  const filterCat  = document.getElementById('filterCategory').value;
  const sortVal    = document.getElementById('sortBy').value;
  const tbody      = document.getElementById('tableBody');
  const emptyState = document.getElementById('emptyState');
  const table      = document.getElementById('expenseTable');

  let filtered = expenses.filter(ex => {
    const matchCat    = !filterCat || ex.category === filterCat;
    const matchSearch = !searchVal ||
      ex.category.toLowerCase().includes(searchVal)    ||
      (ex.description || '').toLowerCase().includes(searchVal) ||
      String(ex.amount).includes(searchVal)            ||
      (ex.date || '').includes(searchVal);
    return matchCat && matchSearch;
  });

  filtered.sort((a, b) => {
    switch (sortVal) {
      case 'date-desc':   return (b.date || '').localeCompare(a.date || '');
      case 'date-asc':    return (a.date || '').localeCompare(b.date || '');
      case 'amount-desc': return b.amount - a.amount;
      case 'amount-asc':  return a.amount - b.amount;
      default: return 0;
    }
  });

  tbody.innerHTML = '';

  if (filtered.length === 0) {
    emptyState.style.display = 'flex';
    table.style.display      = 'none';
  } else {
    emptyState.style.display = 'none';
    table.style.display      = 'table';

    filtered.forEach((ex, idx) => {
      const balClass = ex.balance >= 0 ? 'balance-positive' : 'balance-negative';
      const emoji    = getCategoryEmoji(ex.category);
      const tr       = document.createElement('tr');
      tr.setAttribute('data-id', ex.id);
      tr.innerHTML = `
        <td>${idx + 1}</td>
        <td><span class="badge">${emoji} ${escHtml(ex.category)}</span></td>
        <td class="amount-cell">₹ ${formatINR(ex.amount)}</td>
        <td>${escHtml(ex.description) || '<span style="opacity:.4">—</span>'}</td>
        <td>${formatDate(ex.date)}</td>
        <td class="${balClass}">₹ ${(ex.balance >= 0 ? '+' : '') + formatINR(ex.balance)}</td>
        <td>
          <div class="action-btns">
            <button class="btn-icon edit" onclick="startEdit(${ex.id})" title="Edit">✏️</button>
            <button class="btn-icon del"  onclick="confirmDelete(${ex.id})" title="Delete">🗑️</button>
          </div>
        </td>`;
      tbody.appendChild(tr);
    });
  }
}

// ============================================================
// SUMMARY CARDS
// ============================================================
function updateSummaryCards() {
  const total   = expenses.reduce((s, e) => s + e.amount, 0);
  const balance = currentSalary - total;
  const topCat  = getTopCategory();

  document.getElementById('summSalary').textContent  = '₹ ' + formatINR(currentSalary);
  document.getElementById('summTotal').textContent   = '₹ ' + formatINR(total);
  document.getElementById('summBalance').textContent = '₹ ' + formatINR(balance);
  document.getElementById('summTop').textContent     = topCat || '—';

  const balEl = document.getElementById('summBalance');
  balEl.className = 'card-value ' + (balance >= 0 ? '' : 'danger');
}

function getTopCategory() {
  const map = {};
  expenses.forEach(e => { map[e.category] = (map[e.category] || 0) + e.amount; });
  const sorted = Object.entries(map).sort((a, b) => b[1] - a[1]);
  return sorted.length ? sorted[0][0] : null;
}

// ============================================================
// CHART
// ============================================================
const CHART_COLORS = [
  '#00c6ff','#0072ff','#a78bfa','#f59e0b','#10b981',
  '#ef4444','#ec4899','#06b6d4','#84cc16','#f97316',
];

function renderChart() {
  const ctx = document.getElementById('spendingChart').getContext('2d');
  const map = {};
  expenses.forEach(e => { map[e.category] = (map[e.category] || 0) + e.amount; });
  const labels = Object.keys(map);
  const data   = Object.values(map);
  const total  = data.reduce((s, v) => s + v, 0);

  if (spendingChart) spendingChart.destroy();

  if (!labels.length) {
    spendingChart = new Chart(ctx, {
      type: 'doughnut',
      data: { labels: ['No Expenses'], datasets: [{ data:[1], backgroundColor:['rgba(255,255,255,0.07)'], borderColor:'transparent' }] },
      options: chartOptions(false)
    });
    document.getElementById('legendWrap').innerHTML =
      '<p style="color:var(--text-muted);font-size:.85rem">Add expenses to see breakdown.</p>';
    return;
  }

  spendingChart = new Chart(ctx, {
    type: 'doughnut',
    data: {
      labels,
      datasets: [{
        data,
        backgroundColor: CHART_COLORS.slice(0, labels.length),
        borderColor: 'rgba(10,22,40,0.9)',
        borderWidth: 3,
        hoverOffset: 10,
      }]
    },
    options: chartOptions(true)
  });

  document.getElementById('legendWrap').innerHTML = labels.map((lbl, i) => `
    <div class="legend-item">
      <span class="legend-dot" style="background:${CHART_COLORS[i % CHART_COLORS.length]}"></span>
      <span class="legend-label">${escHtml(lbl)}</span>
      <span class="legend-amount">₹ ${formatINR(data[i])}</span>
      <span class="legend-pct">${total > 0 ? ((data[i]/total)*100).toFixed(1) : 0}%</span>
    </div>`).join('');
}

function chartOptions(interactive) {
  return {
    responsive: false,
    animation: { duration: 600, easing: 'easeInOutQuart' },
    plugins: {
      legend: { display: false },
      tooltip: {
        enabled: interactive,
        backgroundColor: 'rgba(10,22,40,0.95)',
        titleColor: '#00c6ff',
        bodyColor: '#e8edf5',
        borderColor: 'rgba(0,198,255,0.25)',
        borderWidth: 1,
        padding: 12,
        callbacks: {
          label: ctx => ` ₹ ${formatINR(ctx.parsed)} (${((ctx.parsed/ctx.dataset.data.reduce((a,b)=>a+b,0))*100).toFixed(1)}%)`
        }
      }
    },
    cutout: '68%',
  };
}

// ============================================================
// CATEGORY FILTER
// ============================================================
function populateCategoryFilter() {
  const sel  = document.getElementById('filterCategory');
  const prev = sel.value;
  const cats = [...new Set(expenses.map(e => e.category))].sort();
  sel.innerHTML = '<option value="">All Categories</option>' +
    cats.map(c => `<option value="${escHtml(c)}" ${c === prev ? 'selected' : ''}>${escHtml(c)}</option>`).join('');
}

// ============================================================
// CSV EXPORT
// ============================================================
function exportCSV() {
  if (!expenses.length) { showToast('⚠️ No expenses to export.', 'error'); return; }
  const headers = ['#','Category','Amount (INR)','Description','Date','Balance (INR)'];
  const rows = expenses.map((ex, i) => [
    i + 1, `"${ex.category}"`, ex.amount.toFixed(2),
    `"${ex.description || ''}"`, ex.date, ex.balance.toFixed(2)
  ]);
  const csv  = [headers.join(','), ...rows.map(r => r.join(','))].join('\n');
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
  const url  = URL.createObjectURL(blob);
  const a    = document.createElement('a');
  a.href = url; a.download = `expenses_${todayStr()}.csv`; a.click();
  URL.revokeObjectURL(url);
  showToast('📤 CSV exported!', 'success');
}

// ============================================================
// HELPERS
// ============================================================
function clearForm() {
  document.getElementById('categoryInput').value    = '';
  document.getElementById('amountInput').value      = '';
  document.getElementById('descriptionInput').value = '';
  document.getElementById('dateInput').value        = todayStr();
}

function todayStr() {
  return new Date().toISOString().slice(0, 10);
}

function formatINR(val) {
  return Math.abs(val).toLocaleString('en-IN', { minimumFractionDigits:2, maximumFractionDigits:2 });
}

function formatDate(dateStr) {
  if (!dateStr) return '—';
  const s = String(dateStr).slice(0, 10);
  const match = s.match(/^(\d{4})-(\d{2})-(\d{2})$/);
  if (!match) return dateStr;
  const d = new Date(s + 'T00:00:00');
  if (isNaN(d.getTime())) return dateStr;
  return d.toLocaleDateString('en-IN', { day:'2-digit', month:'short', year:'numeric' });
}

function escHtml(str) {
  const el = document.createElement('div');
  el.textContent = str || '';
  return el.innerHTML;
}

function getCategoryEmoji(cat) {
  const map = {
    food:'🍔', rent:'🏠', transport:'🚌', transportation:'🚌',
    entertainment:'🎬', shopping:'🛍️', health:'💊', utilities:'💡',
    education:'📚', savings:'💰', travel:'✈️', other:'📦'
  };
  return map[(cat || '').toLowerCase()] || '🏷️';
}

// ---- TOAST ----
let toastTimer = null;
function showToast(message, type = 'info') {
  const toast = document.getElementById('toast');
  toast.textContent = message;
  toast.className   = `toast ${type} show`;
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => { toast.className = 'toast'; }, 3200);
}
