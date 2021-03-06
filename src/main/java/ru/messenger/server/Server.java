package ru.messenger.server;


import ru.messenger.CustomLogger;
import ru.messenger.JsonTransform;
import ru.messenger.client.Client;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {

    private static final Logger logger = Logger.getLogger(Server.class.getName());
    private final List<ServerConnector> serverConnectorList =
            Collections.synchronizedList(new ArrayList<ServerConnector>());
    private final List<String[]> clientData =
            Collections.synchronizedList(new ArrayList<String[]>());
    private ServerSocket serverSocket;

    public Server() {
        try {
            CustomLogger.getServerLogCustoms(logger);
            serverSocket = new ServerSocket(JsonTransform.getPORT());

            while (true) {
                logger.log(Level.INFO, "Server start successful");
                Socket socket = serverSocket.accept();
                logger.log(Level.INFO, "Request -> Response");
                ServerConnector serverConnector = new ServerConnector(socket);
                serverConnectorList.add(serverConnector);
                serverConnector.start();
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error on server", e);
        } finally {
            closeServer();
            logger.log(Level.INFO, "Server closed successful");
        }
    }

    private void closeServer() {
        try {
            synchronized (clientData) {
                for (String[] strings : clientData) {
                    JsonTransform.putClientDataToJson(strings);
                }
            }
            serverSocket.close();
            synchronized (serverConnectorList) {
                for (ServerConnector aServerConnectorList : serverConnectorList) {
                    (aServerConnectorList).closeClientCanal();
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "!Thread not be aborted!", e);
        }
    }


    private class ServerConnector extends Thread {

        private Socket socket;
        private BufferedReader bufferedReader;
        private PrintWriter printWriter;
        private String userName = "";
        private String currentDate;
        private String ip;

        ServerConnector(Socket socket) throws IOException {
            this.socket = socket;
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            printWriter = new PrintWriter(socket.getOutputStream(), true);
        }

        @Override
        public void run() {
            try {
                userName = bufferedReader.readLine();
                ip = bufferedReader.readLine();
                currentDate = bufferedReader.readLine();
                clientData.add(new String[]{userName,ip,currentDate});
                logger.log(Level.INFO, userName + " come now");
                synchronized (serverConnectorList) {
                    for (ServerConnector aServerConnectorList : serverConnectorList) {
                        (aServerConnectorList).printWriter.println(userName + " : has connect");
                    }
                }

                String line;
                while (true) {
                    line = bufferedReader.readLine();
                    if (line.matches(".*exit.*")) {
                        break;
                    }
                    synchronized (serverConnectorList) {
                        for (ServerConnector aServerConnectorList : serverConnectorList) {
                            (aServerConnectorList).printWriter.println(userName + " : " + line);
                        }
                    }
                }

                synchronized (serverConnectorList) {
                    for (ServerConnector aServerConnectorList : serverConnectorList) {
                        (aServerConnectorList).printWriter.println(userName + " : has left us");
                    }
                }

                if (serverConnectorList.size() == 2) {
                    closeServer();
                }

            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error on server", e);
            } finally {
                closeClientCanal();
                logger.log(Level.INFO, "Client disconnect successful");
            }

        }

        void closeClientCanal() {
            try {
                bufferedReader.close();
                printWriter.close();
                socket.close();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error on server", e);
            }
        }
    }

}
