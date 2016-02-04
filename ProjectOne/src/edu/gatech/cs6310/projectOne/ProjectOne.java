package edu.gatech.cs6310.projectOne;


public class ProjectOne {

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        Project1Scheduler scheduler = new Project1Scheduler();
        scheduler.calculateSchedule(args[0]);

        float result = (float) 3.1415;
        System.out.printf("X=%.2f", result);
    }

}
