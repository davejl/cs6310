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

    private int numStudents;

    // List<GRBConstr> constraints = new ArrayList<GRBConstr>();

    @Override
    public void calculateSchedule(String dataFolder) {
        GRBEnv env;
        try {

            env = new GRBEnv("mip1.log");
            GRBModel model = new GRBModel(env);

            List<StudentDemand> studentDemands = parseStudentDemandFile(dataFolder);

            GRBVar[][][] studCourseSemBooleanVars = createStudCourseSemVariables(model);
            GRBVar courseSemClassSizeVar = createCourseSizeLimitVariables(model);

            addCourseOfferingsConstraintsToModel(model);
            addStudentDemandConstraintsToModel(studentDemands, model,
                    studCourseSemBooleanVars);
            addFullLoadConstraintsToModel(model, studCourseSemBooleanVars);
            addCourseCapacityConstraintsToModel(model,
                    studCourseSemBooleanVars, courseSemClassSizeVar);
            addCoursePrerequisiteConstraintsToModel(model,
                    studCourseSemBooleanVars);

            // TODO: how to make this all classes?
            GRBLinExpr objective = new GRBLinExpr();
            objective.addTerm(1, courseSemClassSizeVar);
            model.setObjective(objective, GRB.MINIMIZE);
            model.optimize();

            // Display our results
            double objectiveValue = model.get(GRB.DoubleAttr.ObjVal);
            System.out.printf("Objective value = %f\n", objectiveValue);

        } catch (IOException ioE) {
            ioE.printStackTrace();
        } catch (GRBException grbE) {
            grbE.printStackTrace();
        }

    }

    private void addCourseOfferingsConstraintsToModel(GRBModel model) {
        // TODO
    }

    private void addCourseCapacityConstraintsToModel(GRBModel model,
            GRBVar[][][] yStudCourseSem, GRBVar classSizeVar)
            throws GRBException {

        // Objective = minimize class size
        for (int course = 0; course < OMCSProgramDetails.getNumCourses(); ++course) {
            for (int semester = 0; semester < OMCSProgramDetails
                    .getNumSemesters(); ++semester) {
                GRBLinExpr classSize = new GRBLinExpr();
                for (int student = 0; student < numStudents; ++student) {
                    classSize.addTerm(1,
                            yStudCourseSem[student][course][semester]);
                }
                if (OMCSProgramDetails.isCourseOffered(course, semester)) {
                    model.addConstr(classSize, GRB.LESS_EQUAL, classSizeVar,
                            "Class_Size_C" + course + "_Se" + semester);
                } else {
                    model.addConstr(classSize, GRB.LESS_EQUAL, 0,
                            "Class_Size_C" + course + "_Se" + semester);
                }

            }
        }
    }

    private void addFullLoadConstraintsToModel(GRBModel model,
            GRBVar[][][] yStudCourseSem) throws GRBException {
        for (int student = 0; student < numStudents; ++student) {
            for (int semester = 0; semester < OMCSProgramDetails
                    .getNumSemesters(); ++semester) {
                GRBLinExpr courseLoad = new GRBLinExpr();
                for (int course = 0; course < OMCSProgramDetails
                        .getNumCourses(); ++course) {
                    courseLoad.addTerm(1,
                            yStudCourseSem[student][course][semester]);
                }
                model.addConstr(courseLoad, GRB.LESS_EQUAL,
                        OMCSProgramDetails.getFullLoad(), "CourseLoad_St"
                                + student + "_Se" + semester);
            }
        }
    }

    private void addCoursePrerequisiteConstraintsToModel(GRBModel model,
            GRBVar[][][] yStudCourseSem) throws GRBException {
        addCoursePrerequisiteConstraintToModel(4, 16, model, yStudCourseSem);
        addCoursePrerequisiteConstraintToModel(12, 1, model, yStudCourseSem);
        addCoursePrerequisiteConstraintToModel(9, 13, model, yStudCourseSem);
        addCoursePrerequisiteConstraintToModel(3, 7, model, yStudCourseSem);
    }

    private void addCoursePrerequisiteConstraintToModel(int j0, int j1,
            GRBModel model, GRBVar[][][] yStudCourseSem) throws GRBException {
        for (int student = 0; student < numStudents; ++student) {
            for (int k1 = 1; k1 < OMCSProgramDetails.getNumSemesters() - 1; ++k1) {
                GRBLinExpr coursePrereqLHS = new GRBLinExpr();
                GRBLinExpr coursePrereqRHS = new GRBLinExpr();
                for (int semester = 0; semester <= k1 - 1; ++semester) {
                    coursePrereqLHS.addTerm(1,
                            yStudCourseSem[student][j1 - 1][semester + 1]);
                    coursePrereqRHS.addTerm(1,
                            yStudCourseSem[student][j0 - 1][semester]);
                }
                model.addConstr(coursePrereqLHS, GRB.LESS_EQUAL,
                        coursePrereqRHS, "Prereq_St" + student + "_C" + j0
                                + "_" + j1);
            }
        }
    }

    private GRBVar createCourseSizeLimitVariables(GRBModel model)
            throws GRBException {
        /*
         * GRBVar[][] yCourseSem = new GRBVar[numCourses][numSemesters];
         * 
         * for (int course = 0; course < numCourses; ++course) { for (int
         * semester = 0; semester < numSemesters; ++semester) {
         * yCourseSem[course][semester] = model.addVar(0, numStudents, 0.0,
         * GRB.INTEGER, "C" + course + "_Se" + semester + "_ClassSize"); } }
         */

        GRBVar ret = model
                .addVar(0, numStudents, 0.0, GRB.INTEGER, "ClassSize");

        model.update();
        return ret;
    }

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

            }
        }
        bufferedReader.close();
        return studentDemands;
    }

    private GRBVar[][][] createStudCourseSemVariables(GRBModel model)
            throws GRBException {
        GRBVar[][][] yStudCourseSem = new GRBVar[numStudents][OMCSProgramDetails
                .getNumCourses()][OMCSProgramDetails.getNumSemesters()];

        for (int student = 0; student < numStudents; ++student) {
            for (int course = 0; course < OMCSProgramDetails.getNumCourses(); ++course) {
                for (int semester = 0; semester < OMCSProgramDetails
                        .getNumSemesters(); ++semester) {
                    yStudCourseSem[student][course][semester] = model.addVar(0,
                            1, 0.0, GRB.BINARY, "St" + student + "_C" + course
                                    + "_Se" + semester);
                }
            }
        }
        model.update();
        return yStudCourseSem;
    }

    private void addStudentDemandConstraintsToModel(
            List<StudentDemand> studentDemands, GRBModel model,
            GRBVar[][][] yStudCourseSem) throws GRBException {
        // Students must take each course that they're requesting
        for (StudentDemand sd : studentDemands) {
            int student = sd.getStudentID() - 1;
            int course = sd.getCourseID() - 1;

            GRBLinExpr studentMustTakeCourse = new GRBLinExpr();
            for (int semester = 0; semester < OMCSProgramDetails
                    .getNumSemesters(); ++semester) {
                studentMustTakeCourse.addTerm(1,
                        yStudCourseSem[student][course][semester]);
            }

            String name = "St" + student + "_must_take_C" + course;
            model.addConstr(studentMustTakeCourse, GRB.EQUAL, 1, name);
        }
    }

    @Override
    public double getObjectiveValue() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Vector<String> getCoursesForStudentSemester(String student,
            String semester) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Vector<String> getStudentsForCourseSemester(String course,
            String semester) {
        // TODO Auto-generated method stub
        return null;
    }

}
