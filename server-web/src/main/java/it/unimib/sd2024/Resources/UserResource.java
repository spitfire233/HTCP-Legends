//UserResource.java
/**
 * This class contains the REST API resources that handle the requests concerning user registration
 * and user authentication
 */
package it.unimib.sd2024.Resources;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import it.unimib.sd2024.Entities.User;
import it.unimib.sd2024.Utils.CookieGenerator;
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
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Path("users")
public class UserResource {


    /**
     * REST API resource that handles the retrieval of the info of a specific from the database
     * @param email The email of the user
     * @param cookie The authentication cookie the system has assigned to the client once it has logged into the system
     * @return A HTTP response to the register request:
     *  <ul>
     *      <li> 200: OK: The user was found; a JSON representation of it was attached to the body of the request </li>
     *      <li> 400: BAD_REQUEST: The email is invalid</li>
     *      <li> 401: UNAUTHORIZED: The client hasn't logged into the system</li>
     *      <li> 404: NOT_FOUND: The requested user wasn't found</li>
     *      <li> 500: INTERNAL_SERVER_ERROR: A server-side error occurred </li>
     *      <li> 503: SERVICE_UNAVAILABLE: An error has occurred with the database</li>
     * </ul>
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUser(@QueryParam("email") String email, @CookieParam("userAuth") String cookie) {
        if(email == null)
            return Response.status(Status.BAD_REQUEST).build();
        if(cookie == null)
            return Response.status(Status.UNAUTHORIZED).build();
        try{
            DBRequest dbRequest = new DBRequest();
            String dbResponse = dbRequest.select("User", "*", "email", email, ConfrontOperator.E);

            if(dbResponse.equals("{}")) { // No such user!
                return Response.status(Status.NOT_FOUND).build();
            } else if(dbResponse.equals("TABLE_NOT_FOUND") || dbResponse.equals("PARAM_NOT_FOUND")) { // DB Errors
                System.out.println("DB Error: " + dbResponse);
                return Response.status(Status.SERVICE_UNAVAILABLE).build();
            }
            // Deserialize user:
            Jsonb jsonb = JsonbBuilder.create();
            Map<String, User> userMap = jsonb.fromJson(dbResponse, new HashMap<String, User>(){}.getClass().getGenericSuperclass());
            User user = userMap.entrySet().iterator().next().getValue();
            // Return response:
            return Response.ok(jsonb.toJson(user, User.class)).build();
        } catch(JsonbException e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        } catch(IOException e) {
            return Response.status(Status.SERVICE_UNAVAILABLE).build();
        }
    }




    /**
     * REST API Resource that handles the registration requests
     * @param user The user to register
     * @return A HTTP response to the register request:
     *  <ul>
     *      <li> 201: CREATED: The register request was handled successfully </li>
     *      <li> 400: BAD_REQUEST: The register data is incomplete or invalid</li>
     *      <li> 409: CONFLICT: The user already exists</li>
     *      <li> 500: INTERNAL_SERVER_ERROR: A server-side error occurred </li>
     *      <li> 503: SERVICE_UNAVAILABLE: An error has occurred with the database</li>
     * </ul>
     */
    @Path("/register/")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registerUser(User user)  {
        // Validate the user data received:
        if(user.getEmail() == null || user.getName() == null || user.getSurname() == null) {
            return Response.status(Status.BAD_REQUEST).build();
        }

        try {
            // Control if there is already a user with the same e-mail:
            DBRequest dbRequest = new DBRequest();
            String dbResponse = dbRequest.select("User", "*", "email", user.getEmail(), ConfrontOperator.E);
            System.out.println("Response arrived: " + dbResponse);

            if(dbResponse.equals("{}")) { // User doesn't exists
                // Get the last index of the table:
                dbResponse = dbRequest.get_last_index("User");
                if(dbResponse.equals("EMPTY")) { // No previously registered user
                    user.setId("1");
                } else if(!dbResponse.equals("TABLE_NOT_FOUND")) {
                    int id = Integer.parseInt(dbResponse) + 1;
                    user.setId(Integer.toString(id));
                } else { // Table missing
                    System.out.println("DB Error: " + dbResponse);
                    return Response.status(Status.SERVICE_UNAVAILABLE).build();
                }
                // Serialize to Json and send to the database:
                Jsonb jsonb = JsonbBuilder.create();
                dbResponse = dbRequest.create_key("User", user.getId(), jsonb.toJson(user, User.class));
                // Check for positive outcome:
                if(!dbResponse.equals("OK!")) {
                    System.out.println("Database error: " + dbResponse);
                    return Response.status(Status.SERVICE_UNAVAILABLE).build();
                }
                return Response.status(Status.CREATED)
                    .cookie(CookieGenerator.generateCookie("userAuth", user.getId()))
                    .build();
            } else if(dbResponse.equals("TABLE_NOT_FOUND") || dbResponse.equals("PARAM_NOT_FOUND")) { // Table missing
                System.out.println("Database Error: " + dbResponse);
                return Response.status(Status.SERVICE_UNAVAILABLE).build();
            } else { // User already exists:
                System.out.println("Register error: user already exists");
                return Response.status(Status.CONFLICT).build();
            }
        } catch(JsonbException e) {
            System.out.println("Failed to serialize to JSON!");
            e.printStackTrace();
            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        } catch (IOException e) {
            System.out.println("Error connecting or interacting with the database!");
            e.printStackTrace();
            return Response.status(Status.SERVICE_UNAVAILABLE).build();
        } catch(Exception e) {
            System.out.println("A server error has occurred");
            e.printStackTrace();
            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * REST API Resource that handles the user authentication
     * @param email The user email
     * @return A response to the authentication request:
     * <ul>
     *      <li>200: OK: The authentication request was handled successfully</li>
     *      <li>401: UNAUTHORIZED: The user doesn't exist</li>
     *      <li>500: INTERNAL_SERVER_ERROR: A server-side error occurred</li>
     *      <li>503: SERVICE_UNAVAILABLE: An error has occurred with the database</li>
     * </ul>
     */
    @Path("/login/")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    public Response login(@QueryParam("email") String email) {
        try {
            // Try to get the user from the database:
            DBRequest dbRequest = new DBRequest();
            String dbResponse = dbRequest.select("User", "*", "email", email, ConfrontOperator.E);

            if(dbResponse.equals("{}")) { // User isn't registered
                System.out.println("User isn't registered");
                return Response.status(Status.UNAUTHORIZED).build();
            } else if(dbResponse.equals("TABLE_NOT_FOUND") || dbResponse.equals("PARAM_NOT_FOUND")) { // DB errors
                System.out.println("DB Error: " + dbResponse);
                return Response.status(Status.SERVICE_UNAVAILABLE).build();
            }

            // Deserialize the JSON representation of the user:
            Jsonb jsonb = JsonbBuilder.create();
            Map<String, User> userMap = jsonb.fromJson(dbResponse, new HashMap<String, User>(){}.getClass().getGenericSuperclass());
            User user = userMap.entrySet().iterator().next().getValue();

            System.out.println(user.getId());

            // Return the response
            return Response.ok()
                           .cookie(CookieGenerator.generateCookie("userAuth", user.getId()))
                           .build();    
        } catch(JsonbException e) {
            System.out.println("A Json Binding error has occurred!");
            e.printStackTrace();
            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        }
        catch(IOException e) {
            System.out.println("Error connecting or interacting with the database!");
            e.printStackTrace();
            return Response.status(Status.SERVICE_UNAVAILABLE).build();
        }
    }

    /**
     * REST API Resource that handles the retrieval of user info from the database.
     * This Resource can only be accessed by the client if it has already logged into the
     * system.
     * @param cookie The authentication cookie the system has assigned to the client once it has logged into the system
     * @return A response to the GET request:
     * <ul>
     *      <li>200: OK: The GET was handled successfully. The response body contains the user info in JSON format</li>
     *      <li>401: UNAUTHORIZED: The client hasn't logged into the system</li>
     *      <li>500: INTERNAL_SERVER_ERROR: A server-side error occurred</li>
     *      <li>503: SERVICE_UNAVAILABLE: An error as occurred with the database</li>
     * </ul>
     */

    @Path("/user/")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserInfo(@CookieParam("userAuth") String cookie) {
        if(cookie == null)
            return Response.status(Status.UNAUTHORIZED).build();
        try{
            // Check if the cookie corresponds to an id of a user:
            DBRequest dbRequest = new DBRequest();
            String dbResponse = dbRequest.select("User", "*", "id", cookie, ConfrontOperator.E);
            

            if(dbResponse.equals("{}")) { // User doesn't exist!
                System.out.println("The user has an invalid cookie! " + cookie);
                return Response.status(Status.UNAUTHORIZED).build();
            } else if (dbResponse.equals("TABLE_NOT_FOUND") || dbResponse.equals("PARAM_NOT_FOUND")) { // DB Errors
                System.out.println("Db Error: " + dbResponse);
                return Response.status(Status.SERVICE_UNAVAILABLE).build();
            }

            // Deserialize the user:
            Jsonb jsonb = JsonbBuilder.create();
            Map<String, User> userMap = jsonb.fromJson(dbResponse, new HashMap<String, User>(){}.getClass().getGenericSuperclass());
            User user = userMap.entrySet().iterator().next().getValue();

            // Return the response
            return Response.ok(user).build();
        } catch(JsonbException e) {
            System.out.println("Failed to parse to JSON!");
            e.printStackTrace();
            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        } catch(IOException e) {
            System.out.println("Failed to connecting or interacting with the DB!");
            e.printStackTrace();
            return Response.status(Status.SERVICE_UNAVAILABLE).build();
        }
    }

}
// EOF -- UserResource.java