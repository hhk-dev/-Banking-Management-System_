package BankSystem;

public interface InterestBearing {
    double interestRate();       // annual rate as a decimal, e.g. 0.04 means 4%
    double calculateInterest();  // annual interest on the current balance

    default double monthlyInterest() {
        return Money.round(calculateInterest() / 12);
    }
}