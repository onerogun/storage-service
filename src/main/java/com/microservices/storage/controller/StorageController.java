package com.microservices.storage.controller;

import com.microservices.storage.service.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/storage")
@Slf4j
public class StorageController {

    private final StorageService storageService;

    @Autowired
    public StorageController(StorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping(value = "/save/{productId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> saveFile(@PathVariable("productId") Long id, @RequestParam(name = "file") MultipartFile file) {
        log.info("Inside of saveFile method of StorageController class, storage-service");
        String path = storageService.save(id, file);
        return new ResponseEntity<>(path, HttpStatus.ACCEPTED);
    }

    @GetMapping("/getItemFiles/{id}/{loc}")
    public ResponseEntity<byte[]> getItemFiles(@PathVariable Long id, @PathVariable String loc) {
        log.info("Inside of getItemFiles method of StorageController class, storage-service");
        log.info("path: " + loc);
        return new ResponseEntity<>(storageService.getItemFiles(id, loc), HttpStatus.OK);
    }

    @DeleteMapping("/deleteItemFiles/{id}/{loc}")
    public ResponseEntity<Void> deleteItemFiles(@PathVariable Long id, @PathVariable String loc) {
        log.info("Inside of deleteItemFiles method of StorageController class, storage-service");
        log.info("ID: " + id +  " path: " + loc);
        return storageService.deleteItemFile(id, loc);
    }

}
