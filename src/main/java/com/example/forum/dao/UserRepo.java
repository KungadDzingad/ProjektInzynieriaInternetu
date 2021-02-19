package com.example.forum.dao;

import com.example.forum.models.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepo extends CrudRepository<User,String> {
    Optional<User> findByLogin(String login);
    Optional<User> findByNickName(String nickName);
}
