package norms;

/**
 * Created by XXX on 8/21/18.
 */
public class IndependentVariables {


    public int numActors;
    public int numObservers;
     // Must be an EVEN number. We are looking at interactions between pairs
    public long masterSeed;
    public int numNorms;
    public double flatness; //establishes how flat a distribution can be.
    public int lifetime;
    public double sanctionRate;
    public double probIgnorance; // Probability that a given reading contains missing values
    public double propMissing; // Proportion of values that are ambiguous in a given reading
    public double sensorReliability;

    public IndependentVariables(){

    }


    public void setNumObservers(int obs){
        this.numObservers = obs;
    }

    public void setNumActors(int act){
        this.numActors = act;
    }

    public void setMasterSeed(long seed){
        this.masterSeed = seed;
    }

    public void setNumNorms(int n){
        this.numNorms = n;
    }

    public void setFlatness(double f){
        this.flatness = f;
    }

    public void setLifetime(int l){
        this.lifetime = l;
    }

    public void setSanctionRate(double s){
        this.sanctionRate = s;
    }

    public void setProbIgnorance(double i){
        this.probIgnorance = i;
    }

    public void setPropMissing(double m){
        this.propMissing = m;
    }

    public void setSensorReliability(double s){
        this.sensorReliability = s;
    }

    public int getNumObservers() {
        return numObservers;
    }

    public int getNumActors() {
        return numActors;
    }

    public long getMasterSeed() {
        return masterSeed;
    }

    public int getNumNorms() {
        return numNorms;
    }

    public double getFlatness() {
        return flatness;
    }

    public int getLifetime() {
        return lifetime;
    }

    public double getSanctionRate() {
        return sanctionRate;
    }

    public double getProbIgnorance() {
        return probIgnorance;
    }

    public double getPropMissing() {
        return propMissing;
    }

    public double getSensorReliability() {
        return sensorReliability;
    }

    public int getPopulation(){
        return this.numActors + this.numObservers;
    }




}
