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

/**
 * Deze klasse verwerkt de enqueteformulieren voor de woningbouwplannen. Elk
 * bouwplan heeft een een aantal standaard gegevens welke in het Bouwplan object
 * worden opgeslagen. Daarnaast heeft een Bouwplan object opmerkingen en een
 * aantal Facts (aantallen van bepaalde soorten bouw).
 * <p>
 * Opmerkingen is nog niet uitgewerkt. Zie Moscow lijst. Opmerkingen moeten
 * prive per ingelogt persoon en algemeen publiekelijk toegankelijk kunnen worden
 * opgeslagem.
 * <p>
 * Facts bestaan in 2 type:
 * <li> Fact met periode: een feit dat aan een jaartal voor een bouwplan hangt
 * <li> Fact zonder periode: feit dat voor een bouwplan geldt
 * Beide worden apart opgeslagen om later afzonderlijk gerapport te kunnen worden.
 * Zie hiervoor ook de klasse BouwplanFactsTotaal, dat nu nog niet gebruikt
 * wordt.
 * <p>
 * Elk Fact wordt gekoppeld aan een FactSoort, welke weer hierarchisch gecategoriseerd
 * is via FactGroup en FactSubgroup. Deze laatse categorisering wordt later
 * gebruikt voor de rapportage.
 * <p>
 * De strategie voor het ophalen en opslaan van de Facts is als volgt. Alle Facts
 * worden volgens een precies te volgen strategie op volgorde uit de database
 * gehaald en in een array geplaatst. Als er geen Fact in de database staat wordt
 * een element met default waarde ingevoegd. Na ombouw naar een dto array voor gebruik
 * in een struts formulier wordt het webformulier gevuld. Na save komt de de array
 * weer in precies de zelfde volgorde binnen. Hiermee is dus bekend welke Fact
 * bij elk dto element in de array hoort. Tenslotte wordt elk element weer terug
 * geschreven naar de database. Indien een Fact al bestaat voor het bouwplan, 
 * wordt het Fact overschreven.
 * <p>
 * Nog niet uitgewerkt is de noodzaak verandering in een log op te nemen via de
 * de klasse TableFieldToTrack en GebruikersLog.
 * <p>
 * Nog niet uitgewerkt is de security met rollen en rechten. Er bestaan al wel
 * enige klassen AccessLevel, Gebruiker en GebruikerAccess.
 * <p>
 * @author Chris
 */
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
