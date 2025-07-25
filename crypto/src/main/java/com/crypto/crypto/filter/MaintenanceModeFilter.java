package com.crypto.crypto.filter;

import com.crypto.crypto.dto.AdminDTOs;
import com.crypto.crypto.service.AdminSettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class MaintenanceModeFilter implements Filter {
    
    @Autowired
    private AdminSettingsService adminSettingsService;
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // Skip maintenance check for admin endpoints and auth
        String requestURI = httpRequest.getRequestURI();
        if (requestURI.startsWith("/api/admin") || 
            requestURI.startsWith("/api/auth") ||
            requestURI.startsWith("/api/test")) {
            chain.doFilter(request, response);
            return;
        }
        
        try {
            AdminDTOs.AdminSettingsResponse settings = adminSettingsService.getAdminSettings();
            if (settings.isMaintenanceMode()) {
                httpResponse.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
                httpResponse.setContentType("application/json");
                httpResponse.getWriter().write(
                    "{\"error\":\"Maintenance Mode\",\"message\":\"Platform is under maintenance. Please try again later.\"}"
                );
                return;
            }
        } catch (Exception e) {
            // If we can't check maintenance mode, allow request to proceed
        }
        
        chain.doFilter(request, response);
    }
} 