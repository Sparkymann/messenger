package ru.messenger.client;


import ru.messenger.CustomLogger;
import ru.messenger.JsonTransform;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Client implements Serializable {
    private transient BufferedReader bufferedReader;
    private transient PrintWriter printWriter;
    private transient Socket socket;
    private transient static Logger logger = Logger.getLogger(Client.class.getName());
    private static String userName;
    private static String currentDate;
    private static String ip;
    private transient Scanner scanner = new Scanner(System.in);

    public Client() {

        try {
            CustomLogger.getClientLogCustoms(logger);

            logger.log(Level.INFO, "Connecting...");

            socket = new Socket(JsonTransform.getLocalIp(), JsonTransform.getPORT());
            logger.log(Level.INFO, "Connecting successful");

            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            printWriter = new PrintWriter(socket.getOutputStream(), true);

            currentDate = new SimpleDateFormat("yyyy.MM.dd : kk.mm.ss").format(new Date());
            ip = InetAddress.getLocalHost().getHostAddress();
            userName = scanner.nextLine();

            printWriter.println(userName);
            printWriter.println(ip);
            printWriter.println(currentDate);

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error on Client", e);
        }
    }

    public void createConnection() {

        ClientMessenger clientMessenger = new ClientMessenger();
        clientMessenger.start();

        String line = "";
        while (!line.matches(".*exit.*")) {
            line = scanner.nextLine();
            printWriter.println(line);
        }
        clientMessenger.setStop();
        close();
    }

    private void close() {
        try {
            bufferedReader.close();
            printWriter.close();
            socket.close();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "!Threads not be aborted", e);
        }
    }

    private class ClientMessenger extends Thread {

        private boolean stop;


        void setStop() {
            this.stop = true;
        }

        @Override
        public void run() {
            try {
                while (!stop) {
                    String line = bufferedReader.readLine();
                    System.out.println(line);
                }
            } catch (IOException e) {
                logger.log(Level.SEVERE, "!Threads not be aborted", e);
            }
        }
    }

}
