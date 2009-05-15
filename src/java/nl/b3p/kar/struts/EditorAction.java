package nl.b3p.kar.struts;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import nl.b3p.commons.struts.ExtendedMethodProperties;
import nl.b3p.kar.persistence.MyEMFDatabase;
import nl.b3p.transmodel.Activation;
import nl.b3p.transmodel.ActivationGroup;
import nl.b3p.transmodel.RoadsideEquipment;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.validator.DynaValidatorForm;
import org.json.JSONObject;

/**
 * $Id$
 */

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
        info.put("tree", buildObjectTree(object));
        return info.toString();
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

    private static String buildObjectTree(Object object) throws Exception {
        Object root = object;
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
        return ((EditorTreeObject)root).serializeToJson().toString();
    }
    
}