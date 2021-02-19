package com.example.forum.controllers;

import com.example.forum.models.Comment;
import com.example.forum.models.requests.CreateNewTopicRequest;
import com.example.forum.models.requests.NewCommentRequest;
import com.example.forum.services.TopicService;
import com.example.forum.models.Topic;
import com.example.forum.services.exceptions.CommentNotFoundException;
import com.example.forum.services.exceptions.TopicNotFoundException;
import com.example.forum.util.CookieUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("api/topics")
public class TopicController {

    private TopicService topicService;

    @Autowired
    public TopicController(TopicService topicService) {
        this.topicService = topicService;
    }

    @GetMapping("/all")
    public Iterable<Topic> getAll(){
        return topicService.findAll();
    }

    @GetMapping("/{id}")
    public Topic getById(@PathVariable long id){
        return topicService.findById(id);
    }

    @PostMapping
    public ResponseEntity<?> createTopic(@RequestBody CreateNewTopicRequest newTopic, HttpServletRequest request){
        String jwt = new CookieUtil().getAuthorizationCookie(request).getValue();
        try{
            topicService.addTopicAsUser(jwt, newTopic);
            return new ResponseEntity<>(HttpStatus.CREATED);
        }catch (UsernameNotFoundException e){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{id}/comment/all")
    public ResponseEntity<Iterable<Comment>> getComments(@PathVariable long id){
        Iterable<Comment> comments = null;
        try{
            comments = topicService.getTopicComments(id);
            return new ResponseEntity<>(comments, HttpStatus.OK);

        } catch (TopicNotFoundException e) {
            return  new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/{id}/comment")
    public ResponseEntity<?> addCommentToTopic(
            @RequestBody NewCommentRequest newComment, @PathVariable long id, HttpServletRequest request){
        String jwt = new CookieUtil().getAuthorizationCookie(request).getValue();
        try{
            topicService.addComment(id,jwt,newComment);
            return new ResponseEntity<>(HttpStatus.CREATED);

        } catch (TopicNotFoundException | UsernameNotFoundException e){
            return  new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}/upvote")
    public ResponseEntity<?> upvoteComment(@PathVariable long id){
        try{
            topicService.upvoteTopic(id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (TopicNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}/downvote")
    public ResponseEntity<?> downvoteComment(@PathVariable long id){
        try{
            topicService.downvoteTopic(id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (TopicNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
