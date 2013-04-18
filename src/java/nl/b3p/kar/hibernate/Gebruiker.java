/**
 * Geo-OV - applicatie voor het registreren van KAR meldpunten               
 *                                                                           
 * Copyright (C) 2009-2013 B3Partners B.V.                                   
 *                                                                           
 * This program is free software: you can redistribute it and/or modify      
 * it under the terms of the GNU Affero General Public License as            
 * published by the Free Software Foundation, either version 3 of the        
 * License, or (at your option) any later version.                           
 *                                                                           
 * This program is distributed in the hope that it will be useful,           
 * but WITHOUT ANY WARRANTY; without even the implied warranty of            
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the              
 * GNU Affero General Public License for more details.                       
 *                                                                           
 * You should have received a copy of the GNU Affero General Public License  
 * along with this program. If not, see <http://www.gnu.org/licenses/>.      
 */

package nl.b3p.kar.hibernate;

import javax.persistence.*;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import nl.b3p.kar.SecurityRealm;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;
import org.stripesstuff.stripersist.Stripersist;

/**
 * Klasse voor definitie van een gebruiker
 * 
 * @author Chris
 */
@Entity
public class Gebruiker implements Principal {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer id;
    
    @Column(unique=true, nullable=false)
    private String username;
    private String passwordsalt;
    private String passwordhash;
    private String fullname;
    private String email;
    private String phone;
    private String position;
    
    /**
     * JSON string met profielinstellingen.
     */
    @Column(columnDefinition="text")
    private String profile;
    
    @ManyToMany
    @JoinTable(joinColumns=@JoinColumn(name="gebruiker"), inverseJoinColumns=@JoinColumn(name="role"))
    private Set<Role> roles = new HashSet();
    
    @OneToMany(mappedBy="gebruiker")
    @MapKeyJoinColumn(name="data_owner")
    @Sort(type=SortType.NATURAL)
    private SortedMap<DataOwner, GebruikerDataOwnerRights> dataOwnerRights = new TreeMap<DataOwner, GebruikerDataOwnerRights>();

    /**
     * Verandert het wachtwoord
     * 
     * @param request nodig voor bepaling random salt
     * @param pw het wachtwoord
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     */
    public void changePassword(HttpServletRequest request, String pw) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        String salt = SecurityRealm.generateHexSalt(request);
        String hash = SecurityRealm.getHexSha1(salt, pw);
        setPasswordsalt(salt);
        setPasswordhash(hash);
    }

    /**
     * Zoekt de huidige gebruiker
     * 
     * @param request waarin gebruiker is te vinden
     * @return de gebruiker
     * @throws Exception
     */
    public static Gebruiker getNonTransientPrincipal(HttpServletRequest request) throws Exception {
        Gebruiker g = (Gebruiker)request.getUserPrincipal();
        if(g == null) {
            return null;
        }
        return Stripersist.getEntityManager().find(Gebruiker.class, g.getId());
    }

    /**
     * Haalt id van gebruiker op
     * 
     * @return id
     */
    public Integer getId() {
        return id;
    }

    /**
     *
     * @param id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Haalt gebruikersnaam op
     * 
     * @return username
     */
    public String getUsername() {
        return username;
    }

    /**
     *
     * @param username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     *
     * @return passwordsalt
     */
    public String getPasswordsalt() {
        return passwordsalt;
    }

    /**
     *
     * @param passwordSalt
     */
    public void setPasswordsalt(String passwordSalt) {
        this.passwordsalt = passwordSalt;
    }

    /**
     *
     * @return passwordhash
     */
    public String getPasswordhash() {
        return passwordhash;
    }

    /**
     *
     * @param passwordhash
     */
    public void setPasswordhash(String passwordhash) {
        this.passwordhash = passwordhash;
    }

    /**
     * Principal implementatie 
     */
    public String getName() {
        return getUsername();
    }

    /**
     *
     * @return fullname
     */
    public String getFullname() {
        return fullname;
    }

    /**
     *
     * @param fullname
     */
    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    /**
     *
     * @return email
     */
    public String getEmail() {
        return email;
    }

    /**
     *
     * @param email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     *
     * @return phone
     */
    public String getPhone() {
        return phone;
    }

    /**
     *
     * @param phone
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     *
     * @return position
     */
    public String getPosition() {
        return position;
    }

    /**
     *
     * @param position
     */
    public void setPosition(String position) {
        this.position = position;
    }   

    /**
     *
     * @return roles
     */
    public Set<Role> getRoles() {
        return roles;
    }

    /**
     *
     * @param roles
     */
    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    /**
     *
     * @return is gebruiker beheerder?
     */
    public boolean isBeheerder() {
        return isInRole(Role.BEHEERDER);
    }
    
    /**
     *
     * @param roleName
     * @return heeft gebruiker de gevraagde rol?
     */
    public boolean isInRole(String roleName) {
        for(Iterator it = getRoles().iterator(); it.hasNext();) {
            Role r = (Role)it.next();
            if(r.getRole().equals(roleName)) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @return dataOwnerRights
     */
    public Map<DataOwner, GebruikerDataOwnerRights> getDataOwnerRights() {
        return dataOwnerRights;
    }

    /**
     *
     * @param dataOwnerRights
     */
    public void setDataOwnerRights(SortedMap<DataOwner, GebruikerDataOwnerRights> dataOwnerRights) {
        this.dataOwnerRights = dataOwnerRights;
    }

    /**
     *
     * @param d
     * @return kan/mag gebruiker de DataOwner bewerken?
     */
    public boolean canEditDataOwner(DataOwner d) {
        GebruikerDataOwnerRights r = dataOwnerRights.get(d);
        return r != null && r.isEditable();
    }

    /**
     *
     * @param d
     * @return kan/mag gebruiker DataOwner valideren
     */
    public boolean canValidateDataOwner(DataOwner d) {
        GebruikerDataOwnerRights r = dataOwnerRights.get(d);
        return r != null && r.isValidatable();
    }

    /**
     *
     * @return dataOwners
     */
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

    /**
     * Zet de rechten mbt de DataOwner
     * @param dao het dataowner object
     * @param editable mag geedit worden
     * @param validatable mag gevalideerd worden
     * @throws Exception
     */
    public void setDataOwnerRight(DataOwner dao, Boolean editable, Boolean validatable) throws Exception {
        EntityManager em = Stripersist.getEntityManager();
        GebruikerDataOwnerRights dor = getDataOwnerRights().get(dao);
        if(dor == null) {
            dor = new GebruikerDataOwnerRights();
            dor.setDataOwner(dao);
            dor.setGebruiker(this);
            em.persist(dor);
            getDataOwnerRights().put(dao, dor);
        }
        if(editable != null) {
            dor.setEditable(editable);
        }
        if(validatable != null) {
            dor.setValidatable(validatable);
        }
    }
}
