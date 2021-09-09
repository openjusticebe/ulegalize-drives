package com.ulegalize.service.user.impl;

import com.ulegalize.domain.dto.User;
import com.ulegalize.security.model.Role;
import com.ulegalize.service.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Slf4j
@Service
public class UserServiceImpl implements UserService {


  // this is just an example, you can load the user from the database from the repository
  public User findByUsername(String username) {
    String userUsername = "user";

    //generated from password encoder VAFFA
    String userPassword = "PXHzloY6OF9t1IDmwUnbt376dgq6E3ODrPib/V1HW/U=";

    String adminUsername = "admin";

    //generated from password encoder
    String adminPassword = "PXHzloY6OF9t1IDmwUnbt376dgq6E3ODrPib/V1HW/U=";

    if (username.equals(userUsername)) {
      return new User(userUsername, userPassword, true, Arrays.asList(Role.ROLE_USER));
    } else if (username.equals(adminUsername)) {
      return new User(adminUsername, adminPassword, true, Arrays.asList(Role.ROLE_ADMIN));
    } else {
      return null;
    }
  }


}
