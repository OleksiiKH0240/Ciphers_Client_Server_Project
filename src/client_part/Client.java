package client_part;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import java.util.Scanner;


public class Client {
    private static BufferedReader sBr;
    private static DataOutputStream sOutStream;
    private static DataInputStream sInStream;
    private static Socket sSocket;
    private static Message sMsgType1;
    private static Message sMsgType2;

    public static String createQuery(String clCmd, Message msg) {
        String query = "";
        clCmd = clCmd.toLowerCase();
        Scanner sc = new Scanner(System.in);

        if (clCmd.equals("register") || clCmd.equals("login")) {
            System.out.println("input your name:");
            String name = sc.nextLine();
            System.out.println("input your password:");
            String password = sc.nextLine();
            query = "<tok>%s</tok> <tok>%s</tok> <tok>%s</tok>".formatted(clCmd, name, password);
        } else if (clCmd.equals("save")) {
            String encodedMessage;
            String name;
            while (true) {
                try {
                    System.out.println("input your name:");
                    name = sc.nextLine();
                    System.out.println("input message you would like to save on server(in one line):");
                    String message = sc.nextLine();
                    encodedMessage = msg.encodeMsg(message);
                    break;
                } catch (NoSuchPaddingException e) {
                    System.out.println(e);
                } catch (InvalidKeyException e) {
                    System.out.println(e);
                } catch (IllegalBlockSizeException e) {
                    System.out.println(e);
                } catch (BadPaddingException e) {
                    System.out.println(e);
                } catch (NoSuchAlgorithmException e) {
                    System.out.println(e);
                } catch (InvalidAlgorithmParameterException e) {
                    System.out.println(e);
                }
            }
            query = "<tok>%s</tok> <tok>%s</tok> <tok>%s</tok> <tok>%s</tok>".formatted(
                    "save", name, msg.getEncryptingType(), encodedMessage);
        } else if (clCmd.equals("load")) {
            System.out.println("input your name:");
            String name = sc.nextLine();
            query = "<tok>%s</tok> <tok>%s</tok>".formatted("load", name);
        }


        if (query.equals("")) query = clCmd;
        return query;
    }

    public static String responseHandler(String response) {
        String modResponse = "";
        if (response.matches("<tok>loaded</tok> <tok>[\\w/]+</tok> <tok>(.\\n{0,2})+</tok>")) {
//            loaded
            String[] tokens = response.split("</tok> <tok>");
            String encryptingType = tokens[1];
            String message = tokens[2].replace("</tok>", "");
            String decodeMessage = "";
            if (encryptingType.equals(sMsgType1.getEncryptingType())) {
//                RSA
                try {
                    decodeMessage = sMsgType1.decodeMsg(message);
                } catch (NoSuchPaddingException | InvalidAlgorithmParameterException |
                        BadPaddingException | IllegalBlockSizeException | InvalidKeyException |
                        NoSuchAlgorithmException e) {
                    e.printStackTrace();
                    return "something went wrong during decoding";
                }

            } else if (encryptingType.equals(sMsgType2.getEncryptingType())) {
//                AES
                try {
                    decodeMessage = sMsgType2.decodeMsg(message);
                } catch (NoSuchPaddingException | InvalidAlgorithmParameterException |
                        BadPaddingException | IllegalBlockSizeException | InvalidKeyException |
                        NoSuchAlgorithmException e) {
                    e.printStackTrace();
                    return "something went wrong during decoding";
                }

            }
            modResponse = "loaded, decoded message: '" + decodeMessage + "'";

        }

        return (modResponse.equals("")) ? response : modResponse;
    }

    // закриває streams пов'язані з сокетом 'sSocket' та сам сокет
    public static int delClient() {
        int errorsCount = 0;
        // закриваємо спочатку канали сокету
        try {
            sInStream.close();
        } catch (IOException e) {
            errorsCount++;
        }
        try {
            sOutStream.close();
        } catch (IOException e) {
            errorsCount++;
        }

        // потім закриваємо сам сокет 'sSocket' спілкування з сервером
        try {
            sSocket.close();
        } catch (IOException e) {
            errorsCount++;
        }

        System.out.println("delClient errors: " + errorsCount);
        return errorsCount;
    }

    public static Message chooseEncryptingType() {
        Message msg = sMsgType1;
        Scanner sc = new Scanner(System.in);

        System.out.println("Choose encryption algorithm:\n" +
                "1- " + sMsgType1.getEncryptingType() + ", key size: " + sMsgType1.getMKeySize() + ";\n" +
                "2- " + sMsgType2.getEncryptingType() + ", key size: " + sMsgType2.getMKeySize() + ".\n" +
                "(1/2):");
        Character mngChar = sc.nextLine().charAt(0);
        if (mngChar == '1') {
            msg = sMsgType1;
        } else if (mngChar == '2') {
            msg = sMsgType2;
        }
        System.out.println("you choose " + msg.getEncryptingType() + " encryption, " +
                "with " + msg.getMKeySize() + " key size");

        return msg;
    }

    public static void main(String[] args) {
        try {
            sSocket = new Socket("localhost", 8080);

            sBr = new BufferedReader(new InputStreamReader(System.in));
            sOutStream = new DataOutputStream(sSocket.getOutputStream());
            sInStream = new DataInputStream(sSocket.getInputStream());

//            boolean s = sSocket.isConnected();
            String clientCommand;
            String query;
            String response;

            Scanner sc = new Scanner(System.in);
            sMsgType1 = new Message("RSA", 2048);
            sMsgType2 = new Message("AES/CBC/PKCS5Padding", 256);

            Message msg = Client.chooseEncryptingType();


            // перевіряємо чи живий канал і працюємо якщо живий
            while (!sSocket.isOutputShutdown()) {

                // чекаємо вводу даних у клієнтську консоль
                clientCommand = sBr.readLine();

                if (clientCommand.equalsIgnoreCase("exit")) {
                    System.out.println("Client kill connection");
                    break;
                } else if (clientCommand.equals("choose algo")) {
                    Client.chooseEncryptingType();
                    continue;
                }

                query = createQuery(clientCommand, msg);

                // пишемо опрацьовані дані з клієнтської консолі
                // в канал сокету клієнта для сервера
                sOutStream.writeUTF(query);
                System.out.println("Client sent message '" + query + "' to server.");
                sOutStream.flush();


                // чекаємо на те, що нам відповість сервер на повідомлення
                System.out.println("waiting for reply...");
                response = sInStream.readUTF();

                response = responseHandler(response);

                System.out.println(response);
            }

            delClient();

        } catch (IOException | NoSuchAlgorithmException e) {
            System.out.println("Exception occurs");
            e.printStackTrace();
            delClient();
        }

    }
}
