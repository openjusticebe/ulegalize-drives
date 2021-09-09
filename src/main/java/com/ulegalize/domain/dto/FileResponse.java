package com.ulegalize.domain.dto;

import lombok.Getter;
import lombok.Setter;

public class FileResponse {
    @Getter
    @Setter
    String contentType;
    @Getter
    @Setter
    String name;
    @Getter
    @Setter
    String binary;
}
