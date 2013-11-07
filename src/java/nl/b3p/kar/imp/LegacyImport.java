package nl.b3p.kar.imp;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.persistence.EntityManager;
import nl.b3p.kar.hibernate.ActivationPoint;
import nl.b3p.kar.hibernate.ActivationPointSignal;
import nl.b3p.kar.hibernate.DataOwner;
import nl.b3p.kar.hibernate.KarAttributes;
import nl.b3p.kar.hibernate.Movement;
import nl.b3p.kar.hibernate.MovementActivationPoint;
import nl.b3p.kar.hibernate.RoadsideEquipment;
import nl.b3p.kar.hibernate.VehicleType;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.geotools.geometry.jts.WKTReader2;
import org.json.JSONArray;
import org.json.JSONException;
import org.stripesstuff.stripersist.Stripersist;

/**
 *
 * @author Matthijs Laan
 */
public class LegacyImport {

    private PrintWriter out;
    private Map<Integer, String> daoCodes = new HashMap();
    private Map<Integer, Map<Integer, ActivationPoint>> activations = new HashMap();
    private List<KarAttributes> karAttributes = new ArrayList();
    private String where = "";
    private Map<String, List<VehicleType>> vehicleTypes = new HashMap();
    
    private Map<Integer,ActivationPoint> leaveAnnouncementActivationpoints = new HashMap();

    private int leaveAnnouncementCounter = -1;
    public LegacyImport(PrintWriter out) {
        this.out = out;
    }

    private Connection getConnection() throws Exception {
        Properties props = new Properties();
        props.put("user", "geo_ov");
        props.put("password", "YYPlDvZMkrOyYFB94rZ");
        return DriverManager.getConnection("jdbc:postgresql://x17/geo_ov_imported", props);
    }

    public void doImport(boolean checkBeforeImport) {
        out.println("Opening connection...");

        Connection c = null;
        List<RoadsideEquipment> savedRseqs = new ArrayList();
        try {
            c = getConnection();
            EntityManager em = Stripersist.getEntityManager();
            preProcess(c);
            out.println("***********");
            out.println("Begin importing");
            out.println("***********");

            List<Map<String, Object>> rseqs = new QueryRunner().query(c, "select * from ovitech_rseq" + where, new MapListHandler());

            for (Map<String, Object> rseq : rseqs) {
                try {
                    if (!em.getTransaction().isActive()) {
                        em.getTransaction().begin();
                    }
                    Integer rseqId = (Integer) rseq.get("id");
                    out.printf("Importing rseq #%d \"%s\"...\n", rseqId, rseq.get("description"));

                    RoadsideEquipment newRseq = new RoadsideEquipment();
                    newRseq.setDataOwner(em.find(DataOwner.class, daoCodes.get((Integer) rseq.get("dataownerid"))));

                    GeometryFactory gf = new GeometryFactory(new PrecisionModel(), 28992);
                    WKTReader reader = new WKTReader2(gf);
                    Point p = (Point) reader.read((String) rseq.get("location"));
                    newRseq.setLocation(p);

                    newRseq.setTown((String)rseq.get("town"));
                    newRseq.setKarAddress((Integer) rseq.get("radioaddress"));
                    Date validFrom = (Date) rseq.get("validfrom");
                    if (validFrom == null) {
                        validFrom = new Date();
                    }
                    newRseq.setValidFrom(validFrom);
                    newRseq.setValidUntil((Date) rseq.get("inactivefrom"));

                    String oldType = (String) rseq.get("type");
                    if ("CROSS".equals(oldType)) {
                        newRseq.setType(RoadsideEquipment.TYPE_CROSSING);
                    } else if ("CLOSE".equals(oldType)) {
                        newRseq.setType(RoadsideEquipment.TYPE_BAR);
                    } else if ("PIU".equals(oldType)) {
                        newRseq.setType(RoadsideEquipment.TYPE_GUARD);
                    } else {
                        out.printf("Invalid type: %s, skipping\n", oldType);
                        continue;
                    }
                    newRseq.setCrossingCode((String) rseq.get("suppliertypenumber"));
                    newRseq.setDescription((String) rseq.get("description"));
                    //newRseq
                    em.persist(newRseq);


                    Map<Integer, ActivationPoint> aps = activations.get(rseqId);
                    SortedSet<ActivationPoint> activationPoints = new TreeSet();
                    for (Integer actId : aps.keySet()) {
                        ActivationPoint ap = aps.get(actId);
                        ap.setRoadsideEquipment(newRseq);
                        em.persist(ap);
                        activationPoints.add(ap);
                    }
                    newRseq.setPoints(activationPoints);
                    em.flush();
                    SortedSet<Movement> mvmnts = getMovements(rseqId, newRseq, aps,c);

                    for (Movement movement : mvmnts) {
                        em.persist(movement);
                        newRseq.getMovements().add(movement);
                    }
                    newRseq.setKarAttributes(karAttributes);
         
                    if(checkBeforeImport && existsInDb(newRseq)){
                        em.getTransaction().rollback();
                    }else{
                        em.persist(newRseq);
                        em.getTransaction().commit();
                        savedRseqs.add(newRseq);
                    }
                    //out.println("JSON format: " + newRseq.getJSON().toString(4));
                } catch (Exception e) {
                    e.printStackTrace(out);
                    if (em.getTransaction().isActive()) {
                        em.getTransaction().rollback();
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace(out);
        } finally {
            if (c != null) {
                out.println("Closing connection.");
                DbUtils.closeQuietly(c);
                out.println("***********");
                out.println("End importing");
                out.println("***********");
            }
        }

        postProcess(savedRseqs);
    }

    // <editor-fold defaultstate="collapsed" desc="Creating of correct datastructures (MAPs, Movements, etc.)">
    private SortedSet<Movement> getMovements(Integer rseqId, RoadsideEquipment rseq, Map<Integer, ActivationPoint> aps, Connection c) {
        SortedSet<Movement> mvmnts = new TreeSet();

        try {
            List<Map<String, Object>> actgroups = new QueryRunner().query(c, "select * from ovitech_activationgroup where rseqid = ?", new MapListHandler(), rseqId);
            for (int i = 0; i < actgroups.size(); i++) {
                Map<String, Object> actGroup = actgroups.get(i);
                Movement m = createMovement(actGroup, rseq, aps,c);
                mvmnts.add(m);
            }
        } catch (Exception e) {
            e.printStackTrace(out);
        }

        return mvmnts;
    }

    private Movement createMovement(Map<String, Object> activationGroup, RoadsideEquipment rseq, Map<Integer, ActivationPoint> aps, Connection c) {
        Movement m = new Movement();
        Integer signalgroup = (Integer) activationGroup.get("karsignalgroup");
        m.setNummer(signalgroup);
        m.setRoadsideEquipment(rseq);
        List<MovementActivationPoint> maps = getMovementActivationPoints(activationGroup, aps, m,c);
        m.setPoints(maps);

        return m;
    }

    private List<MovementActivationPoint> getMovementActivationPoints(Map<String, Object> activationGroup, Map<Integer, ActivationPoint> aps, Movement m, Connection c) {

        List<MovementActivationPoint> maps = new ArrayList();

        boolean leaveannouncement = (Boolean) activationGroup.get("leaveannouncement");
        Map<String, Object> activationInfoForLeaveAnnouncement  = null;
        try {
            Integer groupId = (Integer) activationGroup.get("id");
            List<Map<String, Object>> oviAps = new QueryRunner().query(c, "select * from ovitech_activation where groupId = ? order by index", new MapListHandler(), groupId);
            for (int i = 0; i < oviAps.size(); i++) {
                Map<String, Object> activation = oviAps.get(i);
                MovementActivationPoint map = createMap(activation, activationGroup, aps);
                if (map == null) {
                    continue;
                }
                map.setMovement(m);
                maps.add(map);
                if(leaveannouncement){
                    activationInfoForLeaveAnnouncement = activation;
                }
                
            }
            
            if(leaveannouncement && activationInfoForLeaveAnnouncement != null){
                Map<String, Object> activation = new HashMap();
                activation.put("id",groupId );
                activation.put("karcommandtype","OUT" );
                activation.put("karsignalgroup",activationInfoForLeaveAnnouncement.get("karsignalgroup") );
                activation.put("triggertype",activationInfoForLeaveAnnouncement.get( "triggertype") );
                activation.put("kardistancetillstopline",0.0 );
                activation.put("karusagetype", activationInfoForLeaveAnnouncement.get("karusagetype"));
                MovementActivationPoint map = createMap(activation, activationGroup, leaveAnnouncementActivationpoints);
                if (map != null) {
                    map.setMovement(m);
                    maps.add(map);
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace(out);
        }


        return maps;
    }

    private MovementActivationPoint createMap(Map<String, Object> activation, Map<String, Object> activationGroup, Map<Integer, ActivationPoint> aps) throws Exception {
        MovementActivationPoint map = new MovementActivationPoint();
        Integer apId = (Integer) activation.get("id");
        String oviKarcommandtype = (String) activation.get("karcommandtype");
        Integer karsignalgroup = (Integer) activationGroup.get("karsignalgroup");
        String oviTriggerType = (String) activation.get("triggertype");
        Double distance = (Double) activation.get("kardistancetillstopline");
        if(distance == null){
            distance = 0.0;
        }
        String karusagetype = (String) activation.get("karusagetype");


        map.setBeginEndOrActivation(MovementActivationPoint.ACTIVATION);
        ActivationPoint ap = aps.get(apId);
        if (ap == null) {
            return null;
        }
        map.setPoint(ap);

        ActivationPointSignal activationPointSignal = new ActivationPointSignal();

        /*
         *  Mail Marcel Fick:  06.09.2013 09:20 
         PRQA: Keuze aan vervoerder: Halteknop of automatisch
         PRQM: Altijd halteknop
         PRQAA: Altijd automatisch
         SDCAS: Keuze aan vervoerder: Halteknop of automatisch
         PRQI: Altijd automatisch
         */
        String triggerType = null;
        if (oviTriggerType.equalsIgnoreCase("PRQAA")) {
            triggerType = ActivationPointSignal.TRIGGER_FORCED;
        } else if (oviTriggerType.equalsIgnoreCase("PRQM")) {
            triggerType = ActivationPointSignal.TRIGGER_MANUAL;
        } else if (oviTriggerType.equalsIgnoreCase("PRQA") || oviTriggerType.equalsIgnoreCase("SDCAS")) {
            triggerType = ActivationPointSignal.TRIGGER_STANDARD;
        } else if (oviTriggerType.equalsIgnoreCase("PRQI")) {
            return null;
        }

        int commandType = -1;
        if (oviKarcommandtype.equalsIgnoreCase("IN")) {
            commandType = ActivationPointSignal.COMMAND_INMELDPUNT;
        } else if (oviKarcommandtype.equalsIgnoreCase("PRE")) {
            commandType = ActivationPointSignal.COMMAND_VOORINMELDPUNT;
        } else if (oviKarcommandtype.equalsIgnoreCase("OUT")) {
            commandType = ActivationPointSignal.COMMAND_UITMELDPUNT;
        } else {
            throw new Exception("Unknown kar command type: " + oviKarcommandtype);
        }

        activationPointSignal.setTriggerType(triggerType);
        activationPointSignal.setSignalGroupNumber(karsignalgroup);
        activationPointSignal.setDistanceTillStopLine(distance.intValue());
        activationPointSignal.setKarCommandType(commandType);
        activationPointSignal.setVehicleTypes(getVehicleTypes(karusagetype));
        
        map.setSignal(activationPointSignal);

        return map;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="PreProcessing methods">
    private void preProcess(Connection c) throws Exception {
        out.println("***********");
        out.println("Begin pre-processing.");
        out.println("***********");
        initDaoCodes(c);
        initActivations(c);
        initVehicleTypes();
        initKarattributes();
        out.println("***********");
        out.println("Pre-processing finished.");
        out.println("***********");
    }

    private void initActivations(Connection c) {
        out.println("Initializing activations:");

        try {
            List<Map<String, Object>> rseqs = new QueryRunner().query(c, "select * from ovitech_rseq" + where, new MapListHandler());
            for (Map<String, Object> rseq : rseqs) {

                Integer rseqId = (Integer) rseq.get("id");
                out.print("RSEQ: " + rseqId);
                if (rseqId == null) {
                    continue;
                }
                String updater = (String) rseq.get("update");
                Map<Integer, ActivationPoint> rseqAps = new HashMap();
                activations.put(rseqId, rseqAps);

                List<Map<String, Object>> actGroups = new QueryRunner().query(c, "select * from ovitech_activationgroup where rseqid = ?", new MapListHandler(), rseqId);
                int counter = 1;
                for (Map<String, Object> activationGroup : actGroups) {
                    Integer groupId = (Integer) activationGroup.get("id");
                    Integer signalgroup = (Integer) activationGroup.get("karsignalgroup");
                    boolean isLeaveAnnouncement = (Boolean) activationGroup.get("leaveannouncement");
                    Object geom = activationGroup.get("stoplinelocation");
                    
                    String karusagetype = "";
                    List<Map<String, Object>> acts = new QueryRunner().query(c, "select * from ovitech_activation where groupid = ?", new MapListHandler(), groupId);
                    for (Map<String, Object> ovActivation : acts) {
                        ActivationPoint ap = createActivationPoint(ovActivation, counter, signalgroup,activationGroup);
                        if(ap != null){
                            rseqAps.put((Integer) ovActivation.get("id"), ap);
                            counter++;
                        }
                        if(isLeaveAnnouncement){
                            karusagetype =(String)ovActivation.get("karusagetype");
                        }
                    }
                    
                    if(isLeaveAnnouncement && geom != null && updater != null && !updater.equalsIgnoreCase("Import INCAA")){ 
                        // Maak een uitmeldpunt obv geometrie van activationgroup
                        Map<String,Object> activation = new HashMap();
                        activation.put("karcommandtype", "OUT");
                        activation.put("location", geom);
                        activation.put("karusagetype", karusagetype);
                        ActivationPoint ap = createActivationPoint(activation, counter, (Integer)activationGroup.get("karsignalgroup"), activationGroup);
                        if(ap != null){
                            leaveAnnouncementActivationpoints.put(groupId, ap);
                            rseqAps.put(getLeaveAnnouncementId(), ap);
                            counter++;
                        }
                    }
                    
                }
            }

        } catch (Exception e) {
            e.printStackTrace(out);
        }
    }

    private ActivationPoint createActivationPoint(Map<String, Object> activation, int number, int signalgroup,Map<String, Object> activationGroup) throws ParseException {

        ActivationPoint ap = new ActivationPoint();
        
        String karcommandtype = (String) activation.get("karcommandtype");
        
        String label = createLabel(activation,signalgroup);
        ap.setLabel(label);

        ap.setNummer(number);
        Object geom = activation.get("location");

        if (geom == null) {
            return null;
        }

        GeometryFactory gf = new GeometryFactory(new PrecisionModel(), 28992);
        WKTReader reader = new WKTReader2(gf);
        Point p = (Point) reader.read((String) geom);
        ap.setLocation(p);

        return ap;
    }
    
    private String createLabel(Map<String, Object> activation,int signalgroup ){
        String label = "";
        String karcommandtype = (String) activation.get("karcommandtype");
        String karusagetype = (String) activation.get("karusagetype");
        if (karcommandtype.equalsIgnoreCase("IN")) {
            label += "I";
        } else if (karcommandtype.equalsIgnoreCase("PRE")) {
            label += "V";
        } else if (karcommandtype.equalsIgnoreCase("OUT")) {
            label += "U";
        } else {
        }
        
        if (karusagetype.equalsIgnoreCase("PT")) {
            // geen extra toevoeging label
        }else if(karusagetype.equalsIgnoreCase("ES")) {
            label += "H";
        }else {
            label += "A";
        }
        // Prepend 0 with values smaller than 10
        if(signalgroup < 10){
            label += "0";
        }
        label += signalgroup;
        return label;
    }
    

    private void initDaoCodes(Connection c) throws Exception {
        out.println("Initializing Dao codes:");
        List<Map<String, Object>> dataOwners = new QueryRunner().query(c, "select * from ovitech_dao", new MapListHandler());
        for (Map<String, Object> dao : dataOwners) {
            Integer id = (Integer) dao.get("id");
            String code = (String) dao.get("code");

            if (code.equals("RWSZH")) {
                code = "RWSDZH";
            } else if (code.equals("RWSNH")) {
                code = "RWSDNH";
            } else if (code.equals("RWSZL")) {
                code = "RWSDZL";
            } else if (code.equals("RWSIJS")) {
                code = "RWSDIJG";
            } else if (code.equals("OVITECH")) {
                code = "B3P";
            } else if (code.equals("CBSPV27")) {
                code = "CBSPV0007";
            } else if (code.equals("CBSPV28")) {
                code = "CBSPV0008";
            } else if (code.equals("CBSPV26")) {
                code = "CBSPV0006";
            } else if (code.equals("CBSPV24")) {
                code = "CBSPV0012";
            } else if (code.equals("CBSPV29")) {
                code = "CBSPV0009";
            } else if (code.equals("YLA")) {
                code = "GOVI";
            }
            daoCodes.put(id, code);
        }
        out.println("daoCodes: " + daoCodes);
    }

    private void initKarattributes() throws JSONException {
        out.println("Initializing KAR-attributes:");
        karAttributes = RoadsideEquipment.getDefaultKarAttributes();
        out.println("KAR-attributes initialized.");
    }

    private void initVehicleTypes() {
        EntityManager em = Stripersist.getEntityManager();
        
        // PT
        VehicleType bus = em.find(VehicleType.class, 1);
        VehicleType tram = em.find(VehicleType.class, 2);
        VehicleType hov = em.find(VehicleType.class, 71);
        List<VehicleType> pt = new ArrayList();
        pt.add(hov);
        pt.add(tram);
        pt.add(bus);
        vehicleTypes.put(KarAttributes.SERVICE_PT, pt);
        
        // ES
        VehicleType politie = em.find(VehicleType.class, 3);
        VehicleType brandweer = em.find(VehicleType.class, 4);
        VehicleType ambulance = em.find(VehicleType.class, 5);
        List<VehicleType> es = new ArrayList();
        es.add(politie);
        es.add(brandweer);
        es.add(ambulance);
        vehicleTypes.put(KarAttributes.SERVICE_ES, es);
        
        // OT
        VehicleType cvv = em.find(VehicleType.class, 6);
        VehicleType taxi = em.find(VehicleType.class, 7);
        VehicleType pniu = em.find(VehicleType.class, 69);
        VehicleType marechaussee = em.find(VehicleType.class, 70);
        
        List<VehicleType> ot = new ArrayList();
        ot.add(cvv);
        ot.add(taxi);
        ot.add(pniu);
        ot.add(marechaussee);
        vehicleTypes.put(KarAttributes.SERVICE_OT, ot);
        
        
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="PostProcessing methods">
    private void postProcess(List<RoadsideEquipment> rseqs) {
        out.println("***********");
        out.println("Begin post-processing.");
        out.println("***********");

        out.println("***********");
        out.println("Post-processing finished.");
        out.println("***********");
    }
    // </editor-fold>

    private List<VehicleType> getVehicleTypes(String karusagetype) {
        List<VehicleType> types = new ArrayList();

        if (karusagetype.equalsIgnoreCase("PT")) {
            types.addAll(vehicleTypes.get(KarAttributes.SERVICE_PT));
        }else if(karusagetype.equalsIgnoreCase("ES")) {
            types.addAll(vehicleTypes.get(KarAttributes.SERVICE_ES));
        }else if(karusagetype.equalsIgnoreCase("ESPT")){
            types.addAll(vehicleTypes.get(KarAttributes.SERVICE_PT));
            types.addAll(vehicleTypes.get(KarAttributes.SERVICE_ES));
        }else if(karusagetype.equalsIgnoreCase("ALL")){
            types.addAll(vehicleTypes.get(KarAttributes.SERVICE_PT));
            types.addAll(vehicleTypes.get(KarAttributes.SERVICE_ES));
            types.addAll(vehicleTypes.get(KarAttributes.SERVICE_OT));
        }

        return types;
    }
    
    
    private boolean existsInDb(RoadsideEquipment rseq){
        return rseq.hasDuplicateKARAddressWithinDistance(500);
    }
    
    private int getLeaveAnnouncementId(){
        return --leaveAnnouncementCounter;
    }
}
