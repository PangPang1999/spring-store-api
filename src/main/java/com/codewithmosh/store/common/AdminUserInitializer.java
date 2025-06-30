package com.codewithmosh.store.common;

import com.codewithmosh.store.users.Role;
import com.codewithmosh.store.users.User;
import com.codewithmosh.store.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminUserInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) { // 只在空表时插入
            User admin = new User();
            admin.setName("ppp_melody");
            admin.setEmail("ppp_melody@163.com");
            admin.setPassword(passwordEncoder.encode("666666")); // 自动加密
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);
        }
    }
}
