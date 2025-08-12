package com.softKit.softKit_BE.model.mapper;

import com.softKit.softKit_BE.model.User;
import com.softKit.softKit_BE.model.dto.UserCreateDTO;
import com.softKit.softKit_BE.model.dto.UserUpdateDTO;
import com.softKit.softKit_BE.model.vo.UserResponseVO;

public class ModelMapper {

    public UserResponseVO toResponse(User user) {
        if (user == null) return null;
        return new UserResponseVO(user.getId(), user.getName(), user.getEmail(), user.getPhone(), user.getCreatedAt());
    }

    public User toEntity(UserCreateDTO dto) {
        if (dto == null) return null;
        User usr = new User();
        usr.setName(dto.name());
        usr.setEmail(dto.email());
        usr.setPhone(dto.phone());
        return usr;
    }

    public void updateFromDto(UserUpdateDTO dto, User user) {
        if(dto == null || user == null) return;
        if(dto.name() != null) user.setName(dto.name());
        if(dto.email() != null) user.setEmail(dto.email());
        if(dto.phone() != null) user.setPhone(dto.phone());
    }
}
