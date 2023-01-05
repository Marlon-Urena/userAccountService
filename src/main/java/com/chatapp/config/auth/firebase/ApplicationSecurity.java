package com.chatapp.config.auth.firebase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;


@Configuration
public class ApplicationSecurity extends WebSecurityConfigurerAdapter {

    private final FirebaseFilter firebaseFilter;

    private final FirebaseAuthProvider firebaseAuthProvider;

    @Autowired
    public ApplicationSecurity(FirebaseFilter firebaseFilter, FirebaseAuthProvider firebaseAuthProvider) {
        this.firebaseFilter = firebaseFilter;
        this.firebaseAuthProvider = firebaseAuthProvider;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
            http.cors().and().csrf().disable()
                .addFilterBefore(firebaseFilter, BasicAuthenticationFilter.class)
                .authenticationProvider(firebaseAuthProvider)
                .authorizeRequests()
                .anyRequest().authenticated();

    }
}