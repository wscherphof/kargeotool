/**
 * KAR Geo Tool - applicatie voor het registreren van KAR meldpunten               
 *                                                                           
 * Copyright (C) 2018 B3Partners B.V.                                   
 *                                                                           
 * This program is free software: you can redistribute it and/or modify      
 * it under the terms of the GNU Affero General Public License as            
 * published by the Free Software Foundation, either version 3 of the        
 * License, or (at your option) any later version.                           
 *                                                                           
 * This program is distributed in the hope that it will be useful,           
 * but WITHOUT ANY WARRANTY; without even the implied warranty of            
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the              
 * GNU Affero General Public License for more details.                       
 *                                                                           
 * You should have received a copy of the GNU Affero General Public License  
 * along with this program. If not, see <http://www.gnu.org/licenses/>.      
 */
package nl.b3p.kar.stripes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import javax.servlet.http.HttpServletResponse;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.StrictBinding;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.tag.BeanFirstPopulationStrategy;
import net.sourceforge.stripes.validation.Validate;
import nl.b3p.commons.stripes.CustomPopulationStrategy;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Meine Toonen
 */
@UrlBinding("/action/download")
@StrictBinding
@CustomPopulationStrategy(BeanFirstPopulationStrategy.class)
public class DownloadExportActionBean implements ActionBean {

    private static final Log log = LogFactory.getLog(DownloadExportActionBean.class);
    private ActionBeanContext context;

    @Validate
    private String filename;

    @Override
    public ActionBeanContext getContext() {
        return context;
    }

    @Override
    public void setContext(ActionBeanContext context) {
        this.context = context;
    }

    @DefaultHandler
    public Resolution download() {
        try {
            String downloadLocation = context.getServletContext().getInitParameter("download.location");
            File f = new File(downloadLocation, filename);
            FileInputStream fis = new FileInputStream(f);
            return new StreamingResolution("text/plain") {

                @Override
                public void stream(HttpServletResponse response) throws Exception {
                    OutputStream out = response.getOutputStream();
                    try {
                        IOUtils.copy(fis, out);
                    } finally {
                        if (fis != null) {
                            fis.close();
                            f.delete();
                        }
                    }
                }
            }.setAttachment(true).setFilename(filename);
        } catch (FileNotFoundException ex) {
            log.error("Bestand niet gevonden: " + filename, ex);
        } finally {

        }
        return new ForwardResolution(EditorActionBean.class);
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

}
