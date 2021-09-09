package com.ulegalize.service.openstack;

import com.ulegalize.domain.dto.FileResponse;
import com.ulegalize.domain.dto.ObjectResponse;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;

public interface IJCloudsSwift {
    public void createContainer(String container) throws ResponseStatusException;

    public void uploadObjectFromString(String container, String objName, MultipartFile file) throws IOException;

    public FileResponse downloadObjectFromString(String container, String objName) throws IOException;

    public void listContainers();

    /**
     * NOT Working at 100%
     *
     * @param containerName
     * @param path
     * @return
     * @throws IOException
     */
    public List<ObjectResponse> listObject(String containerName, String path) throws ResponseStatusException;

    public ObjectResponse createFolder(String containerName, String path) throws ResponseStatusException;

}