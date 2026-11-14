package BankSystem;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

// Customer endpoints. Each request sends the login in two headers: X-User-Id and X-Password.
@RestController
@RequestMapping("/api/customer")
public class CustomerController {
    private final Bank bank;

    public CustomerController(Bank bank) {
        this.bank = bank;
    }

    @GetMapping("/me")
    public Customer profile(@RequestHeader("X-User-Id") String userId,
                            @RequestHeader("X-Password") String password) {
        return bank.authenticateCustomer(userId, password);
    }

    @PostMapping("/accounts")
    @ResponseStatus(HttpStatus.CREATED)
    public Account openAccount(@RequestHeader("X-User-Id") String userId,
                               @RequestHeader("X-Password") String password,
                               @RequestBody Requests.OpenAccountRequest body) {
        bank.authenticateCustomer(userId, password);
        return bank.openAccount(userId, body.type(), body.openingBalance());
    }

    // NEW: a customer adds money to one of their own accounts
    @PostMapping("/accounts/{accountNumber}/deposit")
    public void deposit(@RequestHeader("X-User-Id") String userId,
                        @RequestHeader("X-Password") String password,
                        @PathVariable String accountNumber,
                        @RequestBody Requests.AmountRequest body) {
        bank.authenticateCustomer(userId, password);
        bank.customerDeposit(userId, accountNumber, body.amount());
    }

    @PostMapping("/accounts/{accountNumber}/withdraw")
    public void withdraw(@RequestHeader("X-User-Id") String userId,
                         @RequestHeader("X-Password") String password,
                         @PathVariable String accountNumber,
                         @RequestBody Requests.AmountRequest body) {
        bank.authenticateCustomer(userId, password);
        bank.withdraw(userId, accountNumber, body.amount());
    }

    @PostMapping("/transfers")
    public void transfer(@RequestHeader("X-User-Id") String userId,
                         @RequestHeader("X-Password") String password,
                         @RequestBody Requests.TransferRequest body) {
        Customer me = bank.authenticateCustomer(userId, password);
        me.findAccount(body.fromAccount()); // throws if this customer does not own the source account
        bank.transfer(body.fromAccount(), body.toAccount(), body.amount());
    }

    @PostMapping("/loans")
    @ResponseStatus(HttpStatus.CREATED)
    public Loan requestLoan(@RequestHeader("X-User-Id") String userId,
                            @RequestHeader("X-Password") String password,
                            @RequestBody Requests.LoanRequest body) {
        bank.authenticateCustomer(userId, password);
        return bank.requestLoan(userId, body.accountNumber(), body.type(), body.principal(), body.tenureMonths());
    }

    @PostMapping("/loans/{loanId}/repay")
    public double repayLoan(@RequestHeader("X-User-Id") String userId,
                            @RequestHeader("X-Password") String password,
                            @PathVariable String loanId,
                            @RequestBody Requests.AmountRequest body) {
        bank.authenticateCustomer(userId, password);
        return bank.repayLoan(userId, loanId, body.amount());
    }
}