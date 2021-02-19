package com.example.forum.controllers;

import com.example.forum.models.Comment;
import com.example.forum.models.Topic;
import com.example.forum.models.requests.AuthenticationRequest;
import com.example.forum.models.requests.ChangePasswordRequest;
import com.example.forum.models.requests.CreateNewTopicRequest;
import com.example.forum.models.requests.RegisterRequest;
import com.example.forum.models.responses.AuthenticationResponse;
import com.example.forum.services.UserService;
import com.example.forum.models.User;
import com.example.forum.services.exceptions.LoginInUseException;
import com.example.forum.services.exceptions.MailInUseException;
import com.example.forum.services.exceptions.NickNameNotFoundException;
import com.example.forum.services.exceptions.TopicNotFoundException;
import com.example.forum.util.CookieUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final static String COOKIE_NAME = "Authorization";

    private UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest){
        try {
            userService.register(registerRequest);
        } catch (MailInUseException | LoginInUseException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthenticationRequest authenticationRequest, HttpServletResponse servletResponse){
        String jwt = null;
        Cookie cookie = null;
        try {
            jwt = userService.authenticate(authenticationRequest);
            cookie = new Cookie(COOKIE_NAME, jwt);
            cookie.setPath("/api/");
            cookie.isHttpOnly();
            servletResponse.addCookie(cookie);
        } catch (BadCredentialsException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }


    @GetMapping("/all")
    public Iterable<User> getAll(){
        return userService.getAll();
    }

    @GetMapping("/{nickname}")
    public User getById(@PathVariable String nickname ){
        return userService.getByNickName(nickname);
    }

    @GetMapping("/{nickname}/topics")
    public ResponseEntity<List<Topic>> getUsersTopics(@PathVariable String nickname){
        List<Topic> topics = null;
        try {
            topics =  userService.getUsersTopics(nickname);
        } catch (NickNameNotFoundException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(topics,HttpStatus.OK);
    }

    @GetMapping("/{nickname}/comments")
    public ResponseEntity<List<Comment>> getUsersComments(@PathVariable String nickname){
        try{
            List<Comment>  comments = userService.getUsersComments(nickname);
            return new ResponseEntity<>(comments, HttpStatus.FOUND);
        } catch (NickNameNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest newPassword, HttpServletRequest request){
        Cookie cookie = new CookieUtil().getAuthorizationCookie(request);
        try{
            userService.changePassword(cookie.getValue(), newPassword.getPassword());
            return new ResponseEntity<>(HttpStatus.FOUND);
        }catch (UsernameNotFoundException e){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/topic/{id}")
    public ResponseEntity<?> deleteTopic(@PathVariable long id, HttpServletRequest request) {
        Cookie cookie = new CookieUtil().getAuthorizationCookie(request);
        try {
            userService.deleteTopic(cookie.getValue(), id);
            return new ResponseEntity<>(HttpStatus.ACCEPTED);
        } catch (UsernameNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (TopicNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/topic/{id}")
    public ResponseEntity<?> changeTopicBody(@PathVariable long id, HttpServletRequest request, @RequestParam String body) {
        Cookie cookie = new CookieUtil().getAuthorizationCookie(request);
        try {
            userService.changeTopicBody(cookie.getValue(), id, body);
            return new ResponseEntity<>(HttpStatus.ACCEPTED);
        } catch (UsernameNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (TopicNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    @DeleteMapping("/delete-self")
    public ResponseEntity<?> deleteAccount(HttpServletRequest request){
        Cookie cookie = new CookieUtil().getAuthorizationCookie(request);
        try{
            userService.deleteAccount(cookie.getValue());
            return new ResponseEntity<>(HttpStatus.OK);
        }catch (UsernameNotFoundException e){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping
    public String secureTest(){
        return "test";
    }
}
