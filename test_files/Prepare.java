import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Prepare {
    /**
     * main function of Prepare class that do all needed before testing Client-Server application
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        //clear file MessagesFile.txt  before prepare function
        File f1 = new File("resource/MessagesFile.txt");
        FileWriter fw1 = new FileWriter(f1);
        fw1.close();

        //clear file PersonsFile.txt   before prepare function
        File f2 = new File("resource/PersonsFile.txt");
        FileWriter fw2 = new FileWriter(f2);
        fw2.close();

        //create pool for ClientHandler and MultiThreadServerHandler Threads
        ExecutorService sPool = Executors.newFixedThreadPool(2);

        MultiThreadServerHandler serverThread = new MultiThreadServerHandler();
        sPool.execute(serverThread);

        //create input stream from file with client`s commands for Client testing
        File cInputFile = new File("test_resource/testClientInputFile.txt");
        InputStream cInputStream = new FileInputStream(cInputFile);

        //create output stream from file for server(MonoThreadClientHandler) and client(Client) responses
        File cOutputFile = new File("test_resource/testClientOutputFile.txt");
        OutputStream cOutputStream = new FileOutputStream(cOutputFile);

//        Writer sWriter = new OutputStreamWriter(cOutputStream, "UTF-8");
//        for(int i = 0; i < 4; i++){
//            sWriter.write(i + "\n");
//        }
//        sWriter.close();
//        cOutputStream.close();

        ClientHandler clientThread = new ClientHandler(cInputStream, cOutputStream);
        sPool.execute(clientThread);

        sPool.shutdown();




//        File sInputFile = new File("test_resource/testClientInputFile.txt");
//        InputStream sInputStream = new FileInputStream(sInputFile);

    }
}
