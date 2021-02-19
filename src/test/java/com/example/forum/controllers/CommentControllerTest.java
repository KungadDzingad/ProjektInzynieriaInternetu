package com.example.forum.controllers;

import com.example.forum.dao.CommentRepo;
import com.example.forum.models.Comment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.transaction.Transactional;
import java.net.http.HttpHeaders;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class CommentControllerTest {

    @Autowired
    private CommentRepo commentRepo;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @Transactional
    void shouldUpvoteComment() throws Exception {
        //given
        final int startUpvotes = 6;
        Comment comment = new Comment();
        comment.setBody("test");
        comment.setDate(new Date(System.currentTimeMillis()));
        comment.setDownvotes(5);
        comment.setUpvotes(startUpvotes);
        commentRepo.save(comment);
        //when
        MvcResult result = mockMvc.perform(put("/api/comments/"+comment.getId()+"/upvote"))
                .andDo(print())
                .andExpect(status().is(200))
                .andReturn();
        Comment commentAfter = commentRepo.findById(comment.getId()).get();
        assertEquals(commentAfter.getUpvotes(), startUpvotes+1);
        //then
    }

    @Test
    @Transactional
    void shouldDownvoteComment() throws Exception {
        final int startDownvotes = 20;
        Comment comment = new Comment();
        comment.setBody("test");
        comment.setDate(new Date(System.currentTimeMillis()));
        comment.setDownvotes(startDownvotes);
        comment.setUpvotes(100);
        commentRepo.save(comment);

        MvcResult result = mockMvc.perform(put("/api/comments/"+comment.getId()+"/downvote"))
                .andDo(print())
                .andExpect(status().is(200))
                .andReturn();
        Comment commentAfter = commentRepo.findById(comment.getId()).get();
        assertEquals(commentAfter.getDownvotes(), startDownvotes+1);
    }

}