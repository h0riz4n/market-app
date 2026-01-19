package ru.yandex.market_app.security;

import java.text.ParseException;
import java.util.UUID;

import org.springframework.security.oauth2.client.userinfo.DefaultReactiveOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.ReactiveOAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import ru.yandex.market_app.service.UserService;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuthUserService implements ReactiveOAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserService userService;
    private final ReactiveOAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultReactiveOAuth2UserService();

    @Override
    public Mono<OAuth2User> loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        UUID userId;

        try {
            JWT jwt = JWTParser.parse(userRequest.getAccessToken().getTokenValue());
            JWTClaimsSet claims = jwt.getJWTClaimsSet();
            userId = UUID.fromString(claims.getSubject());
        } catch (ParseException ex) {
            throw new OAuth2AuthenticationException(ex.getLocalizedMessage());
        }

        return delegate.loadUser(userRequest)
            .flatMap(oauth2User -> {
                return userService.processUser(userId)
                    .thenReturn(oauth2User);
            });
    }
}
