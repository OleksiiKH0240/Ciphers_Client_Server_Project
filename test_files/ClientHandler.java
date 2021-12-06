import client_part.Client;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ClientHandler implements Runnable {
    public ClientHandler() {
    }

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
