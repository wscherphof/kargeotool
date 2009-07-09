package nl.b3p.kar.hibernate;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import nl.b3p.kar.SecurityRealm;
import nl.b3p.transmodel.DataOwner;

public class Gebruiker implements Principal {
    private Integer id;
    private String username;
    private String passwordsalt;
    private String passwordhash;
    private String fullname;
    private String email;
    private String phone;
    private String position;
    private Set roles;
    private Map<DataOwner, GebruikerDataOwnerRights> dataOwnerRights = new HashMap<DataOwner, GebruikerDataOwnerRights>();

    public void changePassword(HttpServletRequest request, String pw) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        String salt = SecurityRealm.generateHexSalt(request);
        String hash = SecurityRealm.getHexSha1(salt, pw);
        setPasswordsalt(salt);
        setPasswordhash(hash);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordsalt() {
        return passwordsalt;
    }

    public void setPasswordsalt(String passwordSalt) {
        this.passwordsalt = passwordSalt;
    }

    public String getPasswordhash() {
        return passwordhash;
    }

    public void setPasswordhash(String passwordhash) {
        this.passwordhash = passwordhash;
    }

    /* Principal implementatie */
    public String getName() {
        return getUsername();
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }   

    /**
     * @return the roles
     */
    public Set getRoles() {
        return roles;
    }

    /**
     * @param roles the roles to set
     */
    public void setRoles(Set roles) {
        this.roles = roles;
    }

    public boolean isInRole(String roleName) {
        for(Iterator it = getRoles().iterator(); it.hasNext();) {
            Role r = (Role)it.next();
            if(r.getRole().equals(roleName)) {
                return true;
            }
        }
        return false;
    }

    public Map<DataOwner, GebruikerDataOwnerRights> getDataOwnerRights() {
        return dataOwnerRights;
    }

    public void setDataOwnerRights(Map<DataOwner, GebruikerDataOwnerRights> dataOwnerRights) {
        this.dataOwnerRights = dataOwnerRights;
    }

    public boolean canEditDataOwner(DataOwner d) {
        GebruikerDataOwnerRights r = dataOwnerRights.get(d);
        return r != null && r.isEditable();
    }

    public boolean canValidateDataOwner(DataOwner d) {
        GebruikerDataOwnerRights r = dataOwnerRights.get(d);
        return r != null && r.isValidatable();
    }

    public Set<DataOwner> getEditableDataOwners() {
        HashSet<DataOwner> dataOwners = new HashSet<DataOwner>();
        for(Iterator it = dataOwnerRights.entrySet().iterator(); it.hasNext();) {
            Entry<DataOwner, GebruikerDataOwnerRights> entry = (Entry<DataOwner, GebruikerDataOwnerRights>)it.next();
            if(entry.getValue().isEditable()) {
                dataOwners.add(entry.getValue().getDataOwner());
            }
        }
        return dataOwners;
    }
}
