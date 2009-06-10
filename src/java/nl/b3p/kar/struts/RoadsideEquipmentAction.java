package nl.b3p.kar.struts;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import nl.b3p.commons.services.FormUtils;
import nl.b3p.commons.struts.ExtendedMethodProperties;
import nl.b3p.kar.hibernate.KarPunt;
import nl.b3p.transmodel.DataOwner;
import nl.b3p.transmodel.RoadsideEquipment;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.validator.DynaValidatorForm;
import org.hibernate.HibernateException;

public final class RoadsideEquipmentAction extends TreeItemAction {

    protected Map getActionMethodPropertiesMap() {
        Map map = new HashMap();
        map.put("save", new ExtendedMethodProperties("save"));
        map.put("new", new ExtendedMethodProperties("create"));
        map.put("delete", new ExtendedMethodProperties("delete"));
        return map;
    }

    public ActionForward unspecified(ActionMapping mapping, DynaValidatorForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        RoadsideEquipment rseq = getRoadsideEquipment(form, request, true);
        if(rseq == null) {
            addMessage(request, "error.notfound");
            request.setAttribute(HIDE_FORM, Boolean.TRUE);
            return mapping.findForward(SUCCESS);
        }

        createLists(rseq, form, request);

        populateForm(rseq, form, request);

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
            addMessage(request, "error.notfound");
            request.setAttribute(HIDE_FORM, Boolean.TRUE);
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

        boolean newObject = rseq.getId() == null;
        if(newObject) {
            em.persist(rseq);
        }
        if(rseq.getPoint() != null && rseq.getPoint().getId() == null) {
            em.persist(rseq.getPoint());
        }
        em.flush();

        populateForm(rseq, form, request);

        request.setAttribute(TREE_UPDATE, treeUpdateJson(newObject ? "insert" : "update", rseq));

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

        request.setAttribute("roadsideEquipment", rseq);
        if(rseq != null && rseq.getPoint() != null) {
            rseq.getPoint().getGeom();
        }

        if(em.contains(rseq)) {
            request.setAttribute("activationGroupCount",
                    em.createQuery("select count(*) from ActivationGroup ag where ag.roadsideEquipment = :this")
                        .setParameter("this", rseq)
                        .getSingleResult());
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

        String location = FormUtils.nullIfEmpty(form.getString("location"));
        if(location != null) {
            if("delete".equals(location)) {
                KarPunt oldKp = rseq.getPoint();
                rseq.setPoint(null);
                if(oldKp != null) {
                    deleteOrphanKarPoint(oldKp, rseq);
                }
            } else {
                KarPunt kp = rseq.getPoint();
                if(kp == null) {
                    kp = new KarPunt();
                    rseq.setPoint(kp);
                }
                kp.setType(KarPunt.TYPE_ACTIVATION);
                String[] xy = location.split(" ");
                Coordinate c = new Coordinate(Double.parseDouble(xy[0]), Double.parseDouble(xy[1]));
                kp.setGeom(new Point(c, null, 28992));
            }
        }
    }


    private void deleteOrphanKarPoint(KarPunt kp, RoadsideEquipment notCountingThisOne) throws Exception {
        EntityManager em = getEntityManager();
        Object haveOtherRefs = null;
        try {
            haveOtherRefs = em.createQuery("select 1 from Activation a where " +
                    "a <> :notCountingThisOne " +
                    "and a.point = :kp")
                    .setParameter("notCountingThisOne", notCountingThisOne)
                    .setParameter("kp", kp)
                    .getSingleResult();
        } catch(NoResultException nre) {
            // zucht
        }
        if(haveOtherRefs == null) {
            em.remove(kp);
        }
    }
    
    public ActionForward delete(ActionMapping mapping, DynaValidatorForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        EntityManager em = getEntityManager();

        RoadsideEquipment rseq = getRoadsideEquipment(form, request, true);
        if(rseq == null) {
            addMessage(request, "error.notfound");
            request.setAttribute(HIDE_FORM, Boolean.TRUE);
            return mapping.findForward(SUCCESS);
        }
        List agIds = em.createQuery("select id from ActivationGroup ag where ag.roadsideEquipment = :this")
                .setParameter("this", rseq)
                .getResultList();
        if(!agIds.isEmpty()) {
            em.createQuery("delete from Activation a where a.activationGroup.id in (:ids)")
                    .setParameter("ids", agIds)
                    .executeUpdate();
            em.createQuery("delete from ActivationGroup ag where ag.id in (:ids)")
                    .setParameter("ids", agIds)
                    .executeUpdate();
        }
        em.remove(rseq);
        if(rseq.getPoint() != null) {
            deleteOrphanKarPoint(rseq.getPoint(), rseq);
        }
        em.flush();
        em.getTransaction().commit();
        addMessage(request, new ActionMessage("Walapparaat is verwijderd.", false));
        request.setAttribute(HIDE_FORM, Boolean.TRUE);
        request.setAttribute(TREE_UPDATE, treeUpdateJson("remove", rseq));
        return mapping.findForward(SUCCESS);
    }
}
