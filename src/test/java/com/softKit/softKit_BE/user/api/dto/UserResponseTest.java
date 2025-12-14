package com.softKit.softKit_BE.user.api.dto;

import com.softKit.softKit_BE.testSupport.JsonTestSupport;
import com.softKit.softKit_BE.testSupport.UserResponseArbitrariesSupport;
import net.jqwik.api.*;

import static org.assertj.core.api.Assertions.assertThat;

class UserResponseTest extends UserResponseArbitrariesSupport {

	@Property(tries = 200)
	void recordStoresValuesCorrectly(@ForAll("userResponses") UserResponse userResponse) {
		UserResponse copy = new UserResponse(
				userResponse.id(),
				userResponse.fullName(),
				userResponse.email(),
				userResponse.phoneE164(),
				userResponse.role(),
				userResponse.status(),
				userResponse.emailVerifiedAt(),
				userResponse.lastLoginAt(),
				userResponse.createdAt(),
				userResponse.updatedAt()
		);

		assertThat(copy).isEqualTo(userResponse);
	}

	@Property(tries = 150)
	void equalsAndHashCode_sameValues_areEqual(@ForAll("userResponses") UserResponse userResponse) {
		UserResponse firstUser = new UserResponse(
				userResponse.id(), userResponse.fullName(), userResponse.email(), userResponse.phoneE164(),
				userResponse.role(), userResponse.status(),
				userResponse.emailVerifiedAt(), userResponse.lastLoginAt(),
				userResponse.createdAt(), userResponse.updatedAt()
		);

		UserResponse secondUser = new UserResponse(
				userResponse.id(), userResponse.fullName(), userResponse.email(), userResponse.phoneE164(),
				userResponse.role(), userResponse.status(),
				userResponse.emailVerifiedAt(), userResponse.lastLoginAt(),
				userResponse.createdAt(), userResponse.updatedAt()
		);

		assertThat(firstUser).isEqualTo(secondUser);
		assertThat(firstUser.hashCode()).isEqualTo(secondUser.hashCode());
	}

	@Property(tries = 150)
	void jacksonRoundTrip_preservesValues(@ForAll("userResponses") UserResponse u) throws Exception {
		String json = JsonTestSupport.OBJECT_MAPPER.writeValueAsString(u);
		UserResponse back = JsonTestSupport.OBJECT_MAPPER.readValue(json, UserResponse.class);

		assertThat(back).isEqualTo(u);
	}

	@Example
	void nullsAreAllowed_everywhere() throws Exception {
		UserResponse u = new UserResponse(null, null, null, null, null, null, null, null, null, null);

		String json = JsonTestSupport.OBJECT_MAPPER.writeValueAsString(u);
		UserResponse back = JsonTestSupport.OBJECT_MAPPER.readValue(json, UserResponse.class);

		assertThat(back).isEqualTo(u);
	}
}