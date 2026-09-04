package org.craft.backend.exceptions;

public class PasswordResetTokenExpiredException extends RuntimeException {
  public PasswordResetTokenExpiredException(String message) {
    super(message);
  }
}
