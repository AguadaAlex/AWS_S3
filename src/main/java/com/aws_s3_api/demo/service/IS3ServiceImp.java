package com.aws_s3_api.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class IS3ServiceImp implements IS3Service{
    @Value("${spring.destination.folder}")
    private String destinationFolder;
    @Autowired
    private S3Client s3Client;

    @Autowired
    public IS3ServiceImp(S3Client s3Client) {
        this.s3Client = s3Client;
    }


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
    public void uploadFile(String bucketName, String filePath) {
        try {
            // Asegúrate de que filePath es una ruta válida y absoluta
            Path path = Paths.get(filePath).normalize();
            if (!Files.exists(path)) {
                throw new RuntimeException("El archivo no existe: " + path.toString());
            }

            // Usa solo el nombre del archivo como clave en S3
            String keyName = path.getFileName().toString();

            // Crea el objeto para subir a S3
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(keyName)
                    .build();

            // Sube el archivo a S3
            this.s3Client.putObject(putObjectRequest, RequestBody.fromFile(path));
            System.out.println("Archivo subido exitosamente a S3: " + keyName);

        } catch (Exception e) {
            throw new RuntimeException("Error al subir el archivo: " + e.getMessage(), e);
        }
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
    public byte[] downloadFilesS3(String bucket, String key) throws IOException {
        GetObjectRequest getObjectRequest= GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
            ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(getObjectRequest);
            return objectBytes.asByteArray();


    }

    @Override
    public List<String> listPdfFiles(String bucketName) {
        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .build();

        ListObjectsV2Response response = s3Client.listObjectsV2(request);

        // Filtrar archivos con extensión .pdf
        return response.contents().stream()
                .map(S3Object::key)
                .filter(key -> key.endsWith(".pdf"))
                .toList();
    }

    @Override
    public String generatePresignedUploadUrl(String bucketName, String key, Duration duration) {
        return null;
    }

    @Override
    public String generatePresignedDownloadUrl(String bucketName, String key, Duration duration) {
        return null;
    }
    @Override
    public void saveFilesAsZip(String bucketName, List<String> fileNames) throws IOException {
        // Define la ubicación donde se guardará el ZIP
        String zipFilePath = destinationFolder + "/archivos.zip";

        try (FileOutputStream fos = new FileOutputStream(zipFilePath);
             ZipOutputStream zipOut = new ZipOutputStream(fos)) {

            for (String fileName : fileNames) {
                // Descarga el archivo desde S3
                GetObjectRequest request = GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(fileName)
                        .build();
                ResponseBytes<GetObjectResponse> fileBytes = s3Client.getObjectAsBytes(request);
                // Guarda una copia local del archivo
                Path localFilePath = Paths.get(destinationFolder, fileName);
                Files.write(localFilePath, fileBytes.asByteArray());
                System.out.println("Archivo guardado localmente: " + localFilePath);

                // Agrega el archivo al ZIP
                ZipEntry zipEntry = new ZipEntry(fileName);
                zipOut.putNextEntry(zipEntry);
                zipOut.write(fileBytes.asByteArray());
                zipOut.closeEntry();
            }

        } catch (IOException e) {
            throw new RuntimeException("Error al crear el archivo ZIP", e);
        }

        System.out.println("Archivo ZIP guardado en: " + zipFilePath);
    }

    // Método para eliminar archivos de S3
    @Override
    public void deleteAllPdfs(String bucketName, List<String> fileNames)throws IOException {
        for (String fileName : fileNames) {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build();

            s3Client.deleteObject(deleteRequest);
            System.out.println("Archivo eliminado del bucket S3: " + fileName);

            // Eliminar archivo local
            Path localFilePath = Paths.get(destinationFolder, fileName); // Ruta del archivo local
            try {
                Files.deleteIfExists(localFilePath);
                System.out.println("Archivo eliminado localmente: " + localFilePath);
            } catch (IOException e) {
                System.err.println("No se pudo eliminar el archivo local: " + localFilePath);
            }

        }
    }
    @Override
    public void uploadMultipleFiles(String bucketName, String folderPath) {
        System.out.println("Subiendo archivo: " + folderPath);
        try {
            Files.list(Paths.get(folderPath))
                    .filter(file -> file.toString().endsWith(".pdf"))
                    .forEach(file -> {
                        try {
                            Path filePath = file.toAbsolutePath(); // Ruta completa del archivo
                            System.out.println("Subiendo archivo: " + filePath.toString());
                            String keyName = file.getFileName().toString(); // Nombre del archivo en el bucket
                            uploadFile(bucketName, filePath.toString());
                        } catch (RuntimeException e) {
                            System.err.println("Error al subir el archivo: " + e.getMessage());
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException("Error al procesar los archivos: " + e.getMessage(), e);
        }
    }
}
