package com.ulegalize.controller.onedrive;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class OneDriveToken implements Serializable {
    private String onedriveToken;
    private String onedriveRefreshtoken;
    private LocalDateTime onedriveExpiretoken;
}
