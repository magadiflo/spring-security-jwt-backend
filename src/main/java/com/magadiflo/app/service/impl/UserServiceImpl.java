package com.magadiflo.app.service.impl;

import com.magadiflo.app.domain.User;
import com.magadiflo.app.domain.UserPrincipal;
import com.magadiflo.app.enumeration.Role;
import com.magadiflo.app.exception.domain.EmailExistException;
import com.magadiflo.app.exception.domain.UserNotFoundException;
import com.magadiflo.app.exception.domain.UsernameExistException;
import com.magadiflo.app.repository.IUserRepository;
import com.magadiflo.app.service.IUserService;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Date;
import java.util.List;

@Service
@Transactional
@Qualifier("userDetailsService")
public class UserServiceImpl implements IUserService, UserDetailsService {

    //getClass(), es propio de cada clase y devuelve la clase
    //Es como se hiciera UserServiceImpl.class
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final IUserRepository userRepository;

    private BCryptPasswordEncoder passwordEncoder;

    @Autowired //Inyección de Dependencia basada en el constructor (Es la recomendada)
    public UserServiceImpl(IUserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Método al que se llama cada vez que Spring Security
     * intenta comprobar la autenticación del usuario
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = this.userRepository.findUserByUsername(username);
        if (user == null) {
            logger.error("User not found by username: {}", username);
            throw new UsernameNotFoundException("User not found by username: " + username);
        } else {
            user.setLastLoginDateDisplay(user.getLastLoginDate());
            user.setLastLoginDate(new Date());

            this.userRepository.save(user);

            UserPrincipal userPrincipal = new UserPrincipal(user);
            logger.info("Returning found user by username: {}", username);
            return userPrincipal;
        }
    }

    @Override
    public User register(String firstName, String lastName, String username, String email)
            throws UserNotFoundException, EmailExistException, UsernameExistException {

        this.validateNewUsernameAndEmail(StringUtils.EMPTY, username, email);
        String password = this.generatePassword();
        String encodePassword = this.encodePassword(password);

        User user = new User();
        user.setUserId(this.generateUserId());
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUsername(username);
        user.setEmail(email);
        user.setJoinDate(new Date());
        user.setPassword(encodePassword);
        user.setActive(true);
        user.setNotLocked(true);
        user.setRole(Role.ROLE_USER.name());
        user.setAuthorities(Role.ROLE_USER.getAuthorities());
        user.setProfileImageUrl(this.getTemporaryProfileImageUrl());

        this.userRepository.save(user);
        logger.info("New user password {}", password);

        //TODO: send password to user registered

        return user;
    }

    @Override
    public List<User> getUsers() {
        return null;
    }

    @Override
    public User findUserByUsername(String username) {
        return null;
    }

    @Override
    public User findUserByEmail(String email) {
        return null;
    }

    private User validateNewUsernameAndEmail(String currentUsername, String newUsername, String newEmail)
            throws UserNotFoundException, UsernameExistException, EmailExistException {

        if (StringUtils.isNotBlank(currentUsername)) {//Si el currentUsername no está en blanco, entonces se está tratando de ACTUALIZAR
            //Verificamos si existe el usuario con el username actual proporcionado
            User currentUser = this.findUserByUsername(currentUsername);
            if (currentUser == null) {
                throw new UserNotFoundException("No user found by username ".concat(currentUsername));
            }

            //Verificamos si el nuevo username ya alguien lo tiene registrado y además no es el mismo usuario actual
            User userByNewUsername = this.findUserByUsername(newUsername);
            if (userByNewUsername != null && !currentUser.getId().equals(userByNewUsername.getId())) {
                throw new UsernameExistException("Username already exists");
            }

            //Verificamos si el nuevo email ya alguien lo tiene registrado y además no es el mismo usuario actual
            User userByNewEmail = this.findUserByEmail(newEmail);
            if (userByNewEmail != null && !currentUser.getId().equals(userByNewEmail.getId())) {
                throw new EmailExistException("Email already exists");
            }

            return currentUser;
        } else { //Si es un nuevo usuario que se intenta crear
            User userByUsername = this.findUserByUsername(newUsername);
            if (userByUsername != null) { //Ese newUsername ya alguien lo tiene tomado
                throw new UsernameExistException("Username already exists");
            }

            User userByEmail = this.findUserByEmail(newEmail);
            if (userByEmail != null) { //Ese newEmail ya alguien lo tiene tomado
                throw new EmailExistException("Email already exists");
            }

            return null;
        }
    }

    private String generateUserId() {
        return RandomStringUtils.randomNumeric(10);
    }

    private String generatePassword() {
        return RandomStringUtils.randomAlphanumeric(10);
    }

    private String encodePassword(String password) {
        return this.passwordEncoder.encode(password);
    }

    private String getTemporaryProfileImageUrl() {
        //ServletUriComponentsBuilder.fromCurrentContextPath(), devuelve cualquiera sea la URL del servidor real
        //Por ejemplo si estamos en local sería: http://localhost:8081
        return ServletUriComponentsBuilder.fromCurrentContextPath().path("/user/image/profile/temp").toUriString();
    }
}
