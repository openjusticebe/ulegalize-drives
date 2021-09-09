package com.ulegalize.service.onedrive.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@AllArgsConstructor
public class FormToken implements Serializable {
  @Getter
  @Setter
  private String client_id;
  @Getter
  @Setter
  private String redirect_uri;
  @Getter
  @Setter
  private String client_secret;
  @Getter
  @Setter
  private String code;
  @Getter
  @Setter
  private String grant_type;

}
