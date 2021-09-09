package com.ulegalize.service.v2.dropbox;

import com.ulegalize.domain.dto.FileResponse;
import com.ulegalize.domain.dto.ObjectResponse;
import com.ulegalize.service.CommonDriveService;
import com.ulegalize.service.exception.RestException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DropBoxV2Service extends CommonDriveService {
    boolean checkSession(String dropboxToken);

    List<ObjectResponse> getListFolder(String dropboxToken, String objPath, String vcKey) throws RestException;

    FileResponse downloadFile(String dropboxToken, String path, String vcKey) throws RestException;

    void createPath(String path, String dropboxToken) throws RestException;

    void removeObjects(String path, String dropboxToken) throws RestException;

    void renameObject(String fromPath, String toPath, String dropboxToken) throws RestException;

    void createFile(String path, String filename, MultipartFile file, String dropboxToken) throws RestException;

    boolean shareFile(String objPath, String message, List<String> sharedWith, String dropboxToken) throws RestException;

    boolean shareFolder(String objPath, String message, List<String> sharedWith, String dropboxToken) throws RestException;

    String createShareLink(String objPath, String dropboxToken) throws RestException;

    void deleteFileShared(String objPath, String deletedWith, String dropboxToken) throws RestException;

    void deleteFolderShared(String objPath, String deletedWith, String dropboxToken) throws RestException;

}