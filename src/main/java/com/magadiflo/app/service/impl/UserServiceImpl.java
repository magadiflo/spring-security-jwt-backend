package com.magadiflo.app.service.impl;

import com.magadiflo.app.domain.User;
import com.magadiflo.app.domain.UserPrincipal;
import com.magadiflo.app.repository.IUserRepository;
import com.magadiflo.app.service.IUserService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Autowired //Inyección de Dependencia basada en el constructor (Es la recomendada)
    public UserServiceImpl(IUserRepository userRepository) {
        this.userRepository = userRepository;
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
    public User register(String firstName, String lastName, String username, String email) {
        //this.validateNewUsernameAndEmail();
        return null;
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

    private User validateNewUsernameAndEmail(String currentUsername, String newUsername, String newEmail) {
        if (StringUtils.isNotBlank(currentUsername)) {
            User currentUser = this.findUserByUsername(currentUsername);
            if (currentUser == null) {
                throw new UsernameNotFoundException("No user found by username ".concat(currentUsername));
            }
        }
        return null;
    }
}
