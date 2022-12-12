package server_part;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * <p>server class</p>
 * <p>sPersonData: object of class {@link PersonData} that helps to save and load information about clients.</p>
 * <p>sMessageData: object of class {@link MessageData} that helps to save and load message received from Client.</p>
 * <p>sPool: object of class {@link ExecutorService} that manage all threads to multithreading of server.</p>
 * <p>SERVER_TIMEOUT: time in milliseconds after which server.accept function give back control to the main function.</p>
 */
public class MultiThreadServer {
    final static int SERVER_TIMEOUT = 1000;
    final static int MAX_CLIENTS_NUMBER = 2;

    static ExecutorService sPool = Executors.newFixedThreadPool(MAX_CLIENTS_NUMBER);
    static int sCurrClientNumb = 0;
    static ArrayList<MonoThreadClientHandler> sClientsThreads;
    static private PersonData sPersonData = new PersonData();
    static private MessageData sMessageData = new MessageData();

    public static PersonData getSPersonData() {
        return sPersonData;
    }

    public static MessageData getSMessageData() {
        return sMessageData;
    }

    /**
     * <p>main function of MultiThreadServer class, with main conditionally endless cycle, in which you can interact with program</p>
     * <p>and program(MultiThreadServer) can interact with MonoThreadClientHandler objects</p>
     * @param args
     */
    public static void main(String[] args) {

        // start the server on port 8080 and initialize the variable 'br'
        // to process console commands for the server itself
        try (ServerSocket server = new ServerSocket(80);
             BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            server.setSoTimeout(SERVER_TIMEOUT);
            sClientsThreads = new ArrayList<>();

            System.out.println("Server socket created, command console listen to server commands");
            System.out.println("Also server waiting to connect");

            // start the loop if server socket is not closed
            while (!server.isClosed()) {

                // check if server received commands from the console
                if (br.ready()) {
                    System.out.println("Main Server found some messages in channel");

                    // if received 'quit' then initialize server closure and
                    // exit form loop of creating new threads for MonoThreadClientHandler objects
                    String serverCommand = br.readLine();
                    if (serverCommand.equalsIgnoreCase("exit")) {
                        System.out.println("Main Server initiate exiting...");
                        for (MonoThreadClientHandler cl: sClientsThreads){
                            cl.getMInStream().close();
                            cl.getMOutStream().close();
                            cl.getMClientDialog().close();
//                            cl.delClientThread();
                        }

                        for (int i = 0; i < sCurrClientNumb; i++){
                            sClientsThreads.remove(0);
                        }
                        server.close();
                        break;
                    }
                }

                // if nothing is received we wait
                // connect to a socket named - 'client' on
                // server side

                Socket client;
                try {
                    if (sCurrClientNumb < MAX_CLIENTS_NUMBER) {
                        client = server.accept();
                    }
                    else continue;
                } catch (SocketTimeoutException e) {continue;}

                // After receiving the connection request, the server creates a socket
                // to communicate with the client and the MonoThreadClientHandler object, which sends to a separate Thread
                // then the MonoThreadClientHandler object
                // continues to communicate with client instead of server
                // int clientIdx = sCurrClientNumb;

                MonoThreadClientHandler clientThread = new MonoThreadClientHandler(client);
                sClientsThreads.add(clientThread);
                sCurrClientNumb++;
                sPool.execute(clientThread);
                System.out.println("Connection accepted.");
            }

//             sPool.shutdownNow();
            // close the Threads pool after all Threads have shut down
            sPool.shutdown();

        }
        catch (IOException e) {
            System.out.println("Exception occurs:-1");
            e.printStackTrace();
        }
    }
}
