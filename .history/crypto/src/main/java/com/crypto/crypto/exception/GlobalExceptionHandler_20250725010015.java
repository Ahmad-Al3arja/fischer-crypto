package com.crypto.crypto.exception;

import com.crypto.crypto.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import com.crypto.crypto.exception.CustomExceptions;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @Autowired
    private MessageService messageService;
    
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        // Try to get localized message, fallback to original message
        String localizedMessage = getLocalizedMessage(ex.getMessage());
        ErrorResponse error = new ErrorResponse(messageService.getMessage("general.error"), localizedMessage);
        return ResponseEntity.badRequest().body(error);
    }
    
    @ExceptionHandler(CustomExceptions.DuplicatePhoneException.class)
    public ResponseEntity<ErrorResponse> handleDuplicatePhoneException(CustomExceptions.DuplicatePhoneException ex) {
        String message = messageService.getMessage("auth.register.phone_exists");
        ErrorResponse error = new ErrorResponse(messageService.getMessage("auth.register.error", null, "Registration Error"), message);
        return ResponseEntity.badRequest().body(error);
    }
    
    @ExceptionHandler(CustomExceptions.DuplicateUsernameException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateUsernameException(CustomExceptions.DuplicateUsernameException ex) {
        String message = messageService.getMessage("auth.register.username_exists");
        ErrorResponse error = new ErrorResponse(messageService.getMessage("auth.register.error", null, "Registration Error"), message);
        return ResponseEntity.badRequest().body(error);
    }
    
    @ExceptionHandler(CustomExceptions.InvalidReferralCodeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidReferralCodeException(CustomExceptions.InvalidReferralCodeException ex) {
        String message = messageService.getMessage("auth.register.invalid_referral");
        ErrorResponse error = new ErrorResponse(messageService.getMessage("auth.register.error", null, "Registration Error"), message);
        return ResponseEntity.badRequest().body(error);
    }
    
    @ExceptionHandler(CustomExceptions.AccountSuspendedException.class)
    public ResponseEntity<ErrorResponse> handleAccountSuspendedException(CustomExceptions.AccountSuspendedException ex) {
        String message = messageService.getMessage("auth.login.account_suspended");
        ErrorResponse error = new ErrorResponse(messageService.getMessage("auth.account.error", null, "Account Error"), message);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }
    
    @ExceptionHandler(CustomExceptions.InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentialsException(CustomExceptions.InvalidCredentialsException ex) {
        String message = messageService.getMessage("auth.login.invalid_credentials");
        ErrorResponse error = new ErrorResponse(messageService.getMessage("auth.authentication.error", null, "Authentication Error"), message);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }
    
    @ExceptionHandler(CustomExceptions.PasswordMismatchException.class)
    public ResponseEntity<ErrorResponse> handlePasswordMismatchException(CustomExceptions.PasswordMismatchException ex) {
        String message = messageService.getMessage("auth.register.password_mismatch");
        ErrorResponse error = new ErrorResponse(messageService.getMessage("auth.register.error", null, "Registration Error"), message);
        return ResponseEntity.badRequest().body(error);
    }
    
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException ex) {
        String message = messageService.getMessage("auth.login.invalid_credentials");
        ErrorResponse error = new ErrorResponse(messageService.getMessage("auth.authentication.error", null, "Authentication Error"), message);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }
    
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
        String message = messageService.getMessage("general.access_denied");
        ErrorResponse error = new ErrorResponse(messageService.getMessage("general.access_denied"), message);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = getLocalizedValidationMessage(fieldName, error.getDefaultMessage());
            errors.put(fieldName, errorMessage);
        });
        
        String title = messageService.getMessage("validation.error", null, "Validation Error");
        String message = messageService.getMessage("validation.check_fields", null, "Please check the input fields");
        ValidationErrorResponse error = new ValidationErrorResponse(title, message, errors);
        return ResponseEntity.badRequest().body(error);
    }
    
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFoundException(NoHandlerFoundException ex) {
        String message = messageService.getMessage("general.not_found");
        ErrorResponse error = new ErrorResponse(message, ex.getHttpMethod() + " " + ex.getRequestURL());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        String message = messageService.getMessage("general.internal_error");
        ErrorResponse error = new ErrorResponse(message, ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
    
    // Helper method to get localized messages based on exception content
    private String getLocalizedMessage(String originalMessage) {
        // Map common error messages to message keys
        if (originalMessage.contains("User not found")) {
            return messageService.getMessage("user.not_found");
        } else if (originalMessage.contains("Plan not found")) {
            return messageService.getMessage("plan.not_found");
        } else if (originalMessage.contains("Insufficient balance")) {
            return messageService.getMessage("user.balance_insufficient");
        } else if (originalMessage.contains("Minimum withdrawal")) {
            return messageService.getMessage("withdrawal.minimum_amount");
        } else if (originalMessage.contains("Wallet address")) {
            return messageService.getMessage("wallet.address_required");
        } else if (originalMessage.contains("Counter already active")) {
            return messageService.getMessage("counter.already_active");
        } else if (originalMessage.contains("Counter not active")) {
            return messageService.getMessage("counter.not_active");
        } else if (originalMessage.contains("Promo code")) {
            return messageService.getMessage("promo.code_invalid");
        }
        
        // Return original message if no mapping found
        return originalMessage;
    }
    
    // Helper method for validation messages
    private String getLocalizedValidationMessage(String fieldName, String defaultMessage) {
        // Try to get specific validation message
        String key = "validation." + fieldName + ".invalid";
        String localizedMessage = messageService.getMessage(key, null, null);
        
        if (localizedMessage == null || localizedMessage.equals(key)) {
            // Fallback to general validation messages
            if (defaultMessage.contains("required")) {
                return messageService.getMessage("validation.required");
            } else if (defaultMessage.contains("phone")) {
                return messageService.getMessage("validation.phone.invalid");
            } else if (defaultMessage.contains("password")) {
                return messageService.getMessage("validation.password.min_length");
            }
            return defaultMessage;
        }
        
        return localizedMessage;
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
    
    public static class ValidationErrorResponse extends ErrorResponse {
        private Map<String, String> fieldErrors;
        
        public ValidationErrorResponse(String error, String message, Map<String, String> fieldErrors) {
            super(error, message);
            this.fieldErrors = fieldErrors;
        }
        
        public Map<String, String> getFieldErrors() { return fieldErrors; }
        public void setFieldErrors(Map<String, String> fieldErrors) { this.fieldErrors = fieldErrors; }
    }
} 