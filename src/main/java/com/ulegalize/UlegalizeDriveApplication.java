package com.ulegalize;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class UlegalizeDriveApplication {

    public static void main(String[] args) {
        SpringApplication.run(UlegalizeDriveApplication.class, args);

        log.info("WELCOME to ulegal Drive RESTful");
    }

}
