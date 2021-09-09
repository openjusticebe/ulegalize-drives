package com.ulegalize.service.user;

import com.ulegalize.domain.dto.User;

public interface UserService {
  public User findByUsername(String username);
}