package com.ulegalize.service.openstack.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.ByteSource;
import com.google.common.io.Files;
import com.ulegalize.domain.dto.FileResponse;
import com.ulegalize.domain.dto.ObjectResponse;
import com.ulegalize.service.openstack.IJCloudsSwift;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jclouds.ContextBuilder;
import org.jclouds.io.Payload;
import org.jclouds.io.Payloads;
import org.jclouds.io.payloads.ByteSourcePayload;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.jclouds.openstack.keystone.config.KeystoneProperties;
import org.jclouds.openstack.swift.v1.SwiftApi;
import org.jclouds.openstack.swift.v1.domain.Container;
import org.jclouds.openstack.swift.v1.features.ContainerApi;
import org.jclouds.openstack.swift.v1.features.ObjectApi;
import org.jclouds.openstack.swift.v1.functions.ParseObjectListFromResponse;
import org.jclouds.openstack.swift.v1.options.CreateContainerOptions;
import org.jclouds.openstack.swift.v1.options.ListContainerOptions;
import org.jclouds.openstack.swift.v1.options.PutOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import static com.google.common.io.ByteSource.wrap;

@Slf4j
@Service
public class JCloudsSwift implements IJCloudsSwift {
    @Value("${app.openstack.provider}")
    String OPENSTACK_PROVIDER;
    @Value("${app.openstack.username}")
    String OPENSTACK_USERNAME;
    @Value("${app.openstack.password}")
    String OPENSTACK_PASSWORD;
    @Value("${app.openstack.authUrl}")
    String OPENSTACK_AUTHURL;
    @Value("${app.openstack.region}")
    String OPENSTACK_REGION;
    @Value("${app.openstack.tenantId}")
    String OPENSTACK_TENANTID;
    @Value("${app.openstack.tenantName}")
    String OPENSTACK_TENANTNAME;
    @Value("${app.openstack.version}")
    String OPENSTACK_VERSION;
    @Value("${app.openstack.keystoneAuthVersion}")
    String OPENSTACK_KEYSTONE;

    private SwiftApi swiftApi;


//  public static void main(String[] args) throws IOException  {
//    JCloudsSwift jCloudsSwift = new JCloudsSwift();
//
//    try {
//      jCloudsSwift.init();
//      jCloudsSwift.createContainer();
//      jCloudsSwift.uploadObjectFromString();
//      jCloudsSwift.listContainers();
//      jCloudsSwift.close();
//    }
//    catch (Exception e) {
//     log.error("Error while js cloud swift",e);
//    }
//    finally {
//      jCloudsSwift.close();
//    }
//  }

    //    @PostConstruct
    private void init() {


//        overrides.put(KeystoneProperties.PROJECT_DOMAIN_NAME, OPENSTACK_TENANTID);
// Project scoped authorization (can use the project name or the ID)
//        overrides.put(KeystoneProperties.SCOPE, "project:jclouds");
//        overrides.put(KeystoneProperties.SCOPE, "project:"+OPENSTACK_TENANTID);

//        overrides.put(KeystoneProperties.SCOPE, "project:jclouds");
//        overrides.put(KeystoneProperties.SCOPE, "projectId:"+OPENSTACK_TENANTID);// Domain scoped authorization (can use the domain name or the ID)
//        overrides.put(KeystoneProperties.SCOPE, "domain:default");
//        overrides.put(KeystoneProperties.SCOPE, "domainId:"+OPENSTACK_TENANTID);
//        swiftApi = ContextBuilder.newBuilder(provider)
//                .endpoint(OPENSTACK_AUTHURL)
//                .credentials(identityName, credential)
////                .apiVersion(version)
//                .overrides(overrides)
//                .modules(ImmutableSet.of(new SLF4JLoggingModule()))
//                .buildApi(SwiftApi.class);


//        String provider = "openstack-swift";  // openstack-swift or transient
//        BlobStoreContext context = ContextBuilder.newBuilder(provider)
//                .endpoint(OPENSTACK_AUTHURL)
//                .credentials(identityName, credential)
//                                .overrides(overrides)
//                .buildApi(BlobStoreContext.class);
//        BlobStore blobStore = context.getBlobStore();


        listContainers();

//        List<ObjectResponse> avotest = listObject("AVOTEST", "/");
//
//        log.debug("avotest {}", avotest);
    }

    @Override
    public void createContainer(String container) {
        try {
            log.info("Create Container {}", container);

            ContainerApi containerApi = swiftApi.getContainerApi(OPENSTACK_REGION);
            CreateContainerOptions options = CreateContainerOptions.Builder
                    .metadata(ImmutableMap.of(
                            "key1", "value1",
                            "key2", "value2"));

            containerApi.create(container, options);

            log.info("Container {} created", container);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error while createContainer ", e);
        }
    }

    @Override
    public void uploadObjectFromString(String container, String objName, MultipartFile file) throws IOException {
        log.info("Upload Object From String");

        ObjectApi objectApi = swiftApi.getObjectApi(OPENSTACK_REGION, container);
        Payload payload = new ByteSourcePayload(wrap(file.getBytes()));
//    Payload payload = newByteSourcePayload(wrap("Hello World".getBytes()));

        objectApi.put(objName, payload, PutOptions.Builder.metadata(ImmutableMap.of("key1", "value1")));

        log.info("obj name {} created", objName);
    }

    @Override
    public FileResponse downloadObjectFromString(String container, String objName) throws IOException {
        log.info("Upload Object From String");

        FileResponse fileResponse = new FileResponse();
//        ObjectApi objectApi = swiftApi.getObjectApi(OPENSTACK_REGION, container);
//
//        SwiftObject swiftObject = objectApi.get(objName);
//
//        log.info("obj name {} downloaded", objName);
//        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//
//        Payload payload = swiftObject.getPayload();
//        byte[] targetArray = new byte[payload.openStream().available()];
//        String binary = Base64.getEncoder().encodeToString(targetArray);
//
//        fileResponse.setName(swiftObject.getName());
//        fileResponse.setBinary(binary);
//        byteArrayOutputStream.close();

        return fileResponse;
    }

    @Override
    public void listContainers() {
        log.info("List Containers");
        ContainerApi containerApi = swiftApi.getContainerApi(OPENSTACK_REGION);
        Set<Container> containers = containerApi.list().toSet();

        for (Container container : containers) {
            log.info("  " + container.getName());
        }
    }

    @Override
    public List<ObjectResponse> listObject(String containerName, String path) throws ResponseStatusException {
        log.info("Entering listObject container {} and path {}", containerName, path);
        List<ObjectResponse> objectResponses = new ArrayList<>();
        try {
// Please refer to 'Keystone v2-v3 authentication' section for complete authentication use case
            String provider = OPENSTACK_PROVIDER;
//        String identityName = "default" + ":" + OPENSTACK_USERNAME; // tenantName:userName
//        String identityName = OPENSTACK_TENANTID + ":" + OPENSTACK_USERNAME; // tenantName:userName
            String identityName = OPENSTACK_TENANTNAME + ":" + OPENSTACK_USERNAME; // tenantName:userName
            String credential = OPENSTACK_PASSWORD;
            String version = OPENSTACK_VERSION;
            String keystone = OPENSTACK_KEYSTONE;

            final Properties overrides = new Properties();
            overrides.put(KeystoneProperties.KEYSTONE_VERSION, "3");

// Project scoped authorization (can use the project name or the ID)
//        overrides.put(KeystoneProperties.SCOPE, "project:jclouds");
//        overrides.put(KeystoneProperties.SCOPE, "project:"+OPENSTACK_TENANTID);

//        overrides.put(KeystoneProperties.SCOPE, "project:jclouds");
//        overrides.put(KeystoneProperties.SCOPE, "projectId:"+OPENSTACK_TENANTID);// Domain scoped authorization (can use the domain name or the ID)
//        overrides.put(KeystoneProperties.SCOPE, "domain:default");
//        overrides.put(KeystoneProperties.SCOPE, "domainId:"+OPENSTACK_TENANTID);

            swiftApi = ContextBuilder.newBuilder(provider)
                    .endpoint(OPENSTACK_AUTHURL)
                    .credentials(identityName, credential)
                    .overrides(overrides)
                    .modules(ImmutableSet.of(new SLF4JLoggingModule()))
                    .buildApi(SwiftApi.class);

            final int[] count = {StringUtils.countMatches(path, "/")};

            ListContainerOptions listContainerOptions = new ListContainerOptions();
//        listContainerOptions.
            ListContainerOptions filter = ListContainerOptions.Builder.path(path);

            listContainers();


            ObjectApi objectApi = swiftApi.getObjectApi(OPENSTACK_REGION, containerName);

            objectApi.list(filter).stream()
//        objs.stream()
                    .forEach(swiftObject -> {
                        log.info("Object within container {}", swiftObject.getName());
                        ObjectResponse objectResponse = new ObjectResponse();

                        objectResponse.setName(swiftObject.getName());

                        objectResponse.setEtag(swiftObject.getETag());
                        if (swiftObject.getETag() != null && swiftObject.getETag().equals(ParseObjectListFromResponse.SUBDIR_ETAG)) {
                            objectResponse.setSize(null);
                        } else {
                            objectResponse.setSize(swiftObject.getPayload().getContentMetadata().getContentLength());
                        }
                        objectResponse.setLastModified(swiftObject.getLastModified().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
                        objectResponse.setContainer(containerName);

                        objectResponses.add(objectResponse);

                    });
        } catch (Exception e) {
            log.error("Error while listObject {}", path, e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error while listObject", e);
        }
        createFolder(containerName, "cases/000/");
        return objectResponses;

    }

    @Override
    public ObjectResponse createFolder(String containerName, String path) throws ResponseStatusException {
        try {
            File tempFile = File.createTempFile("empty", "txt");

            ObjectApi objectApi = swiftApi.getObjectApi(OPENSTACK_REGION, containerName);

            ByteSource byteSource = Files.asByteSource(tempFile);
            Payload payload = Payloads.newByteSourcePayload(byteSource);

            objectApi.put(path, payload);

            log.info("obj name {} created", path);
        } catch (IOException e) {
            log.error("Error while creating folder {}", path, e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error while creating folder", e);
        }

        return null;
    }

    public void close() throws IOException {
//        Closeables.close(swiftApi, true);
    }

}