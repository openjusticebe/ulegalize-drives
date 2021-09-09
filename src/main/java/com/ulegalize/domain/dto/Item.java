package com.ulegalize.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class Item {

  @Getter
  @Setter
  private Integer value;


  @Getter
  @Setter
  private String label;

}