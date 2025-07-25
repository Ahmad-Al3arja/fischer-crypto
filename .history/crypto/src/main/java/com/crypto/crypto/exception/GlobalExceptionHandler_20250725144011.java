package com.crypto.crypto.exception;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.HashMap;
import java.util.Map;
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(CustomExceptions.UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(CustomExceptions.UserNotFoundException ex) {
        ErrorResponse error = new ErrorResponse("User not found", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    @ExceptionHandler(CustomExceptions.InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentialsException(CustomExceptions.InvalidCredentialsException ex) {
        ErrorResponse error = new ErrorResponse("Invalid credentials", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }
    @ExceptionHandler(CustomExceptions.UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExistsException(CustomExceptions.UserAlreadyExistsException ex) {
        ErrorResponse error = new ErrorResponse("User already exists", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
    @ExceptionHandler(CustomExceptions.InvalidReferralCodeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidReferralCodeException(CustomExceptions.InvalidReferralCodeException ex) {
        ErrorResponse error = new ErrorResponse("Invalid referral code", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    @ExceptionHandler(CustomExceptions.InsufficientBalanceException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientBalanceException(CustomExceptions.InsufficientBalanceException ex) {
        ErrorResponse error = new ErrorResponse("Insufficient balance", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    @ExceptionHandler(CustomExceptions.PlanNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePlanNotFoundException(CustomExceptions.PlanNotFoundException ex) {
        ErrorResponse error = new ErrorResponse("Plan not found", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    @ExceptionHandler(CustomExceptions.DepositNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleDepositNotFoundException(CustomExceptions.DepositNotFoundException ex) {
        ErrorResponse error = new ErrorResponse("Deposit not found", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    @ExceptionHandler(CustomExceptions.WithdrawalNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleWithdrawalNotFoundException(CustomExceptions.WithdrawalNotFoundException ex) {
        ErrorResponse error = new ErrorResponse("Withdrawal not found", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    @ExceptionHandler(CustomExceptions.WalletNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleWalletNotFoundException(CustomExceptions.WalletNotFoundException ex) {
        ErrorResponse error = new ErrorResponse("Wallet not found", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    @ExceptionHandler(CustomExceptions.PromoCodeNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePromoCodeNotFoundException(CustomExceptions.PromoCodeNotFoundException ex) {
        ErrorResponse error = new ErrorResponse("Promo code not found", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    @ExceptionHandler(CustomExceptions.PromoCodeExpiredException.class)
    public ResponseEntity<ErrorResponse> handlePromoCodeExpiredException(CustomExceptions.PromoCodeExpiredException ex) {
        ErrorResponse error = new ErrorResponse("Promo code expired", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    @ExceptionHandler(CustomExceptions.PromoCodeUsageLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handlePromoCodeUsageLimitExceededException(CustomExceptions.PromoCodeUsageLimitExceededException ex) {
        ErrorResponse error = new ErrorResponse("Promo code usage limit exceeded", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    @ExceptionHandler(CustomExceptions.DailyCounterNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleDailyCounterNotFoundException(CustomExceptions.DailyCounterNotFoundException ex) {
        ErrorResponse error = new ErrorResponse("Daily counter not found", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    @ExceptionHandler(CustomExceptions.DailyCounterAlreadyActiveException.class)
    public ResponseEntity<ErrorResponse> handleDailyCounterAlreadyActiveException(CustomExceptions.DailyCounterAlreadyActiveException ex) {
        ErrorResponse error = new ErrorResponse("Daily counter already active", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    @ExceptionHandler(CustomExceptions.DailyCounterNotActiveException.class)
    public ResponseEntity<ErrorResponse> handleDailyCounterNotActiveException(CustomExceptions.DailyCounterNotActiveException ex) {
        ErrorResponse error = new ErrorResponse("Daily counter not active", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    @ExceptionHandler(CustomExceptions.WalletChangeRequestNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleWalletChangeRequestNotFoundException(CustomExceptions.WalletChangeRequestNotFoundException ex) {
        ErrorResponse error = new ErrorResponse("Wallet change request not found", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    @ExceptionHandler(CustomExceptions.WalletChangeRequestAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleWalletChangeRequestAlreadyExistsException(CustomExceptions.WalletChangeRequestAlreadyExistsException ex) {
        ErrorResponse error = new ErrorResponse("Wallet change request already exists", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
    @ExceptionHandler(CustomExceptions.AdminSettingsNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAdminSettingsNotFoundException(CustomExceptions.AdminSettingsNotFoundException ex) {
        ErrorResponse error = new ErrorResponse("Admin settings not found", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    @ExceptionHandler(CustomExceptions.MaintenanceModeException.class)
    public ResponseEntity<ErrorResponse> handleMaintenanceModeException(CustomExceptions.MaintenanceModeException ex) {
        ErrorResponse error = new ErrorResponse("Maintenance mode", ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex) {
        ErrorResponse error = new ErrorResponse("Authentication failed", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException ex) {
        ErrorResponse error = new ErrorResponse("Invalid credentials", "Invalid phone number or password");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
        ErrorResponse error = new ErrorResponse("Access denied", "You don't have permission to access this resource");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        ValidationErrorResponse error = new ValidationErrorResponse("Validation failed", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        ex.printStackTrace(); // Print stack trace to logs
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal server error", "An unexpected error occurred"));
    }
    public static class ErrorResponse {
        private String error;
        private String message;
        public ErrorResponse(String error, String message) {
            this.error = error;
            this.message = message;
        }
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
    public static class ValidationErrorResponse {
        private String error;
        private Map<String, String> errors;
        public ValidationErrorResponse(String error, Map<String, String> errors) {
            this.error = error;
            this.errors = errors;
        }
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
        public Map<String, String> getErrors() { return errors; }
        public void setErrors(Map<String, String> errors) { this.errors = errors; }
    }
} 