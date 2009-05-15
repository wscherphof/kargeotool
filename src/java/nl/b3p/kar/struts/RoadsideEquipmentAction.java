package nl.b3p.kar.struts;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import nl.b3p.commons.services.FormUtils;
import nl.b3p.commons.struts.ExtendedMethodProperties;
import nl.b3p.transmodel.RoadsideEquipment;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.validator.DynaValidatorForm;
import org.hibernate.HibernateException;

public final class RoadsideEquipmentAction extends BaseDatabaseAction {

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
        RoadsideEquipment rseq = getRoadsideEquipment(form, request, true);

        if (rseq == null) {
            addAlternateMessage(mapping, request, "error.notfound");
            return mapping.findForward(SUCCESS);
        }
        createLists(rseq, form, request);

        populateForm(rseq, form, request);

        /*Als er een nieuw object is getekend*/
        if (FormUtils.nullIfEmpty(request.getParameter("newWktgeom"))!=null){
            form.set("location", request.getParameter("newWktgeom"));
        }

        return mapping.findForward(SUCCESS);
    }

    public ActionForward save(ActionMapping mapping, DynaValidatorForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        EntityManager em = getEntityManager();
        RoadsideEquipment rseq = getRoadsideEquipment(form, request, true);
        if(rseq == null) {
            addAlternateMessage(mapping, request, "error.notfound");
            return mapping.findForward(SUCCESS);
        }
        createLists(rseq, form, request);

        /* XXX Check validation */

        populateObject(rseq, form, request, mapping);

        if(rseq.getId() == null) {
            em.persist(rseq);
        }
        em.flush();

        populateForm(rseq, form, request);

        addDefaultMessage(mapping, request);
        return getDefaultForward(mapping, request);
    }

    protected RoadsideEquipment getRoadsideEquipment(DynaValidatorForm form, HttpServletRequest request, boolean createNew) throws Exception {
        EntityManager em = getEntityManager();
        RoadsideEquipment rseq = null;
        Integer id = FormUtils.StringToInteger(form.getString("id"));
        if(null == id && createNew) {
            rseq = new RoadsideEquipment();
        } else if (null != id) {
            rseq = (RoadsideEquipment) em.find(RoadsideEquipment.class, id);
        }
        return rseq;
    }

    protected void createLists(RoadsideEquipment rseq, DynaValidatorForm form, HttpServletRequest request) throws HibernateException, Exception {
        request.setAttribute("roadsideEquipment", rseq);
        rseq.getDataOwner().getName();
        EntityManager em = getEntityManager();
        request.setAttribute("dataOwners", em.createQuery("from DataOwner order by type, name").getResultList());
    }

    protected void populateForm(RoadsideEquipment rseq, DynaValidatorForm form, HttpServletRequest request) throws Exception {
        form.set("id", rseq.getId() + "");
        form.set("unitNumber", rseq.getUnitNumber() + "");
        form.set("description", rseq.getDescription() == null ? null : rseq.getDescription());
    }

    protected void populateObject(RoadsideEquipment rseq, DynaValidatorForm form, HttpServletRequest request, ActionMapping mapping) throws Exception {
        rseq.setUpdater(request.getRemoteUser());
        rseq.setUpdateTime(new Date());
    }
}
