package com.ulegalize.controller.onedrive;

import com.ulegalize.domain.dto.*;
import com.ulegalize.service.exception.RestException;
import com.ulegalize.service.onedrive.OneDriveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/onedrive")
@Slf4j
public class OneDriveREST {
    private final OneDriveService oneDriveService;
    @Value("${app.lawfirm.callback.onedrive}")
    String URL_CALLBACK;

    public OneDriveREST(OneDriveService oneDriveService) {
        this.oneDriveService = oneDriveService;
    }


    @GetMapping("/start")
    public ResponseEntity<?> associateOneDrive(Authentication authentication) throws RestException {
//        LawfirmToken lawfirmToken = (LawfirmToken) authentication.getPrincipal();

        URL url = null;
        url = oneDriveService.startToken("lawfirmToken.getVcKey()");

        return ResponseEntity.ok(url);
    }

    @GetMapping("/finish")
//    @PreAuthorize("hasRole('USER')")
    public RedirectView finish(@RequestParam String code) throws RestException {
        String token = oneDriveService.finishToken(code);

        return new RedirectView(URL_CALLBACK + token);
    }

    @GetMapping(value = "/ops/315/F/{objPath}", consumes = MediaType.APPLICATION_JSON_VALUE)
//  @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<ObjectResponse>> getListFolder(Authentication authentication,
                                                              @PathVariable String objPath) throws RestException {
        List<ObjectResponse> objectResponses = null;
        log.debug("getListFolder obj {}", objPath);

        LawfirmToken lawfirmToken = (LawfirmToken) authentication.getPrincipal();
        String accessToken = oneDriveService.checkTokenExpire(lawfirmToken);

        objectResponses = oneDriveService.getListFolder(objPath, accessToken);

        return ResponseEntity.ok(objectResponses);
    }

    @GetMapping(value = "/ops/315/path/{objPath}", consumes = MediaType.APPLICATION_JSON_VALUE)
//  @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<ObjectResponse>> getFolderByPath(Authentication authentication,
                                                                @PathVariable String objPath) throws RestException {
        List<ObjectResponse> objectResponses = null;
        log.debug("getListFolder obj {}", objPath);

        LawfirmToken lawfirmToken = (LawfirmToken) authentication.getPrincipal();
        String accessToken = oneDriveService.checkTokenExpire(lawfirmToken);

        String objTransform = transformPath(objPath);
        log.debug("obj transformed {}", objTransform);

        objectResponses = oneDriveService.folderByPath(objTransform, accessToken);

        return ResponseEntity.ok(objectResponses);
    }

    @GetMapping("/ops/315/DC/{objPath}")
//    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<FileResponse> downloadFile(Authentication authentication,
                                                     @PathVariable String objPath) throws RestException {
        FileResponse objectResponses = null;
        log.info("objPath {}", objPath);

        try {
            LawfirmToken lawfirmToken = (LawfirmToken) authentication.getPrincipal();
            String accessToken = oneDriveService.checkTokenExpire(lawfirmToken);

            objectResponses = oneDriveService.downloadFile(accessToken, objPath, lawfirmToken.getVcKey());
        } catch (RestException e) {
            log.error("Error while downloading dropbox folder", e);
        }


        return ResponseEntity.ok(objectResponses);
    }

    @PostMapping("/ops/315/F/{path}")
    public ResponseEntity<?> createFile(Authentication authentication,
                                        @PathVariable String path) throws RestException {
        log.info("path {}", path);

        LawfirmToken lawfirmToken = (LawfirmToken) authentication.getPrincipal();
        String accessToken = oneDriveService.checkTokenExpire(lawfirmToken);

        String parentId = getParentIdFromPath(path);
        log.info("parentId {}", parentId);

        String newFolder = getNameFromPath(path);
        log.info("newFolder {}", newFolder);

        oneDriveService.createPath(parentId, newFolder, accessToken);

        return ResponseEntity.ok("");
    }

    @DeleteMapping("/ops/315/D/{path}")
    public ResponseEntity<?> removeFolder(Authentication authentication,
                                          @PathVariable String path) throws RestException {
        log.info("path {}", path);

        try {
            LawfirmToken lawfirmToken = (LawfirmToken) authentication.getPrincipal();
            String accessToken = oneDriveService.checkTokenExpire(lawfirmToken);

            oneDriveService.removeObjects(path, accessToken);
        } catch (RestException e) {
            log.error("Error while removing onedrive folder", e);
        }

        return ResponseEntity.ok("");
    }

    @DeleteMapping("/ops/315/F/{path}")
    public ResponseEntity<?> removeFile(Authentication authentication,
                                        @PathVariable String path) throws RestException {
        log.info("path {}", path);

        LawfirmToken lawfirmToken = (LawfirmToken) authentication.getPrincipal();
        String accessToken = oneDriveService.checkTokenExpire(lawfirmToken);

        oneDriveService.removeObjects(path, accessToken);

        return ResponseEntity.ok("");
    }

    @PutMapping("/ops/315/R/{fromPath}/{toPath}")
    public ResponseEntity<?> renameFile(Authentication authentication,
                                        @PathVariable String fromPath, @PathVariable String toPath) throws RestException {
        log.info("fromPath {}", fromPath);
        log.info("toPath {}", toPath);

        try {
            LawfirmToken lawfirmToken = (LawfirmToken) authentication.getPrincipal();
            String accessToken = oneDriveService.checkTokenExpire(lawfirmToken);

            oneDriveService.renameObject(fromPath, toPath, accessToken);
        } catch (RestException e) {
            log.error("Error while renaming onedrive folder", e);
        }

        return ResponseEntity.ok("");
    }

    @PostMapping(value = "/ops/315/D/{path}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<?> uploadFile(Authentication authentication,
                                        @RequestParam("files") MultipartFile file,
                                        @PathVariable String path) throws RestException {
        log.info("path {}", path);
        log.info("filename {}", file.getOriginalFilename());

        try {
            LawfirmToken lawfirmToken = (LawfirmToken) authentication.getPrincipal();
            String accessToken = oneDriveService.checkTokenExpire(lawfirmToken);

            oneDriveService.createFile(path, file, accessToken);
        } catch (RestException e) {
            log.error("Error while uploading onedrive file", e);
        }


        return ResponseEntity.ok("");
    }

    @PostMapping(value = "/ops/315/S", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> shareFile(Authentication authentication,
                                       @RequestBody ObjectRequest body) throws RestException {
        log.info("obj {}", body.getObj());
        log.info("id {}", body.getId());
        log.info("shared_with {}", body.getShared_with());
        log.info("message {}", body.getMsg());
        log.info("right {}", body.getRight());
        log.info("size {}", body.getSize());

        LawfirmToken lawfirmToken = (LawfirmToken) authentication.getPrincipal();
        String accessToken = oneDriveService.checkTokenExpire(lawfirmToken);

        // count how many , . root ,filename,
        long count2 = body.getObj().codePoints().filter(ch -> ch == ',').count();

        String path = count2 > 1 ? body.getId() : null;

        oneDriveService.shareFolder(path, body.getMsg(), body.getShared_with(), accessToken);

        return ResponseEntity.ok("");
    }

    @PostMapping(value = "/ops/315/LS", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<?> createShareLink(Authentication authentication,
                                             @RequestBody ObjectRequest body) throws RestException {
        log.info("path of the file {}", body.getObj());
        log.info("id of the file {}", body.getId());
        String url = "";

        try {
            LawfirmToken lawfirmToken = (LawfirmToken) authentication.getPrincipal();
            String accessToken = oneDriveService.checkTokenExpire(lawfirmToken);

            // count how many , . root ,filename,
            long count2 = body.getObj().codePoints().filter(ch -> ch == ',').count();

            String path = count2 > 2 ? body.getId() : null;

            log.debug("Path for createShareLink {}", path);

            url = oneDriveService.createShareLink(path, accessToken);
        } catch (RestException e) {
            log.error("Error while creating dropbox folder", e);
        }

        return ResponseEntity.ok(url);
    }

    @GetMapping(value = "/ops/315/DE/{path}/{size}")
    public ResponseEntity<?> getObjSharedWith(Authentication authentication,
                                              @PathVariable("path") String id, @PathVariable(value = "size") Integer size) throws RestException {
        log.info("id {}", id);
        log.info("size {}", size);
        List<EmailResponse> emailResponseList = new ArrayList<>();

        try {
            LawfirmToken lawfirmToken = (LawfirmToken) authentication.getPrincipal();
            String accessToken = oneDriveService.checkTokenExpire(lawfirmToken);
            // count how many , . root ,filename,
            log.debug("Path for createShareLink {}", id);

            emailResponseList = oneDriveService.getObjSharedWith(id, size, accessToken);
        } catch (RestException e) {
            log.error("Error while sharing onedrive folder", e);
        }

        return ResponseEntity.ok(emailResponseList);
    }

    @DeleteMapping(value = "/ops/315/S", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deleteFileShared(Authentication authentication,
                                              @RequestBody ObjectRequest body) throws RestException {
        log.info("obj {}", body.getObj());
        log.info("permissionId {}", body.getPermissionId());

        try {
            LawfirmToken lawfirmToken = (LawfirmToken) authentication.getPrincipal();
            String accessToken = oneDriveService.checkTokenExpire(lawfirmToken);

            // count how many , . root ,filename,
            long count2 = body.getObj().codePoints().filter(ch -> ch == ',').count();

            String path = count2 > 1 ? body.getId() : null;

            log.debug("Path for createShareLink {}", path);
            oneDriveService.deleteFileShared(path, body.getPermissionId(), accessToken);
        } catch (RestException e) {
            log.error("Error while creating dropbox folder", e);
        }

        return ResponseEntity.ok("");
    }

    @GetMapping("/ops/315/S")
    public ResponseEntity<?> getListSharedObj(Authentication authentication) throws RestException {
        List<ObjectResponse> objectResponseList = new ArrayList<>();

        try {
            LawfirmToken lawfirmToken = (LawfirmToken) authentication.getPrincipal();
            String accessToken = oneDriveService.checkTokenExpire(lawfirmToken);

            objectResponseList = oneDriveService.getListSharedObj(accessToken, lawfirmToken.getVcKey());
        } catch (RestException e) {
            log.error("Error while getting list share objects", e);
        }

        return ResponseEntity.ok(objectResponseList);
    }

    private String getParentIdFromPath(String objtPath) {
        String[] arrayObj = objtPath.split(",");

        return arrayObj[0];
    }

    private String getNameFromPath(String objtPath) throws RestException {
        if (objtPath.length() < 2) {
            throw new RestException("obj Path must have a name");
        }
        String[] arrayObj = objtPath.split(",");

        return arrayObj[arrayObj.length - 1];
    }

    private String transformPath(String objtPath) {
        if (objtPath.length() > 1) {
            objtPath = objtPath.substring(1, objtPath.length() - 1);
        }
        return objtPath.replaceAll(",", "/");
    }
}
