package BankSystem;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

// Customers and employees share one table. The user_type column says which kind each row is.
@Entity
@Table(name = "app_user")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "user_type")
public abstract class User {
    @Id
    private String userId;
    private String name;

    // Random value created for each user. It is combined with the password before hashing,
    // so the database never holds the plain password. @JsonIgnore keeps both out of API responses.
    @JsonIgnore
    private String salt;
    @JsonIgnore
    private String passwordHash;

    // Needed by JPA when it loads a row from the database
    protected User() {
    }

    protected User(String userId, String name, String password) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new InvalidInputException("userId is required");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidInputException("name is required");
        }
        if (password == null || password.length() < 6) {
            throw new InvalidInputException("password must be at least 6 characters");
        }
        this.userId = userId;
        this.name = name;
        this.salt = newSalt();
        this.passwordHash = hash(this.salt, password);
    }

    public boolean authenticate(String attempt) {
        return attempt != null && hash(salt, attempt).equals(passwordHash);
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return userId.equals(((User) o).userId);
    }

    @Override
    public int hashCode() {
        return userId.hashCode();
    }

    private static String newSalt() {
        byte[] bytes = new byte[16];
        new SecureRandom().nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    private static String hash(String salt, String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest((salt + password).getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new BankException("SHA-256 is not available" + e);
        }
    }
}