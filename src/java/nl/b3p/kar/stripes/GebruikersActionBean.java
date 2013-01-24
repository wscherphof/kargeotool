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

package nl.b3p.kar.stripes;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.controller.LifecycleStage;
import net.sourceforge.stripes.validation.EmailTypeConverter;
import net.sourceforge.stripes.validation.SimpleError;
import net.sourceforge.stripes.validation.Validate;
import net.sourceforge.stripes.validation.ValidateNestedProperties;
import net.sourceforge.stripes.validation.ValidationErrorHandler;
import net.sourceforge.stripes.validation.ValidationErrors;
import nl.b3p.kar.hibernate.DataOwner;
import nl.b3p.kar.hibernate.Gebruiker;
import nl.b3p.kar.hibernate.GebruikerDataOwnerRights;
import nl.b3p.kar.hibernate.Role;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.stripesstuff.stripersist.EntityTypeConverter;
import org.stripesstuff.stripersist.Stripersist;

/**
 * Stripes klasse waarmee gebruikers kunnen worden getoond en bewerkt.
 * 
 * @author Matthijs Laan
 */
@StrictBinding
@UrlBinding("/action/beheer/gebruikers")
public class GebruikersActionBean implements ActionBean, ValidationErrorHandler {
    private static final String JSP = "/WEB-INF/jsp/beheer/gebruikers.jsp";
    
    private ActionBeanContext context;
    
    private List<Gebruiker> gebruikers;
    
    private List<Role> allRoles;
    
    private List<DataOwner> dataOwners;
    
    private String dataOwnersJson;
    
    @Validate(required=true)
    private Integer role;
    
    @Validate(maxlength=50)
    private String password;
    
    @Validate(converter = EntityTypeConverter.class)
    @ValidateNestedProperties({
        @Validate(field="username", required=true, maxlength=30),
        @Validate(field="fullname", maxlength=50),
        @Validate(field="email", converter=EmailTypeConverter.class, maxlength=50),
        @Validate(field="phone", maxlength=15)
    })          
    private Gebruiker gebruiker;
    
    @Validate
    private List<String> dataOwnersEditable = new ArrayList();
    
    @Validate
    private List<String> dataOwnersValidatable = new ArrayList();

    //<editor-fold defaultstate="collapsed" desc="getters en setters">
    public ActionBeanContext getContext() {
        return context;
    }
    
    public void setContext(ActionBeanContext context) {
        this.context = context;
    }
    
    /**
     *
     * @return
     */
    public List<Gebruiker> getGebruikers() {
        return gebruikers;
    }
    
    /**
     *
     * @param gebruikers
     */
    public void setGebruikers(List<Gebruiker> gebruikers) {
        this.gebruikers = gebruikers;
    }
    
    /**
     *
     * @return
     */
    public Gebruiker getGebruiker() {
        return gebruiker;
    }
    
    /**
     *
     * @param gebruiker
     */
    public void setGebruiker(Gebruiker gebruiker) {
        this.gebruiker = gebruiker;
    }
    
    /**
     *
     * @return
     */
    public String getPassword() {
        return password;
    }
    
    /**
     *
     * @param password
     */
    public void setPassword(String password) {
        this.password = password;
    }
    
    /**
     *
     * @return
     */
    public List<Role> getAllRoles() {
        return allRoles;
    }
    
    /**
     *
     * @param allRoles
     */
    public void setAllRoles(List<Role> allRoles) {
        this.allRoles = allRoles;
    }
    
    /**
     *
     * @return
     */
    public List<DataOwner> getDataOwners() {
        return dataOwners;
    }
    
    /**
     *
     * @param dataOwners
     */
    public void setDataOwners(List<DataOwner> dataOwners) {
        this.dataOwners = dataOwners;
    }
    
    /**
     *
     * @return
     */
    public String getDataOwnersJson() {
        return dataOwnersJson;
    }
    
    /**
     *
     * @param dataOwnersJson
     */
    public void setDataOwnersJson(String dataOwnersJson) {
        this.dataOwnersJson = dataOwnersJson;
    }
    
    /**
     *
     * @return
     */
    public List<String> getDataOwnersEditable() {
        return dataOwnersEditable;
    }
    
    /**
     *
     * @param dataOwnersEditable
     */
    public void setDataOwnersEditable(List<String> dataOwnersEditable) {
        this.dataOwnersEditable = dataOwnersEditable;
    }
    
    /**
     *
     * @return
     */
    public List<String> getDataOwnersValidatable() {
        return dataOwnersValidatable;
    }
    
    /**
     *
     * @param dataOwnersValidatable
     */
    public void setDataOwnersValidatable(List<String> dataOwnersValidatable) {
        this.dataOwnersValidatable = dataOwnersValidatable;
    }
    
    /**
     *
     * @return
     */
    public Integer getRole() {
        return role;
    }
    
    /**
     *
     * @param role
     */
    public void setRole(Integer role) {
        this.role = role;
    }
    //</editor-fold>
    
    /**
     * Methode bouwt lijsten op voor gebruikers en dataowners en rechten voor
     * gebruik in edit pagina.
     */
    @Before(stages = LifecycleStage.BindingAndValidation)
    public void loadLists() {
        gebruikers = Stripersist.getEntityManager().createQuery("from Gebruiker order by id").getResultList();
        allRoles = Stripersist.getEntityManager().createQuery("from Role order by role").getResultList();
        dataOwners = Stripersist.getEntityManager().createQuery("from DataOwner order by code").getResultList();
        
        JSONArray ja = new JSONArray();
        for(DataOwner dao: dataOwners) {
            JSONObject jo = new JSONObject();
            try {
                jo.put("id", dao.getCode());
                jo.put("code", dao.getCode());
                jo.put("name", dao.getOmschrijving());
                ja.put(jo);
            } catch(JSONException je) {
            }
        }        
        dataOwnersJson = ja.toString();
    }

    public Resolution handleValidationErrors(ValidationErrors errors) throws Exception {
        loadGebruikerLists();
        return context.getSourcePageResolution();
    }    
    
    /**
     * Methode bouwt per gebruiker alle onderliggende informatie op.
     */
    @After
    public void loadGebruikerLists() {
        if(gebruiker != null) {
            dataOwnersEditable = new ArrayList();
            dataOwnersValidatable = new ArrayList();

            for(GebruikerDataOwnerRights dor: gebruiker.getDataOwnerRights().values()) {
                if(dor.isEditable()) {
                    dataOwnersEditable.add(dor.getDataOwner().getCode());
                }
                if(dor.isValidatable()) {
                    dataOwnersValidatable.add(dor.getDataOwner().getCode());
                }
            }
        }
    }
    
    /**
     * Default resolution voor het alleen tonen van de lijst met gebruikers.
     * 
     */
    @DefaultHandler
    @DontBind
    public Resolution list() {
        return new ForwardResolution(JSP);
    }

    /**
     * Resolution voor het bewerken van een gebruiker.
     * 
     */
    @DontValidate
    public Resolution edit() {
        
        if(gebruiker != null) {
            String rolename = Role.GEBRUIKER;
            for(Role r: (Set<Role>)gebruiker.getRoles()) {
                if(r.getRole().equals(Role.BEHEERDER)) {
                    rolename = Role.BEHEERDER;
                }
            }
            role = ((Role)Stripersist.getEntityManager().createQuery("from Role where role = :r").setParameter("r", rolename).getSingleResult()).getId();
        }
        
        return new ForwardResolution(JSP);
    }
    
    /**
     * Resolution voor het toevoegen van een gebruiker.
     * 
     */
    @DontValidate
    public Resolution add() {
        gebruiker = new Gebruiker();
        return new ForwardResolution(JSP);
    }
    
    /**
     * Resolution die een gebruiker opslaat.
     *
     */
    public Resolution save() throws Exception {
    
        EntityManager em = Stripersist.getEntityManager();
        
        /* check of username al bestaat */
        Gebruiker bestaandeUsername = null;
        try {
            bestaandeUsername = (Gebruiker)em.createQuery("from Gebruiker g where g.id <> :editingId and lower(g.username) = lower(:username)")
                    .setParameter("editingId", gebruiker.getId() == null ? new Integer(-1) : new Integer(gebruiker.getId()))
                    .setParameter("username", gebruiker.getUsername())
                    .getSingleResult();
        } catch(NoResultException nre) {
            /* debiele API */
        }
        if(bestaandeUsername != null) {
            getContext().getValidationErrors().addGlobalError(new SimpleError("Gebruikersnaam is al in gebruik"));
            return context.getSourcePageResolution();
        }
        
        if(password != null) {
            gebruiker.changePassword(context.getRequest(), password);
            password = null;
        }
        
        gebruiker.getRoles().clear();        
        gebruiker.getRoles().add(em.find(Role.class, role));
        
        gebruiker.getDataOwnerRights().clear();
        em.flush();
        /* XXX werkt niet meer met ID
        for(String daoId: dataOwnersEditable) {
            DataOwner dao = em.find(DataOwner.class, daoId);
            gebruiker.setDataOwnerRight(dao, Boolean.TRUE, null);
        }
        for(String daoId: dataOwnersValidatable) {
            DataOwner dao = em.find(DataOwner.class, daoId);
            gebruiker.setDataOwnerRight(dao, null, Boolean.TRUE);
        } */       
        
        em.persist(gebruiker);
        em.getTransaction().commit();
        
        loadLists();
        
        getContext().getMessages().add(new SimpleMessage("Gebruikersgegevens opgeslagen"));
               
        return new RedirectResolution(this.getClass(), "edit").addParameter("gebruiker", gebruiker.getId()).flash(this);
    }
    
    /**
     * Resolution die een gebruiker verwijdert.
     * 
     */
    public Resolution delete() {
        EntityManager em = Stripersist.getEntityManager();
        em.remove(gebruiker);
        em.getTransaction().commit();
        getContext().getMessages().add(new SimpleMessage("Gebruiker is verwijderd"));
        gebruiker = null;
        return new RedirectResolution(this.getClass(), "list").flash(this);
    }
}
