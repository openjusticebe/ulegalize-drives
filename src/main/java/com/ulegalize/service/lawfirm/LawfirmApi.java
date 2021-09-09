package com.ulegalize.service.lawfirm;

import com.ulegalize.model.entity.LawfirmEntity;
import com.ulegalize.service.exception.RestException;

public interface LawfirmApi {
    /**
     * this is an example of Lawfirm call NOT USED
     *
     * @param payload
     * @param internalToken
     * @throws RestException
     */
    public void updateToken(LawfirmEntity payload, String internalToken) throws RestException;

    public LawfirmEntity getByVcKey(String vckey, String internalToken) throws RestException;
}
