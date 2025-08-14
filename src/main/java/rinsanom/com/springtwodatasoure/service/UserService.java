package rinsanom.com.springtwodatasoure.service;

import rinsanom.com.springtwodatasoure.entity.User;

import java.util.List;

public interface UserService {
    List<User> findAll();
    User save(User user);
}
