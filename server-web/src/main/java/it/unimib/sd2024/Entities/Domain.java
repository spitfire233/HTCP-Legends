// Domain.java
/**
 * This class represent a "domain"
 * This class DOES NOT handle the persistance of the domain in the system.
 */
package it.unimib.sd2024.Entities;
import java.time.LocalDate;
import jakarta.json.bind.annotation.JsonbProperty;

public class Domain {
    // Class attributes
    private String name;
    private String owner;
    private LocalDate registrationDate;
    private LocalDate expireDate;
    // End -- Class attributes

    // Getters

    /**
     * Returns the name of the domain
     * @return The domain name
     */
    public String getName() {
        return this.name;
    }
    /**
     * Returns the owner of the domain
     * @return The domain owner
     */
    public String getOwner() {
        return this.owner;
    }

    /**
     * Returns the registration date of the domain
     * @return The registration date of the domain
     */
    public LocalDate getRegistrationDate() {
        return this.registrationDate;
    }
    
    /**
     * Returns the expire date of the domain
     * @return The expiration date of the domain
     */
    public LocalDate getExpireDate() {
        return this.expireDate;
    }
    // End -- Getters

    // Setters
    /**
     * Sets the name of the domain
     * @param name The name of the domain
     * @throws IllegalArgumentException when the string provided is null
     */
    public void setName(String name) throws IllegalArgumentException {
        if(name == null)
            throw new IllegalArgumentException("Domain name is null!");
        this.name = name;
    }

    /**
     * Sets the owner of the domain
     * @param owner The owner of the domain
     */
    public void setOwner(String owner) {
        this.owner = owner;
    }

    /**
     * Sets the registration date of the domain
     * @param rDate La data di registrazione del dominio
     * @throws IllegalArgumentException When the date provided is null
     */
    @JsonbProperty("registrationdate")
    public void setRegisterDate(LocalDate rDate) throws IllegalArgumentException {
        if(rDate == null)
            throw new IllegalArgumentException("Registration date of the domain is null!");
        this.registrationDate = rDate;
    }

    /**
     * Sets the expiration date of the domain
     * @param rDate The expiration date of the domain
     * @throws IllegalArgumentException When the date provided is null
     * 
     */
    @JsonbProperty("expiredate")
    public void setExpireDate(LocalDate eDate) throws IllegalArgumentException {
        if(eDate.isBefore(this.registrationDate) || eDate == null)
            throw new IllegalArgumentException("The expiration date of the domain is null!");
        this.expireDate = eDate;
    }
    // End -- Setters
}
// EOF -- Domain.java
