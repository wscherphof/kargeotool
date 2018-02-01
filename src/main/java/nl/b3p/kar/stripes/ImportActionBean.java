/**
 * KAR Geo Tool - applicatie voor het registreren van KAR meldpunten
 *
 * Copyright (C) 2009-2013 B3Partners B.V.
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
package nl.b3p.kar.stripes;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.persistence.EntityManager;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.validation.*;
import nl.b3p.incaa.IncaaImport;
import nl.b3p.kar.hibernate.ActivationPoint;
import nl.b3p.kar.hibernate.DataOwner;
import nl.b3p.kar.hibernate.Gebruiker;
import nl.b3p.kar.hibernate.Movement;
import nl.b3p.kar.hibernate.MovementActivationPoint;
import nl.b3p.kar.hibernate.RoadsideEquipment;
import nl.b3p.kar.imp.KV9ValidationError;
import nl.b3p.kar.jaxb.Kv9Def;
import nl.b3p.kar.jaxb.RseqDefs;
import nl.b3p.kar.jaxb.TmiPush;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.stripesstuff.stripersist.Stripersist;
import org.w3c.dom.Document;

/**
 *
 * @author Meine Toonen
 */
@StrictBinding
@UrlBinding("/action/import")
public class ImportActionBean implements ActionBean {
    private static final String SESSION_KEY_KV9_UNMARSHALLED_OBJ = "kv9import";

    private final Log log = LogFactory.getLog(this.getClass());
    private final static String OVERVIEW = "/WEB-INF/jsp/import/import.jsp";
    private ActionBeanContext context;
    @Validate(required = true, on = {"importPtx", "importXml"})
    private FileBean bestand;

    @Validate(required = true, on = {"importXmlSelectedRseqs"}, label = "De selectie van te importeren verkeerssystemen")
    private String selectedRseqPositions;

    private JSONArray imported = new JSONArray();
    private JSONArray globalErrors = new JSONArray();
    private JSONArray allRseqErrors = new JSONArray();

    // <editor-fold defaultstate="collapsed" desc="getters and setters">
    public ActionBeanContext getContext() {
        return context;
    }

    public void setContext(ActionBeanContext context) {
        this.context = context;
    }

    public FileBean getBestand() {
        return bestand;
    }

    public void setBestand(FileBean bestand) {
        this.bestand = bestand;
    }

    public JSONArray getImported() {
        return imported;
    }

    public void setImported(JSONArray imported) {
        this.imported = imported;
    }

    public JSONArray getGlobalErrors() {
        return globalErrors;
    }

    public void setGlobalErrors(JSONArray globalErrors) {
        this.globalErrors = globalErrors;
    }

    public JSONArray getAllRseqErrors() {
        return allRseqErrors;
    }

    public void setAllRseqErrors(JSONArray allRseqErrors) {
        this.allRseqErrors = allRseqErrors;
    }

    public String getSelectedRseqPositions() {
        return selectedRseqPositions;
    }

    public void setSelectedRseqPositions(String selectedRseqPositions) {
        this.selectedRseqPositions = selectedRseqPositions;
    }
    // </editor-fold>

    @DefaultHandler
    public Resolution overview() throws JSONException {
        return new ForwardResolution(OVERVIEW);
    }

    public Resolution validateImportXml() {

        try {

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();

            Document d;

            try {
                d = db.parse(bestand.getInputStream());
                context.getRequest().getSession().setAttribute("kv9file", bestand.getFileName());
            } catch(Exception e) {
                this.context.getValidationErrors().addGlobalError(new SimpleError("Fout bij het parsen van het XML bestand: " + ExceptionUtils.getMessage(e)));
                return new ForwardResolution(OVERVIEW);
            } finally {

            }

            TmiPush push;
            try {
                JAXBContext ctx = JAXBContext.newInstance(TmiPush.class);
                Unmarshaller u = ctx.createUnmarshaller();
                push = (TmiPush) u.unmarshal(d);
            } catch(Exception e) {
                this.context.getValidationErrors().addGlobalError(new SimpleError("Fout opgetreden bij het unmarshallen van het XML bestand: \n" + ExceptionUtils.getMessage(e)));
                return new ForwardResolution(OVERVIEW);
            }

            int num = 0;
            List<Kv9Def> defs = push.getRseqs();

            int rseqDefsPosition = 0, rseqDefPosition = 0;;

            // Algemene errors

            // TODO check F100, F101, F102

            for (Kv9Def kv9Def: defs) {
                List<RseqDefs> rseqs = kv9Def.getRoadsideEquipments();
                for (RseqDefs rseqDef: rseqs) {
                    rseqDefsPosition++;
                    // Multiple RSEQDEF per RSEQDEFS are supported although not allowed in schema
                    if(rseqDef.getRseqs().size() > 1) {
                        globalErrors.put(new KV9ValidationError(false, "F103", "RSEQDEFS #" + rseqDefsPosition, null, null, "Per RSEQDEFS is slechts één RSEQDEF toegestaan").toJSONObject());
                    }

                    for(RoadsideEquipment roadsideEquipment: rseqDef.getRseqs()) {
                        rseqDefPosition++;

                        JSONObject rseqErrors = new JSONObject();
                        rseqErrors.put("position", rseqDefPosition);
                        rseqErrors.put("karAddress", roadsideEquipment.getKarAddress());
                        rseqErrors.put("description", roadsideEquipment.getDescription());
                        JSONArray e = new JSONArray();
                        rseqErrors.put("errors", e);
                        allRseqErrors.put(rseqErrors);

                        List<KV9ValidationError> kvErrors = new ArrayList();
                        roadsideEquipment.setVehicleType(roadsideEquipment.determineType());
                        int validationErrors = roadsideEquipment.validateKV9(kvErrors);
                        boolean importFatal = false;
                        for(KV9ValidationError kvError: kvErrors) {
                            e.put(kvError.toJSONObject());
                            if(kvError.isFatal()) {
                                importFatal = true;
                            }
                        }
                        rseqErrors.put("errorCount", validationErrors);

                        rseqErrors.put("fatal", importFatal);
                        rseqErrors.put("checked", Boolean.FALSE);
                        if(importFatal) {
                            continue;
                        }

                        if(!getGebruiker().canEdit(roadsideEquipment)) {
                            e.put(new KV9ValidationError(true, null, "dataownercode", "Beheerder", roadsideEquipment.getDataOwner().getCode(), "U heeft geen rechten om verkeerssystemen voor deze beheerder te importeren"));
                            rseqErrors.put("fatal", Boolean.TRUE);
                            continue;
                        }

                        addImportedRseq(roadsideEquipment);
                        num++;
                        rseqErrors.put("checked", Boolean.TRUE);
                        getContext().getRequest().getSession().setAttribute(SESSION_KEY_KV9_UNMARSHALLED_OBJ, push);
                    }
                }
            }

        } catch(Exception e) {
            this.context.getValidationErrors().addGlobalError(new SimpleError("Er zijn fouten opgetreden bij het importeren van verkeerssystemen: \n" + ExceptionUtils.getMessage(e)));
            log.error("Import exception", e);
        } finally {
            if(bestand != null) {
                try {
                    bestand.delete();
                } catch(IOException ioe) {
                }
            }
        }
        return new ForwardResolution(OVERVIEW);
    }

    public Resolution importXmlSelectedRseqs() {


        TmiPush push = (TmiPush)getContext().getRequest().getSession().getAttribute(SESSION_KEY_KV9_UNMARSHALLED_OBJ);

        if(push == null) {
            this.context.getValidationErrors().addGlobalError(new SimpleError("Kan de geuploade KV9 XML gegevens niet vinden, probeer opnieuw"));
            return new ForwardResolution(OVERVIEW);
        }

        String[] psplit = selectedRseqPositions.split(",");
        int[] selectedIndexes = new int[psplit.length];
        for(int i = 0; i < psplit.length; i++) {
            selectedIndexes[i] = Integer.parseInt(psplit[i]);
        }

        String date = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date());

        int rseqDefsPosition = 0, rseqDefPosition = 0;;
        EntityManager em = Stripersist.getEntityManager();
        for (Kv9Def kv9Def: push.getRseqs()) {
            List<RseqDefs> rseqs = kv9Def.getRoadsideEquipments();
            for (RseqDefs rseqDef: rseqs) {
                rseqDefsPosition++;

                for(RoadsideEquipment roadsideEquipment: rseqDef.getRseqs()) {
                    rseqDefPosition++;

                    if(Arrays.binarySearch(selectedIndexes, rseqDefPosition) < 0) {
                        continue;
                    }

                    // Entities zijn in vorige, gesloten sessie geladen, laad deze opnieuw
                    roadsideEquipment.setDataOwner(DataOwner.findByCode(roadsideEquipment.getDataOwner().getCode()));

                    List<KV9ValidationError> kvErrors = new ArrayList();
                    int validationErrors = roadsideEquipment.validateKV9(kvErrors);
                    boolean importFatal = false;
                    for(KV9ValidationError kvError: kvErrors) {
                        if(kvError.isFatal()) {
                            importFatal = true;
                            break;
                        }
                    }

                    if(importFatal || !getGebruiker().canEdit(roadsideEquipment)) {
                        this.context.getValidationErrors().addGlobalError(new SimpleError("Kan VRI op positie " + rseqDefPosition + " niet importeren!"));
                        continue;
                    }
                    roadsideEquipment.setMemo(String.format("Geimporteerd uit KV9 XML bestand \"%s\" op %s door %s",
                            (String)getContext().getRequest().getSession().getAttribute("kv9file"),
                            date,
                            getGebruiker().getFullname()
                    ));
                    roadsideEquipment.setVehicleType(roadsideEquipment.determineType());
                    roadsideEquipment.setValidationErrors(validationErrors);

                    SortedSet<Movement> movements = roadsideEquipment.getMovements();

                    // First, save a rseq with no movement to prevent nullchecks on not-yet persisted ActivationPoints (or, not yet persisted rseqs when persisting the activationpoint first)
                    roadsideEquipment.setMovements(new TreeSet<Movement>());
                    em.persist(roadsideEquipment);

                    SortedSet<ActivationPoint>points = roadsideEquipment.getPoints();
                    // Now, persist each activationpoint from the movement.movementactivationpoint. Also add it to the set in the roadsideEquipment.
                    for (Movement movement : movements) {
                        for (MovementActivationPoint p : movement.getPoints()) {
                            ActivationPoint point = p.getPoint();
                            points.add(point);
                            em.persist(point);
                        }
                        em.persist(movement);
                    }
                    em.persist(roadsideEquipment);

                    if(Arrays.binarySearch(selectedIndexes, rseqDefPosition) >= 0) {
                        this.context.getMessages().add(new SimpleMessage("VRI geimporteerd: adres " + roadsideEquipment.getKarAddress() + ", " + roadsideEquipment.getDescription()));
                    }
                }
            }
        }

        Stripersist.getEntityManager().getTransaction().commit();

        getContext().getRequest().getSession().removeAttribute(SESSION_KEY_KV9_UNMARSHALLED_OBJ);

        return new ForwardResolution(OVERVIEW);
    }

    private void addImportedRseq(RoadsideEquipment rseq) throws JSONException {
        JSONObject j = rseq.getRseqGeoJSON().getJSONObject("properties");

        j.put("dataOwner", rseq.getDataOwner().getOmschrijving());

        String type = rseq.getType();
        if (type.equalsIgnoreCase("CROSSING")) {
            j.put("type", "VRI");
        } else if (type.equalsIgnoreCase("BAR")) {
            j.put("type", "Afsluitingssysteem");
        } else if (type.equalsIgnoreCase("GUARD")) {
            j.put("type", "Waarschuwingssyteem");
        }

        j.put("pointCount", rseq.getPoints().size());
        j.put("movementCount", rseq.getMovements().size());
        imported.put(j);
    }

    public Resolution importPtx() {
        final FileBean zipFile = bestand;
        IncaaImport importer = new IncaaImport();
        try {
            List<RoadsideEquipment> rseqs = importer.importPtx(zipFile.getReader(), getGebruiker(),this.context);

        } catch (Exception e) {
            log.error("Fout importeren PTX",e);
            this.context.getValidationErrors().addGlobalError(new SimpleError("Er zijn fouten opgetreden bij het importeren van verkeerssystemen: \n" + ExceptionUtils.getMessage(e)));
        } finally {
            try {
                bestand.delete();
            } catch (IOException ex) {
                log.error(ex);
            }
        }
        return new ForwardResolution(OVERVIEW);
    }

    public Gebruiker getGebruiker() {
        final String attribute = this.getClass().getName() + "_GEBRUIKER";
        Gebruiker g = (Gebruiker) getContext().getRequest().getAttribute(attribute);
        if (g != null) {
            return g;
        }
        Gebruiker principal = (Gebruiker) context.getRequest().getUserPrincipal();
        g = Stripersist.getEntityManager().find(Gebruiker.class, principal.getId());
        getContext().getRequest().setAttribute(attribute, g);
        return g;
    }
}
