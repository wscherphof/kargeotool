/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.b3p.kar.stripes;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
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
import static nl.b3p.kar.hibernate.VehicleType.VEHICLE_TYPE_GEMIXT;
import static nl.b3p.kar.hibernate.VehicleType.VEHICLE_TYPE_HULPDIENSTEN;
import static nl.b3p.kar.hibernate.VehicleType.VEHICLE_TYPE_OV;
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
                List<Long> rseqs = null;

                if (rseq != null) {
                    rseqs = Collections.singletonList(rseq);
                } else {
                    rseqs = (List<Long>) em.createQuery("Select id from RoadsideEquipment").getResultList();
                }
                for (Long id : rseqs) {
                    if(!em.getTransaction().isActive()){
                        em.getTransaction().begin();
                    }
                    RoadsideEquipment rseq = em.find(RoadsideEquipment.class, id);
                    
                    out.println("******************************");
                    rseq.print(out);
                    out.println("------------------------------");
                    String type = rseq.determineType();
                    if (type != null && type.equals(VehicleType.VEHICLE_TYPE_GEMIXT)) {
                        try {
                            processRseq(rseq,em, out);
                        } catch (Exception ex) {
                            log.error("cannot process", ex);
                            out.print(ex.getLocalizedMessage());
                        }
                    }
                    try {
                        out.println("\t " + type);
                        rseq.setVehicleType(type);
                        
                        out.println("After Split:");

                        rseq.print(out);
                        em.persist(rseq);
                        if (commit) {
                            em.getTransaction().commit();
                        }
                    } catch (Exception e) {
                        log.error("Error committing:",e);
                        out.println(e.getLocalizedMessage());
                    }
                }

                out.println("Correct handler");
                out.flush();
            }
        };
    }

    private void processRseq(RoadsideEquipment rseq, EntityManager em,PrintWriter out ) throws Exception {
        
        //em.refresh(rseq);
        
        List<Long> mids = new ArrayList<>();
        for (Movement m : rseq.getMovements()) {
            mids.add(m.getId());
        }
        
        // maak movements
        List<Movement> movementsToProcess = new ArrayList<>();
        for (Long mid : mids) {
            Movement movement = em.find(Movement.class, mid);
            String type = movement.determineVehicleType();
          //  if (type != null && type.equals(VEHICLE_TYPE_GEMIXT)) {
                Movement hd = movement.deepCopy(rseq, em);
                hd.setVehicleType(VEHICLE_TYPE_HULPDIENSTEN);
                movementsToProcess.add(hd);
                
                // maak van origineel OV
                movement.setVehicleType(VEHICLE_TYPE_OV);
                movementsToProcess.add(movement);
           /* }else{
                movement.setVehicleType(type);
                movementsToProcess.add(movement);
            }*/
        }
        
        // filter maps eruit
        for (Movement movement : movementsToProcess) {
            String type = movement.getVehicleType();
            if(type.equals(VEHICLE_TYPE_OV)){
                changeVehicleType(movement, em, VEHICLE_TYPE_HULPDIENSTEN, VEHICLE_TYPE_OV, defaultOVTypes, out);
            }else{
                changeVehicleType(movement, em, VEHICLE_TYPE_OV, VEHICLE_TYPE_HULPDIENSTEN, defaultHDTypes, out);
            }
        }
        
        
        // check of er invalid movements ontstaan zijn, zo ja: verwijder ze
        List<Movement> movementsToRemove= new ArrayList<>();
        for (Movement movement : rseq.getMovements()) {

            if (!isMovementValid(movement)) {
                out.println("Movement invalid: " + movement.getNummer() + " (" + movement.getId() + ")");
                List<MovementActivationPoint> mapsToRemove = new ArrayList<>();
                for (MovementActivationPoint map : movement.getPoints()) {
                    em.remove(map);
                    mapsToRemove.add(map);
                }
                movement.getPoints().removeAll(mapsToRemove);
                movementsToRemove.add(movement);
                em.remove(movement);
            }
        }
        rseq.getMovements().removeAll(movementsToRemove);
        
        // Kijk of er APs bestaan die niet meer gebruikt worden
        
        Set<ActivationPoint> usedAps = new HashSet<>();
        for (Movement movement : rseq.getMovements()) {
            for (MovementActivationPoint point : movement.getPoints()) {
                usedAps.add(point.getPoint());
            }
        }
        Set<ActivationPoint> apsToRemove = new HashSet<>(rseq.getPoints());
        
        apsToRemove.removeAll(usedAps);
        out.println("Removing activationpoints: ");
        out.println("xxxxxxxxxxxxx");
        for (ActivationPoint activationPoint : apsToRemove) {
            out.println(activationPoint.getId() + ", ");
            List<MovementActivationPoint> ms = em.createQuery("From MovementActivationPoint where point = :p").setParameter("p", activationPoint).getResultList();
            for (MovementActivationPoint m : ms) {
                out.println("Removing map: " + m.getId());
                em.remove(m);
                m.getMovement().getPoints().remove(m);
                // verwijder map uit movement
            }
            em.remove(activationPoint);
            rseq.getPoints().remove(activationPoint);
            
        }
        out.println("xxxxxxxxxxxxx");
        em.persist(rseq);
        //em.getTransaction().commit();
    }

    private void changeVehicleType(Movement movement, EntityManager em, String vehicleTypeToRemove,String vehicleTypeToKeep, List<VehicleType> defaultVehicleTypes,PrintWriter out) {
        List<MovementActivationPoint> mapsToRemove = new ArrayList<>();
        for (MovementActivationPoint map : movement.getPoints()) {
            String t = map.determineVehicleType();
            if (t != null && t.equals(vehicleTypeToRemove)) {
                out.println("remove map: " + map.getId());
                em.remove(map);
                
                mapsToRemove.add(map);
            } else {
                if (t != null && t.equals(VEHICLE_TYPE_GEMIXT)) {
                    filterVehicleTypes(defaultVehicleTypes, map.getSignal());
                    // check of er nog voertuigtypes zijn. zo nee, verwijderen
                    if(map.getSignal().getVehicleTypes().isEmpty()){
                        out.println("remove map: " + map.getId());
                        em.remove(map);
                        mapsToRemove.add(map);
                    }
                }
            }
        }
        
        movement.getPoints().removeAll(mapsToRemove);
        
        // check of map wijst naar activationpoint dat nergens meer wordt gebruikt.
        processLabelsAPs(movement, vehicleTypeToKeep);
    }
    
    private boolean isMovementValid(Movement m){
       
        // Een beweging bevat minimaal een beginpunt of een voorinmeldpunt of een inmeldpunt.
        // heeft uitmeldpunt
        // Heeft eindpunt als OV is
        
        // nu alleen checken op uitcheckpunt
        boolean checkout = false;
        
        for (MovementActivationPoint p : m.getPoints()) {
           
            if(p.getBeginEndOrActivation().equals(MovementActivationPoint.ACTIVATION)){
                
                if(p.getSignal().getKarCommandType() == ActivationPointSignal.COMMAND_UITMELDPUNT){
                    checkout = true;
                }
            }
        }
        return checkout;
    }

    private void processLabelsAPs(Movement m, String vehicleType){
        String prefix = vehicleType.equals(VEHICLE_TYPE_OV) ? "" : "H";
        for (MovementActivationPoint point : m.getPoints()) {
            ActivationPoint ap = point.getPoint();
            if(ap.getLabel() == null || ap.getLabel().isEmpty()){
                continue;
            }
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
