package nl.b3p.kar.struts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import nl.b3p.commons.services.FormUtils;
import nl.b3p.commons.struts.ExtendedMethodProperties;
import nl.b3p.kar.hibernate.Gebruiker;
import nl.b3p.kar.hibernate.GebruikerDataOwnerRights;
import nl.b3p.kar.hibernate.Role;
import nl.b3p.transmodel.DataOwner;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.validator.DynaValidatorForm;
import org.hibernate.Hibernate;
import org.json.JSONArray;
import org.json.JSONObject;

public class GebruikersAction extends BaseDatabaseAction {

    private static final String FORM = "form";

    @Override
    protected Map getActionMethodPropertiesMap() {
        Map map = new HashMap();
        map.put("save", new ExtendedMethodProperties("save", FORM, FORM));
        map.put("delete", new ExtendedMethodProperties("delete", FORM, FORM));
        map.put("edit", new ExtendedMethodProperties("edit", FORM, FORM));
        map.put("create", new ExtendedMethodProperties("create", FORM, FORM));
        map.put("list", new ExtendedMethodProperties("list", FORM, FORM));
        return map;
    }

    @Override
    protected ActionForward getUnspecifiedAlternateForward(ActionMapping mapping, HttpServletRequest request) {
        return mapping.findForward(FORM);
    }

    public ActionForward unspecified(ActionMapping mapping, DynaValidatorForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        /* standaard actie is list */
        return list(mapping, form, request, response);
    }

    public ActionForward cancelled(ActionMapping mapping, DynaValidatorForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        /* bij cancel knop clear confirmAction en ga naar list mode */
        DynaValidatorForm dynaForm = (DynaValidatorForm)form;
        dynaForm.set("confirmAction", null);
        dynaForm.set("id", null);
        return list(mapping, form, request, response);
    }

    public ActionForward list(ActionMapping mapping, DynaValidatorForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        createLists(form, request);
        return mapping.findForward(FORM);
    }

    public ActionForward create(ActionMapping mapping, DynaValidatorForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        form.initialize(mapping);
        createLists(form, request);
        form.set("id", -1);
        return mapping.findForward(FORM);
    }

    private void createLists(DynaValidatorForm form, HttpServletRequest request) throws Exception {
        EntityManager em = getEntityManager();
        request.setAttribute("gebruikers", em.createQuery("from Gebruiker order by id").getResultList());
        request.setAttribute("availableRoles", em.createQuery("from Role order by id").getResultList());
        List dataOwners = em.createQuery("from DataOwner" + /* where type = :type */ " order by id")
                /*.setParameter("type", DataOwner.TYPE_ROOW)*/
                .getResultList();
        request.setAttribute("dataOwners", dataOwners);

        JSONArray dataOwnersJson = new JSONArray();
        for(Iterator it = dataOwners.iterator(); it.hasNext();) {
            DataOwner dao = (DataOwner)it.next();
            JSONObject daoJson = new JSONObject();
            daoJson.put("id", dao.getId());
            daoJson.put("code", dao.getCode());
            daoJson.put("name", dao.getName());
            dataOwnersJson.put(daoJson);
        }
        request.setAttribute("dataOwnersJson", dataOwnersJson);
    }

    private Gebruiker getGebruiker(DynaValidatorForm form, HttpServletRequest request, boolean createNew) throws Exception {
        EntityManager em = getEntityManager();

        Integer id = (Integer) form.get("id");

        if(id == null) {
            return null;
        }

        if(id.intValue() == -1 && createNew) {
            Gebruiker g = new Gebruiker();
            return g;
        }
        Gebruiker g = em.find(Gebruiker.class, id);

        if(g == null) {
            addMessage(request, "error.notfound.arg", "gebruiker #" + id);
            form.set("id", null);
            return null;
        } else {
            return g;
        }
    }

    private void populateGebruikerForm(Gebruiker g, DynaValidatorForm form, HttpServletRequest request) {
        form.set("id", g.getId());
        form.set("username", g.getUsername());
        form.set("fullName", g.getFullname() == null ? "" : g.getFullname());
        form.set("email", g.getEmail() == null ? "" : g.getEmail());
        form.set("phone", g.getPhone() == null ? "" : g.getPhone());
        form.set("password", "");
        Iterator it=g.getRoles().iterator();
        Integer[] roles=new Integer[g.getRoles().size()];
        for(int i=0; it.hasNext(); i++){
            roles[i]=((Role)it.next()).getId();
        }
        form.set("roles",roles);

        request.setAttribute("gebruiker", g);

        List dataOwnersEditable = new ArrayList();
        List dataOwnersValidatable = new ArrayList();

        for(Iterator<GebruikerDataOwnerRights> it2 = g.getDataOwnerRights().values().iterator(); it2.hasNext();) {
            GebruikerDataOwnerRights dor = it2.next();
            if(dor.isEditable()) {
                dataOwnersEditable.add(dor.getDataOwner().getId() + "");
            }
            if(dor.isValidatable()) {
                dataOwnersValidatable.add(dor.getDataOwner().getId() + "");
            }
        }
        
        form.set("dataOwnersEditable", dataOwnersEditable.toArray(new String[]{}));
        form.set("dataOwnersValidatable", dataOwnersValidatable.toArray(new String[]{}));

        /* Sorteer values van map op id van dataowner, voor consistente volgorde */
        List<GebruikerDataOwnerRights> dataOwnerRights = new ArrayList<GebruikerDataOwnerRights>();
        dataOwnerRights.addAll(g.getDataOwnerRights().values());
        Collections.sort(dataOwnerRights, new Comparator() {
            public int compare(Object o1, Object o2) {
                GebruikerDataOwnerRights dor1 = (GebruikerDataOwnerRights)o1;
                GebruikerDataOwnerRights dor2 = (GebruikerDataOwnerRights)o2;
                Hibernate.initialize(dor1.getDataOwner());
                Hibernate.initialize(dor2.getDataOwner());
                return dor1.getDataOwner().getId().compareTo(dor2.getDataOwner().getId());
            }
        });
        request.setAttribute("dataOwnerRights", dataOwnerRights);
    }

    private void populateGebruikerObject(DynaValidatorForm form, Gebruiker g, HttpServletRequest request) throws Exception {
        EntityManager em = getEntityManager();
        g.setUsername(form.getString("username"));
        g.setFullname(FormUtils.nullIfEmpty(form.getString("fullName")));
        g.setEmail(FormUtils.nullIfEmpty(form.getString("email")));
        g.setPhone(FormUtils.nullIfEmpty(form.getString("phone")));
        String pw = FormUtils.nullIfEmpty(form.getString("password"));
        if(pw != null) {
            g.changePassword(request, pw);
            form.set("password", null);
        }
        Integer[] roles= (Integer[]) form.get("roles");
        Set s = new HashSet();
        for (int i=0; i < roles.length; i ++){
            Role r = em.find(Role.class, roles[i]);
            if (r!=null)
                s.add(r);
        }
        g.setRoles(s);

        String[] dataOwnersEditable = (String[])form.get("dataOwnersEditable");
        String[] dataOwnersValidatable = (String[])form.get("dataOwnersValidatable");

        g.getDataOwnerRights().clear();
        em.flush();
        for(int i = 0; i < dataOwnersEditable.length; i++) {
            DataOwner dao = em.find(DataOwner.class, Integer.parseInt(dataOwnersEditable[i]));
            g.setDataOwnerRight(dao, Boolean.TRUE, null);
        }
        for(int i = 0; i < dataOwnersValidatable.length; i++) {
            DataOwner dao = em.find(DataOwner.class, Integer.parseInt(dataOwnersValidatable[i]));
            g.setDataOwnerRight(dao, null, Boolean.TRUE);
        }
    }
    
    public ActionForward edit(ActionMapping mapping, DynaValidatorForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        Gebruiker g = getGebruiker(form, request, false);
        if(g == null) {
                return list(mapping, form, request, response);
        }
        populateGebruikerForm(g, form, request);
        createLists(form, request);

        return mapping.findForward(FORM);
    }

    public ActionForward save(ActionMapping mapping, DynaValidatorForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        EntityManager em = getEntityManager();

        ActionErrors errors = form.validate(mapping, request);
        if(!errors.isEmpty()) {
            addMessages(request, errors);
            createLists(form, request);
            return mapping.findForward(FORM);
        }

        Gebruiker g = getGebruiker(form, request, true);
        if(g == null) {
            return list(mapping, form, request, response);
        }

        /* Controles of er al een andere persoon met dezelfde unique properties
         * als in het form staan bestaat. Dit moet voor het veranderen van het
         * persistant object.
         */

        /* check of username al bestaat */
        Gebruiker bestaandeUsername = null;
        try {
            bestaandeUsername = (Gebruiker)em.createQuery("from Gebruiker g where g.id <> :editingId and lower(g.username) = lower(:username)")
                    .setParameter("editingId", g.getId() == null ? new Integer(-1) : new Integer(g.getId()))
                    .setParameter("username", form.getString("username"))
                    .getSingleResult();
        } catch(NoResultException nre) {
            /* debiele API */
        }
        if(bestaandeUsername != null) {
            addMessage(request, "gebruiker.save.usernameBestaat");
            createLists(form, request);
            return mapping.findForward(FORM);
        }

        populateGebruikerObject(form, g, request);

        em.merge(g);
        em.flush();

        populateGebruikerForm(g, form, request);
        createLists(form, request);

        addMessage(request, "gebruiker.save.success");

        return mapping.findForward(FORM);
    }

    public ActionForward delete(ActionMapping mapping, DynaValidatorForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        EntityManager em = getEntityManager();

        if(!"delete".equals(form.get("confirmAction"))) {
            addMessage(request, "gebruiker.delete.confirm");
            form.set("confirmAction", "delete");
            edit(mapping, form, request, response);
            return mapping.findForward(FORM);
        }

        Gebruiker g = getGebruiker(form, request, false);
        if(g == null) {
            return list(mapping, form, request, response);
        }

        em.remove(g);
        em.flush();

        /* XXX pas bij committen krijg je constraint/save exceptions, maar dan
         * is al wel succes message toegevoegd. Dit in request attribute saven
         * en superclass pas bij Tx successvol commit laten toevoegen of methode
         * laten aanroepen die dat doet?
         * Nu al committen kan niet omdat list() weer uitleest? Of dat in nieuwe
         * Tx
         */
        addMessage(request, "gebruiker.delete.success");

        form.set("id", null);

        return list(mapping, form, request, response);
    }
}