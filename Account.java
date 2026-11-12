package BankSystem;

public abstract class Account implements InterestBearing{
    private static int totalAccounts = 0;
    private double balance;
    private final String createdBy;
    private final String accountNumber;

    // Here we, have Implemented User Defined Exception
    protected Account(String createdBy, double openingBalance) {
        if (createdBy == null || createdBy.trim().isEmpty()) {
            throw new InvalidInputException("createdBy (creator name) is required");
        }
        AccountType type = getAccountType();
        if (openingBalance < type.getMinimumBalance()) {
            throw new InvalidInputException(type + " account needs an opening balance of at least " + type.getMinimumBalance());
        }
        this.accountNumber = generateAccountNumber();
        this.createdBy = createdBy;
        this.balance = Money.round(openingBalance);
    }

    private static synchronized String generateAccountNumber() {
        totalAccounts++;
        return "ACC" + (1000 + totalAccounts);
    }

    // Enum is made Abstract due to 3 Types of Account
    public abstract AccountType getAccountType();
    public double minimumBalance() {
        return getAccountType().getMinimumBalance();
    }
    @Override
    public double interestRate() {
        return getAccountType().getInterestRate();
    }
    @Override
    public double calculateInterest() {
        return Money.round(getBalance() * interestRate());
    }

    // Multi-Threading
    // When 1 object is shared between multiple threads, Due to this Ambiguity occurs
    // Using Synchronized keyword --> Multiple Thread cannot access a single method at the same time

    synchronized void deposit(double amount) {
        if (amount <= 0) {
            throw new InvalidInputException("Deposit must be positive");
        }
        balance = Money.round(balance + amount);
    }
    synchronized void withdraw(double amount) {
        if (amount <= 0) {
            throw new InvalidInputException("Withdrawal must be positive");
        }
        if (Money.round(balance - amount) < minimumBalance()) {
            throw new InsufficientFundsException(getAccountType() + " accounts must keep at least "
                    + minimumBalance() + " in the balance");
        }
        balance = Money.round(balance - amount);
    }

    public synchronized double getBalance() {
        return balance;
    }
    public String getAccountNumber() {
        return accountNumber;
    }
    public String getCreatedBy() {
        return createdBy;
    }
    public static int getTotalAccounts() {
        return totalAccounts;
    }

    // Comparison of two objects
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Account)) return false;
        return accountNumber.equals(((Account) o).accountNumber);
    }

    @Override
    public int hashCode() {
        return accountNumber.hashCode();
    }

    public static class SavingsAccount extends Account {
        public SavingsAccount(String createdBy, double balance) {
            super(createdBy, balance);
        }
        @Override
        public AccountType getAccountType() {
            return AccountType.SAVINGS;
        }
    }

    public static class CurrentAccount extends Account {
        public CurrentAccount(String createdBy, double balance) {
            super(createdBy, balance);
        }
        @Override
        public AccountType getAccountType() {
            return AccountType.CURRENT;
        }
    }
    public static class SalaryAccount extends Account {
        public SalaryAccount(String createdBy, double balance) {
            super(createdBy, balance);
        }
        @Override
        public AccountType getAccountType() {
            return AccountType.SALARY;
        }
    }
}