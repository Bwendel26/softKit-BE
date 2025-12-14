package com.softKit.softKit_BE.auth.application.mapper;

import com.softKit.softKit_BE.auth.api.dto.RegisterRequest;
import com.softKit.softKit_BE.auth.api.dto.RegisterResponse;
import com.softKit.softKit_BE.user.domain.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
		componentModel = "spring",
		unmappedSourcePolicy = ReportingPolicy.IGNORE,
		unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface AuthMapper {

	@Mapping(target = "id", source = "id")
	@Mapping(target = "fullName", source = "fullName")
	@Mapping(target = "email", source = "email")
	@Mapping(target = "phoneE164", source = "phoneE164")
	@Mapping(target = "createdAt", source = "createdAt")
	@Mapping(target = "updatedAt", source = "updatedAt")
	RegisterResponse toRegisterResponse(User user);


	@Mapping(target = "fullName", source = "fullName")
	@Mapping(target = "email", source = "email")
	@Mapping(target = "phoneE164", source = "phoneE164")
	@Mapping(target = "passwordHash", ignore = true)
	@Mapping(target = "role", ignore = true)
	@Mapping(target = "status", ignore = true)
	@Mapping(target = "failedLoginAttempts", ignore = true)
	@Mapping(target = "lockedUntil", ignore = true)
	@Mapping(target = "lastLoginAt", ignore = true)
	@Mapping(target = "emailVerifiedAt", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	User fromRegisterRequestToEntity(RegisterRequest dto);
}
