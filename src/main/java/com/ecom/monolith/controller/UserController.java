package com.ecom.monolith.controller;

import com.ecom.monolith.Dto.UsersDto;
import com.ecom.monolith.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping()
    public ResponseEntity<List<UsersDto>> getUsers() {
        logger.info("GET /api/users - Fetching all users");
        List<UsersDto> users = userService.getUsers();
        return ResponseEntity.ok(users);
    }

    @PostMapping()
    public ResponseEntity<UsersDto> addUser(@Valid @RequestBody UsersDto user) {
        logger.info("POST /api/users - Adding new user with email={}", user.getEmail());
        UsersDto usersDto = userService.addUser(user);
        return ResponseEntity.ok(usersDto);
    }

    @GetMapping("{id}")
    public ResponseEntity<UsersDto> findById(@PathVariable("id") Long id) {
        logger.info("GET /api/users/{} - Fetching user by ID", id);
        UsersDto usersDto = userService.findById(id);
        return ResponseEntity.ok(usersDto);
    }

    @PutMapping("{id}")
    public ResponseEntity<UsersDto> updateUser(@PathVariable("id") Long id, @Valid @RequestBody UsersDto users) {
        logger.info("PUT /api/users/{} - Updating user", id);
        UsersDto usersDto = userService.updateUser(id, users);
        return ResponseEntity.ok(usersDto);
    }
}
