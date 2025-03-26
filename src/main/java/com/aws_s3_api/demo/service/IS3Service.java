package com.aws_s3_api.demo.service;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.endpoints.internal.Value;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

public interface IS3Service {


    //Crear Bucket en S3
    String createBucket(String BucketName);

    //Saber si un bucket existe
    String checkIfBucketExist(String bucketName);

    // Listar buckets
    List<String> getAllBuckets();

    //Cargar archivo a un bucket

    void uploadFile(String bucketName, String key);

    //Descargar un archivo de bucket
    void downloadFile(String bucket, String key)throws IOException;

    byte[] downloadFilesS3(String bucket, String key)throws IOException;

    List<String> listPdfFiles(String bucketName);

    void saveFilesAsZip(String bucketName, List<String> fileNames) throws IOException;

    void deleteAllPdfs(String bucketName, List<String> fileNames)throws IOException;

    void uploadMultipleFiles(String bucketName, String folderPath);

    //Generar URL prefirmada para subir archivos
    String generatePresignedUploadUrl(String bucketName, String key , Duration duration);

    //Generar URL prefirmada para descargar archivos

    String generatePresignedDownloadUrl(String bucketName, String key , Duration duration);


}
