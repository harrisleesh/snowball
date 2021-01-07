package org.kiworkshop.snowball.auth;

import org.kiworkshop.snowball.user.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import java.security.Principal;

public interface IAuthenticationFacade {

    Authentication getAuthentication();
    DefaultOAuth2User getPrincipal();
    User getUser();
}
