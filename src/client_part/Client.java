package client_part;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {
    private static BufferedReader sBr;
    private static DataOutputStream sOutStream;
    private static DataInputStream sInStream;
    private static Socket sSocket;

    public static int delClient(){
        try {
            sInStream.close();
            sOutStream.close();
            sSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
            return 1;
        }
        return 0;
    }

    public static void main(String[] args) throws IOException {
        try {
            sSocket = new Socket("localhost", 8080);

            sBr = new BufferedReader(new InputStreamReader(System.in));
            sOutStream = new DataOutputStream(sSocket.getOutputStream());
            sInStream = new DataInputStream(sSocket.getInputStream());

//            boolean s = socket.isConnected();
            String clientCommand;
            String inputStr;

            // перевіряємо чи живий канал і працюємо якщо живий
            while (!sSocket.isOutputShutdown()) {

                // чекаємо вводу даних у клієнтську консоль
                clientCommand = sBr.readLine();//br.readLine();

                // пишемо дані з клієнтської консолі в канал сокету для сервера
                sOutStream.writeUTF(clientCommand);
                System.out.println("Client sent message '" + clientCommand + "' to server.");
                sOutStream.flush();

                if (clientCommand.equalsIgnoreCase("exit")) {
                    System.out.println("Client kill connection");
                    break;
                }

                // чекаємо на те, що нам відповість сервер на повідомлення
                System.out.println("waiting for reply...");
                inputStr = sInStream.readUTF();
                System.out.println(inputStr);
            }

            delClient();

        } catch (IOException e) {
            System.out.println("Exception occurs");
            e.printStackTrace();
            delClient();
        }

    }
}
