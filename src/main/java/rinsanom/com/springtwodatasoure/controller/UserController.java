package rinsanom.com.springtwodatasoure.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rinsanom.com.springtwodatasoure.entity.User;
import rinsanom.com.springtwodatasoure.service.UserService;

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

