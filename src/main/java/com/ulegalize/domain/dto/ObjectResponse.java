package com.ulegalize.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

public class ObjectResponse {
    @Getter
    @Setter
    String id;
    @Getter
    @Setter
    String name;
    @Getter
    @Setter
    String url;
    @Getter
    @Setter
    String etag;
    @Getter
    @Setter
    Long size;
    @Getter
    @Setter
    LocalDate lastModified;
    @Getter
    @Setter
    String container;

}
