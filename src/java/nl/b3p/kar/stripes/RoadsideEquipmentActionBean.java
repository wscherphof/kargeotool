package nl.b3p.kar.stripes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.After;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.SimpleMessage;
import net.sourceforge.stripes.action.StrictBinding;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.controller.LifecycleStage;
import net.sourceforge.stripes.validation.Validate;
import net.sourceforge.stripes.validation.ValidateNestedProperties;
import nl.b3p.kar.hibernate.Gebruiker;
import nl.b3p.kar.hibernate.Role;
import nl.b3p.kar.struts.EditorTreeObject;
import nl.b3p.transmodel.Activation;
import nl.b3p.transmodel.ActivationGroup;
import nl.b3p.transmodel.DataOwner;
import nl.b3p.transmodel.RoadsideEquipment;
import org.hibernate.HibernateException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.stripesstuff.stripersist.EntityTypeConverter;
import org.stripesstuff.stripersist.Stripersist;

/**
 *
 * @author Meine Toonen meinetoonen@b3partners.nl
 */
@StrictBinding
@UrlBinding("/action/edit/roadsideequiment")
public class RoadsideEquipmentActionBean implements ActionBean {

    private static final String JSP = "/WEB-INF/jsp/viewer/editroadsideequipment2.jsp";
    private ActionBeanContext context;
    @Validate(converter = EntityTypeConverter.class)
    @ValidateNestedProperties({
        @Validate (field="type"),
        @Validate (field="unitNumber"),
        @Validate (field="radioAddress"),
        @Validate (field="description"),
        @Validate (field="supplier"),
        @Validate (field="supplierTypeNumber"),
        @Validate (field="installationDate"),
        @Validate (field="selectiveDetectionLoop"),
        @Validate (field="location", converter=WktToGeometryConverter.class),
        @Validate (field="installationDate")
        
    })
    private RoadsideEquipment rseq;
    @Validate(converter = EntityTypeConverter.class)
    private DataOwner dataOwner;
    @Validate
    private boolean validated;
    @Validate
    private boolean validatedAll;
    private Gebruiker gebruiker;
    private Integer activationGroupCount;
    private boolean hideForm;
    private String treeUpdate;
    private String activationIds;
    private List<DataOwner> dataOwners;
    

    @DefaultHandler
    public Resolution view() throws Exception {
        if (rseq == null) {
            context.getMessages().add(new SimpleMessage("error.notfound"));
            hideForm = Boolean.TRUE;
        }
        return new ForwardResolution(JSP);
    }

    public Resolution save() throws Exception {
        EntityManager em = Stripersist.getEntityManager();
        if (rseq == null) {
            context.getMessages().add(new SimpleMessage("error.notfound"));
            hideForm = Boolean.TRUE;
            return new ForwardResolution(JSP);
        }

        /* check hier en niet in populateObject(), omdat nodig is voor auth check */

        if (!gebruiker.isInRole(Role.BEHEERDER)) {
            if (!gebruiker.canEditDataOwner(dataOwner)) {
                context.getMessages().add(new SimpleMessage("error.dataowner.uneditable", dataOwner.getName()));
                return new ForwardResolution(JSP);
            }
        }

        /* XXX Check constraints */

        rseq.setDataOwner(dataOwner);
        //populateObject(rseq, form, request, mapping);

        boolean newObject = rseq.getId() == null;
        if (newObject) {
            em.persist(rseq);
        }

        em.flush();

        //populateForm(rseq, form, request);

        treeUpdate = treeUpdateJson(newObject ? "insert" : "update", rseq, context.getRequest());

        return new ForwardResolution(JSP);
    }

    @After(stages= LifecycleStage.BindingAndValidation)
    protected void createLists() throws HibernateException, Exception {
        EntityManager em = Stripersist.getEntityManager();
        gebruiker = em.find(Gebruiker.class, ((Gebruiker) context.getRequest().getUserPrincipal()).getId());
        if (em.contains(rseq)) {
            activationGroupCount = rseq.getActivationGroups().size();
            rseq.getDataOwner().getName();


            JSONArray rseqId = new JSONArray();
            rseqId.put(rseq.getId());

            List<Integer> aIds = new ArrayList<Integer>();
            Set<ActivationGroup> ags = rseq.getActivationGroups();
            for (Iterator<ActivationGroup> it = ags.iterator(); it.hasNext();) {
                ActivationGroup activationGroup = it.next();
                aIds.addAll(activationGroup.getActivationIds());
            }

            JSONArray aIdsJson = new JSONArray();
            aIdsJson.put(aIds);
            activationIds = aIdsJson.toString();
        }
        if (gebruiker.isInRole(Role.BEHEERDER)) {
            dataOwners = em.createQuery("from DataOwner where type = :type order by name").setParameter("type", DataOwner.TYPE_ROOW).getResultList();
        } else {
            Set dataOwnersUnsorted = gebruiker.getEditableDataOwners();
            dataOwners = new ArrayList();
            for (Iterator<DataOwner> it = dataOwnersUnsorted.iterator(); it.hasNext();) {
                DataOwner dao = it.next();
                if (dao.getType().equals(DataOwner.TYPE_ROOW)) {
                    dataOwners.add(dao);
                }
            }
            Collections.sort(dataOwners, new Comparator() {
                public int compare(Object o1, Object o2) {
                    DataOwner lhs = (DataOwner) o1;
                    DataOwner rhs = (DataOwner) o2;
                    return lhs.getName().compareTo(rhs.getName());
                }
            });
        }

    }
    /*
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

     if (!request.isUserInRole(Role.BEHEERDER)) {
     Gebruiker g = Gebruiker.getNonTransientPrincipal(request);
     DataOwner dao = rseq.getDataOwner();
     request.setAttribute("notEditable", !g.canEditDataOwner(dao));
     request.setAttribute("notValidatable", !g.canValidateDataOwner(dao));
     }
     }

     protected void populateObject(RoadsideEquipment rseq, DynaValidatorForm form, HttpServletRequest request, ActionMapping mapping) throws Exception {

     // form is al gevalideerd, ook unique constraints e.d. zijn al gechecked  

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
     if (location != null) {
     if ("delete".equals(location)) {
     rseq.setLocation(null);
     } else {
     String[] xy = location.split(" ");
     Coordinate c = new Coordinate(Double.parseDouble(xy[0]), Double.parseDouble(xy[1]));
     rseq.setLocation(new Point(c, null, 28992));
     }
     }
     }*/

    public Resolution delete() throws Exception {
        EntityManager em = Stripersist.getEntityManager();

        if (!gebruiker.isInRole(Role.BEHEERDER)) {
            DataOwner dao = rseq.getDataOwner();
            if (!gebruiker.canEditDataOwner(dao)) {
                context.getMessages().add(new SimpleMessage("error.dataowner.uneditable", dataOwner.getName()));
                return new ForwardResolution(JSP);
            }
        }

        em.remove(rseq);
        em.flush();
        em.getTransaction().commit();
        context.getMessages().add(new SimpleMessage("Walapparaat is verwijderd."));
        hideForm = Boolean.TRUE;
        treeUpdate = treeUpdateJson("remove", rseq, context.getRequest());
        return new ForwardResolution(JSP);
    }

    public Resolution validate() throws Exception {

        if (!gebruiker.isInRole(Role.BEHEERDER)) {
            DataOwner dao = rseq.getDataOwner();
            if (!gebruiker.canValidateDataOwner(dao)) {
                context.getMessages().add(new SimpleMessage("error.dataowner.unvalidatable", dao.getName()));
                return new ForwardResolution(JSP);
            }
        }
        if (validated) {
            rseq.setValidator(gebruiker.getFullname());
            rseq.setValidationTime(new Date());
        } else {
            rseq.setValidator(null);
            rseq.setValidationTime(null);
        }
        return new ForwardResolution(JSP);
    }

    public Resolution validateAll() throws Exception {
        if (!gebruiker.isInRole(Role.BEHEERDER)) {
            DataOwner dao = rseq.getDataOwner();
            if (!gebruiker.canValidateDataOwner(dao)) {
                context.getMessages().add(new SimpleMessage("error.dataowner.unvalidatable", dao.getName()));
                return new ForwardResolution(JSP);
            }
        }

        if (validatedAll) {
            rseq.validateAll(gebruiker.getFullname(), new Date());
        } else {
            rseq.validateAll(null, null);
        }
        return new ForwardResolution(JSP);
    }

    private static String treeUpdateJson(String action, EditorTreeObject to, HttpServletRequest request) throws Exception {
        return treeUpdateJson(action, to, request, false);
    }

    private static String treeUpdateJson(String action, EditorTreeObject to, HttpServletRequest request, boolean includeChildren) throws Exception {
        JSONObject update = new JSONObject();
        String id = null, parentId = null;
        if (to instanceof Activation) {
            id = "a:" + ((Activation) to).getId();
            parentId = "ag:" + ((Activation) to).getActivationGroup().getId();
        } else if (to instanceof ActivationGroup) {
            id = "ag:" + ((ActivationGroup) to).getId();
            parentId = "rseq:" + ((ActivationGroup) to).getRoadsideEquipment().getId();
        } else if (to instanceof RoadsideEquipment) {
            id = "rseq:" + ((RoadsideEquipment) to).getId();
        }
        update.put("action", action);
        update.put("id", id);
        update.put("parentId", parentId);
        update.put("object", to.serializeToJson(request, includeChildren));
        return update.toString();
    }

    // <editor-fold desc="Getters and Setters">
    public ActionBeanContext getContext() {
        return context;
    }

    public void setContext(ActionBeanContext context) {
        this.context = context;
    }
    
    public RoadsideEquipment getRseq() {
        return rseq;
    }

    public void setRseq(RoadsideEquipment rseq) {
        this.rseq = rseq;
    }

    public boolean isValidated() {
        return validated;
    }

    public void setValidated(boolean validated) {
        this.validated = validated;
    }

    public DataOwner getDataOwner() {
        return dataOwner;
    }

    public void setDataOwner(DataOwner dataOwner) {
        this.dataOwner = dataOwner;
    }

    public Gebruiker getGebruiker() {
        return gebruiker;
    }

    public void setGebruiker(Gebruiker gebruiker) {
        this.gebruiker = gebruiker;
    }

    public Integer getActivationGroupCount() {
        return activationGroupCount;
    }

    public void setActivationGroupCount(Integer activationGroupCount) {
        this.activationGroupCount = activationGroupCount;
    }

    public boolean isHideForm() {
        return hideForm;
    }

    public void setHideForm(boolean hideForm) {
        this.hideForm = hideForm;
    }

    public String getTreeUpdate() {
        return treeUpdate;
    }

    public void setTreeUpdate(String treeUpdate) {
        this.treeUpdate = treeUpdate;
    }

    public String getActivationIds() {
        return activationIds;
    }

    public void setActivationIds(String activationIds) {
        this.activationIds = activationIds;
    }

    public List<DataOwner> getDataOwners() {
        return dataOwners;
    }

    public void setDataOwners(List<DataOwner> dataOwners) {
        this.dataOwners = dataOwners;
    }

    public boolean isNotEditable() {
        return !gebruiker.canEditDataOwner(rseq.getDataOwner());
    }

    public boolean isNotValidatable() {
        return !gebruiker.canValidateDataOwner(dataOwner);
    }

    public boolean isValidatedAll() {
        return validatedAll;
    }

    public void setValidatedAll(boolean validatedAll) {
        this.validatedAll = validatedAll;
    }
    // </editor-fold>

}
