package norms;

import java.math.BigInteger;
import java.util.*;
import java.util.function.BooleanSupplier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static norms.NormativeAgent.count;

/**
 * Created by XXX on 8/14/18.
 */
public class NormativeSociety {

    private static final Logger log = LogManager.getLogger(NormativeSociety.class);


    /*************************************
     ******* SIMULATION CONSTANTS ********
     *************************************/
    public static final boolean debug = false;

    /*************************************
     ******* INDEPENDENT VARIABLES *******
     *************************************/

    //Population data
    public int numActors;
    public int numObservers;

    // Master Seed
    public long masterSeed;

    //Number of norms in the world
    public int numNorms;

    // Norm Compliance Rates in the Society
    // Std sort of controls the degree to which the agents are compliant here.
    // quite compliant = 0.5/2
    // Less compliant = 0.5/2 (wider, flatter distribution, so more chances for non-compliance
    public double flatness; //establishes how flat a distribution can be.
    public double optionalMean = 0.5;
    public double optionalStd;
    public double obligatoryMean = 1.0;
    public double obligatoryStd;
    public double forbiddenMean = 0.0;
    public double forbiddenStd;

    //Lifetime of world
    public int lifetime;

    // Sanctioning
    // Proportion of lifetimes that involved sanction signals, if needed
    public double sanctionRate;

    // Data ignorance rate (Goes into Observer)
    // Proportion of e's in the data
    public double probIgnorance; // Probability that a given reading contains missing values
    public double propMissing; // Proportion of values that are ambiguous in a given reading

    // Sensor Reliability
    public double sensorReliability;

    // Starting Context
    public String startingContext = "c1";

    /*************************************
     ******** DEPENDENT VARIABLES ********
     *************************************/
    // Overall precision and recall
    public List<Double> precision;
    public List<Double>recall;

    public String outputFile;

    /*************************************
     ******** AUXILIARY VARIABLES ********
     *************************************/
    // bookkeeping lists
    private List<NormativeAgent> agents = new ArrayList<>();
    private List<Actor> actors = new ArrayList<>();
    private List<Observer> observers = new ArrayList<>();
    private VisibleWorld visibleWorld;
    //private List<Long> seeds;
    private HashMap<String,Integer> deonticTruth;
    private List<Integer> actorIDs = new ArrayList<>();
    public int numSeeds; //Each seed produces an adherence and prevalence value, which is used to compute compliance rates
    public int population;



    /**
     * CONSTRUCTOR
     */
    public NormativeSociety(String outputFile, IndependentVariables iv){
        this.outputFile = outputFile;

        setIndependentVariables(iv);

        log.trace("Constructing Society...");
        visibleWorld = new VisibleWorld();
        setDeonticTruth();
        log.debug("Deontic Truths = "+getDeonticTruth());
        createAgents();
        log.debug("Visible World (Actors): "+visibleWorld.getActorIDs());

        log.debug("Norm Compliance Rate: "+actors.get(1).normComplianceRate);

        run();
        setPostSimulationStateVariables();
    }



    private void setIndependentVariables(IndependentVariables iv){
        this.numActors = iv.getNumActors();
        this.numObservers = iv.getNumObservers();
        this.masterSeed = iv.getMasterSeed();
        this.numNorms = iv.getNumNorms();
        this.flatness = iv.getFlatness(); //establishes how flat a distribution can be.
        this.lifetime = iv.getLifetime();
        this.sanctionRate = iv.getSanctionRate();
        this.probIgnorance = iv.getProbIgnorance(); // Probability that a given reading contains missing values
        this.propMissing = iv.getPropMissing(); // Proportion of values that are ambiguous in a given reading
        this.sensorReliability = iv.getSensorReliability();

        this.optionalStd = 0.5/flatness;
        this.obligatoryStd = 0.33/flatness;
        this.forbiddenStd = 0.33/flatness;

        this.numSeeds = numActors;
        this.population =  numActors + numObservers;
    }

    //************************************************************//

    /******************************************************************
     *********** CRITICAL CLASS METHODS REQUIRED IN SIMWORLD ***********
     ******************************************************************/
// Commented out for now    /**
//     * Creates a new Swamp environment
//     * Make sure to call the super class to initalize and start the JAVA client
//     */
//    public Society() {
//        super();
//        this.passClassType(this);
//    }
//
//    /*
//     * Implementing abstract classes from Environment
//     */
//    /**
//     *
//     * @param classManager
//     */
//    //@Override
//    protected void defineListOfAgentsThatRequireSubclass(AgentClassManager classManager) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }

    /**
     * Creates the agents used on the simulation
     */
    //@Override
    protected void createAgents() {
        // Initialize some computationally intensive calculations
        List<String> attributes = Utility.generateAttributes(numNorms);
        List<String> singletons = extractHypotheses(attributes);
        List<BigInteger> power = initializePower(singletons.size());


        initializeActors(attributes,singletons,power);
        visibleWorld.setActorIDs(actorIDs);
        initializeObservers(attributes,singletons,power);


    }

    /**
     * Set the results into the dependent variables after the end of the simulation
     */
    //@Override
    public void setPostSimulationStateVariables(){

        // For each observer

        //System.out.println("\nDeontic Truths: "+this.deonticTruth);
        //System.out.println("Normalized Core: "+ this.observers.get(0).getMentalBoEs().get("c1").getDsframe().getAllNormalizedMasses());

        //System.out.println(this.observers.get(0).getMentalBoEs().get("c1").getBelPlausRules());
        //System.out.println(this.observers.get(0).getIsNorm());

        int[][] confusion = getConfusionMatrix();

//        for (int i = 0; i < confusion.length; i++) {
//            for (int j = 0; j < confusion[i].length; j++) {
//                System.out.print(confusion[i][j] + " ");
//            }
//            System.out.println();
//        }

        precision = new ArrayList<>();
        precision.add(getPrecision(confusion,-1));
        precision.add(getPrecision(confusion,0));
        precision.add(getPrecision(confusion,1));

        recall = new ArrayList<>();
        recall.add(getRecall(confusion,-1));
        recall.add(getRecall(confusion,0));
        recall.add(getRecall(confusion,1));


        List<String> row = packageResults();

        Utility.writeToCSV(row, outputFile);

    }


    private List<String> packageResults(){
        List<String> values = new ArrayList<>();
        values.add(Integer.toString(numActors));
        values.add(Integer.toString(numObservers));
        values.add(Integer.toString(numNorms));
        values.add(Double.toString(flatness));
        values.add(Integer.toString(lifetime));
        values.add(Double.toString(sanctionRate));
        values.add(Double.toString(probIgnorance));
        values.add(Double.toString(propMissing));
        values.add(Double.toString(sensorReliability));
        values.add(Long.toString(masterSeed));
        values.add(Double.toString(precision.get(0)));
        values.add(Double.toString(precision.get(1)));
        values.add(Double.toString(precision.get(2)));
        values.add(Double.toString(recall.get(0)));
        values.add(Double.toString(recall.get(1)));
        values.add(Double.toString(recall.get(2)));
        return values;
    }

    private double getPrecision(int[][] confusion, int deontic){

        int i=-1;
        if (deontic == 1){
            i=2;
        } else if (deontic == -1){
            i=0;
        } else if (deontic == 0){
            i=1;
        } else {
            log.error("Error in input");
        }

        int denom = 0;
        for (int j = 0; j<3; j++){
            denom = denom + confusion[j][i];
        }
        int num = confusion[i][i];
        double precision = ((double) num) / denom;
        return precision;
    }

    private double getRecall(int[][] confusion, int deontic){

        int i=-1;
        if (deontic == 1){
            i=2;
        } else if (deontic == -1){
            i=0;
        } else if (deontic == 0){
            i=1;
        } else {
            log.error("Error in input");
        }

        int denom = 0;
        for (int j = 0; j<3; j++){
            denom = denom + confusion[i][j];
        }
        int num = confusion[i][i];
        double recall = ((double) num) / denom;
        return recall;
    }


    private int[][] getConfusionMatrix(){
        int[][] confusionMatrix = new int [ 3 ] [ 3 ] ; // 0,1,-1 ordering
        int i,j = 0;
        for (int k = 0; k<this.observers.size(); k++){
            HashMap<String,Integer> estimated = getEstimatedDeonticValue(this.observers.get(k),"c1");
            for (Map.Entry<String,Integer> gndTruth : this.deonticTruth.entrySet()){ //i

                if (gndTruth.getValue() == -1){

                    if (estimated.get(gndTruth.getKey()) == -1){ // correct value
                        confusionMatrix[0][0] = confusionMatrix[0][0] + 1;
                    } else if (estimated.get(gndTruth.getKey()) == 0) {
                        confusionMatrix[0][1] = confusionMatrix[0][1] + 1;
                    } else if (estimated.get(gndTruth.getKey()) == 1) {
                        confusionMatrix[0][2] = confusionMatrix[0][2] + 1;
                    } else {
                        log.error("Error in calculating forbidden numbers");
                    }

                } else if (gndTruth.getValue() == 0){

                    if (estimated.get(gndTruth.getKey()) == -1){
                        confusionMatrix[1][0] = confusionMatrix[1][0] + 1;
                    } else if (estimated.get(gndTruth.getKey()) == 0) {
                        confusionMatrix[1][1] = confusionMatrix[1][1] + 1;
                    } else if (estimated.get(gndTruth.getKey()) == 1) {
                        confusionMatrix[1][2] = confusionMatrix[1][2] + 1;
                    } else {
                        log.error("Error in calculating optional numbers");
                    }


                } else if (gndTruth.getValue() == 1){

                    if (estimated.get(gndTruth.getKey()) == -1){
                        confusionMatrix[2][0] = confusionMatrix[2][0] + 1;
                    } else if (estimated.get(gndTruth.getKey()) == 0) {
                        confusionMatrix[2][1] = confusionMatrix[2][1] + 1;
                    } else if (estimated.get(gndTruth.getKey()) == 1) {
                        confusionMatrix[2][2] = confusionMatrix[2][2] + 1;
                    } else {
                        log.error("Error in calculating obligatory numbers");
                    }

                } else{
                    log.error("Error in precision calculations");
                }

            }

        }
        return confusionMatrix;
    }

    private HashMap<String,Integer> getEstimatedDeonticValue(Observer observer, String context){
        // Context input tells you which mental boe to look at
        IndexedDSFrame relevantBoE = observer.getMentalBoEs().get("c1");
        HashMap<String,Integer> estimated = new HashMap<>();
        for (Map.Entry<String,List<Double>> belsplsmap : relevantBoE.getBelPlausRules().entrySet()){
            if (belsplsmap.getValue().get(0) > 0.66){
                // Obligatory (Not sure if sanctioning will fit in here).
                estimated.put(belsplsmap.getKey(),1);
            } else if (belsplsmap.getValue().get(1) < 0.33) {
                // Forbidden
                estimated.put(belsplsmap.getKey(),-1);
            } else {
                // optional
                estimated.put(belsplsmap.getKey(),0);
            }
        }
        return estimated;


    }



    /**
     * Reset the simulation before executing it again
     */
    //@Override
    protected void resetSimulation(){
        //Reset count of actor IDs.
        count = 0;
    }

    /************************************************************
     * ************* IMPORTANT METHODS (not needed for Simworld)
     * But needed here for running trial simulations **********
     *  Simworld already has a mechanism for it *******
     ***********************************************************/



//
    public void run(){

        for (int l=0; l<lifetime; l++){
            visibleWorld.setCurrentIteration(l);

            // Agents sense and act in the world
            for (NormativeAgent a : agents){
                a.sense();
            }
            for (NormativeAgent a : agents){
                a.act();
            }
            // SET the visible world with all the actions performed by all the agents
            // Also SET the visible world with the sanctioning activity
            // (1) first get the entirety of the visible world (with all the actions from all the actors)
            for (NormativeAgent a : agents){
                if (a instanceof Actor){
                    // Add to hastable (key: agent Id, value: list of actions performed)
                    visibleWorld.setVisibleActions(a.getAgentID(),a.getCurrentActionsPerformed());

                    // Check if current actions performed are normative, if not sanction them
                    HashMap<String, Boolean> currentSanctions = new HashMap<>();
                    for (Map.Entry<String,Boolean> entry : a.getCurrentActionsPerformed().entrySet()){
                        // check for sanctioning
                        Boolean sanction = Utility.flipBiasedCoin(sanctionRate);
                        if((deonticTruth.get(entry.getKey()) == 1) && !entry.getValue()){
                            // Obligatory action was not performed
                            currentSanctions.put(entry.getKey(),sanction);

                        } else if ((deonticTruth.get(entry.getKey()) == -1) && entry.getValue()){
                            // Forbidden action was performed
                            currentSanctions.put(entry.getKey(),sanction);
                        } else {
                            // No sanctioning
                            currentSanctions.put(entry.getKey(),!sanction);
                        }
                    }

                    visibleWorld.setVisibleSanctions(a.getAgentID(),currentSanctions);
                }
            }
            // (2) Tell the agents about this updated visible world.
            for (NormativeAgent a : agents){
                a.setVisibleWorld(visibleWorld);
            } // Now the agent's have the latest visible world information

            // Progress bar
            Utility.progressPercentage(l, lifetime-1);
            try {
                Thread.sleep(0);
            } catch (Exception e) {
            }


        }
    }

    /*************************************
     * ******* SUPPORT METHODS**********
     ************************************/

    private List<String> extractHypotheses(List<String> attributes){
        int  n = attributes.size();
        List<String> hypos = new ArrayList<>();
        for (int i = 0; i < Math.pow(2, n); i++) {
            String bin = Integer.toBinaryString(i);
            while (bin.length() < n) {
                bin = "0" + bin; // adding leading zeros if necessary
            }
            hypos.add(bin);
        }
        return hypos;
    }

    private void setDeonticTruth(){
        // Idea here is that for every norm, we set if it is obligatory (1), Forbidden (-1), or optional (0)
        // If obligatory or forbidden norms are violated, then they will be sanctioned at the sanctionRate.
        List<String> norms = Utility.generateAttributes(numNorms);
        //Long seed = getSeeds(1).get(0);
        this.deonticTruth = new HashMap<>();
        int i =0;
        for (String n : norms){
            int deonticChoice = Utility.randomDeonticChoice(masterSeed+i);
            deonticTruth.put(n,deonticChoice);
            i++;
        }
    }

    private HashMap<String,Integer> getDeonticTruth(){
        return this.deonticTruth;
    }

    private List<BigInteger> initializePower(int numberSingletons){
        List<BigInteger> power = new ArrayList<>();
        BigInteger powerVal = new BigInteger("1");
        for (int i = 0; i < numberSingletons; i++){
            power.add(powerVal);
            powerVal = powerVal.multiply(new BigInteger("2"));
        }
        return power;
    }

    private void initializeActors(List<String> attributes, List<String> singletons, List<BigInteger> power){
        log.trace("Initializing Actors");
        List<Long> seeds = getSeeds(numSeeds,masterSeed); // randomly generate n seeds
        //setSeeds(seeds);

        for (int a=0; a<numActors; a++){
            log.trace("Actor #"+a);
            // (1) Setup the vacuous indexedDSFrame(s) (this provides the norms)

            IndexedDSFrame normFrame1_c1 = new IndexedDSFrame(startingContext,attributes,singletons,power);
            HashMap<String,IndexedDSFrame> normMap = new HashMap<>();
            normMap.put(startingContext,normFrame1_c1);

            log.trace("Frame created");

            // (1) Determine the norm adherence rate for each norm in each context
            //// (a)
            Long tempSeed = seeds.get(a); // every actor gets  one seed, which produces adherence and prevalence
            HashMap<List<String>,Double> adherences = getCompliance(deonticTruth,tempSeed);
            HashMap<List<String>,Double> prevalences = getCompliance(deonticTruth,tempSeed+1L);

            // Note: For a given actor, because its the same seed, adh will remain a constant for a
            //   a particular deontic status. So, if there are two norms with the same deontic truth, for
            //   a given actor, the adherences will be the same.

            // (2) Correct adherences to incorporate prevalence
            // // Toss a biased coin and determine if

            HashMap<List<String>,Double> normComplianceRate = new HashMap<>();

            for (HashMap.Entry<List<String>,Double> adherenceEntry : adherences.entrySet()){
                // get the corresponding prevalence
                double prev = prevalences.get(adherenceEntry.getKey());
                if (Utility.flipBiasedCoin(prev)){
                    normComplianceRate.put(adherenceEntry.getKey(),adherenceEntry.getValue());
                } else { // non complying
                    normComplianceRate.put(adherenceEntry.getKey(),0.0);
                }
            }

            // (2) Create actor with calculated normComplianceRates
            Actor actor = new Actor(normMap,normComplianceRate,startingContext);
            actors.add(actor);
            agents.add(actor);
            actorIDs.add(actor.getAgentID());
        }
    }

    private void initializeObservers(List<String> attributes, List<String> singletons, List<BigInteger> power){
        log.trace("Initializing Observers");

        // For each observer
        for (int o=0; o<numObservers; o++){
            // (1) Setup the vacuous indexedDSFrame(s) (this provides the norms)
            IndexedDSFrame normFrame1_c1 = new IndexedDSFrame(startingContext,attributes,singletons,power);
            HashMap<String,IndexedDSFrame> normMap = new HashMap<>();
            normMap.put(startingContext,normFrame1_c1);

            // (2) create template frame (couldbe replaced by cloning
            IndexedDSFrame templateFrame = new IndexedDSFrame(startingContext,attributes,singletons,power);


            // (2) Create observer with this vacuous BoE
            Observer observer = new Observer(normMap, startingContext,propMissing,probIgnorance,sensorReliability,templateFrame);
            observer.setVisibleWorld(visibleWorld);
            observers.add(observer);
            agents.add(observer);

        }
    }

    private static List<Long> getSeeds(int numberOfSeeds, long masterSeed){
        List<Long> seeds = new ArrayList<>();
        Random r = new Random();
        r.setSeed(masterSeed);
        for (int n = 0; n < numberOfSeeds; n++){
            long seed =  r.nextLong();
            seeds.add(seed);
        }

        return seeds;
    }

    /**
     * Returns compliances for a given Seed value
     * @param
     * @param
     * @return
     */
    private HashMap<List<String>,Double> getCompliance(HashMap<String,Integer> deonticTruth, Long tempSeed) {
        HashMap<List<String>,Double> compliances = new HashMap<>();
        double com= 0.0;
        int count = 0;
        for (HashMap.Entry<String, Integer> e : deonticTruth.entrySet()) {
            String key = e.getKey();
            if (e.getValue() == 1) {
                // Obligatory norm
                com = Utility.sampleGaussian(tempSeed+(2+count),obligatoryMean,obligatoryStd);

            } else if (e.getValue() == -1) {
                com = Utility.sampleGaussian(tempSeed-(2+count),forbiddenMean,forbiddenStd);

            } else if (e.getValue() == 0) {
                com = Utility.sampleGaussian(tempSeed+count,optionalMean,optionalStd);

            } else {
                log.error("Error: Can't calculate compliance. DeonticTruth is a weird value.");
            }

            List<String> contextNorm = new ArrayList<>();
            contextNorm.add(startingContext);
            contextNorm.add(e.getKey());

            compliances.put(contextNorm,com);
            count = count + 2;
        }
        return compliances;
    }

    /**
     * Returns a list of length (number of norms) containing prevalences for a given Seed value + 1
     * @param maxNumberOfNorms
     * @param seed
     * @return
     */
    public static List<Double> getPrevalences(int maxNumberOfNorms, long seed){
        List<Double> prevalences = new ArrayList<>();

        Random r = new Random();
        r.setSeed(seed+1);
        for (int n = 0; n < maxNumberOfNorms; n++){
            double p = r.nextDouble();
            prevalences.add(p);
        }

        return prevalences;
    }

//    private void setSeeds(List<Long> seeds){
//        this.seeds = seeds;
//    }

//    public List<Long> getSeeds(){
//        return this.seeds;
//    }



}
