package nl.b3p.kar.stripes;

import java.io.PrintWriter;
import javax.servlet.http.HttpServletResponse;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.StrictBinding;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.Validate;
import nl.b3p.kar.hibernate.Role;
import nl.b3p.kar.imp.LegacyImport;

/**
 *
 * @author Matthijs Laan
 */
@StrictBinding
@UrlBinding("/action/legacy_import")
public class ImportLegacyKv9ActionBean implements ActionBean {

    private ActionBeanContext context;
    @Validate
    private boolean checkBeforeImport = false;

    public ActionBeanContext getContext() {
        return context;
    }

    public void setContext(ActionBeanContext context) {
        this.context = context;
    }

    public boolean isCheckBeforeImport() {
        return checkBeforeImport;
    }

    public void setCheckBeforeImport(boolean checkBeforeImport) {
        this.checkBeforeImport = checkBeforeImport;
    }
   
    public Resolution doImport() {
        
        if(!getContext().getRequest().isUserInRole(Role.BEHEERDER)) {
            throw new IllegalAccessError();
        }
        
        return new StreamingResolution("text/plain") {
            @Override
            public void stream(HttpServletResponse response) throws Exception {
                PrintWriter out = new PrintWriter(response.getWriter());
                new LegacyImport(out).doImport(checkBeforeImport);
                out.flush();
            }            
        };
    }
}