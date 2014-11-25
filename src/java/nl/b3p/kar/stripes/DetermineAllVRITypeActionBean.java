/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.b3p.kar.stripes;

import java.io.PrintWriter;
import java.util.List;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletResponse;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.StrictBinding;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.Validate;
import nl.b3p.kar.hibernate.RoadsideEquipment;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stripesstuff.stripersist.Stripersist;

/**
 *
 * @author meine
 */
@StrictBinding
@UrlBinding("/action/determineAllVRIType")
public class DetermineAllVRITypeActionBean implements ActionBean {

    private static final Log log = LogFactory.getLog(DetermineAllVRITypeActionBean.class);

    @Validate
    private boolean commit = false;

    private ActionBeanContext context;

    public ActionBeanContext getContext() {
        return context;
    }

    public void setContext(ActionBeanContext context) {
        this.context = context;
    }

    public boolean isCommit() {
        return commit;
    }

    public void setCommit(boolean commit) {
        this.commit = commit;
    }


    public Resolution determine() {

        return new StreamingResolution("text/plain") {

            @Override
            public void stream(HttpServletResponse response) throws Exception {
                PrintWriter out = new PrintWriter(response.getWriter());

                EntityManager em = Stripersist.getEntityManager();
                List<RoadsideEquipment> rseqs = (List<RoadsideEquipment>) em.createQuery("from RoadsideEquipment").getResultList();
                for (RoadsideEquipment rseq : rseqs) {
                    out.println("******************************");
                    out.println("RSEQ: " +rseq.getDescription());
                    String type = rseq.determineType();
                    out.println("\t "+ type);
                    rseq.setVehicleType(type);
                    em.persist(rseq);
                }
                if(commit){
                    em.getTransaction().commit();
                }

                out.println("Correct handler");
                out.flush();
            }
        };
    }

    @DefaultHandler
    public Resolution defaultHandler() {

        return new StreamingResolution("text/plain") {

            @Override
            public void stream(HttpServletResponse response) throws Exception {
                PrintWriter out = new PrintWriter(response.getWriter());
                out.println("Call correct handler");
                out.flush();
            }
        };
    }

}
