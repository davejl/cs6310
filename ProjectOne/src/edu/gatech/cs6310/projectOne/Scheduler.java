package edu.gatech.cs6310.projectOne;

import java.util.Vector;

/**
 * Defines a scheduler, used to schedule students in classes
 * 
 * @author David Loibl
 * 
 */
public interface Scheduler {

    /**
     * Calculates the schedule of students based on their requested courses
     * 
     * @param dataFolder
     *            the file to read from for student requested classes
     */
    public void calculateSchedule(String dataFolder);

    /**
     * accessor for the objective value of the model
     * 
     * @return the objective value as a double
     */
    public double getObjectiveValue();

    /**
     * gets the courses that this student is taking this semester
     * 
     * @param student
     *            the student
     * @param semester
     *            the semester
     * @return a vector of class names which the student will take this semester
     */
    public Vector<String> getCoursesForStudentSemester(String student,
                    String semester);

    /**
     * gets the students who are taking this course this semester
     * 
     * @param course
     *            the course
     * @param semester
     *            the semester
     * @return a vector of students who are in the course this semester
     */
    public Vector<String> getStudentsForCourseSemester(String course,
                    String semester);
}
