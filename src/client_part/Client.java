package client_part;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;


public class Client {
    private static InputStream sCustomInputStream = null;
    private static BufferedReader sBr;
    private static OutputStream sCustomOutputStream = null;
    private static Writer sWriter;
//    private static DataOutputStream sDOS;
    private static DataOutputStream sOutStream;
    private static DataInputStream sInStream;
    private static Socket sSocket;
    private static Message sMsgType1;
    private static Message sMsgType2;

    public static void setSCustomInputStream(InputStream sCustomInputStream) {
        Client.sCustomInputStream = sCustomInputStream;
    }

    public static void setSCustomOutputStream(OutputStream customOutputStream) {
        Client.sCustomOutputStream = customOutputStream;
    }

    public static String createQuery(String clCmd, Message msg) throws IOException {
        String query = "";
        clCmd = clCmd.toLowerCase();
//        Scanner sc = new Scanner(System.in);

        if (clCmd.equals("register") || clCmd.equals("login")) {
            System.out.println("input your name:");
//            String name = sc.nextLine();
            String name = sBr.readLine();
            System.out.println("input your password:");
//            String password = sc.nextLine();
            String password = sBr.readLine();
            query = "<tok>%s</tok> <tok>%s</tok> <tok>%s</tok>".formatted(clCmd, name, password);
        } else if (clCmd.equals("save")) {
            String encodedMessage;
            String name;
            while (true) {
                try {
                    System.out.println("input your name:");
//                    name = sc.nextLine();
                    name = sBr.readLine();
                    System.out.println("input message you would like to save on server(in one line):");
//                    String message = sc.nextLine();
                    String message = sBr.readLine();
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
//            String name = sc.nextLine();
            String name = sBr.readLine();
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

    public static Message chooseEncryptingType() throws IOException {
        Message msg = sMsgType1;

        System.out.println("Choose encryption algorithm:\n" +
                "1- " + sMsgType1.getEncryptingType() + ", key size: " + sMsgType1.getMKeySize() + ";\n" +
                "2- " + sMsgType2.getEncryptingType() + ", key size: " + sMsgType2.getMKeySize() + ".\n" +
                "(1/2):");
        String s = sBr.readLine();
        Character mngChar = s.charAt(0);
        if (mngChar == '1') {
            msg = sMsgType1;
        } else if (mngChar == '2') {
            msg = sMsgType2;
        }
        System.out.println("you choose " + msg.getEncryptingType() + " encryption, " +
                "with " + msg.getMKeySize() + " key size");

        return msg;
    }

    public static void main(String[] args) throws IOException {
        try {
            sSocket = new Socket("localhost", 8080);

            //test case
            if (sCustomInputStream != null) {
                sBr = new BufferedReader(new InputStreamReader(sCustomInputStream));
            } else {
                sBr = new BufferedReader(new InputStreamReader(System.in));
            }

            //test case
            if (sCustomOutputStream != null){
                sWriter = new OutputStreamWriter(sCustomOutputStream, "UTF-8");
//                sDOS = new DataOutputStream(sCustomFileOutputStream);
            }

            sOutStream = new DataOutputStream(sSocket.getOutputStream());
            sInStream = new DataInputStream(sSocket.getInputStream());

//            boolean s = sSocket.isConnected();
            String clientCommand;
            String query;
            String response;

//            Scanner sc = new Scanner(System.in);
            sMsgType1 = new Message("RSA", 2048);
            sMsgType2 = new Message("AES/CBC/PKCS5Padding", 256);

            Message msg = Client.chooseEncryptingType();


            // перевіряємо чи живий канал і працюємо якщо живий
            while (!sSocket.isOutputShutdown()) {

                // чекаємо вводу даних у клієнтську консоль
                System.out.println("commands:exit, register, login, save, load, choose algo");
                System.out.println("input:");
                clientCommand = sBr.readLine();
//                test case
                if (sCustomInputStream != null){
                    System.out.println(clientCommand);
                }

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


                //test case
                if (sCustomOutputStream != null) {
                    sWriter.append(response + "\n");
                    sWriter.flush();
//                    System.out.println(response);
                } else{
                    System.out.println(response);
                }



//                    sDOS.writeUTF(response + "\n");
//                    sDOS.flush();
            }

            delClient();
            sBr.close();
            sCustomInputStream.close();

            //test case
            if (sCustomOutputStream != null) {
                sWriter.close();
                sCustomOutputStream.close();
            }

        } catch (IOException | NoSuchAlgorithmException e) {
            System.out.println("Exception occurs");
            e.printStackTrace();
            delClient();
            sBr.close();
            sCustomInputStream.close();

            //test case
            if (sCustomOutputStream != null) {
                sWriter.close();
                sCustomOutputStream.close();
            }
        }

    }
}
