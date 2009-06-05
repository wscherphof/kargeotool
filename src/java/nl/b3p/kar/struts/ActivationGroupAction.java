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
import nl.b3p.transmodel.RoadsideEquipment;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.validator.DynaValidatorForm;
import org.hibernate.HibernateException;

public final class ActivationGroupAction extends TreeItemAction {

    protected Map getActionMethodPropertiesMap() {
        Map map = new HashMap();
        map.put("save", new ExtendedMethodProperties("save"));
        map.put("new", new ExtendedMethodProperties("create"));
        map.put("delete", new ExtendedMethodProperties("delete"));
        return map;
    }

    public ActionForward unspecified(ActionMapping mapping, DynaValidatorForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        ActivationGroup ag = getActivationGroup(form, request, true);
        if(ag == null) {
            addAlternateMessage(mapping, request, "error.notfound");
            request.setAttribute(HIDE_FORM, Boolean.TRUE);
            return mapping.findForward(SUCCESS);
        }
        createLists(ag, form, request);
        populateForm(ag, form, request);

        return mapping.findForward(SUCCESS);
    }

    public ActionForward create(ActionMapping mapping, DynaValidatorForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        createLists(null, form, request);
        return mapping.findForward(SUCCESS);
    }

    private RoadsideEquipment getRseq(DynaValidatorForm form, HttpServletRequest request) throws Exception {
        Integer rseqId = FormUtils.StringToInteger(form.getString("rseqId"));
        RoadsideEquipment rseq = null;
        if(rseqId != null) {
            rseq = getEntityManager().find(RoadsideEquipment.class, rseqId);
        }
        return rseq;
    }
    
    public ActionForward save(ActionMapping mapping, DynaValidatorForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        EntityManager em = getEntityManager();
        ActivationGroup ag = getActivationGroup(form, request, true);
        if(ag == null) {
            addMessage(request, "error.notfound");
            request.setAttribute(HIDE_FORM, Boolean.TRUE);
            return mapping.findForward(SUCCESS);
        }
        createLists(ag, form, request);

        ActionErrors errors = form.validate(mapping, request);
        if(!errors.isEmpty()) {
            addMessages(request, errors);
            return getDefaultForward(mapping, request);
        }

        if(!em.contains(ag)) {
            RoadsideEquipment rseq = getRseq(form, request);
            if(rseq == null) {
                addMessage(request, "errors.required", "Walapparatuur");
                return mapping.findForward(SUCCESS);
            }
            ag.setRoadsideEquipment(rseq);
        }

        /* XXX Check constraints */

        populateObject(ag, form, request, mapping);

        boolean newObject = ag.getId() == null;
        if(newObject) {
            /* Set defaults */
            ag.setValidFrom(new Date());
            ag.setType(ActivationGroup.TYPE_PRQA);
            em.persist(ag);
        }
        em.flush();

        populateForm(ag, form, request);

        request.setAttribute(TREE_UPDATE, treeUpdateJson(newObject ? "insert" : "update", ag));

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
        if(getEntityManager().contains(ag)) {
            request.setAttribute("activationGroup", ag);
            request.setAttribute("rseq", ag.getRoadsideEquipment());
            ag.getRoadsideEquipment().getDataOwner().getName();
        } else {
            RoadsideEquipment rseq = getRseq(form, request);
            if(rseq == null) {
                addMessage(request, "errors.required", "Walapparatuur");
            } else {
                request.setAttribute("rseq", rseq);
                rseq.getDataOwner().getName();
            }
        }
    }

    protected void populateForm(ActivationGroup ag, DynaValidatorForm form, HttpServletRequest request) throws Exception {
        form.set("id", ag.getId() + "");
        form.set("karSignalGroup", ag.getKarSignalGroup() + "");
        String[] d;
        switch(ag.getDirectionAtIntersection()) {
            case 1: d = new String[] {"rechtsaf"}; break;
            case 2: d = new String[] {"rechtdoor"}; break;
            case 3: d = new String[] {"linksaf"}; break;
            case 4: d = new String[] {"rechtsaf","rechtdoor","linksaf"}; break;
            case 5: d = new String[] {"rechtsaf","rechtdoor"}; break;
            case 6: d = new String[] {"rechtdoor","linksaf"}; break;
            case 7: d = new String[] {"linksaf","rechtsaf"}; break;
            default: d = new String[] {"onbekend"}; break;
        }
        form.set("directionAtIntersection", d);
        form.set("metersAfterStopLine",
                ag.getMetersAfterStopLine() == null ? null : ag.getMetersAfterStopLine() + "");
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
        ag.setMetersAfterStopLine(FormUtils.StringToInteger(form.getString("metersAfterStopLine")));
        ag.setFollowDirection("true".equals(form.getString("followDirection")));
        ag.setDescription(FormUtils.nullIfEmpty(form.getString("description")));

        ag.setUpdater(request.getRemoteUser());
        ag.setUpdateTime(new Date());
    }

    public ActionForward delete(ActionMapping mapping, DynaValidatorForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        EntityManager em = getEntityManager();

        ActivationGroup ag = getActivationGroup(form, request, true);
        if (ag == null) {
            addMessage(request, "error.notfound");
            return mapping.findForward(SUCCESS);
        }
        em.remove(ag);
        em.flush();
        em.getTransaction().commit();
        addMessage(request, new ActionMessage("Signaalgroep is verwijderd.", false));
        request.setAttribute(HIDE_FORM, Boolean.TRUE);
        request.setAttribute(TREE_UPDATE, treeUpdateJson("remove", ag));
        return mapping.findForward(SUCCESS);
    }
}