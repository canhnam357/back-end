package com.bookstore.Security;

import com.bookstore.Entity.RefreshToken;
import com.bookstore.Entity.User;
import com.bookstore.Repository.UserRepository;
import com.bookstore.Service.RefreshTokenService;
import com.bookstore.Service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserService userService;

    private final JwtTokenProvider jwtTokenProvider;

    private final RefreshTokenService refreshTokenService;

    private final UserRepository userRepository;

    @Value("${user-url}")
    private String userUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        System.err.println("HERE");

        // find or create user in database
        User user = userService.findOrCreateUser(email, name);

        if (!user.isActive()) {
            String errorRedirectUrl = userUrl + "/callback?error=account_locked";
            getRedirectStrategy().sendRedirect(request, response, errorRedirectUrl);
            return;
        }

        user.setVerified(true);
        userRepository.save(user);

        // Tạo UserDetail để sinh token
        UserDetail userDetail = new UserDetail(user);

        // Tạo JWT access token
        String accessToken = jwtTokenProvider.generateAccessToken(userDetail);
        String _refreshToken = jwtTokenProvider.generateRefreshToken(userDetail);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(_refreshToken);
        refreshToken.setUser(user);

        refreshTokenService.revokeRefreshToken(userDetail.getUserId());
        refreshTokenService.save(refreshToken);

        // Redirect về front-end với token
        String redirectUrl = userUrl + "/callback?accessToken=" + accessToken +
                "&refreshToken=" + refreshToken.getToken();
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
