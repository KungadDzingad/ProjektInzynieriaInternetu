package com.example.forum.controllers;

import com.example.forum.services.CommentService;
import com.example.forum.services.exceptions.CommentNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/comments")
public class CommentController {

    private CommentService commentService;

    @Autowired
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PutMapping("/{id}/upvote")
    public ResponseEntity<?> upvoteComment(@PathVariable long id){
        try{
            commentService.upvote(id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (CommentNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}/downvote")
    public ResponseEntity<?> downvoteComment(@PathVariable long id){
        try{
            commentService.downvote(id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (CommentNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

}
