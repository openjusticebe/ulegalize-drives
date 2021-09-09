package com.ulegalize.service.exception;

public class RestException extends Throwable {
  public RestException(String message) {
    super(message);
  }

  public RestException(String message, Exception e) {
    super(message, e);
  }
}
