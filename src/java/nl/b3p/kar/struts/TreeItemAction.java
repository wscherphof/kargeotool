package nl.b3p.kar.struts;

import nl.b3p.transmodel.Activation;
import nl.b3p.transmodel.ActivationGroup;
import nl.b3p.transmodel.RoadsideEquipment;
import org.json.JSONObject;

public abstract class TreeItemAction extends BaseDatabaseAction {
    protected static final String HIDE_FORM = "hideForm";
    protected static final String TREE_UPDATE = "treeUpdate";

    protected static String treeUpdateJson(String action, EditorTreeObject to) throws Exception {
        return treeUpdateJson(action, to, false);
    }

    protected static String treeUpdateJson(String action, EditorTreeObject to, boolean includeChildren) throws Exception {
        JSONObject update = new JSONObject();
        String id = null, parentId = null;
        if(to instanceof Activation) {
            id = "a:" + ((Activation)to).getId();
            parentId = "ag:" + ((Activation)to).getActivationGroup().getId();
        } else if(to instanceof ActivationGroup) {
            id = "ag:" + ((ActivationGroup)to).getId();
            parentId = "rseq:" + ((ActivationGroup)to).getRoadsideEquipment().getId();
        } else if(to instanceof RoadsideEquipment) {
            id = "rseq:" + ((RoadsideEquipment)to).getId();
        }
        update.put("action", action);
        update.put("id", id);
        update.put("parentId", parentId);
        update.put("object", to.serializeToJson(includeChildren));
        return update.toString();
    }
}