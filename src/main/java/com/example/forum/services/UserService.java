package com.example.forum.services;

import com.example.forum.dao.TopicRepo;
import com.example.forum.dao.UserRepo;
import com.example.forum.models.Comment;
import com.example.forum.models.Topic;
import com.example.forum.models.requests.CreateNewTopicRequest;
import com.example.forum.models.requests.RegisterRequest;
import com.example.forum.services.exceptions.*;
import com.example.forum.models.User;
import com.example.forum.models.requests.AuthenticationRequest;
import com.example.forum.services.security.MyUserDetailsService;
import com.example.forum.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private UserRepo userRepo;
    private PasswordEncoder passwordEncoder;
    private AuthenticationManager authManager;
    private MyUserDetailsService userDetailsService;
    private JwtUtil jwtUtil;
    private TopicRepo topicRepo;

    @Autowired
    public UserService(UserRepo userRepo, PasswordEncoder passwordEncoder, AuthenticationManager authManager, MyUserDetailsService userDetailsService, JwtUtil jwtUtil
    ,TopicRepo topicRepo) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.authManager = authManager;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
        this.topicRepo = topicRepo;
    }

    public void register(RegisterRequest registerRequest) throws MailInUseException, LoginInUseException {
        for (User user : userRepo.findAll()) {
            if(user.getId().equals(registerRequest.getMail()))
                throw new MailInUseException();
            if(user.getLogin().equals(registerRequest.getUsername()))
                throw new LoginInUseException();
        }

        User user = new User();
        user.setId(registerRequest.getMail());
        user.setLogin(registerRequest.getUsername());
        user.setNickName(registerRequest.getNick());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRoles("ROLE_USER");
        user.setActive(true);

        userRepo.save(user);
        logger.info("{} has been registered at {}",user.getLogin(), new Date(System.currentTimeMillis()));
    }

    public String authenticate(AuthenticationRequest request) throws BadCredentialsException{
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()
        ));

        final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        logger.info("Successcul authorization at {}", new Date(System.currentTimeMillis()));
        return jwtUtil.generateToken(userDetails);
    }

    public Iterable<User> getAll(){
        return userRepo.findAll();
    }

    public User getByNickName(String nickName){
        Optional<User> found = userRepo.findByNickName(nickName);
        User user = null;
        if(found.isPresent()){
            user = found.get();
        }
        return user;
    }


    public List<Topic> getUsersTopics(String nickname) throws NickNameNotFoundException{
        Optional<User> user = userRepo.findByNickName(nickname);
        if(user.isEmpty()) throw new NickNameNotFoundException();
        return user.get().getTopics();
    }

    public List<Comment> getUsersComments(String nickname)  throws NickNameNotFoundException{
        Optional<User> user = userRepo.findByNickName(nickname);
        if(user.isEmpty()) throw new NickNameNotFoundException();
        return user.get().getComments();
    }

    public void changePassword(String jwt, String newPass) throws UsernameNotFoundException {
        String username = jwtUtil.extractUserName(jwt);
        System.out.println(username);
        Optional<User> userOptional = userRepo.findByLogin(username);
        if (userOptional.isEmpty())
            throw new UsernameNotFoundException(username);

        User user = userOptional.get();
        user.setPassword(passwordEncoder.encode(newPass));
        userRepo.save(user);
    }

   public void deleteTopic(String jwt, long id) throws UsernameNotFoundException, TopicNotFoundException {
       String username = jwtUtil.extractUserName(jwt);
       Optional<User> userOptional = userRepo.findByLogin(username);

       if (userOptional.isEmpty())
           throw new UsernameNotFoundException(username);
       User user = userOptional.get();
       for (int i = 0; i <userOptional.get().getTopics().size(); i++) {
           Topic topic = user.getTopics().get(i);
           if(topic.getId() == id) {
               user.getTopics().remove(topic);
               topicRepo.delete(topic);
               return;
           }

       }
       throw new TopicNotFoundException();
   }

    public void changeTopicBody(String jwt, long id, String body) throws UsernameNotFoundException, TopicNotFoundException {
        String username = jwtUtil.extractUserName(jwt);
        Optional<User> userOptional = userRepo.findByLogin(username);

        if (userOptional.isEmpty())
            throw new UsernameNotFoundException(username);
        User user = userOptional.get();
        for (int i = 0; i <userOptional.get().getTopics().size(); i++) {
            Topic topic = user.getTopics().get(i);
            if(topic.getId() == id) {
                topic.setBody(body);
                userRepo.save(user);
                return;
            }

        }
        throw new TopicNotFoundException();

    }

    public List<User> getAdmins() {
        Iterable<User> users = userRepo.findAll();
        List<User> admins = new ArrayList<>();

        for (User user : users) {
            if(user.getRoles().contains("ROLE_ADMIN")){
                admins.add(user);
            }
        }

        return admins;
    }

    public void blockUser(String nickname) throws NickNameNotFoundException {
        Optional<User> userOpt = userRepo.findByNickName(nickname);
        if(userOpt.isEmpty())
            throw new NickNameNotFoundException();

        User user = userOpt.get();
        user.setActive(false);
        userRepo.save(user);
    }

    public void unblockUser(String nickname) throws NickNameNotFoundException{
        Optional<User> userOpt = userRepo.findByNickName(nickname);
        if(userOpt.isEmpty())
            throw new NickNameNotFoundException();

        User user = userOpt.get();
        user.setActive(true);
        userRepo.save(user);
    }

    public void promote(String nickname) throws NickNameNotFoundException, UserAlreadyPromoted {
        Optional<User> userOptional = userRepo.findByNickName(nickname);
        if(userOptional.isEmpty())
            throw new NickNameNotFoundException();

        User user = userOptional.get();
        user.setActive(true);
        String role = user.getRoles();
        if(role.contains("ROLE_ADMIN"))
            throw new UserAlreadyPromoted();

        role = role +",ROLE_ADMIN";
        user.setRoles(role);
        userRepo.save(user);
    }

    public void deleteAccount(String jwt) throws UsernameNotFoundException{
        String username = jwtUtil.extractUserName(jwt);
        Optional<User> userOptional = userRepo.findByLogin(username);

        if (userOptional.isEmpty())
            throw new UsernameNotFoundException(username);

        User user = userOptional.get();
        userRepo.delete(user);
    }
}
