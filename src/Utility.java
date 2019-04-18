package norms;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by XXX on 8/14/18.
 */
public class Utility {

    public static boolean flipBiasedCoin(double bias){
        double current = Math.random();
        if(current < bias) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * Generates n attributes. E.g., if n = 5, then [a,b,c,d,e]
     * @param n
     * @return
     */
    public static List<String> generateAttributes(int n){
        List<String> frameSingles = new ArrayList<>();
        for (int i = 0; i < n; ++i){
            frameSingles.add(str(i));
        }
        return frameSingles;
    }

    // Generates alphabet strings for n singletons
    private static String str(int n) {
        return n < 0 ? "" : str((n / 26) - 1) + (char)(65 + n % 26);
    }


    public static int randomDeonticChoice(long seed){
        List<Integer> givenList = Arrays.asList(1, -1, 0);
        Random rand = new Random();
        rand.setSeed(seed);
        int randomElement = givenList.get(rand.nextInt(givenList.size()));
        return randomElement;
    }

    public static double sampleGaussian(Long seed,double desiredMean,double desiredStandardDeviation){
        Random r = new Random();
        r.setSeed(seed);

        Boolean flag = false;
        double mySample = -1;
        while (!flag){
            mySample = r.nextGaussian()*desiredStandardDeviation+desiredMean;
            if (0.0 <= mySample && mySample <= 1.0) {
                flag = true;
            }
        }
        return mySample;
    }


    public static String toNumeralString(final Boolean input) {
        if (input == null) {
            return "null";
        } else {
            return input.booleanValue() ? "1" : "0";
        }
    }

    /**
     * This method basically lists out possible dataline strings when there is ignorance.
     * For example "10e0" could represent "1010" or "1000" as the e = 0,1.
     * So this method will return a list [1010,1000]
     * This method ASSUMES there is at least one "e" in dataline.
     * @param dataLine
     * @return
     */
    public static void getIgnorantProposition(String dataLine, List<String> dataLineStr) {

        // Counts the number of "e"s in the dataline.
        int eCount = 0;
        for (int i = 0; i < dataLine.length(); i++) {
            if (dataLine.charAt(i) == 'e') {
                eCount++;
            }
        }
        // base case: if eCount is zero, then you're done.
        if (eCount == 0) {
            //dataLineStr.add(dataLine);
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
            getIgnorantProposition(dataLine1, dataLineStr);
            getIgnorantProposition(dataLine0, dataLineStr);
        }
    }

    public static String replaceCharAt(String s, int pos, char c) {
        return s.substring(0, pos) + c + s.substring(pos + 1);
    }

    public static void progressPercentage(int remain, int total) {
        if (remain > total) {
            throw new IllegalArgumentException();
        }
        int maxBareSize = 10; // 10unit for 100%
        int remainProcent = ((100 * remain) / total) / maxBareSize;
        char defaultChar = '-';
        String icon = "*";
        String bare = new String(new char[maxBareSize]).replace('\0', defaultChar) + "]";
        StringBuilder bareDone = new StringBuilder();
        bareDone.append("[");
        for (int i = 0; i < remainProcent; i++) {
            bareDone.append(icon);
        }
        String bareRemain = bare.substring(remainProcent, bare.length());
        System.out.print("\r" + bareDone + bareRemain + " " + remainProcent * 10 + "%");
        if (remain == total) {
            System.out.print("\n");
        }
    }


    public static void createCSV(List<String> headerNames, String filename){
        try
        {
            File outputFilename = new File(filename);
            outputFilename.getParentFile().mkdirs();
            FileWriter writer = new FileWriter(outputFilename);

            // Header
            StringBuilder sb_head = new StringBuilder();
            for (int ii = 0; ii<headerNames.size(); ii++){
                sb_head.append(headerNames.get(ii));
                if (ii < headerNames.size()-1){
                    sb_head.append(",");
                }
            }

            writer.append(sb_head.toString());
            writer.append('\n');

            writer.flush();
            writer.close();
        }
        catch(IOException e)
        {

            e.printStackTrace();
        }
    }


    public static void writeToCSV(List<String> exptResults, String filename){
        // Write to file
        try
        {
            File outputFilename = new File(filename);
            outputFilename.getParentFile().mkdirs();
            FileWriter writer = new FileWriter(outputFilename,true);

            StringBuilder sb = new StringBuilder();
            for (int ii = 0; ii<exptResults.size(); ii++){
                sb.append(exptResults.get(ii));
                if (ii < exptResults.size()-1){
                    sb.append(",");
                }
            }
            writer.append(sb.toString());
            writer.append('\n');
            writer.flush();
            writer.close();
        }
        catch(IOException e)
        {

            e.printStackTrace();
        }
    }



}
