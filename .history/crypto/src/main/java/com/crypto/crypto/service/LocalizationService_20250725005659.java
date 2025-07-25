package com.crypto.crypto.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
public class LocalizationService {

    @Autowired
    private MessageSource messageSource;

    /**
     * Get localized message by key
     */
    public String getMessage(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }

    /**
     * Get localized message by key with arguments
     */
    public String getMessage(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }

    /**
     * Get localized message with default value if key not found
     */
    public String getMessage(String key, String defaultMessage) {
        return messageSource.getMessage(key, null, defaultMessage, LocaleContextHolder.getLocale());
    }

    /**
     * Get localized message with arguments and default value
     */
    public String getMessage(String key, String defaultMessage, Object... args) {
        return messageSource.getMessage(key, args, defaultMessage, LocaleContextHolder.getLocale());
    }

    /**
     * Get current locale
     */
    public String getCurrentLocale() {
        return LocaleContextHolder.getLocale().getLanguage();
    }

    /**
     * Check if current locale is Arabic
     */
    public boolean isArabic() {
        return "ar".equals(getCurrentLocale());
    }

    /**
     * Check if current locale is English
     */
    public boolean isEnglish() {
        return "en".equals(getCurrentLocale());
    }
} 