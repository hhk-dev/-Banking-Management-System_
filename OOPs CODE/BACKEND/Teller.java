package BankSystem;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("TELLER")
public class Teller extends Employee {

    protected Teller() {
    }

    public Teller(String userId, String name, String password) {
        super(userId, name, password);
    }

    @Override
    public EmployeeRole getRole() {
        return EmployeeRole.TELLER;
    }
}