package norms;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.rmi.CORBA.Util;

/**
 * Created by XXX on 8/16/18.
 */
public class Sensor {
    private double probMissing;
    private double propIgnorance;
    private double sensorReliability;
    private IndexedDSFrame templateFrame;

    private static final Logger log = LogManager.getLogger(Sensor.class);


    public Sensor(double probMissing, double propIgnorance, double sensorReliability, IndexedDSFrame templateFrame){
        this.probMissing = probMissing;
        this.propIgnorance = propIgnorance;
        this.sensorReliability = sensorReliability;
        this.templateFrame = templateFrame;
    }

    public void clearTemplateFrame() {

        /// CLONE!!!!
        this.templateFrame.getDsframe().clearMasses();
    }

    private String getReading(Integer actorID, VisibleWorld visibleWorld){
        // Get the visible actions for the particular actor
        HashMap<String,Boolean> actorSpecificActions = visibleWorld.getVisibleActions().get(actorID);

        // Generate a templateFrame from this
        //List<String> norms = visibleWorld.getVisibleActions().get(actorID).keySet().stream().collect(Collectors.toList());
        //templateFrame = new IndexedDSFrame("temp",norms);

        // Modify these actions based on propIgnorance, and probMissing
        // (a) decide if there is any ignorance at all in this data (using propIgnorance)
        // (b) if there is ignorance, then decide how much missing data there will be
        StringBuilder bitStr = new StringBuilder();
        Boolean ignorance = Utility.flipBiasedCoin(propIgnorance);
        if (ignorance){
            // For each norm, decide if "e" is needed
            for (HashMap.Entry<String,Boolean> entry : actorSpecificActions.entrySet()){
                Boolean missing = Utility.flipBiasedCoin(probMissing);
                if (missing){
                    bitStr.append("e");
                } else {
                    bitStr.append(Utility.toNumeralString(entry.getValue()));
                }
            }
        } else {
            for (HashMap.Entry<String,Boolean> entry : actorSpecificActions.entrySet()){
                bitStr.append(Utility.toNumeralString(entry.getValue()));
            }
        }

        String bits = bitStr.toString();
        return bits;
    }

    public IndexedDSFrame getBoE(Integer actorID, VisibleWorld visibleWorld){

        // get out 1e01 type reading from the sensor based on ignorance parameters


        // Get reading from the sensor
        // Ignore all readings that are all "e's". That is keep trying to take a reading.
        Boolean sensorFlag = false;
        String dataStr = "";
        while(!sensorFlag){
            dataStr = getReading(actorID,visibleWorld); // E.g., "1e01"
            if(!dataStr.matches("[e]+") && !dataStr.equals("")) {
                sensorFlag = true;
            }
        }

        // Figure out if the action was sanctioned. If so, flip bit.
        // look at dataStr. "11e01"
        // Need to determine
        HashMap<String,Boolean> sanctionsForActor = visibleWorld.getVisibleSanctions().get(actorID);
        List<String> norms = this.templateFrame.getAttributes();
        for (int i = 0; i < dataStr.length(); i++){
            char c = dataStr.charAt(i);
            if (sanctionsForActor.get(norms.get(i))){
                // Sanction alert!
                // Then flip bit
                if (c == '1'){
                    dataStr = Utility.replaceCharAt(dataStr,i,'0');

                } else if (c == '0'){
                    dataStr = Utility.replaceCharAt(dataStr,i,'1');

                }
            }
        }

        // Generate a proposition that works with the IndexedFrame
        List<String> proposition = new ArrayList<>();
        Utility.getIgnorantProposition(dataStr,proposition); // after this the proposition should contain [1001,1101]

        // Set the mass of the templateFrame
        templateFrame.getDsframe().clearMasses();

        if (proposition.isEmpty()){
            templateFrame.getDsframe().setMass(dataStr,sensorReliability);
        } else {
            templateFrame.getDsframe().setMass(proposition,sensorReliability);
        }
        templateFrame.getDsframe().setMassTheta(1-sensorReliability);



        return templateFrame;
    }
}
