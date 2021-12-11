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

    /**
     *
     * @param clCmd client command
     * @param msg
     * @return special String which consists some tokens, with information about server's query,
     * or simple client command
     * query type:
     * clCmd - register : query - '<tok>register</tok> <tok>name</tok> <tok>password</tok>';
     * clCmd - login : query - '<tok>login</tok> <tok>name</tok> <tok>password</tok>';
     * clCmd - save : query - '<tok>save</tok> <tok>name</tok> <tok>encryptingType</tok> <tok>message</tok>';
     * clCmd - load : query - '<tok>load</tok> <tok>name</tok>';
     * where
     * name - identifier for message and client;
     * exit, register, login, save, load, choose algo - clients commands;
     * encryptingType - encrypting type method(RSA, AES), which was used to encrypt message;
     * message - some string, which was encrypted by user, using some encrypting algorithm.
     * @throws IOException
     */
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
        }
        else if (clCmd.equals("save")) {
            String encodedMessage;
            String name;
            while (true) {
                try {
                    System.out.println("input your name:");
//                    name = sc.nextLine();
                    name = sBr.readLine();

                    String message = "";

                    System.out.println("Do you want to load message from file?{y/n}:");
                    Character manageChar = 'n';
                    try {
                        manageChar = sBr.readLine().charAt(0);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (manageChar == 'y'){
                        File f = new File("resource/ClientMessageFile.txt");
                        try {
                            FileReader fr = new FileReader(f);
                            BufferedReader br = new BufferedReader(fr);
                            message = br.readLine();
                            br.close();
                            fr.close();
                        } catch (IOException e) {
                            System.out.println("message not saved to file");
                            e.printStackTrace();
                        }
                    }

                    if (manageChar != 'y' || message.equals("")) {
                        System.out.println("input message you would like to save on server(in one line):");
//                    String message = sc.nextLine();
                        message = sBr.readLine();
                    }
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
        }
        else if (clCmd.equals("load")) {
            System.out.println("input your name:");
//            String name = sc.nextLine();
            String name = sBr.readLine();
            query = "<tok>%s</tok> <tok>%s</tok>".formatted("load", name);
        }


        if (query.equals("")) query = clCmd;
        return query;
    }

    /**
     *
     * @param response server response
     * @return server response in case it not in list below, or some custom object according to server response that received.
     * handle some server responses: [loaded_response,]
     * other server responses invoke no reaction.
     *
     * loaded_response: string, which looking like '<tok>loaded</tok> <tok>encryptingType</tok> <tok>message</tok>',
     * where
     * encryptingType is encrypting type method(RSA, AES), which was used to encrypt message;
     * message is some string, which was encrypted by user, using some encrypting algorithm.
     * returned object in case of loaded_response is decrypted message of server response
     */
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
            System.out.println("Do you want to save decrypted message in file?{y/n}:");
            Character manageChar = 'n';
            try {
                manageChar = sBr.readLine().charAt(0);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (manageChar == 'y'){
                File f = new File("resource/ClientMessageFile.txt");
                try {
                    FileWriter writer = new FileWriter(f);
                    writer.write(decodeMessage);
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    System.out.println("message not saved to file");
                    e.printStackTrace();
                }
            }

        }

        return (modResponse.equals("")) ? response : modResponse;
    }

    /**
     * close clients streams linked with socket 'sSocket' and socket itself
     * @return
     */
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

    /**
     * give an opportunity to change current algorithm of encrypting
     * @return Message object, with chosen encrypt algorithm
     * @throws IOException throws if there are some problems with sBr static variable
     * or with input stream of class Client
     */
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

    /**
     * main function of Client class, with main conditionally endless cycle, in which you can interact with program
     * and program(Client) can interact with MonoThreadClientHandler object
     * @param args
     * @throws IOException throws if there are some problems with sBr\sWriter\sOutStream\sInStream static variables
     * or with input\output streams of class Client
     */
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
