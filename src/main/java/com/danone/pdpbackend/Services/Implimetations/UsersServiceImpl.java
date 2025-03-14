package com.danone.pdpbackend.Services.Implimetations;

import com.danone.pdpbackend.dto.UsersRegisterData;
import com.danone.pdpbackend.Repo.UsersRepo;
import com.danone.pdpbackend.Services.UserService;
import com.danone.pdpbackend.entities.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class UsersServiceImpl implements UserService {
    UsersRepo usersRepo;

    public UsersServiceImpl(UsersRepo usersRepo) {
        this.usersRepo = usersRepo;
    }

    @Override
    public List<User> findAll() {
        return usersRepo.findAllBy();
    }

    @Override
    public User createUser(User user) {
        return usersRepo.save(user);
    }

    @Override
    public User getUserById(Long id){
        return usersRepo.findAllById(id).get(0);
    }

    @Override
    public User updateUser(User appUser, Long id) {
        User user = usersRepo.findAllById(id).get(0);

        if(user == null){
            return null;
        }

        if(appUser.getEmail() != null) user.setEmail(appUser.getEmail());
        if(appUser.getPassword() != null) user.setPassword(appUser.getPassword());
        if(appUser.getFonction() != null) user.setFonction(appUser.getFonction());
        if(appUser.getRole() != null) user.setRole(appUser.getRole());
        if(appUser.getNotel() != null) user.setNotel(appUser.getNotel());

        return usersRepo.save(user);
    }

    @Override
    public boolean deleteUser(Long id) {
        User user = usersRepo.findAllById(id).get(0);
        if(user == null){
            return false;
        }
        usersRepo.deleteById(id);
        return true;
    }

    @Override
    public boolean registerUser(UsersRegisterData user) {
        User currentUser = usersRepo.findByEmail(user.email);
        if(currentUser != null){
            return false;
        }

        User appUser = new User();
        appUser.setEmail(user.email);
        appUser.setPassword(user.password);

        createUser(appUser);

        return true;
    }

    @Override
    public User findByEmail(String email) {
        return usersRepo.findByEmail(email);
    }
}
