package BankSystem;

import jakarta.persistence.CascadeType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@DiscriminatorValue("CUSTOMER")
public class Customer extends User {

    // mappedBy names the field on the child side that points back to this customer
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Account> accounts = new ArrayList<>();

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Loan> loans = new ArrayList<>();

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<Transaction> history = new ArrayList<>();

    protected Customer() {
    }

    public Customer(String userId, String name, String password) {
        super(userId, name, password);
    }

    Account createAccount(AccountType type, double openingBalance) {
        if (type == null) {
            throw new InvalidInputException("Account type is required");
        }
        if (openingBalance < type.getMinimumBalance()) {
            throw new InvalidInputException(type + " account needs an opening balance of at least " + type.getMinimumBalance());
        }
        Account account;
        switch (type) {
            case SAVINGS:
                account = new Account.SavingsAccount(getName(), openingBalance);
                break;
            case CURRENT:
                account = new Account.CurrentAccount(getName(), openingBalance);
                break;
            case SALARY:
                account = new Account.SalaryAccount(getName(), openingBalance);
                break;
            default:
                throw new InvalidInputException("Unsupported account type: " + type);
        }
        account.attachTo(this);
        accounts.add(account);
        return account;
    }

    public Account findAccount(String accountNumber) {
        for (Account a : accounts) {
            if (a.getAccountNumber().equals(accountNumber)) {
                return a;
            }
        }
        throw new NotFoundException("Account not found: " + accountNumber);
    }

    // ---------- Money movement ----------

    void deposit(String accountNumber, double amount) {
        findAccount(accountNumber).deposit(amount);
        record(accountNumber, TransactionType.DEPOSIT, amount, TransactionStatus.COMPLETED, null);
    }

    void withdraw(String accountNumber, double amount) {
        debit(accountNumber, amount, TransactionType.WITHDRAW, null);
    }

    private void debit(String accountNumber, double amount, TransactionType type, String counterparty) {
        Account account = findAccount(accountNumber);
        try {
            account.withdraw(amount);
        } catch (InsufficientFundsException e) {
            record(accountNumber, type, amount, TransactionStatus.FAILED, counterparty);
            throw e;
        }
        record(accountNumber, type, amount, TransactionStatus.COMPLETED, counterparty);
    }

    // TransferIn V/s TransferOut
    void transferOut(String fromAccount, String toAccount, double amount) {
        debit(fromAccount, amount, TransactionType.TRANSFER_OUT, toAccount);
    }

    void transferIn(String toAccount, String fromAccount, double amount) {
        findAccount(toAccount).deposit(amount);
        record(toAccount, TransactionType.TRANSFER_IN, amount, TransactionStatus.COMPLETED, fromAccount);
    }

    // Used when the destination account does not exist, so the sender still has a record.
    void recordFailedTransfer(String fromAccount, String toAccount, double amount) {
        record(fromAccount, TransactionType.TRANSFER_OUT, amount, TransactionStatus.FAILED, toAccount);
    }

    // Records every movement in the history
    private void record(String accountNumber, TransactionType type, double amount, TransactionStatus status, String counterparty) {
        Transaction entry = new Transaction(accountNumber, type, amount, status, counterparty);
        entry.attachTo(this);
        history.add(entry);
    }

    // Monthly interest that the bank provides
    void postMonthlyInterest(String accountNumber) {
        Account account = findAccount(accountNumber);
        double interest = account.monthlyInterest();
        if (interest <= 0) {
            return;
        }
        account.deposit(interest);
        record(accountNumber, TransactionType.INTEREST, interest, TransactionStatus.COMPLETED, null);
    }

    // ---------- Loans ----------

    Loan requestLoanForAccount(String accountNumber, LoanType loanType, double principal, int tenureMonths) {
        LoanPolicy.checkEligibility(this);
        Account target = findAccount(accountNumber);
        // Loan ID = customer ID plus a running number, e.g. cust01-L1
        Loan loan = new Loan(getUserId() + "-L" + (loans.size() + 1), this, target.getAccountNumber(),
                loanType, principal, tenureMonths);
        loans.add(loan);
        return loan;
    }

    public Loan findLoan(String loanId) {
        for (Loan l : loans) {
            if (l.getLoanId().equals(loanId)) {
                return l;
            }
        }
        throw new NotFoundException("Loan not found: " + loanId);
    }

    // Credits the approved principal to the linked account. Called only from the approval path.
    void disburse(Loan loan) {
        if (!loans.contains(loan)) {
            throw new NotFoundException("Loan not found: " + loan.getLoanId());
        }
        Account account = findAccount(loan.getAccountNumber());
        loan.markDisbursed();
        account.deposit(loan.getPrincipal());
        record(account.getAccountNumber(), TransactionType.LOAN_DISBURSEMENT, loan.getPrincipal(),
                TransactionStatus.COMPLETED, loan.getLoanId());
    }

    // Repayment of loan
    double repayLoan(String loanId, double amount) {
        Loan loan = findLoan(loanId);
        if (!loan.isRepayable()) {
            throw new InvalidLoanStateException("Loan " + loanId + " is not open for repayment");
        }
        double interest = loan.interestDue();
        if (amount < interest) {
            throw new InvalidInputException("Payment must cover at least this month's interest of " + interest);
        }
        // The payment is capped at the payoff amount
        double payment = Money.round(Math.min(amount, loan.payoffAmount()));
        debit(loan.getAccountNumber(), payment, TransactionType.LOAN_REPAYMENT, loanId);
        loan.applyRepayment(payment);
        return payment;
    }

    // ---------- Totals and getters ----------

    public double totalBalance() {
        double total = 0;
        for (Account a : accounts) {
            total += a.getBalance();
        }
        return Money.round(total);
    }

    public double projectedAnnualInterest() {
        double total = 0;
        for (Account a : accounts) {
            total += a.calculateInterest();
        }
        return Money.round(total);
    }

    public List<Account> getAccounts() {
        return Collections.unmodifiableList(accounts);
    }

    public List<Loan> getLoans() {
        return Collections.unmodifiableList(loans);
    }

    public List<Transaction> getHistory() {
        return Collections.unmodifiableList(history);
    }
}