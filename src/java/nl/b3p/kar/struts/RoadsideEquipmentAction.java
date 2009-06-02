package nl.b3p.kar.struts;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import nl.b3p.commons.services.FormUtils;
import nl.b3p.commons.struts.ExtendedMethodProperties;
import nl.b3p.transmodel.DataOwner;
import nl.b3p.transmodel.RoadsideEquipment;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.validator.DynaValidatorForm;
import org.hibernate.HibernateException;

public final class RoadsideEquipmentAction extends BaseDatabaseAction {

    private static Log log = LogFactory.getLog(RoadsideEquipmentAction.class);
    
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

        map.put("new", new ExtendedMethodProperties("create"));
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

    public ActionForward create(ActionMapping mapping, DynaValidatorForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        createLists(null, form, request);
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

        ActionErrors errors = form.validate(mapping, request);
        if(!errors.isEmpty()) {
            addMessages(request, errors);
            return getDefaultForward(mapping, request);
        }

        /* XXX Check constraints */

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
        EntityManager em = getEntityManager();
        if(em.contains(rseq)) {
            request.setAttribute("roadsideEquipment", rseq);
            rseq.getDataOwner().getName();
        }
        request.setAttribute("dataOwners", em.createQuery("from DataOwner order by type, name").getResultList());
    }

    protected void populateForm(RoadsideEquipment rseq, DynaValidatorForm form, HttpServletRequest request) throws Exception {
        form.set("id", rseq.getId() + "");
        form.set("dataOwner", rseq.getDataOwner().getCode());
        form.set("unitNumber", rseq.getUnitNumber() + "");
        form.set("type", rseq.getType());
        form.set("radioAddress", rseq.getRadioAddress());
        form.set("description", rseq.getDescription());
        form.set("supplier", rseq.getSupplier());
        form.set("supplierTypeNumber", rseq.getSupplierTypeNumber());
        form.set("installationDate", FormUtils.DateToString(rseq.getInstallationDate(), null));
        form.set("selectiveDetectionLoop", rseq.isSelectiveDetectionLoop() ? "true" : "false");
    }

    protected void populateObject(RoadsideEquipment rseq, DynaValidatorForm form, HttpServletRequest request, ActionMapping mapping) throws Exception {

        /* form is al gevalideerd, ook unique constraints e.d. zijn al gechecked  */

        String dataOwner = FormUtils.nullIfEmpty(form.getString("dataOwner"));
        DataOwner dao = null;
        if(dataOwner != null) {
            dao = getEntityManager().find(DataOwner.class, dataOwner);
        }
        rseq.setDataOwner(dao);
        rseq.setUnitNumber(Integer.parseInt(form.getString("unitNumber")));
        rseq.setType(FormUtils.nullIfEmpty(form.getString("type")));
        rseq.setRadioAddress(FormUtils.nullIfEmpty(form.getString("radioAddress")));
        rseq.setDescription(FormUtils.nullIfEmpty(form.getString("description")));
        rseq.setSupplier(FormUtils.nullIfEmpty(form.getString("supplier")));
        rseq.setSupplierTypeNumber(FormUtils.nullIfEmpty(form.getString("supplierTypeNumber")));
        rseq.setInstallationDate(FormUtils.StringToDate(form.getString("installationDate"), null));
        rseq.setSelectiveDetectionLoop("true".equals(form.getString("selectiveDetectionLoop")));

        rseq.setUpdater(request.getRemoteUser());
        rseq.setUpdateTime(new Date());
    }
}
