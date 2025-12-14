package com.softKit.softKit_BE.testSupport;

import org.springframework.security.core.userdetails.UserDetails;

public final class SecurityTestUsers {
	private SecurityTestUsers() {}

	public static UserDetails user(String username, String... roles) {
		return org.springframework.security.core.userdetails.User
				.withUsername(username)
				.password("x")
				.authorities(roles)
				.build();
	}
}
