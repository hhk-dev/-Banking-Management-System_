package BankSystem;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// Turns the bank's exceptions into clear HTTP responses instead of stack traces.
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler({InvalidInputException.class, NotFoundException.class, UnauthorizedException.class,
            InsufficientFundsException.class, IneligibleForLoanException.class,
            InvalidLoanStateException.class, BankException.class})
    public ResponseEntity<Map<String, String>> handle(RuntimeException e) {
        return ResponseEntity.status(statusFor(e)).body(Map.of("error", String.valueOf(e.getMessage())));
    }

    private HttpStatus statusFor(RuntimeException e) {
        if (e instanceof NotFoundException) return HttpStatus.NOT_FOUND;
        if (e instanceof UnauthorizedException) return HttpStatus.UNAUTHORIZED;
        if (e instanceof InvalidInputException) return HttpStatus.BAD_REQUEST;
        return HttpStatus.UNPROCESSABLE_ENTITY; // business rules: insufficient funds, not eligible, wrong loan state
    }
}