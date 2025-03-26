package com.aws_s3_api.demo.task;

import com.aws_s3_api.demo.service.IS3Service;
import com.aws_s3_api.demo.service.IS3ServiceImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
@Component
public class TaskAwsDownloadImp implements TaskAwsDownload{
    private final IS3ServiceImp s3Service;
    private final S3Client s3Client;

    @Value("${spring.destination.folder}")
    private String destinationFolder;
    @Value("${aws.bucket.origen}")
    private String origen;
    @Value("${aws.bucket.destino}")
    private String destino;
    @Autowired
    public TaskAwsDownloadImp(IS3ServiceImp s3Service, S3Client s3Client) {
        this.s3Service = s3Service;
        this.s3Client = s3Client;
    }

    @Override
    @Scheduled(cron = "${cron.datadog.sync}")
    public void getFileFromAws() {
        List<String> pdfFiles = s3Service.listPdfFiles(origen);
        //Descarga Archivos de S3,  los comprime y los guarda Local


        try {
            s3Service.saveFilesAsZip(origen, pdfFiles);
            System.out.println("Tarea de guardado en Zip exitosa ");

        } catch (IOException e) {
            throw new RuntimeException("Error al Guardar el archivo ZIP", e);
        }

        // Subir archivos comprimidos al segundo bucket
        try {
            s3Service.uploadMultipleFiles(destino, destinationFolder);
            System.out.println("Archivos subidos a S3 exitosamente");
        } catch (RuntimeException e) {
            throw new RuntimeException("Error al subir archivos a S3", e);
        }







        // Elimina el archivo del bucket de S3

        try {
            s3Service.deleteAllPdfs(origen, pdfFiles);
            System.out.println("Archivos eliminados en S3 ");


        } catch (IOException e) {
            throw new RuntimeException("Se produjo un error al tratar de eliminar archivos en S3", e);
        }

    }
}
