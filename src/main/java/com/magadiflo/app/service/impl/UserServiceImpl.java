package com.magadiflo.app.service.impl;

import static com.magadiflo.app.constant.UserImplConstant.*;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import com.magadiflo.app.constant.FileConstant;
import com.magadiflo.app.domain.User;
import com.magadiflo.app.domain.UserPrincipal;
import com.magadiflo.app.enumeration.Role;
import com.magadiflo.app.exception.domain.*;
import com.magadiflo.app.repository.IUserRepository;
import com.magadiflo.app.service.EmailService;
import com.magadiflo.app.service.IUserService;
import com.magadiflo.app.service.LoginAttemptService;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.mail.MessagingException;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
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

    private final BCryptPasswordEncoder passwordEncoder;

    private final LoginAttemptService loginAttemptService;

    private final EmailService emailService;

    @Autowired
    //Inyección de Dependencia basada en el constructor, en este tipo de inyección ya no sería necesario el @Autowired
    public UserServiceImpl(IUserRepository userRepository, BCryptPasswordEncoder passwordEncoder,
                           LoginAttemptService loginAttemptService, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.loginAttemptService = loginAttemptService;
        this.emailService = emailService;
    }

    /**
     * Método al que se llama cada vez que Spring Security
     * intenta comprobar la autenticación del usuario
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = this.userRepository.findUserByUsername(username);
        if (user == null) {
            logger.error(NO_USER_FOUND_BY_USERNAME.concat("{}"), username);
            throw new UsernameNotFoundException(NO_USER_FOUND_BY_USERNAME.concat(username));
        } else {
            this.validateLoginAttempt(user);

            user.setLastLoginDateDisplay(user.getLastLoginDate());
            user.setLastLoginDate(new Date());

            this.userRepository.save(user);

            UserPrincipal userPrincipal = new UserPrincipal(user);
            logger.info(RETURNING_FOUND_USER_BY_USERNAME.concat("{}"), username);
            return userPrincipal;
        }
    }

    @Override
    public User register(String firstName, String lastName, String username, String email)
            throws UserNotFoundException, EmailExistException, UsernameExistException, MessagingException {

        this.validateNewUsernameAndEmail(StringUtils.EMPTY, username, email);
        String password = this.generatePassword();

        User user = new User();
        user.setUserId(this.generateUserId());
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUsername(username);
        user.setEmail(email);
        user.setJoinDate(new Date());
        user.setPassword(this.encodePassword(password));
        user.setActive(true);
        user.setNotLocked(true);
        user.setRole(Role.ROLE_USER.name());
        user.setAuthorities(Role.ROLE_USER.getAuthorities());
        user.setProfileImageUrl(this.getTemporaryProfileImageUrl(username));

        this.userRepository.save(user);
        logger.info("Register New user password: {}", password);
        this.emailService.sendNewPasswordEmail(firstName, password, email);

        return user;
    }

    @Override
    public List<User> getUsers() {
        return this.userRepository.findAll();
    }

    @Override
    public User findUserByUsername(String username) {
        return this.userRepository.findUserByUsername(username);
    }

    @Override
    public User findUserByEmail(String email) {
        return this.userRepository.findUserByEmail(email);
    }

    @Override
    public User addNewUser(String firstName, String lastName, String username, String email, String role,
                           boolean isNotLocked, boolean isActive, MultipartFile profileImage)
            throws UserNotFoundException, EmailExistException, UsernameExistException, IOException, NotAnImageFileException {

        this.validateNewUsernameAndEmail(StringUtils.EMPTY, username, email);

        String password = this.generatePassword();

        User user = new User();
        user.setUserId(this.generateUserId());
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUsername(username);
        user.setEmail(email);
        user.setJoinDate(new Date());
        user.setPassword(this.encodePassword(password));
        user.setActive(isActive);
        user.setNotLocked(isNotLocked);
        user.setRole(this.getRoleEnumName(role).name());
        user.setAuthorities(this.getRoleEnumName(role).getAuthorities());
        user.setProfileImageUrl(this.getTemporaryProfileImageUrl(username));

        this.userRepository.save(user);

        this.saveProfileImage(user, profileImage);
        logger.info("Add New user password: ".concat(password));

        return user;
    }

    @Override
    public User updateUser(String currentUsername, String newFirstName, String newLastName, String newUsername,
                           String newEmail, String role, boolean isNotLocked, boolean isActive, MultipartFile profileImage)
            throws UserNotFoundException, EmailExistException, UsernameExistException, IOException, NotAnImageFileException {

        User currentUser = this.validateNewUsernameAndEmail(currentUsername, newUsername, newEmail);
        currentUser.setFirstName(newFirstName);
        currentUser.setLastName(newLastName);
        currentUser.setUsername(newUsername);
        currentUser.setEmail(newEmail);
        currentUser.setActive(isActive);
        currentUser.setNotLocked(isNotLocked);
        currentUser.setRole(this.getRoleEnumName(role).name());
        currentUser.setAuthorities(this.getRoleEnumName(role).getAuthorities());

        this.userRepository.save(currentUser);

        this.saveProfileImage(currentUser, profileImage);

        return currentUser;
    }

    @Override
    public void deleteUser(String username) throws UserNotFoundException, IOException {
        User user = this.userRepository.findUserByUsername(username);
        if (user == null) {
            throw new UserNotFoundException(NO_USER_FOUND_BY_USERNAME.concat(username));
        }
        Path userFolder = Paths.get(FileConstant.USER_FOLDER.concat(user.getUsername())).toAbsolutePath().normalize();
        FileUtils.deleteDirectory(new File(userFolder.toString()));
        this.userRepository.deleteById(user.getId());
    }

    @Override
    public void resetPassword(String email) throws EmailNotFoundException, MessagingException {
        User user = this.userRepository.findUserByEmail(email);
        if (user == null) {
            throw new EmailNotFoundException(NO_USER_FOUND_BY_EMAIL.concat(email));
        }
        String password = this.generatePassword();
        user.setPassword(this.encodePassword(password));

        logger.info("Reset password: ".concat(password));

        this.userRepository.save(user);
        this.emailService.sendNewPasswordEmail(user.getFirstName(), password, user.getEmail());
    }

    @Override
    public User updateProfileImage(String username, MultipartFile profileImage)
            throws UserNotFoundException, EmailExistException, UsernameExistException, IOException,
            NotAnImageFileException {
        User user = this.validateNewUsernameAndEmail(username, null, null);
        this.saveProfileImage(user, profileImage);
        return user;
    }

    private User validateNewUsernameAndEmail(String currentUsername, String newUsername, String newEmail)
            throws UserNotFoundException, UsernameExistException, EmailExistException {

        User userByNewUsername = this.findUserByUsername(newUsername);
        User userByNewEmail = this.findUserByEmail(newEmail);

        if (StringUtils.isNotBlank(currentUsername)) {//Si existe el currentUsername se está tratando de ACTUALIZAR
            //Verificamos si existe el usuario con el username actual proporcionado
            User currentUser = this.findUserByUsername(currentUsername);
            if (currentUser == null) {
                throw new UserNotFoundException(NO_USER_FOUND_BY_USERNAME.concat(currentUsername));
            }

            if (userByNewUsername != null && !currentUser.getId().equals(userByNewUsername.getId())) {
                throw new UsernameExistException(USERNAME_ALREADY_EXISTS);
            }

            if (userByNewEmail != null && !currentUser.getId().equals(userByNewEmail.getId())) {
                throw new EmailExistException(EMAIL_ALREADY_EXISTS);
            }

            return currentUser;
        } else { //Si es un nuevo usuario que se intenta crear
            if (userByNewUsername != null) {
                throw new UsernameExistException(USERNAME_ALREADY_EXISTS);
            }

            if (userByNewEmail != null) {
                throw new EmailExistException(EMAIL_ALREADY_EXISTS);
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

    private String getTemporaryProfileImageUrl(String username) {
        //ServletUriComponentsBuilder.fromCurrentContextPath(), devuelve cualquiera sea la URL del servidor real
        //Por ejemplo si estamos en local sería: http://localhost:8081
        return ServletUriComponentsBuilder.fromCurrentContextPath().path(FileConstant.DEFAULT_USER_IMAGE_PATH.concat(username)).toUriString();
    }

    private void validateLoginAttempt(User user) {
        if (user.isNotLocked()) {
            if (this.loginAttemptService.hasExceededMaxAttempts(user.getUsername())) {
                user.setNotLocked(false); //La cuenta será bloqueada
            } else {
                user.setNotLocked(true); //La cuenta no estará bloqueada
            }
        } else { //Como la cuenta está bloqueada, solo para estar seguros eliminamos el usuario de la caché, si alguna vez estuvieron
            this.loginAttemptService.evictUserFromLoginAttemptCache(user.getUsername());
        }
    }

    private Role getRoleEnumName(String role) {
        return Role.valueOf(role.toUpperCase());
    }

    private void saveProfileImage(User user, MultipartFile profileImage) throws IOException, NotAnImageFileException {
        if (profileImage != null) {
            if (!Arrays.asList(MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE, MediaType.IMAGE_GIF_VALUE).contains(profileImage.getContentType())) {
                throw new NotAnImageFileException(profileImage.getOriginalFilename().concat(" is not image file. Please upload an image."));
            }
            Path userFolder = Paths.get(FileConstant.USER_FOLDER.concat(user.getUsername())).toAbsolutePath().normalize();
            if (!Files.exists(userFolder)) {
                Files.createDirectories(userFolder);
                logger.info(FileConstant.DIRECTORY_CREATED.concat(String.valueOf(userFolder)));
            }
            Files.deleteIfExists(Paths.get(userFolder + FileConstant.FORWARD_SLASH + user.getUsername() + FileConstant.DOT + FileConstant.JPG_EXTENSION)); //Elimina la imagen si existe
            Files.copy(profileImage.getInputStream(), userFolder.resolve(user.getUsername() + FileConstant.DOT + FileConstant.JPG_EXTENSION), REPLACE_EXISTING); //Con esta línea podríamos obviar la línea anterior, pero para estar seguros de que será una nueva imagen lo dejamos

            user.setProfileImageUrl(this.setProfileImageUrl(user.getUsername()));

            this.userRepository.save(user);
            logger.info(FileConstant.FILE_SAVED_IN_FILE_SYSTEM.concat(profileImage.getOriginalFilename()));
        }
    }

    private String setProfileImageUrl(String username) {
        return ServletUriComponentsBuilder.fromCurrentContextPath().path(FileConstant.USER_IMAGE_PATH + username +
                FileConstant.FORWARD_SLASH + username + FileConstant.DOT + FileConstant.JPG_EXTENSION).toUriString();
    }
}
