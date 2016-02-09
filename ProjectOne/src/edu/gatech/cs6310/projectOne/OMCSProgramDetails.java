package edu.gatech.cs6310.projectOne;

public class OMCSProgramDetails {

    /**
     * definition of which courses are offered when. Each row is a course, and
     * each column is true if the course is offered that semester, and false if
     * the course is not offered
     */
    private final boolean[][] courseOfferings = {
            { true, false, false, true, false, false, true, false, false, true,
                    false, false }, // 1
            { true, true, true, true, true, true, true, true, true, true, true,
                    true }, // 2
            { true, true, true, true, true, true, true, true, true, true, true,
                    true }, // 3
            { true, true, true, true, true, true, true, true, true, true, true,
                    true }, // 4
            { false, true, false, false, true, false, false, true, false,
                    false, true, false }, // 5
            { true, true, true, true, true, true, true, true, true, true, true,
                    true }, // 6
            { true, false, false, true, false, false, true, false, false, true,
                    false, false }, // 7
            { true, true, true, true, true, true, true, true, true, true, true,
                    true }, // 8
            { true, true, true, true, true, true, true, true, true, true, true,
                    true }, // 9
            { false, true, false, false, true, false, false, true, false,
                    false, true, false }, // 10
            { true, false, false, true, false, false, true, false, false, true,
                    false, false }, // 11
            { true, true, true, true, true, true, true, true, true, true, true,
                    true }, // 12
            { true, true, true, true, true, true, true, true, true, true, true,
                    true }, // 13
            { false, true, false, false, true, false, false, true, false,
                    false, true, false }, // 14
            { true, false, false, true, false, false, true, false, false, true,
                    false, false }, // 15
            { false, true, false, false, true, false, false, true, false,
                    false, true, false }, // 16
            { true, false, false, true, false, false, true, false, false, true,
                    false, false }, // 17
            { false, true, false, false, true, false, false, true, false,
                    false, true, false } }; // 18

    /**
     * the number of courses that define a full load. No student can exceed this
     * number of courses per semester.
     */
    private final int fullLoad = 2;

    /**
     * the number of courses offered.
     */
    private final int numCourses = 18;

    /**
     * the number of semesters courses will be offered.
     */
    private final int numSemesters = 12;

    /**
     * which courses must be taken before which other courses.
     */
    private final Prerequisite[] prerequisites = { new Prerequisite(4, 16),
            new Prerequisite(12, 1), new Prerequisite(9, 13),
            new Prerequisite(3, 7) };

    /**
     * accessor for full load variable
     * 
     * @return the full load
     */
    public int getFullLoad() {
        return fullLoad;
    }

    /**
     * accessor for number of courses offered
     * 
     * @return the number of courses
     */
    public int getNumCourses() {
        return numCourses;
    }

    /**
     * accessor for number of semesters
     * 
     * @return the number of semesters
     */
    public int getNumSemesters() {
        return numSemesters;
    }

    /**
     * accessor for the list of prerequisites
     * 
     * @return the list of prerequisites
     */
    public Prerequisite[] getPrerequisites() {
        return prerequisites;
    }

    /**
     * checks if a certain course is offered during a certain semester
     * 
     * @param course
     *            the course requested
     * @param semester
     *            the semester requested
     * @return true if the course is offered during that semester and false if
     *         not.
     */
    public boolean isCourseOffered(final int course, final int semester) {

        int courseIndex = course - 1;
        int semesterIndex = semester - 1;

        if ((courseIndex >= 0) && (courseIndex < numCourses)
                && (semesterIndex >= 0) && (semesterIndex < numSemesters)) {
            return courseOfferings[courseIndex][semesterIndex];
        } else {
            return false;
        }
    }

}
