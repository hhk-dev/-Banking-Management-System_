// Talks to the bank's API and switches between the login, sign-up and dashboard screens.

const API = '/api';
const SESSION_KEY = 'bankSession';
const CREDIT_TYPES = ['DEPOSIT', 'TRANSFER_IN', 'INTEREST', 'LOAN_DISBURSEMENT'];

// Same yearly rates as LoanType.java. If you change a rate there, change it here too.
const ANNUAL_RATE = { HOME: 7.5, PERSONAL: 12, EDUCATION: 9 };

// The database stores a fully repaid loan as CLOSED. The screen calls it CLEARED.
const LOAN_LABEL = { PENDING: 'PENDING', APPROVED: 'APPROVED', REJECTED: 'REJECTED', CLOSED: 'CLEARED' };
const COUNTED_STATUSES = ['PENDING', 'APPROVED', 'CLOSED']; // the tabs that show a number

let session = null;     // { userId, password } while someone is logged in
let customer = null;    // latest data from GET /api/customer/me
let loanFilter = 'ALL'; // the selected loan tab: ALL, PENDING, APPROVED or CLOSED

const $ = (id) => document.getElementById(id);

// ---------- Helpers ----------

// Sends a request with the login headers. Returns the reply, or throws the bank's error message.
async function api(path, method = 'GET', body) {
    const headers = { 'Content-Type': 'application/json' };
    if (session) {
        headers['X-User-Id'] = session.userId;
        headers['X-Password'] = session.password;
    }
    const response = await fetch(API + path, {
        method,
        headers,
        body: body === undefined ? undefined : JSON.stringify(body)
    });
    const text = await response.text();
    let data = null;
    try {
        data = text ? JSON.parse(text) : null;
    } catch (e) {
        data = null;
    }
    if (!response.ok) {
        throw new Error((data && data.error) || `Something went wrong (status ${response.status})`);
    }
    return data;
}

// Makes text safe to place inside the page.
function esc(value) {
    return String(value ?? '').replace(/[&<>"']/g, (c) => (
        { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]
    ));
}

function money(value) {
    return '₹' + Number(value).toLocaleString('en-IN', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    });
}

// Rounds to two decimal places, the same way the bank does (Money.round)
function round2(value) {
    return Math.round(value * 100) / 100;
}

// "2026-11-14T01:49:53.919529" becomes "2026-11-14 01:49"
function when(timestamp) {
    return timestamp ? timestamp.replace('T', ' ').slice(0, 16) : '';
}

// The text shown on screen for a stored loan status
function labelFor(status) {
    return LOAN_LABEL[status] || status;
}

// This month's interest, worked out the same way as the bank: outstanding × (annual rate ÷ 12)
function interestFor(loan) {
    const monthlyRate = ANNUAL_RATE[loan.type] / 1200;
    return round2(loan.outstanding * monthlyRate);
}

// The amount that clears the loan: outstanding balance plus this month's interest
function payoffFor(loan) {
    return round2(loan.outstanding + interestFor(loan));
}

function notice(text = '', type = 'success') {
    const box = $('notice');
    box.textContent = text;
    box.className = `notice ${type}`;
    box.hidden = !text;
}

// Shows exactly one of the three screens
function showScreen(name) {
    $('login-view').hidden = name !== 'login';
    $('signup-view').hidden = name !== 'signup';
    $('dashboard').hidden = name !== 'dashboard';
}

// ---------- Login, sign-up and logout ----------

async function loadDashboard() {
    customer = await api('/customer/me');
    render();
}

async function restoreSession() {
    const saved = sessionStorage.getItem(SESSION_KEY);
    if (!saved) return;
    session = JSON.parse(saved);
    try {
        await loadDashboard();
    } catch (err) {
        logout();
    }
}

function logout() {
    session = null;
    customer = null;
    loanFilter = 'ALL';
    sessionStorage.removeItem(SESSION_KEY);
    notice('');
    showScreen('login');
}

function showSignup() {
    $('signup-form').reset();
    $('signup-message').textContent = '';
    $('signup-message').className = 'message';
    showScreen('signup');
}

// Returns to the login screen. A success message can be shown, and the user ID can be filled in.
function showLogin(message = '', userId = '', isSuccess = false) {
    const box = $('login-message');
    box.textContent = message;
    box.className = isSuccess ? 'message success' : 'message';
    $('login-password').value = '';
    if (userId) {
        $('login-user').value = userId;
    }
    showScreen('login');
}

// ---------- Drawing the dashboard ----------

function render() {
    showScreen('dashboard');
    $('welcome').textContent = `Welcome, ${customer.name}`;
    const total = customer.accounts.reduce((sum, a) => sum + a.balance, 0);
    $('total-balance').textContent = money(total);
    renderAccounts();
    renderLoans();
    renderTransactions();
    fillSelects();
}

function renderAccounts() {
    const accounts = customer.accounts;
    $('accounts').innerHTML = accounts.length
        ? accounts.map((a) => `
        <div class="account">
          <div>
            <strong>${esc(a.accountType)}</strong>
            <div class="muted small">${esc(a.accountNumber)}</div>
          </div>
          <div class="amount">${money(a.balance)}</div>
        </div>`).join('')
        : '<p class="muted">No accounts yet. Open one below.</p>';
}

// ---------- Loans with filter tabs ----------

function renderLoans() {
    updateLoanTabs();

    const loans = loanFilter === 'ALL'
        ? customer.loans
        : customer.loans.filter((l) => l.status === loanFilter);

    if (!loans.length) {
        $('loans').innerHTML = loanFilter === 'ALL'
            ? '<p class="muted">No loans yet.</p>'
            : `<p class="muted">No ${labelFor(loanFilter).toLowerCase()} loans.</p>`;
        return;
    }

    $('loans').innerHTML = `<table>
      <thead>
        <tr>
          <th>Loan ID</th><th>Type</th><th>Principal</th>
          <th>Outstanding</th><th>Monthly EMI</th><th>Status</th>
        </tr>
      </thead>
      <tbody>
        ${loans.map((l) => `
          <tr>
            <td>${esc(l.loanId)}</td>
            <td>${esc(l.type)}</td>
            <td>${money(l.principal)}</td>
            <td>${money(l.outstanding)}</td>
            <td>${money(l.monthlyEmi)}</td>
            <td><span class="badge ${esc(l.status)}">${esc(labelFor(l.status))}</span></td>
          </tr>`).join('')}
      </tbody>
    </table>`;
}

// Writes the number of loans into each tab and highlights the selected one
function updateLoanTabs() {
    const counts = { ALL: customer.loans.length };
    COUNTED_STATUSES.forEach((s) => {
        counts[s] = customer.loans.filter((l) => l.status === s).length;
    });
    document.querySelectorAll('#loan-tabs .tab').forEach((tab) => {
        const key = tab.dataset.filter;
        tab.querySelector('.count').textContent = counts[key];
        tab.classList.toggle('active', key === loanFilter);
    });
}

// ---------- Transactions: every entry, newest first, with a count ----------

function renderTransactions() {
    // Newest first. The server returns the history oldest first.
    const rows = customer.history.slice().reverse();

    if (!rows.length) {
        $('transactions').innerHTML = '<p class="muted">No transactions yet.</p>';
        return;
    }

    $('transactions').innerHTML = `
    <p class="muted small tx-count">Showing all ${rows.length} transactions, newest first.</p>
    <div class="tx-scroll">
      <table>
        <thead>
          <tr>
            <th>Date</th><th>Type</th><th>Account</th>
            <th>Amount</th><th>Status</th><th>Counterparty</th>
          </tr>
        </thead>
        <tbody>
          ${rows.map((t) => {
        const credit = CREDIT_TYPES.includes(t.type);
        const failed = t.status === 'FAILED';
        const cls = failed ? 'failed' : (credit ? 'credit' : 'debit');
        const sign = failed ? '' : (credit ? '+' : '-');
        return `
              <tr>
                <td>${esc(when(t.timestamp))}</td>
                <td>${esc(t.type)}</td>
                <td>${esc(t.accountNumber)}</td>
                <td class="${cls}">${sign}${money(t.amount)}</td>
                <td><span class="badge ${esc(t.status)}">${esc(t.status)}</span></td>
                <td>${esc(t.counterparty)}</td>
              </tr>`;
    }).join('')}
        </tbody>
      </table>
    </div>`;
}

// Fills every account dropdown with this customer's own accounts
function fillSelects() {
    const accountOptions = customer.accounts.map((a) =>
        `<option value="${esc(a.accountNumber)}">${esc(a.accountType)} - ${esc(a.accountNumber)} (${money(a.balance)})</option>`
    ).join('');
    $('deposit-account').innerHTML = accountOptions;
    $('withdraw-account').innerHTML = accountOptions;
    $('transfer-from').innerHTML = accountOptions;
    $('loan-account').innerHTML = accountOptions;

    fillRepaySection();
}

// Only approved, paid-out loans can be paid. Otherwise the dropdown, amount and Pay button are greyed out.
function fillRepaySection() {
    const payable = customer.loans.filter((l) => l.repayable);
    const hasLoans = payable.length > 0;

    $('repay-loan').innerHTML = hasLoans
        ? payable.map((l) =>
            `<option value="${esc(l.loanId)}">${esc(l.loanId)} · ${esc(l.type)} · owes ${money(l.outstanding)}</option>`
        ).join('')
        : '<option value="">No approved loan to pay yet</option>';

    $('repay-loan').disabled = !hasLoans;
    $('repay-amount').disabled = !hasLoans;
    $('repay-form').querySelector('button[type="submit"]').disabled = !hasLoans;

    updateRepayAmount();
}

// Fills the amount with the full payoff for the selected loan and explains it under the box
function updateRepayAmount() {
    const hint = $('repay-form').querySelector('p.small');
    const loan = customer.loans.find((l) => l.loanId === $('repay-loan').value && l.repayable);

    if (!loan) {
        $('repay-amount').value = '';
        hint.textContent = 'Repayment opens once a loan is approved. Pending loans cannot be paid yet.';
        return;
    }

    const payoff = payoffFor(loan);
    $('repay-amount').value = payoff.toFixed(2);
    hint.textContent = `Full payoff: ${money(payoff)} clears this loan. It includes ${money(interestFor(loan))} interest for this month. You may pay less, as long as it covers that interest.`;
}

// ---------- Actions ----------

// Runs one action, reloads the data so the screen shows the change, then reports the result.
async function act(form, action) {
    try {
        const message = await action();
        form.reset();
        await loadDashboard();
        notice(message, 'success');
    } catch (err) {
        notice(err.message, 'error');
    }
}

// ---------- Event wiring ----------

$('login-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const message = $('login-message');
    session = { userId: $('login-user').value.trim(), password: $('login-password').value };
    message.className = 'message';
    message.textContent = 'Checking...';
    try {
        await loadDashboard();
        sessionStorage.setItem(SESSION_KEY, JSON.stringify(session));
        $('login-password').value = '';
        message.textContent = '';
    } catch (err) {
        session = null;
        message.textContent = err.message;
    }
});

$('show-signup').addEventListener('click', showSignup);
$('show-login').addEventListener('click', () => showLogin());

$('signup-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const userId = $('signup-user').value.trim();
    const name = $('signup-name').value.trim();
    const password = $('signup-password').value;
    const confirm = $('signup-confirm').value;
    const message = $('signup-message');

    message.className = 'message';
    if (password !== confirm) {
        message.textContent = 'The two passwords do not match.';
        return;
    }
    message.textContent = 'Creating your login...';
    try {
        await api('/customers', 'POST', { userId, name, password });
        showLogin('Login created. You can log in now.', userId, true);
    } catch (err) {
        message.textContent = err.message;
    }
});

$('logout-btn').addEventListener('click', logout);

$('refresh-btn').addEventListener('click', () => {
    loadDashboard().catch((err) => notice(err.message, 'error'));
});

// Clicking a loan tab shows only the loans in that group
document.querySelectorAll('#loan-tabs .tab').forEach((tab) => {
    tab.addEventListener('click', () => {
        loanFilter = tab.dataset.filter;
        renderLoans();
    });
});

// Choosing a different loan fills in its payoff amount
$('repay-loan').addEventListener('change', updateRepayAmount);

$('open-account-form').addEventListener('submit', (e) => {
    e.preventDefault();
    const body = {
        type: $('new-account-type').value,
        openingBalance: Number($('new-account-amount').value)
    };
    act(e.target, async () => {
        await api('/customer/accounts', 'POST', body);
        return `New ${body.type.toLowerCase()} account opened.`;
    });
});

$('deposit-form').addEventListener('submit', (e) => {
    e.preventDefault();
    const accountNumber = $('deposit-account').value;
    const amount = Number($('deposit-amount').value);
    act(e.target, async () => {
        await api(`/customer/accounts/${encodeURIComponent(accountNumber)}/deposit`, 'POST', { amount });
        return `Deposited ${money(amount)}.`;
    });
});

$('withdraw-form').addEventListener('submit', (e) => {
    e.preventDefault();
    const accountNumber = $('withdraw-account').value;
    const amount = Number($('withdraw-amount').value);
    act(e.target, async () => {
        await api(`/customer/accounts/${encodeURIComponent(accountNumber)}/withdraw`, 'POST', { amount });
        return `Withdrew ${money(amount)}.`;
    });
});

$('transfer-form').addEventListener('submit', (e) => {
    e.preventDefault();
    const body = {
        fromAccount: $('transfer-from').value,
        toAccount: $('transfer-to').value.trim(),
        amount: Number($('transfer-amount').value)
    };
    act(e.target, async () => {
        await api('/customer/transfers', 'POST', body);
        return `Transferred ${money(body.amount)} to ${body.toAccount}.`;
    });
});

// The system decides the loan at once, so the message shows the real result
$('loan-form').addEventListener('submit', (e) => {
    e.preventDefault();
    const body = {
        accountNumber: $('loan-account').value,
        type: $('loan-type').value,
        principal: Number($('loan-amount').value),
        tenureMonths: Number($('loan-months').value)
    };
    act(e.target, async () => {
        const loan = await api('/customer/loans', 'POST', body);
        if (loan.status === 'APPROVED') {
            return `Loan ${loan.loanId} approved. ${money(loan.principal)} has been credited to your account.`;
        }
        return `Loan ${loan.loanId} is ${labelFor(loan.status).toLowerCase()}.`;
    });
});

$('repay-form').addEventListener('submit', (e) => {
    e.preventDefault();
    const loanId = $('repay-loan').value;
    if (!loanId) {
        notice('No approved loan to pay yet.', 'error');
        return;
    }
    const amount = Number($('repay-amount').value);
    act(e.target, async () => {
        const paid = await api(`/customer/loans/${encodeURIComponent(loanId)}/repay`, 'POST', { amount });
        await loadDashboard(); // refresh first, so the status is current
        const loan = customer.loans.find((l) => l.loanId === loanId);
        if (loan && loan.status === 'CLOSED') {
            return `Paid ${money(paid)}. Loan ${loanId} is fully paid and now CLEARED.`;
        }
        return `Paid ${money(paid)} towards ${loanId}.`;
    });
});

restoreSession();