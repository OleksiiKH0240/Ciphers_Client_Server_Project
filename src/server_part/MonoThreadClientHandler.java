package server_part;

import server_part.auth.Login;
import server_part.auth.Register;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MonoThreadClientHandler implements Runnable {

    private Socket mClientDialog;
    private DataOutputStream mOutStream;
    private DataInputStream mInStream;
//    private boolean sIsClosed = false;
    private boolean mIsLoggedIn = false;


    public MonoThreadClientHandler(Socket client) throws IOException {
        mClientDialog = client;
        // ініціюємо streams для обміну інформацією між сервером та сокетом 'sClientDialog'

        // stream запису до сокету
        mOutStream = new DataOutputStream(mClientDialog.getOutputStream());

        // stream читання з сокету
        mInStream = new DataInputStream(mClientDialog.getInputStream());

    }

    /**
     * закриває streams пов'язані з сокетом 'sClientDialog' та сам сокет, видаляє сокет зі списку сокетів
     * та заменшує кількість активних скоетів на 1 в MultiThreadServer.java
     * @return number of errors that occur during function running
     */
    public int delClientThread() {
        int errorsCount = 0;

        // зменшуємо кількість активних клієнтів на 1,
        // видаляємо хендлер клієнта зі списку хендлерів на сервері
        MultiThreadServer.sCurrClientNumb -= 1;
        MultiThreadServer.sClientsThreads.remove(this);

        // закриваємо спочатку канали сокету
        try{
            mInStream.close();
        }
        catch (IOException e) {
            errorsCount++;
        }
        try {
            mOutStream.close();
        }
        catch (IOException e) {
            errorsCount++;
        }

        // потім закриваємо сам сокет 'sClientDialog' спілкування з сервером
        try {
            mClientDialog.close();
        }
        catch (IOException e) {
            errorsCount++;
        }
//        System.out.println("delClientThread " + this.getClass());

        System.out.println("delClientThread errors: " + errorsCount);
        return errorsCount;
    }

    public DataInputStream getMInStream() {
        return mInStream;
    }

    public DataOutputStream getMOutStream() {
        return mOutStream;
    }

    public Socket getMClientDialog() {
        return mClientDialog;
    }

    public void setISLoggedIn(boolean loggedIn) {
        mIsLoggedIn = loggedIn;
    }

    /**
     * handle queries from client's and provides special response for some query types.
     * query types: [register, login, save, load]
     * @param query string of client`s query
     * @return
     * query, if query type is not in list above
     * Register.registerPerson return, if query type is 'register';
     *
     * Login.loginPerson return, if query type is 'login';
     *
     * Message return, if query type is 'save';
     * Message object save function return:
     * "Forbidden option, you must log in first", if person that did not log in, try to use save option
     * "Message was saved", if all is ok
     * "FileNotFound", if file to save message was not found
     * "Message was not saved, something went wrong", if something went wrong during message saving
     *
     * Message object load function return, if query type is 'load'
     */
    public String queryHandler(String query){
//        Pattern pattern = Pattern.compile("<tok>\\w+</tok> <tok>\\w+</tok> <tok>\\w+</tok> <tok>(.\\n{0,2})+</tok>");
//        Matcher matcher = pattern.matcher(query);
//        boolean state1 = matcher.find();
//        boolean state2 = query.matches("<tok>\\w+</tok> <tok>\\w+</tok> <tok>\\w+</tok> <tok>(.\\n{0,2})+</tok>");


        String response = "";
        if (query.matches("<tok>\\w+</tok> <tok>\\w+</tok> <tok>\\w+</tok>")){
//            cases: register, login
            String[] tokens = query.split("</tok> <tok>");
            tokens[0] = tokens[0].replace("<tok>", "");
            tokens[2] = tokens[2].replace("</tok>", "");
            String name, password;
            if (tokens[0].equals("register")){
                name = tokens[1];
                password = tokens[2];
                response = Register.registerPerson(name, password);
            }
            else if (tokens[0].equals("login")){
                name = tokens[1];
                password = tokens[2];
                response = Login.loginPerson(name, password);
                if (response.equals("Successfully Logged In")){
                    mIsLoggedIn = true;
                }
            }
        }
        else if (query.matches("<tok>\\w+</tok> <tok>\\w+</tok> <tok>[\\w/]+</tok> <tok>(.\\n{0,2})+</tok>")){
//            cases: save
            String[] tokens = query.split("</tok> <tok>");
            tokens[0] = tokens[0].replace("<tok>", "");
            tokens[3] = tokens[3].replace("</tok>", "");
            if (tokens[0].equals("save")){
                if (!mIsLoggedIn){
                    response = "Forbidden option, you must log in first";
                    return response;
                }
                String name, encryptingType, message;
                name = tokens[1];
                encryptingType = tokens[2];
                message = tokens[3];
                int res = MultiThreadServer.getSMessageData().saveMessage(name, encryptingType, message);
                if (res == 0){
                    response = "Message was saved";
                }
                else if (res == -1){
                    response = "FileNotFound";
                }
                else if (res == -2){
                    response = "Message was not saved, something went wrong";
                }
            }
        }
        else if (query.matches("<tok>\\w+</tok> <tok>\\w+</tok>")){
//            cases: load
            if (!mIsLoggedIn){
                response = "Forbidden option, you must log in first";
                return response;
            }
            String[] tokens = query.split("</tok> <tok>");
            String name = tokens[1].replace("</tok>", "");
            response = MultiThreadServer.getSMessageData().loadMessage(name);
            boolean state = !response.contains("InputOutputException")
                    && !response.contains("FileNotFound")
                    && !response.contains("Message, with given name, was not found");
            if (state) {
                response = "<tok>loaded</tok> " + response;
            }
        }

        if (response.equals("")) response = query;
        return response;
    }

    /**
     * main function for MonoThreadClientHandler class with main conditionally endless cycle, in which you can interact with program
     * and program(MonoThreadClientHandler) can interact with client(Client)
     * function runs, when server create new Thread for MonoThreadClientHandler object,
     * after client get successfully connected to server(MultiThreadServer)
     */
    @Override
    public void run() {

        try {

            // рядок з інформацією для сокету клієнта
            String response;

            // рядок з інформацією від сокета клієнта
            String query;

            // обмінюємося інформацією з сокетом клієнта поки він не закрився
            while (!mClientDialog.isClosed()) {
                query = this.mInStream.readUTF();

                if (query.equalsIgnoreCase("exit")) {
                    System.out.println("Client initialize connection kill");

                    this.delClientThread();
                    System.out.println("Client disconnected");
                    System.out.println("Closing connections and read, write channels.");
                    break;
                }

                response = queryHandler(query);
                if (response.equals(query)) response = "server read: '" + response + "'";

                System.out.println(response);
                mOutStream.writeUTF(response);

                // відправляє повідомлення не чекаючи наповнення буфера
                mOutStream.flush();

            }

        } catch (IOException e) {
            System.out.println("Exception occurs(MonoThreadClientHandler)");
            e.printStackTrace();
            this.delClientThread();
        }

    }
}