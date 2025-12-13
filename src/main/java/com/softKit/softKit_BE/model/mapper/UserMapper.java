package com.softKit.softKit_BE.model.mapper;

import com.softKit.softKit_BE.model.User;
import com.softKit.softKit_BE.model.dto.requests.RegisterRequest;
import com.softKit.softKit_BE.model.dto.requests.UserCreateRequest;
import com.softKit.softKit_BE.model.dto.requests.UserSelfUpdateRequest;
import com.softKit.softKit_BE.model.dto.requests.UserUpdateRequest;
import com.softKit.softKit_BE.model.dto.responses.RegisterResponse;
import com.softKit.softKit_BE.model.dto.responses.UserResponse;
import org.mapstruct.*;

@Mapper(
		componentModel = "spring",
		unmappedSourcePolicy = ReportingPolicy.IGNORE,
		unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface UserMapper {

	// ---------- ENTITY -> RESPONSE ----------

	// Para fluxo de register (AuthController)
	@Mapping(target = "id", source = "id")
	@Mapping(target = "fullName", source = "fullName")
	@Mapping(target = "email", source = "email")
	@Mapping(target = "phoneE164", source = "phoneE164")
	@Mapping(target = "createdAt", source = "createdAt")
	@Mapping(target = "updatedAt", source = "updatedAt")
	RegisterResponse toRegisterResponse(User user);

	// Para API de usuários em geral (UserController)
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


	// ---------- REQUEST -> ENTITY (CREATE) ----------

	// Admin criando usuário
	@Mapping(target = "fullName", source = "fullName")
	@Mapping(target = "email", source = "email")
	@Mapping(target = "phoneE164", source = "phoneE164")
	@Mapping(target = "passwordHash", ignore = true)          // setado no service
	@Mapping(target = "role", ignore = true)                   // default no service/domínio
	@Mapping(target = "status", ignore = true)                 // default no service/domínio
	@Mapping(target = "failedLoginAttempts", ignore = true)
	@Mapping(target = "lockedUntil", ignore = true)
	@Mapping(target = "lastLoginAt", ignore = true)
	@Mapping(target = "emailVerifiedAt", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	User fromCreateRequestToEntity(UserCreateRequest dto);

	// Auto-registro (fluxo de /auth/register)
	@Mapping(target = "fullName", source = "fullName")
	@Mapping(target = "email", source = "email")
	@Mapping(target = "phoneE164", source = "phoneE164")
	@Mapping(target = "passwordHash", ignore = true)          // setado no service
	@Mapping(target = "role", ignore = true)                  // default no service/domínio
	@Mapping(target = "status", ignore = true)                // default no service/domínio
	@Mapping(target = "failedLoginAttempts", ignore = true)
	@Mapping(target = "lockedUntil", ignore = true)
	@Mapping(target = "lastLoginAt", ignore = true)
	@Mapping(target = "emailVerifiedAt", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	User fromRegisterRequestToEntity(RegisterRequest dto);


	// ---------- UPDATE COMPLETO (ADMIN / DONO via /users/{id}) ----------

	/**
	 * Atualiza os dados "administrativos" do usuário.
	 * - Permite mudar fullName, email, phoneE164.
	 * - NÃO mexe em senha, role, status, campos de segurança nem timestamps.
	 * - Ignora valores nulos na request.
	 */
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


	// ---------- UPDATE DE PERFIL (SELF-SERVICE via /users/me) ----------

	/**
	 * Atualiza apenas dados de perfil do usuário logado:
	 * - fullName, phoneE164
	 * - NÃO toca em email, senha, role, status, segurança, timestamps.
	 * - Ignora valores nulos.
	 */
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
