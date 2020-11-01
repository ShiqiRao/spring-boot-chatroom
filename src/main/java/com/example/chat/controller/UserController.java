package com.example.chat.controller;

import com.example.chat.entity.TUser;
import com.example.chat.model.R;
import com.example.chat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;

    @PostMapping("/session")
    public R<?> loginOrRegister(HttpServletRequest request, @RequestParam String username, @RequestParam String password) throws ServletException {
        Optional<TUser> userOptional = userRepository.findOne(Example.of(new TUser().setUsername(username)));
        if (!userOptional.isPresent()) {
            TUser user = new TUser()
                    .setUsername(username)
                    .setPassword(encoder.encode(password));
            userRepository.insert(user);
        } else {
            return R.failed("账号已存在");
        }
        return R.ok();
    }

}
