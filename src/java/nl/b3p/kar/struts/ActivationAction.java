package nl.b3p.kar.struts;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
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
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.validator.DynaValidatorForm;
import org.hibernate.HibernateException;

public final class ActivationAction extends BaseDatabaseAction {

    protected Log log = LogFactory.getLog(this.getClass());
    protected static final String VIEWER = "viewer";
    protected static final String SAVE = "save";
    protected static final String WKTGEOM_NOTVALID_ERROR_KEY = "error.wktgeomnotvalid";
    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

    protected Map getActionMethodPropertiesMap() {
        Map map = new HashMap();
        ExtendedMethodProperties hibProp = null;
        hibProp = new ExtendedMethodProperties(SAVE);
        hibProp.setDefaultMessageKey("warning.crud.savedone");
        hibProp.setDefaultForwardName(SUCCESS);
        hibProp.setAlternateForwardName(FAILURE);
        hibProp.setAlternateMessageKey("error.crud.savefailed");
        map.put(SAVE, hibProp);

        map.put("new", new ExtendedMethodProperties("create"));
        return map;
    }

    public ActionForward unspecified(ActionMapping mapping, DynaValidatorForm dynaForm, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (isCancelled(request)) {
            return mapping.findForward(VIEWER);
        }
        Activation activation = getActivation(dynaForm, request, true);

        if (activation == null) {
            addAlternateMessage(mapping, request, "error.notfound");
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

    public ActionForward create(ActionMapping mapping, DynaValidatorForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        createLists(null, form, request);
        return mapping.findForward(SUCCESS);
    }

    private ActivationGroup getActivationGroup(DynaValidatorForm form, HttpServletRequest request) throws Exception {
        Integer agId = FormUtils.StringToInteger(form.getString("agId"));
        ActivationGroup ag = null;
        if(agId != null) {
            ag = getEntityManager().find(ActivationGroup.class, agId);
        }
        return ag;
    }

    public ActionForward save(ActionMapping mapping, DynaValidatorForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        EntityManager em = getEntityManager();
        Activation activation = getActivation(form, request, true);
        if (activation == null) {
            addAlternateMessage(mapping, request, "Niet gevonden");
            return mapping.findForward(SUCCESS);
        }
        createLists(activation, form, request);
        
        ActionErrors errors = form.validate(mapping, request);
        if(!errors.isEmpty()) {
            addMessages(request, errors);
            return getDefaultForward(mapping, request);
        }

        if(!em.contains(activation)) {
            ActivationGroup ag = getActivationGroup(form, request);
            if(ag == null) {
                addMessage(request, "errors.required", "Signaalgroep");
                return getDefaultForward(mapping, request);
            }
            activation.setActivationGroup(ag);
        }

        /* XXX Check constraints */

        populateObject(activation, form, request, mapping);

        if(activation.getId() == null) {
            // XXX
            activation.setValidFrom(new Date());
            activation.setIndex(activation.getActivationGroup().getActivations().size()+1);
            em.persist(activation);
        }
        em.flush();

        populateForm(activation, form, request);

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
        if(em.contains(activation)) {
            request.setAttribute("activation", activation);
            if(activation.getPoint() != null) {
                activation.getPoint().getGeom();
            }
            request.setAttribute("activationGroup", activation.getActivationGroup());
            activation.getActivationGroup().getRoadsideEquipment().getDataOwner().getName();
        } else {
            ActivationGroup ag = getActivationGroup(form, request);
            if(ag == null) {
                addMessage(request, "errors.required", "Signaalgroep");
            } else {
                request.setAttribute("activationGroup", ag);
                ag.getRoadsideEquipment().getDataOwner().getName();
            }
        }
    }

    protected void populateForm(Activation a, DynaValidatorForm form, HttpServletRequest request) throws Exception {
        form.set("id", a.getId() + "");

        form.set("karUsageType", a.getKarUsageType());
        form.set("triggerType", a.getTriggerType() == null ? null : a.getTriggerType() + "");
        form.set("commandType", a.getCommandType() == null ? null : a.getCommandType() + "");

        NumberFormat nf = DecimalFormat.getInstance(Locale.ENGLISH);
        nf.setGroupingUsed(false);
        
        form.set("karDistanceTillStopLine", a.getKarDistanceTillStopLine() == null ? null : nf.format( a.getKarDistanceTillStopLine()));
        form.set("karTimeTillStopLine", a.getKarTimeTillStopLine() == null ? null : nf.format(a.getKarTimeTillStopLine()));
    }   

    protected void populateObject(Activation a, DynaValidatorForm form, HttpServletRequest request, ActionMapping mapping) throws Exception {
        EntityManager em = getEntityManager();

        a.setKarUsageType(form.getString("karUsageType"));
        a.setTriggerType(form.getString("triggerType"));
        Integer commandType = FormUtils.StringToInteger(form.getString("commandType"));
        if(commandType != null && (commandType < 1 || commandType > 3)) {
            commandType = null;
        }
        a.setCommandType(commandType);
        String val = FormUtils.nullIfEmpty(form.getString("karDistanceTillStopLine"));
        if(val == null) {
            a.setKarDistanceTillStopLine(null);
        } else {
            a.setKarDistanceTillStopLine(Double.parseDouble(val));
        }
        val = FormUtils.nullIfEmpty(form.getString("karTimeTillStopLine"));
        if(val == null) {
            a.setKarTimeTillStopLine(null);
        } else {
            a.setKarTimeTillStopLine(Double.parseDouble(val));
        }
        a.setUpdater(request.getRemoteUser());
        a.setUpdateTime(new Date());
    }
}
