package BankSystem;

public final class Money {
    private Money() {}

    // Rounding-off to two decimal places.
    public static double round(double amount) {
        // Making whole number --> Rounding off --> dividing by 100
        return Math.round(amount * 100.0) / 100.0;
    }
}
