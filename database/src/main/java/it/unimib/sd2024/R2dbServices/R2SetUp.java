package it.unimib.sd2024.R2dbServices;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbException;

import it.unimib.sd2024.exceptions.*;

/**
 * SetUpR2
 */
public class R2SetUp {

    // Singleton instance of R2db
    private static final R2db db = R2db.getInstance();

    public static void setUpR2db(String[] josns) {
        System.out.println("\nInitiated database setup...\n");

        File folder = new File("./dbJsons/");

        // Check if the directory exists and is valid
        if (!folder.exists() || !folder.isDirectory()) {
            System.out.println("The specified folder does not exist or is not a directory.");
            return;
        }

        // List all files in the directory
        File[] listOfFiles = folder.listFiles();

        // List all files in the directory
        if (listOfFiles != null) {

            // Process each file individually
            for (File file : listOfFiles) {
                if (file.isFile() && file.getName().endsWith(".json")){
                    
                    // Process the file
                    processFile(file);
                } else if (file.isDirectory()) {
                    System.out.println("Directory: " + file.getName());
                }
            }
        }

        // Process additional JSON files specified in the arguments
        for (String jsonFileName : josns) {
            String path = "./dbJsons/" + jsonFileName;
            // Process the specified file
            processFile(new File(path));
        }

        System.out.println("End of database setUp\n");
    }

    // Method to process a single file
    private static void processFile(File file) {
        String path = file.getPath();
        try {

            // Fetch JSON data from the file and set up the database
            setup(fetchJsonFromFile(path), file.getName());

        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + path);
        } catch (JsonbException e) {
            System.out.println("Error in json parsing: " + path);
        } catch (FileErrorException e) {
            System.out.println("File already exists: " + path);
        } catch (IOException e) {
            System.out.println("Error in file reading: " + path);
        } catch (TableAlreadyExistsException e) {
            System.out.println("Table already exists: " + path);
        }
    }

    // Method to read JSON data from a file
    public static Map<String, Object> fetchJsonFromFile(String jsonPath)
            throws JsonbException, FileNotFoundException, FileErrorException {

        File jsonFile = new File(jsonPath);
        if (jsonFile.length() == 0) {
            return null;
        }

        // Try to read the file and deserialize JSON data into a map
        try (FileReader reader = new FileReader(jsonFile)) {
            Jsonb jsonb = JsonbBuilder.create();

            // Deserialize JSON data into a Map
            return jsonb.fromJson(reader, new HashMap<String, Object>() {
            }.getClass().getGenericSuperclass());

        } catch (IOException e) {
            throw new FileErrorException();
        }
    }

    // Sets up the database table with the provided JSON data
    public static void setup(Map<String, Object> json, String name_tab)
            throws JsonbException, FileNotFoundException, IOException, FileErrorException, TableAlreadyExistsException {

        // Convert the file name to a table name
        String nomeTabella = convertToTableName(name_tab);

        // Create the database table
        db.create_tab(nomeTabella);
        if (json != null) {
            for (Map.Entry<String, Object> entry : json.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                
                // Insert data into the table
                db.create(nomeTabella, key, value);
            }
        }
    }

    // Converts a file name to a database table name
    private static String convertToTableName(String nameTab) {
        String tmp = nameTab.substring(0, nameTab.lastIndexOf(".")).toLowerCase();
        return tmp.substring(0, 1).toUpperCase() + tmp.substring(1);
    }
}