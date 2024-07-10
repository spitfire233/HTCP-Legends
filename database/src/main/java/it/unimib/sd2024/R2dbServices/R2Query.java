package it.unimib.sd2024.R2dbServices;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;

import it.unimib.sd2024.exceptions.*;

public class R2Query {
    private final R2db db;
    private static R2Query r2q = null;
    private static Jsonb jsonb = JsonbBuilder.create();

    private R2Query() {
        this.db = R2db.getInstance();
    }

    public synchronized static R2Query getInstance() {
        if (r2q == null)
            r2q = new R2Query();
        return r2q;
    }

    public String query(String query)
            throws R2dbFileNotFoundException, TableIsEmptyException, R2dbErrorException,
            TableAlreadyExistsException, UndefinedKeyException, JsonBException, FileErrorException {
        Object response = null;
        String[] querySplit = query.split(" ");
        
        // Lowercase all the query
        for(int i = 0; i < querySplit.length; i++) {
            querySplit[i] = querySplit[i].toLowerCase();
        }
        String command = querySplit[0];

        // Capitalize the first letter of the table name
        String tmp = querySplit[1];
        querySplit[1] = tmp.substring(0, 1).toUpperCase() + tmp.substring(1);

        // Switch statement to handle different query commands
        switch (command) {
            case "create":
                try {
                    db.create_tab(querySplit[1]);
                } catch (TableAlreadyExistsException e) {
                    throw new TableAlreadyExistsException();
                } catch (FileErrorException e) {
                    throw new FileErrorException();
                } catch (Exception e) {
                    throw new R2dbErrorException();
                } finally {
                    response = "OK!";
                }
                break;
            case "create_key":
                try {
                    db.create(querySplit[1], querySplit[2], querySplit[3]);
                } catch (UndefinedKeyException e) {
                    throw new UndefinedKeyException();
                } catch (R2dbFileNotFoundException e) {
                    throw new R2dbFileNotFoundException();
                } catch (JsonBException e) {
                    throw new JsonBException();
                } catch (Exception e) {
                    throw new R2dbErrorException();
                } finally {
                    response = "OK!";
                }
                break;
            case "select":
                if (querySplit.length == 3)
                    try {
                        response = db.select(querySplit[1], querySplit[2]);
                    } catch (FileErrorException e) {
                        throw new FileErrorException();
                    } catch (R2dbFileNotFoundException e) {
                        throw new R2dbFileNotFoundException();
                    } catch (JsonBException e) {
                        throw new JsonBException();
                    } catch (UndefinedKeyException e) {
                        throw new UndefinedKeyException();
                    } catch (Exception e) {
                        throw new R2dbErrorException();
                    }
                else
                    try {
                        response = db.select(querySplit[1], querySplit[2], querySplit[4], querySplit[5], querySplit[6]);
                    } catch (FileErrorException e) {
                        throw new FileErrorException();
                    } catch (R2dbFileNotFoundException e) {
                        throw new R2dbFileNotFoundException();
                    } catch (JsonBException e) {
                        throw new JsonBException();
                    } catch (UndefinedKeyException e) {
                        throw new UndefinedKeyException();
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new R2dbErrorException();
                    }
                break;
            case "modify":
                try {
                    db.modify(querySplit[1], querySplit[2], querySplit[3], querySplit[4]);
                } catch (UndefinedKeyException e) {
                    throw new UndefinedKeyException();
                } catch (FileErrorException e) {
                    throw new FileErrorException();
                } catch (TableIsEmptyException e) {
                    throw new TableIsEmptyException();
                } catch (JsonBException e) {
                    throw new JsonBException();
                } catch (Exception e) {
                    throw new R2dbErrorException();
                } finally {
                    response = "OK!";
                }
                break;
            case "delete":
                try {
                    db.delete_tab(querySplit[1]);
                } catch (UndefinedKeyException e) {
                    throw new UndefinedKeyException();
                } catch (FileErrorException e) {
                    throw new FileErrorException();
                } catch (JsonBException e) {
                    throw new JsonBException();
                } catch (Exception e) {
                    throw new R2dbErrorException();
                } finally {
                    response = "OK!";
                }
                break;
            case "delete_key":
                try {
                    db.delete(querySplit[1], querySplit[2]);
                } catch (UndefinedKeyException e) {
                    throw new UndefinedKeyException();
                } catch (FileErrorException e) {
                    throw new FileErrorException();
                } catch (TableIsEmptyException e) {
                    throw new TableIsEmptyException();
                } catch (JsonBException e) {
                    throw new JsonBException();
                } catch (Exception e) {
                    throw new R2dbErrorException();
                } finally {
                    response = "OK!";
                }
                break;
            case "get_last_index":
                try {
                    response = db.LastIndex(querySplit[1]);
                } catch (UndefinedKeyException e) {
                    throw new UndefinedKeyException();
                } catch (R2dbFileNotFoundException e) {
                    throw new R2dbFileNotFoundException();
                } catch (JsonBException e) {
                    throw new JsonBException();
                } catch (TableIsEmptyException e) {
                    throw new TableIsEmptyException();
                } catch (Exception e) {
                    throw new R2dbErrorException();
                }
                break;
            default:
                throw new R2dbErrorException();
        }
        
        // Return the response in JSON format
        return jsonb.toJson(response);
    }
}
