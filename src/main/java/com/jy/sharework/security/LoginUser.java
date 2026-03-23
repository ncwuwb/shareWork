package com.jy.sharework.security;

import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collections;

@Getter
public class LoginUser extends User {
    private final Long userId;
    private final Integer roleType;

    public LoginUser(Long userId, String phone, Integer roleType, String password, boolean enabled) {
        super(phone, password == null ? "" : password,
                enabled,
                true, true, true,
                Collections.singletonList(new SimpleGrantedAuthority(roleType == 1 || roleType == 2 ? "ROLE_ADMIN" : "ROLE_USER")));
        this.userId = userId;
        this.roleType = roleType;
    }

    public boolean isSuperAdmin() {
        return roleType != null && roleType == 1;
    }

    public boolean isAreaAdmin() {
        return roleType != null && roleType == 2;
    }
}
