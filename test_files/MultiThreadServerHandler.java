import server_part.MultiThreadServer;

public class MultiThreadServerHandler implements Runnable{

    @Override
    public void run() {
        MultiThreadServer.main(new String[]{});
    }
}
