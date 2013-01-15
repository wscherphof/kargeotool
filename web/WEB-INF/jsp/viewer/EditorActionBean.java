package nl.b3p.kar.stripes;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
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

    public Resolution getIdentifyTree() throws Exception {
        List objects = new ArrayList();

        objects.addAll(getFeatureListEntities(layers, "walapparatuur", RoadsideEquipment.class));
        objects.addAll(getFeatureListEntities(layers, "signaalgroepen", ActivationGroup.class));
        objects.addAll(getFeatureListEntities(layers, "triggerpunten", Activation.class));

        if (objects.isEmpty()) {
            return new StreamingResolution("application/json", "error: no objects found");
        }

        Envelope envelope = new Envelope();
        for (Iterator it = objects.iterator(); it.hasNext();) {
            Object obj = it.next();
            Coordinate c = getObjectCoordinate(obj);
            if (c != null) {
                envelope.expandToInclude(c);
            }
        }
        JSONObject info = new JSONObject();
        if (!envelope.isNull()) {
            info.put("envelope", "{minX: " + envelope.getMinX() + ", maxX: " + envelope.getMaxX()
                    + ", minY: " + envelope.getMinY() + ", maxY: " + envelope.getMaxY() + "}");
        }
        if (objects.size() == 1) {
            info.put("selectedObject", ((EditorTreeObject) objects.get(0)).serializeToJson(context.getRequest(), false));
        }
        info.put("tree", buildObjectTree(objects));
        return new StreamingResolution("application/json", info.toString());
    }

    private List getFeatureListEntities(JSONObject layers, String layerName, Class entityClass) throws Exception {
        EntityManager em = Stripersist.getEntityManager();

        if (!layers.has(layerName)) {
            return new ArrayList();
        }
        JSONArray features = layers.getJSONArray(layerName);
        List ids = new ArrayList();
        for (int i = 0; i < features.length(); i++) {
            JSONObject feature = features.getJSONObject(i);
            ids.add(feature.getInt("id"));
        }
        List objects = new ArrayList();
        if (!ids.isEmpty()) {
            objects = em.createQuery("from " + entityClass.getName() + " where id in (:ids)")
                    .setParameter("ids", ids)
                    .getResultList();
        }
        return objects;
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
    // </editor-fold>
}
