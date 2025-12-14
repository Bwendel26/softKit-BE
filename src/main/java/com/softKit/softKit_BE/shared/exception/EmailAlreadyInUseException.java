package com.softKit.softKit_BE.shared.exception;

public class EmailAlreadyInUseException extends RuntimeException {

	public EmailAlreadyInUseException(String email) {
		super("Email already in use: " + email);
	}
}
