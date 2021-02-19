package com.example.forum.controllers;

import com.example.forum.models.User;
import com.example.forum.services.CommentService;
import com.example.forum.services.TopicService;
import com.example.forum.services.UserService;
import com.example.forum.services.exceptions.CommentNotFoundException;
import com.example.forum.services.exceptions.NickNameNotFoundException;
import com.example.forum.services.exceptions.TopicNotFoundException;
import com.example.forum.services.exceptions.UserAlreadyPromoted;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private UserService userService;
    private TopicService topicService;
    private CommentService commentService;

    @Autowired
    public AdminController(UserService userService, TopicService topicService, CommentService commentService) {
        this.userService = userService;
        this.topicService = topicService;
        this.commentService = commentService;
    }


    @GetMapping("/all")
    public ResponseEntity<List<User>> getAdmins(){
        return new ResponseEntity<>(userService.getAdmins(), HttpStatus.OK);
    }

    @PutMapping("/users/{nickname}/block")
    public ResponseEntity<?> blockUser(@PathVariable String nickname){
        try{
            userService.blockUser(nickname);
            return new ResponseEntity<>(HttpStatus.ACCEPTED);
        } catch (NickNameNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/users/{nickname}/unblock")
    public ResponseEntity<?> unblockUser(@PathVariable String nickname){
        try{
            userService.unblockUser(nickname);
            return new ResponseEntity<>(HttpStatus.ACCEPTED);
        } catch (NickNameNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/users/{nickname}/promote")
    public ResponseEntity<?> promoteUser(@PathVariable String nickname){
        try{
            userService.promote(nickname);
            return new ResponseEntity<>(HttpStatus.ACCEPTED);
        } catch (UserAlreadyPromoted userAlreadyPromoted) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        } catch (NickNameNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/topics/{id}")
    public ResponseEntity<?> deleteTopic(@PathVariable long id){
        try{
            topicService.deleteTopic(id);
            return new ResponseEntity<>(HttpStatus.ACCEPTED);
        } catch (TopicNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/comments/{id}")
    public ResponseEntity<?> deleteComment(@PathVariable long id){
        try{
            commentService.deleteComment(id);
            return new ResponseEntity<>(HttpStatus.ACCEPTED);
        } catch (CommentNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

}
