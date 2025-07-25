package com.crypto.crypto.exception;

/**
 * Custom exceptions for better error handling
 */
public class CustomExceptions {
    
    public static class DuplicatePhoneException extends RuntimeException {
        public DuplicatePhoneException(String phoneNumber) {
            super("Phone number '" + phoneNumber + "' is already registered");
        }
    }
    
    public static class DuplicateUsernameException extends RuntimeException {
        public DuplicateUsernameException(String username) {
            super("Username '" + username + "' is already taken");
        }
    }
    
    public static class InvalidReferralCodeException extends RuntimeException {
        public InvalidReferralCodeException(String referralCode) {
            super("Invalid referral code: '" + referralCode + "'");
        }
    }
    
    public static class AccountSuspendedException extends RuntimeException {
        public AccountSuspendedException() {
            super("Your account has been suspended");
        }
    }
    
    public static class InvalidCredentialsException extends RuntimeException {
        public InvalidCredentialsException() {
            super("Invalid phone number or password");
        }
    }
    
    public static class PasswordMismatchException extends RuntimeException {
        public PasswordMismatchException() {
            super("Passwords do not match");
        }
    }
} 