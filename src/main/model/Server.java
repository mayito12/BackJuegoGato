package main.model;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Observable;

public class Server extends Observable implements Runnable{
    private ServerSocket serverSocket;

    public Server(ServerSocket serverSocket){
        this.serverSocket = serverSocket;
    }

    @Override
    public void run() {

        while (!serverSocket.isClosed()) {
            try {
                System.out.println("1");
                Socket socket = serverSocket.accept();
                System.out.println("2");
                this.setChanged();
                this.notifyObservers(socket);
            }
            catch (IOException e) {
                e.printStackTrace();
            }


        }

    }

}


