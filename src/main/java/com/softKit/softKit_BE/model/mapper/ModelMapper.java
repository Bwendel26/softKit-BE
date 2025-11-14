package com.softKit.softKit_BE.model.mapper;

import com.softKit.softKit_BE.model.User;
import com.softKit.softKit_BE.model.dto.UserCreateDTO;
import com.softKit.softKit_BE.model.dto.UserUpdateDTO;
import com.softKit.softKit_BE.model.vo.UserResponseVO;
import org.springframework.stereotype.Component;

@Component
public class ModelMapper {

    public UserResponseVO toResponse(User user) {
        if (user == null) return null;
        return new UserResponseVO(
                user.getId(),
                user.getFullName(),
                user.getUsername(),
                user.getEmail(),
                user.getPhone(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }

    public User toEntity(UserCreateDTO dto) {
        if (dto == null) return null;
        User usr = new User();
        usr.setFullName(dto.fullName());
        usr.setUsername(dto.username());
        usr.setEmail(dto.email());
        usr.setPhone(dto.phone());
        return usr;
    }

    public UserResponseVO userToResponseVO(User user) {
        return new UserResponseVO(
                user.getId(),
                user.getFullName(),
                user.getUsername(),
                user.getEmail(),
                user.getPhone(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    public void updateFromDto(UserUpdateDTO dto, User user) {
        if(dto == null || user == null) return;
        if(dto.fullName() != null) user.setFullName(dto.fullName());
        if(dto.email() != null) user.setEmail(dto.email());
        if(dto.phone() != null) user.setPhone(dto.phone());
    }
}
