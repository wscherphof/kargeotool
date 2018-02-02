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
package nl.b3p.kar;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.persistence.EntityManager;
import nl.b3p.kar.hibernate.DataOwner;
import nl.b3p.kar.stripes.ExportActionBean;
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
public class AdminExportJob implements Job {

    private static final Log log = LogFactory.getLog(AdminExportJob.class);

    @Override
    public void execute(JobExecutionContext jec) throws JobExecutionException {

        String fromAddress = jec.getJobDetail().getJobDataMap().getString(CronInitialiser.PARAM_INFORM_ADMINS_FROMADDRESS);
        String toAddress = jec.getJobDetail().getJobDataMap().getString(CronInitialiser.PARAM_INFORM_ADMINS_TOADDRESS);
        run(fromAddress, toAddress);
    }

    public void run(String fromAddress,String toAddress) {
        try {
            createMessage(fromAddress, toAddress);
        } catch(Exception ex){
            log.error("Cannot create messages: ",ex);
        } finally {
        }
    }

    private void createMessage(String fromAddress, String toAddress) {
        File f = null;
        try {
            Stripersist.requestInit();
            EntityManager em = Stripersist.getEntityManager();
            List<DataOwner> dos = em.createQuery("from DataOwner order by omschrijving").getResultList();
            String body = getBody();
            Date d = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy",Locale.forLanguageTag("NL"));
            
            List<List<String>> values = ExportActionBean.getExportValues(dos, em);
            f = ExportActionBean.getAdminExport(values);
            Mailer.sendMail("KAR Geo Tool", fromAddress, toAddress, "[KAR Geo Tool] Nieuw beheerders overzicht voor " + sdf.format(d), body,f, "Beheerdersoverzicht " + sdf.format(d) +".csv");
        } catch (AddressException ex) {
            log.error("Cannot send inform message:", ex);
        } catch (MessagingException ex) {
            log.error("Cannot send inform message:", ex);
        } catch (Exception ex) {
            log.error("Cannot send inform message:", ex);
        }finally{
            Stripersist.requestComplete();
            if(f != null){
                f.delete();
            }
        }
    }
    
    private String getBody() {
        String body = "";
        body += "Beste, <br/>";
        body += "<br/>";
        body += "Hierbij het beheerdersoverzicht van alle beheerders. <br/>";
        body += "Met vriendelijke groet <br/>";
        
        return body;
    }
}
