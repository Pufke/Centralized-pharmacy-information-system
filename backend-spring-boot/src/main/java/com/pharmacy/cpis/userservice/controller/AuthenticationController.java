package com.pharmacy.cpis.userservice.controller;

import com.pharmacy.cpis.userservice.model.users.Authority;
import com.pharmacy.cpis.userservice.model.users.Patient;
import com.pharmacy.cpis.userservice.model.users.UserAccount;
import com.pharmacy.cpis.userservice.service.*;
import com.pharmacy.cpis.userservice.dto.JwtAuthenticationRequest;
import com.pharmacy.cpis.userservice.dto.UserActivationDTO;
import com.pharmacy.cpis.userservice.dto.UserRegisterDTO;
import com.pharmacy.cpis.userservice.dto.UserTokenState;
import com.pharmacy.cpis.userservice.service.impl.PatientRegistrationService;
import com.pharmacy.cpis.util.security.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

// Controller in charge of user authentication
@RestController
@RequestMapping(value = "auth")
public class AuthenticationController {

    @Autowired
    private Environment env;

    @Autowired
    private TokenUtils tokenUtils;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private IUserService userService;

    @Autowired
    private IPatientRegistrationService patientRegistrationService;

    // The first endpoint that affects the user when logging in.
    // Then he only knows his username and password and forwards it to the backend.
    @PostMapping("/login")
    public ResponseEntity<UserTokenState> createAuthenticationToken(@RequestBody JwtAuthenticationRequest authenticationRequest,
                                                                    HttpServletResponse response) {

        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(authenticationRequest.getEmail(),
                        authenticationRequest.getPassword()));

        // Insert the user into the current security context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Create a token for that user
        UserAccount user = (UserAccount) authentication.getPrincipal();
        String jwt = tokenUtils.generateToken(user.getUsername(), userService.getUserRole(user));
        int expiresIn = tokenUtils.getExpiredIn();

        // Return the token in response to successful authentication
        return ResponseEntity.ok(new UserTokenState(jwt, expiresIn));
    }

    // Endpoint to register a new user
    @PostMapping("/signup")
    public ResponseEntity<UserAccount> addUser(@RequestBody UserRegisterDTO userRequest) {
        if (patientRegistrationService.existsByEmail(userRequest.getEmail()))
            return new ResponseEntity<>(HttpStatus.CONFLICT);

        UserAccount addedAccount = patientRegistrationService.registerPatient(userRequest);
        return new ResponseEntity<>(addedAccount, HttpStatus.CREATED);
    }

    // GET is because of easy click-activate method
    @GetMapping("activate/{id}")
    public void activateAccount(@PathVariable Long id, HttpServletResponse httpServletResponse) {
        userService.activatePatientAccount(id);
        redirectToLoginPage(httpServletResponse);
    }

    private void redirectToLoginPage(HttpServletResponse httpServletResponse) {
        String projectUrl = env.getProperty("APP_FRONT") + env.getProperty("FRONT_PORT") + "/";
        httpServletResponse.setHeader("Location", projectUrl);
        httpServletResponse.setStatus(302);
    }

}