package com.example.forum.services;

import com.example.forum.dao.CommentRepo;
import com.example.forum.dao.TopicRepo;
import com.example.forum.dao.UserRepo;
import com.example.forum.models.Comment;
import com.example.forum.models.Topic;
import com.example.forum.models.User;
import com.example.forum.models.requests.CreateNewTopicRequest;
import com.example.forum.models.requests.NewCommentRequest;
import com.example.forum.services.exceptions.CommentNotFoundException;
import com.example.forum.services.exceptions.TopicNotFoundException;
import com.example.forum.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
public class TopicService {

    private TopicRepo topicRepo;
    private UserRepo userRepo;
    private JwtUtil jwtUtil;
    private CommentRepo commentRepo;

    @Autowired
    public TopicService(TopicRepo topicRepo, UserRepo userRepo, CommentRepo commentRepo, JwtUtil jwtUtil) {
        this.topicRepo = topicRepo;
        this.userRepo = userRepo;
        this.jwtUtil = jwtUtil;
        this.commentRepo = commentRepo;
    }

    public Iterable<Topic> findAll(){
        return topicRepo.findAll();
    }

    public Topic findById(long id){
        return topicRepo.findById(id).get();
    }

    public void addTopicAsUser(String jwt, CreateNewTopicRequest newTopic) throws UsernameNotFoundException {
        String username = jwtUtil.extractUserName(jwt);
        Optional<User> user = userRepo.findByLogin(username);
        if(user.isEmpty()) throw new UsernameNotFoundException(username);
        User foundUser = user.get();
        Topic topic = new Topic();
        topic.setHead(newTopic.getHead());
        topic.setCategory(newTopic.getCategory());
        topic.setBody(newTopic.getBody());
        topic.setDate(new Date(System.currentTimeMillis()));
        foundUser.addTopic(topic);
        userRepo.save(foundUser);
    }

    public Iterable<Comment> getTopicComments(long id) throws TopicNotFoundException{
        Optional<Topic> topicOptional = topicRepo.findById(id);
        if(topicOptional.isEmpty()){
            throw new TopicNotFoundException();
        }

        Topic topic = topicOptional.get();
        return topic.getComments();
    }

    public void addComment(long id, String jwt, NewCommentRequest newComment) throws TopicNotFoundException, UsernameNotFoundException{
        String username = jwtUtil.extractUserName(jwt);
        Optional<User> userOptional = userRepo.findByLogin(username);
        if(userOptional.isEmpty()) throw new UsernameNotFoundException(username);
        Optional<Topic> topicOptional = topicRepo.findById(id);
        if(topicOptional.isEmpty()) throw new TopicNotFoundException();

        User user = userOptional.get();
        Topic topic = topicOptional.get();

        Comment comment = new Comment();
        comment.setBody(newComment.getBody());
        comment.setDate(new Date(System.currentTimeMillis()));
        user.addComment(comment);
        topic.addComment(comment);
        commentRepo.save(comment);

    }

    public void upvoteTopic(long id) throws TopicNotFoundException {
        Optional<Topic> topicOptional = topicRepo.findById(id);
        if(topicOptional.isEmpty())
            throw new TopicNotFoundException();

        Topic topic = topicOptional.get();
        topic.upvote();
        topicRepo.save(topic);
    }

    public void downvoteTopic(long id) throws TopicNotFoundException{
        Optional<Topic> topicOptional = topicRepo.findById(id);
        if(topicOptional.isEmpty())
            throw new TopicNotFoundException();

        Topic topic = topicOptional.get();
        topic.downvote();
        topicRepo.save(topic);
    }

    public void deleteTopic(long id) throws TopicNotFoundException {
        Optional<Topic> topicOptional = topicRepo.findById(id);
        if(topicOptional.isEmpty())
            throw new TopicNotFoundException();
        Topic topic = topicOptional.get();
        topicRepo.delete(topic);
    }
}
