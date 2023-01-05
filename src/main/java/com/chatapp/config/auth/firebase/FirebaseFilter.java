package com.chatapp.config.auth.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class FirebaseFilter extends OncePerRequestFilter {

    private static final String HEADER_NAME = "Authorization";

    private FirebaseAuth firebaseAuth;

    @Autowired
    public FirebaseFilter(FirebaseAuth firebaseAuth) {
        this.firebaseAuth = firebaseAuth;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader(HEADER_NAME);
        String idToken = getBearerToken(authHeader);
        try {
            FirebaseToken firebaseToken = firebaseAuth.verifyIdToken(idToken);

            String id = firebaseToken.getUid();

            Authentication auth = new FirebaseAuthenticationToken(id, firebaseToken);
            SecurityContextHolder.getContext().setAuthentication(auth);

            filterChain.doFilter(request, response);

        } catch (FirebaseAuthException e) {
            throw new SecurityException(e);
        }
    }

    private String getBearerToken(String authHeader) {
        String bearerToken = "";
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            bearerToken = authHeader.substring(7);
        }
        return bearerToken;
    }
}
