package com.github.wnameless.json.jsonschemadatagenerator;

public class InvalidJsonPathException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public InvalidJsonPathException(String message) {
    super(message);
  }

  public InvalidJsonPathException(String message, Throwable cause) {
    super(message, cause);
  }

}
