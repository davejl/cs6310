package edu.gatech.cs6310.projectOne;

public class OMCSProgramDetails {

    private static final int NUM_COURSES = 18;
    private static final int FULL_LOAD = 2;

    public static int getFullLoad() {
        return FULL_LOAD;
    }

    public static int getNumCourses() {
        return NUM_COURSES;
    }

    public static int getNumSemesters() {
        return NUM_SEMESTERS;
    }

    private static final int NUM_SEMESTERS = 12;

    private static final boolean[][] COURSE_OFFERINGS = {
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

    public static boolean isCourseOffered(final int course, final int semester) {
        if ((course - 1 < NUM_COURSES) && (semester - 1 < NUM_SEMESTERS)) {
            return COURSE_OFFERINGS[course - 1][semester - 1];
        } else {
            return false;
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub

    }

}
