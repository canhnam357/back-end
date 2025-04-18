package com.bookstore.Security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
        if (jwtEx instanceof ExpiredJwtException) {
            resultMessage = "Token is expired. Please login again!";
        } else if (jwtEx instanceof MalformedJwtException) {
            resultMessage = "Invalid JWT token. Please login again!";
        } else if (jwtEx instanceof SignatureException) {
            resultMessage = "Invalid JWT signature. Please login again!";
        } else if (jwtEx != null) {
            resultMessage = jwtEx.getMessage();
        }

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        Map<String, Object> errorMap = new HashMap<>();
        errorMap.put("success", false);
        errorMap.put("message", "Access Denied");
        errorMap.put("result", resultMessage);
        errorMap.put("statusCode", "401");

        ObjectMapper objectMapper = new ObjectMapper();
        String errorJson = objectMapper.writeValueAsString(errorMap);

        response.getWriter().write(errorJson);
        response.getWriter().flush();
    }
}
