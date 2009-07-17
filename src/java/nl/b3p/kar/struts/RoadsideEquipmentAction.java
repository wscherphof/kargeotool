package nl.b3p.kar.struts;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import nl.b3p.commons.services.FormUtils;
import nl.b3p.commons.struts.ExtendedMethodProperties;
import nl.b3p.kar.hibernate.Gebruiker;
import nl.b3p.kar.hibernate.Role;
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
        map.put("validate", new ExtendedMethodProperties("validate"));
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
        form.initialize(mapping);
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

        /* check hier en niet in populateObject(), omdat nodig is voor auth check */
        Integer daoId = FormUtils.StringToInteger(form.getString("dataOwner"));
        DataOwner dao = daoId == null ? null : em.find(DataOwner.class, daoId);
        if(dao == null) {
            addMessage(request, "errors.required", "Databeheerder");
            return mapping.findForward(SUCCESS);
        }

        if(!request.isUserInRole(Role.BEHEERDER)) {
            Gebruiker g = Gebruiker.getNonTransientPrincipal(request);
            if(!g.canEditDataOwner(dao)) {
                addMessage(request, "error.dataowner.uneditable", dao.getName());
                return mapping.findForward(SUCCESS);
            }
        }

        /* XXX Check constraints */

        rseq.setDataOwner(dao);
        populateObject(rseq, form, request, mapping);

        boolean newObject = rseq.getId() == null;
        if(newObject) {
            em.persist(rseq);
        }

        em.flush();

        populateForm(rseq, form, request);

        request.setAttribute(TREE_UPDATE, treeUpdateJson(newObject ? "insert" : "update", rseq, request));

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

        if(em.contains(rseq)) {
            request.setAttribute("activationGroupCount",
                    em.createQuery("select count(*) from ActivationGroup ag where ag.roadsideEquipment = :this")
                        .setParameter("this", rseq)
                        .getSingleResult());
            rseq.getDataOwner().getName();
        }
        List dataOwners;
        if(request.isUserInRole(Role.BEHEERDER)) {
            dataOwners = em.createQuery("from DataOwner where type = :type order by id")
                .setParameter("type", DataOwner.TYPE_ROOW)
                .getResultList();
        } else {
            Gebruiker g = Gebruiker.getNonTransientPrincipal(request);
            Set dataOwnersUnsorted = g.getDataOwnerRights().keySet();
            dataOwners = new ArrayList();
            for (Iterator<DataOwner> it = dataOwnersUnsorted.iterator(); it.hasNext();) {
                DataOwner dao = it.next();
                if(dao.getType().equals(DataOwner.TYPE_ROOW)) {
                    dataOwners.add(dao);
                }
            }
            Collections.sort(dataOwners,  new Comparator() {
            public int compare(Object o1, Object o2) {
                DataOwner lhs = (DataOwner)o1;
                DataOwner rhs = (DataOwner)o2;
                return lhs.getId().compareTo(rhs.getId());
            }
        });
        }
        request.setAttribute("dataOwners", dataOwners);
    }

    protected void populateForm(RoadsideEquipment rseq, DynaValidatorForm form, HttpServletRequest request) throws Exception {
        form.set("id", rseq.getId() + "");
        form.set("dataOwner", rseq.getDataOwner().getId() + "");
        form.set("unitNumber", rseq.getUnitNumber() + "");
        form.set("type", rseq.getType());
        form.set("radioAddress", rseq.getRadioAddress());
        form.set("description", rseq.getDescription());
        form.set("supplier", rseq.getSupplier());
        form.set("supplierTypeNumber", rseq.getSupplierTypeNumber());
        form.set("installationDate", FormUtils.DateToString(rseq.getInstallationDate(), null));
        form.set("selectiveDetectionLoop", rseq.isSelectiveDetectionLoop() ? "true" : "false");

        if(!request.isUserInRole(Role.BEHEERDER)) {
            Gebruiker g = Gebruiker.getNonTransientPrincipal(request);
            DataOwner dao = rseq.getDataOwner();
            request.setAttribute("notEditable", !g.canEditDataOwner(dao));
            request.setAttribute("notValidatable", !g.canValidateDataOwner(dao));
        }
    }

    protected void populateObject(RoadsideEquipment rseq, DynaValidatorForm form, HttpServletRequest request, ActionMapping mapping) throws Exception {

        /* form is al gevalideerd, ook unique constraints e.d. zijn al gechecked  */

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
                rseq.setLocation(null);
            } else {
                String[] xy = location.split(" ");
                Coordinate c = new Coordinate(Double.parseDouble(xy[0]), Double.parseDouble(xy[1]));
                rseq.setLocation(new Point(c, null, 28992));
            }
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
        createLists(rseq, form, request);

        if(!request.isUserInRole(Role.BEHEERDER)) {
            Gebruiker g = Gebruiker.getNonTransientPrincipal(request);
            DataOwner dao = rseq.getDataOwner();
            if(!g.canEditDataOwner(dao)) {
                addMessage(request, "error.dataowner.uneditable", dao.getName());
                return mapping.findForward(SUCCESS);
            }
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
        em.flush();
        em.getTransaction().commit();
        addMessage(request, new ActionMessage("Walapparaat is verwijderd.", false));
        request.setAttribute(HIDE_FORM, Boolean.TRUE);
        request.setAttribute(TREE_UPDATE, treeUpdateJson("remove", rseq, request));
        return mapping.findForward(SUCCESS);
    }

    public ActionForward validate(ActionMapping mapping, DynaValidatorForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        RoadsideEquipment rseq = getRoadsideEquipment(form, request, true);
        if (rseq == null) {
            addMessage(request, "error.notfound");
            return mapping.findForward(SUCCESS);
        }
        if(!request.isUserInRole(Role.BEHEERDER)) {
            Gebruiker g = Gebruiker.getNonTransientPrincipal(request);
            DataOwner dao = rseq.getDataOwner();
            if(!g.canValidateDataOwner(dao)) {
                addMessage(request, "error.dataowner.unvalidatable", dao.getName());
                return mapping.findForward(SUCCESS);
            }
        }
        if("true".equals(form.getString("validated"))) {
            rseq.setValidator(request.getRemoteUser());
            rseq.setValidationTime(new Date());
        } else {
            rseq.setValidator(null);
            rseq.setValidationTime(null);
        }
        createLists(rseq, form, request);

        /* nodig omdat form mogelijk disabled is */
        populateForm(rseq, form, request);
        return mapping.findForward(SUCCESS);
    }
}
