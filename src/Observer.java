package norms;

import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by XXX on 8/14/18.
 */
public class Observer extends NormativeAgent{
    private List<Integer> currentActorPair = new ArrayList<>();
    private Sensor sensor;
    private HashMap<Integer,IndexedDSFrame> incomingDataBoE;
    private double alpha;
    private Set<String> isNorm; // when a convention attribute is sanctioned, it is added to this list of norms

    private static final Logger log = LogManager.getLogger(Observer.class);

    private Stack<Integer> actorStack = new Stack<>();


    public Observer(HashMap<String,IndexedDSFrame> normBoEs, String startingContext, double probMissing, double propIgnorance, double sensorReliability,
                    IndexedDSFrame templateFrame){
        super();
        // Set Agent Type
        this.agentType = AgentType.OBSERVER;
        this.mentalBoEs = normBoEs;
        this.startingContext = startingContext;
        this.sensor = new Sensor(probMissing, propIgnorance, sensorReliability, templateFrame);
        this.isNorm = new HashSet<>();

    }

    @Override
    public void sense() {
        log.trace("Observer sensing");
        //only sense if there is something to sense

        if (visibleWorld.getVisibleActions().isEmpty()){
            log.trace("Nothing to sense");
        } else {
            // Set Alpha (based on current iteration from the visible world.
            setAlpha();

            // Determine and set current context
            String c = determineCurrentContext();
            setCurrentContext(c);

            // get the relevant mentalBoE
            IndexedDSFrame relevantMentalBoE = mentalBoEs.get(c);

            // Clear template frame
            sensor.clearTemplateFrame();

            // Randomly select two interacting actors from the visible world
            // (a) if stack is empty then create a fresh stack. Also, if stack has one actor, then set both actors to the same.
            setCurrentActorPair();

            //Initialize incomingDataBoe
            incomingDataBoE = new HashMap<>();

            // Sense each of their actions (under conditions of uncertainty)
            //  Types of uncertainty: mass (sensor reliability),
            //incomingDataBoE.clear();
            for (Integer actor : currentActorPair){
                IndexedDSFrame dataBoE =  sensor.getBoE(actor, visibleWorld);
                incomingDataBoE.put(actor,dataBoE);
                for (Map.Entry<String,Boolean> normEntry : this.visibleWorld.getVisibleSanctions().get(actor).entrySet() ){
                    if (normEntry.getValue()){
                        this.isNorm.add(normEntry.getKey());
                    }
                }
            }
        }



    }

    @Override
    public void act(){
        // learn/update own Indexed frame based on current actions of the selected actors.
        log.trace("Observer learning");

        if (visibleWorld.getVisibleActions().isEmpty()){
            log.trace("Nothing to learn yet");
        } else {

            // get the relevant mentalBoE
            IndexedDSFrame relevantMentalBoE = mentalBoEs.get(currentContext);
            //System.out.println("Frame (before update): "+relevantMentalBoE.getDsframe().getAllNormalizedMasses());
            for (Map.Entry<Integer, IndexedDSFrame> actorSpecificBoE : incomingDataBoE.entrySet()) {
                //System.out.println(">>Data: "+actorSpecificBoE.getValue().getDsframe().getAllNormalizedMasses());
                relevantMentalBoE.update(actorSpecificBoE.getValue(), this.alpha);
            }
            //System.out.println("Frame (after update): "+relevantMentalBoE.getDsframe().getAllNormalizedMasses());
        }

    }


    private void setCurrentActorPair(){
        int actor1;
        int actor2;
        if (actorStack.empty()){
            //create new stack
            actorStack.addAll(visibleWorld.getActorIDs());
            Collections.shuffle(actorStack);
            actor1 = actorStack.pop();
            actor2 = actorStack.pop();
        } else {
            if (actorStack.size()!=1){
                // pop two actors out
                actor1 = actorStack.pop();
                actor2 = actorStack.pop();
            } else {
                actor1 = actorStack.pop();
                actor2 = actor1;
            }
        }

        if (!currentActorPair.isEmpty()){
            currentActorPair.clear();
        }
        currentActorPair.add(actor1);
        currentActorPair.add(actor2);

    }

    /**
     * Simple case here of setting the current context to the starting context
     * @return
     */
    private String determineCurrentContext(){
        String c = startingContext;
        return c;
    }

    private void setAlpha(){
        int l = visibleWorld.getCurrentIteration();
        double m = 0;
        double alpha = (l + m)/(l+m+1);
        this.alpha = alpha;
    }

    public Set<String> getIsNorm(){
        return this.isNorm;
    }


}
