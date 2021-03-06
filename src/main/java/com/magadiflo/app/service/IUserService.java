package com.magadiflo.app.service;

import com.magadiflo.app.domain.User;
import com.magadiflo.app.exception.domain.*;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.List;

public interface IUserService {

    User register(String firstName, String lastName, String username, String email)
            throws UserNotFoundException, EmailExistException, UsernameExistException, MessagingException;

    List<User> getUsers();

    User findUserByUsername(String username);

    User findUserByEmail(String email);

    User addNewUser(String firstName, String lastName, String username, String email, String role, boolean isNotLocked,
                    boolean isActive, MultipartFile profileImage) throws UserNotFoundException, EmailExistException, UsernameExistException, IOException, NotAnImageFileException;

    User updateUser(String currentUsername, String newFirstName, String newLastName, String newUsername, String newEmail,
                    String role, boolean isNotLocked, boolean isActive, MultipartFile profileImage) throws UserNotFoundException, EmailExistException, UsernameExistException, IOException, NotAnImageFileException;

    void deleteUser(String username) throws UserNotFoundException, IOException;

    void resetPassword(String email) throws EmailNotFoundException, MessagingException;

    User updateProfileImage(String username, MultipartFile profileImage) throws UserNotFoundException, EmailExistException, UsernameExistException, IOException, NotAnImageFileException;
}
