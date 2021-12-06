import client_part.Client;
import server_part.MultiThreadServer;

import java.io.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Tests {
    public static void main(String[] args) throws IOException {


        List<String> checkClientFileContent;
        //get content of file 'testCheckClientOutputFile.txt'
        File f = new File("test_resource/testCheckClientOutputFile.txt");
        FileReader fr = new FileReader(f);
        BufferedReader bf = new BufferedReader(fr);
        checkClientFileContent = bf.lines().toList();
        String[] check_arr = checkClientFileContent.toArray(new String[0]);
        bf.close();
        fr.close();

        //get content of file 'testClientOutputFile.txt'
        List<String> clientFileContent;
        f = new File("test_resource/testClientOutputFile.txt");
        fr = new FileReader(f);
        bf = new BufferedReader(fr);
        clientFileContent = bf.lines().toList();
        String[] arr = clientFileContent.toArray(new String[0]);
        bf.close();
        fr.close();

        for (int i = 0; i < checkClientFileContent.size(); i++){
            System.out.println("test passed - " + check_arr[i].equals(arr[i]));
        }
    }
}