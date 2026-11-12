package BankSystem;

public class IneligibleForLoanException extends RuntimeException {
    public IneligibleForLoanException(String message) {
        super(message);
    }
}
