package com.chatapp.service.implementation;

import com.chatapp.service.FirebaseUserDetailsService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class FirebaseUserDetailsServiceImpl implements FirebaseUserDetailsService {

    private final FirebaseAuth firebaseAuth;

    @Autowired
    public FirebaseUserDetailsServiceImpl(FirebaseAuth firebaseAuth) {
        this.firebaseAuth = firebaseAuth;
    }

    @Override
    public UserDetails loadUserByUsername(String uid) throws UsernameNotFoundException {
        try {
            UserRecord userRecord = firebaseAuth.getUser(uid);
            Map<String, Object> claims = userRecord.getCustomClaims();

            Set<GrantedAuthority> grantedClaims = new HashSet<>();
            for (String claim : claims.keySet()) {
                grantedClaims.add(new SimpleGrantedAuthority(claim));
            }

            return new User(userRecord.getUid(), userRecord.getEmail(), grantedClaims);
        } catch (FirebaseAuthException e) {
            throw new UsernameNotFoundException(e.getMessage());
        }

    }
}
