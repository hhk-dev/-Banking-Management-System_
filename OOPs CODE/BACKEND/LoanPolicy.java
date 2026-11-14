package BankSystem;

public final class LoanPolicy {
    public static final double MINIMUM_TOTAL_BALANCE = 500.0;
    private LoanPolicy() {}
    public static boolean isEligible(Customer customer) {
        return !customer.getAccounts().isEmpty() && customer.totalBalance() >= MINIMUM_TOTAL_BALANCE;
    }

    public static void checkEligibility(Customer customer) {
        if (!isEligible(customer)) {
            throw new IneligibleForLoanException("Loans require at least one account and a total balance of " + MINIMUM_TOTAL_BALANCE);
        }
    }
}