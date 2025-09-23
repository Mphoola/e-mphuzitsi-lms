package com.mphoola.e_empuzitsi.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mphoola.e_empuzitsi.util.ApiResponse;
import com.mphoola.e_empuzitsi.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    private final ObjectMapper objectMapper;
    
    public JwtAuthenticationFilter(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService, ObjectMapper objectMapper) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.objectMapper = objectMapper;
    }
    
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        try {
            String jwt = getJwtFromRequest(request);
            
            if (StringUtils.hasText(jwt)) {
                try {
                    // Try to validate and extract username
                    String username = jwtUtil.getUsernameFromToken(jwt);
                    
                    // Load user details
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    
                    // Create authentication token
                    UsernamePasswordAuthenticationToken authentication = 
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());
                    
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    // Set authentication in security context
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    log.debug("Successfully authenticated user: {} for path: {}", 
                             username, request.getRequestURI());
                    
                } catch (ExpiredJwtException ex) {
                    log.warn("Expired JWT token detected for path: {} - {}", 
                            request.getRequestURI(), ex.getMessage());
                    
                    // Handle expired JWT directly in the filter
                    handleExpiredJwtException(response, ex);
                    return; // Stop further processing
                } catch (Exception ex) {
                    log.warn("Invalid JWT token for path: {} - {}", 
                            request.getRequestURI(), ex.getMessage());
                    // For other JWT errors, just log and continue without authentication
                }
            }
        } catch (Exception ex) {
            log.error("Cannot set user authentication: {}", ex.getMessage(), ex);
            // Clear security context on authentication failure
            SecurityContextHolder.clearContext();
        }
        
        filterChain.doFilter(request, response);
    }
    
    /**
     * Handle expired JWT exception by setting appropriate response
     */
    private void handleExpiredJwtException(HttpServletResponse response, ExpiredJwtException ex) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        
        Map<String, Object> errorResponse = ApiResponse.unauthorized("Token has expired. Please login again.").getBody();
        
        String jsonResponse = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }
    
    /**
     * Extract JWT token from request header
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Remove "Bearer " prefix
        }
        
        return null;
    }
}
