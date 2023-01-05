package com.chatapp.config.auth.firebase;

import com.chatapp.exception.FirebaseUserNotFoundException;
import com.chatapp.service.FirebaseUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class FirebaseAuthProvider implements AuthenticationProvider {

    private FirebaseUserDetailsService firebaseUserDetailsService;

    @Autowired
    public FirebaseAuthProvider(FirebaseUserDetailsService firebaseUserDetailsService) {
        this.firebaseUserDetailsService = firebaseUserDetailsService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (!supports(authentication.getClass())) {
            return null;
        }

        FirebaseAuthenticationToken authenticationToken = (FirebaseAuthenticationToken) authentication;
        UserDetails details = firebaseUserDetailsService.loadUserByUsername(authenticationToken.getName());
        if (details == null) {
            throw new FirebaseUserNotFoundException();
        }

        authenticationToken = new FirebaseAuthenticationToken(details, authentication.getCredentials(),
                details.getAuthorities());

        return authenticationToken;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return (FirebaseAuthenticationToken.class.isAssignableFrom(authentication));
    }
}
