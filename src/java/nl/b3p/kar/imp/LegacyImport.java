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
    private String where = " WHERE id = 1698 and location is not null";

    public LegacyImport(PrintWriter out) {
        this.out = out;
    }

    private Connection getConnection() throws Exception {
        Properties props = new Properties();
        props.put("user", "geo_ov");
        props.put("password", "YYPlDvZMkrOyYFB94rZ");
        return DriverManager.getConnection("jdbc:postgresql://x17/geo_ov_imported", props);
    }

    public void doImport() {
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
                    SortedSet<Movement> mvmnts = getMovements(rseqId, newRseq, aps);

                    for (Movement movement : mvmnts) {
                        em.persist(movement);
                        newRseq.getMovements().add(movement);
                    }
                    newRseq.setKarAttributes(karAttributes);
                    em.persist(newRseq);
                    em.getTransaction().commit();
                    savedRseqs.add(newRseq);
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
    private SortedSet<Movement> getMovements(Integer rseqId, RoadsideEquipment rseq, Map<Integer, ActivationPoint> aps) {
        SortedSet<Movement> mvmnts = new TreeSet();

        Connection c = null;
        try {
            c = getConnection();
            List<Map<String, Object>> actgroups = new QueryRunner().query(c, "select * from ovitech_activationgroup where rseqid = ?", new MapListHandler(), rseqId);
            for (int i = 0; i < actgroups.size(); i++) {
                Map<String, Object> actGroup = actgroups.get(i);
                Movement m = createMovement(actGroup, rseq, aps);
                mvmnts.add(m);
            }
        } catch (Exception e) {
            e.printStackTrace(out);
        }

        return mvmnts;
    }

    private Movement createMovement(Map<String, Object> activationGroup, RoadsideEquipment rseq, Map<Integer, ActivationPoint> aps) {
        Movement m = new Movement();
        Integer signalgroup = (Integer) activationGroup.get("karsignalgroup");
        m.setNummer(signalgroup);
        m.setRoadsideEquipment(rseq);
        List<MovementActivationPoint> maps = getMovementActivationPoints(activationGroup, aps, m);
        m.setPoints(maps);

        return m;
    }

    private List<MovementActivationPoint> getMovementActivationPoints(Map<String, Object> activationGroup, Map<Integer, ActivationPoint> aps, Movement m) {

        List<MovementActivationPoint> maps = new ArrayList();

        Connection c = null;
        try {
            Integer groupId = (Integer) activationGroup.get("id");
            c = getConnection();
            List<Map<String, Object>> oviAps = new QueryRunner().query(c, "select * from ovitech_activation where groupId = ?", new MapListHandler(), groupId);
            for (int i = 0; i < oviAps.size(); i++) {
                Map<String, Object> activation = oviAps.get(i);
                MovementActivationPoint map = createMap(activation, activationGroup, aps);
                if (map == null) {
                    continue;
                }
                map.setMovement(m);
                maps.add(map);
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
        if (oviTriggerType.equalsIgnoreCase("PRQAA") || oviTriggerType.equalsIgnoreCase("PRQI")) {
            triggerType = ActivationPointSignal.TRIGGER_FORCED;
        } else if (oviTriggerType.equalsIgnoreCase("PRQM")) {
            triggerType = ActivationPointSignal.TRIGGER_MANUAL;
        } else if (oviTriggerType.equalsIgnoreCase("PRQA") || oviTriggerType.equalsIgnoreCase("SDCAS")) {
            triggerType = ActivationPointSignal.TRIGGER_STANDARD;
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

        map.setSignal(activationPointSignal);
        // ToDo virtual local loop number
        // ToDo vehicletypes

        return map;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="PreProcessing methods">
    private void preProcess(Connection c) throws Exception {
        out.println("***********");
        out.println("Begin pre-processing.");
        out.println("***********");
        initDaoCodes(c);
        initActivations();
        initKarattributes();
        out.println("***********");
        out.println("Pre-processing finished.");
        out.println("***********");
    }

    private void initActivations() {
        out.println("Initializing activations:");

        Connection c = null;
        try {
            c = getConnection();
            List<Map<String, Object>> rseqs = new QueryRunner().query(c, "select * from ovitech_rseq" + where, new MapListHandler());
            for (Map<String, Object> rseq : rseqs) {

                Integer rseqId = (Integer) rseq.get("id");
                out.print("RSEQ: " + rseqId);
                if (rseqId == null) {
                    continue;
                }
                Map<Integer, ActivationPoint> rseqAps = new HashMap();
                activations.put(rseqId, rseqAps);

                List<Map<String, Object>> actGroups = new QueryRunner().query(c, "select * from ovitech_activationgroup where rseqid = ?", new MapListHandler(), rseqId);
                int counter = 1;
                for (Map<String, Object> activationGroup : actGroups) {
                    Integer groupId = (Integer) activationGroup.get("id");
                    Integer signalgroup = (Integer) activationGroup.get("karsignalgroup");

                    List<Map<String, Object>> acts = new QueryRunner().query(c, "select * from ovitech_activation where groupid = ? and location is not null", new MapListHandler(), groupId);
                    for (Map<String, Object> ovActivation : acts) {
                        ActivationPoint ap = createActivationPoint(ovActivation, counter, signalgroup);

                        rseqAps.put((Integer) ovActivation.get("id"), ap);
                        counter++;
                    }
                }
                out.println(" has " + counter + " activations.");
            }

        } catch (Exception e) {
            e.printStackTrace(out);
        }
    }

    private ActivationPoint createActivationPoint(Map<String, Object> activation, int number, int signalgroup) throws ParseException {

        ActivationPoint ap = new ActivationPoint();
        String karcommandtype = (String) activation.get("karcommandtype");
        String label = "";
        if (karcommandtype.equalsIgnoreCase("IN")) {
            label += "I";
        } else if (karcommandtype.equalsIgnoreCase("PRE")) {
            label += "V";
        } else if (karcommandtype.equalsIgnoreCase("OUT")) {
            label += "U";
        } else {
        }
        label += signalgroup;
        ap.setLabel(label);

        ap.setNummer(number);
        Object geom = activation.get("location");

        if (geom != null) {
            GeometryFactory gf = new GeometryFactory(new PrecisionModel(), 28992);
            WKTReader reader = new WKTReader2(gf);
            Point p = (Point) reader.read((String) geom);
            ap.setLocation(p);
        }

        return ap;
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
        JSONArray ptBitmask = new JSONArray();
        JSONArray esBitmask = new JSONArray();
        JSONArray otBitmask = new JSONArray();
        Integer[] disabledDefaults = {0, 3, 4, 7, 8, 9, 11, 15, 16, 17, 19, 20, 21, 22, 23};
        List<Integer> disabled = Arrays.asList(disabledDefaults);

        for (int i = 0; i < 24; i++) {
            if (disabled.contains(i)) {
                ptBitmask.put(false);
                esBitmask.put(false);
                otBitmask.put(false);
            } else {
                ptBitmask.put(true);
                esBitmask.put(true);
                otBitmask.put(true);
            }

        }

        karAttributes.add(new KarAttributes(KarAttributes.SERVICE_PT, ActivationPointSignal.COMMAND_INMELDPUNT, ptBitmask));
        karAttributes.add(new KarAttributes(KarAttributes.SERVICE_OT, ActivationPointSignal.COMMAND_INMELDPUNT, ptBitmask));
        karAttributes.add(new KarAttributes(KarAttributes.SERVICE_ES, ActivationPointSignal.COMMAND_INMELDPUNT, ptBitmask));
        karAttributes.add(new KarAttributes(KarAttributes.SERVICE_PT, ActivationPointSignal.COMMAND_UITMELDPUNT, ptBitmask));
        karAttributes.add(new KarAttributes(KarAttributes.SERVICE_OT, ActivationPointSignal.COMMAND_UITMELDPUNT, ptBitmask));
        karAttributes.add(new KarAttributes(KarAttributes.SERVICE_ES, ActivationPointSignal.COMMAND_UITMELDPUNT, ptBitmask));
        karAttributes.add(new KarAttributes(KarAttributes.SERVICE_PT, ActivationPointSignal.COMMAND_VOORINMELDPUNT, ptBitmask));
        karAttributes.add(new KarAttributes(KarAttributes.SERVICE_OT, ActivationPointSignal.COMMAND_VOORINMELDPUNT, ptBitmask));
        karAttributes.add(new KarAttributes(KarAttributes.SERVICE_ES, ActivationPointSignal.COMMAND_VOORINMELDPUNT, ptBitmask));

        out.println("KAR-attributes initialized.");
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
}
