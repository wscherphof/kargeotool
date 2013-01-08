package nl.b3p.kar.stripes;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.controller.LifecycleStage;
import net.sourceforge.stripes.validation.EmailTypeConverter;
import net.sourceforge.stripes.validation.Validate;
import net.sourceforge.stripes.validation.ValidateNestedProperties;
import nl.b3p.kar.hibernate.Gebruiker;
import nl.b3p.kar.hibernate.Role;
import org.stripesstuff.stripersist.EntityTypeConverter;
import org.stripesstuff.stripersist.Stripersist;

/**
 *
 * @author Matthijs Laan
 */
@StrictBinding
@UrlBinding("/action/beheer/gebruikers")
public class GebruikersActionBean implements ActionBean {
    private static final String JSP = "/WEB-INF/jsp/beheer/gebruikers2.jsp";
    
    private ActionBeanContext context;
    
    private List<Gebruiker> gebruikers;
    
    private List<Role> allRoles;
    
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

    public Integer getRole() {
        return role;
    }

    public void setRole(Integer role) {
        this.role = role;
    }
    
    @Before(stages= LifecycleStage.BindingAndValidation)
    public void loadLists() {
        gebruikers = Stripersist.getEntityManager().createQuery("from Gebruiker order by id").getResultList();
        allRoles = Stripersist.getEntityManager().createQuery("from Role order by role").getResultList();
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
    
    public Resolution save() {
        
        EntityManager em = Stripersist.getEntityManager();
        
        gebruiker.getRoles().clear();        
        gebruiker.getRoles().add(em.find(Role.class, role));
        
        em.persist(gebruiker);
        em.getTransaction().commit();
        
        loadLists();
        
        getContext().getMessages().add(new SimpleMessage("Gebruikersgegevens opgeslagen"));
               
        return new RedirectResolution(this.getClass(), "edit").addParameter("gebruiker", gebruiker.getId()).flash(this);
    }
}
