package com.magadiflo.app.service;

import com.magadiflo.app.domain.User;
import com.magadiflo.app.exception.domain.EmailExistException;
import com.magadiflo.app.exception.domain.UserNotFoundException;
import com.magadiflo.app.exception.domain.UsernameExistException;

import java.util.List;

public interface IUserService {

    User register(String firstName, String lastName, String username, String email) throws UserNotFoundException, EmailExistException, UsernameExistException;

    List<User> getUsers();

    User findUserByUsername(String username);

    User findUserByEmail(String email);

}
