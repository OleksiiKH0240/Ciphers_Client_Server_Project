package server_part;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) {
        //  стартуємо сервер на порту 8080
        try (ServerSocket server = new ServerSocket(8080)) {
//          чекаємо на підключення до сокету 'client' на серверній стороні
            Socket client = server.accept();
            System.out.println("Connection accepted.");

            // ініціюємо streams для обміну інформацією між сервером та сокетом 'client'

            // stream запису до сокету
            DataOutputStream outStream = new DataOutputStream(client.getOutputStream());

            // stream читання з сокету
            DataInputStream inStream = new DataInputStream(client.getInputStream());

//          рядок з інформацією для сокету
            String outputStr;

//          рядок з інформацією від сокета
            String inputStr;
            // обмінюємося інформацією з сокетом поки він не закрився
            while (!client.isClosed()) {
                inputStr = inStream.readUTF();

                if (inputStr.equalsIgnoreCase("exit")) {
                    System.out.println("Client initialize connection kill");
                    outStream.writeUTF("server gets: '" + inputStr + "'");
                    outStream.flush();
                    break;
                }

                outputStr = "server read: '" + inputStr + "'";
                System.out.println(outputStr);
                outStream.writeUTF(outputStr);

                // відправляє повідомлення не чекаючи наповнення буфера,
                // згідно з налаштуваннями системи
                outStream.flush();


            }

            System.out.println("Client disconnected");
            System.out.println("Closing connections & channels.");

            // закриваємо спочатку канали сокету
            inStream.close();
            outStream.close();

            // потім закриваємо сам сокет спілкування на стороні сервера
            client.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
