package com.aws_s3_api.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
public class S3Config {
    @Value("${aws.access.key}")
    private String awsAccessKey;
    @Value("${aws.secret.key}")
    private String awsSecretKey;
    @Value("${aws.region}")
    private String region;
    /* Cliente S3 Sincrono

     */
    @Bean
    public S3Client getyS3Client(){
        AwsCredentials basicCredentials= AwsBasicCredentials.create(awsAccessKey,awsSecretKey);
        //Configuración para acceso a la consola de Amazon WS
        return S3Client.builder()
                .region(Region.of(region))
                .endpointOverride(URI.create("https://s3.us-east-1.amazonaws.com"))
                .credentialsProvider(StaticCredentialsProvider.create(basicCredentials))
                .build();
    }

    /* Cliente S3 Asincrono

     */

    @Bean
    public S3AsyncClient getyS3AsyncClient(){
        AwsCredentials basicCredentials= AwsBasicCredentials.create(awsAccessKey,awsSecretKey);
        //Configuración para acceso a la consola de Amazon WS
        return S3AsyncClient.builder()
                .region(Region.of(region))
                .endpointOverride(URI.create("https://s3.us-east-1.amazonaws.com"))
                .credentialsProvider(StaticCredentialsProvider.create(basicCredentials))
                .build();

    }

}
