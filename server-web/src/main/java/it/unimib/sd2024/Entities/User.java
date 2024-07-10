// User.java
/**
 * This class represents a "User"
 * This class DOES NOT handle the persistence of the users in the system
 */
package it.unimib.sd2024.Entities;

public class User {
    // Class attributes
    private String email;
    private String id;
    private String name;
    private String surname;
    // END -- Class attributes

    // Getters
    /**
     * Returns the email of a user
     * @return the user's email
     */
    public String getEmail() {
        return this.email;
    }
    
    /**
     * Returns the unique identifier of the user
     * @return the user's ID
     */
    public String getId() {
        return this.id;
    }

    /**
     * Returns the user's name
     * @return the user's name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the user's surname
     * @return the user's surname
     */
    public String getSurname() {
        return this.surname;
    }

    // END -- Getters

    // Setters

    /**
     * Sets the user's email
     * @param email 
     * @throws IllegalArgumentException When the string passed is null
     */
    public void setEmail(String email) throws IllegalArgumentException {
        if(email == null)
            throw new IllegalArgumentException("Email is null!");
        this.email = email;
    }

    /**
     * Sets the user's ID
     * @param id 
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Sets user's name
     * @param name 
     * @throws IllegalArgumentException When the string passed is null
     */
    public void setName(String name) throws IllegalArgumentException {
        if(name == null)
            throw new IllegalArgumentException("Name is null!");
        this.name = name;
    }

    /**
     * Sets the user's surname
     * @param surname 
     * @throws IllegalArgumentException when the string passed is null
     */
    public void setSurname(String surname) throws IllegalArgumentException {
        if(surname == null)
            throw new IllegalArgumentException("Surname is null!");
        this.surname = surname;
    }
}
// EOF -- User.java