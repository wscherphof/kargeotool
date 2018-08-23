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

package nl.b3p.kar;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import nl.b3p.kar.hibernate.Gebruiker;
import nl.b3p.kar.hibernate.RoadsideEquipment;
import nl.b3p.kar.stripes.ExportActionBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stripesstuff.stripersist.Stripersist;

/**
 *
 * @author Meine Toonen
 */
public class ExportCreatorThread extends Thread {

    private static final Log log = LogFactory.getLog(ExportCreatorThread.class);
    private final String exportType;
    private List<RoadsideEquipment> roadsideEquipmentList;
    private final List<RoadsideEquipment> oldList;
    private Gebruiker gebruiker;
    private final String fromAddress;
    private final String appURL;

    private final String downloadLocation = "/home/meine/kardownloads/";

    public ExportCreatorThread(String exportType, List<RoadsideEquipment> roadsideEquipmentList, Gebruiker gebruiker, String fromAddress, String appURL) {
        this.exportType = exportType;
        this.oldList = roadsideEquipmentList;
        this.gebruiker = gebruiker;
        this.fromAddress = fromAddress;
        this.appURL = appURL;
    }

    @Override
    public void run() {
        Stripersist.requestInit();
        EntityTransaction transaction = Stripersist.getEntityManager().getTransaction();
        if (!transaction.isActive()) {
            transaction.begin();
        }
        refreshEntities();
        File f = null;
        File destination = null;
        Date now = new Date();
        DateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String filename = null;
        switch (exportType) {
            case "incaa":
                filename = "HLPXXXXX";
                filename += sdf.format(now);
                filename += ".ptx";
                f = ExportActionBean.exportPtx(null, roadsideEquipmentList);
                break;
            case "kv9":
                try {
                    String prefix = "geo-ov_";
                    if (roadsideEquipmentList.size() == 1) {
                        prefix = "" + roadsideEquipmentList.get(0).getKarAddress();
                    }
                    filename = prefix + "_kv9_" + sdf.format(now) + ".xml";
                    destination = new File(downloadLocation, filename);
                    ByteArrayOutputStream bos = ExportActionBean.exportXml(null, roadsideEquipmentList);
                    try (OutputStream outputStream = new FileOutputStream(destination)) {
                        bos.writeTo(outputStream);
                    } catch (Exception e) {
                        log.error("Error exporting kv9: ", e);
                    }
                } catch (Exception ex) {
                    log.error("Error exporting kv9: ", ex);
                }   break;
            case "csvsimple":
                try {
                    f = ExportActionBean.exportCSVSimple(roadsideEquipmentList);
                } catch (IOException ex) {
                    log.error("Error exporting csvsimple: ", ex);
                }   break;
            case "csvextended":
                try {
                    f = ExportActionBean.exportCSVExtended(roadsideEquipmentList);
                } catch (IOException ex) {
                    log.error("Error exporting SVExtended: ", ex);
                }   break;
            default:
                log.error("Exporttype unknown: " + exportType);
                break;
        }
        if (f != null) {
            destination = new File(downloadLocation, filename != null ? filename : f.getName());
            f.renameTo(destination);
        }
        transaction.rollback();
        Stripersist.requestComplete();
        
        if (destination != null) {
            String body = "";
            body += "Beste, <br/>";
            body += "<br/>";
            body += "Uw download is gereed. U kunt hem hier downloaden: <a href=\"" + appURL + "/action/download?filename=" + destination.getName() + "\">link</a>.<br/>";
            body += "<br/>Met vriendelijke groet <br/>";
            sendMail(body);
        } else {
            String body = "";
            body += "Beste, <br/>";
            body += "<br/>";
            body += "Uw export kon helaas niet gemaakt worden. Neem contact op met CROW-NDOV.";
            sendMail(body);
            //mail error
        }
    }
    
    private void sendMail(String body){
        try {
            Mailer.sendMail("KAR Geo Tool", fromAddress, "[KAR Geo Tool] Uw export staat klaar", body, gebruiker.getEmail());
        } catch (Exception ex) {
            Logger.getLogger(ExportCreatorThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void refreshEntities(){
        EntityManager em = Stripersist.getEntityManager();
        roadsideEquipmentList = new ArrayList<>();
        for (RoadsideEquipment roadsideEquipment : oldList) {
            roadsideEquipmentList.add(em.find(RoadsideEquipment.class, roadsideEquipment.getId()));
        }
        gebruiker = em.find(Gebruiker.class, gebruiker.getId());
    }
}
