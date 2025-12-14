package com.softKit.softKit_BE.testSupport;

import com.softKit.softKit_BE.user.api.dto.UserResponse;
import com.softKit.softKit_BE.user.domain.enums.Role;
import com.softKit.softKit_BE.user.domain.enums.Status;
import net.jqwik.api.*;
import net.jqwik.api.Tuple.Tuple8;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

public abstract class UserResponseArbitrariesSupport extends EmailArbitrariesSupport {

	@Provide
	public Arbitrary<UUID> uuids() {
		return Arbitraries.create(UUID::randomUUID).injectNull(0.05);
	}

	@Provide
	public Arbitrary<String> nameLike() {
		char[] chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ ".toCharArray();
		return Arbitraries.strings()
				.withChars(chars)
				.ofMinLength(1)
				.ofMaxLength(200)
				.map(String::trim)
				.filter(s -> !s.isBlank())
				.injectNull(0.05);
	}

	@Provide
	public Arbitrary<String> phoneLike() {
		char[] chars = "+0123456789".toCharArray();
		return Arbitraries.strings()
				.withChars(chars)
				.ofMinLength(0)
				.ofMaxLength(20)
				.injectNull(0.35);
	}

	@Provide
	public Arbitrary<Role> anyRole() {
		return Arbitraries.of(Role.values()).injectNull(0.10);
	}

	@Provide
	public Arbitrary<Status> anyStatus() {
		return Arbitraries.of(Status.values()).injectNull(0.10);
	}

	@Provide
	public Arbitrary<OffsetDateTime> offsetDateTimeUtc() {
		long start = Instant.parse("2000-01-01T00:00:00Z").getEpochSecond();
		long end = Instant.parse("2100-01-01T00:00:00Z").getEpochSecond();

		return Arbitraries.longs()
				.between(start, end)
				.map(sec -> OffsetDateTime.ofInstant(Instant.ofEpochSecond(sec), ZoneOffset.UTC));
	}

	@Provide
	public Arbitrary<OffsetDateTime> nullableOffsetDateTimeUtc() {
		return offsetDateTimeUtc().injectNull(0.40);
	}

	@Provide
	public Arbitrary<UserResponse> userResponses() {

		// 8 primeiros (limite do jqwik)
		Arbitrary<Tuple8<UUID, String, String, String, Role, Status, OffsetDateTime, OffsetDateTime>> first8 =
				Combinators.combine(
						uuids(),
						nameLike(),
						validEmailsUpTo150().injectNull(0.05), // pode ser null no Response
						phoneLike(),
						anyRole(),
						anyStatus(),
						nullableOffsetDateTimeUtc(), // emailVerifiedAt
						nullableOffsetDateTimeUtc()  // lastLoginAt
				).as(Tuple::of);

		// +2 últimos (createdAt, updatedAt)
		return Combinators.combine(
				first8,
				nullableOffsetDateTimeUtc(),
				nullableOffsetDateTimeUtc()
		).as((t8, createdAt, updatedAt) -> {
			assert t8 != null;
			return new UserResponse(
					t8.get1(),
					t8.get2(),
					t8.get3(),
					t8.get4(),
					t8.get5(),
					t8.get6(),
					t8.get7(),
					t8.get8(),
					createdAt,
					updatedAt
			);
		});
	}

	@Provide
	public Arbitrary<UserResponse> userResponsesNullable() {
		return userResponses().injectNull(0.20);
	}
}
