/**
 * Connection.java
 * This class handles a generic connection to a server.
 */

package it.unimib.sd2024.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Connection {
    // Class attributes
    private Socket connection;
    private PrintWriter inputStream;
    private BufferedReader outputStream;
    // END -- Class attributes

    // Class methods

    /**
     * Constructor for the class. Creates a new connection to address:port during the instantiation of the object
     * @param address The address of the server
     * @param port The port where the server is listening for connections
     * @throws IOException When there's a connection error with the server
     */
    public Connection(String address, int port) throws IOException {
        connection = new Socket(address, port);
        inputStream = new PrintWriter(connection.getOutputStream());
        outputStream = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        System.out.println("Connection successfully established on port: " + connection.getPort());
    }


    /**
     * Sends a message to the server
     * @param message The message to send
     */
    public void send(String message) {
        inputStream.println(message);
        inputStream.println("END");
        inputStream.flush();
        System.out.println("DBRequest: request sent!");
    }

    /**
     * Reads a message from the server.
     * @return The message red
     * @throws IOException When there's a connection problem with the server
     */
    public String read() throws IOException {
        return outputStream.readLine();
    }
    
    /**
     * Closes the connection to the server
     * @throws IOException
     */
    public void close() throws IOException {
        this.inputStream.close();
        this.outputStream.close();
        this.connection.close();
    }
    // END -- Class methods
}
// EOF -- Connection.java
