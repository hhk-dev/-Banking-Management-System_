package BankSystem;

// The shapes of the JSON bodies the REST endpoints accept.
public final class Requests {
    private Requests() {
    }

    public record CustomerRequest(String userId, String name, String password) {}
    public record EmployeeRequest(String userId, String name, String password, EmployeeRole role) {}
    public record OpenAccountRequest(AccountType type, double openingBalance) {}
    public record AmountRequest(double amount) {}
    public record DepositRequest(String accountNumber, double amount) {}
    public record TransferRequest(String fromAccount, String toAccount, double amount) {}
    public record LoanRequest(String accountNumber, LoanType type, double principal, int tenureMonths) {}
}