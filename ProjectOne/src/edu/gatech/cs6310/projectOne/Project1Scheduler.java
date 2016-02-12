package edu.gatech.cs6310.projectOne;

import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class Project1Scheduler implements Scheduler {

    /**
     * the number of students requesting courses
     */
    private int numStudents;

    /**
     * the objective value as calculated by the model
     */
    private Double objectiveValue = null;

    /**
     * The details for the OMCS Program.
     */
    private final OMCSProgramDetails omcsProgramDetails = new OMCSProgramDetails();

    /**
     * GRBVars created in the model to represent which courses each student is
     * taking during which semester.
     */
    private GRBVar[][][] studCourseSemBooleanVars;

    /**
     * the list of student demands
     */
    private List<StudentDemand> studentDemands;

    @Override
    public void calculateSchedule(String dataFolder) {
        GRBEnv env;
        try {
            env = new GRBEnv("grb.log");
            env.set(GRB.IntParam.LogToConsole, 0);

            GRBModel model = new GRBModel(env);

            studentDemands = parseStudentDemandFile(dataFolder);

            studCourseSemBooleanVars = createStudCourseSemVariables(model);
            GRBVar courseSemClassSizeVar = createCourseSizeLimitVariable(model);

            addConstraints(model, courseSemClassSizeVar);
            setObjective(model, courseSemClassSizeVar);

            model.optimize();
            objectiveValue = model.get(GRB.DoubleAttr.ObjVal);

        } catch (IOException ioE) {
            ioE.printStackTrace();
        } catch (GRBException grbE) {
            grbE.printStackTrace();
        }

    }

    @Override
    public Vector<String> getCoursesForStudentSemester(String student,
            String semester) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public double getObjectiveValue() {
        return objectiveValue;
    }

    @Override
    public Vector<String> getStudentsForCourseSemester(String course,
            String semester) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * adds all constraints to our model
     * 
     * @param model
     *            the model to add the constraints to
     * @param courseSemClassSizeVar
     *            objective of our model
     * @throws GRBException
     *             on any exception from within GRB
     */
    private void addConstraints(GRBModel model, GRBVar courseSemClassSizeVar)
            throws GRBException {
        addFullLoadConstraintsToModel(model);
        addStudentOnlyTakeCourseOnceToModel(model);
        addCourseCapacityConstraintsToModel(courseSemClassSizeVar, model);
        addCoursePrerequisiteConstraintsToModel(model);
        addStudentDemandConstraintsToModel(studentDemands, model);
    }

    /**
     * adds all course capacity constraints to the model based on when classes
     * are offered
     * 
     * @param classSizeVar
     *            GRBVar which defines the size of a all classes. This is our
     *            objective.
     * @param model
     *            the model to add the constraints to
     * @throws GRBException
     *             on any exception from within GRB
     */
    private void addCourseCapacityConstraintsToModel(GRBVar classSizeVar,
            GRBModel model) throws GRBException {

        // Objective = minimize class size
        for (int course = 1; course <= omcsProgramDetails.getNumCourses(); ++course) {
            for (int semester = 1; semester <= omcsProgramDetails
                    .getNumSemesters(); ++semester) {
                GRBLinExpr classSize = new GRBLinExpr();
                for (int student = 1; student <= numStudents; ++student) {
                    classSize
                            .addTerm(
                                    1,
                                    studCourseSemBooleanVars[student][course][semester]);
                }

                if (omcsProgramDetails.isCourseOffered(course, semester)) {
                    String name = String.format("ClassCapacity_Co%d_Se%d",
                            course, semester);
                    model.addConstr(classSize, GRB.LESS_EQUAL, classSizeVar,
                            name);
                } else {
                    String name = String.format(
                            "ClassCapacity_CourseNotOffered_Co%d_Se%d", course,
                            semester);
                    model.addConstr(classSize, GRB.LESS_EQUAL, 0, name);
                }

            }
        }
    }

    /**
     * adds all prerequisite course constraints to the model based on the
     * omcsProgramDetails.
     * 
     * @param model
     *            the model to add the constraints to
     * @throws GRBException
     *             on any exception from within GRB
     */
    private void addCoursePrerequisiteConstraintsToModel(GRBModel model)
            throws GRBException {
        for (Prerequisite p : omcsProgramDetails.getPrerequisites()) {
            addCoursePrerequisiteConstraintToModel(p.getPrereq(),
                    p.getPostreq(), model);
        }
    }

    /**
     * adds a course prerequisite/postrequisite pair to our model
     * 
     * @param prereq
     *            the prerequisite course
     * @param postreq
     *            the postrequisite course
     * @param model
     *            the model to add the constraints to
     * @throws GRBException
     *             on any exception from within GRB
     */
    private void addCoursePrerequisiteConstraintToModel(int prereq,
            int postreq, GRBModel model) throws GRBException {

        for (int student = 1; student <= numStudents; ++student) {
            for (int semester = 1; semester <= omcsProgramDetails
                    .getNumSemesters(); ++semester) {
                GRBLinExpr coursePrereqLHS = new GRBLinExpr();
                for (int k = 1; k < semester; ++k) {
                    coursePrereqLHS.addTerm(1,
                            studCourseSemBooleanVars[student][prereq][k]);
                }
                String name = String.format("Prereq_St%d_Co%d_%d", student,
                        prereq, postreq);
                model.addConstr(coursePrereqLHS, GRB.GREATER_EQUAL,
                        studCourseSemBooleanVars[student][postreq][semester],
                        name);
            }
        }

    }

    /**
     * add full load constraints to our model. No student can take more than 2
     * classes at once.
     * 
     * @param model
     *            the model to add the constraints to
     * @throws GRBException
     *             on any exception from within GRB
     */
    private void addFullLoadConstraintsToModel(GRBModel model)
            throws GRBException {
        for (int student = 1; student <= numStudents; ++student) {
            for (int semester = 1; semester <= omcsProgramDetails
                    .getNumSemesters(); ++semester) {
                GRBLinExpr courseLoad = new GRBLinExpr();
                for (int course = 1; course <= omcsProgramDetails
                        .getNumCourses(); ++course) {
                    courseLoad
                            .addTerm(
                                    1,
                                    studCourseSemBooleanVars[student][course][semester]);
                }

                String name = String.format("FullLoad_St%d_Se%d", student,
                        semester);

                model.addConstr(courseLoad, GRB.LESS_EQUAL,
                        omcsProgramDetails.getFullLoad(), name);
            }
        }
    }

    /**
     * add student requested classes as constraints to the model
     * 
     * @param studentDemands
     * @param model
     *            the model to add the constraints to
     * @throws GRBException
     *             on any exception from within GRB
     */
    private void addStudentDemandConstraintsToModel(
            List<StudentDemand> studentDemands, GRBModel model)
            throws GRBException {
        // Students must take each course that they're requesting
        for (StudentDemand sd : studentDemands) {
            int student = sd.getStudentID();
            int course = sd.getCourseID();

            GRBLinExpr studentMustTakeCourse = new GRBLinExpr();
            for (int semester = 1; semester <= omcsProgramDetails
                    .getNumSemesters(); ++semester) {
                studentMustTakeCourse.addTerm(1,
                        studCourseSemBooleanVars[student][course][semester]);
            }

            String name = String.format("St%d_must_take_Co%d", student, course);
            model.addConstr(studentMustTakeCourse, GRB.EQUAL, 1, name);
        }
    }

    /**
     * adds constraints to the model which allows a class to be only taken once.
     * 
     * @param model
     *            the model to add the constraints to
     * @throws GRBException
     *             on any exception from within GRB
     */
    private void addStudentOnlyTakeCourseOnceToModel(GRBModel model)
            throws GRBException {

        for (int student = 1; student <= numStudents; ++student) {
            for (int course = 1; course <= omcsProgramDetails.getNumCourses(); ++course) {
                GRBLinExpr le = new GRBLinExpr();

                for (int semester = 1; semester <= omcsProgramDetails
                        .getNumSemesters(); ++semester) {
                    le.addTerm(1,
                            studCourseSemBooleanVars[student][course][semester]);

                }
                String name = String.format("St%d_take_Co%d_only_once",
                        student, course);

                model.addConstr(le, GRB.LESS_EQUAL, 1, name);
            }
        }
    }

    /**
     * creates the objective and returns it as a GRB Variable
     * 
     * @param model
     *            the model to add the constraints to
     * @return the GRBVar which defines the size of the largest course
     * @throws GRBException
     *             on any exception from within GRB
     */
    private GRBVar createCourseSizeLimitVariable(GRBModel model)
            throws GRBException {

        GRBVar ret = model
                .addVar(0, numStudents, 0.0, GRB.INTEGER, "ClassSize");

        model.update();
        return ret;
    }

    /**
     * creates the GRBVars which are then used to build all constraints.
     * 
     * @param model
     *            the model to add the constraints to
     * @return a GRBVar three-d array which corresponds to each student, course,
     *         semester combination
     * @throws GRBException
     *             on any exception from within GRB
     */
    private GRBVar[][][] createStudCourseSemVariables(GRBModel model)
            throws GRBException {
        GRBVar[][][] yStudCourseSem = new GRBVar[numStudents + 1][omcsProgramDetails
                .getNumCourses() + 1][omcsProgramDetails.getNumSemesters() + 1];

        String format = "St%0" + String.valueOf(numStudents).length()
                + "d_Co%0"
                + String.valueOf(omcsProgramDetails.getNumCourses()).length()
                + "d_Se%0"
                + String.valueOf(omcsProgramDetails.getNumSemesters()).length()
                + "d";

        for (int student = 1; student <= numStudents; ++student) {
            for (int course = 1; course <= omcsProgramDetails.getNumCourses(); ++course) {
                for (int semester = 1; semester <= omcsProgramDetails
                        .getNumSemesters(); ++semester) {

                    yStudCourseSem[student][course][semester] = model.addVar(0,
                            1, 0.0, GRB.BINARY,
                            String.format(format, student, course, semester));

                }
            }
        }
        model.update();
        return yStudCourseSem;
    }

    /**
     * parses the input file for the list of students and the courses they are
     * requesting to take
     * 
     * @param dataFolder
     *            the input file which contains the students' requested courses
     * @return a list of StudentDemands
     * @throws FileNotFoundException
     *             on failure to open the file
     * @throws IOException
     *             on failure to read the file
     */
    private List<StudentDemand> parseStudentDemandFile(String dataFolder)
            throws FileNotFoundException, IOException {
        final String csvSplitBy = ",";

        List<StudentDemand> studentDemands = new ArrayList<StudentDemand>();

        BufferedReader bufferedReader = new BufferedReader(new FileReader(
                dataFolder));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            try {
                String[] strings = line.split(csvSplitBy);
                int[] numbers = new int[strings.length];

                for (int i = 0; i < numbers.length; i++) {
                    numbers[i] = Integer.parseInt(strings[i]);
                }

                if (numbers[0] > numStudents) {
                    numStudents = numbers[0];
                }

                studentDemands.add(new StudentDemand(numbers[0], numbers[1],
                        numbers[2]));
            } catch (NumberFormatException nfE) {
                // a line in the file is malformed. ignore it
            }
        }
        bufferedReader.close();
        return studentDemands;
    }

    /**
     * sets the objective of the model, to minimize the size of the classes
     * 
     * @param model
     *            the model to add the constraints to
     * @param courseSemClassSizeVar
     *            the class size GRBVar
     * @throws GRBException
     *             on any exception from within GRB
     */
    private void setObjective(GRBModel model, GRBVar courseSemClassSizeVar)
            throws GRBException {
        GRBLinExpr objective = new GRBLinExpr();
        objective.addTerm(1, courseSemClassSizeVar);

        model.setObjective(objective, GRB.MINIMIZE);
    }

}
