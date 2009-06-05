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

    public static String getObjectInfo(String type, String id) throws Exception {
        return (String)executeInTransaction("getObjectInfoFromDb", type, id);
    }

    public static String getKarPuntInfo(String type, String id) throws Exception {
        return (String)executeInTransaction("getKarPuntInfoFromDb", type, id);
    }

    public static String getMultipleKarPuntInfo(String features) throws Exception {
        return (String)executeInTransaction("getMultipleKarPuntInfoFromDb", features);
    }

    public static String getObjectInfoFromDb(String... args) throws Exception {
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
    public static String getKarPuntInfoFromDb(String... args) throws Exception {
        /* id is the id of a KarPunt, so find the first object of type that has
         * a reference to that KarPunt
         */
        String type = args[0];
        String id = args[1];
        String clazz = null;
        if("a".equals(type)) {
            clazz = "Activation";
            type = "a";
        } else if("ag".equals(type)) {
            clazz = "ActivationGroup";
            type = "ag";
        } else if("rseq".equals(type)) {
            clazz = "RoadsideEquipment";
            type = "rseq";
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
        Integer objectId = (Integer)getEntityManager().createQuery("select id from " + clazz + " where point.id = :id")
                .setParameter("id", idObj)
                .setMaxResults(1)
                .getSingleResult();
        if(objectId == null) {
            return "error: no object " + type + " found for point " + idObj;
        }

        return getObjectInfoFromDb(type, objectId + "");
    }

    public static String getMultipleKarPuntInfoFromDb(String... args) throws Exception {
        JSONArray features;
        List karPuntIds = new ArrayList();
        try {
            features = new JSONArray(args[0]);

            for(int i = 0; i < features.length(); i++) {
                JSONObject jo = features.getJSONObject(i);
                int id = jo.getInt("id");
                String type = jo.getString("type");
                if(type.equals(KarPunt.TYPE_ACTIVATION)
                || type.equals(KarPunt.TYPE_ACTIVATION_GROUP)
                || type.equals(KarPunt.TYPE_ROADSIDE_EQUIPMENT)) {
                    karPuntIds.add(id);
                }
            }
        } catch(JSONException je) {
            return "error: json exception";
        }
        if(karPuntIds.isEmpty()) {
            return "error: no valid karpunt id's found";
        }
        List objects = getEntityManager().createQuery(
                "from Activation where point.id in (:ids) " +
                "union from ActivationGroup where point.id in (:ids) " +
                "union from RoadsideEquipment where point.id in (:ids)")
                .setParameter("ids", karPuntIds)
                .getResultList();

        if(objects.isEmpty()) {
            return "error: no objects found";
        }

        JSONObject info = new JSONObject();
        JSONArray jObjects = new JSONArray();
        Envelope envelope = new Envelope();
        for(Iterator it = objects.iterator(); it.hasNext();) {
            Object obj = it.next();
            String type = null;
            Integer id = null;
            if(obj instanceof Activation) {
                type = "a";
                Activation a = (Activation)obj;
                id = ((Activation)obj).getId();
            } else if(obj instanceof ActivationGroup) {
                type = "ag";
                id = ((ActivationGroup)obj).getId();
            } else if(obj instanceof RoadsideEquipment) {
                type = "rseq";
                id = ((RoadsideEquipment)obj).getId();
            }
            Coordinate c = getObjectCoordinate(obj);
            if(c != null) {
                envelope.expandToInclude(c);
            }
            jObjects.put(type + ":" + id);
        }
        info.put("objects", jObjects);
        if(!envelope.isNull()) {
            info.put("envelope", "{minX: " + envelope.getMinX() + ", maxX: " + envelope.getMaxX()
                    + ", minY: " + envelope.getMinY() + ", maxY: " + envelope.getMaxY() + "}");
        }
        info.put("tree", buildObjectTree(objects));
        return info.toString();
    }

    private static String buildObjectTree(List objects) throws Exception {

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
        return root.toString();
    }

}