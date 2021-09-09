package com.ulegalize.controller.v2.dropbox;

import com.ulegalize.domain.dto.*;
import com.ulegalize.service.exception.RestException;
import com.ulegalize.service.v2.dropbox.DropBoxV2Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/v2/dropbox")
@Slf4j
public class DropboxV2REST {
    private final DropBoxV2Service dropBoxService;
    @Value("${app.lawfirm.callback.v2.dropbox}")
    String URL_CALLBACK;

    public DropboxV2REST(DropBoxV2Service dropBoxService) {
        this.dropBoxService = dropBoxService;
    }

    @GetMapping("/check")
    public ResponseEntity<Boolean> checkToken() {
        log.debug("Entering checkToken()");
        LawfirmToken lawfirmToken = (LawfirmToken) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String dropboxToken = lawfirmToken.getDropboxToken();

        log.info("dropboxToken {}", dropboxToken);

        log.info("checkToken vckey {}", lawfirmToken.getVcKey());

        return ResponseEntity.ok(dropBoxService.checkSession(dropboxToken));
    }

    @GetMapping("/start")
    public ResponseEntity<?> associateDropbox() {
        LawfirmToken lawfirmToken = (LawfirmToken) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        URL url = null;
        try {
            url = dropBoxService.startToken(lawfirmToken.getVcKey());
            log.debug("starting {} with url {}", lawfirmToken.getVcKey(), url);
        } catch (RestException e) {
            log.error("Error while associateDropbox", e);
        }
        return ResponseEntity.ok(url);
    }

    @GetMapping("/finish")
    public RedirectView finish(@RequestParam String code) {
        String token = null;
        try {
            token = dropBoxService.finishToken(code);

        } catch (RestException e) {
            log.error("Error while finishing dropbox folder", e);
        }

        return new RedirectView(URL_CALLBACK + token);
    }

    @PostMapping("/folders")
    public ResponseEntity<?> createFiles(@RequestBody List<String> paths) {
        LawfirmToken lawfirmToken = (LawfirmToken) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String dropboxToken = lawfirmToken.getDropboxToken();

        log.info("dropboxToken {}", dropboxToken);
        log.info("path {}", paths);

        try {

            for (String path : paths) {
                path = transformPath(path);
                log.info("transformed path {}", path);

                dropBoxService.createPath(path, dropboxToken);
            }
        } catch (RestException e) {
            log.error("Error while creating dropbox file", e);
        }

        return ResponseEntity.ok("");
    }

    @PostMapping(value = "/file")
//    @PostMapping(value = "/file/{path}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<?> uploadFile(@RequestParam("files") MultipartFile file,
                                        @RequestParam("folderPath") String folderPath) throws RestException {
        LawfirmToken lawfirmToken = (LawfirmToken) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String dropboxToken = lawfirmToken.getDropboxToken();

        String path = URLDecoder.decode(folderPath);
        log.info("dropboxToken {}", dropboxToken);
        log.info("path {}", path);
        log.info("filename {}", file.getOriginalFilename());

        try {

            path = transformPath(path);
            log.info("transformed obj {}", path);

            path = path.isEmpty() ? "/" : path;
            log.info("path for upload {}", path);

            dropBoxService.createFile(path, file.getOriginalFilename(), file, dropboxToken);
        } catch (RestException e) {
            log.error("Error while creating dropbox folder", e);
        }


        return ResponseEntity.ok("");
    }

    @GetMapping("/folder/{objPath}")
    public ResponseEntity<List<ObjectResponse>> getListFolder(@PathVariable String objPath) {
        List<ObjectResponse> objectResponses = null;
        LawfirmToken lawfirmToken = (LawfirmToken) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String dropboxToken = lawfirmToken.getDropboxToken();

        log.info("dropboxToken {}", dropboxToken);
        log.info("objPath {}", objPath);

        try {
            objPath = transformPath(objPath);
            String vckey = lawfirmToken.getVcKey();
            log.info("transformed objPath {}", objPath);

            objectResponses = dropBoxService.getListFolder(dropboxToken, objPath, vckey);
        } catch (RestException e) {
            log.error("Error while getting dropbox folder", e);
        }


        return ResponseEntity.ok(objectResponses);
    }

    @DeleteMapping(value = "/file", consumes = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<?> removeFile(
            @RequestBody String path) throws RestException, IOException {
        LawfirmToken lawfirmToken = (LawfirmToken) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String dropboxToken = lawfirmToken.getDropboxToken();

        log.info("dropboxToken {}", dropboxToken);
        path = URLDecoder.decode(path);
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

    @DeleteMapping("/folder")
    public ResponseEntity<?> removeFolder(@RequestBody String path) throws RestException {
        LawfirmToken lawfirmToken = (LawfirmToken) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String dropboxToken = lawfirmToken.getDropboxToken();

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


    @GetMapping("/file/{objPath}")
    public ResponseEntity<FileResponse> downloadFile(@PathVariable String objPath) throws RestException {
        FileResponse objectResponses = null;
        LawfirmToken lawfirmToken = (LawfirmToken) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String dropboxToken = lawfirmToken.getDropboxToken();

        log.info("dropboxToken {}", dropboxToken);
        log.info("objPath {}", objPath);

        try {
            objPath = transformPath(objPath);
            log.info("transformed objPath {}", objPath);

            objectResponses = dropBoxService.downloadFile(dropboxToken, objPath, lawfirmToken.getVcKey());
        } catch (RestException e) {
            log.error("Error while downloading dropbox folder", e);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Error while downloading dropbox folder");
        }


        return ResponseEntity.ok(objectResponses);
    }

    @GetMapping(value = "/share/link/{path}")
    public ResponseEntity<String> createShareLink(
            @PathVariable String path) throws RestException {
        LawfirmToken lawfirmToken = (LawfirmToken) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String dropboxToken = lawfirmToken.getDropboxToken();

        log.info("dropboxToken {}", dropboxToken);
        log.info("obj {}", path);

        String obj = transformPath(path);
        log.info("transformed obj {}", obj);
        String url = "";

        try {
            url = dropBoxService.createShareLink(obj, dropboxToken);
        } catch (RestException e) {
            log.error("Error while creating dropbox folder", e);
        }

        return ResponseEntity.ok(url);
    }

    @PostMapping("/move/file")
    public ResponseEntity<?> renameFile(
            @RequestBody RequestDropbox requestDropbox) throws RestException {
        log.debug("Entering renameFile {}", requestDropbox);
        LawfirmToken lawfirmToken = (LawfirmToken) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String dropboxToken = lawfirmToken.getDropboxToken();

        String fromPath = requestDropbox.getPath();
        String toPath = requestDropbox.getNewPath();
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


    @PostMapping("/move")
    public ResponseEntity<?> renameFolder(
            @RequestBody RequestDropbox requestDropbox) throws RestException {
        log.debug("Entering renameFolder {}", requestDropbox);
        LawfirmToken lawfirmToken = (LawfirmToken) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String dropboxToken = lawfirmToken.getDropboxToken();

        String fromPath = requestDropbox.getPath();
        String toPath = requestDropbox.getNewPath();
        log.info("dropboxToken {}", dropboxToken);
        log.info("fromPath {}", fromPath);
        log.info("toPath {}", toPath);

        try {
            fromPath = transformPath(fromPath);
            // remove the last character f it's a folder
            fromPath = fromPath.substring(0, fromPath.length() - 1);
            log.info("transformed fromPath {}", fromPath);

            toPath = transformPath(toPath);
            // remove the last character f it's a folder
            toPath = toPath.substring(0, toPath.length() - 1);
            log.info("transformed obj {}", toPath);

            dropBoxService.renameObject(fromPath, toPath, dropboxToken);
        } catch (RestException e) {
            log.error("Error while removing dropbox folder", e);
        }

        return ResponseEntity.ok("");
    }


    @PostMapping(value = "/share", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> shareFile(
            @RequestBody ObjectRequest body) throws RestException {
        LawfirmToken lawfirmToken = (LawfirmToken) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String dropboxToken = lawfirmToken.getDropboxToken();

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

            if (body.getSize() == null || body.getSize() == 0) {
                shared = dropBoxService.shareFolder(obj, body.getMsg(), body.getShared_with(), dropboxToken);
            } else {
                shared = dropBoxService.shareFile(obj, body.getMsg(), body.getShared_with(), dropboxToken);
            }
        } catch (RestException e) {
            log.error("Error while creating dropbox folder", e);
        }


        return ResponseEntity.ok(shared);
    }


    @GetMapping(value = "/ops/315/DE/{path}/{size}")
    public ResponseEntity<List<EmailResponse>> getObjSharedWith(
            @PathVariable("path") String obj, @PathVariable(value = "size") Integer size) throws RestException {
        LawfirmToken lawfirmToken = (LawfirmToken) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String dropboxToken = lawfirmToken.getDropboxToken();

        log.info("dropboxToken {}", dropboxToken);
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
    public ResponseEntity<?> deleteFileShared(@RequestBody ObjectRequest body) throws RestException {
        LawfirmToken lawfirmToken = (LawfirmToken) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String dropboxToken = lawfirmToken.getDropboxToken();

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
    public ResponseEntity<?> getListSharedObj() throws RestException {
        LawfirmToken lawfirmToken = (LawfirmToken) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String dropboxToken = lawfirmToken.getDropboxToken();

        log.info("dropboxToken {}", dropboxToken);
        List<ObjectResponse> objectResponseList = new ArrayList<>();
        try {
            objectResponseList = dropBoxService.getListSharedObj(dropboxToken, lawfirmToken.getVcKey());
        } catch (RestException e) {
            log.error("Error while creating dropbox folder", e);
        }

        return ResponseEntity.ok(objectResponseList);
    }

    private String transformPath(String objtPath) {
        String path = objtPath != null && !objtPath.isEmpty() ? URLDecoder.decode(objtPath) : "";
        // the first char must be / exept root
        if (path.length() == 1) {
            return "";
        } else if (path.length() > 2) {
            // check the first char if it s "/"
            path = path.substring(0, 1).equalsIgnoreCase("/") ? path : "/" + path;
            // check if the last character is NOT "/"
            return path.substring(path.length() - 1).equalsIgnoreCase("/") ? path.substring(0, path.length() - 1) : path;
        } else {
            return "";
        }
    }
}
