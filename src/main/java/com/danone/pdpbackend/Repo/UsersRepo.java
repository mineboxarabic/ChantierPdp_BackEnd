package com.danone.pdpbackend.Repo;

import com.danone.pdpbackend.entities.AppUser;
import org.springframework.data.repository.Repository;

import java.util.List;

@org.springframework.stereotype.Repository
public interface UsersRepo extends Repository<AppUser, Long>
{


    List<AppUser> findAll();
    List<AppUser> findAllBy();

    AppUser save(AppUser appUser);

    List<AppUser> findAllById(Long id);

    void deleteById(Long id);

    AppUser findByEmail(String email);
}
