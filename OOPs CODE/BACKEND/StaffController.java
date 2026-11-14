package BankSystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

// Staff endpoints. Same login headers as customers. The role is checked on every call,
// so a teller's login cannot use the manager endpoints.
@RestController
@RequestMapping("/api")
public class StaffController {
    private final Bank bank;

    public StaffController(Bank bank) {
        this.bank = bank;
    }

    // ---------- Teller ----------

    @PostMapping("/teller/deposits")
    public void depositCash(@RequestHeader("X-User-Id") String userId,
                            @RequestHeader("X-Password") String password,
                            @RequestBody Requests.DepositRequest body) {
        Teller teller = asRole(userId, password, Teller.class);
        bank.depositCash(teller, body.accountNumber(), body.amount());
    }

    // ---------- Loan officer ----------

    @PostMapping("/loan-officer/customers/{customerId}/loans/{loanId}/decision")
    public Loan decideLoan(@RequestHeader("X-User-Id") String userId,
                           @RequestHeader("X-Password") String password,
                           @PathVariable String customerId,
                           @PathVariable String loanId,
                           @RequestParam boolean approve) {
        LoanOfficer officer = asRole(userId, password, LoanOfficer.class);
        return bank.reviewLoan(officer, customerId, loanId, approve);
    }

    // ---------- Manager ----------

    @GetMapping("/manager/customers")
    public List<Customer> allCustomers(@RequestHeader("X-User-Id") String userId,
                                       @RequestHeader("X-Password") String password) {
        asRole(userId, password, Manager.class);
        return bank.getCustomers();
    }

    @GetMapping("/manager/customers/{customerId}/report")
    public Map<String, String> report(@RequestHeader("X-User-Id") String userId,
                                      @RequestHeader("X-Password") String password,
                                      @PathVariable String customerId) {
        Manager manager = asRole(userId, password, Manager.class);
        return Map.of("report", bank.customerReport(manager, customerId));
    }

    // Saves the statement as a text file in the ./statements folder of the project
    @PostMapping("/manager/customers/{customerId}/statement")
    public Map<String, String> statement(@RequestHeader("X-User-Id") String userId,
                                         @RequestHeader("X-Password") String password,
                                         @PathVariable String customerId) throws IOException {
        Manager manager = asRole(userId, password, Manager.class);
        Path file = Path.of("statements", customerId + ".txt");
        Files.createDirectories(file.toAbsolutePath().getParent());
        bank.exportStatement(manager, customerId, file);
        return Map.of("savedTo", file.toAbsolutePath().toString());
    }

    @PostMapping("/manager/interest")
    public Map<String, String> runMonthlyInterest(@RequestHeader("X-User-Id") String userId,
                                                  @RequestHeader("X-Password") String password) {
        asRole(userId, password, Manager.class);
        bank.runMonthlyInterest();
        return Map.of("message", "Monthly interest posted to every account");
    }

    // Logs the employee in and checks that they have the role this endpoint needs
    private <T extends Employee> T asRole(String userId, String password, Class<T> role) {
        Employee employee = bank.authenticateEmployee(userId, password);
        if (!role.isInstance(employee)) {
            throw new UnauthorizedException("This action is only for " + role.getSimpleName() + "s");
        }
        return role.cast(employee);
    }
}