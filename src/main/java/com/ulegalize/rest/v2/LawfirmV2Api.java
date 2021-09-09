package com.ulegalize.rest.v2;

import com.ulegalize.domain.dto.LawfirmToken;
import org.springframework.web.server.ResponseStatusException;

public interface LawfirmV2Api {
    LawfirmToken getUserProfile(String email, String token) throws ResponseStatusException;
}
