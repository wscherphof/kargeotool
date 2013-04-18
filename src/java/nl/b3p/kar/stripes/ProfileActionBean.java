package nl.b3p.kar.stripes;

import java.io.StringReader;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.StrictBinding;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.Validate;
import nl.b3p.kar.hibernate.Gebruiker;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.stripesstuff.stripersist.Stripersist;

/**
 * Stripes klasse waarmee profielinstellingen kunnen worden opgeslagen.
 * 
 * @author Matthijs Laan
 */
@StrictBinding
@UrlBinding("/action/profile/{$event}")
public class ProfileActionBean implements ActionBean {
    private static final Log log = LogFactory.getLog(ProfileActionBean.class);
    
    private ActionBeanContext context;

    @Validate
    private String settings;

    public String getSettings() {
        return settings;
    }

    public void setSettings(String settings) {
        this.settings = settings;
    }
    
    public ActionBeanContext getContext() {
        return context;
    }

    public void setContext(ActionBeanContext context) {
        this.context = context;
    }

    @DefaultHandler
    public Resolution save() throws JSONException {
        JSONObject info = new JSONObject();
        info.put("success", Boolean.FALSE);
        try {
            Gebruiker principal = (Gebruiker) context.getRequest().getUserPrincipal();
            Gebruiker g = Stripersist.getEntityManager().find(Gebruiker.class, principal.getId());
            g.setProfile(settings);
            Stripersist.getEntityManager().flush();
            Stripersist.getEntityManager().getTransaction().commit();
            info.put("success", Boolean.TRUE);
        } catch (Exception e) {
            log.error("error saving profile", e);
            info.put("error", ExceptionUtils.getMessage(e));
        }
        return new StreamingResolution("application/json", new StringReader(info.toString(4)));
    }
    
}
