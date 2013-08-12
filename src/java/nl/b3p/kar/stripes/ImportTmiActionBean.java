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

import java.io.PrintWriter;
import java.util.Date;
import javax.servlet.http.HttpServletResponse;
import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.validation.*;
import nl.b3p.kar.hibernate.Role;
import nl.b3p.tmi.TmiDbImport;

/**
 *
 * @author Matthijs Laan
 */
@StrictBinding
@UrlBinding("/action/tmi_import")
public class ImportTmiActionBean implements ActionBean {

    private ActionBeanContext context;

    @Validate
    private FileBean bestand;
    
    @Validate
    private String encoding;
    
    @Validate 
    private String jndi;
    
    @Validate
    private String schema;
    
    @Validate
    private int batch = 1;
    
    @Validate
    private boolean transaction;
    
    @Validate
    private String titel;
    
    @Validate
    private Date validFrom;
    
    @Validate
    private Date validUntil;
    
    @Validate
    private String description;
    
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

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public String getJndi() {
        return jndi;
    }

    public void setJndi(String jndi) {
        this.jndi = jndi;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public int getBatch() {
        return batch;
    }

    public void setBatch(int batch) {
        this.batch = batch;
    }

    public boolean isTransaction() {
        return transaction;
    }

    public void setTransaction(boolean transaction) {
        this.transaction = transaction;
    }

    public String getTitel() {
        return titel;
    }

    public void setTitel(String titel) {
        this.titel = titel;
    }

    public Date getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(Date validFrom) {
        this.validFrom = validFrom;
    }

    public Date getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(Date validUntil) {
        this.validUntil = validUntil;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
   
    @HandlesEvent("import")
    public Resolution doImport() {
        
        if(!getContext().getRequest().isUserInRole(Role.BEHEERDER)) {
            throw new IllegalAccessError();
        }
        
        final FileBean zipFile = bestand;
        
        return new StreamingResolution("text/plain") {
            @Override
            public void stream(HttpServletResponse response) throws Exception {
                PrintWriter out = new PrintWriter(response.getWriter());
                out.format("Inlezen zip-bestand \"%s\"...\n", zipFile.getFileName());
                new TmiDbImport(out, jndi, schema, batch, transaction).loadFromZip(zipFile.getInputStream(), zipFile.getFileName(), encoding, titel, validFrom, validUntil, description);
                out.flush();
            }            
        };
    }
}
