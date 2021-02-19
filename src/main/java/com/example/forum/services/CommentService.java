package com.example.forum.services;

import com.example.forum.dao.CommentRepo;
import com.example.forum.dao.TopicRepo;
import com.example.forum.dao.UserRepo;
import com.example.forum.models.Comment;
import com.example.forum.services.exceptions.CommentNotFoundException;
import com.example.forum.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CommentService {

    private CommentRepo commentRepo;
    private TopicRepo topicRepo;
    private UserRepo userRepo;
    private JwtUtil jwtUtil;

    @Autowired
    public CommentService(CommentRepo commentRepo, TopicRepo topicRepo, UserRepo userRepo, JwtUtil jwtUtil) {
        this.commentRepo = commentRepo;
        this.topicRepo = topicRepo;
        this.userRepo = userRepo;
        this.jwtUtil = jwtUtil;
    }

    public void upvote(long id) throws CommentNotFoundException{
        Optional<Comment> commentOptional = commentRepo.findById(id);
        if(commentOptional.isEmpty())
            throw new CommentNotFoundException();

        Comment comment = commentOptional.get();
        comment.upvote();
        commentRepo.save(comment);
    }

    public void downvote(long id) throws CommentNotFoundException{
        Optional<Comment> commentOptional = commentRepo.findById(id);
        if(commentOptional.isEmpty())
            throw new CommentNotFoundException();

        Comment comment = commentOptional.get();
        comment.downvote();
        commentRepo.save(comment);
    }

    public void deleteComment(long id) throws CommentNotFoundException{
        Optional<Comment> commentOptional = commentRepo.findById(id);
        if(commentOptional.isEmpty())
            throw new CommentNotFoundException();

        Comment comment = commentOptional.get();
        commentRepo.delete(comment);
    }
}
