package BankSystem;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;

// Stored in the database. The ID and timestamp are filled in automatically when the row is saved.
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    @JsonIgnore
    private Customer customer;

    private String accountNumber;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    private double amount;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    private String counterparty; // the other account or loan involved

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime timestamp;

    protected Transaction() {
    }

    public Transaction(String accountNumber, TransactionType type, double amount,
                       TransactionStatus status, String counterparty) {
        if (accountNumber == null || type == null || status == null) {
            throw new InvalidInputException("accountNumber, type and status are required");
        }
        if (amount <= 0) {
            throw new InvalidInputException("Amount must be positive");
        }
        this.accountNumber = accountNumber;
        this.type = type;
        this.amount = amount;
        this.status = status;
        this.counterparty = counterparty == null ? "-" : counterparty;
    }

    void attachTo(Customer customer) {
        this.customer = customer;
    }

    public String getTransactionId() {
        return "T" + id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public TransactionType getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public String getCounterparty() {
        return counterparty;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "Transaction[" + getTransactionId() + ", " + accountNumber + ", " + type + ", " + amount
                + ", " + status + ", " + counterparty + ", " + timestamp + "]";
    }
}