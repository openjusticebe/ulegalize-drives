package com.ulegalize.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@AllArgsConstructor
public class EmailResponse implements Serializable {
    @Getter
    @Setter
    String id;
    @Getter
    @Setter
    String email;
    @Getter
    @Setter
    String fullname;
}
