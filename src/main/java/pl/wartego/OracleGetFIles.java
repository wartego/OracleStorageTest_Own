package pl.wartego;

import com.oracle.bmc.ConfigFileReader;
import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.bmc.objectstorage.model.ObjectSummary;
import com.oracle.bmc.objectstorage.requests.CreateBucketRequest;
import com.oracle.bmc.objectstorage.requests.GetObjectRequest;
import com.oracle.bmc.objectstorage.requests.ListObjectsRequest;
import com.oracle.bmc.objectstorage.requests.PutObjectRequest;
import com.oracle.bmc.objectstorage.responses.GetObjectResponse;
import com.oracle.bmc.objectstorage.responses.ListObjectsResponse;
import com.oracle.bmc.objectstorage.transfer.UploadConfiguration;
import com.oracle.bmc.objectstorage.transfer.UploadManager;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

public class OracleGetFIles {
    static  ConfigFileReader.ConfigFile configFile;
    static AuthenticationDetailsProvider provider;
    static ObjectStorageClient client ;

            static{
                try {
                    configFile = ConfigFileReader.parseDefault();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                provider = new ConfigFileAuthenticationDetailsProvider(configFile);

                /* Create a service client */
                client = ObjectStorageClient.builder().build(provider);
            }

    public static void main(String[] args) throws IOException {

                getOracleFile();
      // getObjectFromBucket(getOracleFile());;
        saveFilesOnOCI();
    }
    public static List<ObjectSummary> getOracleFile() throws IOException {


        /* Create a request and dependent object(s). */

        ListObjectsRequest listObjectsRequest = ListObjectsRequest.builder()
                .namespaceName("frfoypo4fiml")
                .bucketName("bucket-test")
                .fields("size,md5,timeCreated")

               .build();

        /* Send request to the Client */
        ListObjectsResponse response = client.listObjects(listObjectsRequest);
        List<ObjectSummary> objects = response.getListObjects().getObjects();
        Long size = objects.get(0).getSize();

        objects.forEach((s)-> System.out.println("Name: " + s.getName() + " , size[bytes]: " + s.getSize()));
        //objects.forEach(System.out::println);

        System.out.println(size);
       // ListObjects listObjects = response.getListObjects();
       // GetBucketResponse response1 = response.getListObjects();

      //  System.out.println(listObjects.getObjects().get(0).getName());

        return objects;
    }


    public static void getObjectFromBucket(List<ObjectSummary> objectSummaries){

        objectSummaries.forEach((s)-> {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .namespaceName("frfoypo4fiml")
                    .bucketName("bucket-test")
                    .objectName(s.getName())
                    .build();

            GetObjectResponse response = client.getObject(getObjectRequest);
            File existFile = new File("C:\\OracleBucketFilesTest\\" + s.getName());
            //System.out.println("Oracle size: "+ s.getSize() + " current disk size: " + existFile.length());

            boolean exist;
            if(exist = (!existFile.exists() || (existFile.exists() && existFile.length() != s.getSize()))){
                existFile.delete(); //delete before save new file
                final BufferedInputStream input = new BufferedInputStream(response.getInputStream());
                try {
                    Files.copy(input, new File("C:\\OracleBucketFilesTest\\" + s.getName()).toPath()); //location to local path
                    // objectData.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }


            }


        });


    }
    public static void saveFilesOnOCI(){
        File fileToUpload = new File("C:\\OracleBucketFilesTest\\upload\\uploadText.txt");
        UploadConfiguration uploadConfiguration =
                UploadConfiguration.builder()
                        .allowMultipartUploads(true)
                        .allowParallelUploads(true)
                        .build();

        UploadManager uploadManager = new UploadManager(client, uploadConfiguration);
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .namespaceName("frfoypo4fiml")
                .bucketName("bucket-test")
                .objectName(fileToUpload.getName())
                .build();



        UploadManager.UploadRequest uploadDetails =
                UploadManager.UploadRequest.builder(fileToUpload).allowOverwrite(true).build(putObjectRequest);
        UploadManager.UploadResponse response = uploadManager.upload(uploadDetails);
        System.out.println(response);



        // fetch the object just uploaded
        GetObjectResponse getResponse =
                client.getObject(
                        GetObjectRequest.builder()
                                .namespaceName("frfoypo4fiml")
                                .bucketName("bucket-test")
                                .objectName(fileToUpload.getName())
                                .build());

        // use the response's function to print the fetched object's metadata
        System.out.println(getResponse.getOpcMeta());

        // stream contents should match the file uploaded
        try (final InputStream fileStream = getResponse.getInputStream()) {
            // use fileStream
        } catch(Exception e) {// try-with-resources automatically closes fileStream

        }
    }

    }


