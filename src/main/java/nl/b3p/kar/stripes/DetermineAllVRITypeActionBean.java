/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.b3p.kar.stripes;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import nl.b3p.kar.hibernate.VehicleType;
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

    @Validate
    private Long rseq;

    public Long getRseq() {
        return rseq;
    }

    public void setRseq(Long rseq) {
        this.rseq = rseq;
    }

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
                    out.println("RSEQ: " + rseq.getDescription());
                    String type = rseq.determineType();
                    out.println("\t " + type);
                    rseq.setVehicleType(type);
                    em.persist(rseq);
                }
                if (commit) {
                    em.getTransaction().commit();
                }

                out.println("Correct handler");
                out.flush();
            }
        };
    }

    public Resolution splitLayers() {
        return new StreamingResolution("text/plain") {

            @Override
            public void stream(HttpServletResponse response) throws IOException {

                //haal rseqs op
                // per rseq, bepaal type
                // als type gemixt is
                // maak kopie
                // maak van kopie type Hulpdienst
                // gooi van kopie de punten van type OV weg
                // sla kopie op
                // maak van origineel type OV
                // gooi van origineel alle punten van type Hulpdienst weg
                // sla origineel op
                PrintWriter out = new PrintWriter(response.getWriter());

                EntityManager em = Stripersist.getEntityManager();

                List<RoadsideEquipment> rseqs = null;

                if (rseq != null) {
                    rseqs = (List<RoadsideEquipment>) em.createQuery("from RoadsideEquipment where id = :id").setParameter("id", rseq).getResultList();
                } else {
                    rseqs = (List<RoadsideEquipment>) em.createQuery("from RoadsideEquipment").getResultList();
                }
                for (RoadsideEquipment rseq : rseqs) {
                    out.println("******************************");
                    out.println("RSEQ: " + rseq.getDescription());
                    String type = rseq.determineType();
                    if (type.equals(VehicleType.VEHICLE_TYPE_GEMIXT)) {
                        try {
                            processRseq(rseq,em);
                        } catch (Exception ex) {
                            log.error("cannot process", ex);
                            out.print(ex.getLocalizedMessage());
                        }
                    }
                    out.println("\t " + type);
                    rseq.setVehicleType(type);
                    em.persist(rseq);
                }
                if (commit) {
                    //   em.getTransaction().commit();
                }

                out.println("Correct handler");
                out.flush();
            }
        };
    }

    private void processRseq(RoadsideEquipment rseq, EntityManager em) throws Exception {
        makeHulpdienst(rseq,em);
        makePointsOV(rseq);

    }

    private void makeHulpdienst(RoadsideEquipment ovRseq, EntityManager em) throws Exception {
        em.refresh(ovRseq);
        RoadsideEquipment hd = ovRseq.deepCopy(em);
        hd.setDescription(hd.getDescription() + "HD");
        em.persist(hd);
        em.detach(ovRseq);
        em.getTransaction().commit();
        int a = 0;
    }

    private void makePointsOV(RoadsideEquipment ovRseq) {

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
