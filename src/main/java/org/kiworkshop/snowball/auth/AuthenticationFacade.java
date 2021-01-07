package org.kiworkshop.snowball.auth;

import org.kiworkshop.snowball.user.entity.User;
import org.kiworkshop.snowball.user.entity.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
public class AuthenticationFacade implements IAuthenticationFacade {
    @Autowired
    UserRepository userRepository;

    @Override
    public Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    @Override
    public DefaultOAuth2User getPrincipal() {
        return (DefaultOAuth2User) getAuthentication().getPrincipal();
    }

    @Override
    public User getUser() {
        return userRepository.findByEmail(getPrincipal().getAttribute("email")).orElseThrow(() -> new IllegalStateException("유저 attribute가 존재하지 않습니다."));
    }
}