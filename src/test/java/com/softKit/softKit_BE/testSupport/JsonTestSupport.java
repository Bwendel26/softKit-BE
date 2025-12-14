package com.softKit.softKit_BE.testSupport;

import com.fasterxml.jackson.databind.ObjectMapper;

public final class JsonTestSupport {

	private JsonTestSupport() {}

	public static final ObjectMapper OBJECT_MAPPER =
			new ObjectMapper().findAndRegisterModules();
}
