package com.danone.pdpbackend.Repo;

import com.danone.pdpbackend.entities.User;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;

@org.springframework.stereotype.Repository
public interface UsersRepo extends Repository<User, Long>
{


    List<User> findAll();
    List<User> findAllBy();


    Optional<User> findByUsername(String username);
    
    User save(User user);

    List<User> findAllById(Long id);

    void deleteById(Long id);

    User findByEmail(String email);

    Boolean existsUserByUsername(String username);
}
