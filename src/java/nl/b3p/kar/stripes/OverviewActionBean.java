/**
 * Geo-OV - applicatie voor het registreren van KAR meldpunten
 *
 * Copyright (C) 2009-2014 B3Partners B.V.
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

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.SimpleMessage;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.StrictBinding;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.Validate;
import nl.b3p.kar.hibernate.Gebruiker;
import nl.b3p.kar.hibernate.InformMessage;
import nl.b3p.kar.hibernate.RoadsideEquipment;
import nl.b3p.kar.inform.CarrierInformer;
import nl.b3p.kar.inform.CarrierInformerListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stripesstuff.stripersist.Stripersist;

/**
 *
 * @author Meine Toonen
 */
@StrictBinding
@UrlBinding("/action/overview")
public class OverviewActionBean implements ActionBean{
    private static final Log log = LogFactory.getLog(DetermineAllVRITypeActionBean.class);


    private static final String OVERVIEW_CARRIER = "/WEB-INF/jsp/overview/carrier.jsp";
    private static final String OVERVIEW_DATAOWNER = "/WEB-INF/jsp/overview/dataowner.jsp";
    private static final String OVERVIEW_DATAOWNER_SIMPLE = "/WEB-INF/jsp/overview/dataowner_simple.jsp";

    private ActionBeanContext context;

    private List<InformMessage> messages = new ArrayList<InformMessage>();

    @Validate
    private InformMessage message;

    private String rseqIds;

    @Validate
    private RoadsideEquipment rseq;

    @DefaultHandler
    public Resolution overview() {
        EntityManager em = Stripersist.getEntityManager();
        Gebruiker geb = getGebruiker();
        if (geb.isVervoerder()) {
            messages = em.createQuery("From InformMessage where vervoerder = :vervoerder and mailSent = true and mailProcessed = false", InformMessage.class).setParameter("vervoerder", getGebruiker()).getResultList();
            rseqIds = "";
            for (InformMessage msg : messages) {
                rseqIds += ", " + msg.getRseq().getId();
            }
            if (!rseqIds.isEmpty()) {
                rseqIds = rseqIds.substring(2);
            }
            return new ForwardResolution(OVERVIEW_CARRIER);
        } else {
            if(rseq == null){
                messages = em.createQuery("From InformMessage where afzender = :afzender", InformMessage.class).setParameter("afzender", getGebruiker()).getResultList();
                return new ForwardResolution(OVERVIEW_DATAOWNER);
            }else{
                messages = em.createQuery("From InformMessage where afzender = :afzender and rseq = :rseq", InformMessage.class).setParameter("afzender", getGebruiker()).setParameter("rseq", rseq).getResultList();

                return new ForwardResolution(OVERVIEW_DATAOWNER_SIMPLE);
            }
        }

    }

    public Resolution readMessage(){

        EntityManager em = Stripersist.getEntityManager();

        List<InformMessage> messages = em.createQuery("FROM InformMessage where vervoerder = :vervoerder and afzender = :afzender and mailSent = true and mailProcessed = false "
                + "and rseq = :rseq", InformMessage.class)
                .setParameter("vervoerder", message.getVervoerder())
                .setParameter("afzender", message.getAfzender())
                .setParameter("rseq", message.getRseq()).getResultList();

        for (InformMessage msg : messages) {
            msg.setMailProcessed(true);
            msg.setProcessedAt(new Date());
            em.persist(msg);
        }
        em.getTransaction().commit();
        context.getMessages().add(new SimpleMessage("Bericht over kruispunt " + message.getRseq().getDescription() + " succesvol verwerkt."));
        return overview();
    }

    public Resolution testInformer(){

        CarrierInformer ci = new CarrierInformer();
        String url = context.getServletContext().getInitParameter(CarrierInformerListener.PARAM_INFORM_CARRIERS_APPLICATION_URL);
        ci.run(url, "kmail.b3p.nl", "support@b3partners.nl");
        String llalala = "asfasfdasfsdf";
        return new StreamingResolution("plain/text", new StringReader(llalala));
    }

    // <editor-fold desc="Getters and setters" defaultstate="collapsed">
    public ActionBeanContext getContext() {
        return context;
    }

    public void setContext(ActionBeanContext context) {
        this.context = context;
    }

    public List<InformMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<InformMessage> messages) {
        this.messages = messages;
    }

    public InformMessage getMessage() {
        return message;
    }

    public void setMessage(InformMessage message) {
        this.message = message;
    }

    public String getRseqIds() {
        return rseqIds;
    }

    public void setRseqIds(String rseqIds) {
        this.rseqIds = rseqIds;
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

    public RoadsideEquipment getRseq() {
        return rseq;
    }

    public void setRseq(RoadsideEquipment rseq) {
        this.rseq = rseq;
    }
    // </editor-fold>

}
