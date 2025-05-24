package com.bookstore.Security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final UserDetailService userDetailService;


    private final MyBasicAuthenticationEntryPoint myBasicAuthenticationEntryPoint;

    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    private final PasswordEncoder passwordEncoder;

    @Bean
    public JwtAuthenticationFilter JwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(){
        final DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setPasswordEncoder(passwordEncoder);
        authenticationProvider.setUserDetailsService(userDetailService);
        return authenticationProvider;
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("success", false);
            errorMap.put("message", "Forbidden");
            errorMap.put("result", "Access denied: Insufficient permissions!");
            errorMap.put("statusCode", "403");
            ObjectMapper objectMapper = new ObjectMapper();
            response.getWriter().write(objectMapper.writeValueAsString(errorMap));
            response.getWriter().flush();
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/auth/**",
                                         "/payment-return",
                                            "/api/authors/**",
                                        "/api/books/**",
                                "/api/categories/**",
                                "/api/distributors/**",
                                "/api/publishers/**",
                                "/api/reviews/**").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/employee/**").hasAnyRole("ADMIN", "EMPLOYEE")
                        .requestMatchers("/api/shipper/**").hasAnyRole("ADMIN", "SHIPPER")
                        .anyRequest().authenticated()
                )
                .sessionManagement(sess -> sess
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .httpBasic(basic -> basic
                        .authenticationEntryPoint(myBasicAuthenticationEntryPoint)
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(myBasicAuthenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler())
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(JwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .cors(cors -> cors
                        .configurationSource(corsConfigurationSource())
                )
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(auth -> auth
                                .baseUri("/api/auth/oauth2/authorization")
                        )
                        .redirectionEndpoint(redirection -> redirection
                                .baseUri("/api/auth/callback/*")
                        )
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                );

        return http.build();
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        final CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("https://erotskoob.xyz", "https://www.erotskoob.xyz",
                "https://shipper.erotskoob.xyz", "https://www.shipper.erotskoob.xyz",
                "https://admin.erotskoob.xyz", "https://www.admin.erotskoob.xyz",
                "https://employee.erotskoob.xyz", "https://www.employee.erotskoob.xyz",
                "http://localhost:3006", "http://localhost:3000", "http://localhost:3010", "http://localhost:3020"));
        configuration.setAllowedMethods(List.of("HEAD", "GET", "POST", "PUT", "DELETE", "PATCH"));
        configuration.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type"));
        configuration.setAllowCredentials(true); // Bật credentials
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}