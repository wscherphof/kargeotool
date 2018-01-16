/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.b3p.kar.stripes;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
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
import nl.b3p.kar.hibernate.ActivationPoint;
import nl.b3p.kar.hibernate.ActivationPointSignal;
import nl.b3p.kar.hibernate.Movement;
import nl.b3p.kar.hibernate.MovementActivationPoint;
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

    // <editor-fold defaultstate="collapsed">
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
    //</editor-fold>

    private List<VehicleType> defaultHDTypes = new ArrayList<>();
    private List<VehicleType> defaultOVTypes = new ArrayList<>();
    
    
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
        EntityManager em = Stripersist.getEntityManager();
        defaultHDTypes = em.createQuery("FROM VehicleType WHERE groep = :groep").setParameter("groep", "Hulpdiensten").getResultList();
        defaultOVTypes = em.createQuery("FROM VehicleType WHERE groep = :groep").setParameter("groep", "OV").getResultList();
        return new StreamingResolution("text/plain") {

            @Override
            public void stream(HttpServletResponse response) throws IOException {

                // haal rseqs op
                // per rseq, bepaal type
                // als type gemixt is
                    // loop over alle bewegingen
                        // als beweging OV is, doe niks
                        // als beweging HD is, doe niks
                        // als beweging gemixt is
                            // dupliceer beweging, inclusief punten
                                // maak van duplicaat HD
                                // maak van origineel OV
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
                    em.getTransaction().commit();
                }

                out.println("Correct handler");
                out.flush();
            }
        };
    }

    private void processRseq(RoadsideEquipment rseq, EntityManager em) throws Exception {
        for (Movement movement : rseq.getMovements()) {
            String type = movement.determineVehicleType();
            if (type.equals(VehicleType.VEHICLE_TYPE_GEMIXT)) {
  
                Movement hd = movement.deepCopy(rseq, em);
                // maak van origineel OV
                changeVehicleType(movement, em, VehicleType.VEHICLE_TYPE_HULPDIENSTEN, defaultOVTypes);
                movement.setVehicleType(VehicleType.VEHICLE_TYPE_OV);
                
                // maak van copy HD
                changeVehicleType(hd, em, VehicleType.VEHICLE_TYPE_OV, defaultHDTypes);
                hd.setVehicleType(VehicleType.VEHICLE_TYPE_HULPDIENSTEN);
                
            }else{
                movement.setVehicleType(type);
            }
        }
        em.persist(rseq);
    }

    private void changeVehicleType(Movement movement, EntityManager em, String vehicleTypeToRemove, List<VehicleType> defaultVehicleTypes) {
        
        List<MovementActivationPoint> mapsToRemove = new ArrayList<>();
        for (MovementActivationPoint map : movement.getPoints()) {
            String t = map.determineVehicleType();
            if (t != null && t.equals(vehicleTypeToRemove)) {
                em.remove(map);
                
                mapsToRemove.add(map);
            } else {
                if (t != null && t.equals(VehicleType.VEHICLE_TYPE_GEMIXT)) {
                    filterVehicleTypes(defaultVehicleTypes, map.getSignal());
                }
            }
        }
        movement.getPoints().removeAll(mapsToRemove);
        processLabelsAPs(movement, vehicleTypeToRemove.equals(VehicleType.VEHICLE_TYPE_OV) ? "H" : "");
    }

    private void processLabelsAPs(Movement m, String prefix){
        for (MovementActivationPoint point : m.getPoints()) {
            ActivationPoint ap = point.getPoint();
            String label = ap.getLabel().toLowerCase();
            if(label.startsWith("va") || label.startsWith("ia") || label.startsWith("ua")){
                ap.setLabel(ap.getLabel().substring(0,1) + prefix + ap.getLabel().substring(2));
            }
        }
    }

    private void filterVehicleTypes(List<VehicleType> allTypes,ActivationPointSignal aps){
        for (Iterator<VehicleType> iterator = aps.getVehicleTypes().iterator(); iterator.hasNext();) {
            VehicleType next = iterator.next();
            boolean found = false;
            for (VehicleType vt : allTypes) {
                if(vt.getNummer() == next.getNummer()){
                    found = true;
                    break;
                }
            }
            if(!found){
                iterator.remove();
            }
        }
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
