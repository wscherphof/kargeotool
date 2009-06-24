package nl.b3p.kar.struts;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import nl.b3p.commons.services.FormUtils;
import nl.b3p.commons.struts.ExtendedMethodProperties;
import nl.b3p.transmodel.Activation;
import nl.b3p.transmodel.ActivationGroup;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.validator.DynaValidatorForm;
import org.hibernate.HibernateException;

public final class ActivationAction extends TreeItemAction {

    protected Map getActionMethodPropertiesMap() {
        Map map = new HashMap();
        map.put("save", new ExtendedMethodProperties("save"));
        map.put("new", new ExtendedMethodProperties("create"));
        map.put("delete", new ExtendedMethodProperties("delete"));
        map.put("validate", new ExtendedMethodProperties("validate"));
        return map;
    }

    public ActionForward unspecified(ActionMapping mapping, DynaValidatorForm dynaForm, HttpServletRequest request, HttpServletResponse response) throws Exception {
        Activation activation = getActivation(dynaForm, request, true);
        if(activation == null) {
            addMessage(request, "error.notfound");
            request.setAttribute(HIDE_FORM, Boolean.TRUE);
            return mapping.findForward(SUCCESS);
        }        

        createLists(activation, dynaForm, request);

        populateForm(activation, dynaForm, request);
        
        return mapping.findForward(SUCCESS);
    }

    public ActionForward create(ActionMapping mapping, DynaValidatorForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        form.initialize(mapping);
        createLists(null, form, request);
        /* Default voor commandType is Inmelding */
        form.set("commandType", "1");
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
            addMessage(request, "error.notfound");
            request.setAttribute(HIDE_FORM, Boolean.TRUE);
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
                return mapping.findForward(SUCCESS);
            }
            activation.setActivationGroup(ag);
        }

        /* XXX Check constraints */

        populateObject(activation, form, request, mapping);

        boolean newObject = activation.getId() == null;
        if(newObject) {
            /* Set defaults */
            activation.setValidFrom(new Date());
            activation.setIndex(activation.getActivationGroup().getActivations().size()+1);
            em.persist(activation);
        }

        em.flush();

        populateForm(activation, form, request);

        request.setAttribute(TREE_UPDATE, treeUpdateJson(newObject ? "insert" : "update", activation));

        addDefaultMessage(mapping, request);
        return getDefaultForward(mapping, request);
    }
    
    protected Activation getActivation(DynaValidatorForm dynaForm, HttpServletRequest request, boolean createNew) throws Exception {
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

        request.setAttribute("activation", activation);

        if(em.contains(activation)) {
            request.setAttribute("activationGroup", activation.getActivationGroup());
            activation.getActivationGroup().getRoadsideEquipment().getDataOwner().getName();
        } else {
            /* Hier komen we na klik op "nieuw triggerpunt" en ook bij save van nieuwe */
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

        String location = FormUtils.nullIfEmpty(form.getString("location"));
        if(location != null) {
            if("delete".equals(location)) {
                a.setLocation(null);
            } else {
                String[] xy = location.split(" ");
                Coordinate c = new Coordinate(Double.parseDouble(xy[0]), Double.parseDouble(xy[1]));
                a.setLocation(new Point(c, null, 28992));
            }
        }
    }

    public ActionForward delete(ActionMapping mapping, DynaValidatorForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        EntityManager em = getEntityManager();

        Activation activation = getActivation(form, request, true);
        if (activation == null) {
            addMessage(request, "error.notfound");
            request.setAttribute(HIDE_FORM, Boolean.TRUE);
            return mapping.findForward(SUCCESS);
        }
        /* Pas evt indexen van activations later in lijst aan */
        List activations = activation.getActivationGroup().getActivations();
        int idx = activations.indexOf(activation);
        activations.remove(activation);
        for(int i = idx; i < activations.size(); i++) {
            Activation a = (Activation)activations.get(i);
            a.setIndex(i+1); /* List is 1-based */
        }

        em.flush();
        em.getTransaction().commit();
        addMessage(request, new ActionMessage("Trigger is verwijderd.", false));
        request.setAttribute(HIDE_FORM, Boolean.TRUE);
        request.setAttribute(TREE_UPDATE, treeUpdateJson("remove", activation));
        return mapping.findForward(SUCCESS);
    }

    public ActionForward validate(ActionMapping mapping, DynaValidatorForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        Activation a = getActivation(form, request, true);
        if (a == null) {
            addMessage(request, "error.notfound");
            return mapping.findForward(SUCCESS);
        }
        if("true".equals(form.getString("validated"))) {
            a.setValidator(request.getRemoteUser());
            a.setValidationTime(new Date());
        } else {
            a.setValidator(null);
            a.setValidationTime(null);
        }
        createLists(a, form, request);
        return mapping.findForward(SUCCESS);
    }
}
