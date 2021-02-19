package com.example.forum.controllers;

import com.example.forum.dao.TopicRepo;
import com.example.forum.dao.UserRepo;
import com.example.forum.models.Topic;
import com.example.forum.models.User;
import com.example.forum.models.security.MyUserDetails;
import com.example.forum.services.security.MyUserDetailsService;
import com.example.forum.util.CookieUtil;
import com.example.forum.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;


import javax.servlet.http.Cookie;
import javax.transaction.Transactional;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private MyUserDetailsService userDetailsService;
    @Autowired
    private TopicRepo topicRepo;

    @Transactional
    @BeforeEach
    void clearBase(){
        userRepo.deleteAll();
    }

    @Test
    @Transactional
    void loginAndGetToken() throws Exception{
        //given
        User user = new User();
        user.setId("test@o2.pl");
        user.setRoles("ROLE_USER");
        user.setActive(true);
        user.setLogin("test");
        user.setPassword(passwordEncoder.encode("test"));
        user.setNickName("123");
        userRepo.save(user);
        //when

        MvcResult login = mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .content("{\n\"username\": \"test\",\n \"password\": \"test\"\n}"))
                .andDo(print())
                .andExpect(status().is(200)).andReturn();

        assertTrue(userRepo.findByLogin(user.getLogin()).isPresent());
    }

    @Test
    @Transactional
    @WithMockUser(roles = "USER")
    void shouldGetAll() throws Exception {

        User user1 = new User();
        user1.setId("1@1.pl");
        user1.setPassword(passwordEncoder.encode("123"));
        user1.setLogin("1");
        user1.setNickName("1");
        user1.setRoles("ROLE_USER");
        User user2 = new User();
        user2.setId("2@2.pl");
        user2.setLogin("2");
        user2.setNickName("2");
        user2.setRoles("ROLE_USER");
        user2.setPassword(passwordEncoder.encode("123"));
        userRepo.save(user1);
        userRepo.save(user2);

        MvcResult result = mockMvc.perform(get("/api/users/all"))
            .andDo(print())
            .andExpect(status().is(200))
            .andReturn();

        List<User> users =(List<User>) userRepo.findAll();
        assertEquals(users.size(),2);
        assertTrue(userRepo.findByLogin(user2.getLogin()).isPresent());
    }

    @Test
    @Transactional
    @WithMockUser
    void shouldGetOneByNickName() throws Exception {
        User user1 = new User();
        user1.setId("test");
        user1.setPassword(passwordEncoder.encode("test"));
        user1.setLogin("test");
        user1.setNickName("test");
        user1.setRoles("ROLE_USER");
        userRepo.save(user1);

        mockMvc.perform(get("/api/users/"+user1.getId()))
                .andDo(print())
                .andExpect(status().is(200));

        assertTrue(userRepo.findByLogin(user1.getLogin()).isPresent());
    }

    @Test
    @Transactional
    @WithMockUser
    void shouldDeleteTopic() throws Exception {
        User user = new User();
        user.setId("test");
        user.setPassword(passwordEncoder.encode("test"));
        user.setLogin("test");
        user.setNickName("test");
        user.setRoles("ROLE_USER");
        userRepo.save(user);
        Topic topic = new Topic();
        userRepo.findByLogin(user.getLogin()).get().addTopic(topic);
        topic.setDate(new Date(System.currentTimeMillis()));
        topic.setBody("test");
        topic.setHead("test");
        topic.setCategory("test");

        topicRepo.save(topic);


        final UserDetails userDetails = userDetailsService.loadUserByUsername(user.getLogin());
        String token = jwtUtil.generateToken(userDetails);
        Cookie tokenCookie = new Cookie("Authorization",token);
        tokenCookie.setPath("/api/");

        assertEquals(topicRepo.findById(topic.getId()).get().getUser().getId(),user.getId());

        mockMvc.perform(delete("/api/users/topic/"+topic.getId())
        .cookie(tokenCookie))
                .andDo(print())
                .andExpect(status().is(202));

        assertEquals(userRepo.findByLogin(user.getLogin()).get().getTopics().size(), 0);

    }
}