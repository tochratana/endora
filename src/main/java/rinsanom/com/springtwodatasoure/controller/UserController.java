package rinsanom.com.springtwodatasoure.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import rinsanom.com.springtwodatasoure.entity.User;
import rinsanom.com.springtwodatasoure.service.UserService;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;


    @GetMapping
    public Iterable<User> findAll() {
        return userService.findAll();
    }


    @PostMapping
    public User save(@RequestBody User user) {
        return userService.save(user);
    }

}

