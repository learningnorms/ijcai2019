package norms;

import java.util.HashMap;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by XXX on 8/14/18.
 */
public class VisibleWorld {

    private static final Logger log = LogManager.getLogger(VisibleWorld.class);

    public HashMap<Integer,HashMap<String,Boolean>> visibleActions; // Once every actor performs an action (1) or not (0), it is visible to others
    public HashMap<Integer,HashMap<String,Boolean>> visibleSanctions; //Provides, for every action whether it was sanctioned (1) or not (0)
    public List<Integer> actorIDs;
    private int currentIteration;


    public VisibleWorld(){
        this.visibleActions = new HashMap<>();
        this.visibleSanctions = new HashMap<>();
    }

    public void setVisibleActions(Integer agentID, HashMap<String,Boolean> currentActions){
        this.visibleActions.put(agentID,currentActions);
    }

    public void setVisibleSanctions(Integer agentID, HashMap<String,Boolean> currentSanctions){
        this.visibleSanctions.put(agentID,currentSanctions);
    }

    public void setActorIDs(List<Integer> agentIDs){
        this.actorIDs = agentIDs;
    }

    public List<Integer> getActorIDs(){
        return this.actorIDs;
    }

    public HashMap<Integer,HashMap<String,Boolean>> getVisibleActions(){
        return this.visibleActions;
    }

    public HashMap<Integer,HashMap<String,Boolean>> getVisibleSanctions(){ return this.visibleSanctions; }

    public int getCurrentIteration(){
        return this.currentIteration;
    }

    public void setCurrentIteration(int i){
        this.currentIteration = i;
    }

}
