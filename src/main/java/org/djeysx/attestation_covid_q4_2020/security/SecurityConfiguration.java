package org.djeysx.attestation_covid_q4_2020.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http //
             // .authorizeRequests().antMatchers("/*").anonymous()//
             // .and()//
                .authorizeRequests().antMatchers("/**").authenticated()//
                .and()//
                .formLogin().permitAll()//
                .and()//
                .logout().permitAll()//
                .and()//
                .rememberMe().key("QPWOEIRUTZ").tokenValiditySeconds(60 * 60 * 24 * 90)// 90 jours
                .userDetailsService(authProvider)
                .alwaysRemember(true)//
                .and()//
                .csrf().disable()//
        ;
    }

    @Autowired
    protected ProfileAuthenticationProvider authProvider;

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authProvider);
    }
    
}
