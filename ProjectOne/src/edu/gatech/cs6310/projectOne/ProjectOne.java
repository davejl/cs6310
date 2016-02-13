package edu.gatech.cs6310.projectOne;

import java.security.InvalidParameterException;
import java.util.Arrays;

/**
 * Main class for Project One
 * 
 * @author David Loibl
 * 
 */
public class ProjectOne {

    /**
     * main function
     * 
     * @param args
     *            arguments to main function
     */
    public static void main(String[] args) {

        int filenameIndex = Arrays.asList(args).indexOf("-i") + 1;

        if (filenameIndex == 0 || filenameIndex >= args.length) {
            throw new InvalidParameterException("Usage: java "
                            + ProjectOne.class.getName()
                            + " -i <student demands filename>");
        }

        String inputFilename = args[filenameIndex];

        Project1Scheduler scheduler = new Project1Scheduler();
        scheduler.calculateSchedule(inputFilename);
        System.out.printf("X=%.2f", scheduler.getObjectiveValue());

    }

}
