package nl.b3p.kar.struts;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import nl.b3p.commons.services.FormUtils;
import nl.b3p.commons.struts.ExtendedMethodProperties;
import nl.b3p.transmodel.Activation;
import nl.b3p.transmodel.ActivationGroup;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.validator.DynaValidatorForm;
import org.hibernate.HibernateException;

public final class ActivationAction extends BaseDatabaseAction {

    protected Log log = LogFactory.getLog(this.getClass());
    protected static final String VIEWER = "viewer";
    protected static final String SAVE = "save";
    protected static final String WKTGEOM_NOTVALID_ERROR_KEY = "error.wktgeomnotvalid";
    
    protected Map getActionMethodPropertiesMap() {
        Map map = new HashMap();
        ExtendedMethodProperties hibProp = null;
        hibProp = new ExtendedMethodProperties(SAVE);
        hibProp.setDefaultMessageKey("warning.crud.savedone");
        hibProp.setDefaultForwardName(SUCCESS);
        hibProp.setAlternateForwardName(FAILURE);
        hibProp.setAlternateMessageKey("error.crud.savefailed");
        map.put(SAVE, hibProp);   
        return map;
    }

    public ActionForward unspecified(ActionMapping mapping, DynaValidatorForm dynaForm, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (isCancelled(request)) {
            return mapping.findForward(VIEWER);
        }
        Activation activation = getActivation(dynaForm, request, true);

        if (activation == null) {
            addAlternateMessage(mapping, request, "Niet gevonden");
            return mapping.findForward(SUCCESS);
        }        
        createLists(activation, dynaForm, request);

        populateForm(activation, dynaForm, request);
        
        /*Als er een nieuw object is getekend*/
        if (FormUtils.nullIfEmpty(request.getParameter("newWktgeom"))!=null){
            dynaForm.set("location", request.getParameter("newWktgeom"));
        }
        return mapping.findForward(SUCCESS);
    }

    public ActionForward save(ActionMapping mapping, DynaValidatorForm dynaForm, HttpServletRequest request, HttpServletResponse response) throws Exception {
        EntityManager em = getEntityManager();
        Activation activation = getActivation(dynaForm, request, true);
        if (activation == null) {
            addAlternateMessage(mapping, request, "Niet gevonden");
            return mapping.findForward(SUCCESS);
        }
        populateObject(activation, dynaForm, request, mapping);
        if (activation.getLocation()!=null){
            if (activation.getId() == null) {
                em.persist(activation);
            } else {
                em.merge(activation);
            }
            em.flush();
        }
        createLists(activation, dynaForm, request);
        addDefaultMessage(mapping, request);
        return getDefaultForward(mapping, request);
    }
    protected Activation getActivation(DynaValidatorForm dynaForm, HttpServletRequest request, boolean createNew) throws Exception {
        log.debug("Getting entity manager ......");
        EntityManager em = getEntityManager();
        Activation activation = null;
        Integer id = FormUtils.StringToInteger(dynaForm.getString("id"));
        if (null == id && createNew) {
            activation = new Activation();
        } else if (null != id) {
            activation = (Activation) em.find(Activation.class, id.intValue());
        }
        return activation;
    }

    protected void createLists(Activation activation, DynaValidatorForm form, HttpServletRequest request) throws HibernateException, Exception {
        EntityManager em = getEntityManager();
        request.setAttribute("activationGroupList", em.createQuery("from ActivationGroup order by id").getResultList());
    }

    protected void populateForm(Activation a, DynaValidatorForm dynaForm, HttpServletRequest request) throws Exception {
        dynaForm.set("id",a.getId());
        if (a.getActivationGroup()!=null)
            dynaForm.set("activationGroup",a.getActivationGroup().getId());
        dynaForm.set("index",a.getIndex());
        if (a.getValidFrom()!=null){
            dynaForm.set("validFrom",FormUtils.DateToFormString(a.getValidFrom(),request.getLocale()));
        }
        dynaForm.set("karUsageType",a.getKarUsageType());
        dynaForm.set("type",a.getType());
        dynaForm.set("commandType",""+a.getCommandType());
        dynaForm.set("karDistanceTillStopLine",a.getKarDistanceTillStopLine());
        dynaForm.set("karTimeTillStopLine",a.getKarTimeTillStopLine());
        dynaForm.set("karRadioPower",a.getKarRadioPower());
        dynaForm.set("metersBeforeRoadsSideEquipmentLocation",a.getMetersBeforeRoadsideEquipmentLocation());
        dynaForm.set("angleToNorth",a.getAngleToNorth());
        dynaForm.set("updater",a.getUpdater());
        if (a.getUpdateTime()!=null){
            dynaForm.set("updateTime",FormUtils.DateToFormString(a.getUpdateTime(),request.getLocale()));
        }
        dynaForm.set("validator",a.getValidator());
        if (a.getValidationTime()!=null)
            dynaForm.set("validationTime",FormUtils.DateToFormString(a.getValidationTime(),request.getLocale()));
        dynaForm.set("location",null);
    }   

    protected void populateObject(Activation a, DynaValidatorForm dynaForm, HttpServletRequest request, ActionMapping mapping) throws Exception {
        EntityManager em = getEntityManager();
        if (FormUtils.nullIfEmpty(dynaForm.getString("location"))!=null){
            String wktGeom=dynaForm.getString("location");
            WKTReader wktreader = new WKTReader(new GeometryFactory(new PrecisionModel(), 28992));
            Point geom = null;
            try {
                geom = (Point) wktreader.read(wktGeom);

            } catch (ParseException p) {
                addAlternateMessage(mapping, request, WKTGEOM_NOTVALID_ERROR_KEY);
            }
            a.setLocation(geom);
        }
        if (FormUtils.nullIfEmpty(dynaForm.getString("activationGroup"))!=null){
            ActivationGroup ag=em.find(ActivationGroup.class, FormUtils.StringToInteger(dynaForm.getString("activationGroup")));
            a.setActivationGroup(ag);
        }
        a.setIndex(FormUtils.StringToInteger(dynaForm.getString("activationGroup")));
        a.setValidFrom(FormUtils.StringToDate(dynaForm.getString("validFrom"), request.getLocale()));
        a.setKarUsageType(dynaForm.getString("karUsageType"));
        a.setType(dynaForm.getString("type"));
        a.setCommandType(FormUtils.StringToInt(dynaForm.getString("commandType")));
        a.setKarDistanceTillStopLine(FormUtils.StringToDouble(dynaForm.getString("karDistanceTillStopLine")));
        a.setKarTimeTillStopLine(FormUtils.StringToDouble(dynaForm.getString("karTimeTillStopLine")));
        a.setKarRadioPower(FormUtils.StringToDouble(dynaForm.getString("karRadioPower")));
        a.setMetersBeforeRoadsideEquipmentLocation(FormUtils.StringToDouble(dynaForm.getString("metersBeforeRoadsSideEquipmentLocation")));
        a.setAngleToNorth(FormUtils.StringToDouble(dynaForm.getString("angleToNorth")));
        a.setUpdater(dynaForm.getString("updater"));
        a.setUpdateTime(FormUtils.StringToDate(dynaForm.getString("updateTime"), request.getLocale()));
        a.setValidator(dynaForm.getString("validator"));
        a.setValidationTime(FormUtils.StringToDate(dynaForm.getString("validationTime"), request.getLocale()));    }

}
