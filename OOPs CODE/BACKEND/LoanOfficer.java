package BankSystem;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("LOAN_OFFICER")
public class LoanOfficer extends Employee {

    protected LoanOfficer() {
    }

    public LoanOfficer(String userId, String name, String password) {
        super(userId, name, password);
    }

    @Override
    public EmployeeRole getRole() {
        return EmployeeRole.LOAN_OFFICER;
    }

    // The only path to a loan decision. Bank calls this after checking the officer is registered.
    Loan decide(Customer customer, Loan loan, boolean approve) {
        if (!approve) {
            loan.reject();
            return loan;
        }
        if (!verifyCustomer(customer)) {
            throw new IneligibleForLoanException("Customer " + customer.getUserId()
                    + " no longer meets the loan eligibility rules");
        }
        loan.approve();
        customer.disburse(loan);
        return loan;
    }
}