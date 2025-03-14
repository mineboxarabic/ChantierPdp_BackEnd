package com.danone.pdpbackend.Utils;

import org.springframework.security.core.GrantedAuthority;

public enum Roles implements GrantedAuthority {
    ADMIN,
    WORKER,
    REU,
    REE;

    @Override
    public String getAuthority() {
        return name();
    }
}
