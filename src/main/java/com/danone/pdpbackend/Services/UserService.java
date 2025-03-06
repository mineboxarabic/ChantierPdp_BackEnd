package com.danone.pdpbackend.Services;

import com.danone.pdpbackend.dto.UsersRegisterData;
import com.danone.pdpbackend.entities.AppUser;

import java.util.List;

public interface UserService {


    List<AppUser> findAll();

    AppUser createUser(AppUser appUser);


    AppUser getUserById(Long id);

    AppUser updateUser(AppUser appUser, Long id);

    boolean deleteUser(Long id);

    boolean registerUser(UsersRegisterData user);

    AppUser findByEmail(String email);
}
