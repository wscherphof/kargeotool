package nl.b3p.kar.struts;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import nl.b3p.commons.services.FormUtils;
import nl.b3p.commons.struts.ExtendedMethodProperties;
import nl.b3p.kar.hibernate.KarPunt;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.validator.DynaValidatorForm;
import org.hibernate.HibernateException;

public final class KarPuntAction extends BaseDatabaseAction {

    protected Log log = LogFactory.getLog(this.getClass());
    protected static final String VIEWER = "viewer";
    protected static final String SAVE = "save";
    
    protected Map getActionMethodPropertiesMap() {
        Map map = new HashMap();
        ExtendedMethodProperties hibProp = null;
        hibProp = new ExtendedMethodProperties(SAVE);
        hibProp.setDefaultMessageKey("warning.crud.savedone");
        hibProp.setDefaultForwardName(SUCCESS);
        hibProp.setAlternateForwardName(FAILURE);
        hibProp.setAlternateMessageKey("error.crud.savefailed");
        map.put(SAVE, hibProp);   
        return map;
    }

    public ActionForward unspecified(ActionMapping mapping, DynaValidatorForm dynaForm, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (isCancelled(request)) {
            return mapping.findForward(VIEWER);
        }
        KarPunt karpunt = getKarPunt(dynaForm, request, true);

        if (karpunt == null) {
            addAlternateMessage(mapping, request, "Niet gevonden");
            return mapping.findForward(SUCCESS);
        }        
        createLists(karpunt, dynaForm, request);

        populateForm(karpunt, dynaForm, request);
        
        /*Als er een nieuw object is getekend*/
        if (FormUtils.nullIfEmpty(request.getParameter("newWktgeom"))!=null){
            dynaForm.set("thegeom", request.getParameter("newWktgeom"));
        }
        return mapping.findForward(SUCCESS);
    }

    public ActionForward save(ActionMapping mapping, DynaValidatorForm dynaForm, HttpServletRequest request, HttpServletResponse response) throws Exception {
        EntityManager em = getEntityManager();
        KarPunt karpunt = getKarPunt(dynaForm, request, true);
        if (karpunt == null) {
            addAlternateMessage(mapping, request, "Niet gevonden");
            return mapping.findForward(SUCCESS);
        }
        populateObject(karpunt, dynaForm, request, mapping);
        if (karpunt.getGeom()!=null){
            if (karpunt.getId() == null) {
                em.persist(karpunt);
            } else {
                em.merge(karpunt);
            }
            em.flush();
        }
        createLists(karpunt, dynaForm, request);        
        addDefaultMessage(mapping, request);
        return getDefaultForward(mapping, request);
    }
    protected KarPunt getKarPunt(DynaValidatorForm dynaForm, HttpServletRequest request, boolean createNew) throws Exception {
        log.debug("Getting entity manager ......");
        EntityManager em = getEntityManager();
        KarPunt karpunt = null;
        Integer id = FormUtils.StringToInteger(dynaForm.getString("id"));
        if (null == id && createNew) {
            karpunt = new KarPunt();
        } else if (null != id) {
            karpunt = (KarPunt) em.find(KarPunt.class, id.intValue());
        }
        return karpunt;
    }

    protected void createLists(KarPunt karpunt, DynaValidatorForm form, HttpServletRequest request) throws HibernateException, Exception {
        
    }

    protected void populateForm(KarPunt karpunt, DynaValidatorForm dynaForm, HttpServletRequest request) throws Exception {
        dynaForm.set("id",karpunt.getId());
        dynaForm.set("description",karpunt.getDescription());
        dynaForm.set("thegeom",null);
    }   

    protected void populateObject(KarPunt karpunt, DynaValidatorForm dynaForm, HttpServletRequest request, ActionMapping mapping) throws Exception {
        if (FormUtils.nullIfEmpty(dynaForm.getString("thegeom"))!=null){
            String wktGeom=dynaForm.getString("thegeom");
            WKTReader wktreader = new WKTReader(new GeometryFactory(new PrecisionModel(), 28992));
            Point geom = null;
            try {
                geom = (Point) wktreader.read(wktGeom);

            } catch (ParseException p) {
               // addAlternateMessage(mapping, request, WKTGEOM_NOTVALID_ERROR_KEY);
            }
            karpunt.setGeom(geom);
        }
        karpunt.setDescription(FormUtils.nullIfEmpty(dynaForm.getString("description")));

    }

}
