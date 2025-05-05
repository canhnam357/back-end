package com.bookstore.Security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authorization.method.AuthorizeReturnObject;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {

    @Autowired
    private UserDetailService userDetailService;


    @Autowired
    private MyBasicAuthenticationEntryPoint myBasicAuthenticationEntryPoint;


    @Autowired
    private OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
                        .redirectionEndpoint(redir -> redir
                                .baseUri("/api/auth/callback/*")
                        )
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                );

        return http.build();
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        final CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3006", "http://localhost:3000", "http://localhost:3010", "http://localhost:3020")); // Chỉ định rõ origin
        configuration.setAllowedMethods(List.of("HEAD", "GET", "POST", "PUT", "DELETE", "PATCH"));
        configuration.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type"));
        configuration.setAllowCredentials(true); // Bật credentials
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}