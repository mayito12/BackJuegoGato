package main.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import main.model.Nodo;
import main.model.Server;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

public class Controller implements Observer {
    ServerSocket serverSocket = null;
    private final int PORT = 3001;
    private ArrayList<Nodo> poolSocket = new ArrayList<>();

    @FXML
    private Button btnOpenServer;

    @FXML
    private Button btnSalir;

    @FXML
    private ListView<String> listClient;

    @FXML
    private Circle circleLed;

    @FXML
    void OpenServerOnMouseClicked(MouseEvent event) {
        byte[] ipBytes = {(byte)192,(byte)168,(byte)0, (byte)110 };
        InetAddress ip = null;

        try {
            ip = InetAddress.getByAddress(ipBytes);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        try {
            serverSocket = new ServerSocket(PORT,100,ip);
            listClient.getItems().add("Server abierto: " + serverSocket.getInetAddress().getHostName());
            circleLed.setFill(Color.GREEN);

           Server server = new Server(serverSocket);
           server.addObserver(this);
           new Thread(server).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
/*        finally {
            try {
                serverSocket.close();
                listClient.getItems().add("Server cerrado");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/

    }

    @FXML
    void SalirOnMouseClicked(MouseEvent event) {
        System.exit(1);
    }

    @Override
    public void update(Observable o, Object arg) {
        Socket socket = (Socket)arg;
        if (o instanceof Server) {
            poolSocket.add(new Nodo(socket.hashCode(),"nodo"+poolSocket.size(),socket));
            broadCast(); // Broadcast a todos los sockets conectados para actualizar la lista de conexiones
            // Crear un hilo que reciba mensajes entrantes de ese nuevo socket creado
        }

        Platform.runLater(() -> listClient.getItems().add(socket.getInetAddress().getHostName()));

    }

    private void broadCast(){
        DataOutputStream bufferDeSalida = null;
        Nodo ultimaConexion = poolSocket.get(poolSocket.size()-1);
        for (Nodo nodo: poolSocket) {
            try {
                bufferDeSalida = new DataOutputStream(nodo.getSocket().getOutputStream());
                bufferDeSalida.flush();
                bufferDeSalida.writeUTF("1:Servidor:"+nodo.getName()+":"+ultimaConexion.getName());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}



