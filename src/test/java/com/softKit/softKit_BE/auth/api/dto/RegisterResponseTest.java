package com.softKit.softKit_BE.auth.api.dto;

import com.softKit.softKit_BE.testSupport.JsonTestSupport;
import com.softKit.softKit_BE.testSupport.UserResponseArbitrariesSupport;
import net.jqwik.api.*;

import static org.assertj.core.api.Assertions.assertThat;

class RegisterResponseTest extends UserResponseArbitrariesSupport {

	@Provide
	Arbitrary<RegisterResponse> registerResponses() {
		return Combinators.combine(
				uuids(),
				nameLike(),
				safeEmailLikeNullable(),
				phoneLike(),
				nullableOffsetDateTimeUtc(),
				nullableOffsetDateTimeUtc()
		).as(RegisterResponse::new
		);
	}

	@Property(tries = 200)
	void recordStoresValuesCorrectly(@ForAll("registerResponses") RegisterResponse registerResponse) {
		RegisterResponse copy = new RegisterResponse(
				registerResponse.id(),
				registerResponse.fullName(),
				registerResponse.email(),
				registerResponse.phoneE164(),
				registerResponse.createdAt(),
				registerResponse.updatedAt()
		);

		assertThat(copy).isEqualTo(registerResponse);
	}

	@Property(tries = 150)
	void equalsAndHashCode_sameValues_areEqual(@ForAll("registerResponses") RegisterResponse registerResponse) {
		RegisterResponse firstResponse = new RegisterResponse(
				registerResponse.id(),
				registerResponse.fullName(),
				registerResponse.email(),
				registerResponse.phoneE164(),
				registerResponse.createdAt(),
				registerResponse.updatedAt()
		);

		RegisterResponse secondResponse = new RegisterResponse(
				registerResponse.id(),
				registerResponse.fullName(),
				registerResponse.email(),
				registerResponse.phoneE164(),
				registerResponse.createdAt(),
				registerResponse.updatedAt()
		);

		assertThat(firstResponse).isEqualTo(secondResponse);
		assertThat(firstResponse.hashCode()).isEqualTo(secondResponse.hashCode());
	}

	@Property(tries = 150)
	void jacksonRoundTrip_preservesValues(@ForAll("registerResponses") RegisterResponse registerResponse) throws Exception {
		String json = JsonTestSupport.OBJECT_MAPPER.writeValueAsString(registerResponse);
		RegisterResponse back = JsonTestSupport.OBJECT_MAPPER.readValue(json, RegisterResponse.class);

		assertThat(back).isEqualTo(registerResponse);
	}

	@Example
	void nullsAreAllowed_everywhere() throws Exception {
		RegisterResponse registerResponse = new RegisterResponse(null, null, null, null, null, null);

		String json = JsonTestSupport.OBJECT_MAPPER.writeValueAsString(registerResponse);
		RegisterResponse back = JsonTestSupport.OBJECT_MAPPER.readValue(json, RegisterResponse.class);

		assertThat(back).isEqualTo(registerResponse);
	}
}