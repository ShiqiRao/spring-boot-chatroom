package com.example.chat.security;

import com.example.chat.entity.TUser;
import com.example.chat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Transactional
@Service
@RequiredArgsConstructor
public class AuthorityUserDetailServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<TUser> userOptional = userRepository.findOne(Example.of(new TUser().setUsername(username)));
        if (!userOptional.isPresent()) {
            throw new UsernameNotFoundException("username not found");
        }
        TUser user = userOptional.get();
        return new User(user.getUsername(), user.getPassword(), Collections.singleton(new SimpleGrantedAuthority("USER")));
    }
}
