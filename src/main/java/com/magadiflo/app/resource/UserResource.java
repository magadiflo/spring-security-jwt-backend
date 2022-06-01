package com.magadiflo.app.resource;

import com.magadiflo.app.domain.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/users")
public class UserResource {

    @GetMapping("/home")
    public String home() {
        return "Application works";
    }

}
