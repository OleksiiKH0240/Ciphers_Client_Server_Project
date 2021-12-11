import client_part.Client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ClientHandler implements Runnable {
    public ClientHandler() {
    }

    /**
     *
     * @param customInputStream input stream created from file with client`s commands
     * @param customOutputStream output stream created from file for server response and client response
     */
    public ClientHandler(InputStream customInputStream, OutputStream customOutputStream) {
        Client.setSCustomInputStream(customInputStream);
        Client.setSCustomOutputStream(customOutputStream);
    }

    @Override
    public void run() {
        try {
            Client.main(new String[]{});
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
