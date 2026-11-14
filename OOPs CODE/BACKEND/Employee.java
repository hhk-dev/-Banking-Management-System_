package BankSystem;

import jakarta.persistence.Entity;
import java.util.List;

@Entity
public abstract class Employee extends User {

    protected Employee() {
    }

    protected Employee(String userId, String name, String password) {
        super(userId, name, password);
    }

    // Same rule as loan requests, so the two can never disagree.
    public boolean verifyCustomer(Customer customer) {
        return LoanPolicy.isEligible(customer);
    }

    public String generateReport(List<Transaction> rows) {
        double credits = 0;
        double debits = 0;
        int failed = 0;
        for (Transaction t : rows) {
            if (t.getStatus() == TransactionStatus.FAILED) {
                failed++;
            } else if (isCredit(t.getType())) {
                credits += t.getAmount();
            } else {
                debits += t.getAmount();
            }
        }
        return "Report by " + getName() + ": " + rows.size() + " entries, credits " + Money.round(credits)
                + ", debits " + Money.round(debits) + ", failed " + failed;
    }

    private static boolean isCredit(TransactionType type) {
        switch (type) {
            case DEPOSIT:
            case TRANSFER_IN:
            case INTEREST:
            case LOAN_DISBURSEMENT:
                return true;
            default:
                return false;
        }
    }

    public abstract EmployeeRole getRole();
}