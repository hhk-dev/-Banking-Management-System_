package BankSystem;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

// Open endpoints. Anyone can create a customer or an employee login.
// Lock /api/employees down before using this for real.
@RestController
@RequestMapping("/api")
public class RegistrationController {
    private final Bank bank;

    public RegistrationController(Bank bank) {
        this.bank = bank;
    }

    @PostMapping("/customers")
    @ResponseStatus(HttpStatus.CREATED)
    public Customer registerCustomer(@RequestBody Requests.CustomerRequest body) {
        return bank.registerCustomer(body.userId(), body.name(), body.password());
    }

    @PostMapping("/employees")
    @ResponseStatus(HttpStatus.CREATED)
    public Employee registerEmployee(@RequestBody Requests.EmployeeRequest body) {
        if (body.role() == null) {
            throw new InvalidInputException("Role is required: TELLER, LOAN_OFFICER or MANAGER");
        }
        Employee employee = switch (body.role()) {
            case TELLER -> new Teller(body.userId(), body.name(), body.password());
            case LOAN_OFFICER -> new LoanOfficer(body.userId(), body.name(), body.password());
            case MANAGER -> new Manager(body.userId(), body.name(), body.password());
        };
        return bank.registerEmployee(employee);
    }
}