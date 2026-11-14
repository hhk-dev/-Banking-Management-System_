package BankSystem;

public enum AccountType {
    SAVINGS(500.0, 0.04),
    CURRENT(1000.0, 0.0),
    SALARY(0.0, 0.02);

    private final double minimumBalance;
    private final double interestRate;

    AccountType(double minimumBalance, double interestRate) {
        this.minimumBalance = minimumBalance;
        this.interestRate = interestRate;
    }

    public double getMinimumBalance() {
        return minimumBalance;
    }

    public double getInterestRate() {
        return interestRate;
    }
}