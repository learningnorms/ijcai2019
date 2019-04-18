package norms;

import org.jfree.util.HashNMap;

import java.util.HashMap;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by XXX on 8/14/18.
 */
public abstract class NormativeAgent {

    private static final Logger log = LogManager.getLogger(NormativeAgent.class);

    private int agentID;
    public AgentType agentType;
    public String currentContext;
    public HashMap<String,IndexedDSFrame> mentalBoEs;  //key: context, value: IndexedFoD (This is what the agent has learned)
    public HashMap<String,Boolean> currentActionsPerformed; // This could be visible to other agents
    public VisibleWorld visibleWorld;
    public String startingContext;
    public static int count=0;

    ///////////////////////////////////////////////////////////////////
    /////////// CONSTRUCTOR ////////////////////////////////////
    /////////////////////////////////////////////////////////////////


    public NormativeAgent(){
        this.agentID = ++count;
        currentActionsPerformed = new HashMap<>();
    }


    ///////////////////////////////////////////////////////////////////
    /////////// ABSTRACT METHODS ////////////////////////////////////
    /////////////////////////////////////////////////////////////////

    abstract void sense();
    // Sense the environment and current goals
    // Update knowledge and beliefs
    // Set current context (based on env and goals)

    abstract void act();
    // Get indexed FoD for the current context
    // Perform one or more actions for the norms that apply in the context (maybe based on beliefs or on adherence, prevalence etc.)


    /////////////////////////////////////////////////////////////////
    ///////// SETTERS AND GETTERS /////////////////////////////////
    //////////////////////////////////////////////////////////////

    public int getAgentID(){
        return this.agentID;
    }

    public void setVisibleWorld(VisibleWorld visibleWorld){
        this.visibleWorld = visibleWorld;
    }

    public void setCurrentContext(String c){
        currentContext = c;
    }

    public HashMap<String,Boolean> getCurrentActionsPerformed(){
        return this.currentActionsPerformed;
    }

    public HashMap<String,IndexedDSFrame> getMentalBoEs(){
        return this.mentalBoEs;
    }
}

//Todo - allow for richer context and norm representation - maybe use Tweety to capture symbolic FOL representation
