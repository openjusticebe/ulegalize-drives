package com.ulegalize.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class InboxDTO {


    private String id;
    private String filename;
    private Date recDate;

    public InboxDTO() {
    }


}
