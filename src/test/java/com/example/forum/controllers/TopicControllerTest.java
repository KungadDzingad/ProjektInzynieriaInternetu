package com.example.forum.controllers;

import com.example.forum.dao.TopicRepo;
import com.example.forum.dao.UserRepo;
import com.example.forum.models.Topic;
import com.example.forum.models.User;
import com.example.forum.services.security.MyUserDetailsService;
import com.example.forum.util.CookieUtil;
import com.example.forum.util.JwtUtil;
import org.aspectj.lang.annotation.Before;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.servlet.http.Cookie;
import javax.transaction.Transactional;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser
class TopicControllerTest {

    @Autowired
    private TopicRepo topicRepo;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private MyUserDetailsService userDetailsService;

    private Cookie tokenCookie;
    private User user;

    @BeforeEach
    @Transactional
    void loginBeforeTests() throws Exception {
        user = new User();
        user.setId("test");
        user.setPassword(passwordEncoder.encode("test"));
        user.setLogin("test");
        user.setNickName("test");
        user.setRoles("ROLE_USER");
        userRepo.save(user);

        final UserDetails userDetails = userDetailsService.loadUserByUsername(user.getLogin());
        String token = jwtUtil.generateToken(userDetails);
        tokenCookie = new Cookie("Authorization",token);
        tokenCookie.setPath("/api/");
    }

    @AfterEach
    void deleteUserAfterTest(){
        userRepo.delete(user);
    }


    @Test
    @Transactional
    void shouldCreateTopic() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/topics").cookie(tokenCookie)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .content("{\"head\": \"test\", \"category\": \"test\", \"body\": \"test\"}"))
                .andDo(print())
                .andExpect(status().is(201))
                .andReturn();

        List<Topic> topics = (List)topicRepo.findAll();
        assertEquals((topics).size(), 1);
    }

    @Test
    @Transactional
    void shouldAddCommentToTopic() throws Exception {
        Topic topic = new Topic();
        user.addTopic(topic);
        topic.setDate(new Date(System.currentTimeMillis()));
        topic.setBody("test");
        topic.setHead("test");
        topic.setCategory(  "test");
        topicRepo.save(topic);

        MvcResult result = mockMvc.perform(post("/api/topics/"+topic.getId()+"/comment").cookie(tokenCookie)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .content("{\"body\": \"comment test\"}"))
                .andDo(print())
                .andExpect(status().is(201)).andReturn();


        assertEquals(topicRepo.findById(topic.getId()).get().getComments().size(), 1);
    }
}








