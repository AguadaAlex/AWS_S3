package com.aws_s3_api.demo.controller;

import com.aws_s3_api.demo.service.IS3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("s3")
public class AppController {
    @Value("${spring.destination.folder}")
    private String destinationFolder;
    @Autowired
    private IS3Service service;

    @PostMapping("/create")
    public ResponseEntity<String> createBucket(@RequestParam String bucketName){
        return ResponseEntity.ok(this.service.createBucket(bucketName));

    }

    @GetMapping("/check/{bucketName}")
    public ResponseEntity<String> checkbucket(@PathVariable String bucketName){
        return ResponseEntity.ok(this.service.checkIfBucketExist(bucketName));
    }
    @GetMapping("/List")
    public ResponseEntity<List<String>> listBuckets(){
        return ResponseEntity.ok(this.service.getAllBuckets());
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFiles(@RequestParam String bucketName,
                                              @RequestParam String key,
                                              @RequestParam MultipartFile file) throws IOException {
        try{
            Path staticDic= Paths.get(destinationFolder);
            // si no existe directorio en S3 se lo crea
            if (!Files.exists(staticDic)) {
                Files.createDirectories(staticDic);
                
            }
            //Creamos el archivo en S3
            Path filePath= staticDic.resolve(file.getOriginalFilename());
            //Se llena el archivo
            Path finalPath= Files.write(filePath, file.getBytes());

            Boolean result=this.service.uploadFile(bucketName, key, finalPath);
            if(result){
                Files.delete(finalPath);
                return ResponseEntity.ok("Archivo cargado correctamente");
            }else{
                return ResponseEntity.internalServerError().body("Error al cargar el archivo al bucket");
            }

        }catch (IOException exception){
           throw new IOException("Error al generar ");
        }

    }

    @PostMapping("/download")
    public ResponseEntity<String> downloadFile(@RequestParam String bucketName, @RequestParam String key) throws IOException {
        this.service.downloadFile(bucketName,key);
        return ResponseEntity.ok("Archivo descargado correctamente");

    }
}
