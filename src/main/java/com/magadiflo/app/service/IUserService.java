package com.magadiflo.app.service;

import com.magadiflo.app.domain.User;

import java.util.List;

public interface IUserService {

    User register(String firstName, String lastName, String username, String email);

    List<User> getUsers();

    User findUserByUsername(String username);

    User findUserByEmail(String email);

}
