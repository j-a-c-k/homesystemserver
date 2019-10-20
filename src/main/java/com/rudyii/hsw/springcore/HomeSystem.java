package com.rudyii.hsw.springcore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;

@EnableGlobalMethodSecurity(prePostEnabled = true)
@SpringBootApplication
@Configuration
@ImportResource("classpath:app-context.xml")
public class HomeSystem extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(HomeSystem.class, args);
    }
}
