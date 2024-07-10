package it.unimib.sd2024.R2dbServices;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbException;

import it.unimib.sd2024.exceptions.*;

/**
 * Represents a JSON-based database service with file system persistence.
 */
public class R2db {
    private static R2db r2db = null;
    private Jsonb jsonb;
    private final Map<String, File> db;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * Private constructor initializes the database map and JSON binding utility.
     */
    private R2db() {
        this.db = new ConcurrentHashMap<>();
        jsonb = JsonbBuilder.create();
    }

    /**
     * Returns the singleton instance of R2db.
     * 
     * @return R2db instance
     */
    public synchronized static R2db getInstance() {
        if (r2db == null) {
            r2db = new R2db();
        }
        return r2db;
    }

    /**
     * Creates a new table in the database.
     * 
     * @param name_tab Name of the table to create
     * @throws FileErrorException          If file creation fails
     * @throws TableAlreadyExistsException If the table already exists
     */
    public void create_tab(String name_tab) throws FileErrorException, TableAlreadyExistsException {
        // Acquires write lock for thread safety
        lock.writeLock().lock();
        try {
            if (db.containsKey(name_tab)) {
                throw new TableAlreadyExistsException();
            }
            try {
                File file = new File("./dbJsons/" + name_tab + ".json");

                // Attempts to create the file
                file.createNewFile();

                // Adds table to the database map
                db.put(name_tab, file);
            } catch (Exception e) {
                throw new FileErrorException();
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Creates or updates a key-value pair in the specified table.
     * 
     * @param name_tab Name of the table
     * @param key      Key to store or update
     * @param value    Value associated with the key
     * @throws JsonBException            If JSON serialization or deserialization
     *                                   fails
     * @throws R2dbFileNotFoundException If file is not found during operations
     * @throws UncheckedIOException      If an IO error occurs
     */
    public void create(String name_tab, String key, Object value)
            throws JsonBException, R2dbFileNotFoundException, UncheckedIOException {

        // Acquires write lock for thread safety
        lock.writeLock().lock();
        try {
            if (!db.containsKey(name_tab)) {
                throw new UndefinedKeyException();
            }

            // Read JSON file and convert to a Map
            Map<String, Object> map = null;
            if (db.get(name_tab).length() == 0) {
                map = new HashMap<>();
            } else {
                try (BufferedReader reader = new BufferedReader(new FileReader("./dbJsons/" + name_tab + ".json"))) {
                    map = jsonb.fromJson(reader,
                            new HashMap<String, Object>() {
                            }.getClass().getGenericSuperclass());
                } catch (JsonbException e) {
                    throw new JsonBException();
                } catch (IOException e) {
                    throw new R2dbFileNotFoundException();
                }
            }

            // Handle JSON serialization based on value type
            if (value.getClass().equals(String.class)) {
                Map<String, Object> k = new HashMap<>();
                String jString = value.toString();
                k = jsonb.fromJson(jString, jString.getClass().getGenericSuperclass());

                // Adds key-value pair to map
                map.put(key, k);

            } else {

                // Adds key-value pair to map
                map.put(key, value);
            }
            String fileJson = jsonb.toJson(map);

            // Write modified map back to JSON file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("./dbJsons/" + name_tab + ".json"))) {
                writer.write(fileJson);
            } catch (IOException e) {
                throw new R2dbFileNotFoundException();
            }

            // Closes Jsonb resource
            try {
                jsonb.close();
            } catch (Exception e) {
                throw new JsonBException();
            }
        } finally {
            // Releases write lock
            lock.writeLock().unlock();
        }

    }

    /**
     * Retrieves data from the specified table based on a key.
     * 
     * @param name_tab Name of the table
     * @param key      Key to retrieve data for
     * @return JSON string representing the retrieved data
     * @throws R2dbFileNotFoundException If file is not found during operations
     * @throws JsonBException            If JSON serialization or deserialization
     *                                   fails
     * @throws FileErrorException        If an error occurs while accessing the file
     * @throws UndefinedKeyException     If the key is not found
     */
    public Object select(String name_tab, String key)
            throws R2dbFileNotFoundException, JsonBException, FileErrorException, UndefinedKeyException {

        // Acquires read lock for thread safety
        lock.readLock().lock();
        try {
            Map<String, Object> result = new HashMap<>();
            String jString = "";

            // Check if the database contains the specified table
            if (db.containsKey(name_tab)) {
                Map<String, Object> map = null;

                // Try to read from the JSON file corresponding to the table
                try (BufferedReader reader = new BufferedReader(new FileReader("./dbJsons/" + name_tab + ".json"))) {
                    if (reader.ready()) {

                        // Deserialize JSON content into a map
                        map = jsonb.fromJson(reader,
                                new HashMap<String, Object>() {
                                }.getClass().getGenericSuperclass());
                    }
                } catch (JsonbException e) {
                    throw new JsonBException();
                } catch (IOException e) {
                    throw new FileErrorException();
                }

                if (map != null) {
                    // If key is "*", retrieve all entries from the map
                    if (key.equals("*")) {
                        for (Entry<String, Object> entry : map.entrySet()) {
                            String k = entry.getKey();
                            result.put(k, entry.getValue());
                        }
                    } else {
                        // If key is specific, retrieve only that entry from the map
                        for (Entry<String, Object> entry : map.entrySet()) {
                            String k = entry.getKey();
                            if (k.equals(key)) {
                                result.put(k, entry.getValue());
                            }
                        }

                    }
                }
            } else {
                throw new R2dbFileNotFoundException();
            }

            if (result.toString().equals("{}")) {
                throw new UndefinedKeyException();
            }

            // Serialize the result map to JSON string
            try {
                jString = jsonb.toJson(result);
            } catch (JsonbException e) {
                throw new JsonBException();
            }
            return jString;

        } finally {
            // Release the read lock
            lock.readLock().unlock();
        }
    }

    /**
     * Retrieves data from the specified table based on key, parameter, operation,
     * and value.
     * 
     * @param name_tab Name of the table
     * @param key      Key to retrieve data for
     * @param param    Parameter to filter by
     * @param op       Operation (e.g., "e" for equals, "b" for greater than, etc.)
     * @param value    Value to compare with
     * @return JSON string representing the retrieved data
     * @throws R2dbFileNotFoundException If file is not found during operations
     * @throws JsonBException            If JSON serialization or deserialization
     *                                   fails
     * @throws FileErrorException        If an error occurs while accessing the file
     * @throws UndefinedKeyException     If the key is not found
     */
    public Object select(String name_tab, String key, String param, String op, String value)
            throws R2dbFileNotFoundException, JsonBException, FileErrorException, UndefinedKeyException {
        // Acquires read lock for thread safety
        lock.readLock().lock();
        try {
            Map<String, Object> result = new HashMap<>();

            // Checks if the specified table exists in the database
            if (db.containsKey(name_tab)) {
                Map<String, Object> map = null;

                // Attempts to read and parse JSON data from the corresponding file
                try (BufferedReader reader = new BufferedReader(new FileReader("./dbJsons/" + name_tab + ".json"))) {
                    if (reader.ready()) {

                        // Parses JSON data into a map
                        map = jsonb.fromJson(reader,
                                new HashMap<String, Object>() {
                                }.getClass().getGenericSuperclass());
                    }
                } catch (JsonbException e) {
                    throw new JsonBException();
                } catch (IOException e) {
                    throw new FileErrorException();
                }
                if (map != null) {
                    if (key.equals("*")) {

                        // Iterates over entries in the map if key is "*"

                        for (Entry<String, Object> entry : map.entrySet()) {

                            String k = entry.getKey();
                            Map<String, Object> iterable = (Map<String, Object>) entry.getValue();
                            for (Entry iterable_element : iterable.entrySet()) {
                                if (iterable_element.getKey().equals(param)) {
                                    if (op.equals("e") && iterable_element.getValue().equals(value)) {
                                        result.put(k, entry.getValue());
                                    } else if (op.equals("b")
                                            && value.compareTo(iterable_element.getValue().toString()) < 0) {
                                        result.put(k, entry.getValue());
                                    } else if (op.equals("l")
                                            && value.compareTo(iterable_element.getValue().toString()) > 0) {
                                        result.put(k, entry.getValue());
                                    } else if (op.equals("be")
                                            && value.compareTo(iterable_element.getValue().toString()) <= 0) {
                                        result.put(k, entry.getValue());
                                    } else if (op.equals("le")
                                            && value.compareTo(iterable_element.getValue().toString()) >= 0) {
                                        result.put(k, entry.getValue());
                                    }

                                }
                            }

                        }
                    } else {
                        // Processes specific key entries
                        for (Entry<String, Object> entry : map.entrySet()) {
                            String k = entry.getKey();

                            if (k.equals(key)) {

                                Map<String, Object> iterable = (Map<String, Object>) entry.getValue();
                                for (Entry iterable_element : iterable.entrySet()) {
                                    if (iterable_element.getKey().equals(param)) {
                                        if (op.equals("e") && iterable_element.getValue().equals(value)) {
                                            result.put(k, entry.getValue());
                                        } else if (op.equals("b")
                                                && value.compareTo(iterable_element.getValue().toString()) < 0) {
                                            result.put(k, entry.getValue());
                                        } else if (op.equals("l")
                                                && value.compareTo(iterable_element.getValue().toString()) > 0) {
                                            result.put(k, entry.getValue());
                                        } else if (op.equals("be")
                                                && value.compareTo(iterable_element.getValue().toString()) <= 0) {
                                            result.put(k, entry.getValue());
                                        } else if (op.equals("le")
                                                && value.compareTo(iterable_element.getValue().toString()) >= 0) {
                                            result.put(k, entry.getValue());
                                        }
                                    }
                                }
                            }

                        }
                    }
                }
            } else {
                throw new UndefinedKeyException();
            }
            String jString = "";

            // Converts result map to JSON string
            try {
                jString = jsonb.toJson(result);
            } catch (JsonbException e) {
                throw new JsonBException();
            }
            return jString;
        } finally {
            // Releases read lock
            lock.readLock().unlock();
        }

    }

    /**
     * Modifies an existing key-value pair in the specified table.
     * 
     * @param name_tab Name of the table
     * @param key      Key to modify
     * @param value    New value to set
     * @throws JsonBException            If JSON serialization or deserialization
     *                                   fails
     * @throws R2dbFileNotFoundException If file is not found during operations
     * @throws FileErrorException        If an error occurs while accessing the file
     * @throws UndefinedKeyException     If the key is not found
     */
    public void modify(String name_tab, String key, String param, Object new_value)
            throws UndefinedKeyException, FileErrorException, TableIsEmptyException, JsonBException {
        lock.writeLock().lock();
        try {
            if (db.containsKey(name_tab)) {

                // Lettura del file JSON e conversione in una Map
                Map<String, Object> map = null;
                try (BufferedReader reader = new BufferedReader(new FileReader("./dbJsons/" + name_tab + ".json"))) {
                    map = jsonb.fromJson(reader,
                            new HashMap<String, Object>() {
                            }.getClass().getGenericSuperclass());
                } catch (JsonbException e) {
                    throw new JsonBException();
                } catch (IOException e) {
                    throw new FileErrorException();
                }

                if (map != null) {
                    if (new_value.getClass().equals(String.class)) {
                        // If the new_value is of type String, update a specific nested parameter in the
                        // map
                        // Clean the input:
                        String new_value_str = (String)new_value;
                        new_value_str = new_value_str.replaceAll("\"", "");
                        for (Entry<String, Object> entry : map.entrySet()) {
                            if (entry.getKey().equals(key)) {

                                Map<String, Object> itemEntry = (Map<String, Object>) entry.getValue();
                                for (Entry<String, Object> item : itemEntry.entrySet()) {
                                    if (item.getKey().equals(param)) {
                                        itemEntry.put(param, new_value_str);
                                        map.put(key, itemEntry);
                                    }
                                }
                            }
                        }
                    } else {
                        map.put(key, new_value);
                    }

                    // Convert the updated map back to JSON format
                    String fileJson = "";
                    try {
                        fileJson = jsonb.toJson(map);
                    } catch (JsonbException e) {
                        throw new JsonBException();
                    }

                    // Write the modified map to the JSON file
                    try (BufferedWriter writer = new BufferedWriter(
                            new FileWriter("./dbJsons/" + name_tab + ".json"))) {
                        writer.write(fileJson);
                        writer.close();
                    } catch (IOException e) {
                        throw new FileErrorException();
                    }

                } else {
                    throw new TableIsEmptyException();
                }
            } else {
                throw new UndefinedKeyException();
            }
        } finally {
            // Releases the write lock
            lock.writeLock().unlock();
        }

    }

    /**
     * Deletes a key-value pair from the specified table.
     * 
     * @param name_tab Name of the table
     * @param key      Key to delete
     * @throws R2dbFileNotFoundException If file is not found during operations
     * @throws FileErrorException        If an error occurs while accessing the file
     * @throws UndefinedKeyException     If the key is not found
     */
    public void delete(String name_tab, String key)
            throws UndefinedKeyException, FileErrorException, TableIsEmptyException, JsonBException {
        lock.writeLock().lock();
        try {
            if (!db.containsKey(name_tab)) {
                throw new UndefinedKeyException();
            }
            // Lettura del file JSON e conversione in una Map
            Map<String, Object> map = null;

            // Read the JSON file and convert it to a Map
            try (BufferedReader reader = new BufferedReader(new FileReader("./dbJsons/" + name_tab + ".json"))) {
                map = jsonb.fromJson(reader,
                        new HashMap<String, Object>() {
                        }.getClass().getGenericSuperclass());
            } catch (JsonbException e) {
                throw new JsonBException();
            } catch (IOException e) {
                throw new R2dbFileNotFoundException();
            }

            // Check if the map is not empty
            if (map != null) {

                // Remove the specified key from the map
                map.remove(key);
                String fileJson = "";
                try {
                    fileJson = jsonb.toJson(map);
                } catch (JsonbException e) {
                    throw new JsonBException();
                }

                // Write the modified map back to the JSON file
                try (BufferedWriter writer = new BufferedWriter(new FileWriter("./dbJsons/" + name_tab + ".json"))) {
                    writer.write(fileJson);
                } catch (IOException e) {
                    throw new R2dbFileNotFoundException();
                }

            } else {
                throw new TableIsEmptyException();
            }
        } finally {
            // releases the write lock
            lock.writeLock().unlock();
        }

    }

    /**
     * Deletes the specified table and its associated JSON file.
     * 
     * @param name_tab Name of the table to delete
     * @throws R2dbFileNotFoundException If file is not found during operations
     * @throws FileErrorException        If an error occurs while accessing the file
     */
    public void delete_tab(String name_tab) throws UndefinedKeyException, FileErrorException {
        lock.writeLock().lock();
        try {
            // Checks if the database contains the specified table name
            if (!db.containsKey(name_tab)) {
                throw new UndefinedKeyException();
            }
            try {
                // Deletes the table file and removes it from the database map
                db.get(name_tab).delete();
                db.remove(name_tab);
            } catch (Exception e) {
                throw new R2dbFileNotFoundException();
            }
        } finally {
            // Releases the write lock
            lock.writeLock().unlock();
        }
    }

    /**
     * Retrieves the last index (maximum key) from the specified table.
     * 
     * @param name_tab Name of the table
     * @return Last index (maximum key) as a JSON string
     * @throws R2dbFileNotFoundException If file is not found during operations
     * @throws FileErrorException        If an error occurs while accessing the file
     * @throws UndefinedKeyException     If no key is found
     */
    public Object LastIndex(String name_tab)
            throws R2dbFileNotFoundException, TableIsEmptyException, UndefinedKeyException, JsonBException {

        // Acquires a read lock
        lock.readLock().lock();
        try {

             // Checks if the database contains the specified table name
            if (!db.containsKey(name_tab)) {
                throw new UndefinedKeyException();
            }
            // Checks if the table is not empty
            if (!(db.get(name_tab).length() == 0)) {
                Map<String, Object> map = null;

                // Reads the JSON file associated with the table and converts it into a Map
                try (BufferedReader reader = new BufferedReader(new FileReader("./dbJsons/" + name_tab + ".json"))) {
                    map = jsonb.fromJson(reader,
                            new HashMap<String, Object>() {
                            }.getClass().getGenericSuperclass());

                } catch (JsonbException e) {
                    throw new JsonBException();
                } catch (IOException e) {
                    throw new R2dbFileNotFoundException();
                }

                // Finds the maximum key (last index) in the map
                String max = "";
                for (Entry<String, Object> entry : map.entrySet()) {
                    String k = entry.getKey();
                    if (k.compareTo(max) > 0)
                        max = k;
                }

                // Returns the last index (maximum key) as a JSON string
                return max;
            } else {
                throw new TableIsEmptyException();
            }
        } finally {
            // Releases the read lock
            lock.readLock().unlock();
        }
    }
}