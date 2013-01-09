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
import nl.b3p.kar.hibernate.Gebruiker;
import nl.b3p.kar.hibernate.GebruikerDataOwnerRights;
import nl.b3p.kar.hibernate.Role;
import nl.b3p.transmodel.DataOwner;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.stripesstuff.stripersist.EntityTypeConverter;
import org.stripesstuff.stripersist.Stripersist;

/**
 *
 * @author Matthijs Laan
 */
@StrictBinding
@UrlBinding("/action/beheer/gebruikers")
public class GebruikersActionBean implements ActionBean, ValidationErrorHandler {
    private static final String JSP = "/WEB-INF/jsp/beheer/gebruikers2.jsp";
    
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

    public ActionBeanContext getContext() {
        return context;
    }

    public void setContext(ActionBeanContext context) {
        this.context = context;
    }

    public List<Gebruiker> getGebruikers() {
        return gebruikers;
    }

    public void setGebruikers(List<Gebruiker> gebruikers) {
        this.gebruikers = gebruikers;
    }

    public Gebruiker getGebruiker() {
        return gebruiker;
    }

    public void setGebruiker(Gebruiker gebruiker) {
        this.gebruiker = gebruiker;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Role> getAllRoles() {
        return allRoles;
    }

    public void setAllRoles(List<Role> allRoles) {
        this.allRoles = allRoles;
    }

    public List<DataOwner> getDataOwners() {
        return dataOwners;
    }

    public void setDataOwners(List<DataOwner> dataOwners) {
        this.dataOwners = dataOwners;
    }

    public String getDataOwnersJson() {
        return dataOwnersJson;
    }

    public void setDataOwnersJson(String dataOwnersJson) {
        this.dataOwnersJson = dataOwnersJson;
    }

    public List<String> getDataOwnersEditable() {
        return dataOwnersEditable;
    }

    public void setDataOwnersEditable(List<String> dataOwnersEditable) {
        this.dataOwnersEditable = dataOwnersEditable;
    }

    public List<String> getDataOwnersValidatable() {
        return dataOwnersValidatable;
    }

    public void setDataOwnersValidatable(List<String> dataOwnersValidatable) {
        this.dataOwnersValidatable = dataOwnersValidatable;
    }
    
    public Integer getRole() {
        return role;
    }

    public void setRole(Integer role) {
        this.role = role;
    }
    
    @Before(stages = LifecycleStage.BindingAndValidation)
    public void loadLists() {
        gebruikers = Stripersist.getEntityManager().createQuery("from Gebruiker order by id").getResultList();
        allRoles = Stripersist.getEntityManager().createQuery("from Role order by role").getResultList();
        dataOwners = Stripersist.getEntityManager().createQuery("from DataOwner where type = :type order by name")
                .setParameter("type", DataOwner.TYPE_ROOW)
                .getResultList();
        
        JSONArray ja = new JSONArray();
        for(DataOwner dao: dataOwners) {
            JSONObject jo = new JSONObject();
            try {
                jo.put("id", dao.getId());
                jo.put("code", dao.getCode());
                jo.put("name", dao.getName());
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
    
    @After
    public void loadGebruikerLists() {
        if(gebruiker != null) {
            dataOwnersEditable = new ArrayList();
            dataOwnersValidatable = new ArrayList();

            for(GebruikerDataOwnerRights dor: gebruiker.getDataOwnerRights().values()) {
                if(dor.isEditable()) {
                    dataOwnersEditable.add(dor.getDataOwner().getId() + "");
                }
                if(dor.isValidatable()) {
                    dataOwnersValidatable.add(dor.getDataOwner().getId() + "");
                }
            }
        }
    }
    
    @DefaultHandler
    @DontBind
    public Resolution list() {
        return new ForwardResolution(JSP);
    }

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
    
    @DontValidate
    public Resolution add() {
        gebruiker = new Gebruiker();
        return new ForwardResolution(JSP);
    }
    
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
        for(String daoId: dataOwnersEditable) {
            DataOwner dao = em.find(DataOwner.class, Integer.parseInt(daoId));
            gebruiker.setDataOwnerRight(dao, Boolean.TRUE, null);
        }
        for(String daoId: dataOwnersValidatable) {
            DataOwner dao = em.find(DataOwner.class, Integer.parseInt(daoId));
            gebruiker.setDataOwnerRight(dao, null, Boolean.TRUE);
        }        
        
        em.persist(gebruiker);
        em.getTransaction().commit();
        
        loadLists();
        
        getContext().getMessages().add(new SimpleMessage("Gebruikersgegevens opgeslagen"));
               
        return new RedirectResolution(this.getClass(), "edit").addParameter("gebruiker", gebruiker.getId()).flash(this);
    }
    
    public Resolution delete() {
        EntityManager em = Stripersist.getEntityManager();
        em.remove(gebruiker);
        em.getTransaction().commit();
        getContext().getMessages().add(new SimpleMessage("Gebruiker is verwijderd"));
        gebruiker = null;
        return new RedirectResolution(this.getClass(), "list").flash(this);
    }
}
