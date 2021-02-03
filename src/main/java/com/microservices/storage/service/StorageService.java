package com.microservices.storage.service;

import com.microservices.storage.streamchannels.SourceChannels;
import com.microservices.storage.VO.PathObj;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.http.ResponseEntity;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Slf4j
@EnableBinding(SourceChannels.class)
public class StorageService {

    @Value("${app.upload.dir:${user.home}}")
    private String uploadDir;

    private final String FILE_PATH = "pics";



    @Autowired
    private SourceChannels sourceChannels;

    @Autowired
    private RestTemplate restTemplate;


    /**
     *
     * @return Directory where files are saved
     */
    public String getPathToDirectory () {
        return uploadDir + File.separator + FILE_PATH + File.separator;
    }

    public String save(Long id, MultipartFile file) {
        log.info("Inside of save method of StorageService class, storage-service");
        //Directory where files are saved
        String pathToDir  = getPathToDirectory();
        //Directory for each item
        String dirName = StringUtils.cleanPath(String.valueOf(id));
        //Inside of item owned directory
        Path location = Paths.get(pathToDir + dirName);
        //Create if it does not exist
        File directory = new File(String.valueOf(location));
        if(!directory.exists()) {
            directory.mkdir();
        }
        //Add UUID in front of file name
        String saveLocation =  StringUtils.cleanPath(String.valueOf(UUID.randomUUID())) +file.getOriginalFilename();
        log.info(saveLocation);
        location = Paths.get(pathToDir + dirName + File.separator  + saveLocation);


        try {
            Files.copy(file.getInputStream(), location, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }


        PathObj pathObj = new PathObj();
        pathObj.setPath(saveLocation);
        pathObj.setItemId(id);
        publishPath(pathObj);
        return saveLocation;
    }

    /**
     * Send path info to rabbitMQ and get from item service to save to database
     */
    private void publishPath(PathObj pathObj) {
        log.info("Inside of publishPath method of StorageService class, storage-service");
        sourceChannels.outputFileCreated().send(MessageBuilder.withPayload(pathObj).setHeader("itemId", pathObj.getItemId()).build());
    }

    public byte[] getItemFiles(Long id,  String savedLocation) {
        log.info("Inside of getItemFiles method of StorageService class, storage-service");
        // PathObjList response= restTemplate.getForObject("http://item-service/items/getItemFileLocations/" + itemId, PathObjList.class);
        //Directory where files are saved
        String pathToDir  = getPathToDirectory();
        //Directory for each item
        String dirName = StringUtils.cleanPath(String.valueOf(id));

        log.info(savedLocation);
        //Inside of item owned directory
        Path path =  Paths.get(pathToDir + dirName + File.separator  + savedLocation);
                if (Files.exists(path)){
                    try {

                        DataInputStream dataInputStream = new DataInputStream(Files.newInputStream(path));
                        int size = dataInputStream.available(); //size of the Input stream
                        byte[] data = new byte[size];
                        dataInputStream.read(data);
                      //  byte[] data = ByteStreams.toByteArray(Files.newInputStream(path));
                        return data;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
        return null;
    }

    public ResponseEntity<Void> deleteItemFile(Long id, String loc) {
        log.info("Inside of deleteItemFiles method of StorageService class, storage-service");
        String pathToDir  = getPathToDirectory();
        //Directory for each item
        String dirName = StringUtils.cleanPath(String.valueOf(id));
        //Inside of item owned directory
        Path path =  Paths.get(pathToDir + dirName + File.separator  + loc);

        boolean deleted = false;
        try {
            deleted =  Files.deleteIfExists(path);
            if(deleted){
                publishDeletedPath(id, loc);
            }

        } catch (IOException e) {
            deleted = false;
            e.printStackTrace();
        }

        return  (deleted ?  ResponseEntity.ok().build() : ResponseEntity.notFound().build());
    }


    private void publishDeletedPath(Long id, String loc) {
        log.info("Inside of publishDeletedPath method of StorageService class, storage-service");
        PathObj pathObj = new PathObj();
        pathObj.setItemId(id);
        pathObj.setPath(loc);
        sourceChannels.outputFileDeleted().send(MessageBuilder.withPayload(pathObj).build());
    }
}
