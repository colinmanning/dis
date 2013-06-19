package com.setantamedia.fulcrum.workflow;

import com.setantamedia.fulcrum.actions.ActionProcessor;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.log4j.Logger;

/**
 *
 * @author Colin Manning
 */
public class WorkflowManager {

    private static Logger logger = Logger.getLogger(WorkflowManager.class);
    private HashMap<String, Workflow> workflows = new HashMap<>();
    private HashMap<String, ActionProcessor> actionProcessors = new HashMap<>();
    public final static int DEFAULT_MAX_WORKER_THREADS = 4;
    private ExecutorService threadPool = null;

    public WorkflowManager() {
    }

    public void init() {
        threadPool = Executors.newFixedThreadPool(DEFAULT_MAX_WORKER_THREADS);
    }

    public void destroy() {
        if (threadPool != null) {
            threadPool.shutdownNow();
        }
    }

    public void registerWorkflow(String name, Workflow workflow) {
        workflows.put(name, workflow);
    }

    public int runWorkflow(String name, ArrayList<Path> files, HashMap<String, Object> params, Workflow successWorkflow, Workflow failedWorkflow) {
        int result = Workflow.RETURN_STATUS_UNKNOWN;
        Workflow workflow = workflows.get(name);
        if (workflow != null) {
            workflow.setInputFiles(files);
            workflow.setInputParams(params);
            threadPool.execute(workflow);
        }
        return result;
    }

    public HashMap<String, Workflow> getWorkflows() {
        return workflows;
    }

    public void setWorkflows(HashMap<String, Workflow> workflows) {
        this.workflows = workflows;
    }

    public HashMap<String, ActionProcessor> getActionProcessors() {
        return actionProcessors;
    }

    public void setActionProcessors(HashMap<String, ActionProcessor> actionProcessors) {
        this.actionProcessors = actionProcessors;
    }

    public void addActionProcessor(ActionProcessor actionProcessor) {
        actionProcessors.put(actionProcessor.getName(), actionProcessor);
    }

    public void removeActionProcessor(String actionName) {
        if (actionProcessors.get(actionName) != null) {
            actionProcessors.remove(actionName);
        }
    }

    public ActionProcessor getActionProcessor(String actionName) {
        return actionProcessors.get(actionName);
    }
    
    public void addWorkflow(Workflow workflow) {
        workflows.put(workflow.getName(), workflow);
    }

    public void removeWorkflow(String workflowName) {
        if (workflows.get(workflowName) != null) {
            workflows.remove(workflowName);
        }
    }

    public Workflow getWorkflow(String workflowName) {
        return workflows.get(workflowName);
    }
}
