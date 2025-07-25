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
    
    public String getMessage(String code) {
        return getMessage(code, null, null);
    }
    
    public String getMessage(String code, Object[] args) {
        return getMessage(code, args, null);
    }
    
    public String getMessage(String code, Object[] args, String defaultMessage) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(code, args, defaultMessage, locale);
    }
    
    public String getMessage(String code, String defaultMessage) {
        return getMessage(code, null, defaultMessage);
    }
} 