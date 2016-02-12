package edu.gatech.cs6310.projectOne;

/**
 * Used to define prequisite classes
 * 
 * @author David Loibl
 * 
 */
public class Prerequisite {

    /**
     * courseID of the postrequisite course
     */
    private final int postreq;

    /**
     * courseID of the prerequisite course
     */
    private final int prereq;

    /**
     * constructor for a Prerequisite
     * 
     * @param prereq
     *            the prerequisite courseID
     * @param postreq
     *            the postrequisite courseID
     */
    public Prerequisite(int prereq, int postreq) {
        this.prereq = prereq;
        this.postreq = postreq;
    }

    /**
     * accessor for postreq
     * 
     * @return the postrequisite courseID
     */
    public int getPostreq() {
        return postreq;
    }

    /**
     * accessor for prereq
     * 
     * @return the prerequisite course
     */
    public int getPrereq() {
        return prereq;
    }
}
