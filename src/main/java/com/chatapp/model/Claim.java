package com.chatapp.model;

import org.springframework.security.core.GrantedAuthority;

public class Claim implements GrantedAuthority {

    private String authority;

    public Claim(String authority) {
        this.authority = authority;
    }

    @Override
    public String getAuthority() {
        return authority;
    }
}
