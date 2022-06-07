package com.magadiflo.app.service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Almacenará en la caché el número de intentos fallidos
 * al iniciar sesión (usando la librería Guava).
 * Se configura para que el usuario tenga únicamente 5 intentos.
 * Con esta funcionalidad se quiere proteger a la aplicación
 * de Ataques de Fuerza Bruta.
 * <p>
 * ------- CACHE --------
 * USER             ATTEMPTS
 * user 1           1
 * user 2           3
 * user 3           2
 * <p>
 * Configuración usando librería guava de google
 * <a href="https://github.com/google/guava/wiki/CachesExplained">Google/Guava</a>
 */

@Service
public class LoginAttemptService {

    private static final int MAXIMUM_NUMBER_OF_ATTEMPTS = 5;
    private static final int ATTEMPT_INCREMENT = 1;
    private LoadingCache<String, Integer> loginAttemptCache;

    //Inicializando la Caché dentro del constructor
    //maximumSize(100), tendremos 100 entradas en la caché como máximo
    public LoginAttemptService() {
        super();
        this.loginAttemptCache = CacheBuilder.newBuilder().expireAfterWrite(15, TimeUnit.MINUTES)
                .maximumSize(100).build(new CacheLoader<String, Integer>() {
                    @Override
                    public Integer load(String key) throws Exception {
                        return 0;
                    }
                });
    }

    //Desalojar al usuario de la caché de intento de inicio de sesión
    //Elimina al usuario de la memoria de caché
    public void evictUserFromLoginAttemptCache(String username) {
        this.loginAttemptCache.invalidate(username);//Buscará una key con el "username" y lo eliminará junto a su valor
    }

    public void addUserToLoginAttemptCache(String username) {
        int attempts = 0;
        try {
            attempts = ATTEMPT_INCREMENT + this.loginAttemptCache.get(username);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        this.loginAttemptCache.put(username, attempts);
    }

    public boolean hasExceededMaxAttempts(String username) {
        try {
            return this.loginAttemptCache.get(username) >= MAXIMUM_NUMBER_OF_ATTEMPTS;
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return false;
    }

}
