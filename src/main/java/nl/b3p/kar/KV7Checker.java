/*
 * Copyright (C) 2018 B3Partners B.V.
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

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 *
 * @author Meine Toonen
 */
public class KV7Checker implements Job {

    private static final Log log = LogFactory.getLog(KV7Checker.class);
    private final int THRESHOLD = 32;

    @Override
    public void execute(JobExecutionContext jec) throws JobExecutionException {

        String fromAddress = jec.getJobDetail().getJobDataMap().getString(CronInitialiser.PARAM_INFORM_KV7CHECKER_FROMADDRESS);
        String toAddress1 = jec.getJobDetail().getJobDataMap().getString(CronInitialiser.PARAM_INFORM_KV7CHECKER_TOADDRESS1);
        String toAddress2 = jec.getJobDetail().getJobDataMap().getString(CronInitialiser.PARAM_INFORM_KV7CHECKER_TOADDRESS2);
        run(fromAddress, toAddress1, toAddress2);
    }

    public void run(String fromAddress, String toAddress1, String toAddress2) {
        Connection conn = null;
        try {
            Context initCtx = new InitialContext();
            DataSource ds = (DataSource) initCtx.lookup("java:comp/env/jdbc/kv7netwerk");
            if (ds != null) {
                log.debug("  KV7Checker(): open connection");
                conn = ds.getConnection();

                Date d = new QueryRunner().query(conn,
                        "select max(processed_date) from data.netwerk", new ScalarHandler<Date>());
                LocalDate d1 = new LocalDate(d);
                LocalDate d2 = new LocalDate(new Date());
                int days = Days.daysBetween(d1, d2).getDays();
                if (days > THRESHOLD) {
                    String body1 = getBody(days, d, "Graag controleren wat de oorzaak is.");
                    String body2 = getBody(days, d, "Een ticket aanmaken en assignen aan Badr.");

                    Mailer.sendMail("KAR Geo Tool", fromAddress, toAddress1, "[KAR Geo Tool] KV7 deze maand niet ontvangen", body1);
                    Mailer.sendMail("KAR Geo Tool", fromAddress, toAddress2, "[KAR Geo Tool] KV7 deze maand niet ontvangen", body2);
                }
            }else{
                log.error("Geen kv7netwerk datasource geconfigureerd");
            }
        } catch (AddressException ex) {
            log.error("Cannot send inform message:", ex);
        } catch (MessagingException ex) {
            log.error("Cannot send inform message:", ex);
        } catch (Exception ex) {
            log.error("Cannot send inform message:", ex);
        } finally {
            DbUtils.closeQuietly(conn);
        }
    }

    private String getBody(int days, Date d, String extramessage) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", Locale.forLanguageTag("NL"));
        String body = "";
        body += "Beste, \n";
        body += "\n";
        body += "Het is " + days + " dagen geleden dat er voor het laatst een KV7 bestand is ontvangen. De laatste keer was op " +sdf.format(d)+ ". " + extramessage;
        body += "\nMet vriendelijke groet \n";

        return body;
    }
}
