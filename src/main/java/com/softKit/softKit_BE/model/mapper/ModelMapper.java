package com.softKit.softKit_BE.model.mapper;

import com.softKit.softKit_BE.model.User;
import com.softKit.softKit_BE.model.vo.UserVO;

public class ModelMapper {

    public UserVO userToVO(User user) {
        return new UserVO(user.getName(), user.getEmail(), user.getPhone());
    }

    public User userVOToUser(UserVO userVO) {
        User user = new User();
        user.setName(userVO.name());
        user.setEmail(userVO.email());
        user.setPhone(userVO.phone());

        return user;
    }
}
