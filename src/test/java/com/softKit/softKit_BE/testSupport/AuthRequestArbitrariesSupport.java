package com.softKit.softKit_BE.testSupport;

import net.jqwik.api.*;

public abstract class AuthRequestArbitrariesSupport extends PasswordArbitrariesSupport {

	@Provide
	public Arbitrary<String> validFullNameUpTo200_required() {
		// reaproveita do UserFieldArbitrariesSupport, mas garante não-null
		return validFullNameUpTo200().filter(s -> s != null);
	}

	@Provide
	public Arbitrary<String> validEmailUpTo150_required() {
		return validEmailsUpTo150().filter(s -> s != null);
	}

	@Provide
	public Arbitrary<String> phoneNullableUpTo20() {
		// phone é opcional em RegisterRequest
		return phoneUpTo20().injectNull(0.40);
	}

	@Provide
	public Arbitrary<String> password_required_valid() {
		return validPasswords().filter(s -> s != null && !s.isBlank());
	}
}