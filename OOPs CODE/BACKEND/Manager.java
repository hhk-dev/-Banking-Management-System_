package BankSystem;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("MANAGER")
public class Manager extends Employee {

    protected Manager() {
    }

    public Manager(String userId, String name, String password) {
        super(userId, name, password);
    }

    @Override
    public EmployeeRole getRole() {
        return EmployeeRole.MANAGER;
    }
}