package com.httpServer.Config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class MyThread extends Thread {
    private Socket client;

    public MyThread( Socket client) {

        this.client = client;
    }


    @Override
    public void run() {
        System.out.println("Debug: establishing connection with client " + client.toString());

        try {
            BufferedReader br = new BufferedReader
                    (new InputStreamReader(client.getInputStream()));


            StringBuilder requestBuilder = new StringBuilder();
            String line;
            while (!(line = br.readLine()).isBlank()) {
                requestBuilder.append(line + "\r\n");
            }


            String request = requestBuilder.toString();
            String[] splitRequestLines = request.split("\r\n");
            String[] requestLine = splitRequestLines[0].split(" ");
            String method = requestLine[0];
            String path = requestLine[1];
            String version = requestLine[2];
            String host = splitRequestLines[1].split(" ")[1];

            List<String> headers = new ArrayList<>();
            for (int i = 2; i < splitRequestLines.length; i++) {
                String header = splitRequestLines[i];
                headers.add(header);
            }

            String accessLog = String.format("Client %s, method %s, path %s, version %s, host %s",
                    client.toString(), method, path, version, host, headers.toString());
            System.out.println(accessLog);

            Path filePath = getFilePath(path);

            if (Files.exists(filePath)) {
                String contentType = getContentType(filePath);
                sendResponseToClient(client, "200 OK", contentType, Files.readAllBytes(filePath));
            } else {
                //404  error
                byte[] contentNotFound = "<h1>Content Not Found :(</h1>".getBytes();
                sendResponseToClient(client, "404 Page Not Found", "text/html", contentNotFound);
            }

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

    }


    private static String getContentType(Path filePath) throws IOException {
        return Files.probeContentType(filePath);
    }

    private static Path getFilePath(String path){
        if ("/".equals(path)) {
            path = "/simple-html.html";
        } else{
            path = "/jsutajson.json";
        }

        return Paths.get("src/main/resources", path);
    }

    private static void sendResponseToClient(Socket client, String status,
                                             String contentType, byte[] content)
            throws IOException {
        String val = "";
        for(byte by: content){
            val+= (char) by;
        }

        OutputStream clientOutput = client.getOutputStream();
        clientOutput.write(("HTTP/1.1 \r\n" + status + "\r\n").getBytes());
        clientOutput.write(("ContentLength: " + val.getBytes().length + "\r\n").getBytes());
        clientOutput.write(("ContentType: " + contentType + "\r\n").getBytes());
        clientOutput.write("\r\n".getBytes());
        clientOutput.write(val.getBytes());
        clientOutput.write("\r\n\r\n".getBytes());
        clientOutput.flush();
        client.close();

    }
}
