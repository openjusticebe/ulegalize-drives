package com.ulegalize.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

public class ObjectRequest implements Serializable {
  @Getter
  @Setter
  String obj;
  @Getter
  @Setter
  String id;
  @Getter
  @Setter
  List<String> shared_with;
  @Getter
  @Setter
  String msg;
  @Getter
  @Setter
  String right;
  @Getter
  @Setter
  Integer size;
  @Getter
  @Setter
  String deleted_with;
  @Getter
  @Setter
  String permissionId;

}
