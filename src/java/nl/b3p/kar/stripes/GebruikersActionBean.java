package nl.b3p.kar.stripes;

import java.util.List;
import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.validation.EmailTypeConverter;
import net.sourceforge.stripes.validation.Validate;
import net.sourceforge.stripes.validation.ValidateNestedProperties;
import nl.b3p.kar.hibernate.Gebruiker;
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
    
    @ValidateNestedProperties({
        @Validate(field="username", required=true, maxlength=30),
        @Validate(field="fullName", maxlength=50),
        @Validate(field="email", converter=EmailTypeConverter.class, maxlength=50),
        @Validate(field="phone", maxlength=15)
    })      
    private List<Gebruiker> gebruikers;
    
    @Validate(maxlength=50)
    private String password;
    
    @Validate(converter = EntityTypeConverter.class)
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
    
    @After
    public void loadGebruikersList() {
        gebruikers = Stripersist.getEntityManager().createQuery("from Gebruiker order by id").getResultList();
    }
    
    @DefaultHandler
    public Resolution list() {
        return new ForwardResolution(JSP);
    }
}
