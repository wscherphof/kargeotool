package nl.b3p.kar.stripes;

import java.io.StringReader;
import java.util.Set;
import javax.persistence.EntityManager;
import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.validation.Validate;
import nl.b3p.kar.hibernate.*;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.stripesstuff.stripersist.Stripersist;

/**
 *
 * @author Meine Toonen meinetoonen@b3partners.nl
 */
@StrictBinding
@UrlBinding("/action/viewer/editor")
public class EditorActionBean implements ActionBean {
    private static final Log log = LogFactory.getLog(EditorActionBean.class);

    private static final String JSP = "/WEB-INF/jsp/viewer/editor2.jsp";
    private ActionBeanContext context;
    private boolean magWalapparaatMaken;
    
    @Validate
    private Integer karAddress;
    
    @Validate
    private RoadsideEquipment2 rseq;
    
    @Validate
    private JSONObject layers;

    @DefaultHandler
    public Resolution view() throws Exception {

        EntityManager em = Stripersist.getEntityManager();
        Gebruiker principal = (Gebruiker) context.getRequest().getUserPrincipal();
        Gebruiker g = em.find(Gebruiker.class, principal.getId());
        boolean isBeheerder = g.isBeheerder();

        Set s = g.getEditableDataOwners();
        if (s.size() >= 1 || isBeheerder) {
            magWalapparaatMaken = true;
        } else {
            magWalapparaatMaken = false;
        }
        return new ForwardResolution(JSP);
    }
    
    public Resolution gfi(){
        return new StreamingResolution("application/json", "error: no objects found");
    }
    
    public Resolution rseqInfo2() throws Exception {
        EntityManager em = Stripersist.getEntityManager();
        
        JSONObject info = new JSONObject();
        info.put("success", Boolean.FALSE);
        try {
            RoadsideEquipment2 rseq2;
            
            if(rseq != null) {
                rseq2 = rseq;
            } else {
                rseq2 = (RoadsideEquipment2)em.createQuery("from RoadsideEquipment2 where karAddress = :un")
                    .setParameter("un", karAddress)
                    .getSingleResult();
            }
        
            info.put("rseq",rseq2.getRseqGeoJSON());
            info.put("points",rseq2.getPointsGeoJSON());
            info.put("success", Boolean.TRUE);
        } catch(Exception e) {
            log.error("rseqInfo2 exception", e);
            info.put("error", ExceptionUtils.getMessage(e));            
        }
        return new StreamingResolution("application/json",  new StringReader(info.toString(4)));
    }
    
    public Resolution rseqJSON() throws Exception {
        EntityManager em = Stripersist.getEntityManager();
        
        JSONObject info = new JSONObject();
        info.put("success", Boolean.FALSE);
        try {
            RoadsideEquipment2 rseq2;
            
            if(rseq != null) {
                rseq2 = rseq;
            } else {
                rseq2 = (RoadsideEquipment2)em.createQuery("from RoadsideEquipment2 where karAddress = :a")
                    .setParameter("a", karAddress)
                    .getSingleResult();
            }

            info.put("roadsideEquipment",rseq2.getJSON());
            
            info.put("success", Boolean.TRUE);
        } catch(Exception e) {
            log.error("rseqJSON exception", e);
            info.put("error", ExceptionUtils.getMessage(e));            
        }
        return new StreamingResolution("application/json",  new StringReader(info.toString(4)));
    }
    
    // <editor-fold desc="Getters and Setters">
    public ActionBeanContext getContext() {
        return context;
    }

    public void setContext(ActionBeanContext context) {
        this.context = context;
    }

    public Integer getKarAddress() {
        return karAddress;
    }

    public void setKarAddress(Integer karAddress) {
        this.karAddress = karAddress;
    }

    public JSONObject getLayers() {
        return layers;
    }

    public void setLayers(JSONObject layers) {
        this.layers = layers;
    }

    public boolean isMagWalapparaatMaken() {
        return magWalapparaatMaken;
    }

    public void setMagWalapparaatMaken(boolean magWalapparaatMaken) {
        this.magWalapparaatMaken = magWalapparaatMaken;
    }

    public RoadsideEquipment2 getRseq() {
        return rseq;
    }

    public void setRseq(RoadsideEquipment2 rseq) {
        this.rseq = rseq;
    }
    // </editor-fold>
}
