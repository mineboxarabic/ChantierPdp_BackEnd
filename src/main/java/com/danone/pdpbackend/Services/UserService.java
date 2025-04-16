package com.danone.pdpbackend.Services;

import com.danone.pdpbackend.entities.dto.UsersRegisterData;
import com.danone.pdpbackend.entities.User;

import java.util.List;

public interface UserService {


    List<User> findAll();

    User createUser(User user);


    User getUserById(Long id);

    User updateUser(User user, Long id);

    boolean deleteUser(Long id);

    boolean registerUser(UsersRegisterData user);

    User findByEmail(String email);

    User findById(Long id);
}
