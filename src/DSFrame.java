/**
 * The MIT License
 *
 *  Copyright (c) 2017, Vasanth Sarathy
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */

package norms;

import java.math.BigInteger;
import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by XXX on 7/7/17. Updated 8/16/18
 * Implemented (with modifications: Polpitiya, L. G. (2017). Efficient Computation of Belief Theoretic Conditionals, 62, 265–276.
 */
public class DSFrame {

    private List<BigInteger> power; // Lookup table containing keys of singletons in the DSVector
    private int numberOfSingletons;
    private List<String> singletons; // List of lowercased singletons of size n. The index of these singletons is important.
    private HashMap<BigInteger,Double> massesDSVector; // Hashmap containing masses (unnormalized)
    private static long count=0L;
    private long frameId;

    private static final Logger log = LogManager.getLogger(DSFrame.class);

    //****************** CONSTRUCTORS ************************************//


    /**
     * Constructor function that initializes the DSFrame given a list of singletons.
     * @param singletons
     */

    public DSFrame(List<String> singletons){
        singletons.stream().map(string -> string.toLowerCase()); // converts all singletons to lower case
        this.singletons = singletons;
        this.numberOfSingletons = singletons.size();
        BigInteger two = new BigInteger("2");
        BigInteger sizeOfPowerSet = two.pow(this.numberOfSingletons);

        this.massesDSVector = new HashMap<>();

        this.power = initializePower(this.numberOfSingletons);

        log.debug("Initialized frame");
        log.debug("Singletons: "+this.singletons);
        log.debug("Masses: "+this.massesDSVector);
        log.debug("Power: "+ this.power);

        //setting frame id
        this.frameId = ++count;
    }


    public DSFrame(List<String> singletons, List<BigInteger> power){
        singletons.stream().map(string -> string.toLowerCase()); // converts all singletons to lower case
        this.singletons = singletons;
        this.numberOfSingletons = singletons.size();
        BigInteger two = new BigInteger("2");
        BigInteger sizeOfPowerSet = two.pow(this.numberOfSingletons);

        this.massesDSVector = new HashMap<>();

        this.power = power;
        //initializePower();

        log.debug("Initialized frame");
        log.debug("Singletons: "+this.singletons);
        log.debug("Masses: "+this.massesDSVector);
        log.debug("Power: "+ this.power);

        //setting frame id
        this.frameId = ++count;
    }
    //**********************************************************************//

    //****************** GENERAL FRAME STUFF ************************************//
    /**
     * Returns the singletons in the FoD.
     * @return
     */
    public List<String> getSingletons(){
        return this.singletons;
    }

    /**
     * Returns the lookup table called "Power"
     * @return
     */
    public List<BigInteger> getPower(){
        return this.power;
    }

    /**
     * Returns those elements with non-zero masses.
     * @return
     */
    public HashMap<BigInteger,Double> getCore() {
        // Core is simply the masses hash table
        return this.massesDSVector;
    }

    /**
     * (NOT IMPLEMENTED)
     * Returns propositions that have non-zero beliefs
     * COULD BE COMPUTATIONALLY EXPENSIVE
     * @return
     */
    public Map<List<String>,Double> getBeliefCore() {
        // Not implemented
        // could be a very costly computation for large frames.

        return new HashMap<>();
    }

    /**
     * (WARNING!! Could be computationally expensive)
     * Returns a proposition (list of singleton strings), given an index in the masses DS Vector
     * @param in
     * @return
     */
    public List<String> getProposition(BigInteger in){
        // Create a bitset:
        BitSet bits = new BitSet(this.singletons.size());
        int index = 0;
        while (in != BigInteger.valueOf(0)) {
            if (in.mod(BigInteger.valueOf(2)) != BigInteger.valueOf(0)) {
                bits.set(index);
            }
            ++index;
            in = in.shiftRight(1);
        }
        List<String> proposition = new ArrayList<>();
        try {
            for (int i = bits.nextSetBit(0); i != -1; i = bits.nextSetBit(i+1)) {
                proposition.add(this.singletons.get(i));
            }
        } catch (IndexOutOfBoundsException e){
            System.out.println("Exception thrown  :" + e);
        }


        return proposition;
    }

    /**
     * Returns the id of a frame. The id is automatically created at instantiation.
     * Each instance of the frame has a new frame id.
     * @return
     */
    public long getFrameId(){
        return this.frameId;
    }

    //****************** SET/GET MASSES ************************************//
    /**
     * sets mass for a specific singleton
     * @param singleton
     * @param mass
     */
    public void setMass(String singleton, double mass){
        singleton.toLowerCase(); //NEW
        List<String> listForm = new ArrayList<>();
        listForm.add(singleton);
        setMass(listForm,mass);
    }

    /**
     * Sets mass for a proposition (list of singleton)
     * @param proposition
     * @param mass
     */
    public void setMass(List<String> proposition, double mass){
        proposition.stream().map(string -> string.toLowerCase()); //NEW
        BigInteger index = accessMass(proposition); // Access the corresponding location in the DSVector
        this.massesDSVector.put(index,mass);
    }

    /**
     * Get mass for a particular singleton.
     * @param singleton
     * @return
     */
    public double getMass(String singleton){
        singleton.toLowerCase(); //NEW
        List<String> listForm = new ArrayList<>();
        listForm.add(singleton);
        return getMass(listForm);
    }

    /**
     * Get mass for a proposition (list of singletons).
     * @param proposition
     * @return
     */
    public double getMass(List<String> proposition){
        proposition.stream().map(string -> string.toLowerCase()); //NEW
        BigInteger index = accessMass(proposition);
        return this.massesDSVector.get(index);
    }

    /**
     * Returns normalized mass for a singleton.
     * @param singleton
     * @return
     */
    public double getNormalizedMass(String singleton){
        singleton.toLowerCase(); //NEW
        return getMass(singleton)/getNormalizingConstant();
    }

    /**
     * Returns the normalized mass for a proposition.
     * @param proposition
     * @return
     */
    public double getNormalizedMass(List<String> proposition){
        proposition.stream().map(string -> string.toLowerCase()); //NEW
        return getMass(proposition)/getNormalizingConstant();
    }

    /**
     * Returns all normalized masses. Masses for all propositions in the frame.
     * @return
     */
    public HashMap<BigInteger,Double> getAllNormalizedMasses(){
        HashMap<BigInteger,Double> normalizedMasses = new HashMap<>();
        for (Map.Entry<BigInteger,Double> entry : this.massesDSVector.entrySet()){
            double nMass = entry.getValue()/getNormalizingConstant();
            normalizedMasses.put(entry.getKey(),nMass);
        }
        return normalizedMasses;
    }

    /**
     * Clears all masses in the DSVector.
     * Useful when conditioning and updating.
     */
    public void clearMasses(){
        this.massesDSVector.clear();
    }

    //****************** SET/GET BELIEFS, PLAUSIBILITIES, STRADDLE ************************************//
    /**
     * Returns the belief of a proposition.
     * @param proposition
     * @return
     */
    public double getBelief(List<String> proposition){
        proposition.stream().map(string -> string.toLowerCase()); // NEW
        HashMap<BigInteger,Double> subsetOfCore = getSubsetsInCore(proposition);
        double sum = 0.0;
        for (Map.Entry<BigInteger,Double> entry : subsetOfCore.entrySet()){
            sum = sum + entry.getValue();
        }
        return sum/getNormalizingConstant();
    }

    /**
     * Returns the plausibility of a proposition.
     * @param proposition
     * @return
     */
    public double getPlausibility(List<String> proposition){
        proposition.stream().map(string -> string.toLowerCase()); //NEW

        // Need more efficient way to compute plausibility
        // Iterate through hashmap and determine if an entry can support the complement
        // i.e., if it contains elements not in the proposition given
        // Then add these up, normalize them and then do a 1 minus on that result

        double sum = 0.0;
        for (Map.Entry<BigInteger,Double> entry : this.massesDSVector.entrySet()){
            List<String> item = getProposition(entry.getKey());

            if (!Collections.disjoint(item, proposition)){
            } else {
                sum = sum + entry.getValue();
            }
        }

        double plausibility = 1 - sum/getNormalizingConstant();
        return plausibility;
    }


    //******************* SPECIAL METHODS FOR LARGE FRAMES *****************************//

    /**
     * Sets the mass of theta
     */
    public void setMassTheta(double mass){
        setMass(this.singletons,mass);
    }

    /**
     * Makes a whole frame vacuous m(Theta) = 1.
     */
    public void makeVacuous(){
        this.massesDSVector.clear();
        setMassTheta(1.0);
    }

    //**********************************************************************************//

    /**
     * Given a proposition, determine conditional masses for items in the core
     * @param proposition
     * @return
     */
    public HashMap<BigInteger,Double> getConditionedCore(List<String> a){
        a.stream().map(string -> string.toLowerCase()); //NEW

        // Get the part of the core that is a subset of input "a"
        HashMap<BigInteger,Double> subsetOfCore = getSubsetsInCore(a);

        // Iterate through each item in the core
        for (Map.Entry<BigInteger,Double> entry : subsetOfCore.entrySet()){

            List<String> b = getProposition(entry.getKey());

            // Do a set difference A-B
            List<String> temp = new ArrayList<>(a);
            temp.removeAll(b);

            double pl_aMinusB = 0.0;
            if (!temp.isEmpty()){
                pl_aMinusB = getPlausibility(temp);
            }

            double mass_bGa = (entry.getValue())/(entry.getValue() + pl_aMinusB);
            entry.setValue(mass_bGa);
        }
        return subsetOfCore;
    }

    // m(b|a)
    public double conditionalMass(BigInteger b, BigInteger a){
        List<String> b_prop = getProposition(b);
        List<String> a_prop = getProposition(a);
        return conditionalMass(b_prop,a_prop);
    }


    public double conditionalMass(List<String> b, List<String> a){
        a.stream().map(string -> string.toLowerCase()); //NEW]

        // Get the part of the core that is a subset of input "a"
        HashMap<BigInteger,Double> subsetOfCore = getSubsetsInCore(a);
        Double massB = this.massesDSVector.get(accessMass(b));
        double pl_aMinusB = 0.0;
        if (massB != null){
            // Do a set difference A-B
            List<String> temp = new ArrayList<>(a);
            temp.removeAll(b);
            if (!temp.isEmpty()){
                pl_aMinusB = getPlausibility(temp);
            }
        } else {
            massB = 0.0;
        }
        double mass_bGa = (massB)/(massB + pl_aMinusB);
        return mass_bGa;
    }

    /**
     * CUE Algorithm - Mass-based Conditional Update
     * @param newFrame
     * @param alpha
     */
    public void update(DSFrame newFrame, double alpha){

        for (Map.Entry<BigInteger,Double> b : newFrame.getAllNormalizedMasses().entrySet()){

            // condition all in newFrame on a
            double sum = 0;
            for (Map.Entry<BigInteger,Double> a : newFrame.getCore().entrySet()){

                // Check if b is a subset of a. If not, then m(b|a) = 0
                List<String> a_prop = newFrame.getProposition(a.getKey());
                List<String> b_prop = newFrame.getProposition(b.getKey());
                Boolean isSubset = a_prop.containsAll(b_prop);
                double m_bGa = 0;
                if (isSubset){
                    m_bGa = newFrame.conditionalMass(b.getKey(),a.getKey());
                }
                double beta = a.getValue();
                double mult = m_bGa * beta;
                sum = sum + mult;
            }

            Double currentMass = this.massesDSVector.get(b.getKey());
            double term1 = 0.0;
            if (currentMass != null){
                term1 = alpha * currentMass;
            }
            double term2 = (1-alpha) * sum;
            setMass(getProposition(b.getKey()),term1 + term2);
        }
    }



    //****************** IMPORTANT HELPERS **********************************************//

    /**
     * Initializes lookup table called "power".
     * Each index i in this lookup table contains the value 2^i.
     * The lookup table is of size = number of singletons, and represents the position of the singleton masses in massesDSVector
     */
    private void initializePower(){
        this.power = new ArrayList<>();
        BigInteger powerVal = new BigInteger("1");
        for (int i = 0; i < this.numberOfSingletons; i++){
            this.power.add(powerVal);
            powerVal = powerVal.multiply(new BigInteger("2"));
        }
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

    /**
     * Returns a DSVector key of a proposition.
     * Useful for various operations including setMass, etc.
     * Algorithm obtained from Polpitiya paper 2017.
     * @param proposition
     * @return
     */
    public BigInteger accessMass(List<String> proposition){
        proposition.stream().map(string -> string.toLowerCase()); //NEW
        BigInteger key = BigInteger.valueOf(0);
        for (String singleton : proposition){
            try{
                int i = this.singletons.indexOf(singleton);
                key = key.add(power.get(i));
            } catch (Exception e){
                System.out.println("Error: One of the singletons in the proposition is not found in the frame");
            }
        }

        return key;
    }

    /**
     * Returns the sum of masses in the DS-Vector.
     * The Masses DSVector is unnormalized and so will need to be divided by this normalizing constant.
     * See the toString() method for the use of the normalizing constant.
     * @return
     */
    public double getNormalizingConstant(){
        double sum = 0;
        for (Map.Entry<BigInteger,Double> entry : this.massesDSVector.entrySet()){
            sum = sum + entry.getValue();
        }
        return sum;
    }


    /**
     * Given a proposition, return that part of the core that has only those propositions that are subsets of input
     * WORKS ON A NORMALIZED CORE.
     * @param proposition
     * @return
     */
    public HashMap<BigInteger,Double> getSubsetsInCore(List<String> proposition){
        proposition.stream().map(string -> string.toLowerCase()); //NEW
        HashMap<BigInteger,Double> coreSubsets = new HashMap<>();

        // Iterate through each item in the core and check if it is a subset
        coreloop:
        for (Map.Entry<BigInteger,Double> entry : this.massesDSVector.entrySet()){

            List<String> itemInCore = getProposition(entry.getKey());
            List<Integer> singletonIndexes = new ArrayList<>();
            for (String item : itemInCore){
                singletonIndexes.add(this.singletons.indexOf(item));
            }

            //String bitstring = entry.getKey().toString(2); // convert biginteger to bitstring
            //List<Integer> singletonIndexes = setBits(bitstring); // get singleton
            for (int singInd : singletonIndexes){
                if (!proposition.contains(this.singletons.get(singInd))){
                    continue coreloop;
                }
            }
            coreSubsets.put(entry.getKey(),entry.getValue());
        }
        return coreSubsets;
    }

    /**
     * Helper method to convert a bit string "01011" to a List of indexes of set bits [0,1,3]
     * @param in
     * @return
     */
    List<Integer> setBits(String in) {
        final List<Integer> setBits = new ArrayList<>();
        for (int i = in.length()-1, j = 0; i >= 0; i--, j++)
            if (in.charAt(i) == '1') setBits.add(j);
        return setBits;
    }


    /**
     * Returns the complement of a proposition in the frame.
     * helper method for plausibility
     * @param proposition
     * @return
     */
    public List<String> getComplement(List<String> proposition){
        proposition.stream().map(string -> string.toLowerCase()); //NEW
        // Create a bitset:
        BigInteger location = accessMass(proposition);
        BitSet bits = new BitSet(this.singletons.size());
        int index = 0;
        while (location != BigInteger.valueOf(0)) {
            if (location.mod(BigInteger.valueOf(2)) != BigInteger.valueOf(0)) {
                bits.set(index);
            }
            ++index;
            location = location.shiftRight(1);

        }
        bits.flip(0,this.singletons.size());
        List<String> complement = new ArrayList<>();
        for (int i = bits.nextSetBit(0); i != -1; i = bits.nextSetBit(i + 1)) {
            complement.add(this.singletons.get(i));
        }
        return complement;
    }






}
