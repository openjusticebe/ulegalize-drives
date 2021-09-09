package com.ulegalize.service.onedrive;

import com.ulegalize.domain.dto.FileResponse;
import com.ulegalize.domain.dto.LawfirmToken;
import com.ulegalize.domain.dto.ObjectResponse;
import com.ulegalize.service.CommonDriveService;
import com.ulegalize.service.exception.RestException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface OneDriveService extends CommonDriveService {
    /**
     * check before each call the token
     *
     * @param lawfirmToken
     * @return
     * @throws RestException
     */
    String checkTokenExpire(LawfirmToken lawfirmToken) throws RestException;

    List<ObjectResponse> getListFolder(String objPath, String accessToken) throws RestException;

    List<ObjectResponse> folderByPath(String objPath, String accessToken) throws RestException;

    FileResponse downloadFile(String dropboxToken, String path, String vcKey) throws RestException;

    void createPath(String parentId, String newFolder, String accessToken) throws RestException;

    void removeObjects(String parentId, String accessToken) throws RestException;

    void renameObject(String parentId, String newFile, String accessToken) throws RestException;

    void createFile(String path, MultipartFile file, String accessToken) throws RestException;

    boolean shareFolder(String objPath, String message, List<String> sharedWith, String accessToken) throws RestException;

    String createShareLink(String parentId, String accessToken) throws RestException;

    void deleteFileShared(String objPath, String deletedWith, String accessToken) throws RestException;

}