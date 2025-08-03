package com.softKit.softKit_BE.service;

import com.softKit.softKit_BE.model.User;
import com.softKit.softKit_BE.model.mapper.ModelMapper;
import com.softKit.softKit_BE.model.vo.UserVO;
import com.softKit.softKit_BE.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    UserRepository repository;

    ModelMapper mapper = new ModelMapper();

    public UserVO getUserById(Long id) {
        var user = repository.findById(id).orElseThrow();
        return mapper.userToVO(user);
    }

    public UserVO save(UserVO userVO) {
        User user = mapper.userVOToUser(userVO);
        return mapper.userToVO(repository.save(user));
    }

    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }
}
