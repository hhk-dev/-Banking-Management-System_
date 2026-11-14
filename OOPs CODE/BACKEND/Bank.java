package BankSystem;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Spring creates one Bank object and injects it into the controllers.
// It replaces the old in-memory maps with database calls. Each public method runs as one
// database transaction, so a half-finished change is rolled back.
// Failed attempts (insufficient funds, unknown destination) are still saved to the history,
// so those two exceptions must NOT roll the transaction back.
@Service
@Transactional(noRollbackFor = {InsufficientFundsException.class, NotFoundException.class})
public class Bank {
    private final CustomerRepository customers;
    private final EmployeeRepository employees;
    private final AccountRepository accounts;

    public Bank(CustomerRepository customers, EmployeeRepository employees, AccountRepository accounts) {
        this.customers = customers;
        this.employees = employees;
        this.accounts = accounts;
    }


    // ---------- Registration ----------

    public Customer registerCustomer(String userId, String customerName, String password) {
        Customer customer = new Customer(userId, customerName, password); // validates the input first
        requireUnusedId(userId);
        return customers.save(customer);
    }

    // Returns the concrete type it was given, so new Teller(...) comes back as a Teller.
    public <E extends Employee> E registerEmployee(E employee) {
        if (employee == null) {
            throw new InvalidInputException("Employee is required");
        }
        requireUnusedId(employee.getUserId());
        return employees.save(employee);
    }

    private void requireUnusedId(String userId) {
        if (customers.existsById(userId) || employees.existsById(userId)) {
            throw new InvalidInputException("User ID already in use: " + userId);
        }
    }

    // ---------- Login ----------

    // A wrong password and an unknown ID give the same message, so nothing extra is revealed.
    public Customer authenticateCustomer(String userId, String password) {
        return customers.findById(userId)
                .filter(c -> c.authenticate(password))
                .orElseThrow(() -> new UnauthorizedException("Wrong user ID or password"));
    }

    public Employee authenticateEmployee(String userId, String password) {
        return employees.findById(userId)
                .filter(e -> e.authenticate(password))
                .orElseThrow(() -> new UnauthorizedException("Wrong user ID or password"));
    }

    // ---------- Lookup ----------

    public Customer getCustomer(String userId) {
        return customers.findById(userId)
                .orElseThrow(() -> new NotFoundException("Customer not found: " + userId));
    }

    public Customer findAccountOwner(String accountNumber) {
        return ownerOf(accountNumber)
                .orElseThrow(() -> new NotFoundException("Account not found: " + accountNumber));
    }

    private Optional<Customer> ownerOf(String accountNumber) {
        return accounts.findByAccountNumber(accountNumber).map(Account::getOwner);
    }

    public List<Customer> getCustomers() {
        return customers.findAll();
    }

    // ---------- Customer operations (every change goes through here) ----------

    public Account openAccount(String userId, AccountType type, double openingBalance) {
        return getCustomer(userId).createAccount(type, openingBalance);
    }

    public void withdraw(String userId, String accountNumber, double amount) {
        getCustomer(userId).withdraw(accountNumber, amount);
    }

    public Loan requestLoan(String userId, String accountNumber, LoanType type,
                            double principal, int tenureMonths) {
        return getCustomer(userId).requestLoanForAccount(accountNumber, type, principal, tenureMonths);
    }

    public double repayLoan(String userId, String loanId, double amount) {
        return getCustomer(userId).repayLoan(loanId, amount);
    }

    // Debit first. If the debit is refused, nothing is credited to the recipient.
    public void transfer(String fromAccount, String toAccount, double amount) {
        double rounded = Money.round(amount);
        if (rounded <= 0) {
            throw new InvalidInputException("Transfer amount must be positive");
        }
        Customer sender = findAccountOwner(fromAccount);
        if (Objects.equals(fromAccount, toAccount)) {
            throw new InvalidInputException("Cannot transfer to the same account");
        }
        Optional<Customer> recipient = ownerOf(toAccount);
        if (recipient.isEmpty()) {
            sender.recordFailedTransfer(fromAccount, toAccount, rounded);
            throw new NotFoundException("Destination account not found: " + toAccount);
        }
        sender.transferOut(fromAccount, toAccount, rounded);
        recipient.get().transferIn(toAccount, fromAccount, rounded);
    }

    // ---------- Teller: cash deposits ----------

    // A customer adds money to one of their own accounts.
    // getCustomer and findAccount make sure the account belongs to this customer.
    public void customerDeposit(String userId, String accountNumber, double amount) {
        getCustomer(userId).deposit(accountNumber, amount);
    }
    public void depositCash(Teller teller, String accountNumber, double amount) {
        requireRegistered(teller, "accept cash deposits");
        findAccountOwner(accountNumber).deposit(accountNumber, amount);
    }

    // ---------- Loan officer: approve or reject ----------

    public Loan reviewLoan(LoanOfficer officer, String userId, String loanId, boolean approve) {
        requireRegistered(officer, "review loans");
        Customer customer = getCustomer(userId);
        Loan loan = customer.findLoan(loanId);
        return officer.decide(customer, loan, approve);
    }

    // ---------- Manager: reports and files ----------

    public String customerReport(Manager manager, String userId) {
        requireRegistered(manager, "generate reports");
        return manager.generateReport(getCustomer(userId).getHistory());
    }

    public void exportStatement(Manager manager, String userId, Path file) throws IOException {
        requireRegistered(manager, "export statements");
        Customer customer = getCustomer(userId);
        List<String> lines = new ArrayList<>();
        lines.add(manager.generateReport(customer.getHistory()));
        for (Transaction t : customer.getHistory()) {
            lines.add(t.toString());
        }
        StatementWriter.write(file, lines);
    }

    // ---------- Bank-wide job ----------

    // Posts one month of interest to every account. Running it twice posts interest twice.
    public void runMonthlyInterest() {
        for (Customer customer : customers.findAll()) {
            for (Account account : customer.getAccounts()) {
                customer.postMonthlyInterest(account.getAccountNumber());
            }
        }
    }

    // ---------- Authorization ----------

    private void requireRegistered(Employee employee, String action) {
        if (employee == null || !employees.existsById(employee.getUserId())) {
            throw new UnauthorizedException("Only registered employees can " + action);
        }
    }
}