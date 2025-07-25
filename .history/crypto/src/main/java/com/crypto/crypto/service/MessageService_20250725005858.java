package com.crypto.crypto.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class MessageService {

    @Autowired
    private MessageSource messageSource;

    public String getMessage(String key) {
        return getMessage(key, null);
    }

    public String getMessage(String key, Object[] args) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(key, args, key, locale);
    }

    public String getMessage(String key, Object[] args, String defaultMessage) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(key, args, defaultMessage, locale);
    }

    // Convenience methods for common messages
    public String getSuccessMessage(String operation) {
        return getMessage(operation + ".success");
    }

    public String getErrorMessage(String operation) {
        return getMessage(operation + ".error");
    }

    public String getNotFoundMessage(String entity) {
        return getMessage(entity + ".not_found");
    }

    public String getValidationMessage(String field, String validation) {
        return getMessage("validation." + field + "." + validation);
    }
} 