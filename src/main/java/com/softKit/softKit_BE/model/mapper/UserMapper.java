package com.softKit.softKit_BE.model.mapper;

import com.softKit.softKit_BE.model.User;
import com.softKit.softKit_BE.model.dto.UserCreateDTO;
import com.softKit.softKit_BE.model.dto.UserUpdateDTO;
import com.softKit.softKit_BE.model.vo.UserResponseVO;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    //Entity to UserResponseVO
    @Mapping(target = "id", source = "id")
    @Mapping(target = "fullName", source = "fullName")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "phone", source = "phone")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    UserResponseVO toUserResponseVO(User user);

    //UserCreateDTO to User entity

    @Mapping(target = "fullName", source = "fullName")
    @Mapping(target = "username", source = "username")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "phone", source = "phone")
    User toEntity(UserCreateDTO dto);


    //updates User data with UserUpdateDTO data
    //ignores null from DTO
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "profile", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(UserUpdateDTO dto, @MappingTarget User user);

}
