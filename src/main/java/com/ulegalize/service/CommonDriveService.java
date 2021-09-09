package com.ulegalize.service;

import com.ulegalize.domain.dto.EmailResponse;
import com.ulegalize.domain.dto.ObjectResponse;
import com.ulegalize.service.exception.RestException;

import java.net.URL;
import java.util.List;

public interface CommonDriveService {
    URL startToken(String vcKey) throws RestException;

    String finishToken(String code) throws RestException;

    /**
     * Object shared with
     *
     * @param objPath
     * @param size
     * @param accessToken
     * @return the list of members
     * @throws RestException
     */
    List<EmailResponse> getObjSharedWith(String objPath, Integer size, String accessToken) throws RestException;

    List<ObjectResponse> getListSharedObj(String accessToken, String vcKey) throws RestException;

}
