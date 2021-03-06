package com.pharmacy.cpis.util.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.pharmacy.cpis.util.security.TokenUtils;
import com.pharmacy.cpis.util.security.auth.RestAuthenticationEntryPoint;
import com.pharmacy.cpis.util.security.auth.TokenAuthenticationFilter;
import com.pharmacy.cpis.util.security.auth.UserAccountDetailsService;

// Enable annotation "@Pre*" & "@Post*" which will check authorization for every method access (with that annotation ofc)
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    // Implement PasswordEncoder using the BCrypt hashing function.
    // BCrypt defaults to 10 rounds of forward value hashing.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Service which is used for reading data about application users
    @Autowired
    private UserAccountDetailsService jwtUserDetailsService;

    // Handler for returning 401, when client with an incorrect username and password tries to access a resource
    @Autowired
    private RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    // We register an authentication manager who will do user authentication for us
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    // Define instructions for the authentication manager which service to use to extract data about the user who wants to be authenticated,
    // as well as through which encoder to pass the password received from the client in the request to compare the adequate hash obtained as a result of the bcrypt algorithm with the one in the database (since no plain password is stored in the database)
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(jwtUserDetailsService).passwordEncoder(passwordEncoder());
    }

    // We inject an implementation from the TokenUtils class so we can use its methods to work with JWT in the TokenAuthenticationFilter
    @Autowired
    private TokenUtils tokenUtils;

    // We define access rights to specific URLs
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                // Communication between the client and the server is stateless since it is a REST application
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()

                // Process all unauthenticated requests uniformly and send a 401 error
                .exceptionHandling().authenticationEntryPoint(restAuthenticationEntryPoint).and()

                // Allow all users to access defined paths (/h2-console/** if we are use temporary DB instead postgres )
                .authorizeRequests()
                .antMatchers("/auth/**").permitAll()
                .antMatchers("/h2-console/**").permitAll()
                .antMatchers("/api/pharmacies/all").permitAll()
                // Allow only this methods from api drugs, because we need that for guest
                .antMatchers("/api/drugs").permitAll()
                .antMatchers("/api/drugs/availabledrugs").permitAll()
                .antMatchers("/api/drugs/types").permitAll()
                .antMatchers("/api/drugs/forms").permitAll()

                //TODO:delete this permission
                .antMatchers("/api/consultations/**").permitAll()

                // For every other request the user must be authenticated
                .anyRequest().authenticated().and()
                // For development purposes include configuration for CORS from the WebConfig class
                .cors().and()

                // Insert custom filter TokenAuthenticationFilter to check JWT tokens instead of pure username and password (performed by BasicAuthenticationFilter)
                .addFilterBefore(new TokenAuthenticationFilter(tokenUtils, jwtUserDetailsService),
                        BasicAuthenticationFilter.class);
        // Due to the simplicity of the example
        http.csrf().disable();
    }

    // General security of the application
    @Override
    public void configure(WebSecurity web) throws Exception {
        // TokenAuthenticationFilter will ignore everything below the specified path
//        web.ignoring().antMatchers(HttpMethod.POST ,"/auth/**");
        web.ignoring().antMatchers(HttpMethod.GET, "/", "/webjars/**", "/*.html", "/favicon.ico", "/**/*.html",
                "/**/*.css", "/**/*.js");
    }

}
