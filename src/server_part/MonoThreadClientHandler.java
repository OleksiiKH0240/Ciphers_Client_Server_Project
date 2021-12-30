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
        // initiate streams to exchange information between the server and the 'sClientDialog' socket

        // stream for writing in socket
        mOutStream = new DataOutputStream(mClientDialog.getOutputStream());

        // stream for reading from socket
        mInStream = new DataInputStream(mClientDialog.getInputStream());

    }

    /**
     * <p>closes the streams associated with the 'sClientDialog' socket and the socket itself,</p>
     * <p>removes the socket from the socket list</p>
     * <p>and reduces the number of active sockets by 1 in MultiThreadServer.java</p>
     * @return number of errors that occur during function running
     */
    public int delClientThread() {
        int errorsCount = 0;

        // reduce the number of active customers by 1,
        // remove the client handler from the list of handlers on the server
        MultiThreadServer.sCurrClientNumb -= 1;
        MultiThreadServer.sClientsThreads.remove(this);

        // close the socket streams first
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

        // then close the socket 'sClientDialog' communication socket
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
     * <p>handle queries from client's and provides special response for some query types.</p>
     * <p>query types: [register, login, save, load]</p>
     * @param query string of client`s query
     * @return
     * <p>query, if query type is not in list above</p>
     *
     * <p>Register.registerPerson return, if query type is 'register';</p>
     *
     * <p>Login.loginPerson return, if query type is 'login';</p>
     *
     * <p>Message return, if query type is 'save';</p>
     *
     * <p>Message object save function return:</p>
     * <p>"Forbidden option, you must log in first", if person that did not log in, try to use save option</p>
     * <p>"Message was saved", if all is ok</p>
     * <p>"FileNotFound", if file to save message was not found</p>
     * <p>"Message was not saved, something went wrong", if something went wrong during message saving</p>
     *
     * <p>Message object load function return, if query type is 'load'</p>
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
     * <p>main function for MonoThreadClientHandler class with main conditionally endless cycle, in which you can interact with program</p>
     * <p>and program(MonoThreadClientHandler) can interact with client(Client)</p>
     * <p>function runs, when server create new Thread for MonoThreadClientHandler object,</p>
     * <p>after client get successfully connected to server(MultiThreadServer)</p>
     */
    @Override
    public void run() {

        try {

            // string with information for client's socket
            String response;

            // string with information from client's socket
            String query;

            // exchange information with client's socket, until it closes
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

                // sends a message without waiting for the buffer to fill
                mOutStream.flush();

            }

        } catch (IOException e) {
            System.out.println("Exception occurs(MonoThreadClientHandler)");
            e.printStackTrace();
            this.delClientThread();
        }

    }


}