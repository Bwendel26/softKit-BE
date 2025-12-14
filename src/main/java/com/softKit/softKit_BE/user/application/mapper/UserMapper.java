package com.softKit.softKit_BE.user.application.mapper;

import com.softKit.softKit_BE.user.domain.User;
import com.softKit.softKit_BE.user.api.dto.UserCreateRequest;
import com.softKit.softKit_BE.user.api.dto.UserSelfUpdateRequest;
import com.softKit.softKit_BE.user.api.dto.UserUpdateRequest;
import com.softKit.softKit_BE.user.api.dto.UserResponse;
import org.mapstruct.*;

@Mapper(
		componentModel = "spring",
		unmappedSourcePolicy = ReportingPolicy.IGNORE,
		unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface UserMapper {

	@Mapping(target = "id", source = "id")
	@Mapping(target = "fullName", source = "fullName")
	@Mapping(target = "email", source = "email")
	@Mapping(target = "phoneE164", source = "phoneE164")
	@Mapping(target = "role", source = "role")
	@Mapping(target = "status", source = "status")
	@Mapping(target = "emailVerifiedAt", source = "emailVerifiedAt")
	@Mapping(target = "lastLoginAt", source = "lastLoginAt")
	@Mapping(target = "createdAt", source = "createdAt")
	@Mapping(target = "updatedAt", source = "updatedAt")
	UserResponse toUserResponse(User user);


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
	User fromCreateRequestToEntity(UserCreateRequest dto);


	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "passwordHash", ignore = true)
	@Mapping(target = "role", ignore = true)
	@Mapping(target = "status", ignore = true)
	@Mapping(target = "failedLoginAttempts", ignore = true)
	@Mapping(target = "lockedUntil", ignore = true)
	@Mapping(target = "lastLoginAt", ignore = true)
	@Mapping(target = "emailVerifiedAt", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	void updateEntityFromDto(UserUpdateRequest dto, @MappingTarget User user);


	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "email", ignore = true)
	@Mapping(target = "passwordHash", ignore = true)
	@Mapping(target = "role", ignore = true)
	@Mapping(target = "status", ignore = true)
	@Mapping(target = "failedLoginAttempts", ignore = true)
	@Mapping(target = "lockedUntil", ignore = true)
	@Mapping(target = "lastLoginAt", ignore = true)
	@Mapping(target = "emailVerifiedAt", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	void updateEntityFromSelfDto(UserSelfUpdateRequest dto, @MappingTarget User user);
}
