package com.ulegalize.domain.dto;

import lombok.Data;

@Data
public class RequestDropbox {
    String path;
    String newPath;

    public RequestDropbox() {
    }

}
