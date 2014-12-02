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

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.JobExecutionContextImpl;
import org.quartz.impl.StdSchedulerFactory;

/**
 *
 * @author Meine Toonen
 */
public class CarrierInformerListener implements ServletContextListener {

    private static final String PARAM_INFORM_CARRIERS_INTERVAL = "inform.carriers.schedule";
    public static final String PARAM_INFORM_CARRIERS_HOST = "inform.carriers.host";
    public static final String PARAM_INFORM_CARRIERS_FROMADDRESS = "inform.carriers.fromAddress";
    public static final String PARAM_INFORM_CARRIERS_APPLICATION_URL = "application-url";
    private static final Log log = LogFactory.getLog(CarrierInformerListener.class);
    private ServletContext context;
    private Scheduler scheduler;
    private String interval;
    private String applicationUrl;
    private String host;
    private String fromAddress;

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        init(sce);
       /* CarrierInformer ci = new CarrierInformer();

        ci.run(applicationUrl);*/
        if (interval.equalsIgnoreCase("-1") || interval == null) {
            return;
        }


          Properties props = new Properties();
         props.put("org.quartz.scheduler.instanceName", "MonitoringScheduler");
         props.put("org.quartz.threadPool.threadCount", "1");
         props.put("org.quartz.scheduler.interruptJobsOnShutdownWithWait", "true");
         // Job store for monitoring does not need to be persistent
         props.put("org.quartz.jobStore.class", "org.quartz.simpl.RAMJobStore");
         try {
         scheduler = new StdSchedulerFactory(props).getScheduler();

         scheduler.startDelayed(60);

         JobDetail job = JobBuilder.newJob(CarrierInformer.class)
         .withIdentity("InformCarriersJob", "informcarriersgroup")
         .build();

         job.getJobDataMap().put(PARAM_INFORM_CARRIERS_APPLICATION_URL, applicationUrl);
         job.getJobDataMap().put(PARAM_INFORM_CARRIERS_HOST, host);
         job.getJobDataMap().put(PARAM_INFORM_CARRIERS_FROMADDRESS, fromAddress);
         log.info("Scheduling indexing job for expression " + interval + " minutes");

         CronScheduleBuilder cronSchedule = CronScheduleBuilder.cronSchedule(interval);
         Trigger trigger = TriggerBuilder.newTrigger()
         .withIdentity("informcarriersjob", "informcarriersgroup")
         .startNow()
         .withSchedule(cronSchedule)
         .build();

         scheduler.scheduleJob(job, trigger);
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

        interval = context.getInitParameter(PARAM_INFORM_CARRIERS_INTERVAL);
        applicationUrl = context.getInitParameter(PARAM_INFORM_CARRIERS_APPLICATION_URL);
        host = context.getInitParameter(PARAM_INFORM_CARRIERS_HOST);
        fromAddress = context.getInitParameter(PARAM_INFORM_CARRIERS_FROMADDRESS);

    }
}
