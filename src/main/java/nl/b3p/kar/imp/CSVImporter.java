/**
 * KAR Geo Tool - applicatie voor het registreren van KAR meldpunten
 *
 * Copyright (C) 2018B3Partners B.V.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package nl.b3p.kar.imp;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import nl.b3p.kar.hibernate.ActivationPoint;
import nl.b3p.kar.hibernate.ActivationPointSignal;
import nl.b3p.kar.hibernate.DataOwner;
import nl.b3p.kar.hibernate.Movement;
import nl.b3p.kar.hibernate.MovementActivationPoint;
import nl.b3p.kar.hibernate.RoadsideEquipment;
import nl.b3p.kar.hibernate.VehicleType;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Meine Toonen
 */
public class CSVImporter {

    private final Log log = LogFactory.getLog(this.getClass());

    // <editor-fold desc="CSV Constants" defaultstate="collapsed">
    private final int CSV_COL_RSEQTYPE = 0;
    private final int CSV_COL_DATAOWNER = 1;
    private final int CSV_COL_CROSSINGCODE = 2;
    private final int CSV_COL_TOWN = 3;
    private final int CSV_COL_DESCRIPTION = 4;
    private final int CSV_COL_VALIDFROM = 5;
    private final int CSV_COL_VALIDUNTIL = 6;
    private final int CSV_COL_KARADDRESS = 7;
    private final int CSV_COL_SIGNALGROUP = 8;
    private final int CSV_COL_DIRECTION = 9;
    private final int CSV_COL_MOVEMENTLABEL = 10;
    private final int CSV_COL_MOVEMENTNUMBER = 11;
    private final int CSV_COL_LABELACTIVATIONPOINT = 12;
    private final int CSV_COL_KARTYPE = 13;
    private final int CSV_COL_TRIGGERTYPE = 14;
    private final int CSV_COL_RDX = 15;
    private final int CSV_COL_RDY = 16;
    private final int CSV_COL_DISTANCE = 17;
    private final int CSV_COL_BUS = 18;
    private final int CSV_COL_TRAM = 19;
    private final int CSV_COL_CVV = 20;
    private final int CSV_COL_TAXI = 21;
    private final int CSV_COL_HOV = 22;
    private final int CSV_COL_POLITIE = 23;
    private final int CSV_COL_BRANDWEER = 24;
    private final int CSV_COL_AMBULANCE = 25;
    private final int CSV_COL_POLITIENIETINUNIFORM = 26;
    private final int CSV_COL_MARECHAUSSEE = 27;
    private final int CSV_COL_VIRTUALLOCALLOOPNUMBER = 28;
    // </editor-fold>

    private final Reader fr;

    private CSVParser p;

    private final List<DataOwner> dataowners;
    private final List<VehicleType> vehiclestypes;
    private final Map<Integer, VehicleType> vehicleMap = new HashMap<>();
    private Map<String, String> movementWithVehicleType;
    private List<CSVRecord> records;

    private final int RIJKSDRIEHOEKSTELSEL = 28992;
    private final GeometryFactory gf = new GeometryFactory(new PrecisionModel(), RIJKSDRIEHOEKSTELSEL);

    public CSVImporter(Reader fr, List<DataOwner> dataowners, List<VehicleType> vehiclestypes) {
        this.fr = fr;
        this.dataowners = dataowners;
        this.vehiclestypes = vehiclestypes;
    }

    void init() throws IOException {
        p = new CSVParser(fr, CSVFormat.DEFAULT.withFirstRecordAsHeader().withDelimiter(';'));
       
        Map<String, Integer> header = p.getHeaderMap();

        if (!validateHeader(header)) {
            throw new IllegalArgumentException("Init failed: header invalid");
        }

        Map<Integer, VehicleType> vtByNummer = new HashMap<>();
        for (VehicleType vt : vehiclestypes) {
            vtByNummer.put(vt.getNummer(), vt);
        }

        vehicleMap.put(18, vtByNummer.get(1));
        vehicleMap.put(19, vtByNummer.get(2));
        vehicleMap.put(20, vtByNummer.get(6));
        vehicleMap.put(21, vtByNummer.get(7));
        vehicleMap.put(22, vtByNummer.get(71));
        vehicleMap.put(23, vtByNummer.get(3));
        vehicleMap.put(24, vtByNummer.get(4));
        vehicleMap.put(25, vtByNummer.get(5));
        vehicleMap.put(26, vtByNummer.get(69));
        vehicleMap.put(27, vtByNummer.get(70));
        records = p.getRecords();
        Map<Long, Integer> errors = validate();
        if(!errors.isEmpty()){
            String errorString = "";
            for (Long row : errors.keySet()) {
                errorString += row + " x " + errors.get(row) + ", ";
            }
            errorString = errorString.substring(0, errorString.length() - 2);
            throw new IllegalArgumentException("Er missen waardes in het CSV bestand. Deze staan op regel x kolom: \n" + errorString);
        }
        groupMovementByVehicleType();
    }

    public List<RoadsideEquipment> process() throws FileNotFoundException, ParseException {
        List<RoadsideEquipment> rseqs = new ArrayList<>();
        try {
            init();
            List<List<CSVRecord>> rseqsList = groupByRoadsideEquipment();
            for (List<CSVRecord> r : rseqsList) {
                RoadsideEquipment rseq = processRseq(r);
                rseqs.add(rseq);
            }
        } catch (IOException ex) {
            log.error("Cannot parse csv file", ex);
        }
        return rseqs;
    }

    RoadsideEquipment processRseq(List<CSVRecord> records) throws IOException, ParseException {
        RoadsideEquipment rseq = parseRseq(records);

        Map<String, ActivationPoint> aps = new HashMap<>();

        List<List<CSVRecord>> movementsRecords = groupByMovement(records);
        List<Movement> movements = new ArrayList<>();

        for (List<CSVRecord> movementRecords : movementsRecords) {
            Movement m = processMovement(movementRecords, aps, rseq);
            movements.add(m);
        }

        rseq.getMovements().addAll(movements);

        Set<ActivationPoint> points = rseq.getPoints();
        double x = 0, y = 0;
        int count = 0;
        for (Movement m : movements) {
            for (MovementActivationPoint point : m.getPoints()) {
                if(point.getSignal() != null && point.getSignal().getKarCommandType() == ActivationPointSignal.COMMAND_UITMELDPUNT){
                    x += point.getPoint().getLocation().getX();
                    y += point.getPoint().getLocation().getY();
                    count++;
                }
            }
        }
        x =( x / count) + 5;
        y = y / count;
        rseq.setLocation(gf.createPoint(new Coordinate(x, y)));
        return rseq;
    }

    Movement processMovement(List<CSVRecord> records, Map<String, ActivationPoint> aps, RoadsideEquipment rseq) {
        Movement m = parseMovement(records, rseq);

        List<MovementActivationPoint> maps = new ArrayList<>();
        MovementActivationPoint uit = null;
        for (CSVRecord record : records) {
            MovementActivationPoint map = parseMovementActivationPoint(record, aps, rseq, m);
            if (map.getSignal() != null && map.getSignal().getKarCommandType() == ActivationPointSignal.COMMAND_UITMELDPUNT) {
                uit = map;
            }
            maps.add(map);
        }
        
        if(uit == null){
            throw new IllegalArgumentException("Beweging zonder uitmeldpunt gevonden. Beweging van regel " + records.get(0).getRecordNumber() + " tot " + records.get(records.size() -1).getRecordNumber());
        }
        m.determineVehicleType();
        for (MovementActivationPoint map : maps) {
            String l = getActivationPointLabel(map, m, uit.getSignal().getSignalGroupNumber());
            map.getPoint().setLabel(l);
        }

        return m;
    }

    // <editor-fold desc="Parsing functions" defaultstate="collapsed">
    RoadsideEquipment parseRseq(List<CSVRecord> records) throws ParseException {
        CSVRecord f = records.get(0);

        String type = f.get(CSV_COL_RSEQTYPE);
        String dataowner = f.get(CSV_COL_DATAOWNER);
        String crossingcode = f.get(CSV_COL_CROSSINGCODE);
        String town = f.get(CSV_COL_TOWN);
        String description = f.get(CSV_COL_DESCRIPTION);
        String validfrom = f.get(CSV_COL_VALIDFROM);
        String validuntil = f.get(CSV_COL_VALIDUNTIL);
        String karaddress = f.get(CSV_COL_KARADDRESS);

        DateFormat stomdateformat = new SimpleDateFormat("dd-MM-yyyy");
        String t;
        switch (type) {
            case "VRI":
                t = RoadsideEquipment.TYPE_CROSSING;
                break;
            case "Bewakingssysteem":
                t = RoadsideEquipment.TYPE_GUARD;
                break;
            case "Afsluitsysteem":
                t = RoadsideEquipment.TYPE_BAR;
                break;
            default:
                throw new IllegalArgumentException ("Type verkeerssysteem verkeerd: " + type);
        }

        RoadsideEquipment r = new RoadsideEquipment();
        r.setCrossingCode(crossingcode);
        r.setTown(town);
        r.setKarAddress(Integer.parseInt(karaddress));
        r.setDescription(description);
        r.setType(t);
        r.setValidFrom(validfrom != null && !validfrom.isEmpty() ? stomdateformat.parse(validfrom) : new Date());
        r.setValidUntil(validuntil != null && !validuntil.isEmpty() ? stomdateformat.parse(validuntil) : null);
        r.setDataOwner(findDataOwner(dataowner));
        return r;
    }

    Movement parseMovement(List<CSVRecord> records, RoadsideEquipment rseq) {
        Movement m = new Movement();
        CSVRecord r = records.get(0);
        String movementnumber = r.get(CSV_COL_MOVEMENTNUMBER);
        m.setNummer(Integer.parseInt(movementnumber));
        m.setRoadsideEquipment(rseq);
        return m;
    }

    MovementActivationPoint parseMovementActivationPoint(CSVRecord record, Map<String, ActivationPoint> aps, RoadsideEquipment rseq, Movement m) {
        MovementActivationPoint map = new MovementActivationPoint();
        String id = getActivationPointIdentifier(record);
        if (!aps.containsKey(id)) {
            aps.put(id, parseActivationPoint(record, rseq, m));
        }
        ActivationPoint ap = aps.get(id);
        rseq.getPoints().add(ap);
        map.setPoint(ap);
        String t = record.get(CSV_COL_KARTYPE);
        String beginEndOrActivation;
        switch (t) {
            case "begin":
                beginEndOrActivation = MovementActivationPoint.BEGIN;
                break;
            case "eind":
                beginEndOrActivation = MovementActivationPoint.END;
                break;
            default:
                beginEndOrActivation = MovementActivationPoint.ACTIVATION;
                ActivationPointSignal signal = parseActivationPointSignal(record);
                map.setSignal(signal);
                break;
        }
        map.setMovement(m);
        map.setBeginEndOrActivation(beginEndOrActivation);

        m.getPoints().add(map);

        return map;
    }

    ActivationPoint parseActivationPoint(CSVRecord record, RoadsideEquipment rseq, Movement m) {
        ActivationPoint ap = new ActivationPoint();

        String label = record.get(CSV_COL_LABELACTIVATIONPOINT);
        String rdx = record.get(CSV_COL_RDX);
        String rdy = record.get(CSV_COL_RDY);
        Double x = Double.parseDouble(rdx);
        Double y = Double.parseDouble(rdy);

        if (!(x > 0 && x < 300000 && y > 290000 && y < 630000)) {
            throw new IllegalArgumentException("Coordinaten liggen buiten Nederland.");
        }
        Point point = gf.createPoint(new Coordinate(x, y));

        ap.setNummer(rseq.getPoints().size() + 1);
        ap.setLabel(label);
        ap.setLocation(point);
        ap.setRoadsideEquipment(rseq);
        return ap;
    }

    ActivationPointSignal parseActivationPointSignal(CSVRecord r) {
        ActivationPointSignal aps = new ActivationPointSignal();

        String direction = r.get(CSV_COL_DIRECTION);
        direction = direction != null && !direction.isEmpty() ? direction.replaceAll("-", ",") : null;
        String distance = r.get(CSV_COL_DISTANCE);
        String sg = r.get(CSV_COL_SIGNALGROUP);
        String vlln = r.get(CSV_COL_VIRTUALLOCALLOOPNUMBER);
        Integer virtualLocalLoopNumber = vlln != null && !vlln.isEmpty() ? Integer.parseInt(vlln) : null;
        String tt = r.get(CSV_COL_TRIGGERTYPE);
        String ct = r.get(CSV_COL_KARTYPE);
        
        List<VehicleType> vhts = getVehicleTypes(r);
        
        if(distance == null || distance.isEmpty()){
            throw new IllegalArgumentException("Afstand mist op rij " + r.getRecordNumber());
        }
        
        if(sg == null || sg.isEmpty()){
            throw new IllegalArgumentException("Signaalgroep mist op rij " + r.getRecordNumber());
        }
        
        if(tt == null || tt.isEmpty()){
            throw new IllegalArgumentException("Triggertype mist op rij " + r.getRecordNumber());
        }
        
        if(vhts.isEmpty()){
            throw new IllegalArgumentException("Geen modaliteit ingevuld op regel " + r.getRecordNumber());
        }

        Integer dist = Integer.parseInt(distance);
        Integer signaalgroep = Integer.parseInt(sg);
        
      
        String triggertype = null;
        switch (tt) {
            case "standaard":
                triggertype = ActivationPointSignal.TRIGGER_STANDARD;
                break;
            case "automatisch":
                triggertype = ActivationPointSignal.TRIGGER_MANUAL;
                break;
            case "handmatig":
                triggertype = ActivationPointSignal.TRIGGER_MANUAL;
                break;
        }

        Integer commandtype = null;
        switch (ct) {
            case "voor":
                commandtype = ActivationPointSignal.COMMAND_VOORINMELDPUNT;
                break;
            case "in":
                commandtype = ActivationPointSignal.COMMAND_INMELDPUNT;
                break;
            case "uit":
                commandtype = ActivationPointSignal.COMMAND_UITMELDPUNT;
                break;
        }
        aps.setDirection(direction);
        aps.setDistanceTillStopLine(dist);
        aps.setSignalGroupNumber(signaalgroep);
        aps.setTriggerType(triggertype);
        aps.setKarCommandType(commandtype);
        aps.setVirtualLocalLoopNumber(virtualLocalLoopNumber);
        aps.setVehicleTypes(vhts);

        return aps;
    }
    // </editor-fold>

    // <editor-fold desc="Grouping functions" defaultstate="collapsed">
    void groupMovementByVehicleType (){
        movementWithVehicleType = new HashMap<>();
        Map<String, Set<VehicleType>> ms = new HashMap<>();
        for (CSVRecord r : records) {
            List<VehicleType> vhts = getVehicleTypes(r);
            String movementnumber = r.get(CSV_COL_MOVEMENTNUMBER);
            if(!ms.containsKey(movementnumber)){
                ms.put(movementnumber, new HashSet<VehicleType>());
            }
            Set<VehicleType> v = ms.get(movementnumber);
            v.addAll(vhts);
        }
        for (String movementNumber : ms.keySet()) {
            Set<VehicleType> vh = ms.get(movementNumber);
            String vehicleType = null;
            for (VehicleType vt : vh) {
                if (vehicleType == null) {
                    vehicleType = vt.getGroep();
                } else if (!vehicleType.equals(vt.getGroep())) {
                    throw new IllegalArgumentException("Beweging met zowel hulpdiensten als ov gedetecteerd.");
                }
            }
            movementWithVehicleType.put(movementNumber, vehicleType);
        }
    }
    
    List<List<CSVRecord>> groupByMovement(List<CSVRecord> records) throws IOException {
        Map<Integer, List<CSVRecord>> pointsByMovement = new HashMap<>();
        for (CSVRecord record : records) {
            Integer volgnummer = Integer.parseInt(record.get(CSV_COL_MOVEMENTNUMBER));
            if (!pointsByMovement.containsKey(volgnummer)) {
                pointsByMovement.put(volgnummer, new ArrayList<CSVRecord>());
            }
            pointsByMovement.get(volgnummer).add(record);
        }

        return new ArrayList(pointsByMovement.values());
    }

    List<List<CSVRecord>> groupByRoadsideEquipment() throws IOException {
        Map<String, List<CSVRecord>> pointsByRseq = new HashMap<>();
        for (CSVRecord record : records) {
            String id = getRseqIdentifier(record);

            if (!pointsByRseq.containsKey(id)) {
                pointsByRseq.put(id, new ArrayList<CSVRecord>());
            }
            pointsByRseq.get(id).add(record);
        }

        return new ArrayList<>(pointsByRseq.values());
    }

    // </editor-fold>
    
    // <editor-fold desc="Helper functions" defaultstate="collapsed">
    Map<Long, Integer> validate(){
        Map<Long, Integer> m = new HashMap<>();
        int [] nonEmptyColumns = {0,1,2,3,7,11,13,15,16};
        for (CSVRecord record : records) {
            for (int nonEmptyColumn : nonEmptyColumns) {
                if(record.get(nonEmptyColumn).isEmpty()){
                    m.put(record.getRecordNumber(), nonEmptyColumn);
                }
            }
        }
        return m;
    }
    
    String getActivationPointLabel(MovementActivationPoint map, Movement m, int sg) {
        String label = map.getPoint().getLabel();
        if (label.isEmpty()) {
            String vt = m.getVehicleType();
            String ct;
            if (map.getBeginEndOrActivation().equals(MovementActivationPoint.ACTIVATION)) {
                switch (map.getSignal().getKarCommandType()) {
                    case 1:
                        ct = "In";
                        break;
                    case 2:
                        ct = "UIT";
                        break;
                    case 3:
                        ct = "VOOR";
                        break;
                    default:
                        throw new IllegalArgumentException("Onbekend karcommandtype gevonden: " + map.getSignal().getKarCommandType());
                }
            } else {
                ct = map.getBeginEndOrActivation();
            }

            label = vt.equalsIgnoreCase("Hulpdiensten") ? "H" : "";
            label += ct.substring(0, 1).toUpperCase();
            label += sg;
        }
        return label;
    }

    String getRseqIdentifier(CSVRecord c) {
        String soort = c.get(CSV_COL_RSEQTYPE);
        String beheerder = c.get(CSV_COL_DATAOWNER);
        String aanduiding = c.get(CSV_COL_CROSSINGCODE);
        String plaats = c.get(CSV_COL_TOWN);
        String karadres = c.get(CSV_COL_KARADDRESS);
        return soort + beheerder + aanduiding + plaats + karadres;
    }

    String getActivationPointIdentifier(CSVRecord c) {
        String signaalgroep = c.get(CSV_COL_SIGNALGROUP);
        String kartype = c.get(CSV_COL_KARTYPE);
        String rdx = c.get(CSV_COL_RDX);
        String rdy = c.get(CSV_COL_RDY);
        String movementnumber = c.get(CSV_COL_MOVEMENTNUMBER);
        String vehicleType = movementWithVehicleType.get(movementnumber);

        return vehicleType + signaalgroep + kartype + rdx + rdy;
    }

    List<VehicleType> getVehicleTypes(CSVRecord r) {
        List<VehicleType> vhts = new ArrayList<>();
        for (int i = CSV_COL_BUS; i <= CSV_COL_MARECHAUSSEE; i++) {
            String hasVT = r.get(i);
            if (hasVT != null && hasVT.equalsIgnoreCase("x")) {
                vhts.add(vehicleMap.get(i));
            }
        }
        return vhts;
    }

    DataOwner findDataOwner(String omschrijving) {
        for (DataOwner dataowner : dataowners) {
            if (dataowner.getOmschrijving().equalsIgnoreCase(omschrijving)) {
                return dataowner;
            }
        }
        throw new IllegalArgumentException("Dataowner niet gevonden: " + omschrijving);
    }

    boolean validateHeader(Map<String, Integer> header) {
        return true;
    }

    public void close() throws IOException {
        fr.close();
    }
    // </editor-fold>

}
