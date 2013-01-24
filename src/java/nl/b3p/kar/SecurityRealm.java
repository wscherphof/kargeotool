/*
 * $Id: SecurityRealm.java 9928 2009-01-16 13:24:00Z Jytte $
 */

package nl.b3p.kar;

import java.io.UnsupportedEncodingException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.servlet.http.HttpServletRequest;
import nl.b3p.kar.persistence.MyEMFDatabase;
import nl.b3p.kar.hibernate.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.securityfilter.realm.SecurityRealmInterface;
import org.stripesstuff.stripersist.Stripersist;

/**
 * SecurityRealm implementatie voor SecurityFilter 2.0.
 *
 * Deze realm gebruikt "Gebruiker" objecten als Principal.
 *
 * Wachtwoorden worden gehashed opgeslagen in de database en er wordt gebruik
 * gemaakt van een salt. De salt en het wachtwoord geconcateneerd worden
 * gehashed met SHA-1. De salt en hash worden als hex in de database opgeslagen
 * in een varchar kolom omdat dit wat handzamer is dan een blob/bytea kolom.
 *
 * Wachtwoorden worden gehashed in UTF8 encoding, dus
 * salt = 00 11 22 33, wachtwoord = "test", hash phrase = 00 11 22 33 74 65 73 74
 * sha-1 hash = af30b67b3c0e3fcd1d80ba679770f3947f6edd8d
 * salt = 00 11 22 33, wachtwoord = "tëst", hash phrase = 00 11 22 33 74 c3 ab 73 74
 * sha-1 hash = d49a8431ec274a1433b7fdda34e4de0b2784b812
 */
public class SecurityRealm implements SecurityRealmInterface {

    private static final Log auditLog = LogFactory.getLog("audit");
    
    private static final int SALT_SIZE = 4;

    /**
     * @param request gebruikt voor initialisatie PRNG met IP en poort van client
     * @return salt in hex string
     */
    public static String generateHexSalt(HttpServletRequest request) {        
        long seed = System.currentTimeMillis();
        long ip = 1;
        try {
            InetAddress addr = InetAddress.getByName(request.getRemoteAddr());
            if(addr instanceof Inet4Address) {
                byte raw[] = ((Inet4Address)addr).getAddress();
                ip = raw[0] << 24 | raw[1] << 16 | raw[2] << 8 | raw[3];
            }
        } catch (UnknownHostException ex) {
            /* ip blijft 1 */
        }
        seed = seed * ip;
        if(request.getRemotePort() != 0) {
            seed = seed * request.getRemotePort();
        }
        Random random = new Random(seed);
        StringBuffer salt = new StringBuffer(SALT_SIZE*2);
        for(int i = 0; i < SALT_SIZE; i++) {
            int b = random.nextInt(16);
            salt.append(Integer.toHexString(b));
            b = random.nextInt(16);
            salt.append(Integer.toHexString(b));
        }
        return salt.toString();
    }

	public static String getHexSha1(String saltHex, String phrase) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        saltHex = saltHex.trim();
        if(saltHex.length() % 2 != 0) {
            throw new IllegalArgumentException("Invalid salt hex length (must be divisible by 2): " + saltHex.length());
        }

        byte[] salt = new byte[saltHex.length() / 2];
        try {
            for(int i = 0; i < saltHex.length() / 2; i++) {
                int hexIdx = i*2;
                int highNibble = Integer.parseInt(saltHex.substring(hexIdx,hexIdx+1), 16);
                int lowNibble = Integer.parseInt(saltHex.substring(hexIdx+1,hexIdx+2), 16);
                salt[i] = (byte)(highNibble << 4 | lowNibble);
            }
        } catch(NumberFormatException nfe) {
            throw new IllegalArgumentException("Invalid hex characters in salt parameter: " + saltHex);
        }
        return getHexSha1(salt, phrase);
    }

    public static String getHexSha1(byte[] salt, String phrase) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        byte[] phraseUTF8 = phrase.getBytes("UTF8");
        byte[] saltedPhrase = new byte[salt.length + phraseUTF8.length];

        System.arraycopy(salt, 0, saltedPhrase, 0, salt.length);
        System.arraycopy(phraseUTF8, 0, saltedPhrase, salt.length, phraseUTF8.length);

        MessageDigest md = MessageDigest.getInstance("SHA-1");
		byte[] digest = md.digest(saltedPhrase);

        /* Converteer byte array naar hex-weergave */
        StringBuffer sb = new StringBuffer(digest.length*2);
        for(int i = 0; i < digest.length; i++) {
            sb.append(Integer.toHexString(digest[i] >> 4 & 0xf)); /* and mask met 0xf nodig door sign-extenden van bytes... */
            sb.append(Integer.toHexString(digest[i] & 0xf));
        }
        return sb.toString();
	}
    
    /*public static void testGetHexSha1() throws Exception {
        String salt = "00112233";
        String phrase = "test";
        System.out.println(getHexSha1(salt, phrase).equals("af30b67b3c0e3fcd1d80ba679770f3947f6edd8d"));
        phrase = "tëst";
        System.out.println(getHexSha1(salt, phrase).equals("d49a8431ec274a1433b7fdda34e4de0b2784b812"));
    }*/

    public Principal authenticate(String username, String password) {
        /* TODO: "LOGIN", "username" en "ALLOW" MDC vars maken, gebruik wel filter dat bij
         * elk request MDC.clear() doet en filter dat username instelt
         */
        String auditAllow = "LOGIN ALLOW: username=\"" + username + "\": ";
        String auditDeny = "LOGIN DENY:  username=\"" + username + "\": ";

        try {
            Stripersist.requestInit();
            EntityManager em = Stripersist.getEntityManager();

            try {
                Gebruiker g = (Gebruiker)em.createQuery(
                        "from Gebruiker g where " +
                        "g.username = :username ")
                    .setParameter("username", username)
                    .getSingleResult();

                /* Check of het gegeven password overeenkomt: hash de salt met
                 * het gegeven password, indien dit overeenkomt met de opgeslagen
                 * hash is het password correct
                 */
                String hash = getHexSha1(g.getPasswordsalt(), password);
                if(hash.equals(g.getPasswordhash())) {
                    String info =  "authenticatie check ok; ";
                    Set roles=g.getRoles();
                    if (roles!=null){
                        Iterator it= roles.iterator();
                        while (it.hasNext()){
                            info+= " "+((Role)it.next()).getRole();
                        }
                    }
                    auditLog.info(auditAllow + info);
                    return g;
                } else {
                    auditLog.info(auditDeny + "password hash komt niet overeen (ongeldig wachtwoord)");
                    return null;
                }
            } catch(NoResultException nre) {
                auditLog.info(auditDeny + "geen gebruiker gevonden voor gebruikersnaam");
                return null;
            } 
        } catch(Exception e) {
            auditLog.error("LOGIN ERROR: username=\"" + username + "\": exception", e);
            throw new RuntimeException("Exception checking authentication database", e);
        } finally {
            Stripersist.requestComplete();
        }
    }

    /**
     * Er zijn drie rollen, bepaald door het feit of bij een Gebruiker entity
     * de gemeente, regio of provincie relatie not-null is.
     */
    public boolean isUserInRole(Principal principal, String role) {
        if(principal == null) {
            return false;
        }        
        Gebruiker g = (Gebruiker)principal;
        Set roles= g.getRoles();
        Iterator it=roles.iterator();
        while (it.hasNext()){
            Role r = (Role)it.next();
            if (role.equalsIgnoreCase(r.getRole())){
                return true;
            }
        }
        return false;
    }
}
