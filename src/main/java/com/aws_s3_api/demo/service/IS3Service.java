package com.aws_s3_api.demo.service;

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

    Boolean uploadFile(String bucketName, String key, Path fileLocation);

    //Descargar un archivo de bucket
    void downloadFile(String bucket, String key)throws IOException;

    //Generar URL prefirmada para subir archivos
    String generatePresignedUploadUrl(String bucketName, String key , Duration duration);

    //Generar URL prefirmada para descargar archivos

    String generatePresignedDownloadUrl(String bucketName, String key , Duration duration);


}
