package com.httpServer.Config;


import java.io.IOException;

import java.net.ServerSocket;
import java.net.Socket;


public class ConfigurationManager {

    public static void main(String[] args) throws IOException {

        ServerSocket serverSocket = new ServerSocket(8080);
        Socket client = null;
            try {
                while (true) {
                    client = serverSocket.accept();
                    MyThread myThread = new MyThread(client);
                    myThread.start();




                }
            }catch (IOException e){
                client.close();
                serverSocket.close();
            }



        }
}

