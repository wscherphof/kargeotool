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

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.persistence.EntityManager;

import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.validation.SimpleError;
import net.sourceforge.stripes.validation.Validate;
import nl.b3p.geotools.data.dxf.DXFDataStore;
import nl.b3p.kar.hibernate.DataOwner;
import nl.b3p.kar.hibernate.Gebruiker;
import nl.b3p.kar.hibernate.RoadsideEquipment;
import nl.b3p.kar.hibernate.Upload;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.stripesstuff.stripersist.Stripersist;

/**
 *
 * @author Meine Toonen
 */
@StrictBinding
@UrlBinding("/action/upload")
public class UploadDXFActionBean implements ActionBean {

    private static final Log log = LogFactory.getLog(UploadDXFActionBean.class);
    private ActionBeanContext context;

    private SimpleFeatureBuilder featureBuilder;

    private final String JSP_VIEW = "/WEB-INF/jsp/dxf/view.jsp";
    private final String JSP_UPLOAD = "/WEB-INF/jsp/dxf/upload.jsp";
    
    @Validate(required = true, on = "upload")
    private FileBean bestand;
    
    @Validate(required = true, on = "upload")
    private RoadsideEquipment rseq;
    
    @Validate
    private String description;
    
    private Gebruiker gebruiker = null;
    
    private List<Upload> uploads;
    
    @Validate
    private Upload uploadFile;
    
    // <editor-fold desc="Getters and setters" defaultstate="collapsed">
    public FileBean getBestand() {
        return bestand;
    }

    public void setBestand(FileBean bestand) {
        this.bestand = bestand;
    }

    @Override
    public ActionBeanContext getContext() {
        return context;
    }

    @Override
    public void setContext(ActionBeanContext context) {
        this.context = context;
    }

    public SimpleFeatureBuilder getFeatureBuilder() {
        return featureBuilder;
    }

    public void setFeatureBuilder(SimpleFeatureBuilder featureBuilder) {
        this.featureBuilder = featureBuilder;
    }

    public RoadsideEquipment getRseq() {
        return rseq;
    }

    public void setRseq(RoadsideEquipment rseq) {
        this.rseq = rseq;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Upload> getUploads() {
        return uploads;
    }

    public void setUploads(List<Upload> uploads) {
        this.uploads = uploads;
    }

    public Upload getUploadFile() {
        return uploadFile;
    }

    public void setUploadFile(Upload uploadFile) {
        this.uploadFile = uploadFile;
    }
    

    // </editor-fold>

    @DefaultHandler
    public Resolution view(){
        
        return new ForwardResolution(JSP_VIEW);
    }
    
    public Resolution preupload() {
        return new ForwardResolution(JSP_UPLOAD);
    }
    
    public Resolution remove() {
        Gebruiker g = getGebruiker();
        if(!g.isBeheerder() && !g.getEditableDataOwners().contains(uploadFile.getDataOwner())){
            context.getValidationErrors().add("user",new SimpleError("Gebruiker niet gerechtigd om upload te verwijderen"));
            return view();
        }
        EntityManager em = Stripersist.getEntityManager();
        int numdeleted = em.createNativeQuery("DELETE FROM dxf_features WHERE upload = :upload").setParameter("upload", uploadFile.getId()).executeUpdate();
        em.remove(uploadFile);
        em.getTransaction().commit();
        context.getMessages().add(new SimpleMessage("Upload en de bijbehorende " + numdeleted + " geometrien verwijderd."));
        return view();
    }

    public Resolution upload() {
        try {
            gebruiker = getGebruiker();
            Upload upload = new Upload();
            upload.setDataOwner(rseq.getDataOwner());
            upload.setUser_(gebruiker);
            upload.setRseq(rseq);
            upload.setFilename(bestand.getFileName());
            upload.setUploaddate(new Date());
            upload.setDescription(description);
            EntityManager em = Stripersist.getEntityManager();
            em.persist(upload);
            em.getTransaction().commit();
            
            File f = File.createTempFile("dxfupload", "" + System.currentTimeMillis());
            FileUtils.copyInputStreamToFile(bestand.getInputStream(), f);
            processFile(f, createDbDatastore(), upload.getId());
            context.getMessages().add(new SimpleMessage("Uploaden gelukt"));
            String anchor = "x=" + rseq.getLocation().getX() + "&y=" + rseq.getLocation().getY() + "&zoom= "+13;
            return new RedirectResolution(EditorActionBean.class).setAnchor(anchor);
        } catch (IOException ex) {
            log.error("Error reading dxf", ex);
            return new ForwardResolution(JSP_UPLOAD);
        }

    }
    
    @After
    private void createLists(){
        EntityManager em = Stripersist.getEntityManager();
        List<Upload> uls = em.createQuery("from Upload order by filename,uploaddate").getResultList();
        Gebruiker g = getGebruiker();
        Set<DataOwner> daos = g.getEditableDataOwners();
        uploads = new ArrayList<>();
        for (Upload ul : uls) {
            if(daos.contains(ul.getDataOwner()) || g.isBeheerder()){
                uploads.add(ul);
            }
        }
    }

    private void processFile(File file, DataStore dataStore2Write, Integer uploadId) {
        try {
            URL dxf_url = file.toURL();
            DXFDataStore dataStore2Read = new DXFDataStore(dxf_url, "EPSG:28992");

            String[] typeNames = dataStore2Read.getTypeNames();
            for (String typeName : typeNames) {
                saveFeatures(dataStore2Read, dataStore2Write, "dxf_features", typeName, uploadId);
            }
        } catch (Exception ex) {
            log.error(ex.getLocalizedMessage());
            context.getValidationErrors().add("Error", new SimpleError("Fout tijdens importeren: " + ex.getLocalizedMessage()));
        }
    }

    private void saveFeatures(DataStore source, DataStore dest, String destFeatureType, String sourceFeatureType, Integer uploadId) throws IOException, Exception {
        SimpleFeatureSource sfs = source.getFeatureSource(sourceFeatureType);
        SimpleFeatureType sft = (SimpleFeatureType) sfs.getSchema();
        SimpleFeatureCollection sourceFc = sfs.getFeatures();

        SimpleFeatureStore destFs = (SimpleFeatureStore) dest.getFeatureSource(destFeatureType);
        SimpleFeatureType t = destFs.getSchema();

        featureBuilder = new SimpleFeatureBuilder(t);

        //maak een transactie
        Transaction transaction = new DefaultTransaction("create");
        destFs.setTransaction(transaction);
        Date d = new Date();
        //schrijf de data
        try {

            List<SimpleFeature> features = new ArrayList<>();
            SimpleFeatureIterator it = sourceFc.features();
            while (it.hasNext()) {
                SimpleFeature sourceFeature = it.next();
                SimpleFeature destFeature = processFeature(sourceFeature, sft, uploadId);
                features.add(destFeature);
            }
            SimpleFeatureCollection collection = DataUtilities.collection(features);
            destFs.addFeatures(collection);
            transaction.commit();
        } catch (IOException | NoSuchElementException exc) {
            log.error("Fout bij schrijven van features naar datastore... Doe rollback", exc);
            transaction.rollback();
            throw exc;
        } finally {
            transaction.close();
            dest.dispose();
        }
    }

    private SimpleFeature processFeature(SimpleFeature source, SimpleFeatureType newFt, Integer uploadId) {
        Object geom = source.getDefaultGeometry();
        if(geom == null){
            return null;
        }
        Geometry g = (Geometry)geom;
        Point p = g.getCentroid();
        if (p.getX() < 646 || p.getY() < 308975) {
            return null;
        }
        for (AttributeDescriptor attributeDescriptor : newFt.getAttributeDescriptors()) {
            Object b = source.getAttribute(attributeDescriptor.getLocalName());
            featureBuilder.set(attributeDescriptor.getName(), b);
        }
        
        featureBuilder.set("upload", uploadId);
        SimpleFeature feature = featureBuilder.buildFeature(null);
        return feature;
    }

    private static DataStore createDbDatastore() throws IOException {
        Map map = new HashMap();
        map.put("dbtype", "postgis");
        map.put("jndiReferenceName", "java:comp/env/jdbc/kargeotool");

        DataStore store = DataStoreFinder.getDataStore(map);
        return store;
    }
    
    public Gebruiker getGebruiker() {
        final String attribute = this.getClass().getName() + "_GEBRUIKER";
        Gebruiker g = (Gebruiker)getContext().getRequest().getAttribute(attribute);
        if(g != null) {
            return g;
        }
        Gebruiker principal = (Gebruiker) context.getRequest().getUserPrincipal();
        g = Stripersist.getEntityManager().find(Gebruiker.class, principal.getId());
        getContext().getRequest().setAttribute(attribute, g);
        return g;
    }
    
}
