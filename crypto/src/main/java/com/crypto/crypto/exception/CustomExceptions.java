package com.crypto.crypto.exception;
public class CustomExceptions {
    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String message) {
            super(message);
        }
    }
    public static class InvalidCredentialsException extends RuntimeException {
        public InvalidCredentialsException(String message) {
            super(message);
        }
    }
    public static class UserAlreadyExistsException extends RuntimeException {
        public UserAlreadyExistsException(String message) {
            super(message);
        }
    }
    public static class InvalidReferralCodeException extends RuntimeException {
        public InvalidReferralCodeException(String message) {
            super(message);
        }
    }
    public static class InsufficientBalanceException extends RuntimeException {
        public InsufficientBalanceException(String message) {
            super(message);
        }
    }
    public static class PlanNotFoundException extends RuntimeException {
        public PlanNotFoundException(String message) {
            super(message);
        }
    }
    public static class DepositNotFoundException extends RuntimeException {
        public DepositNotFoundException(String message) {
            super(message);
        }
    }
    public static class WithdrawalNotFoundException extends RuntimeException {
        public WithdrawalNotFoundException(String message) {
            super(message);
        }
    }
    public static class WalletNotFoundException extends RuntimeException {
        public WalletNotFoundException(String message) {
            super(message);
        }
    }
    public static class PromoCodeNotFoundException extends RuntimeException {
        public PromoCodeNotFoundException(String message) {
            super(message);
        }
    }
    public static class PromoCodeExpiredException extends RuntimeException {
        public PromoCodeExpiredException(String message) {
            super(message);
        }
    }
    public static class PromoCodeUsageLimitExceededException extends RuntimeException {
        public PromoCodeUsageLimitExceededException(String message) {
            super(message);
        }
    }
    public static class DailyCounterNotFoundException extends RuntimeException {
        public DailyCounterNotFoundException(String message) {
            super(message);
        }
    }
    public static class DailyCounterAlreadyActiveException extends RuntimeException {
        public DailyCounterAlreadyActiveException(String message) {
            super(message);
        }
    }
    public static class DailyCounterNotActiveException extends RuntimeException {
        public DailyCounterNotActiveException(String message) {
            super(message);
        }
    }
    public static class WalletChangeRequestNotFoundException extends RuntimeException {
        public WalletChangeRequestNotFoundException(String message) {
            super(message);
        }
    }
    public static class WalletChangeRequestAlreadyExistsException extends RuntimeException {
        public WalletChangeRequestAlreadyExistsException(String message) {
            super(message);
        }
    }
    public static class AdminSettingsNotFoundException extends RuntimeException {
        public AdminSettingsNotFoundException(String message) {
            super(message);
        }
    }
    public static class MaintenanceModeException extends RuntimeException {
        public MaintenanceModeException(String message) {
            super(message);
        }
    }
} 