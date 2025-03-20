package com.aws_s3_api.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
@Service
public class IS3ServiceImp implements IS3Service{
    @Value("${spring.destination.folder}")
    private String destinationFolder;

    @Autowired
    private S3Client s3Client;

    @Override
    public String createBucket(String bucketName) {
        CreateBucketResponse response=this.s3Client.createBucket(bucketBuilder -> bucketBuilder.bucket(bucketName));

        return "Bucket creado en ubicación: " + response.location();
    }

    @Override
    public String checkIfBucketExist(String bucketName) {
        try{
            this.s3Client.headBucket(headBucket ->headBucket.bucket(bucketName));
            return "el bucket " + bucketName + " si existe. ";
        }catch (S3Exception exception){
                return "el bucket " + bucketName + " no existe. ";
        }

    }

    @Override
    public List<String> getAllBuckets() {
        ListBucketsResponse bucketResponse=this.s3Client.listBuckets();
        if(bucketResponse.hasBuckets()){
            return bucketResponse.buckets()
                    .stream()
                    .map(Bucket::name)
                    .toList();
        }else{
            return List.of();
        }
    }

    @Override
    public Boolean uploadFile(String bucketName, String key, Path fileLocation) {
        PutObjectRequest putObjectRequest= PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
        PutObjectResponse putObjectResponse= this.s3Client.putObject(putObjectRequest, fileLocation);
        return putObjectResponse.sdkHttpResponse().isSuccessful();
    }

    @Override
    public void downloadFile(String bucket, String key) throws IOException {
        GetObjectRequest getObjectRequest= GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        ResponseBytes<GetObjectResponse> objectBytes = this.s3Client.getObjectAsBytes(getObjectRequest);
        String filename;
        if(key.contains("/")){
            filename=key.substring(key.lastIndexOf("/"));
        }else {
            filename=key;
        }
        String filePath = Paths.get(destinationFolder, filename).toString();
        File file= new File(filePath);
        file.getParentFile().mkdir();
        try(FileOutputStream fos = new FileOutputStream(file)){
            fos.write(objectBytes.asByteArray());

        }catch (IOException exception){
            throw new IOException("Error al descargar archivo" + exception.getCause());
        }


    }

    @Override
    public String generatePresignedUploadUrl(String bucketName, String key, Duration duration) {
        return null;
    }

    @Override
    public String generatePresignedDownloadUrl(String bucketName, String key, Duration duration) {
        return null;
    }
}
