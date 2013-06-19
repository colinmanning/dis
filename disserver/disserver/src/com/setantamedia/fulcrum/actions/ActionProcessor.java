package com.setantamedia.fulcrum.actions;

import com.setantamedia.fulcrum.AdvancedServer;
import com.setantamedia.fulcrum.common.Dam;
import com.setantamedia.fulcrum.common.Query;
import com.setantamedia.fulcrum.config.FulcrumConfig;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.json.JSONObject;

/**
 * Run actions in the DIS environment. Actions defined in the DIS config file
 * will need to reference sub classes of this abstract class.
 *
 * @author Colin Maning
 */
public abstract class ActionProcessor {

    protected String name = null;
    protected HashMap<String, String> params = new HashMap<>();
    protected AdvancedServer mainServer;
    protected FulcrumConfig fulcrumConfig = null;
    protected Dam dam = null;

    public ActionProcessor() {
    }

    public void init() {
    }

    public abstract JSONObject execute(HashMap<String, String> params);

    public HashMap<String, String> getParams() {
        return params;
    }

    public void setParams(HashMap<String, String> params) {
        this.params = params;
    }

    public AdvancedServer getMainServer() {
        return mainServer;
    }

    public void setMainServer(AdvancedServer mainServer) {
        this.mainServer = mainServer;
    }

    public FulcrumConfig getFulcrumConfig() {
        return fulcrumConfig;
    }

    public void setFulcrumConfig(FulcrumConfig fulcrumConfig) {
        this.fulcrumConfig = fulcrumConfig;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    protected String buildNamedQuery(Query namedQuery, HashSet<String> ignoreParams, HashMap<String, String> params) {
        String result = "";
        HashMap<String, String> queryParams = new HashMap<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (ignoreParams.contains(entry.getKey())) {
                continue;
            }
            queryParams.put(entry.getKey(), entry.getValue());
        }
        result = namedQuery.buildInstance(queryParams);
        return result;
    }

    public Dam getDam() {
        return dam;
    }

    public void setDam(Dam dam) {
        this.dam = dam;
    }
}
