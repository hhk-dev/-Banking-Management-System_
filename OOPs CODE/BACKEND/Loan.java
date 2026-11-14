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

@Entity
@Table(name = "loan")
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private String loanId;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    @JsonIgnore
    private Customer owner;

    private String accountNumber;

    @Enumerated(EnumType.STRING)
    private LoanType type;

    private double principal;
    private int tenureMonths;
    private double monthlyEmi;
    private double outstanding;

    @Enumerated(EnumType.STRING)
    private LoanStatus status = LoanStatus.PENDING;

    private boolean disbursed = false;

    protected Loan() {
    }

    public Loan(String loanId, Customer owner, String accountNumber, LoanType type, double principal, int tenureMonths) {
        if (loanId == null || owner == null || accountNumber == null || type == null) {
            throw new InvalidInputException("Loan details cannot be null");
        }
        if (Money.round(principal) <= 0) {
            throw new InvalidInputException("Principal must be positive");
        }
        if (tenureMonths <= 0) {
            throw new InvalidInputException("Tenure must be at least one month");
        }
        this.loanId = loanId;
        this.owner = owner;
        this.accountNumber = accountNumber;
        this.type = type;
        this.principal = Money.round(principal);
        this.tenureMonths = tenureMonths;
        this.outstanding = this.principal;
        this.monthlyEmi = computeEmi(this.principal, monthlyRate(), tenureMonths);
    }

    public String getCustomerId() {
        return owner.getUserId();
    }

    private double monthlyRate() {
        return type.getAnnualRatePercent() / 1200.0;
    }

    // Reducing-balance EMI: P * r * (1+r)^n / ((1+r)^n - 1)
    private static double computeEmi(double principal, double monthlyRate, int months) {
        if (monthlyRate == 0) {
            return Money.round(principal / months);
        }
        double factor = Math.pow(1 + monthlyRate, months);
        return Money.round(principal * monthlyRate * factor / (factor - 1));
    }

    // Interest for the current month, charged on the outstanding balance.
    public synchronized double interestDue() {
        return Money.round(outstanding * monthlyRate());
    }

    // Total amount to be paid for this month: outstanding balance plus interest
    public synchronized double payoffAmount() {
        return Money.round(outstanding + interestDue());
    }

    // Total interest paid over the full loan period
    public double projectedInterest() {
        return Money.round(monthlyEmi * tenureMonths - principal);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Loan)) return false;
        return loanId.equals(((Loan) o).loanId);
    }

    @Override
    public int hashCode() {
        return loanId.hashCode();
    }

    private void requireStatus(LoanStatus expected, String action) {
        if (status != expected) {
            throw new InvalidLoanStateException("Cannot " + action + " loan " + loanId + " while it is " + status);
        }
    }

    synchronized void approve() {
        requireStatus(LoanStatus.PENDING, "approve");
        status = LoanStatus.APPROVED;
    }

    synchronized void reject() {
        requireStatus(LoanStatus.PENDING, "reject");
        status = LoanStatus.REJECTED;
    }

    // Marking the loan as paid out. This can only happen once.
    synchronized void markDisbursed() {
        requireStatus(LoanStatus.APPROVED, "disburse");
        if (disbursed) {
            throw new InvalidLoanStateException("Loan " + loanId + " has already been disbursed");
        }
        disbursed = true;
    }

    public synchronized boolean isRepayable() {
        return status == LoanStatus.APPROVED && disbursed;
    }

    synchronized void applyRepayment(double payment) {
        if (!isRepayable()) {
            throw new InvalidLoanStateException("Loan " + loanId + " is not open for repayment");
        }
        if (payment < interestDue() || payment > payoffAmount()) {
            throw new InvalidInputException("Payment must be between " + interestDue()
                    + " and " + payoffAmount());
        }
        outstanding = Money.round(outstanding - (payment - interestDue()));
        if (outstanding <= 0) {
            outstanding = 0;
            status = LoanStatus.CLOSED;
        }
    }

    // Getters
    public String getLoanId() {
        return loanId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public LoanType getType() {
        return type;
    }

    public double getPrincipal() {
        return principal;
    }

    public int getTenureMonths() {
        return tenureMonths;
    }

    public double getMonthlyEmi() {
        return monthlyEmi;
    }

    public synchronized double getOutstanding() {
        return outstanding;
    }

    public synchronized LoanStatus getStatus() {
        return status;
    }

    public synchronized boolean isDisbursed() {
        return disbursed;
    }
}