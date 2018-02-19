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

import java.util.Properties;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

/**
 *
 * @author Meine Toonen
 */
public class CronInitialiser implements ServletContextListener {

    private static final String PARAM_INFORM_ADMINS_INTERVAL = "inform.admins.schedule";
    public static final String PARAM_INFORM_ADMINS_FROMADDRESS = "inform.admins.fromAddress";
    public static final String PARAM_INFORM_ADMINS_TOADDRESS = "inform.admins.toAddress";

    private static final String PARAM_INFORM_KV7CHECKER_INTERVAL = "inform.kv7checker.schedule";
    public static final String PARAM_INFORM_KV7CHECKER_FROMADDRESS = "inform.kv7checker.fromAddress";
    public static final String PARAM_INFORM_KV7CHECKER_TOADDRESS1 = "inform.kv7checker.toAddress1";
    public static final String PARAM_INFORM_KV7CHECKER_TOADDRESS2 = "inform.kv7checker.toAddress2";
    
    private static final Log log = LogFactory.getLog(CronInitialiser.class);
    private ServletContext context;
    
    private Scheduler scheduler;
    
    private String informinterval;
    private String informfromAddress;
    private String informtoAddress;
    
    private String kv7checkerinterval;
    private String kv7checkerfromAddress;
    private String kv7checkertoAddress1;
    private String kv7checkertoAddress2;

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        init(sce);
        initAdminExport();
    }
    
    private void initAdminExport(){

        Properties props = new Properties();
        props.put("org.quartz.scheduler.instanceName", "CronInitialiser");
        props.put("org.quartz.threadPool.threadCount", "1");
        props.put("org.quartz.scheduler.interruptJobsOnShutdownWithWait", "true");
        // Job store for monitoring does not need to be persistent
        props.put("org.quartz.jobStore.class", "org.quartz.simpl.RAMJobStore");
        try {
            scheduler = new StdSchedulerFactory(props).getScheduler();

            scheduler.startDelayed(60);

            log.info("Scheduling indexing job for expression " + informinterval + " minutes");

            if (!informinterval.equals("-1")) {
                JobDetail job = JobBuilder.newJob(AdminExportJob.class)
                        .withIdentity("AdminExportJob", "AdminExportgroup")
                        .build();

                job.getJobDataMap().put(PARAM_INFORM_ADMINS_FROMADDRESS, informfromAddress);
                job.getJobDataMap().put(PARAM_INFORM_ADMINS_TOADDRESS, informtoAddress);
                CronScheduleBuilder cronSchedule = CronScheduleBuilder.cronSchedule(informinterval);
                Trigger trigger = TriggerBuilder.newTrigger()
                        .withIdentity("informcarriersjob", "informcarriersgroup")
                        .startNow()
                        .withSchedule(cronSchedule)
                        .build();

                scheduler.scheduleJob(job, trigger);
            }
            
            if (!kv7checkerinterval.equals("-1")) {
                
                JobDetail job = JobBuilder.newJob(AdminExportJob.class)
                        .withIdentity("kv7checkerJob", "kv7checkergroup")
                        .build();

                job.getJobDataMap().put(PARAM_INFORM_KV7CHECKER_FROMADDRESS, kv7checkerfromAddress);
                job.getJobDataMap().put(PARAM_INFORM_KV7CHECKER_TOADDRESS1, kv7checkertoAddress1);
                job.getJobDataMap().put(PARAM_INFORM_KV7CHECKER_TOADDRESS2, kv7checkertoAddress2);

                CronScheduleBuilder cronSchedule = CronScheduleBuilder.cronSchedule(kv7checkerinterval);
                Trigger trigger = TriggerBuilder.newTrigger()
                        .withIdentity("kv7checkerintervaljob", "kv7checkerintervalgroup")
                        .startNow()
                        .withSchedule(cronSchedule)
                        .build();

                scheduler.scheduleJob(job, trigger);
            }
        } catch (SchedulerException ex) {
            log.error("Cannot create scheduler. ", ex);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (scheduler != null) {
            try {
                scheduler.shutdown(true);
            } catch (SchedulerException ex) {
                log.error("Cannot shutdown quartz scheduler. ", ex);
            }
        }
    }

    private void init(ServletContextEvent sce) {
        this.context = sce.getServletContext();

        informinterval = context.getInitParameter(PARAM_INFORM_ADMINS_INTERVAL);
        informfromAddress = context.getInitParameter(PARAM_INFORM_ADMINS_FROMADDRESS);
        informtoAddress = context.getInitParameter(PARAM_INFORM_ADMINS_TOADDRESS);

        kv7checkerinterval = context.getInitParameter(PARAM_INFORM_KV7CHECKER_INTERVAL);
        kv7checkerfromAddress = context.getInitParameter(PARAM_INFORM_KV7CHECKER_FROMADDRESS);
        kv7checkertoAddress1 = context.getInitParameter(PARAM_INFORM_KV7CHECKER_TOADDRESS1);
        kv7checkertoAddress2 = context.getInitParameter(PARAM_INFORM_KV7CHECKER_TOADDRESS2);
    }
}
