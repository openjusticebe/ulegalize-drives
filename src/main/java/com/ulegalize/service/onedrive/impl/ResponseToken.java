package com.ulegalize.service.onedrive.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@AllArgsConstructor
public class ResponseToken implements Serializable {
  @Getter
  @Setter
  private String token_type;
  @Getter
  @Setter
  private String expires_in;
  @Getter
  @Setter
  private String scope;
  @Getter
  @Setter
  private String access_token;
  @Getter
  @Setter
  private String refresh_token;
  @Getter
  @Setter
  private String id_token;

  public ResponseToken() {
  }
}
