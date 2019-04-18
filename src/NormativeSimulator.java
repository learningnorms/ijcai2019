package norms;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.rmi.CORBA.Util;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by XXX on 8/14/18.
 */
public class NormativeSimulator {

    private static final Logger log = LogManager.getLogger(NormativeSimulator.class);

    /**
     * Main method
     * @param args
     */
    public static void main(String[] args) {
        List<Integer> numActors_list = new ArrayList<>(Arrays.asList(50,100,200));
        List<Integer> numObservers_list = new ArrayList<>(Arrays.asList(5,15,25));
        List<Integer> numNorms_list = new ArrayList<>(Arrays.asList(5,7,11,13));
        List<Double> flatness_list  = new ArrayList<>(Arrays.asList(2.0));
        List<Integer> lifetime_list  = new ArrayList<>(Arrays.asList(50,100));
        List<Double>  sanctionRate_list = new ArrayList<>(Arrays.asList(0.25,1.0));
        List<Double> probIgnorance_list  = new ArrayList<>(Arrays.asList(0.1,0.9));
        List<Double> propMissing_list  = new ArrayList<>(Arrays.asList(0.1,0.9));
        List<Double> sensorReliability  = new ArrayList<>(Arrays.asList(0.1,0.99));


        List<Long> masterSeed_list  = getSeeds(10);

        String outputFile = "papers/AAAI_2019/data/Experiment.csv";
        Utility.createCSV(getHeaders(),outputFile);

        int total = masterSeed_list.size()*numNorms_list.size()*numActors_list.size()*numObservers_list.size()*flatness_list.size()*
                lifetime_list.size()*sanctionRate_list.size()*probIgnorance_list.size()*propMissing_list.size()*
                sensorReliability.size();

       int counter =0;
        for (long d : masterSeed_list){
            for (int n : numNorms_list){
                for (int a : numActors_list){
                    for (int o : numObservers_list){
                        for (double f : flatness_list) {
                            for (int l : lifetime_list) {
                                for (double s : sanctionRate_list) {
                                    for (double i : probIgnorance_list) {
                                        for (double m : propMissing_list) {
                                            for (double r : sensorReliability) {

                                                IndependentVariables iv = new IndependentVariables();
                                                iv.setSensorReliability(r);
                                                iv.setPropMissing(m);
                                                iv.setProbIgnorance(i);
                                                iv.setSanctionRate(s);
                                                iv.setLifetime(l);
                                                iv.setFlatness(f);
                                                iv.setNumObservers(o);
                                                iv.setNumActors(a);
                                                iv.setNumNorms(n);
                                                iv.setMasterSeed(d);

                                                System.out.println("(Simulation Run: " + counter + "/" + total + ")");
                                                NormativeSociety world = new NormativeSociety(outputFile, iv); // Creates and runs a world
                                                world.resetSimulation();
                                                counter++;


                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }


    }


    private static List<Long> getSeeds(int numberOfSeeds){
        List<Long> seeds = new ArrayList<>();
        Random r = new Random();
        for (int n = 0; n < numberOfSeeds; n++){
            long seed =  r.nextLong();
            seeds.add(seed);
        }

        return seeds;
    }


    public static List<String> getHeaders(){
        List<String> names = new ArrayList<>();
        names.add("numActors");
        names.add("numObservers");
        names.add("numNorms");
        names.add("lifetime");
        names.add("flatness");
        names.add("sanctionRate");
        names.add("probIgnorance");
        names.add("propMissing");
        names.add("sensorReliability");
        names.add("masterSeed");
        names.add("precision_Forbidden");
        names.add("precision_Optional");
        names.add("precision_Obligatory");
        names.add("recall_Forbidden");
        names.add("recall_Optional");
        names.add("recall_Obligatory");
        return names;
    }

}
