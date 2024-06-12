package de.happybavarian07.webui.utils;

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.utils.LogPrefix;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;
import java.util.List;
import java.util.logging.Level;

public class JwtUtil {
    private final String SECRET_KEY; // Replace with your own secret key
    private final long EXPIRATION_TIME = 864000000; // 5 days in milliseconds

    public JwtUtil(String SECRET_KEY) {
        this.SECRET_KEY = SECRET_KEY;
    }

    public String generateToken(String subject, String username, List<String> roles) {
        return Jwts.builder()
                .setSubject(subject)
                .claim("username", username) // Add the username as a claim
                .claim("roles", roles) // Add the roles as a claim
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
                .compact();
    }

    public boolean isTokenExpired(String token) {
        try {
        return Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody().getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            // Log the exception using plugin logger and return true
            AdminPanelMain.getPlugin().getFileLogger().writeToLog(Level.SEVERE, "An error occurred while checking if a token is expired: ("+ e + ": " + e.getMessage() + ")", LogPrefix.WEBUI_AUTH);
            return true;
        }
    }

    public String getSubject(String token) {
        return Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody().getSubject();
    }

    public String[] getRoles(String token) {
        if (isTokenExpired(token)) {
            return new String[]{"EXPIRED"};
        }
        if(token == null) {
            return new String[]{"INVALID"};
        }

        List<String> roles = (List<String>) Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody().get("roles");
        return roles.toArray(new String[0]);
    }
}
