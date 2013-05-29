/**
 * Geo-OV - applicatie voor het registreren van KAR meldpunten
 *
 * Copyright (C) 2009-2013 B3Partners B.V.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package nl.b3p.kar.stripes;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.controller.LifecycleStage;
import net.sourceforge.stripes.validation.SimpleError;
import net.sourceforge.stripes.validation.Validate;
import nl.b3p.kar.hibernate.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.stripesstuff.stripersist.Stripersist;

/**
 * Stripes klasse welke de edit functionaliteit regelt.
 *
 * @author Meine Toonen meinetoonen@b3partners.nl
 */
@StrictBinding
@UrlBinding("/action/rights")
public class RightsActionBean implements ActionBean {

    private static final Log log = LogFactory.getLog(RightsActionBean.class);
    private static final String JSP = "/WEB-INF/jsp/beheer/rights.jsp";
    private static final String detail = "/WEB-INF/jsp/beheer/vriDetail.jsp";
    private ActionBeanContext context;
    private JSONArray rseqs;
    @Validate
    private RoadsideEquipment rseq;
    private JSONObject rseqJson;
    private String dataOwner;
    private JSONArray gebruikers;

    /**
     * Stripes methode waarmee de view van het edit proces wordt voorbereid.
     *
     * @return Stripes Resolution view
     * @throws Exception
     */
    @DefaultHandler
    public Resolution view() throws Exception {

        EntityManager em = Stripersist.getEntityManager();

        return new ForwardResolution(JSP);
    }

    @After(stages = LifecycleStage.BindingAndValidation)
    private void lists() {

        EntityManager em = Stripersist.getEntityManager();
        try {
            Gebruiker g = getGebruiker();
            Set<DataOwner> rights = g.getEditableDataOwners();

            boolean isBeheerder = g.isBeheerder();
            String query = "FROM RoadsideEquipment WHERE ";
            if (!rights.isEmpty()) {
                query += "dataOwner in :list OR ";
            }
            query += "true = :beheerder";

            Query q = em.createQuery(query).setParameter("beheerder", isBeheerder);
            if (!rights.isEmpty()) {
                q.setParameter("list", rights);
            }
            List<RoadsideEquipment> rseqList = q.getResultList();
            rseqs = new JSONArray();
            for (RoadsideEquipment rseqObj : rseqList) {

                JSONObject jRseq = new JSONObject();
                jRseq.put("id", rseqObj.getId());
                jRseq.put("naam", rseqObj.getDescription());
                jRseq.put("karAddress", rseqObj.getKarAddress());
                jRseq.put("dataowner", rseqObj.getDataOwner().getOmschrijving());
                String type = rseqObj.getType();
                if (type.equalsIgnoreCase("CROSSING")) {
                    jRseq.put("type", "VRI");
                } else if (type.equalsIgnoreCase("BAR")) {
                    jRseq.put("type", "Afsluitingssysteem");
                } else if (type.equalsIgnoreCase("GUARD")) {
                    jRseq.put("type", "Waarschuwingssyteem");
                }
                rseqs.put(jRseq);
            }
        } catch (JSONException e) {
            context.getValidationErrors().add("VRI", new SimpleError("Kan geen verkeerssystemen ophalen.", e.getMessage()));
        }

        List<Gebruiker> gebruikersList = em.createQuery("FROM Gebruiker order by fullname", Gebruiker.class).getResultList();
        gebruikers = new JSONArray();
        for (Gebruiker gebruiker : gebruikersList) {
            try {
                JSONObject geb = new JSONObject();
                geb.put("id", gebruiker.getId());
                geb.put("username", gebruiker.getUsername());
                geb.put("fullname", gebruiker.getFullname());
                gebruikers.put(geb);
            } catch (JSONException ex) {
                log.error("Kan gebruiker niet ophalen:",ex);
            }
        }
    }

    public Resolution edit() throws JSONException {

        EntityManager em = Stripersist.getEntityManager();
        rseqJson = rseq.getJSON();
     
        dataOwner = rseq.getDataOwner().getOmschrijving();

        return new ForwardResolution(detail);
    }

    public Gebruiker getGebruiker() {
        final String attribute = this.getClass().getName() + "_GEBRUIKER";
        Gebruiker g = (Gebruiker) getContext().getRequest().getAttribute(attribute);
        if (g != null) {
            return g;
        }
        Gebruiker principal = (Gebruiker) context.getRequest().getUserPrincipal();
        g = Stripersist.getEntityManager().find(Gebruiker.class, principal.getId());
        getContext().getRequest().setAttribute(attribute, g);
        return g;
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

    public JSONArray getGebruikers() {
        return gebruikers;
    }

    public void setGebruikers(JSONArray gebruikers) {
        this.gebruikers = gebruikers;
    }

    public JSONObject getRseqJson() {
        return rseqJson;
    }

    public void setRseqJson(JSONObject rseqJson) {
        this.rseqJson = rseqJson;
    }

    public String getDataOwner() {
        return dataOwner;
    }

    public void setDataOwner(String dataOwner) {
        this.dataOwner = dataOwner;
    }

    public JSONArray getRseqs() {
        return rseqs;
    }

    public void setRseqs(JSONArray rseqs) {
        this.rseqs = rseqs;
    }
    // </editor-fold>
}
