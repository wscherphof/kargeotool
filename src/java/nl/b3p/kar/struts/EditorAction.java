package nl.b3p.kar.struts;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import nl.b3p.commons.struts.ExtendedMethodProperties;
import nl.b3p.kar.hibernate.KarPunt;
import nl.b3p.kar.persistence.MyEMFDatabase;
import nl.b3p.transmodel.Activation;
import nl.b3p.transmodel.ActivationGroup;
import nl.b3p.transmodel.RoadsideEquipment;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.validator.DynaValidatorForm;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class EditorAction extends BaseDatabaseAction {
    private static final Log log = LogFactory.getLog(EditorAction.class);
    
    @Override
    protected Map getActionMethodPropertiesMap() {
        Map map = new HashMap();
        map.put("editor", new ExtendedMethodProperties("editor"));
        return map;
    }

    public ActionForward unspecified(ActionMapping mapping, DynaValidatorForm dynaForm, HttpServletRequest request, HttpServletResponse response) throws Exception {
        return mapping.findForward("editor");
    }

    public static Object executeInTransaction(String method, String... args) throws Exception {
        Object identity = null;
        try {
            identity = MyEMFDatabase.createEntityManager(MyEMFDatabase.MAIN_EM);
            EntityManager em = getEntityManager();
            EntityTransaction tx = em.getTransaction();
            tx.begin();

            try {
                Object result = EditorAction.class.getMethod(method, new String[] {}.getClass()).invoke(null, (Object)args);
                tx.commit();
                return result;
            } catch (Exception exception) {
                log.error("Exception occured" + (tx.isActive() ? ", rollback" : "tx not active"), exception);
                if(tx.isActive()) {
                    tx.rollback();
                }
                String msg = null;
                Throwable e = exception;
                if(exception instanceof InvocationTargetException) {
                    e = exception.getCause();
                }
                if (e instanceof org.hibernate.JDBCException) {
                    msg = "error: " + e.getMessage();
                    SQLException sqle = ((org.hibernate.JDBCException) e).getSQLException();
                    msg = msg + ": " + sqle;
                    SQLException nextSqlE = sqle.getNextException();
                    if (nextSqlE != null) {
                        msg = msg + ": " + nextSqlE;
                    }
                } else if (e instanceof java.sql.SQLException) {
                    msg = "error: " + e.getMessage();
                    SQLException nextSqlE = ((java.sql.SQLException) e).getNextException();
                    if (nextSqlE != null) {
                        msg = msg + ": " + nextSqlE;
                    }
                } else {
                    msg = "error: Exception " + e.getClass().getName() + ": " + e.getMessage();
                }
                return msg;
            }
        } catch (Throwable e) {
            log.error("Exception occured while getting EntityManager: ", e);
            return "error: exception " + e.getClass().getName() + ": " + e.getMessage();
        } finally {
            log.debug("Closing entity manager .....");
            MyEMFDatabase.closeEntityManager(identity, MyEMFDatabase.MAIN_EM);
        }
    }

    public static String getObjectTree(String type, String id) throws Exception {
        return (String)executeInTransaction("getObjectTreeFromDb", type, id);
    }

    public static String getIdentifyTree(String layers) throws Exception {
        return (String)executeInTransaction("getIdentifyTreeFromDb", layers);
    }

    public static String getObjectTreeFromDb(String... args) throws Exception {
        String type = args[0];
        String id = args[1];
        Class clazz = null;
        if("a".equals(type)) {
            clazz = Activation.class;
        } else if("ag".equals(type)) {
            clazz = ActivationGroup.class;
        } else if("rseq".equals(type)) {
            clazz = RoadsideEquipment.class;
        }
        if(clazz == null) {
            return "error: invalid object type";
        }
        Integer idObj;
        try {
            idObj = new Integer(Integer.parseInt(id));
        } catch(NumberFormatException nfe) {
            return "error: invalid id";
        }

        Object object = getEntityManager().find(clazz, idObj);
        if(object == null) {
            return "error: object " + type + ":" + idObj + " not found";
        }
        JSONObject info = new JSONObject();
        info.put("object", type + ":" + idObj);
        Coordinate c = getObjectCoordinate(object);
        if(c != null) {
            info.put("envelope", "{minX: " + c.x + ", maxX: " + c.x
                    + ", minY: " + c.y + ", maxY: " + c.y + "}");
        }
        info.put("tree", buildObjectTree(Arrays.asList(new Object[] {object})));
        return info.toString();
    }

    public static String getIdentifyTreeFromDb(String... args) throws Exception {

        JSONObject layers = new JSONObject(args[0]);

        List objects = new ArrayList();

        objects.addAll(getFeatureListEntities(layers, "walapparatuur", RoadsideEquipment.class));
        objects.addAll(getFeatureListEntities(layers, "signaalgroepen", ActivationGroup.class));
        objects.addAll(getFeatureListEntities(layers, "triggerpunten", Activation.class));

        if(objects.isEmpty()) {
            return "error: no objects found";
        }

        Envelope envelope = new Envelope();
        for(Iterator it = objects.iterator(); it.hasNext();) {
            Object obj = it.next();
            Coordinate c = getObjectCoordinate(obj);
            if(c != null) {
                envelope.expandToInclude(c);
            }
        }
        JSONObject info = new JSONObject();
        if(!envelope.isNull()) {
            info.put("envelope", "{minX: " + envelope.getMinX() + ", maxX: " + envelope.getMaxX()
                    + ", minY: " + envelope.getMinY() + ", maxY: " + envelope.getMaxY() + "}");
        }
        if(objects.size() == 1) {
            info.put("selectedObject", ((EditorTreeObject)objects.get(0)).serializeToJson(false));
        }
        info.put("tree", buildObjectTree(objects));
        return info.toString();
    }

    private static List getFeatureListEntities(JSONObject layers, String layerName, Class entityClass) throws Exception {
        EntityManager em = getEntityManager();

        if(!layers.has(layerName)) {
            return new ArrayList();
        }
        JSONArray features = layers.getJSONArray(layerName);
        List ids = new ArrayList();
        for(int i = 0; i < features.length(); i++) {
            JSONObject feature = features.getJSONObject(i);
            ids.add(feature.getInt("id"));
        }
        List objects = new ArrayList();
        if(!ids.isEmpty()) {
            objects = em.createQuery("from " + entityClass.getName() + " where id in (:ids)")
                    .setParameter("ids", ids)
                    .getResultList();
        }
        return objects;
    }

    private static Coordinate getObjectCoordinate(Object obj) {
        if(obj == null) {
            return null;
        }
        if(obj instanceof Activation) {
            return ((Activation)obj).getPoint() == null ? null : ((Activation)obj).getPoint().getGeom().getCoordinate();
        }
        if(obj instanceof ActivationGroup) {
            return ((ActivationGroup)obj).getPoint() == null ? null : ((ActivationGroup)obj).getPoint().getGeom().getCoordinate();
        }
        if(obj instanceof RoadsideEquipment) {
            return ((RoadsideEquipment)obj).getPoint() == null ? null : ((RoadsideEquipment)obj).getPoint().getGeom().getCoordinate();
        }
        return null;
    }

    private static JSONObject buildObjectTree(List objects) throws Exception {

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
            children.put(((EditorTreeObject)it.next()).serializeToJson());
        }
        return root;
    }

}