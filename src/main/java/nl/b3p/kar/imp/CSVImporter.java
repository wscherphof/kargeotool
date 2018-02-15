/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
import java.util.HashMap;
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
    private final int CSV_COL_SIGNALGROUP =  8;
    private final int CSV_COL_DIRECTION =  9;
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
    private final int CSV_COL_HOV =  22;
    private final int CSV_COL_POLITIE = 23;
    private final int CSV_COL_BRANDWEER = 24;
    private final int CSV_COL_AMBULANCE = 25;
    private final int CSV_COL_POLITIENIETINUNIFORM = 26;
    private final int CSV_COL_MARECHAUSSEE = 27;
    private final int CSV_COL_VIRTUALLOCALLOOPNUMBER = 28;
    // </editor-fold>
    
    private Reader fr;
    
    private CSVParser p;

    private List<DataOwner> dataowners;
    private List<VehicleType> vehiclestypes;
    private Map<Integer, VehicleType> vehicleMap = new HashMap<>();
    
    private final int RIJKSDRIEHOEKSTELSEL = 28992;
    private GeometryFactory gf = new GeometryFactory(new PrecisionModel(), RIJKSDRIEHOEKSTELSEL);
    
    public CSVImporter(Reader fr, List<DataOwner> dataowners, List<VehicleType> vehiclestypes) {
        this.fr = fr;
        this.dataowners = dataowners;
        this.vehiclestypes = vehiclestypes;
         // verzamel zakje karpunten per rseq
        // per rseq: groepeer movements
        // per movement movementactivationpoints maken
    }

    void init() throws IOException {
        p = new CSVParser(fr, CSVFormat.DEFAULT.withFirstRecordAsHeader());
        Map<String, Integer> header = p.getHeaderMap();

        if (!validateHeader(header)) {
            throw new IllegalArgumentException("Init failed: header invalid");
        }
        
        Map<Integer, VehicleType> vtByNummer = new HashMap<>();
        for (VehicleType vt : vehiclestypes) {
            vtByNummer.put(vt.getNummer(), vt);
        }
        
        vehicleMap.put(18,vtByNummer.get(1));
        vehicleMap.put(19,vtByNummer.get(2));
        vehicleMap.put(20,vtByNummer.get(6));
        vehicleMap.put(21,vtByNummer.get(7));
        vehicleMap.put(22,vtByNummer.get(71));
        vehicleMap.put(23,vtByNummer.get(3));
        vehicleMap.put(24,vtByNummer.get(4));
        vehicleMap.put(25,vtByNummer.get(5));
        vehicleMap.put(26,vtByNummer.get(69));
        vehicleMap.put(27,vtByNummer.get(70));
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
        for (ActivationPoint point : points) {
            x += point.getLocation().getX();
            y += point.getLocation().getY();
        }
        x = x / points.size();
        y = y / points.size();
        rseq.setLocation(gf.createPoint(new Coordinate(x, y)));
        return rseq;
    }
    
    Movement processMovement(List<CSVRecord> records, Map<String, ActivationPoint> aps, RoadsideEquipment rseq){
        Movement m = parseMovement(records, aps, rseq);
        
        for (CSVRecord record : records) {
            MovementActivationPoint map = createMovementActivationPoint(record, aps, rseq, m);
           // m.getPoints().add(map);
            //rseq.getPoints().add(map.getPoint());
        }
        
        return m;
    }
    
    MovementActivationPoint createMovementActivationPoint(CSVRecord record, Map<String, ActivationPoint> aps, RoadsideEquipment rseq, Movement m){
        MovementActivationPoint map = new MovementActivationPoint();
        
        String id = getActivationPointIdentifier(record);
        if(!aps.containsKey(id)){
            aps.put(id, parseActivationPoint(record, rseq));
        }
        ActivationPoint ap = aps.get(id);
        rseq.getPoints().add(ap);
        
        String t = record.get(CSV_COL_KARTYPE);
        String beginEndOrActivation;
        switch(t){
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
        map.setPoint(ap);
        map.setMovement(m);
        map.setBeginEndOrActivation(beginEndOrActivation);
        
        m.getPoints().add(map);
        
        return map;
    }

    // <editor-fold desc="Parsing functions" defaultstate="collapsed">
    ActivationPoint parseActivationPoint(CSVRecord record, RoadsideEquipment rseq){
        ActivationPoint ap = new ActivationPoint();
        String label = record.get(CSV_COL_LABELACTIVATIONPOINT);
        
        String rdx = record.get(CSV_COL_RDX);
        String rdy = record.get(CSV_COL_RDY);
        Double x = Double.parseDouble(rdx);
        Double y = Double.parseDouble(rdy);
        Point point = gf.createPoint(new Coordinate(x, y));
        
        ap.setNummer(rseq.getPoints().size() + 1);
        ap.setLabel(label);
        ap.setLocation(point);
        ap.setRoadsideEquipment(rseq);
        return ap;
    }
    
    ActivationPointSignal parseActivationPointSignal(CSVRecord r){
        ActivationPointSignal aps = new ActivationPointSignal();
        
        String direction = r.get(CSV_COL_DIRECTION);
        direction = direction != null && !direction.isEmpty() ? direction.replaceAll("-", ",") : null;
        String distance = r.get(CSV_COL_DISTANCE);
        Integer dist = distance != null && !distance.isEmpty() ? Integer.parseInt(distance) : null;
        Integer signaalgroep = Integer.parseInt(r.get(CSV_COL_SIGNALGROUP));
        String vlln = r.get(CSV_COL_VIRTUALLOCALLOOPNUMBER);
        Integer virtualLocalLoopNumber = vlln != null && !vlln.isEmpty() ? Integer.parseInt(vlln) : null;
        String tt = r.get(CSV_COL_TRIGGERTYPE);
        String ct = r.get(CSV_COL_KARTYPE);
        List<VehicleType> vhts = getVehicleTypes(r);
        
        String triggertype = null;
        switch(tt){
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
        switch(ct){
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
    
    RoadsideEquipment parseRseq(List<CSVRecord> records) throws ParseException{
        CSVRecord f = records.get(0);
        
        String type = f.get(CSV_COL_RSEQTYPE);
        String dataowner = f.get(CSV_COL_DATAOWNER);
        String crossingcode =f.get(CSV_COL_CROSSINGCODE);
        String town = f.get(CSV_COL_TOWN);
        String description = f.get(CSV_COL_DESCRIPTION);
        String validfrom = f.get(CSV_COL_VALIDFROM);
        String validuntil = f.get(CSV_COL_VALIDUNTIL);
        String karaddress = f.get(CSV_COL_KARADDRESS);
        
        DateFormat stomdateformat = new SimpleDateFormat("dd-MM-yyyy");
        String t;
        switch(type){
            default:
            case "VRI":
                t = RoadsideEquipment.TYPE_CROSSING;
                break;
            case "Bewakingssysteem":
                t = RoadsideEquipment.TYPE_GUARD;
                break;
            case "Afsluitsysteem":
                t = RoadsideEquipment.TYPE_BAR;
                break;
        }
        
        RoadsideEquipment r = new RoadsideEquipment();
        r.setCrossingCode(crossingcode);
        r.setTown(town);
        r.setKarAddress(Integer.parseInt(karaddress));
        r.setDescription(description);
        r.setType(t);
        r.setValidFrom(validfrom != null ? stomdateformat.parse(validfrom): null);
        r.setValidUntil(validuntil != null && !validuntil.isEmpty() ? stomdateformat.parse(validuntil): null);
        r.setDataOwner(findDataOwner(dataowner));
        return r;
    }
    
    Movement parseMovement(List<CSVRecord> records, Map<String, ActivationPoint> aps, RoadsideEquipment rseq){
        Movement m = new Movement();
        CSVRecord r = records.get(0);
        String movementnumber = r.get(CSV_COL_MOVEMENTNUMBER);
        m.setNummer(Integer.parseInt(movementnumber));
        m.setRoadsideEquipment(rseq);
        return m;
    }
    // </editor-fold>
    
    // <editor-fold desc="Grouping functions" defaultstate="collapsed">    
    List<List<CSVRecord>> groupByMovement(List<CSVRecord> records) throws IOException {
        Map<Integer, List<CSVRecord>> pointsByMovement = new HashMap<>();
        for (CSVRecord record : records) {
            Integer volgnummer = Integer.parseInt(record.get(CSV_COL_MOVEMENTNUMBER));
            if(!pointsByMovement.containsKey(volgnummer)){
                pointsByMovement.put(volgnummer, new ArrayList<CSVRecord>());
            }
            pointsByMovement.get(volgnummer).add(record);
        }

        return new ArrayList(pointsByMovement.values());
    }

    List<List<CSVRecord>> groupByRoadsideEquipment() throws IOException {
        Map<String, List<CSVRecord>> pointsByRseq = new HashMap<>();
        List<CSVRecord> records = p.getRecords();
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
    String getRseqIdentifier(CSVRecord c) {
        String soort = c.get(CSV_COL_RSEQTYPE);
        String beheerder = c.get(CSV_COL_DATAOWNER);
        String aanduiding = c.get(CSV_COL_CROSSINGCODE);
        String plaats = c.get(CSV_COL_TOWN);
        String karadres = c.get(CSV_COL_KARADDRESS);
        return soort + beheerder + aanduiding + plaats + karadres;
    }
    
    String getActivationPointIdentifier(CSVRecord c) {
        //én signaalgroep én type melding én RD-X én RD-y overeenkomen
        String signaalgroep = c.get(CSV_COL_SIGNALGROUP);
        String kartype = c.get(CSV_COL_KARTYPE);
        String rdx = c.get(CSV_COL_RDX);
        String rdy = c.get(CSV_COL_RDY);
        
        return signaalgroep + kartype + rdx + rdy;
    }
    
    List<VehicleType> getVehicleTypes(CSVRecord r){
        List<VehicleType> vhts = new ArrayList<>();
        for (int i = CSV_COL_BUS; i <= CSV_COL_MARECHAUSSEE; i++) {
            String hasVT = r.get(i);
            if(hasVT != null && hasVT.equalsIgnoreCase("x")){
                vhts.add(vehicleMap.get(i));
            }
        }
        return vhts;
    }
    
    DataOwner findDataOwner(String omschrijving){
        for (DataOwner dataowner : dataowners) {
            if(dataowner.getOmschrijving().equalsIgnoreCase(omschrijving)){
                return dataowner;
            }
        }
        return null;
    }
    
    boolean validateHeader(Map<String, Integer> header) {
        return true;
    }

    public void close() throws IOException{
        fr.close();
    }
    // </editor-fold>

}
