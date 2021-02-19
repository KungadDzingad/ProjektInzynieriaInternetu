package com.example.forum.controllers;

import com.example.forum.dao.UserRepo;
import com.example.forum.models.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultHandler;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.transaction.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(roles = {"USER","ADMIN"})
class AdminControllerTest {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @Transactional
    void promoteUserShouldReturnStatus409() throws Exception {
        User user = new User();
        user.setRoles("ROLE_USER,ROLE_ADMIN");
        user.setLogin("test");
        user.setPassword(passwordEncoder.encode("test"));
        user.setId("test@test.pl");
        user.setNickName("test");
        userRepo.save(user);

        mockMvc.perform(put("/api/admin/users/"+user.getNickName()+"/promote"))
                .andDo(print())
                .andExpect(status().is(409));
    }
}