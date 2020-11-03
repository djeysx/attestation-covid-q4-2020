package org.djeysx.attestation_covid_q4_2020.security;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.google.common.base.Verify;
import com.google.common.collect.ImmutableList;

@Component
public class ProfileAuthenticationProvider implements AuthenticationProvider, UserDetailsService {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("${auth.profiles.path}")
    protected Path profilesPath;

    @PostConstruct
    public void init() {
        Verify.verify(Files.isDirectory(profilesPath), "Dossier profiles absent [%s]", profilesPath);
        log.info("init OK [{}]", profilesPath);
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        try {
            String name = authentication.getName();
            String password = authentication.getCredentials().toString();
            log.info("Try Login [{}:{}]", name, password);
            UserProfile userProfile = readUserProfile(name);

            Verify.verify(userProfile.getPassword().equals(password), "Authentication failed");

            return new UserProfileAuthenticationToken(name, null, ImmutableList.of(), null);
        } catch (RuntimeException e) {
            throw new BadCredentialsException("Access Denied", e);
        }
    }

    public UserProfile readUserProfile(String name) {
        checkUsernameFileValidity(name);
        log.debug("read user[{}]", name);
        Path profilePath = profilesPath.resolve(name + ".properties");
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(profilePath)) {
            props.load(in);
            UserProfile userProfile = new UserProfile();
            userProfile.nom = checkNotNull(props.getProperty("nom"), "profile manque attr [nom]");
            userProfile.prenom = checkNotNull(props.getProperty("prenom"), "profile manque attr [prenom]");
            userProfile.dateNaissance = checkNotNull(props.getProperty("dateNaissance"), "profile manque attr [dateNaissance]");
            userProfile.lieuNaissance = checkNotNull(props.getProperty("lieuNaissance"), "profile manque attr [lieuNaissance]");
            userProfile.adresse = checkNotNull(props.getProperty("adresse"), "profile manque attr [adresse]");
            userProfile.ville = checkNotNull(props.getProperty("ville"), "profile manque attr [ville]");
            userProfile.codePostal = checkNotNull(props.getProperty("codePostal"), "profile manque attr [codePostal]");
            return userProfile;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    protected void checkUsernameFileValidity(String name) {
        checkNotNull(name, "name is null");
        Verify.verify(name.length() >= 2, "name size too short");
        for (int pos = 0, len = name.length(); pos < len; pos++) {
            char c = name.charAt(pos);
            Verify.verify((c >= 'a' && c <= 'z') || c == '.', "name character forbidden");
        }
        Path profilePath = profilesPath.resolve(name + ".properties");
        Verify.verify(Files.isRegularFile(profilePath), "Profile not found");
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserProfile userProfile;
        try {
            userProfile = readUserProfile(username);
        } catch (Exception e) {
            throw new UsernameNotFoundException("No user", e);
        }

        return new UserDetails() {
            private static final long serialVersionUID = 1317940575014416813L;

            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return null;
            }

            @Override
            public String getPassword() {
                return userProfile.getPassword();
            }

            @Override
            public String getUsername() {
                return username;
            }

            @Override
            public boolean isAccountNonExpired() {
                return true;
            }

            @Override
            public boolean isAccountNonLocked() {
                return true;
            }

            @Override
            public boolean isCredentialsNonExpired() {
                return true;
            }

            @Override
            public boolean isEnabled() {
                return true;
            }
        };
    }
}
