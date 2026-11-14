package BankSystem;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "account")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "account_kind")
public abstract class Account implements InterestBearing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private String accountNumber;

    private double balance;
    private String createdBy;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    @JsonIgnore
    private Customer owner;

    protected Account() {
    }

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

    // The old counter restarted at 1001 on every launch, so it could clash with saved accounts.
    // A random ID stays unique across restarts.
    private static String generateAccountNumber() {
        return "ACC" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }

    void attachTo(Customer owner) {
        this.owner = owner;
    }

    Customer getOwner() {
        return owner;
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

    // Multi-Threading: synchronized stops two threads from changing one balance at the same time.
    // The database transaction in Bank protects the rest.
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

    @Entity
    @DiscriminatorValue("SAVINGS")
    public static class SavingsAccount extends Account {
        protected SavingsAccount() {
        }

        public SavingsAccount(String createdBy, double balance) {
            super(createdBy, balance);
        }

        @Override
        public AccountType getAccountType() {
            return AccountType.SAVINGS;
        }
    }

    @Entity
    @DiscriminatorValue("CURRENT")
    public static class CurrentAccount extends Account {
        protected CurrentAccount() {
        }

        public CurrentAccount(String createdBy, double balance) {
            super(createdBy, balance);
        }

        @Override
        public AccountType getAccountType() {
            return AccountType.CURRENT;
        }
    }

    @Entity
    @DiscriminatorValue("SALARY")
    public static class SalaryAccount extends Account {
        protected SalaryAccount() {
        }

        public SalaryAccount(String createdBy, double balance) {
            super(createdBy, balance);
        }

        @Override
        public AccountType getAccountType() {
            return AccountType.SALARY;
        }
    }
}