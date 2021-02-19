package com.example.forum.services.security;

import com.example.forum.dao.UserRepo;
import com.example.forum.models.User;
import com.example.forum.models.security.MyUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepo userRepo;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepo.findByLogin(username);
        if(user.isEmpty())
            throw new UsernameNotFoundException(username +  "Not found");

        return user.map(MyUserDetails::new).get();
    }
}
