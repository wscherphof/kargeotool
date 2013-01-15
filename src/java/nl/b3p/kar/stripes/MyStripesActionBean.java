package nl.b3p.kar.stripes;

import java.util.List;
import javax.persistence.EntityManager;
import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.validation.Validate;
import nl.b3p.kar.hibernate.Gebruiker;
import nl.b3p.kar.hibernate.VehicleType;
import nl.b3p.transmodel.ActivationGroup;
import org.apache.catalina.tribes.util.Arrays;
import org.stripesstuff.stripersist.EntityTypeConverter;
import org.stripesstuff.stripersist.Stripersist;

/**
 *
 * @author Matthijs Laan
 */
@StrictBinding
public class MyStripesActionBean implements ActionBean {
    private ActionBeanContext context;
    
    @Validate(converter = EntityTypeConverter.class)
    private Gebruiker g;
    
    @Validate(converter = EntityTypeConverter.class)
    private ActivationGroup ag;

    public ActionBeanContext getContext() {
        return context;
    }

    public void setContext(ActionBeanContext context) {
        this.context = context;
    }

    public Gebruiker getG() {
        return g;
    }

    public void setG(Gebruiker g) {
        this.g = g;
    }

    public ActivationGroup getAg() {
        return ag;
    }

    public void setAg(ActivationGroup ag) {
        this.ag = ag;
    }
    
    public Resolution test() {
        
        EntityManager em = Stripersist.getEntityManager();
        
        List<VehicleType> vehicleTypes = em.createQuery("from VehicleType order by nummer").getResultList();
                
        return new StreamingResolution("text/plain", "It works! Gebruiker: " + (g == null ? "-" : g.getFullname()) + ", " + (ag == null ? "-" : ag.getUpdateTime()) + "\nVehicle types: " + vehicleTypes);
        
    }
    
}
