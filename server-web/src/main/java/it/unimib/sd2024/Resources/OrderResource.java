package it.unimib.sd2024.Resources;

import java.io.IOException;
import it.unimib.sd2024.Entities.Order;
import it.unimib.sd2024.Utils.DBRequest;
import it.unimib.sd2024.Utils.DBRequest.ConfrontOperator;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Path("orders")
public class OrderResource {
    /**
     * REST API endpoint that handles the retrieval of the user's orders from the database
     * @param cookie The user authorization cookie, used only for authorization purposes
     * @return  The response to the request:
     * <ul>
     *      <li>200: OK: The request was handled successfully; a JSON with the user's orders was attached to the body of the response</li>
     *      <li>401: UNAUTHORIZED: The user isn't authenticated!</li>
     *      <li>503: SERVICE_UNAVAILABLE: An error has occurred with the database</li>
     * </ul>
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserOrders(@CookieParam("userAuth") String cookie) {
        if(cookie == null)
            return Response.status(Status.UNAUTHORIZED).build();

        try {
            // Get the user's orders from the database:
            DBRequest dbRequest = new DBRequest();
            String dbResponse = dbRequest.select("Order", "*", "client", cookie, ConfrontOperator.E);

            if(dbRequest.equals("TABLE_NOT_FOUND") || dbResponse.equals("PARAM_NOT_FOUND")) {
                System.out.println("DB Error: " + dbResponse);
                return Response.status(Status.SERVICE_UNAVAILABLE).build();
            }
            return Response.ok(dbResponse).build();

        } catch(IOException e) {
            System.out.println("Error connecting or interacting with the database");
            return Response.status(Status.SERVICE_UNAVAILABLE).build();
        }
    }


    /**
     * REST API Endpoint that handles the registration of a new order
     * @param order The order to register
     * @param cookie The user authorization cookie, used only for authorization purposes
     * @return The response to the request:
     * <ul>
     *      <li>201: CREATED: The order was registered successfully</li>
     *      <li>400: BAD_REQUEST: The order passed isn't valid</li>
     *      <li>401: UNAUTHORIZED: The user isn't authenticated!</li>
     *      <li>500: INTERNAL_SERVER_ERROR: A server-side error has occurred</li>
     *      <li>503: SERVICE_UNAVAILABLE: An error has occurred with the database</li>
     * </ul>
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response placeOrder(Order order, @CookieParam("userAuth") String cookie) {
        // Check user input:
        if(order.getDomain() == null || order.getCost() == 0 || order.getOrderType() == null || order.getDate() == null) {
            return Response.status(Status.BAD_REQUEST).build();
        }
        // Check if user is authenticated:
        if(cookie == null)
            return Response.status(Status.UNAUTHORIZED).build();
        try {
            // Set the client:
            order.setClient(cookie);

            // Create the order on the database:
            DBRequest dbRequest = new DBRequest();
            String dbResponse = dbRequest.get_last_index("Order");

            int last_index;
            if(dbResponse.equals("EMPTY")) { // No previously registered order
                last_index = 1;
            } else if(!dbResponse.equals("TABLE_NOT_FOUND")) {
                last_index = Integer.parseInt(dbResponse) + 1;
            } else { // DB Error
                System.out.println("DB Error: " + dbResponse);
                return Response.status(Status.SERVICE_UNAVAILABLE).build();
            }
            Jsonb jsonb = JsonbBuilder.create();
            dbResponse = dbRequest.create_key("Order", Integer.toString(last_index), jsonb.toJson(order));

            if(!dbResponse.equals("OK!")) { // DB Error
                System.out.println("DB Error: " + dbResponse);
                return Response.status(Status.SERVICE_UNAVAILABLE).build();
            }
            return Response.status(Status.CREATED).build();
        } catch(JsonbException e) {
            System.out.println("Failed to parse to JSON!");
            e.printStackTrace();
            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        } catch (IOException e) {
            System.out.println("An error has occurred interacting or connecting to the database");
            return Response.status(Status.SERVICE_UNAVAILABLE).build();
        }
    }
}
