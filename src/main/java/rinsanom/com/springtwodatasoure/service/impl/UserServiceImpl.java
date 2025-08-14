package rinsanom.com.springtwodatasoure.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rinsanom.com.springtwodatasoure.entity.User;
import rinsanom.com.springtwodatasoure.repository.postgrest.UserRepository;
import rinsanom.com.springtwodatasoure.service.UserService;

import java.util.List;
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private  final UserRepository userRepository;
    @Override
    public User save(User user) {
        return userRepository.save(user);
    }
    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }
}
