package com.ulegalize.controller.dropbox;

import com.ulegalize.domain.dto.*;
import com.ulegalize.service.dropbox.DropBoxService;
import com.ulegalize.service.exception.RestException;
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
@RequestMapping("/dropbox")
@Slf4j
public class DropboxREST {
    private final DropBoxService dropBoxService;
    @Value("${app.lawfirm.callback.dropbox}")
    String URL_CALLBACK;

    public DropboxREST(DropBoxService dropBoxService) {
        this.dropBoxService = dropBoxService;
    }


    @GetMapping("/start")
    public ResponseEntity<?> associateDropbox(Authentication authentication) {
        LawfirmToken lawfirmToken = (LawfirmToken) authentication.getPrincipal();

        URL url = null;
        try {
            url = dropBoxService.startToken(lawfirmToken.getVcKey());
        } catch (RestException e) {
            log.error("Error while associateDropbox", e);
        }
        return ResponseEntity.ok(url);
    }

    @GetMapping("/finish")
//    @PreAuthorize("hasRole('USER')")
    public RedirectView finish(@RequestParam String code) {
        String token = null;
        try {
            token = dropBoxService.finishToken(code);

        } catch (RestException e) {
            log.error("Error while finishing dropbox folder", e);
        }

        return new RedirectView(URL_CALLBACK + token);
    }

    @GetMapping("/ops/315/F/{objPath}")
    public ResponseEntity<List<ObjectResponse>> getListFolder(Authentication authentication, @RequestHeader("dropbox_token") String dropboxToken, @PathVariable String objPath) {
        List<ObjectResponse> objectResponses = null;
        log.info("dropboxToken {}", dropboxToken);
        log.info("objPath {}", objPath);

        try {
            LawfirmToken lawfirmToken = (LawfirmToken) authentication.getPrincipal();

            objPath = transformPath(objPath);
            log.info("transformed objPath {}", objPath);

            objectResponses = dropBoxService.getListFolder(dropboxToken, objPath, lawfirmToken.getVcKey());
        } catch (RestException e) {
            log.error("Error while getting dropbox folder", e);
        }


        return ResponseEntity.ok(objectResponses);
    }

    @GetMapping("/ops/315/DC/{objPath}")
    public ResponseEntity<FileResponse> downloadFile(Authentication authentication, @RequestHeader("dropbox_token") String dropboxToken, @PathVariable String objPath) throws RestException {
        FileResponse objectResponses = null;
        log.info("dropboxToken {}", dropboxToken);
        log.info("objPath {}", objPath);

        try {
            LawfirmToken lawfirmToken = (LawfirmToken) authentication.getPrincipal();

            objPath = transformPath(objPath);
            log.info("transformed objPath {}", objPath);

            objectResponses = dropBoxService.downloadFile(dropboxToken, objPath, lawfirmToken.getVcKey());
        } catch (RestException e) {
            log.error("Error while downloading dropbox folder", e);
        }


        return ResponseEntity.ok(objectResponses);
    }

    @PostMapping("/ops/315/F/{path}")
    public ResponseEntity<?> createFile(@RequestHeader("dropbox_token") String dropboxToken,
                                        @PathVariable String path) throws RestException {
        log.info("dropboxToken {}", dropboxToken);
        log.info("path {}", path);

        try {

            path = transformPath(path);
            log.info("transformed path {}", path);

            dropBoxService.createPath(path, dropboxToken);
        } catch (RestException e) {
            log.error("Error while creating dropbox file", e);
        }

        return ResponseEntity.ok("");
    }

    @DeleteMapping("/ops/315/D/{path}")
    public ResponseEntity<?> removeFolder(@RequestHeader("dropbox_token") String dropboxToken,
                                          @PathVariable String path) throws RestException {
        log.info("dropboxToken {}", dropboxToken);
        log.info("path {}", path);

        try {

            path = transformPath(path);
            log.info("transformed path {}", path);

            dropBoxService.removeObjects(path, dropboxToken);
        } catch (RestException e) {
            log.error("Error while removing dropbox folder", e);
        }

        return ResponseEntity.ok("");
    }

    @DeleteMapping("/ops/315/F/{path}")
    public ResponseEntity<?> removeFile(@RequestHeader("dropbox_token") String dropboxToken,
                                        @PathVariable String path) throws RestException {
        log.info("dropboxToken {}", dropboxToken);
        log.info("path {}", path);

        try {

            path = transformPath(path);
            log.info("transformed path {}", path);

            dropBoxService.removeObjects(path, dropboxToken);
        } catch (RestException e) {
            log.error("Error while removing dropbox folder", e);
        }

        return ResponseEntity.ok("");
    }

    @PutMapping("/ops/315/R/{fromPath}/{toPath}")
    public ResponseEntity<?> renameFile(@RequestHeader("dropbox_token") String dropboxToken,
                                        @PathVariable String fromPath, @PathVariable String toPath) throws RestException {
        log.info("dropboxToken {}", dropboxToken);
        log.info("fromPath {}", fromPath);
        log.info("toPath {}", toPath);

        try {
            fromPath = transformPath(fromPath);
            log.info("transformed fromPath {}", fromPath);

            toPath = transformPath(toPath);
            log.info("transformed obj {}", toPath);

            dropBoxService.renameObject(fromPath, toPath, dropboxToken);
        } catch (RestException e) {
            log.error("Error while removing dropbox folder", e);
        }

        return ResponseEntity.ok("");
    }

    @PostMapping(value = "/ops/315/D/{path}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<?> uploadFile(@RequestParam("files") MultipartFile file,
                                        @RequestHeader("dropbox_token") String dropboxToken,
                                        @PathVariable String path) throws RestException {
        log.info("dropboxToken {}", dropboxToken);
        log.info("path {}", path);
        log.info("filename {}", file.getOriginalFilename());

        try {

            path = transformPath(path);
            log.info("transformed obj {}", path);

            dropBoxService.createFile(path, file.getOriginalFilename(), file, dropboxToken);
        } catch (RestException e) {
            log.error("Error while creating dropbox folder", e);
        }


        return ResponseEntity.ok("");
    }

    @PostMapping(value = "/ops/315/S", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> shareFile(@RequestHeader("dropbox_token") String dropboxToken,
                                             @RequestBody ObjectRequest body) throws RestException {
        log.info("dropboxToken {}", dropboxToken);
        log.info("obj {}", body.getObj());
        log.info("shared_with {}", body.getShared_with());
        log.info("message {}", body.getMsg());
        log.info("right {}", body.getRight());
        log.info("size {}", body.getSize());

        boolean shared = false;
        try {

            String obj = transformPath(body.getObj());
            log.info("transformed obj {}", obj);

            if (body.getSize() == 0) {
                shared = dropBoxService.shareFolder(obj, body.getMsg(), body.getShared_with(), dropboxToken);
            } else {
                shared = dropBoxService.shareFile(obj, body.getMsg(), body.getShared_with(), dropboxToken);
            }
        } catch (RestException e) {
            log.error("Error while creating dropbox folder", e);
        }


        return ResponseEntity.ok(shared);
    }

    @PostMapping(value = "/ops/315/LS", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> createShareLink(@RequestHeader("dropbox_token") String dropboxToken,
                                                  @RequestBody ObjectRequest body) throws RestException {
        log.info("dropboxToken {}", dropboxToken);
        log.info("obj {}", body.getObj());

        String obj = transformPath(body.getObj());
        log.info("transformed obj {}", obj);
        String url = "";

        try {
            url = dropBoxService.createShareLink(obj, dropboxToken);
        } catch (RestException e) {
            log.error("Error while creating dropbox folder", e);
        }

        return ResponseEntity.ok(url);
    }

    @GetMapping(value = "/ops/315/DE/{path}/{size}")
    public ResponseEntity<List<EmailResponse>> getObjSharedWith(@RequestHeader("dropbox_token") String dropboxToken,
                                                                @PathVariable("path") String obj, @PathVariable(value = "size") Integer size) throws RestException {
        log.info("getObjSharedWith dropboxToken {}", dropboxToken);
        log.info("obj {}", obj);
        log.info("size {}", size);
        List<EmailResponse> emailResponseList = new ArrayList<>();

        try {
            obj = transformPath(obj);
            log.info("transformed obj {}", obj);

            emailResponseList = dropBoxService.getObjSharedWith(obj, size, dropboxToken);
        } catch (RestException e) {
            log.error("Error while sharing with dropbox folder", e);
        }
        return ResponseEntity.ok(emailResponseList);
    }

    @DeleteMapping(value = "/ops/315/S", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deleteFileShared(@RequestHeader("dropbox_token") String dropboxToken,
                                              @RequestBody ObjectRequest body) throws RestException {
        log.info("dropboxToken {}", dropboxToken);
        log.info("obj {}", body.getObj());
        log.info("getDeleted_with {}", body.getShared_with().get(0));

        try {

            String obj = transformPath(body.getObj());
            log.info("transformed obj {}", obj);

            if (body.getSize() == 0) {
                dropBoxService.deleteFolderShared(obj, body.getShared_with().get(0), dropboxToken);
            } else {
                dropBoxService.deleteFileShared(obj, body.getShared_with().get(0), dropboxToken);
            }
        } catch (RestException e) {
            log.error("Error while creating dropbox folder", e);
        }

        return ResponseEntity.ok("");
    }

    @GetMapping("/ops/315/S")
    public ResponseEntity<?> getListSharedObj(Authentication authentication,
                                              @RequestHeader("dropbox_token") String dropboxToken) throws RestException {
        log.info("dropboxToken {}", dropboxToken);
        List<ObjectResponse> objectResponseList = new ArrayList<>();
        try {
            LawfirmToken lawfirmToken = (LawfirmToken) authentication.getPrincipal();

            objectResponseList = dropBoxService.getListSharedObj(dropboxToken, lawfirmToken.getVcKey());
        } catch (RestException e) {
            log.error("Error while creating dropbox folder", e);
        }

        return ResponseEntity.ok(objectResponseList);
    }

    private String transformPath(String objtPath) {
        objtPath = objtPath.substring(0, objtPath.length() - 1);

        return objtPath.replaceAll(",", "/");
    }
}
