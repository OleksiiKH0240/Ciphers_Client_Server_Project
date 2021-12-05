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

    public static void main(String[] args) {

        // стартуємо сервер на порту 8080 та ініціалізуємо змінну 'br'
        // для обробки консольних команд для самого сервера
        try (ServerSocket server = new ServerSocket(8080);
             BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            server.setSoTimeout(SERVER_TIMEOUT);
            sClientsThreads = new ArrayList<>();

            System.out.println("Server socket created, command console listen to server commands");
            System.out.println("Also server waiting to connect");

            // стартуємо цикл за умови, що серверний сокет не закритий
            while (!server.isClosed()) {

                // перевіряємо, чи отримував сервер команді з консолі
                if (br.ready()) {
                    System.out.println("Main Server found some messages in channel");

                    // якщо отримали 'quit' то ініціалізуємо закриття сервера та
                    //вихід із циклу роздачі 'threads' монопотокових серверів
                    String serverCommand = br.readLine();
                    if (serverCommand.equalsIgnoreCase("exit")) {
                        System.out.println("Main Server initiate exiting...");
                        for (MonoThreadClientHandler cl: sClientsThreads){
//                            cl.getsInStream().close();
//                            cl.getsOutStream().close();
//                            cl.getsClientDialog().close();
                            cl.delClientThread();
                        }

                        for (int i = 0; i < sCurrClientNumb; i++){
                            sClientsThreads.remove(0);
                        }
                        server.close();
                        break;
                    }
                }

                // якщо нічого не отримали чекаємо
                // підключення до сокету під ім'ям - 'client' на
                // серверной стороні

                Socket client;
                try {
                    if (sCurrClientNumb < MAX_CLIENTS_NUMBER) {
                        client = server.accept();
                    }
                    else continue;
                } catch (SocketTimeoutException e) {continue;}

                // Після отримання запиту на підключення сервер створює сокет
                // для спілкування з клієнтом та відправляє його в окремий Thread
                // MonoThreadClientHandler і той
                // продовжує спілкування від імені сервера
//                int clientIdx = sCurrClientNumb;

                MonoThreadClientHandler clientThread = new MonoThreadClientHandler(client);
                sClientsThreads.add(clientThread);
                sCurrClientNumb++;
                sPool.execute(clientThread);
                System.out.println("Connection accepted.");
            }

//             sPool.shutdownNow();
            // закриття пулу Threads після завершення роботи всіх Threads
            sPool.shutdown();

        }
        catch (IOException e) {
            System.out.println("Exception occurs:-1");
            e.printStackTrace();
        }
    }
}
