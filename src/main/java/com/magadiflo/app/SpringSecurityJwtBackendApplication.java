package com.magadiflo.app;

import com.magadiflo.app.constant.FileConstant;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.File;

@SpringBootApplication
public class SpringSecurityJwtBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringSecurityJwtBackendApplication.class, args);

		//Creando el directorio donde se almacenarán las imágenes
		//En el caso de mi pc sería: C:\Users\USUARIO\supportportal\user
		new File(FileConstant.USER_FOLDER).mkdirs();
	}

	@Bean
	public BCryptPasswordEncoder bCryptPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}

}
