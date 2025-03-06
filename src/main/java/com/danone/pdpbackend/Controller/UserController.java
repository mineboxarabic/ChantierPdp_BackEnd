package com.danone.pdpbackend.Controller;


import com.danone.pdpbackend.Services.UserService;
import com.danone.pdpbackend.Utils.ApiResponse;
import com.danone.pdpbackend.dto.UsersRegisterData;
import com.danone.pdpbackend.entities.AppUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@Slf4j
public class UserController {
    UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }

    //Read
    @GetMapping("")
    public ResponseEntity<ApiResponse<List<AppUser>>> fetchAll(){
        return new ResponseEntity<>(new ApiResponse<>(userService.findAll(), "Info fetched"), HttpStatus.OK);
    }

    //Create
    @PostMapping("")
    public ResponseEntity<ApiResponse<AppUser>> createUser(@RequestBody AppUser appUser){
        log.info("Creating user with name: {}", appUser.getName());
        userService.createUser(appUser);
        return new ResponseEntity<>(new ApiResponse<>( userService.createUser(appUser), "User Created Successfully"), HttpStatus.CREATED);
    }

    //Update
   @PutMapping("/{id}")
   public ResponseEntity<ApiResponse<AppUser>> updateUser(@RequestBody AppUser appUser, @PathVariable Long id){
       log.info("Updating user with id: {}", id);
       AppUser appUser1 = userService.updateUser(appUser, id);
       if(appUser1 == null){
           return new ResponseEntity<>(new ApiResponse<>(appUser1,"User Not Found"), HttpStatus.NOT_FOUND);
       }
       return new ResponseEntity<>(new ApiResponse<>(appUser1, "User Updated Successfully"), HttpStatus.OK);
   }

   @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Boolean>> deleteUser(@PathVariable Long id){
        if(!userService.deleteUser(id)){
            return new ResponseEntity<>(new ApiResponse<>(false,"User Not Found"), HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(new ApiResponse<>(true, "User Deleted Successfully"), HttpStatus.OK);
   }


   @PostMapping("register")
    public ResponseEntity<String> registerUser(@RequestBody UsersRegisterData user){

        if(userService.registerUser(user)){
            return new ResponseEntity<>("User Registered Successfully", HttpStatus.OK);
        }
        return new ResponseEntity<>("User Found", HttpStatus.CONFLICT);
   }


   @PostMapping("login")
    public ResponseEntity<Object> loginUser(@RequestBody UsersRegisterData user){

        AppUser appUser = userService.findByEmail(user.email);

        if(appUser != null){
            if(user.password.equals(appUser.getPassword())){

                return new ResponseEntity<>(appUser, HttpStatus.OK);
            }else{
                return new ResponseEntity<>("User Login Failed (Worng password)", HttpStatus.CONFLICT);
            }
        }
        return new ResponseEntity<>("User Not Found", HttpStatus.NOT_FOUND);
   }

    



}
