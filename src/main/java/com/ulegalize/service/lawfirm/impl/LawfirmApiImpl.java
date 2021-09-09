package com.ulegalize.service.lawfirm.impl;

import com.ulegalize.model.entity.LawfirmEntity;
import com.ulegalize.service.exception.RestException;
import com.ulegalize.service.lawfirm.LawfirmApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

@Service
@Slf4j
public class LawfirmApiImpl implements LawfirmApi {
    @Value("${app.ulegalize.lawfirm.api}")
    String URL_LAWFIRM_API;

    @Override
    public void updateToken(LawfirmEntity payload, String internalToken) throws RestException {
        try {
            log.debug("Entering updateToken with payload : {}", payload);
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("x-access-token", internalToken);

            HttpEntity<LawfirmEntity> request = new HttpEntity<>(payload, headers);
            restTemplate.put(URL_LAWFIRM_API + "/lawfirm/update-token", request);
        } catch (Exception e) {
            throw new RestException("Error while updateToken " + e.getMessage(), e);
        }

    }

    @Override
    public LawfirmEntity getByVcKey(String vckey, String internalToken) throws RestException {
        try {
            log.debug("Entering getByVcKey with vckey : {}", vckey);
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("x-access-token", internalToken);
            HttpEntity request = new HttpEntity(headers);
            ResponseEntity<LawfirmEntity> response = restTemplate.exchange(URL_LAWFIRM_API + "/lawfirm/" + vckey, HttpMethod.GET, request, LawfirmEntity.class);

            return response.getBody();
        } catch (Exception e) {
            throw new RestException("Error while getByVcKey " + e.getMessage(), e);
        }

    }
}
