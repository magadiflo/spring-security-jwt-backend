package com.magadiflo.app.service;

import com.magadiflo.app.domain.User;
import com.magadiflo.app.exception.domain.EmailExistException;
import com.magadiflo.app.exception.domain.UserNotFoundException;
import com.magadiflo.app.exception.domain.UsernameExistException;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import java.util.List;

public interface IUserService {

    User register(String firstName, String lastName, String username, String email)
            throws UserNotFoundException, EmailExistException, UsernameExistException, MessagingException;

    List<User> getUsers();

    User findUserByUsername(String username);

    User findUserByEmail(String email);

    User addNewUser(String firstName, String lastName, String username, String email, String role, boolean isNotLocked,
                    boolean isActive, MultipartFile profileImage) throws UserNotFoundException, EmailExistException, UsernameExistException;

    User updateUser(String currentUsername, String newFirstName, String newLastName, String newUsername, String newEmail,
                    String role, boolean isNotLocked, boolean isActive, MultipartFile profileImage);

    void deleteUser(Long id);

    void resetPassword(String email);

    User updateProfileImage(String username, MultipartFile profileImage);
}
