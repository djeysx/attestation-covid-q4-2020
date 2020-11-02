package org.djeysx.attestation_covid_q4_2020.security;

import java.util.Collection;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

@SuppressWarnings("serial")
public class UserProfileAuthenticationToken extends UsernamePasswordAuthenticationToken {

    public UserProfileAuthenticationToken(Object principal, Object credentials, UserProfile userProfile) {
        super(principal, credentials);
        this.setDetails(userProfile);
    }

    public UserProfileAuthenticationToken(Object principal, Object credentials,
            Collection<? extends GrantedAuthority> authorities, UserProfile userProfile) {
        super(principal, credentials, authorities);
        this.setDetails(userProfile);
    }

    @Override
    public UserProfile getDetails() {
        return (UserProfile) super.getDetails();
    }
}
