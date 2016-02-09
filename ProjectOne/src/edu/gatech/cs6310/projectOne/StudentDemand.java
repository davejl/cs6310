package edu.gatech.cs6310.projectOne;

public class StudentDemand {

    /**
     * the course ID of the course that the student is requesting
     */
    private final int courseID;

    /**
     * the semester ID of the semester the student is requesting to take the
     * course
     */
    private final int semesterID;

    /**
     * the student ID of the student requesting the class
     */
    private final int studentID;

    /**
     * constructor for the StudentDemand.
     * 
     * @param studentID
     *            the student ID of the student requesting the class
     * @param courseID
     *            the course ID of the course that the student is requesting
     * @param semesterID
     *            the semester ID of the semester the student is requesting to
     *            take the course
     */
    public StudentDemand(int studentID, int courseID, int semesterID) {
        this.studentID = studentID;
        this.courseID = courseID;
        this.semesterID = semesterID;
    }

    /**
     * accessor for courseID
     * 
     * @return the course ID for this Student Demand
     */
    public int getCourseID() {
        return courseID;
    }

    /**
     * accessor for semesterID
     * 
     * @return the semester ID for this Student Demand
     */
    public int getSemesterID() {
        return semesterID;
    }

    /**
     * accessor for studentID
     * 
     * @return the student ID for this Student Demand
     */
    public int getStudentID() {
        return studentID;
    }

}
