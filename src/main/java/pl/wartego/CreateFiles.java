package pl.wartego;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;



public class CreateFiles {
    public static void main(String[] args) throws IOException {
        List<String> lists = new ArrayList<>();

        for (int i = 0; i < 5000000; i++) {
            lists.add("Line : " + i );
        }
        for (int i = 0; i < 20; i++) {
            Path file = Files.createFile(Path.of("C:\\OracleBucketFilesTest\\File"+i+".txt"));
            Files.write(file,lists);
        }

    }
}
