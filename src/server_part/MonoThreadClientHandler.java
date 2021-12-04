package server_part;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class MonoThreadClientHandler implements Runnable {

    private Socket sClientDialog;
    private DataOutputStream sOutStream;
    private DataInputStream sInStream;


    public MonoThreadClientHandler(Socket client) throws IOException {
        sClientDialog = client;
        // ініціюємо streams для обміну інформацією між сервером та сокетом 'sClientDialog'

        // stream запису до сокету
        sOutStream = new DataOutputStream(sClientDialog.getOutputStream());

        // stream читання з сокету
        sInStream = new DataInputStream(sClientDialog.getInputStream());

    }

    public int delClientThread() throws IOException{
        try {
            // зменшуємо кількість активних клієнтів на 1,
            // видаляємо хендлер клієнта зі списку хендлерів на сервері
            MultiThreadServer.sCurrClientNumb -= 1;
            MultiThreadServer.sClientsThreads.remove(this);

            // закриваємо спочатку канали сокету
            sInStream.close();
            sOutStream.close();

            // потім закриваємо сам сокет 'sClientDialog' спілкування з сервером
            sClientDialog.close();

        } catch (IOException e) {
            e.printStackTrace();
            return 1;
        }
        return 0;
    }

    public DataInputStream getsInStream() {
        return sInStream;
    }

    public DataOutputStream getsOutStream() {
        return sOutStream;
    }

    public Socket getsClientDialog() {
        return sClientDialog;
    }

    @Override
    public void run() {

        try {

            // рядок з інформацією для сокету клієнта
            String outputStr;

            // рядок з інформацією від сокета клієнта
            String inputStr;

            // обмінюємося інформацією з сокетом клієнта поки він не закрився
            while (!sClientDialog.isClosed()) {
                inputStr = this.sInStream.readUTF();

                if (inputStr.equalsIgnoreCase("exit")) {
                    System.out.println("Client initialize connection kill");
//                    outStream.writeUTF("in last time server read : '" + inputStr + "'");
//                    outStream.flush();
//                    MultiThreadServer.sCurrClientNumb -= 1;
//                    MultiThreadServer.sClientsThreads.remove(this);
                    this.delClientThread();
                    System.out.println("Client disconnected");
                    System.out.println("Closing connections and read, write channels.");
                    break;
                }

                outputStr = "server read: '" + inputStr + "'";
                System.out.println(outputStr);
                sOutStream.writeUTF(outputStr);

                // відправляє повідомлення не чекаючи наповнення буфера
                sOutStream.flush();

            }

        }
        catch (IOException e) {
            System.out.println("Exception occurs");
            e.printStackTrace();
            try {
                this.delClientThread();
            } catch (IOException ex) {
                System.out.println("Exception occurs");
                ex.printStackTrace();
            }
        }

    }
}