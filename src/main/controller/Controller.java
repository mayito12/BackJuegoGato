package main.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import main.model.ClientSocket;
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
        byte[] ipBytes = {(byte)192,(byte)168,(byte)0, (byte)7 };
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

        if (o instanceof Server) {
            Socket socket = (Socket)arg;
            poolSocket.add(new Nodo(socket.hashCode(),"Nodo"+poolSocket.size(),socket));
            // Broadcast a todos los sockets conectados para actualizar la lista de conexiones
            broadCast();
            // Crear un hilo que reciba mensajes entrantes de ese nuevo socket creado
            ClientSocket clientSocket = new ClientSocket(socket);
            clientSocket.addObserver(this);
            new Thread(clientSocket).start();
            Platform.runLater(() -> listClient.getItems().add(socket.getInetAddress().getHostName()));
        }
        if (o instanceof ClientSocket){
            String mensaje = (String)arg;
            String[] datagrama;
            datagrama = mensaje.split("-");
            //enviando informacion
            sendMessage(datagrama[0],datagrama[1]);
            //Mostrando en listClient
            Platform.runLater(() -> listClient.getItems().add(mensaje));
            }
        }

        //Platform.runLater(() -> listClient.getItems().add(socket.getInetAddress().getHostName()));


    private void broadCast(){
        DataOutputStream bufferDeSalida = null;
        Nodo ultimaConexion = poolSocket.get(poolSocket.size()-1);
        for (Nodo nodo: poolSocket) {
            try {
                bufferDeSalida = new DataOutputStream(nodo.getSocket().getOutputStream());
                bufferDeSalida.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendMessage(String destino, String mensaje) {
        DataOutputStream bufferDeSalida = null;
        for (Nodo nodo : poolSocket) {
            if (destino.equals(nodo.getName())) {
                try {
                    bufferDeSalida= new DataOutputStream(nodo.getSocket().getOutputStream());
                    bufferDeSalida.flush();
                    bufferDeSalida.writeUTF(mensaje);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}



