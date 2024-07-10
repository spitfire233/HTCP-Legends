/** Order.java
 * This class represents an order for a domain.
 * This class DOESN'T handle the persistance of orders in the system.
 */
package it.unimib.sd2024.Entities;

import java.time.LocalDate;

public class Order {

    // Class attributes
    private String domain;
    private String client;
    private LocalDate date;
    int cost;
    private enum OrderType {
        REGISTER,
        RENEWAL
    }
    private OrderType orderType;
    // END -- Class attributes

    // Getters

    /**
     * Returns the domain associated with the order
     * @return The domain associated with the order
     */
    public String getDomain() {
        return this.domain;
    }

    /**
     * Returns the client associated with the order
     * @return The client associated with the order
     */
    public String getClient() {
        return this.client;
    }

    /**
     * Returns the cost of the order
     * @return The cost of the order
     */
    public int getCost() {
        return this.cost;
    }

    /**
     * Returns the order type
     * @return The order type
     */
    public String getOrderType() {
        return this.orderType.toString();
    }

    /**
     * Returns the date in which the order was placed
     * @return The date in which the order was placed
     */
    public LocalDate getDate() {
        return this.date;
    }
    // END -- Getters

    // Setters

    /**
     * Sets the domain associated with the order
     * @param domain The domain associated with the order
     */
    public void setDomain(String domain) throws IllegalArgumentException {
        if(domain == null)
            throw new IllegalArgumentException("Domain cannot be null!");
        this.domain = domain;
    }

    /**
     * Sets the client associated with the order
     * @param client The client associated with the order
     */
    public void setClient(String client) throws IllegalArgumentException {
        if(client == null)
            throw new IllegalArgumentException("Name cannot be null!");
        this.client = client;
    }

    /**
     * Sets the cost of the order
     * @param cost the cost of the order
     */
    public void setCost(int cost) throws IllegalArgumentException {
        if(cost == 0)
            throw new IllegalArgumentException("Cost cannot be 0!");
        this.cost = cost;
    }

    /**
     * Sets the order type.
     * @param orderType
     */
    public void setOrderType(String orderType) throws IllegalArgumentException{
        if(!orderType.toUpperCase().equals("REGISTER") && !orderType.toUpperCase().equals("RENEWAL"))
            throw new IllegalArgumentException("Invalid order type!");
        this.orderType = OrderType.valueOf(orderType.toUpperCase());
    }
    /**
     * Sets the date of the order
     * @param date The date of the order
     * @throws IllegalArgumentException When the the date provides is nulls
     */
    public void setDate(LocalDate date) throws IllegalArgumentException {
        if(date == null)
            throw new IllegalArgumentException("The date is invalid!");
        this.date = date;
    }
    // END -- Setters
}
// EOF -- Order.java
