package it.unimib.sd2024;

import java.net.*;

import it.unimib.sd2024.R2dbServices.R2SetUp;
import it.unimib.sd2024.communicationServices.Handler;

import java.io.*;

/**
 * Classe principale in cui parte il database.
 */
public class Main {
    /**
     * Porta di ascolto.
     */
    public static final int PORT = 3030;

    /**
     * Avvia il database e l'ascolto di nuove connessioni.
     */
    public static void startServer() throws IOException {
        ServerSocket server = new ServerSocket(PORT);

        System.out.println("Database listening at localhost Port : " + PORT);

        try {
            while (true) {
                new Handler(server.accept()).start();
            }
        } catch (IOException e) {
            System.err.println(e);
        } finally {
            server.close();
        }
    }


    /**
     * Metodo principale di avvio del database.
     *
     * @param args argomenti passati a riga di comando.
     *
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        R2SetUp.setUpR2db(args);
        startServer();
    }
}

