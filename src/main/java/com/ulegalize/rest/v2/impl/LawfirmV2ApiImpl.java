package com.ulegalize.rest.v2.impl;

import com.ulegalize.domain.dto.LawfirmToken;
import com.ulegalize.dto.ProfileDTO;
import com.ulegalize.exception.RestTemplateResponseErrorHandler;
import com.ulegalize.rest.v2.LawfirmV2Api;
import com.ulegalize.security.EnumRights;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class LawfirmV2ApiImpl implements LawfirmV2Api {
    @Value("${app.ulegalize.lawfirm.api}")
    String URL_LAWFIRM_API;

    private RestTemplate restTemplate;

    public LawfirmV2ApiImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.restTemplate.setErrorHandler(new RestTemplateResponseErrorHandler());
    }

    @Override
    public LawfirmToken getUserProfile(String email, String token) throws ResponseStatusException {
        log.debug("Entering getUserProfile with email : {}", email);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", "Bearer " + token);
        HttpEntity request = new HttpEntity(headers);
        ResponseEntity<ProfileDTO> response = restTemplate.exchange(URL_LAWFIRM_API + "/v2/login/user", HttpMethod.GET, request, ProfileDTO.class);
        ProfileDTO profileDTO = response.getBody();
        List<EnumRights> enumRights = new ArrayList<>();

        if (profileDTO != null) {
            enumRights = profileDTO.getEnumRights().stream().map(EnumRights::fromId).collect(Collectors.toList());
        }

        return new LawfirmToken(profileDTO.getId(), profileDTO.getFullName(),
                profileDTO.getEmail(), profileDTO.getVcKeySelected(), "", true,
                enumRights,
                token,
                profileDTO.isTemporaryVc(),
                profileDTO.getLanguage(),
                profileDTO.getSymbolCurrency(),
                profileDTO.getFullName(),
                profileDTO.getDriveType(), profileDTO.getDropboxToken());
    }
}
