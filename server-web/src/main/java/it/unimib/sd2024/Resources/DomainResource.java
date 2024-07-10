/**
 * DomainResource.java
 * This class contains the REST API resources that handle the requests concerning domain registration, renewal and retrieval
 */

package it.unimib.sd2024.Resources;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import it.unimib.sd2024.Entities.Domain;
import it.unimib.sd2024.Entities.User;
import it.unimib.sd2024.Utils.DBRequest;
import it.unimib.sd2024.Utils.DBRequest.ConfrontOperator;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbException;
import jakarta.json.bind.annotation.JsonbDateFormat;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Path("domains")
public class DomainResource {
    private static List<String> domainsInRegistration = Collections.synchronizedList(new ArrayList<String>());

    // REST API INTERFACE:
    /**
     * REST API resource that handles the retrieval of all the domains registered in the database
     * @param cookie The authorization cookie of the client; used for authentication purposes
     * @return The response to the request:
     * <ul>
     *      <li>200: OK: The request was handles successfully; a JSON object containing all the domains
     *      was attached to the request</li>
     *      <li>401: UNAUTHORIZED: The client isn't authenticated</li>
     *      <li>503: SERVICE_UNAVAILABLE: An error has occurred with the database</li>
     * </ul>
     */

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDomains(@CookieParam("userAuth") String cookie) {
        if(cookie == null)
            return Response.status(Status.UNAUTHORIZED).build();

        try {
            // Send the request to the database and obtain the response:
            DBRequest dbRequest = new DBRequest();
            String dbResponse = dbRequest.select("Domain", "*");

            // Check for database errors:
            if(dbResponse.equals("TABLE_NOT_FOUND")) {
                System.out.println("Seems like the domain table isn't present in the database");
                return Response.status(Status.SERVICE_UNAVAILABLE).build();
            }

            // Return the JSON
            return Response.ok(dbResponse).build();
        } catch(IOException e) {
            System.out.println("Error connecting or interacting with the database");
            return Response.status(Status.SERVICE_UNAVAILABLE).build();
        }
    }

    /**
     * REST API resource that handles the registration of a new domain
     * @param cookie The authorization cookie of the client; used for authentication purposes
     * @param domain The domain to register
     * @return The response to the request:
     * <ul>
     *      <li>201: CREATED: The domain was registered correctly</li>
     *      <li>400: BAD_REQUEST: The domain passed isn't valid!</li>
     *      <li>401: UNAUTHORIZED: The client isn't authenticated</li>
     *      <li>409: CONFLICT: The domain to register already exists or is currently in registration</li>
     *      <li>500: INTERNAL_SERVER_ERROR: A server-side error occurred</li>
     *      <li>503: SERVICE_UNAVAILABLE: An error has occurred with the database</li>
     * </ul>
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @JsonbDateFormat(value = "yyyy-MM-dd")
    public Response registerDomain(@CookieParam("userAuth") String cookie, Domain domain) {
        // Check user input:
        if(domain.getName() == null || domain.getRegistrationDate() == null || domain.getExpireDate() == null) {
            return Response.status(Status.BAD_REQUEST).build();
        }
        // Check if the user is logged:
        if(cookie == null)
            return Response.status(Status.UNAUTHORIZED).build();
        // Check if the domain is currently in registration:
        if(domainsInRegistration.contains(domain.getName()))
            return Response.status(Status.CONFLICT).build();
        try {
            // Register the domain as "in registration":
            domainsInRegistration.add(domain.getName());
            // Get the User from the database:
            DBRequest dbRequest = new DBRequest();
            String dbResponse = dbRequest.select("User", cookie);

            if(dbResponse.equals("{}")) { // User logged with an invalid cookie
                System.out.println("Invalid session!");
                return Response.status(Status.UNAUTHORIZED).build();
            } else if(dbResponse.equals("TABLE_NOT_FOUND") || dbResponse.equals("PARAM_NOT_FOUND")
                                                                    || dbResponse.equals("KEY_NOT_FOUND")) { // DB Error
                System.out.println("DB Error: " + dbResponse);
                return Response.status(Status.SERVICE_UNAVAILABLE).build();
            }

            // Deserialize User:
            Jsonb jsonb = JsonbBuilder.create();
            Map<String, User> userMap = jsonb.fromJson(dbResponse, new HashMap<String, User>(){}.getClass().getGenericSuperclass());
            User user = userMap.get(cookie);
            // Set domain owner:
            domain.setOwner(user.getEmail());

            // Register the domain on the database
            dbResponse = dbRequest.get_last_index("Domain");
            int id;

            if(dbResponse.equals("EMPTY")) { // No domain previously registered
                id = 1;
            } else {
                id = Integer.parseInt(dbResponse) + 1;
            }
            dbResponse = dbRequest.create_key("Domain", Integer.toString(id), jsonb.toJson(domain, Domain.class));

            if(!dbResponse.equals("OK!")) {
                System.out.println("DB error: " + dbResponse);
                return Response.status(Status.SERVICE_UNAVAILABLE).build();
            }
            return Response.status(Status.CREATED).build();
        } catch(JsonbException e) {
            e.printStackTrace();
            System.out.println("Error serializing or deserializing to/from JSON");
            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        } catch(IOException e) {
            e.printStackTrace();
            System.out.println("Error connecting or interacting with the database!");
            return Response.status(Status.SERVICE_UNAVAILABLE).build();
        } finally {
            domainsInRegistration.remove(domain.getName());
        }
    }

    /**+
     * REST API resource that checks if a domain is available
     * @param domain The domain to check
     * @param cookie The authorization cookie of the client; used for authentication purposes
     * @return A response to the request:
     * <ul>
     *      <li>200: OK: The domain is NOT available; a JSON with the information on the domain has been attached to the
     *                   body of the response</li>
     *      <li>204: NO_CONTENT: The domain is available</li>
     *      <li>400: BAD_REQUEST: The client request contains a null domain</li>
     *      <li>401: UNAUTHORIZED: The client isn't authenticated</li>
     *      <li>409: CONFLICT: The domain is currently in registration and thus isn't available</li>
     *      <li>500: INTERNAL_SERVER_ERROR: A server-side error has occurred</li>
     *      <li>503: SERVICE_UNAVAILABLE: An error has occurred with the database</li>
     * </ul>
     */
    @Path("/isAvailable")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @JsonbDateFormat(value = "yyyy-MM-dd")
    public Response isDomainAvailable(@QueryParam("domain") String domainName, @CookieParam("userAuth") String cookie) {
        // Validate user input
        if(domainName == null)
            return Response.status(Status.BAD_REQUEST).build();
        // Validate user has a cookie
        if(cookie == null)
            return Response.status(Status.UNAUTHORIZED).build(); 
        // Check if the domain is in registration:
        if(domainsInRegistration.contains(domainName))
            return Response.status(Status.CONFLICT).build();    
        

        try{
            // Preliminary steps
            Jsonb jsonb = JsonbBuilder.create();
            boolean isAvailable = true ;
            Domain domain = null;
            // Check if the domain exists:
            DBRequest dbRequest = new DBRequest();
            String dbResponse = dbRequest.select("Domain", "*", "name", domainName, ConfrontOperator.E);

            if(!dbResponse.equals("{}")) { // A domain with this name exists;
                Map<String, Domain> domainMap = jsonb.fromJson(dbResponse, new HashMap<String, Domain>(){}.getClass().getGenericSuperclass());
                Iterator<Entry<String, Domain>> iterator = domainMap.entrySet().iterator();
                while(iterator.hasNext()) {
                    domain = iterator.next().getValue();
                    LocalDate now = LocalDate.now();
                    if(!now.isAfter(domain.getExpireDate())) // Domain hasn't expired yet!
                        isAvailable = false;
                }
            } else if (domainName.equals("TABLE_NOT_FOUND") || domainName.equals("PARAM_NOT_FOUND")) { // DB Errors
                System.out.println("DB Error: " + dbResponse);
                return Response.status(Status.SERVICE_UNAVAILABLE).build();
            }

            if(isAvailable)
                return Response.status(Status.NO_CONTENT).build();
            else
                return Response.ok(jsonb.toJson(domain)).build();
        } catch(JsonbException e) {
            System.out.println("A JsonB error has occurred!");
            e.printStackTrace();
            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        } catch(IOException e) {
            System.out.println("Error connecting or interacting with the database!");
            e.printStackTrace();
            return Response.status(Status.SERVICE_UNAVAILABLE).build();
        }
    }

    /**
     * REST API resource the handles the extraction of a domain from the database
     * @param domain The domain to extract
     * @param cookie The authorization cookie of the client; used for authentication purposes
     * @return A response to the request:
     * <ul>
     *      <li>200: OK: The domain is present; a JSON containing the domain information has been attached to
     *       to the response body</li>
     *      <li>400: BAD_REQUEST: The client request contains a null domain</li>
     *      <li>401: UNAUTHORIZED: The client isn't authenticated</li>
     *      <li>404: NOT_FOUND: The specified domain isn't in the database</li>
     *      <li>500: INTERNAL_SERVER_ERROR: A server-side error has occurred</li>
     *      <li>503: SERVICE_UNAVAILABLE: An error has occurred with the database</li>
     * </ul>
     */
    @Path("/{domain}/")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDomain(@PathParam("domain") String domainName, @CookieParam("userAuth") String cookie) {
        // Validate user input:
        if(domainName == null)
            return Response.status(Status.BAD_REQUEST).build();
        if(cookie == null)
            return Response.status(Status.UNAUTHORIZED).build();

        try {
            // Get the domain from the database:
            DBRequest dbRequest = new DBRequest();
            String dbResponse = dbRequest.select("Domain", "*", "name", domainName, ConfrontOperator.E);

            if(dbResponse.equals("{}")) {  // Domain isn't present in the database
                System.out.println("Domain isn't present in the database");
                return Response.status(Status.NOT_FOUND).build();
            } else if(dbResponse.equals("TABLE_NOT_FOUND") || dbResponse.equals("PARAM_NOT_FOUND")) { // DB Error
                System.out.println("DB Error: " + dbResponse);
                return Response.status(Status.SERVICE_UNAVAILABLE).build();
            }

            // Deserialize the domain
            Jsonb jsonb = JsonbBuilder.create();
            Map<String, Domain> domainMap = jsonb.fromJson(dbResponse, new HashMap<String, Domain>(){}.getClass().getGenericSuperclass());
            Domain domain = domainMap.entrySet().iterator().next().getValue();

            return Response.ok(jsonb.toJson(domain, Domain.class)).build();
        } catch(JsonbException e) {
            System.out.println("Error deserializing JSON");
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
     * REST API resource that handles the renewal of a domain:
     * @param cookie The authorization cookie of the client; used for authentication purposes
     * @param domain The domain to renew
     * @param new_expr_date The new expire date in format "YYYY-MM-DD"
     * @return A response to the request:
     * <ul>
     *      <li>200: OK: The domain was modified successfully!</li>
     *      <li>400: BAD_REQUEST: The client request contains a null new_expr_date</li>
     *      <li>401: UNAUTHORIZED: The client isn't authenticated</li>
     *      <li>404: NOT_FOUND: The specified domain isn't present in the database</li>
     *      <li>500: INTERNAL_SERVER_ERROR: A server-side error has occurred</li>
     *      <li>503: SERVICE_UNAVAILABLE: An error has occurred with the database</li>
     * </ul>
     */

    @Path("/renewDomain/{domain}/")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @JsonbDateFormat(value = "yyyy-MM-dd")
    public Response renewDomain(@PathParam("domain") String domainName, @QueryParam("date") String new_expr_date, @CookieParam("userAuth") String cookie) {
        // Validate user input:
        if(new_expr_date == null)
            return Response.status(Status.BAD_REQUEST).build();
        // Validate user authentication:
        if(cookie == null)
            return Response.status(Status.UNAUTHORIZED).build();

        try {
            // Get the domain from the database:
            DBRequest dbRequest = new DBRequest();
            String dbResponse = dbRequest.select("Domain", "*", "name", domainName, ConfrontOperator.E);

            if(dbResponse.equals("{}")) { // No such domain in the database
                System.out.println("The specified domain isn't in the database!");
                return Response.status(Status.NOT_FOUND).build();
            } else if(dbResponse.equals("TABLE_NOT_FOUND") || dbResponse.equals("PARAM_NOT_FOUND")) { // DB Error
                System.out.println("DB Error: " + dbResponse);
                return Response.status(Status.SERVICE_UNAVAILABLE).build();
            }

            // Modify the domain:
            Jsonb jsonb = JsonbBuilder.create();
            Map<String, Domain> domainMap = jsonb.fromJson(dbResponse, new HashMap<String, Domain>(){}.getClass().getGenericSuperclass());
            String domainKey = domainMap.entrySet().iterator().next().getKey();
            
            dbResponse = dbRequest.modify("Domain", domainKey, "expiredate", new_expr_date);

            if(!dbResponse.equals("OK!")) { //DB Error
                System.out.println("DB Error: " + dbResponse);
                return Response.status(Status.SERVICE_UNAVAILABLE).build();
            }
            return Response.ok().build();
        } catch(JsonbException e) {
            System.out.println("Error deserializing JSON");
            e.printStackTrace();
            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        } catch(IOException e) {
            return Response.status(Status.SERVICE_UNAVAILABLE).build();
        }
    }
    /**
     * REST API resource that handles the retrieval of all the domains registered by a certain user
     * @param cookie The authorization cookie of the client; used for authentication purposes
     * @return A response to the request:
     * <ul>
     *      <li>200: OK: The request was handles successfully; a JSON containing all the user's domains was attched
     *      to the body of the respons </li>
     *      <li>401: UNAUTHORIZED: The client isn't authenticated</li>
     *      <li>500: INTERNAL_SERVER_ERROR: A server-side error has occurred</li>
     *      <li>503: SERVICE_UNAVAILABLE: An error has occurred with the database</li>
     * </ul>
     */
    @Path("/userDomains/")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserDomain(@CookieParam("userAuth") String cookie) {
        // Check if the user is authenticated
        if(cookie == null)
            return Response.status(Status.UNAUTHORIZED).build();

        try {
            // Get the user from the database:
            DBRequest dbRequest = new DBRequest();
            String dbResponse = dbRequest.select("User", "*", "id", cookie, ConfrontOperator.E);

            if(dbResponse.equals("{}")) { // No such user exists!
                System.out.println("This user doesn't exist");
                return Response.status(Status.UNAUTHORIZED).build();
            } else if(dbResponse.equals("TABLE_NOT_FOUND") || dbResponse.equals("PARAM_NOT_FOUND")) { // DB Error
                System.out.println("DB Error " + dbResponse);
                return Response.status(Status.SERVICE_UNAVAILABLE).build();
            }

            // Deserialize user:
            Jsonb jsonb = JsonbBuilder.create();
            Map<String, User> userMap = jsonb.fromJson(dbResponse, new HashMap<String, User>(){}.getClass().getGenericSuperclass());
            User user = userMap.get(cookie);

            // Get the email and send the request:
            dbResponse = dbRequest.select("Domain", "*", "owner", user.getEmail(), ConfrontOperator.E);

            if(dbResponse.equals("TABLE_NOT_FOUND") || dbResponse.equals("PARAM_NOT_FOUND")) { // DB Error
                System.out.println("DB Error " + dbResponse);
                return Response.status(Status.SERVICE_UNAVAILABLE).build();
            }
            return Response.ok(dbResponse).build();
        } catch(JsonbException e) {
            System.out.println("Error deserializing JSON");
            e.printStackTrace();
            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        } catch(IOException e) {
            System.out.println("An error occurred interacting or connecting to the database!");
            return Response.status(Status.SERVICE_UNAVAILABLE).build();
        }
    }
    // END -- REST API INTERFACE
}
// EOF -- DomainResource.java