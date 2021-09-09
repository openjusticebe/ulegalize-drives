package com.ulegalize.service.onedrive.impl;

import com.ulegalize.domain.dto.EmailResponse;
import com.ulegalize.domain.dto.FileResponse;
import com.ulegalize.domain.dto.LawfirmToken;
import com.ulegalize.domain.dto.ObjectResponse;
import com.ulegalize.model.entity.LawfirmEntity;
import com.ulegalize.service.exception.RestException;
import com.ulegalize.service.lawfirm.LawfirmApi;
import com.ulegalize.service.onedrive.OneDriveService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.nuxeo.onedrive.client.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class OneDriveServiceImpl implements OneDriveService {
    @Value("${app.ulegalize.lawfirm.api}")
    private String URL_LAWFIRM_API;
    @Value("${app.onedrive.clientid}")
    private String CLIENT_ID;
    @Value("${app.onedrive.secret}")
    private String SECRET_TOKEN;
    @Value("${app.onedrive.api.authorize}")
    private String URL_GLOBAL_AUTHORIZE_REST;
    @Value("${app.onedrive.api.token}")
    private String URL_GLOBAL_REST;
    @Value("${app.onedrive.api.graph}")
    private String URL_GRAPH_REST;
    @Value("${app.onedrive.callback}")
    private String URL_CALLBACK;
    private String[] scope = {"files.readwrite.all", "offline_access"};

    @Autowired
    private LawfirmApi lawfirmApi;

    @Override
    public URL startToken(String vcKey) throws RestException {
        URL urlRedirect = null;
        URL urlOutput = null;

//        String redirectUri = "https://ulegalize.appspot.com/dropbox/finish";
        try {
            urlRedirect = getRedirectUrl();

        } catch (URISyntaxException e) {
            log.error("error URISyntaxException with url ");

        } catch (MalformedURLException e) {
            log.error("error MalformedURLException with url ");
        }

        try {

            String apiUrl = URL_GLOBAL_AUTHORIZE_REST.replaceAll("\\{client_id\\}", CLIENT_ID);
            apiUrl = apiUrl.replaceAll("\\{redirect_uri\\}", String.valueOf(urlRedirect));
            URI uri = new URI(apiUrl);
            urlOutput = uri.toURL();
        } catch (URISyntaxException e) {
            log.error("error URISyntaxException with url ");
            throw new RestException("Error during uri syntax");
        } catch (MalformedURLException e) {
            log.error("error MalformedURLException with url ");
            throw new RestException("Error during uri");
        } catch (Exception e) {
            log.error("Error while starting", e);
            throw new RestException("Error while starting", e);
        }

        return urlOutput;

    }

    private URL getRedirectUrl() throws URISyntaxException, MalformedURLException {
        URI uri = new URI(URL_CALLBACK);
        return uri.toURL();
    }

    @Override
    public String finishToken(String code) throws RestException {
        String token = null;

        try {
            log.info("Entering finish token with code {}", code);
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("client_id", CLIENT_ID);
            map.add("redirect_uri", getRedirectUrl().toURI().toString());
            map.add("client_secret", SECRET_TOKEN);
            map.add("code", code);
            map.add("grant_type", "authorization_code");

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

            ResponseEntity<ResponseToken> response = restTemplate.postForEntity(URL_GLOBAL_REST, request, ResponseToken.class);

            token = response.getBody().getAccess_token() + "&refresh=" + response.getBody().getRefresh_token();

        } catch (Exception e) {
            log.error("Error in finishToken: {}", e.getMessage(), e);
            throw new RestException("Error finishing token " + e.getMessage(), e);
        }

        return token;

    }

    @Override
    public String checkTokenExpire(LawfirmToken lawfirmToken) throws RestException {
        LawfirmEntity lawfirmEntity = lawfirmApi.getByVcKey(lawfirmToken.getVcKey(), lawfirmToken.getToken());
        String accessToken = lawfirmEntity.getOnedriveToken();

        // if the expire_in from token expired ask a new refresh token (3600s valid)
        if (lawfirmEntity.getExpireToken().plusMinutes(50).compareTo(LocalDateTime.now()) < 0) {
            accessToken = refreshToken(lawfirmEntity.getRefreshToken());
            LawfirmEntity payload = new LawfirmEntity();
            payload.setDriveType("onedrive");
            payload.setOnedriveToken(accessToken);
            payload.setRefreshToken(lawfirmEntity.getRefreshToken());
            lawfirmApi.updateToken(payload, lawfirmToken.getToken());
        }

        return accessToken;
    }

    private String refreshToken(String refreshToken) throws RestException {
        String accessToken = null;
        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("client_id", CLIENT_ID);
            map.add("redirect_uri", getRedirectUrl().toURI().toString());
            map.add("client_secret", SECRET_TOKEN);
            map.add("refresh_token", refreshToken);
            map.add("grant_type", "refresh_token");

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

            ResponseEntity<ResponseToken> response = restTemplate.postForEntity(URL_GLOBAL_REST, request, ResponseToken.class);

            accessToken = response.getBody().getAccess_token();

        } catch (Exception e) {
            log.error("Error in finishToken: {}", e.getMessage(), e);
            throw new RestException("Error finishing token " + e.getMessage(), e);
        }

        return accessToken;

    }

    @Override
    public List<ObjectResponse> getListFolder(String objPath, String accessToken) throws RestException {
        log.info("Entering getListFolder with objPath {} ", objPath);
        List<ObjectResponse> objectResponseList = new ArrayList<>();

        try {
            OneDriveFolder root;
            OneDriveAPI api = new OneDriveBasicAPI(accessToken);

            if (objPath != null && objPath.length() > 1) {
                log.debug("OneDriveFolder id {}", objPath);

                root = new OneDriveFolder(api, objPath);
            } else {
                log.debug("OneDriveFolder root api");
                root = OneDriveFolder.getRoot(api);
            }

            for (OneDriveItem.Metadata metadata : root.getChildren()) {
                ObjectResponse objectResponse = new ObjectResponse();

                if (metadata.isFile()) {
                    objectResponse.setName(metadata.getName());
//                    FileMetadata meta = (FileMetadata) metadata;

                    objectResponse.setEtag(metadata.getId());
                    objectResponse.setSize(metadata.getSize());
                    objectResponse.setLastModified(metadata.getLastModifiedDateTime().toLocalDate());
                } else {

                    // remove /drive/root: from parent path
                    objectResponse.setName(metadata.getParentReference().getPath().substring(12) + "/" + metadata.getName() + "/");
//                    FolderMetadata meta = (FolderMetadata) metadata;

                    objectResponse.setEtag(metadata.getId());
                    objectResponse.setSize(null);
                    objectResponse.setLastModified(metadata.getLastModifiedDateTime().toLocalDate());
                }
                objectResponse.setId(metadata.getId());

                objectResponse.setContainer(metadata.getParentReference().getId());

                objectResponseList.add(objectResponse);

            }

        } catch (Exception e) {
            log.error("Error in getListFolder: {}", e.getMessage(), e);
            throw new RestException("Error getListFolder " + e.getMessage(), e);
        }

        return objectResponseList;

    }

    @Override
    public List<ObjectResponse> folderByPath(String objPath, String accessToken) throws RestException {
        log.info("Entering getListFolder with objPath {} ", objPath);
        List<ObjectResponse> objectResponseList = new ArrayList<>();

        try {
            OneDriveFolder root;
            OneDriveAPI api = new OneDriveBasicAPI(accessToken);
            root = new OneDriveFolder(api, objPath);


            OneDriveFolder.Metadata metadata = root.getByPath();
            objectResponseList = getListFolder(metadata.getFolderId(), accessToken);

        } catch (Exception e) {
            log.error("Error in getListFolder: {}", e.getMessage(), e);
            throw new RestException("Error getListFolder " + e.getMessage(), e);
        }

        return objectResponseList;

    }

    @Override
    public FileResponse downloadFile(String accessToken, String path, String vcKey) throws RestException {
        if (path == null || path.isEmpty()) {
            throw new RestException("Not File selected");
        }

        FileResponse fileResponse = new FileResponse();

        try {
            OneDriveAPI api = new OneDriveBasicAPI(accessToken);

            OneDriveFile file = new OneDriveFile(api, path);
            InputStream inputStream = file.download();

            byte[] binaryArray = IOUtils.toByteArray(inputStream);
            String binary = Base64.getEncoder().encodeToString(binaryArray);

            fileResponse.setName(file.getMetadata().getName());
            fileResponse.setBinary(binary);
        } catch (IOException ex) {
            log.error(ex.getMessage());
            throw new RestException("Error while downloading file");

        }
        return fileResponse;

    }

    private void createSystemFolders(String accessToken) {
        List<String> arraySystemFolders = Arrays.asList("templates", "factures", "dossiers", "@postin");
        OneDriveAPI api = new OneDriveBasicAPI(accessToken);
        OneDriveFolder folderDrive = new OneDriveFolder(api, "");

        arraySystemFolders.forEach(folder -> {
            try {
                folderDrive.createFolder(true, folder);

//              Item item = oneDrive.drive().root().children().createFolder(folder, "rename");
                log.info("Un dossier {} créé avec succès lors de l'auth OneDrive.", folder);
            } catch (Exception e) {
                log.warn("Error while create folder {}", folder, e);
            }
        });

    }

    @Override
    public void createPath(String parentId, String newFolder, String accessToken) throws RestException {
        try {
            OneDriveAPI api = new OneDriveBasicAPI(accessToken);

            OneDriveFolder folder = new OneDriveFolder(api, parentId);

            OneDriveFolder.Metadata metadata = folder.createFolder(false, newFolder);
            log.debug("Folder {} created successfully", newFolder);
        } catch (OneDriveAPIException e) {
            log.error("Error while creating folder in onedrive with {} . get Response {}", parentId, e.getResponse(), e);
            throw new RestException("Error while creating folder ");
        } catch (IOException e) {
            log.error("Error while creating folder in onedrive with {} .", parentId, e);
            throw new RestException("Error while creating folder ");
        }
    }

    @Override
    public void removeObjects(String parentId, String accessToken) throws RestException {
        try {
            log.debug("Entering removeObjects id {}", parentId);
            OneDriveAPI api = new OneDriveBasicAPI(accessToken);

            OneDriveItem folder = new OneDriveFolder(api, parentId);
            folder.deleteItem();
            log.debug("Leaving removeObjects id {}", parentId);

        } catch (OneDriveAPIException e) {
            log.error("Error while deleting folder in onedrive with {}", parentId, e);
            throw new RestException("Error while deleting folder ");
        }

    }

    @Override
    public void renameObject(String parentId, String newFile, String accessToken) throws RestException {
        try {
            OneDriveAPI api = new OneDriveBasicAPI(accessToken);

            OneDriveFile file = new OneDriveFile(api, parentId);
            file.renameItem(newFile, "");
        } catch (OneDriveAPIException e) {
            log.error("Error while renaming folder in onedrive with {}", parentId, e);
            throw new RestException("Error while renaming folder ");
        }
    }

    @Override
    public void createFile(String path, MultipartFile file, String accessToken) throws RestException {
        try {
            OneDriveAPI api = new OneDriveBasicAPI(accessToken);
            // if the first 2 letters are ,,
            if (path.length() > 1 && path.substring(0, 2).equalsIgnoreCase(",,")) {
                path = path.substring(1);
            }

            path = path.replaceAll(",", "/") + file.getOriginalFilename();
            OneDriveFile fileDrive = new OneDriveFile(api, path);

            fileDrive.upload(file.getSize(), file.getInputStream());
        } catch (OneDriveAPIException e) {
            log.error("Error while uploading file message {} ", e.getResponse(), e);
            throw new RestException("Error while OneDriveAPIException " + e.getResponse(), e);
        } catch (IOException e) {
            log.error("Error while uploading file message {} ", e.getMessage(), e);
            throw new RestException("Error while IOException " + e.getMessage(), e);
        }
    }

    @Override
    public boolean shareFolder(String objPath, String message, List<String> sharedWith, String accessToken) throws RestException {
        try {
            OneDriveAPI api = new OneDriveBasicAPI(accessToken);
            OneDriveFile file;
            if (objPath == null) {
                file = new OneDriveFile(api);
            } else {
                file = new OneDriveFile(api, objPath);
            }
            file.createShare(sharedWith);
            log.info("File {} shared successfully with {}.", objPath, sharedWith);

            return true;
        } catch (OneDriveAPIException e) {
            log.error("Error while sharing file in onedrive with {}. message {}", objPath, e.getResponse(), e);
            throw new RestException("Error while renaming folder ");
        }
//        // Create Dropbox client
//        DbxRequestConfig config = DbxRequestConfig.newBuilder(CLIENT_ID).build();
//        DbxClientV2 client = new DbxClientV2(config, accessToken);
//        try {
//            List<AddMember> newMembers = new ArrayList<>();
//
//            for (String mail : sharedWith) {
//                MemberSelector newMember = MemberSelector.email(mail);
//                AddMember addMember = new AddMember(newMember);
//                newMembers.add(addMember);
//            }
//
//            client.sharing().addFolderMember(objPath, newMembers);
//
//            log.info("File {} shared successfully.", objPath);
//
//            return true;
//        } catch (DbxException e) {
//            log.error("Error while sharing file {}", objPath, e);
//            throw new RestException("Error while sharing file ");
//
//        }

    }

    @Override
    public String createShareLink(String parentId, String accessToken) throws RestException {
        try {
            OneDriveAPI api = new OneDriveBasicAPI(accessToken);
            OneDriveItem file;
            // root
            if (parentId == null) {
                file = new OneDriveFile(api);
            } else {
                file = new OneDriveFile(api, parentId);
            }
            OneDrivePermission.Metadata metadata = file.createSharedLink(OneDriveSharingLink.Type.VIEW);

            log.debug("web url {}", metadata.getLink().getWebUrl());
            return metadata.getLink().getWebUrl();
        } catch (OneDriveAPIException e) {
            log.error("Error while sharing link file in onedrive with {}. message {}", parentId, e.getResponse(), e);
            throw new RestException("Error while sharing link file");
        }
    }

    @Override
    public List<EmailResponse> getObjSharedWith(String id, Integer size, String accessToken) throws RestException {
        List<EmailResponse> emailResponseList = new ArrayList<>();

        emailResponseList = getObjSharedWithNoSize(false, id, accessToken);
//        if (size != null && size == 0) {
//        } else {
//            emailResponseList = getObjSharedWithNoSize(objPath, accessToken);
//        }

        return emailResponseList;
    }

    @Override
    public void deleteFileShared(String parentId, String permissionId, String accessToken) throws RestException {
        try {

            // get permission permissionId
            OneDriveAPI api = new OneDriveBasicAPI(accessToken);

            log.debug("Permissionid {}", permissionId);
            if (permissionId != null) {
                OneDrivePermission oneDrivePermission;
                if (parentId == null) {
                    oneDrivePermission = new OneDrivePermission(api, permissionId);
                } else {
                    oneDrivePermission = new OneDrivePermission(api, parentId, permissionId);
                }

                oneDrivePermission.delete();

                log.debug("Permission deleted");
            } else {
                log.warn("NO Permission deleted");
            }

        } catch (OneDriveAPIException e) {
            log.error("Error while deleting share file in onedrive with {}. message {}", parentId, e.getResponse(), e);
            throw new RestException("Error while deleting share link file");
        }
    }

    @Override
    public List<ObjectResponse> getListSharedObj(String accessToken, String vcKey) throws RestException {
        List<ObjectResponse> objectResponseList = new ArrayList<>();
        try {
            OneDriveAPI api = new OneDriveBasicAPI(accessToken);
            OneDriveItem file = new OneDriveFile(api);
            Iterable<OneDrivePermission.Metadata> metadataIterable = file.getShareList();
            Iterator iterator = metadataIterable.iterator();

            while (iterator.hasNext()) {
                OneDrivePermission.Metadata metadata = (OneDrivePermission.Metadata) iterator.next();
                ObjectResponse objectResponse = new ObjectResponse();

                objectResponse.setName(metadata.getGrantedTo().getUser().getDisplayName());
//                objectResponse.setUrl(metadata.getPreviewUrl());
                objectResponse.setEtag(metadata.getGrantedTo().getUser().getId());
                objectResponse.setSize(null);
                objectResponse.setContainer(vcKey);
                objectResponseList.add(objectResponse);
            }


            return objectResponseList;

        } catch (OneDriveAPIException e) {
            log.error("Error while sharing list file in onedrive with {}. message {}", vcKey, e.getResponse(), e);
            throw new RestException("Error while sharing list file");
        }
    }

    private List<EmailResponse> getObjSharedWithNoSize(boolean isRoot, String objPath, String accessToken) throws RestException {
        List<EmailResponse> emailResponseList = new ArrayList<>();
        try {
            OneDriveAPI api = new OneDriveBasicAPI(accessToken);
            OneDriveItem file;
            if (isRoot) {
                file = new OneDriveFile(api);
            } else {
                file = new OneDriveFile(api, objPath);
            }
            Iterable<OneDrivePermission.Metadata> metadataIterable = file.getShareList();
            Iterator iterator = metadataIterable.iterator();

            while (iterator.hasNext()) {
                OneDrivePermission.Metadata metadata = (OneDrivePermission.Metadata) iterator.next();
                if (metadata.getInvitation() != null) {
                    EmailResponse emailResponse = new EmailResponse(metadata.getPermissionId(), metadata.getInvitation().getEmail(), "");
                    emailResponseList.add(emailResponse);
                }
            }


            return emailResponseList;

        } catch (OneDriveAPIException e) {
            log.error("Error while sharing file {}", objPath, e);
            throw new RestException("Error while sharing list file");
        }
    }

}
