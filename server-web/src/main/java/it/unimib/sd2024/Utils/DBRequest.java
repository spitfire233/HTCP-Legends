// DBRequest.java
/**
 * This class handles the sending of queries to the database R2DB and the receiving of their results
 */

package it.unimib.sd2024.Utils;

import java.io.IOException;

public class DBRequest {
    // Class attributes
    private static final String address = "localhost";
    private static final int port = 3030;
    Connection dbConnection;

    public static enum ConfrontOperator {
        E, // Equals
        B, // Bigger
        S, // Smaller
        BE, // Bigger or equal
        LE // Less or equal
    }
    // END -- Class attributes

    // Class methods:

    /**
     * Creates a new table in the database
     * @param tab_name The table name
     * @return The response of the database:
     * <ul>
     *      <li>OK!: Query executed</li>
     *      <li>ALREADY_EXISTS: The table already exists</li>
     *      <li>ERROR: A generic error has occurred</li>
     * </ul>
     * @throws IOException If a connection error with the database occurs
     */
    public String create(String tab_name) throws IOException {
        // Connect to the database:
        dbConnection = new Connection(address, port);

        // Send request:
        dbConnection.send("CREATE " + tab_name);

        // Obtain response:
        String response = dbConnection.read();
        dbConnection.close();
        return response;
    }


    /**
     * Creates a key in the specified table
     * @param tab The name of the table
     * @param key The key with which identify the value
     * @param value The value of the key. IT MUST BE WELL-FORMED JSON
     * @return The response of the database:
     * <ul>
     *      <li>OK!: Query executed</li>
     *      <li>ALREADY_EXISTS: The key already exists</li>
     *      <li>ERROR: A generic error has occurred</li>
     * </ul>
     * @throws IOException If a connection error with the database occurs
     */
    public String create_key(String tab, String key, String value) throws IOException {
        // Connect to the database:
        dbConnection = new Connection(address, port);

        // Send request:
        dbConnection.send("CREATE_KEY " + tab + " " + key + " " + value);

        // Obtain response:
        String response = dbConnection.read();
        dbConnection.close();
        return response;
    }

    /**
     * Returns the specified key in JSON String format
     * @param tab The table from which extract the key
     * @param key The key to extract
     * @return The response of the database:
     * <ul>
     *      <li>The JSON representation of the requested key (in String format)</li>
     *      <li>TABLE_NOT_FOUND: The table wasn't found in the database </li>
     *      <li>KEY_NOT_FOUND: The key wasn't found in the table </li>
     * </ul>
     * @throws IOException If a connection error with the database occurs
     */

    public String select(String tab, String key) throws IOException {
        // Connect to the database:
        dbConnection = new Connection(address, port);

        // Send request:
        dbConnection.send("SELECT " + tab + " " + key);
        // Obtain response:
        String response = dbConnection.read();
        dbConnection.close();
        return response;
    }

    /**
     * Returns the specified key in JSON String format
     * @param tab The table from which extract the key
     * @param key The key to extract
     * @param param The parameter of the key to use for the confront
     * @param paramValue The value to confront param with
     * @param confrontOperator The confront operator to use
     * <ul>
     *      <li>E: Equal (=)</li>
     *      <li>B: Bigger (>)</li>
     *      <li>S: Smaller (<)</li>
     *      <li>BE: Bigger or equal (>=)</li>
     *      <li>LE: Less or equal (<=)</li>
     * </ul>
     * @return The response of the database:
     * <ul>
     *      <li>The JSON representation of the requested key (in String format)</li>
     *      <li>TABLE_NOT_FOUND: The table wasn't found in the database </li>
     *      <li>KEY_NOT_FOUND: The key wasn't found in the table </li>
     *      <li>PARAM_NOT_FOUND The parameter wasn't found in the key</li>
     * </ul>
     * @throws IOException If a connection error with the database occurs
     */

    public String select(String tab, String key, String param, String paramValue, ConfrontOperator confrontOperator) throws IOException {
        // Connect to the database:
        dbConnection = new Connection(address, port);

        // Send request:
        dbConnection.send("SELECT " + tab + " " + key + " WHERE " + param + " " + confrontOperator.toString().toLowerCase() + " " + paramValue);

        // Obtain response:
        String response = dbConnection.read();

        dbConnection.close();
        return response;
    }

    /**
     * Modifies a parameter of the specified key
     * @param tab The table in which the key resides
     * @param key The key to modify
     * @param param The parameter to modify
     * @param newValue The new value of the parameter
     * @return The response of the database:
     * <ul>
     *      <li>OK!: The key was modified successfully</li>
     *      <li>TABLE_NOT_FOUND: The table wasn't found in the database </li>
     *      <li>KEY_NOT_FOUND: The key wasn't found in the table </li>
     *      <li>PARAM_NOT_FOUND The parameter wasn't found in the key</li>
     * </ul>
     * @throws IOException If a connection error with the database occurs
     */
    public String modify(String tab, String key, String param, String newValue) throws IOException {
         // Connect to the database:
         dbConnection = new Connection(address, port);

         // Send request:
         dbConnection.send("MODIFY " + tab + " " + key + " " + param + " " + newValue);
         // Obtain response:
         String response = dbConnection.read();
         dbConnection.close();
         return response;
    }

    /**
     * Deletes a table from the database
     * @param tab The table to delete
     * @return The response of the database:
     * <ul>
     *      <li>OK!: The key was modified successfully</li>
     *      <li>TABLE_NOT_FOUND: The table wasn't found in the database </li>
     *      <li>ERROR: A generic error has occurred </li>
     * </ul>
     * @throws IOException If a connection error with the database occurs
     */

    public String delete(String tab) throws IOException {
         // Connect to the database:
         dbConnection = new Connection(address, port);

         // Send request:
         dbConnection.send("DELETE " + tab);
         // Obtain response:
         String response = dbConnection.read();
         dbConnection.close();
         return response;
    }

    /**
     * Deletes a key from the database
     * @param tab The table from which to delete key
     * @param key The key to delete
     * @return The response of the database:
     * <ul>
     *      <li>OK!: The key was modified successfully</li>
     *      <li>TABLE_NOT_FOUND: The table wasn't found in the database </li>
     *      <li>KEY_NOT_FOUND: The key wasn't found in the table</li>
     *      <li>ERROR: A generic error has occurred </li>
     * </ul>
     * @throws IOException
     */
    public String delete_key(String tab, String key) throws IOException {
        // Connect to the database:
        dbConnection = new Connection(address, port);

        // Send request:
        dbConnection.send("DELETE_KEY " + tab + " " + key);
        // Obtain response:
        String response = dbConnection.read();
        dbConnection.close();
        return response;
    }


    public String get_last_index(String tab) throws IOException {
        // Connect to the database:
        dbConnection = new Connection(address, port);

        // Send request:
        dbConnection.send("GET_LAST_INDEX " + tab);

        // Obtain response:
        String response = dbConnection.read();
        dbConnection.close();
        return response;
    }

}
// EOF -- DBRequest.java
