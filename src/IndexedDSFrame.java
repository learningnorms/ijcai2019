package norms;

import java.math.BigInteger;
import java.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by XXX on 8/14/18.
 */
public class IndexedDSFrame {

    private static final Logger log = LogManager.getLogger(IndexedDSFrame.class);

    // antecedent --> attribute_1, antecedent --> attribute_2, ...

    private DSFrame dsframe; // BoE
    private String frameName; // Just the antecedent
    private List<String> attributes;
    private long frameId;
    private Map<String,List<String>> rulesMap; // maps an attribute name to the specific proposition in the frame
    private Map<String,List<Double>> belPlausRules; //key: attribute name, value: [bel, pl] of the corresponding rule
    private static long count=0L;


    //****************** CONSTRUCTORS ************************************//
    /**
     * Constructor. Given an antecedent and a set of possible consequents
     * @param antecedent
     * @param attributes
     */
    public IndexedDSFrame(String antecedent, List<String> attributes){
        this.frameName = antecedent;
        this.attributes = attributes;
        initialize(this.attributes);
        this.frameId = ++count;
    }

    public IndexedDSFrame(String antecedent, List<String> attributes, List<String> singletons, List<BigInteger> power){
        this.frameName = antecedent;
        this.attributes = attributes;
        initializeWithSingletons(singletons,power);
        this.frameId = ++count;
    }
    //**********************************************************************//



    //****************** BASIC GETTERS ************************************//
    public DSFrame getDsframe(){
        return this.dsframe;
    }

    public String getFrameName(){
        return this.frameName;
    }

    public long getFrameId(){
        return this.frameId;
    }

    public List<String> getAttributes(){
        return this.attributes;
    }

    public Map<String,List<String>> getRulesMap(){
        return this.rulesMap;
    }

    public Map<String,List<Double>> getBelPlausRules(){
        return this.belPlausRules;
    }
    //**********************************************************************//


    //****************** DS-FRAME OPERATIONS ************************************//
    public double getRuleBel(String attribute){
        // Lookup rules map for the attribute
        List<String> proposition = this.rulesMap.get(attribute);
        double bel = this.dsframe.getBelief(proposition);
        return bel;
    }

    public double getRulePl(String attribute){
        // Lookup rules map for the attribute
        List<String> proposition = this.rulesMap.get(attribute);
        double pl = this.dsframe.getPlausibility(proposition);
        return pl;
    }

    private void updateBelsPlsMap(){

        // For each attribute (i.e. Norm) in the frame being updated:
        for (Map.Entry<String, List<Double>> attEntry : this.belPlausRules.entrySet()){
            List<String> attProp  = this.getRulesMap().get(attEntry.getKey());
            double bel = this.dsframe.getBelief(attProp);
            double pl = this.dsframe.getPlausibility(attProp);
            List<Double> unc = new ArrayList<>(Arrays.asList(bel,pl));
            this.belPlausRules.put(attEntry.getKey(),unc);
        }

    }

    private void updateBelsPlsMapALT(IndexedDSFrame incomingIndexedFrame, HashMap<BigInteger,Double> priorMasses) {
        HashMap<BigInteger, Double> incomingCore = incomingIndexedFrame.getDsframe().getAllNormalizedMasses();
        // Update the bels and plaus hashmap (without computing bels with getBel())
        //  we can do this by looking at the incoming BoE and seeing what proposition
        //  then we can see what bits where set and only update those

        // For each attribute in the frame being updated:
        for (Map.Entry<String, List<Double>> attEntry : this.belPlausRules.entrySet()) {

            //Find corresponding bit location in the attrivbutes array
            int bitLoc = this.attributes.indexOf(attEntry.getKey());


            //for each entry in the incoming data focal set (Typically, this is A, Theta),
            // check if this data entry will influence the mental beliefs and plaus.
            for (Map.Entry<BigInteger, Double> dataEntry : incomingCore.entrySet()) {

                // Decide if attEntry belief and plausibility will be increased or decreased
                // (a) get the proposition
                List<String> prop = incomingIndexedFrame.getDsframe().getProposition(dataEntry.getKey());

                if (prop.size() < incomingIndexedFrame.getDsframe().getSingletons().size()) {

                    // (b) run through this proposition and see if the bit was set and remained set.
                    Boolean flag = false;
                    int counter = 0;
                    BitSet andedBitset = new BitSet(this.attributes.size());
                    BitSet oredBitset = new BitSet(this.attributes.size());
                    andedBitset.set(0, this.attributes.size(), true);
                    oredBitset.set(0, this.attributes.size(), false);
                    for (String item : prop) {
                        BitSet first = fromString(item);
                        andedBitset.and(first);
                        oredBitset.or(first);
                    }

                    // Pull out new mass of data entry proposition
                    // Note all the beliefs and plausibilities will be updated by this.
                    double massA = this.getDsframe().getNormalizedMass(prop);
                    double prior = 0.0;
                    if (priorMasses.get(dataEntry.getKey()) != null) {
                        prior = priorMasses.get(dataEntry.getKey());
                    } else {
                        prior = 0.0;
                    }
                    double change = massA - prior;
                    // (A) Increase Belief
                    // Look through andedBitSet and say whether the bit corresponding to the norm was set
                    if (andedBitset.get(bitLoc)) {
                        // Increase belief
                        double belnew = attEntry.getValue().get(0) + change; // new belief
                        if (belnew > 1.0) {
                            belnew = 1.0;
                        }
                        List<Double> belPlausNew = new ArrayList<>(Arrays.asList(belnew, attEntry.getValue().get(1)));
                        attEntry.setValue(belPlausNew);
                    } else {
                        if (oredBitset.get(bitLoc)) {
                            // Increase plausibility
                            double plnew = attEntry.getValue().get(1) + change; // new plausibility
                            if (plnew > 1.0) {
                                plnew = 1.0;
                            }
                            List<Double> belPlausNewNext = new ArrayList<>(Arrays.asList(attEntry.getValue().get(0), plnew));
                            attEntry.setValue(belPlausNewNext);
                        } else {
                            // Decrease plausibility
                            double plnew = attEntry.getValue().get(1) - change; // new plausibility
                            if (plnew < 0.0) { // if plaus is less than belief
                                plnew = 0.0;
                            }
                            List<Double> belPlausNewNext = new ArrayList<>(Arrays.asList(attEntry.getValue().get(0), plnew));
                            attEntry.setValue(belPlausNewNext);
                        }
                    }

                } else {
                    // Theta: Should increase the plausibility of all propositions
                    double massT = this.getDsframe().getNormalizedMass(prop);
                    double prior = 0.0;
                    if (priorMasses.get(dataEntry.getKey()) != null) {
                        prior = priorMasses.get(dataEntry.getKey());
                    } else {
                        prior = 0.0;
                    }
                    double change = massT - prior;
                    double plnew = attEntry.getValue().get(1) + change;
                    List<Double> belPlausNewNext = new ArrayList<>(Arrays.asList(attEntry.getValue().get(0), plnew));
                    attEntry.setValue(belPlausNewNext);

                }
            }
        }
    }



    public void update(IndexedDSFrame incomingIndexedFrame, double alpha){

        HashMap<BigInteger,Double> incomingCore = incomingIndexedFrame.getDsframe().getAllNormalizedMasses();
        HashMap<BigInteger,Double> priorMasses = new HashMap<>();

        // For each of the incoming propositions, get the mass in mental boe before update
        for (Map.Entry<BigInteger,Double> dataEntry : incomingCore.entrySet()){
            priorMasses.put(dataEntry.getKey(),this.dsframe.getAllNormalizedMasses().get(dataEntry.getKey()));
        }

        // Perform UPDATE operation
        this.dsframe.update(incomingIndexedFrame.getDsframe(),alpha);


        updateBelsPlsMap();

        }

    private String toString(BitSet bs) {
        return Long.toString(bs.toLongArray()[0], 2);
    }


    private BitSet fromString(String binary) {
        BitSet bitset = new BitSet(binary.length());
        for (int i = 0; i < binary.length(); i++) {
            if (binary.charAt(i) == '1') {
                bitset.set(i);
            }
        }
        return bitset;
    }


    //**********************************************************************//

    /**
     * Sets the DSFrame based on the attributes, sets rules.
     * The idea is to elaborate the list of attributes to all possible combinations of itself
     * and its negations.
     * @param attributes
     */
    private void initialize(List<String> attributes){
        List<String> singletons = extractHypotheses(attributes);
        List<BigInteger> power = initializePower(singletons.size());
        initializeWithSingletons(singletons,power);
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

    private void initializeWithSingletons(List<String> singletons, List<BigInteger> power){
        // Initialize DSFrame

        this.dsframe = new DSFrame(singletons,power);
        this.dsframe.setMass(singletons,1.0); // Vacuous mass function

        // Sets the rules (i.e., figure out propositions for rules, and
        // initialize beliefs and plausibilities for the rules.
        this.rulesMap = new HashMap<>();
        this.belPlausRules = new HashMap<>();
        List<Double> vacuousUncertainty = new ArrayList<>(Arrays.asList(0.0,1.0));
        for (int i = 0; i < attributes.size(); i++){
            // For each attribute, get the proposition that corresponds to the rule.
            List<String> prop = getRuleProposition(attributes.get(i),attributes);
            this.rulesMap.put(attributes.get(i),prop);
            this.belPlausRules.put(attributes.get(i),vacuousUncertainty);
        }


    }

    /**
     * Returns a proposition (list of singletons), where each singleton is off the form "10010"
     * The returned proposition corresponds to a rule proposition.
     * @param attribute
     * @param attributes
     * @return
     */
    private List<String> getRuleProposition(String  attribute, List<String> attributes){
        // Generate a string of n "e"s. Where n is the size of the attribute list
        String eStr = String.join("", Collections.nCopies(attributes.size(), "e"));

        // Replace the one character at position
        int pos = attributes.indexOf(attribute);
        String updatedEStr = replaceCharAt(eStr,pos,'1');

        List<String> proposition = new ArrayList<>();
        getIgnorancePossibilities(updatedEStr,proposition);

        return proposition;
    }

    /**
     * Given some set of n attributes, the idea is to extract 2^n singletons for the frame.
     * Singeletons are named after the corresponding bitset e.g., 000110100  of length n
     * @param attributes
     * @return
     */
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

    /**
     * This method basically lists out possible dataline strings when there is ignorance.
     * For example "10e0" could represent "1010" or "1000" as the e = 0,1.
     * So this method will return a list [1010,1000]
     * This method ASSUMES there is at least one "e" in dataline.
     * @param dataLine
     * @return
     */
    private void getIgnorancePossibilities(String dataLine, List<String> dataLineStr) {

        // Counts the number of "e"s in the dataline.
        int eCount = 0;
        for (int i = 0; i < dataLine.length(); i++) {
            if (dataLine.charAt(i) == 'e') {
                eCount++;
            }
        }
        // base case: if eCount is zero, then you're done.
        if (eCount == 0) {
            return;
        } else {
            // extract index of the first e
            int eIndex = dataLine.indexOf('e');
            String dataLine1 = replaceCharAt(dataLine, eIndex, '1');
            String dataLine0 = replaceCharAt(dataLine, eIndex, '0');
            if (eCount == 1) {
                dataLineStr.add(dataLine1);
                dataLineStr.add(dataLine0);
            }
            getIgnorancePossibilities(dataLine1, dataLineStr);
            getIgnorancePossibilities(dataLine0, dataLineStr);
        }
    }

    private String replaceCharAt(String s, int pos, char c) {
        return s.substring(0, pos) + c + s.substring(pos + 1);
    }

}