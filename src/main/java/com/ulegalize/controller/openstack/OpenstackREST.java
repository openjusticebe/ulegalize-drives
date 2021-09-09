package com.ulegalize.controller.openstack;

import com.ulegalize.domain.dto.LawfirmToken;
import com.ulegalize.domain.dto.ObjectResponse;
import com.ulegalize.service.exception.RestException;
import com.ulegalize.service.openstack.IJCloudsSwift;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/openstack")
@Slf4j
public class OpenstackREST {
    private final IJCloudsSwift ijCloudsSwift;

    public OpenstackREST(IJCloudsSwift ijCloudsSwift) {
        this.ijCloudsSwift = ijCloudsSwift;
    }

    @GetMapping("/ops/256/F/{objPath}")
    public ResponseEntity<List<ObjectResponse>> getListFolder(@PathVariable String objPath) throws ResponseStatusException {
        List<ObjectResponse> objectResponses = null;
        log.info("objPath {}", objPath);

        LawfirmToken lawfirmToken = (LawfirmToken) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        objPath = transformPath(objPath);
        log.info("transformed objPath {}", objPath);

        objectResponses = ijCloudsSwift.listObject(lawfirmToken.getVcKey(), objPath);


        return ResponseEntity.ok(objectResponses);
    }

    @PostMapping("/ops/256/F/{path}")
    public ResponseEntity<?> createFolderCases(@RequestBody String container, @PathVariable String path) throws RestException {
        log.info("path {}", path);

        LawfirmToken lawfirmToken = (LawfirmToken) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        path = transformPath(path);
        log.info("transformed path {}", path);

        ijCloudsSwift.createFolder(container, path);

        return ResponseEntity.ok("");
    }


    @PostMapping("/container")
    public ResponseEntity<?> createContainer(@RequestBody String container) throws ResponseStatusException {
        log.info("container {}", container);

        LawfirmToken lawfirmToken = (LawfirmToken) SecurityContextHolder.getContext().getAuthentication().getPrincipal();


        ijCloudsSwift.createContainer(container);

        return ResponseEntity.ok("");
    }

    private String transformPath(String objtPath) {
        objtPath = objtPath.substring(1);

        return objtPath.replaceAll(",", "/");
    }
}
