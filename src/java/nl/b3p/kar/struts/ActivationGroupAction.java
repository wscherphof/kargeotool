package nl.b3p.kar.struts;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import nl.b3p.commons.services.FormUtils;
import nl.b3p.commons.struts.ExtendedMethodProperties;
import nl.b3p.transmodel.ActivationGroup;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.validator.DynaValidatorForm;
import org.hibernate.HibernateException;

public final class ActivationGroupAction extends BaseDatabaseAction {

    private static Log log = LogFactory.getLog(ActivationGroupAction.class);
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

    public ActionForward unspecified(ActionMapping mapping, DynaValidatorForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (isCancelled(request)) {
            return mapping.findForward(VIEWER);
        }
        ActivationGroup ag = getActivationGroup(form, request, true);

        if (ag == null) {
            addAlternateMessage(mapping, request, "error.notfound");
            return mapping.findForward(SUCCESS);
        }
        createLists(ag, form, request);

        populateForm(ag, form, request);

        /*Als er een nieuw object is getekend*/
        if (FormUtils.nullIfEmpty(request.getParameter("newWktgeom"))!=null){
            form.set("location", request.getParameter("newWktgeom"));
        }
        
        return mapping.findForward(SUCCESS);
    }

    public ActionForward save(ActionMapping mapping, DynaValidatorForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        EntityManager em = getEntityManager();
        ActivationGroup ag = getActivationGroup(form, request, true);
        if(ag == null) {
            addAlternateMessage(mapping, request, "error.notfound");
            return mapping.findForward(SUCCESS);
        }
        createLists(ag, form, request);

        /* XXX Check validation */

        populateObject(ag, form, request, mapping);

        if(ag.getId() == null) {
            em.persist(ag);
        }
        em.flush();

        populateForm(ag, form, request);

        addDefaultMessage(mapping, request);
        return getDefaultForward(mapping, request);
    }

    protected ActivationGroup getActivationGroup(DynaValidatorForm form, HttpServletRequest request, boolean createNew) throws Exception {
        EntityManager em = getEntityManager();
        ActivationGroup ag = null;
        Integer id = FormUtils.StringToInteger(form.getString("id"));
        if(null == id && createNew) {
            ag = new ActivationGroup();
        } else if (null != id) {
            ag = (ActivationGroup) em.find(ActivationGroup.class, id);
        }
        return ag;
    }

    protected void createLists(ActivationGroup ag, DynaValidatorForm form, HttpServletRequest request) throws HibernateException, Exception {
        request.setAttribute("activationGroup", ag);
        if(ag.getRoadsideEquipment() != null) {
            ag.getRoadsideEquipment().getDataOwner().getName();
        }
    }

    protected void populateForm(ActivationGroup ag, DynaValidatorForm form, HttpServletRequest request) throws Exception {
        form.set("id", ag.getId() + "");
        form.set("karSignalGroup", ag.getKarSignalGroup() + "");
        String[] d = null;
        switch(ag.getDirectionAtIntersection()) {
            case 0: d = new String[] {"onbekend"}; break;
            case 1: d = new String[] {"rechtsaf"}; break;
            case 2: d = new String[] {"rechtdoor"}; break;
            case 3: d = new String[] {"linksaf"}; break;
            case 4: d = new String[] {"rechtsaf","rechtdoor","linksaf"}; break;
            case 5: d = new String[] {"rechtsaf","rechtdoor"}; break;
            case 6: d = new String[] {"rechtdoor","linksaf"}; break;
            case 7: d = new String[] {"linksaf","rechtsaf"}; break;
        }
        form.set("directionAtIntersection", d);
        form.set("metersAfterRoadsideEquipmentLocation", 
                ag.getMetersAfterRoadsideEquipmentLocation() == null ? null : ag.getMetersAfterRoadsideEquipmentLocation() + "");
        form.set("followDirection", ag.isFollowDirection() ? "true" : "false");
        form.set("description", ag.getDescription() == null ? null : ag.getDescription());
    }

    protected void populateObject(ActivationGroup ag, DynaValidatorForm form, HttpServletRequest request, ActionMapping mapping) throws Exception {
        EntityManager em = getEntityManager();

        ag.setKarSignalGroup(Integer.parseInt(form.getString("karSignalGroup")));

        String[] dirOpts = (String[])form.get("directionAtIntersection");
        Set dirs = new HashSet();
        dirs.addAll(Arrays.asList(dirOpts));

        int d;
        if(dirs.contains("rechtsaf") && dirs.contains("rechtdoor") && dirs.contains("linksaf")) {
            d = 4;
        } else if(dirs.contains("rechtsaf") && dirs.contains("rechtdoor")) {
            d = 5;
        } else if(dirs.contains("rechtdoor") && dirs.contains("linksaf")) {
            d = 6;
        } else if(dirs.contains("linksaf") && dirs.contains("rechtsaf")) {
            d = 7;
        } else if(dirs.contains("linksaf")) {
            d = 3;
        } else if(dirs.contains("rechtdoor")) {
            d = 2;
        } else if(dirs.contains("rechtsaf")) {
            d = 1;
        } else {
            d = 0;
        }
        ag.setDirectionAtIntersection(d);
        ag.setMetersAfterRoadsideEquipmentLocation(FormUtils.StringToInteger(form.getString("metersAfterRoadsideEquipmentLocation")));
        ag.setFollowDirection("true".equals(form.getString("followDirection")));
        ag.setDescription(FormUtils.nullIfEmpty(form.getString("description")));

        ag.setUpdater(request.getRemoteUser());
        ag.setUpdateTime(new Date());
    }
}
