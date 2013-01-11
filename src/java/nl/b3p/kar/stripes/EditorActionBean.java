package nl.b3p.kar.stripes;

import com.vividsolutions.jts.geom.Coordinate;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.validation.ValidationErrorHandler;
import net.sourceforge.stripes.validation.ValidationErrors;
import nl.b3p.kar.hibernate.Gebruiker;
import nl.b3p.kar.struts.EditorTreeObject;
import nl.b3p.transmodel.Activation;
import nl.b3p.transmodel.ActivationGroup;
import nl.b3p.transmodel.RoadsideEquipment;
import org.json.JSONArray;
import org.json.JSONObject;
import org.stripesstuff.stripersist.Stripersist;

/**
 *
 * @author Meine Toonen meinetoonen@b3partners.nl
 */
@StrictBinding
@UrlBinding("/action/viewer/editor")
public class EditorActionBean implements ActionBean, ValidationErrorHandler {

    private static final String JSP = "/WEB-INF/jsp/viewer/editor2.jsp";
    private ActionBeanContext context;
    private boolean magWalapparaatMaken;

    @DefaultHandler
    public Resolution view() throws Exception {

        EntityManager em = Stripersist.getEntityManager();
        Gebruiker principal = (Gebruiker)context.getRequest().getUserPrincipal();
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
    
    public Resolution getObjectTree(String type, String id) throws Exception {
        Class clazz = null;
        if("a".equals(type)) {
            clazz = Activation.class;
        } else if("ag".equals(type)) {
            clazz = ActivationGroup.class;
        } else if("rseq".equals(type)) {
            clazz = RoadsideEquipment.class;
        }
        if(clazz == null) {
            return new StreamingResolution("application/json","error: invalid object type");
        }
        Integer idObj;
        try {
            idObj = new Integer(Integer.parseInt(id));
        } catch(NumberFormatException nfe) {
            return new StreamingResolution("application/json","error: invalid id");
        }

        Object object = Stripersist.getEntityManager().find(clazz, idObj);
        if(object == null) {
            return new StreamingResolution("application/json","error: object " + type + ":" + idObj + " not found");
        }
        JSONObject info = new JSONObject();
        info.put("object", type + ":" + idObj);
        Coordinate c = getObjectCoordinate(object);
        if(c != null) {
            info.put("envelope", "{minX: " + c.x + ", maxX: " + c.x
                    + ", minY: " + c.y + ", maxY: " + c.y + "}");
        }
        info.put("tree", buildObjectTree(Arrays.asList(new Object[] {object}), context.getRequest()));
        
        return new StreamingResolution("application/json", new StringReader(info.toString(4)));
        
    }
    
    private JSONObject buildObjectTree(List objects, HttpServletRequest request) throws Exception {
        List roots = new ArrayList();

        for(Iterator it = objects.iterator(); it.hasNext();) {
            Object root = it.next();
            if(root instanceof Activation) {
                Activation a = (Activation)root;
                if(a.getActivationGroup() != null) {
                    root = a.getActivationGroup();
                }
            }
            if(root instanceof ActivationGroup) {
                ActivationGroup ag = (ActivationGroup)root;
                if(ag.getRoadsideEquipment() != null) {
                    root = ag.getRoadsideEquipment();
                }
            }
            if(!roots.contains(root)) {
                roots.add(root);
            }
        }

        JSONObject root = new JSONObject();
        root.put("id", "root");
        JSONArray children = new JSONArray();
        root.put("children", children);
        for(Iterator it = roots.iterator(); it.hasNext();) {
            children.put(((EditorTreeObject)it.next()).serializeToJson(context.getRequest()));
        }
        return root;
    }

    
    private static Coordinate getObjectCoordinate(Object obj) {
        if(obj == null) {
            return null;
        }
        if(obj instanceof Activation) {
            return ((Activation)obj).getLocation() == null ? null : ((Activation)obj).getLocation().getCoordinate();
        }
        if(obj instanceof ActivationGroup) {
            return ((ActivationGroup)obj).getStopLineLocation() == null ? null : ((ActivationGroup)obj).getStopLineLocation().getCoordinate();
        }
        if(obj instanceof RoadsideEquipment) {
            return ((RoadsideEquipment)obj).getLocation() == null ? null : ((RoadsideEquipment)obj).getLocation().getCoordinate();
        }
        return null;
    }

    public Resolution handleValidationErrors(ValidationErrors errors) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    // <editor-fold desc="Getters and Setters">
    public ActionBeanContext getContext() {
        return context;
    }

    public void setContext(ActionBeanContext context) {
        this.context = context;
    }

    public boolean isMagWalapparaatMaken() {
        return magWalapparaatMaken;
    }

    public void setMagWalapparaatMaken(boolean magWalapparaatMaken) {
        this.magWalapparaatMaken = magWalapparaatMaken;
    }
    // </editor-fold>
}
