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
import java.util.List;
import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.validation.*;
import nl.b3p.incaa.IncaaImport;
import nl.b3p.kar.hibernate.RoadsideEquipment;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Meine Toonen
 */
@StrictBinding
@UrlBinding("/action/ptx_import")
public class ImportPtxActionBean implements ActionBean {

    private final Log log = LogFactory.getLog(this.getClass());
    private final static String OVERVIEW = "/WEB-INF/jsp/import/import_incaa.jsp";
    private ActionBeanContext context;
    @Validate(required = true, on = "doImport")
    private FileBean bestand;

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
    // </editor-fold>

    @DefaultHandler
    public Resolution overview() {

        return new ForwardResolution(OVERVIEW);
    }

    public Resolution doImport() {
        final FileBean zipFile = bestand;
        IncaaImport importer = new IncaaImport();
        try {
            List<RoadsideEquipment> rseqs = importer.importPtx(zipFile.getReader());
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
}
