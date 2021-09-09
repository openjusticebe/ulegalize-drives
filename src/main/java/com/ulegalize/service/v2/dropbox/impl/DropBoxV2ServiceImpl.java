package com.ulegalize.service.v2.dropbox.impl;

import com.dropbox.core.*;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.*;
import com.dropbox.core.v2.sharing.*;
import com.dropbox.core.v2.users.BasicAccount;
import com.ulegalize.domain.dto.EmailResponse;
import com.ulegalize.domain.dto.FileResponse;
import com.ulegalize.domain.dto.ObjectResponse;
import com.ulegalize.service.exception.RestException;
import com.ulegalize.service.lawfirm.LawfirmApi;
import com.ulegalize.service.v2.dropbox.DropBoxV2Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DropBoxV2ServiceImpl implements DropBoxV2Service {
  @Value("${app.dropbox.key}")
  private String KEY;
  @Value("${app.dropbox.secret}")
  private String SECRET_TOKEN;
  @Value("${app.dropbox.v2.callback}")
  private String URL_CALLBACK;
  @Value("${app.dropbox.api}")
  private String URL_API;

  @Autowired
  private LawfirmApi lawfirmApi;

  @Override
  public URL startToken(String vcKey) throws RestException {
    URL urlOutput = null;

    try {
      String apiUrl = getRedirectUrl();

      URI uri = new URI(apiUrl);
      urlOutput = uri.toURL();

    } catch (URISyntaxException e) {
      log.error("error URISyntaxException with url ");
      throw new RestException("Error during uri syntax");


    } catch (MalformedURLException e) {
      log.error("error MalformedURLException with url ");
      throw new RestException("Error during uri");
    }

    return urlOutput;

  }

  private String getRedirectUrl() {
    try {
      URI uri = new URI(URL_CALLBACK);

      String apiUrl = URL_API.replaceAll("\\{client_id\\}", KEY);
      return apiUrl.replaceAll("\\{redirect_uri\\}", String.valueOf(uri.toURL()));

    } catch (URISyntaxException e) {
      log.error("error URISyntaxException with url ");

    } catch (MalformedURLException e) {
      log.error("error MalformedURLException with url ");
    }

    return "";
  }

  @Override
  public String finishToken(String code) throws RestException {
    String token = null;

    DbxAuthFinish authFinish;
    try {

      DbxAppInfo appInfo = new DbxAppInfo(KEY, SECRET_TOKEN);

      // Run through Dropbox API authorization process
      DbxRequestConfig requestConfig = new DbxRequestConfig(KEY);
      DbxWebAuth webAuth = new DbxWebAuth(requestConfig, appInfo);
      URI uri = new URI(URL_CALLBACK);

      authFinish = webAuth.finishFromCode(code, uri.toURL().toString());

      token = authFinish.getAccessToken();

      createSystemFolders(token);
    } catch (DbxException ex) {
      log.error("Error in DbxWebAuth.authorize: {}", ex.getMessage(), ex);
      throw new RestException("Error finishing token");

    } catch (MalformedURLException ex) {

      log.error("Error in MalformedURLException: {}", ex.getMessage(), ex);
      throw new RestException("Error finishing token");
    } catch (URISyntaxException ex) {

      log.error("Error in URISyntaxException: {}", ex.getMessage(), ex);
      throw new RestException("Error finishing token");
    }

    return token;

  }

  @Override
  public boolean checkSession(String dropboxToken) {
    try {
      // Create Dropbox client
      DbxRequestConfig config = DbxRequestConfig.newBuilder(KEY).build();
      DbxClientV2 client = new DbxClientV2(config, dropboxToken);
      return client.files().listFolder("").getEntries() != null;

    } catch (DbxException e) {
      log.error("Error while checking session dropbox", e);
      return false;

    }
  }

  @Override
  public List<ObjectResponse> getListFolder(String dropboxToken, String objPath, String vcKey) throws RestException {

    List<ObjectResponse> objectResponses = new ArrayList<>();
    try {
      // Create Dropbox client
      DbxRequestConfig config = DbxRequestConfig.newBuilder(KEY).build();
      DbxClientV2 client = new DbxClientV2(config, dropboxToken);

      // Get files and folder metadata from Dropbox root directory
      ListFolderResult result = null;
      result = client.files().listFolder(objPath);

      for (Metadata metadata : result.getEntries()) {
        ObjectResponse objectResponse = new ObjectResponse();
        if (metadata instanceof FileMetadata) {
          objectResponse.setName(metadata.getPathDisplay().substring(1));
          FileMetadata meta = (FileMetadata) metadata;

          objectResponse.setEtag(meta.getId());
          objectResponse.setSize(meta.getSize());
          objectResponse.setLastModified(meta.getClientModified().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        } else if (metadata instanceof FolderMetadata) {
          objectResponse.setName(metadata.getPathDisplay().substring(1) + "/");
          FolderMetadata meta = (FolderMetadata) metadata;

          objectResponse.setEtag(meta.getId());
          objectResponse.setSize(null);
          objectResponse.setLastModified(null);
        }
        objectResponse.setContainer(vcKey);

        objectResponses.add(objectResponse);

      }
    } catch (DbxException e) {
      log.error("Error while listing folder", e);
      throw new RestException("Error while listing folder");

    }

    return objectResponses;
  }

  @Override
  public FileResponse downloadFile(String dropboxToken, String path, String vcKey) throws RestException {
    if (path == null || path.isEmpty()) {
      throw new RestException("Not File selected");
    }
    FileResponse fileResponse = new FileResponse();

    try {
      // Create Dropbox client
      DbxRequestConfig config = DbxRequestConfig.newBuilder(KEY).build();
      DbxClientV2 client = new DbxClientV2(config, dropboxToken);
//        ProgressListener progressListener = l -> printProgress(l, localFile.length());

      DbxDownloader<FileMetadata> result = client.files().download(path);


      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

      FileMetadata fileMetadata = result.download(byteArrayOutputStream);

      String binary = Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());

      fileResponse.setName(fileMetadata.getName());
      fileResponse.setBinary(binary);
      byteArrayOutputStream.close();

    } catch (DbxException | IOException ex) {
      log.error(ex.getMessage());
      throw new RestException("Error while downloading file");

    }
    return fileResponse;

  }

  private void createSystemFolders(String dropboxToken) {
    List<String> arraySystemFolders = Arrays.asList("/templates", "/factures", "/dossiers", "/@postin");

    // Create Dropbox client
    DbxRequestConfig config = DbxRequestConfig.newBuilder(KEY).build();
    DbxClientV2 client = new DbxClientV2(config, dropboxToken);
//        ProgressListener progressListener = l -> printProgress(l, localFile.length());

    arraySystemFolders.forEach(folder -> {
      try {

        client.files().createFolderV2(folder);
        log.info("Un dossier {} créé avec succès lors de l'auth dropbox.", folder);
      } catch (DbxException e) {
        log.warn("Error while create folder {}", folder, e);
      }
    });

  }

  @Override
  public void createPath(String path, String dropboxToken) throws RestException {

    try {
// Create Dropbox client
      DbxRequestConfig config = DbxRequestConfig.newBuilder(KEY).build();
      DbxClientV2 client = new DbxClientV2(config, dropboxToken);

      client.files().createFolderV2(path);
      log.info("Folder {} created successfully.", path);
    } catch (DbxException e) {
      log.error("Error while create folder {}", path, e);
      throw new RestException("Error while create folder ");

    }
  }

  @Override
  public void removeObjects(String path, String dropboxToken) throws RestException {

    // Create Dropbox client
    DbxRequestConfig config = DbxRequestConfig.newBuilder(KEY).build();
    DbxClientV2 client = new DbxClientV2(config, dropboxToken);
    try {

      client.files().deleteV2(path);
      log.info("Folder {} deleted successfully.", path);
    } catch (DbxException e) {
      log.error("Error while deleting folder {}", path, e);
      throw new RestException("Error while create folder ");

    }

  }

  @Override
  public void renameObject(String fromPath, String toPath, String dropboxToken) throws RestException {
    // Create Dropbox client
    DbxRequestConfig config = DbxRequestConfig.newBuilder(KEY).build();
    DbxClientV2 client = new DbxClientV2(config, dropboxToken);
    try {

      client.files().moveV2(fromPath, toPath);
      log.info("File moved from {} to {} successfully.", fromPath, toPath);
    } catch (DbxException e) {
      log.error("Error while moving file from {} to {}", fromPath, toPath, e);
      throw new RestException("Error while rename folder ");
    }

  }

  @Override
  public void createFile(String path, String filename, MultipartFile file, String dropboxToken) throws RestException {
    log.debug("Creating File {} and filename {}", path, filename);
    // Create Dropbox client
    DbxRequestConfig config = DbxRequestConfig.newBuilder(KEY).build();
    DbxClientV2 client = new DbxClientV2(config, dropboxToken);
    try {

      UploadUploader uploadUploader = client.files().upload(path + filename);

      uploadUploader.uploadAndFinish(file.getInputStream());
      log.info("Folder {} created successfully.", path);
    } catch (DbxException | IOException e) {
      log.error("Error while uploading folder {}", path, e);
      throw new RestException("Error while create file ");

    }

  }

  @Override
  public boolean shareFile(String objPath, String message, List<String> sharedWith, String dropboxToken) throws RestException {

    // Create Dropbox client
    DbxRequestConfig config = DbxRequestConfig.newBuilder(KEY).build();
    DbxClientV2 client = new DbxClientV2(config, dropboxToken);
    try {
      List<MemberSelector> newMembers = new ArrayList<>();

      for (String mail : sharedWith) {
        MemberSelector newMember = MemberSelector.email(mail);
        newMembers.add(newMember);
      }

      List<FileMemberActionResult> fileMemberActionResults = client.sharing().addFileMember(objPath, newMembers);

      log.info("File {} shared successfully with members {}.", objPath, sharedWith);

      return true;
    } catch (DbxException e) {
      log.error("Error while sharing file {}", objPath, e);
      throw new RestException("Error while sharing file ");

    }

  }

  @Override
  public boolean shareFolder(String objPath, String message, List<String> sharedWith, String dropboxToken) throws RestException {

    // Create Dropbox client
    DbxRequestConfig config = DbxRequestConfig.newBuilder(KEY).build();
    DbxClientV2 client = new DbxClientV2(config, dropboxToken);
    try {
      List<AddMember> newMembers = new ArrayList<>();

      for (String mail : sharedWith) {
        MemberSelector newMember = MemberSelector.email(mail);
        AddMember addMember = new AddMember(newMember);
        newMembers.add(addMember);
      }
      ShareFolderLaunch sfl = client.sharing().shareFolder(objPath);

      client.sharing().addFolderMember(sfl.getCompleteValue().getSharedFolderId(), newMembers);

      log.info("File {} shared successfully.", objPath);

      return true;
    } catch (DbxException e) {
      log.error("Error while sharing file {}", objPath, e);
      throw new RestException("Error while sharing file ");

    }

  }

  @Override
  public String createShareLink(String objPath, String dropboxToken) throws RestException {

    // Create Dropbox client
    DbxRequestConfig config = DbxRequestConfig.newBuilder(KEY).build();
    DbxClientV2 client = new DbxClientV2(config, dropboxToken);
    try {
      ListSharedLinksResult result = client.sharing().listSharedLinksBuilder()
              .withPath(objPath).withDirectOnly(true).start();
      if (result == null || result.getLinks().isEmpty()) {
        SharedLinkMetadata sharedLinkMetadata = client.sharing()
                .createSharedLinkWithSettings(objPath);

        log.info("File {} shared successfully.", objPath);

        return sharedLinkMetadata.getUrl();
      } else {
        return result.getLinks().get(0).getUrl();
      }
    } catch (CreateSharedLinkWithSettingsErrorException e) {
      log.warn("Error while sharing file {}", objPath, e);
      throw new RestException("Error while sharing file ");

    } catch (DbxException e) {
      log.error("Error while sharing file {}", objPath, e);
      throw new RestException("Error while sharing file ");

    }

  }

  @Override
  public List<EmailResponse> getObjSharedWith(String objPath, Integer size, String dropboxToken) throws RestException {
    log.debug("Entering getObjSharedWith for objPath {}", objPath);
    List<EmailResponse> emailResponseList = new ArrayList<>();

    if (size != null && size > 0) {
      emailResponseList = getObjSharedWithNoSize(objPath, dropboxToken);
    } else {
      emailResponseList = getFolderMetaData(objPath, dropboxToken);
    }

    return emailResponseList;
  }

  @Override
  public void deleteFileShared(String objPath, String deletedWith, String dropboxToken) throws RestException {
    try {
      // Create Dropbox client
      DbxRequestConfig config = DbxRequestConfig.newBuilder(KEY).build();
      DbxClientV2 client = new DbxClientV2(config, dropboxToken);

      MemberSelector newMember = MemberSelector.email(deletedWith);

      client.sharing()
              .removeFileMember2(objPath, newMember);
      log.debug("member {} has been removed ", deletedWith);
    } catch (DbxException e) {
      log.error("Error while  deleting file {}", objPath, e);
      throw new RestException("Error while deleting file ");

    }
  }

  @Override
  public void deleteFolderShared(String objPath, String deletedWith, String dropboxToken) throws RestException {
    try {
      // Create Dropbox client
      DbxRequestConfig config = DbxRequestConfig.newBuilder(KEY).build();
      DbxClientV2 client = new DbxClientV2(config, dropboxToken);

      MemberSelector newMember = MemberSelector.email(deletedWith);

      client.sharing()
              .removeFolderMember(objPath, newMember, false);
    } catch (DbxException e) {
      log.error("Error while  deleting file {}", objPath, e);
      throw new RestException("Error while deleting file ");

    }
  }

  @Override
  public List<ObjectResponse> getListSharedObj(String dropboxToken, String vcKey) throws RestException {
    List<ObjectResponse> objectResponseList = new ArrayList<>();
    try {
      // Create Dropbox client
      DbxRequestConfig config = DbxRequestConfig.newBuilder(KEY).build();
      DbxClientV2 client = new DbxClientV2(config, dropboxToken);

      ListFilesResult listFilesResult = client.sharing()
              .listReceivedFiles();

      for (SharedFileMetadata sharedFileMetadata : listFilesResult.getEntries()) {
        ObjectResponse objectResponse = new ObjectResponse();

        objectResponse.setName(sharedFileMetadata.getName());
        objectResponse.setUrl(sharedFileMetadata.getPreviewUrl());
        objectResponse.setEtag(sharedFileMetadata.getId());
        objectResponse.setSize(null);
        objectResponse.setContainer(vcKey);
        objectResponseList.add(objectResponse);
      }
      return objectResponseList;
    } catch (DbxException e) {
      log.error("Error while getting list received files", e);
      throw new RestException("Error while getting list received files");

    }
  }

  private List<EmailResponse> getObjSharedWithNoSize(String objPath, String dropboxToken) throws RestException {

    // Create Dropbox client
    DbxRequestConfig config = DbxRequestConfig.newBuilder(KEY).build();
    DbxClientV2 client = new DbxClientV2(config, dropboxToken);

    List<EmailResponse> emailResponseList = new ArrayList<>();
    try {

      SharedFileMembers sharedFileMembers = client.sharing()
              .listFileMembers(objPath);

      log.info("File {} shared successfully.", objPath);

      for (InviteeMembershipInfo inviteeMembershipInfo : sharedFileMembers.getInvitees()) {
        String email = inviteeMembershipInfo.getInvitee().getEmailValue();
        EmailResponse emailResponse = new EmailResponse(null, email, "");

        emailResponseList.add(emailResponse);
      }
      List<UserMembershipInfo> userMembershipInfos = sharedFileMembers
              .getUsers()
              .stream()
              .filter(user -> !user.getAccessType().equals(AccessLevel.OWNER)).collect(Collectors.toList());

      userMembershipInfos.stream().forEach(user -> {
        try {
          BasicAccount basicAccount = client.users().getAccount(user.getUser().getAccountId());

          EmailResponse emailResponse = new EmailResponse(user.getUser().getAccountId(), basicAccount.getEmail(), basicAccount.getName().getDisplayName());

          emailResponseList.add(emailResponse);
        } catch (DbxException e) {
          log.error("Error while getting user by account id", user.getUser().getAccountId(), e);
        }
      });


      return emailResponseList;
    } catch (DbxException e) {
      log.error("Error while sharing file {}", objPath, e);
      throw new RestException("Error while sharing file ");

    }
  }

  private List<EmailResponse> getFolderMetaData(String objPath, String dropboxToken) throws RestException {

    // Create Dropbox client
    DbxRequestConfig config = DbxRequestConfig.newBuilder(KEY).build();
    DbxClientV2 client = new DbxClientV2(config, dropboxToken);

    List<EmailResponse> emailResponseList = new ArrayList<>();
    try {

      Metadata sharedFileMembers = client.files().getMetadata(objPath);

      String sharedFolderId = sharedFileMembers.getParentSharedFolderId();

      if (sharedFolderId != null) {
        SharedFolderMembers sharedFolderMembers = client.sharing().listFolderMembers(sharedFolderId);
        log.info("File {} shared successfully.", objPath);

        for (InviteeMembershipInfo inviteeMembershipInfo : sharedFolderMembers.getInvitees()) {
          String email = inviteeMembershipInfo.getInvitee().getEmailValue();
          EmailResponse emailResponse = new EmailResponse(null, email, "");

          emailResponseList.add(emailResponse);
        }
        List<UserMembershipInfo> userMembershipInfos = sharedFolderMembers
                .getUsers()
                .stream()
                .filter(user -> !user.getAccessType().equals(AccessLevel.OWNER)).collect(Collectors.toList());

        userMembershipInfos.stream().forEach(user -> {
          try {
            BasicAccount basicAccount = client.users().getAccount(user.getUser().getAccountId());

            EmailResponse emailResponse = new EmailResponse(user.getUser().getAccountId(), basicAccount.getEmail(), basicAccount.getName().getDisplayName());

            emailResponseList.add(emailResponse);
          } catch (DbxException e) {
            log.error("Error while getting user by account id", user.getUser().getAccountId(), e);
          }
        });
      }

      return emailResponseList;
    } catch (DbxException e) {
      log.error("Error while sharing file {}", objPath, e);
      throw new RestException("Error while sharing file ");

    }
  }


  private boolean getAlreadyExistingShareLink(String objPath, String dropboxToken) throws RestException {

    // Create Dropbox client
    DbxRequestConfig config = DbxRequestConfig.newBuilder(KEY).build();
    DbxClientV2 client = new DbxClientV2(config, dropboxToken);
    try {

      ListSharedLinksResult listSharedLinksResult = client.sharing()
              .listSharedLinksBuilder()
              .withPath(objPath)
              .withDirectOnly(true)
              .start();

      log.info("File {} shared successfully.", objPath);

      return true;
    } catch (DbxException e) {
      log.error("Error while sharing file {}", objPath, e);
      throw new RestException("Error while sharing file ");

    }

  }


}
