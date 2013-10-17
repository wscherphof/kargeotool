/**
 * Geo-OV - applicatie voor het registreren van KAR meldpunten
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
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.validation.*;
import nl.b3p.incaa.IncaaImport;
import nl.b3p.kar.hibernate.Gebruiker;
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

/**
 *
 * @author Meine Toonen
 */
@StrictBinding
@UrlBinding("/action/import")
public class ImportActionBean implements ActionBean {

    private final Log log = LogFactory.getLog(this.getClass());
    private final static String OVERVIEW = "/WEB-INF/jsp/import/import.jsp";
    private ActionBeanContext context;
    @Validate(required = true, on = {"importPtx", "importXml"})
    private FileBean bestand;

    private JSONArray imported = new JSONArray();
    private JSONArray globalErrors = new JSONArray();
    private JSONArray allRseqErrors = new JSONArray();
    
    // <editor-fold desc="getters and setters">
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
    // </editor-fold>

    @DefaultHandler
    public Resolution overview() {

        return new ForwardResolution(OVERVIEW);
    }

    public Resolution importXml() {
        
        try {
            JAXBContext ctx = JAXBContext.newInstance(TmiPush.class);
            Unmarshaller u = ctx.createUnmarshaller();
            Gebruiker g = getGebruiker();
            EntityManager em = Stripersist.getEntityManager();
            TmiPush push = (TmiPush) u.unmarshal(bestand.getInputStream());
            int num = 0;
            List<Kv9Def> defs = push.getRseqs();
            String date = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date());
            
            int rseqDefsPosition = 0, rseqDefPosition = 0;;
            
            // Algemene errors
            
            // TODO check F100, F101, F102
            
            for (Kv9Def kv9Def : defs) {
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
                        JSONArray e = new JSONArray();
                        rseqErrors.put("errors", e);
                        allRseqErrors.put(rseqErrors);
                        
                        List<KV9ValidationError> kvErrors = new ArrayList();
                        boolean fatal = false;
                        roadsideEquipment.validateKV9(kvErrors);
                        for(KV9ValidationError kvError: kvErrors) {
                            e.put(kvError.toJSONObject());
                            if(kvError.isFatal()) {
                                fatal = true;
                            }
                        }
                        
                        rseqErrors.put("fatal", fatal);
                        if(fatal) {
                            continue;
                        }
                        
                        // g.canEditVRI(roadsideEquipment) niet van toepassing:
                        // nieuw ingelezen KV9 RSEQ kan nog geen rechten hebben
                        // (id is null)
                        if(!g.isBeheerder() && !g.canEditDataOwner(roadsideEquipment.getDataOwner())) {
                            e.put(new KV9ValidationError(true, null, "dataownercode", "Beheerder", roadsideEquipment.getDataOwner().getCode(), "U heeft geen rechten om verkeerssystemen voor deze beheerder te importeren"));
                            rseqErrors.put("fatal", Boolean.TRUE);
                            continue;
                        }
                        
                        roadsideEquipment.setMemo(String.format("Geimporteerd uit KV9 XML bestand \"%s\" op %s door %s",
                                bestand.getFileName(),
                                date,
                                g.getFullname()
                        ));
                        em.persist(roadsideEquipment);
                        addImportedRseq(roadsideEquipment);
                        num++;
                    }
                }
            }
            em.getTransaction().commit();
            
            this.context.getMessages().add(new SimpleMessage(("globalErrors: " + globalErrors.toString(4))));
            this.context.getMessages().add(new SimpleMessage(("allRseqErrors: " + allRseqErrors.toString(4))));

            
            this.context.getMessages().add(new SimpleMessage(("Er zijn " + num + " verkeerssystemen succesvol geïmporteerd.")));
        } catch(Exception e) {
            this.context.getValidationErrors().addGlobalError(new SimpleError("Er zijn fouten opgetreden bij het importeren van verkeerssystemen: \n" + ExceptionUtils.getMessage(e)));
            log.error("Import exception", e);
        }
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
            List<RoadsideEquipment> rseqs = importer.importPtx(zipFile.getReader(), getGebruiker());
            this.context.getMessages().add(new SimpleMessage(("Er zijn " + rseqs.size() + " verkeerssystemen succesvol geïmporteerd.")));
        } catch (Exception e) {
            log.error(e);
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
