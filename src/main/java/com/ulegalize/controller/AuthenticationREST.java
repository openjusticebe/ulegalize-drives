package com.ulegalize.controller;


import com.ulegalize.domain.dto.LawfirmToken;
import com.ulegalize.domain.dto.User;
import com.ulegalize.security.JWTUtil;
import com.ulegalize.security.PBKDF2Encoder;
import com.ulegalize.security.model.AuthRequest;
import com.ulegalize.security.model.AuthResponse;
import com.ulegalize.service.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class AuthenticationREST {

  @Autowired
  private JWTUtil jwtUtil;

  @Autowired
  private PBKDF2Encoder passwordEncoder;

  @Autowired
  private UserService userService;


  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest authRequest) {
    try {
      User u = userService.findByUsername(authRequest.getUsername());
      if (u != null) {
        CharSequence password = u.getPassword();
        String passwordInput = passwordEncoder.encode(authRequest.getPassword());
        if (password.equals(passwordInput)) {
          return ResponseEntity.ok(new AuthResponse(jwtUtil.generateToken(u)));
        } else {
          log.error("password not match");
          return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
      } else {
        log.error("user null");
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
      }

    } catch (Exception e) {
      log.error("error", e);
      return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
  }

  @PostMapping("/authentication")
  public ResponseEntity<AuthResponse> authentificate(Authentication authentication) {
    try {

      LawfirmToken lawfirmToken = (LawfirmToken) authentication.getPrincipal();
      return ResponseEntity.ok(new AuthResponse(lawfirmToken.getVcKey()));

    } catch (Exception e) {
      log.error("error", e);
      return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
  }

}
