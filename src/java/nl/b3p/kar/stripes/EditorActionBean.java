package nl.b3p.kar.stripes;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.validation.Validate;
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
    @Validate
    private Integer unitNumber;

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

    public Resolution rseqUnitNumberTree() throws Exception {

        List rseqs = Stripersist.getEntityManager().createQuery("from RoadsideEquipment where unitNumber = :n")
                .setParameter("n", unitNumber)
                .getResultList();
        if (rseqs.isEmpty()) {
            return new StreamingResolution("application/json", "error: Geen walapparatuur met dit nummer gevonden");
        }

        JSONObject info = new JSONObject();

        Envelope envelope = new Envelope();
        for (Iterator it = rseqs.iterator(); it.hasNext();) {
            Object obj = it.next();
            Coordinate c = getObjectCoordinate(obj);
            if (c != null) {
                envelope.expandToInclude(c);
            }
        }
        if (!envelope.isNull()) {
            info.put("envelope", "{minX: " + envelope.getMinX() + ", maxX: " + envelope.getMaxX()
                    + ", minY: " + envelope.getMinY() + ", maxY: " + envelope.getMaxY() + "}");
        }
        if (rseqs.size() == 1) {
            info.put("selectedObject", ((EditorTreeObject) rseqs.get(0)).serializeToJson(context.getRequest(), false));
        }
        info.put("tree", buildObjectTree(rseqs));
        return new StreamingResolution("application/json", info.toString());
    }

    private JSONObject buildObjectTree(List objects) throws Exception {
        List roots = new ArrayList();

        for (Iterator it = objects.iterator(); it.hasNext();) {
            Object root = it.next();
            if (root instanceof Activation) {
                Activation a = (Activation) root;
                if (a.getActivationGroup() != null) {
                    root = a.getActivationGroup();
                }
            }
            if (root instanceof ActivationGroup) {
                ActivationGroup ag = (ActivationGroup) root;
                if (ag.getRoadsideEquipment() != null) {
                    root = ag.getRoadsideEquipment();
                }
            }
            if (!roots.contains(root)) {
                roots.add(root);
            }
        }

        JSONObject root = new JSONObject();
        root.put("id", "root");
        JSONArray children = new JSONArray();
        root.put("children", children);
        for (Iterator it = roots.iterator(); it.hasNext();) {
            children.put(((EditorTreeObject) it.next()).serializeToJson(context.getRequest()));
        }
        return root;
    }

    private static Coordinate getObjectCoordinate(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Activation) {
            return ((Activation) obj).getLocation() == null ? null : ((Activation) obj).getLocation().getCoordinate();
        }
        if (obj instanceof ActivationGroup) {
            return ((ActivationGroup) obj).getStopLineLocation() == null ? null : ((ActivationGroup) obj).getStopLineLocation().getCoordinate();
        }
        if (obj instanceof RoadsideEquipment) {
            return ((RoadsideEquipment) obj).getLocation() == null ? null : ((RoadsideEquipment) obj).getLocation().getCoordinate();
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

    public Integer getUnitNumber() {
        return unitNumber;
    }

    public void setUnitNumber(Integer unitNumber) {
        this.unitNumber = unitNumber;
    }

    public boolean isMagWalapparaatMaken() {
        return magWalapparaatMaken;
    }

    public void setMagWalapparaatMaken(boolean magWalapparaatMaken) {
        this.magWalapparaatMaken = magWalapparaatMaken;
    }
    // </editor-fold>
}
