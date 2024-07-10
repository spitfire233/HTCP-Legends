// CookieGenerator.java
/**
 * This class handles the generation of cookies. The cookies generated from the class may not be compliant
 * to the latest security standards
 */

package it.unimib.sd2024.Utils;

import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.NewCookie.SameSite;

public class CookieGenerator {
    // Class methods:

    /**
     * Generates a new cookie
     * @param name The name of the cookie
     * @param value The value of the cookie
     * @return The generated cookie
     */
    public static NewCookie generateCookie(String name, String value) {
         NewCookie cookie = new NewCookie.Builder(name)
            .sameSite(SameSite.NONE)
            .path("/")
            .domain("localhost")
            .maxAge(-1)
            .secure(true)
            .value(value)
            .build();
        return cookie;
    }
    // END -- Class methods
}
// EOF -- CookieGenerator.java
