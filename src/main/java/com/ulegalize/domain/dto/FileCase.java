package com.ulegalize.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class FileCase {

  @Getter
  @Setter
  private String container;


  @Getter
  @Setter
  private String folderPath;

  @Getter
  @Setter
  private String recUser;

}