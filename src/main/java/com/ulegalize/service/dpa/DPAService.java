package com.ulegalize.service.dpa;

import com.ulegalize.service.exception.RestException;

import java.net.URL;

public interface DPAService {

    URL startToken(String vcKey) throws RestException;

    String finishToken(String code) throws RestException;
    void calculatePrice() throws RestException;

}