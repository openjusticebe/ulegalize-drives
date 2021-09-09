package com.ulegalize.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LawfirmEntity {

    private String vckey;

    private String name;

    private String alias;

    private String street;
    private String city;
    private String postalCode;
    private String countryCode;
    private String email;
    private String companyNumber;
    private String phoneNumber;
    private String fax;
    private String website;
    private String driveType;
    private String dropboxToken;
    private String onedriveToken;
    private LocalDateTime expireToken;
    private String refreshToken;

}
