package nl.b3p.kar.struts;

import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import nl.b3p.commons.services.FormUtils;
import nl.b3p.commons.struts.ExtendedMethodProperties;
import nl.b3p.kar.hibernate.Gebruiker;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.validator.DynaValidatorForm;
import org.hibernate.HibernateException;

/**
 *
 * @author Jytte
 */
public class EditUserAction extends BaseDatabaseAction {

    protected Log log = LogFactory.getLog(this.getClass());
    protected static final String SAVE = "save";

    protected Map getActionMethodPropertiesMap() {
        Map map = new HashMap();

        ExtendedMethodProperties hibProp = null;

        hibProp = new ExtendedMethodProperties(SAVE);
        hibProp.setDefaultMessageKey("warning.user.saved");
        hibProp.setDefaultForwardName(SUCCESS);
        hibProp.setAlternateForwardName(FAILURE);
        hibProp.setAlternateMessageKey("warning.user.saveerror");
        map.put(SAVE, hibProp);

        return map;
    }

    public ActionForward unspecified(ActionMapping mapping, DynaValidatorForm dynaForm, HttpServletRequest request, HttpServletResponse response) throws Exception {
        Gebruiker user = (Gebruiker) request.getUserPrincipal();
        populateForm(user, dynaForm, request);

        return mapping.findForward(SUCCESS);
    }

    public ActionForward save(ActionMapping mapping, DynaValidatorForm dynaForm, HttpServletRequest request, HttpServletResponse response) throws Exception {
        EntityManager em = getEntityManager();
        Gebruiker user = (Gebruiker) request.getUserPrincipal();
        
        user.setFullname(dynaForm.getString("volledigenaam"));
        user.setEmail(dynaForm.getString("email"));
        user.setPhone(dynaForm.getString("telefoon"));
        user.setPosition(dynaForm.getString("positie"));

        String pw = FormUtils.nullIfEmpty(dynaForm.getString("wachtwoord"));
        if(pw != null) {
            user.changePassword(request, pw);
            dynaForm.set("wachtwoord", null);
        }

        em.merge(user);

        this.addDefaultMessage(mapping, request);
        return getDefaultForward(mapping, request);
    }

    protected void createLists(DynaValidatorForm form, HttpServletRequest request) throws HibernateException, Exception {
    }

    protected void populateForm(Gebruiker gebruiker, DynaValidatorForm dynaForm, HttpServletRequest request) throws Exception {
        dynaForm.set("username", gebruiker.getUsername());
        dynaForm.set("volledigenaam", gebruiker.getFullname());
        dynaForm.set("email", gebruiker.getEmail());
        dynaForm.set("telefoon", gebruiker.getPhone());
        dynaForm.set("positie", gebruiker.getPosition());        
    }
}
