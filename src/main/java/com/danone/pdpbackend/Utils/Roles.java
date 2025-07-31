package com.danone.pdpbackend.Utils;

import org.springframework.security.core.GrantedAuthority;

public enum Roles implements GrantedAuthority {
    ADMIN,
    REU,
    REE,
    USER;

    @Override
    public String getAuthority() {
        return name();
    }
}
