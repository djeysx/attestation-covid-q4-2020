package org.djeysx.attestation_covid_q4_2020;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import com.google.common.base.Verify;
import com.google.common.collect.ImmutableList;

@Component
public class ProfileAuthenticationProvider implements AuthenticationProvider {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("${auth.profiles.path}")
    protected Path profilesPath;

    @PostConstruct
    public void init_AceLdapAuthenticationProvider() {
        Verify.verify(Files.isDirectory(profilesPath), "Dossier profiles absent [%s]", profilesPath);
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String name = authentication.getName();
        String password = authentication.getCredentials().toString();
        checkUsernameFileValidity(name);
        UserProfile userProfile = readUserProfile(name);

        Verify.verify(userProfile.getPassword().equals(password), "Authentication failed");

        return new UserProfileAuthenticationToken(name, null, ImmutableList.of(), userProfile);
    }

    private UserProfile readUserProfile(String name) {
        UserProfile userProfile = new UserProfile();
        Path profilePath = profilesPath.resolve(name + ".properties");
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(profilePath)) {
            props.load(in);
            userProfile.nom = checkNotNull(props.getProperty("nom"), "Le profile ne contient pas [nom]");
            userProfile.prenom = checkNotNull(props.getProperty("nom"), "Le profile ne contient pas [prenom]");
            userProfile.dateNaissance = checkNotNull(props.getProperty("nom"), "Le profile ne contient pas [dateNaissance]");
            userProfile.lieuNaissance = checkNotNull(props.getProperty("nom"), "Le profile ne contient pas [lieuNaissance]");
            userProfile.adresse = checkNotNull(props.getProperty("nom"), "Le profile ne contient pas [adresse]");
            userProfile.ville = checkNotNull(props.getProperty("nom"), "Le profile ne contient pas [ville]");
            userProfile.codePostal = checkNotNull(props.getProperty("nom"), "Le profile ne contient pas [codePostal]");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return userProfile;
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
        return authentication.equals(UserProfileAuthenticationToken.class);
    }

}
