/*
 * Copyright (C) 2014 B3Partners B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.b3p.kar.inform;

import java.util.Date;
import java.util.List;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.persistence.EntityManager;
import nl.b3p.kar.hibernate.InformMessage;
import nl.b3p.kar.hibernate.RoadsideEquipment;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.stripesstuff.stripersist.Stripersist;

/**
 *
 * @author Meine Toonen
 */
public class CarrierInformer implements Job {

    private static final Log log = LogFactory.getLog(CarrierInformer.class);

    public void execute(JobExecutionContext jec) throws JobExecutionException {

        String url = jec.getJobDetail().getJobDataMap().getString(CarrierInformerListener.PARAM_INFORM_CARRIERS_APPLICATION_URL);
        run(url);
    }

    public void run(String url) {
        try {
            Stripersist.requestInit();
            EntityManager em = Stripersist.getEntityManager();
            List<InformMessage> messages = em.createQuery("From InformMessage where mailSent = false", InformMessage.class).getResultList();
            for (InformMessage message : messages) {
                createMessage(message, url);
                em.persist(message);
            }
            em.getTransaction().commit();
        } catch(Exception ex){
            log.error("Cannot create messages: ",ex);
        } finally {
            Stripersist.requestComplete();
        }
    }

    private void createMessage(InformMessage inform, String appUrl) {
        try {
            Properties props = System.getProperties();
            // -- Attaching to default Session, or we could start a new one --
            props.put("mail.smtp.host", "kmail.b3p.nl");
            Session session = Session.getDefaultInstance(props, null);
            // -- Create a new message --
            MimeMessage msg = new MimeMessage(session);
            // -- Set the FROM and TO fields --
            msg.setFrom(new InternetAddress(inform.getAfzender().getEmail()));
            msg.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(inform.getVervoerder().getEmail(), false));

            msg.setSubject("[geo-ov] Er staat een nieuw bericht voor u klaar");
            String body = getBody(inform, appUrl);
            msg.setText(body, "utf-8", "html");

            msg.setSentDate(new Date());
            Transport.send(msg);
            inform.setMailSent(true);
        } catch (AddressException ex) {
            log.error("Cannot send inform message:", ex);
        } catch (MessagingException ex) {
            log.error("Cannot send inform message:", ex);
        }
    }

    private String getBody(InformMessage inform, String appUrl) {
        RoadsideEquipment rseq = inform.getRseq();
        String name = inform.getVervoerder().getFullname() != null ? inform.getVervoerder().getFullname()  : "vervoeder";
        String body = "";
        body += "Beste " + name + ", <br/>";
        body += "<br/>";
        body += "U heeft een bericht via de applicatie geo-ov gekregen van " + inform.getAfzender().getFullname();
        body += ". Dit betreft een update van het kruispunt met KAR-adres " + rseq.getKarAddress() + ": " + rseq.getDescription() + " | " + rseq.getCrossingCode() + ".<br/>";
        body += "Een export kunt u ";
        body += "<a href=\"" + appUrl + "/action/export?exportXml=true&rseq=" + rseq.getId() + "\">hier</a> ";
        body += " downloaden. <br/>";
        body += "Een overzicht vind u ";
        body += "<a href=\"" + appUrl + "/action/overview?carrier=true\">hier</a>.<br/><br/>";
        body += "U wordt vriendelijk verzocht het door te geven als u de wijzingen heeft verwerkt. Dit kan via de overzichtspagina, of via ";
        body += "<a href=\"" + appUrl + "/action/overview?readMessage=true&message=" + inform.getId()  + "\">deze link</a>.<br/><br/>";
        body += "Met vriendelijke groet.";
        return body;
    }
}
