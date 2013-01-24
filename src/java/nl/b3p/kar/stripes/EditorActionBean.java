package nl.b3p.kar.stripes;

import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.EntityManager;
import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.validation.Validate;
import nl.b3p.geojson.GeoJSON;
import nl.b3p.kar.hibernate.*;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.stripesstuff.stripersist.Stripersist;

/**
 *
 * @author Meine Toonen meinetoonen@b3partners.nl
 */
@StrictBinding
@UrlBinding("/action/editor")
public class EditorActionBean implements ActionBean {

    private static final Log log = LogFactory.getLog(EditorActionBean.class);
    private static final String JSP = "/WEB-INF/jsp/viewer/editor2.jsp";
    private ActionBeanContext context;
    private boolean magWalapparaatMaken;
    @Validate
    private Integer karAddress;
    @Validate
    private RoadsideEquipment2 rseq;
    
    @Validate
    private String json;
    
    @Validate
    private JSONObject layers;
    private JSONArray vehicleTypesJSON;
    private JSONArray dataOwnersJSON;

    @DefaultHandler
    public Resolution view() throws Exception {

        EntityManager em = Stripersist.getEntityManager();
        Gebruiker principal = (Gebruiker) context.getRequest().getUserPrincipal();
        Gebruiker g = em.find(Gebruiker.class, principal.getId());
        boolean isBeheerder = g.isBeheerder();

        Set s = g.getEditableDataOwners();
        if (s.size() >= 1 || isBeheerder) {
            magWalapparaatMaken = true;
        } else {
            magWalapparaatMaken = false;
        }

        vehicleTypesJSON = new JSONArray();
        for (VehicleType vt : (List<VehicleType>) em.createQuery("from VehicleType order by nummer").getResultList()) {
            JSONObject jvt = new JSONObject();
            jvt.put("nummer", vt.getNummer());
            jvt.put("omschrijving", vt.getOmschrijving());
            vehicleTypesJSON.put(jvt);
        }

        dataOwnersJSON = new JSONArray();
        for (DataOwner2 dao : (List<DataOwner2>) em.createQuery("from DataOwner2 order by code").getResultList()) {
            JSONObject jdao = new JSONObject();
            jdao.put("code", dao.getCode());
            jdao.put("classificatie", dao.getClassificatie());
            jdao.put("companyNumber", dao.getCompanyNumber());
            jdao.put("omschrijving", dao.getOmschrijving());
            dataOwnersJSON.put(jdao);
        }

        return new ForwardResolution(JSP);
    }

    public Resolution gfi() {
        return new StreamingResolution("application/json", "error: no objects found");
    }

    public Resolution rseqInfo2() throws Exception {
        EntityManager em = Stripersist.getEntityManager();

        JSONObject info = new JSONObject();
        info.put("success", Boolean.FALSE);
        try {
            RoadsideEquipment2 rseq2;

            if (rseq != null) {
                rseq2 = rseq;
            } else {
                rseq2 = (RoadsideEquipment2) em.createQuery("from RoadsideEquipment2 where karAddress = :un")
                        .setParameter("un", karAddress)
                        .getSingleResult();
            }

            info.put("rseq", rseq2.getRseqGeoJSON());
            info.put("points", rseq2.getPointsGeoJSON());
            info.put("success", Boolean.TRUE);
        } catch (Exception e) {
            log.error("rseqInfo2 exception", e);
            info.put("error", ExceptionUtils.getMessage(e));
        }
        return new StreamingResolution("application/json", new StringReader(info.toString(4)));
    }

    public Resolution rseqJSON() throws Exception {
        EntityManager em = Stripersist.getEntityManager();

        JSONObject info = new JSONObject();
        info.put("success", Boolean.FALSE);
        try {
            RoadsideEquipment2 rseq2;

            if (rseq != null) {
                rseq2 = rseq;
            } else {
                rseq2 = (RoadsideEquipment2) em.createQuery("from RoadsideEquipment2 where karAddress = :a")
                        .setParameter("a", karAddress)
                        .getSingleResult();
            }

            info.put("roadsideEquipment", rseq2.getJSON());

            info.put("success", Boolean.TRUE);
        } catch (Exception e) {
            log.error("rseqJSON exception", e);
            info.put("error", ExceptionUtils.getMessage(e));
        }
        return new StreamingResolution("application/json", new StringReader(info.toString(4)));
    }

    public Resolution allRseqJSON() throws Exception {
        EntityManager em = Stripersist.getEntityManager();

        JSONObject info = new JSONObject();
        info.put("success", Boolean.FALSE);
        try {
            List<RoadsideEquipment2> rseq2;

            rseq2 = (List<RoadsideEquipment2>) em.createQuery("from RoadsideEquipment2").getResultList();
            JSONArray rseqs = new JSONArray();
            for (RoadsideEquipment2 r : rseq2) {
                rseqs.put(r.getRseqGeoJSON());
            }
            info.put("rseqs", rseqs);

            info.put("success", Boolean.TRUE);
        } catch (Exception e) {
            log.error("allRseqJSON exception", e);
            info.put("error", ExceptionUtils.getMessage(e));
        }
        return new StreamingResolution("application/json", new StringReader(info.toString(4)));
    }

    private static final boolean isNew(JSONObject j) {
        String id = j.optString("id");
        return id != null && id.startsWith("ext-gen");
    }
    
    public Resolution saveOrUpdateRseq() throws Exception {
        EntityManager em = Stripersist.getEntityManager();
        
        JSONObject info = new JSONObject();
        info.put("success", Boolean.FALSE);
        try {
            JSONObject jrseq = new JSONObject(json);

            if(isNew(jrseq)) {
                rseq = new RoadsideEquipment2();
            } else {
                rseq = em.find(RoadsideEquipment2.class, jrseq.getLong("id"));
            }

            rseq.setLocation(GeoJSON.toPoint(jrseq.getJSONObject("location")));
            rseq.setDescription(jrseq.optString("description"));
            rseq.setTown(jrseq.optString("town"));
            rseq.setType(jrseq.getString("type"));
            rseq.setKarAddress(jrseq.getInt("karAddress"));
            rseq.setCrossingCode(jrseq.optString("crossingCode"));
            rseq.setDataOwner(em.find(DataOwner2.class, jrseq.getString("dataOwner")));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            rseq.setValidFrom(jrseq.has("validFrom") ? sdf.parse(jrseq.getString("validFrom")) : null);
            rseq.setValidUntil(jrseq.has("validUntil") ? sdf.parse(jrseq.getString("validUntil")) : null);
            
            JSONArray jpts = jrseq.getJSONArray("points");
            Map<String, ActivationPoint2> pointsByJSONId = new HashMap();
            for(int i = 0; i < jpts.length(); i++) {
                JSONObject jpt = jpts.getJSONObject(i);
                
                ActivationPoint2 p = null;
                if(isNew(jpt)) {
                    p = new ActivationPoint2();
                    p.setRoadsideEquipment(rseq);
                } else {
                    for(ActivationPoint2 ap2: rseq.getPoints()) {
                        if(ap2.getId().equals(jpt.getLong("id"))) {
                            p = ap2;
                            break;
                        }
                    }   
                }
                pointsByJSONId.put(jpt.getString("id"), p);
                p.setNummer(jpt.has("nummer") ?  jpt.getInt("nummer") : null);
                p.setLabel(jpt.optString("label"));
                p.setLocation(GeoJSON.toPoint(jpt.getJSONObject("geometry")));
                if(p.getId() == null) {
                    rseq.getPoints().add(p);
                    em.persist(p);
                }
            }
            // XXX delete niet goed
            rseq.getPoints().retainAll(pointsByJSONId.values());
            
            Set<Long> mIds = new HashSet();
            for(Movement m: rseq.getMovements()) {
                m.getPoints().clear();
                mIds.add(m.getId());
            }
            em.flush();
            if(!mIds.isEmpty()) {
                em.createNativeQuery("delete from movement_activation_point where movement in (:m)")
                    .setParameter("m", mIds)
                    .executeUpdate();
            }
            rseq.getMovements().clear();
            em.flush();

            JSONArray jmvmts = jrseq.getJSONArray("movements");
            for(int i = 0; i < jmvmts.length(); i++) {
                JSONObject jmvmt = jmvmts.getJSONObject(i);
                Movement m = new Movement();
                m.setRoadsideEquipment(rseq);
                m.setNummer(jmvmt.has("nummer") ? jmvmt.getInt("nummer") : null);
                
                JSONArray jmaps = jmvmt.getJSONArray("maps");
                for(int j = 0; j < jmaps.length(); j++) {
                    JSONObject jmap = jmaps.getJSONObject(j);
                    MovementActivationPoint map = new MovementActivationPoint();
                    map.setMovement(m);
                    map.setBeginEndOrActivation(jmap.getString("beginEndOrActivation"));
                    map.setPoint(pointsByJSONId.get(jmap.getString("pointId")));
                    
                    if(MovementActivationPoint.ACTIVATION.equals(map.getBeginEndOrActivation())) {
                        ActivationPointSignal signal = new ActivationPointSignal();
                        map.setSignal(signal);
                        signal.setDistanceTillStopLine(jmap.has("distanceTillStopLine") ? jmap.getInt("distanceTillStopLine") : null);
                        signal.setKarCommandType(jmap.getInt("commandType"));
                        signal.setSignalGroupNumber(jmap.has("signalGroupNumber") ? jmap.getInt("signalGroupNumber") : null);
                        signal.setVirtualLocalLoopNumber(jmap.has("virtualLocalLoopNumber") ? jmap.getInt("virtualLocalLoopNumber") : null);
                        signal.setTriggerType(jmap.getString("triggerType"));
                        JSONArray vtids = jmap.optJSONArray("vehicleTypes");
                        if(vtids != null) {
                            for(int k = 0; k < vtids.length(); k++) {
                                signal.getVehicleTypes().add(em.find(VehicleType.class, vtids.getInt(k)));
                            }
                        }
                    }
                    m.getPoints().add(map);
                }
                if(!m.getPoints().isEmpty()) {
                    m.setNummer(rseq.getMovements().size()+1);
                    em.persist(m);
                    rseq.getMovements().add(m);
                }
            }           
            
            em.persist(rseq);
            em.getTransaction().commit();
            
            info.put("roadsideEquipment", rseq.getJSON());
            info.put("success", Boolean.TRUE);
        } catch (Exception e) {
            log.error("saveOrUpdateRseq exception", e);
            info.put("error", ExceptionUtils.getMessage(e));
        }
        return new StreamingResolution("application/json", new StringReader(info.toString(4)));
    }

    // <editor-fold desc="Getters and Setters">
    public ActionBeanContext getContext() {
        return context;
    }

    public void setContext(ActionBeanContext context) {
        this.context = context;
    }

    public Integer getKarAddress() {
        return karAddress;
    }

    public void setKarAddress(Integer karAddress) {
        this.karAddress = karAddress;
    }

    public JSONObject getLayers() {
        return layers;
    }

    public void setLayers(JSONObject layers) {
        this.layers = layers;
    }

    public boolean isMagWalapparaatMaken() {
        return magWalapparaatMaken;
    }

    public void setMagWalapparaatMaken(boolean magWalapparaatMaken) {
        this.magWalapparaatMaken = magWalapparaatMaken;
    }

    public RoadsideEquipment2 getRseq() {
        return rseq;
    }

    public void setRseq(RoadsideEquipment2 rseq) {
        this.rseq = rseq;
    }

    public JSONArray getDataOwnersJSON() {
        return dataOwnersJSON;
    }

    public void setDataOwnersJSON(JSONArray dataOwnersJSON) {
        this.dataOwnersJSON = dataOwnersJSON;
    }

    public JSONArray getVehicleTypesJSON() {
        return vehicleTypesJSON;
    }

    public void setVehicleTypesJSON(JSONArray vehicleTypesJSON) {
        this.vehicleTypesJSON = vehicleTypesJSON;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }
    // </editor-fold>
}
