package it.unimib.sd2024.communicationServices;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import it.unimib.sd2024.R2dbServices.R2Query;
import it.unimib.sd2024.exceptions.*;

/**
 * Handler di una connessione del client.
 */
public class Handler extends Thread {
    private Socket client;
    private final BufferedReader input;
    private final PrintWriter output;
    private final R2Query r2q;

    /**
     * Constructor to initialize the handler with client socket and setup input/output streams.
     * @param client The socket representing the client connection.
     * @throws IOException If there is an error setting up input or output streams.
     */
    public Handler(Socket client) throws IOException {
        this.client = client;
        this.input = new BufferedReader(new InputStreamReader(this.client.getInputStream()));
        this.output = new PrintWriter(this.client.getOutputStream(), true);
        this.r2q = R2Query.getInstance();
    }

    public void run() {
        try {

            // Retrieve script sent by client
            String script = getScript();

            System.out.println("Processing request");

            // Process the script and get response
            String response = handleEvaluation(script);

             // Send response back to client
            sendResponseBatch(response);

            System.out.println("Client served"+ Thread.currentThread().getName());
        } catch (IOException e) {
            System.out.println("Socket error: "+ e.getMessage());
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                System.out.println("Failed to close client socket: "+ e.getMessage());
            }
        }
    }



    /**
     * Method to read the script sent by the client.
     * @return The complete script as a String.
     * @throws IOException If there is an error reading the script.
     */
    private String getScript() throws IOException {
        StringBuilder script = new StringBuilder();
        String line;
        while (!(line = input.readLine()).equals("END")) {
            script.append(line);
        }
        return script.toString();
    }


    /**
     * Method to handle evaluation of the script using R2Query.
     * @param request The script to be evaluated.
     * @return The response generated from evaluating the script.
     */
    private String handleEvaluation(String request)  {
        StringBuilder response = new StringBuilder();
        try {
            System.out.println("Request: "+request);

            // Execute query using R2Query
            String jsonResponse = this.r2q.query(request);
            // Clean the JSON
            jsonResponse = jsonResponse.substring(1, jsonResponse.length() - 1).replaceAll("\\\\", "");
            response.append(jsonResponse);

        }catch (R2dbFileNotFoundException e) {
            response.append("TABLE_NOT_FOUND");
        } catch (TableIsEmptyException e) {
            response.append("EMPTY");
        } catch (UndefinedKeyException e) {
            response.append("KEY_NOT_FOUND");
        }catch (FileErrorException | JsonBException | R2dbErrorException e) {
            response.append("ERROR");
        } catch (TableAlreadyExistsException e) {
            response.append("ALREADY_EXISTS");
        }
        
        // Return final response as String
        return response.toString();
    }

    /**
     * Invia la risposta al client.
     * @param response La risposta da inviare
     */
    private void sendResponseBatch(String response) {
        this.output.write(response);
        this.output.flush();
    }
}