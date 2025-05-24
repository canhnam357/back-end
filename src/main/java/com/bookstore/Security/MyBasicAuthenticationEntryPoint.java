package com.bookstore.Security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class MyBasicAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        Throwable jwtEx = (Throwable) request.getAttribute("jwt_exception");
        String resultMessage = "Please login again!";
        int statusCode = HttpServletResponse.SC_UNAUTHORIZED;

        if (jwtEx instanceof ExpiredJwtException) {
            resultMessage = "Token is expired. Please use refresh token or login again!";
        } else if (jwtEx instanceof MalformedJwtException) {
            resultMessage = "Invalid JWT token format. Please login again!";
        } else if (jwtEx instanceof SignatureException) {
            resultMessage = "Invalid JWT signature. Please login again!";
        } else if (jwtEx instanceof AccessDeniedException) {
            resultMessage = "Access denied: Insufficient permissions!";
            statusCode = HttpServletResponse.SC_FORBIDDEN;
        } else if (jwtEx != null) {
            resultMessage = jwtEx.getMessage();
        }

        response.setContentType("application/json");
        response.setStatus(statusCode);

        Map<String, Object> errorMap = new HashMap<>();
        errorMap.put("success", false);
        errorMap.put("message", statusCode == HttpServletResponse.SC_FORBIDDEN ? "Forbidden" : "Access Denied");
        errorMap.put("result", resultMessage);
        errorMap.put("statusCode", String.valueOf(statusCode));

        ObjectMapper objectMapper = new ObjectMapper();
        String errorJson = objectMapper.writeValueAsString(errorMap);
        response.getWriter().write(errorJson);
        response.getWriter().flush();
    }
}