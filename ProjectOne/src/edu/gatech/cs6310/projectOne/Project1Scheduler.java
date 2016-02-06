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
    private String format;
    List<StudentDemand> studentDemands;

    @Override
    public void calculateSchedule(String dataFolder) {
        GRBEnv env;
        try {
            env = new GRBEnv("mip1.log");
            env.set(GRB.IntParam.LogToConsole, 0);

            GRBModel model = new GRBModel(env);

            studentDemands = parseStudentDemandFile(dataFolder);

            GRBVar[][][] studCourseSemBooleanVars = createStudCourseSemVariables(model);
            GRBVar courseSemClassSizeVar = createCourseSizeLimitVariables(model);

            addFullLoadConstraintsToModel(model, studCourseSemBooleanVars);
            addStudendNotTakeCourseTwiceToModel(model, studCourseSemBooleanVars);
            addCourseCapacityConstraintsToModel(courseSemClassSizeVar, model,
                    studCourseSemBooleanVars);
            addCoursePrerequisiteConstraintsToModel(model,
                    studCourseSemBooleanVars);
            addStudentDemandConstraintsToModel(studentDemands, model,
                    studCourseSemBooleanVars);

            setObjective(model, courseSemClassSizeVar);

            model.optimize();

            // printStudCourseSemVariables(studCourseSemBooleanVars);

            // Display our results
            double objectiveValue = model.get(GRB.DoubleAttr.ObjVal);
            System.out.printf("X=%f", objectiveValue);

        } catch (IOException ioE) {
            ioE.printStackTrace();
        } catch (GRBException grbE) {
            grbE.printStackTrace();
        }

    }

    private void addStudendNotTakeCourseTwiceToModel(GRBModel model,
            GRBVar[][][] studCourseSemBooleanVars) throws GRBException {

        for (int student = 1; student <= numStudents; ++student) {
            for (int course = 1; course <= OMCSProgramDetails.getNumCourses(); ++course) {
                GRBLinExpr le = new GRBLinExpr();

                for (int semester = 1; semester <= OMCSProgramDetails
                        .getNumSemesters(); ++semester) {
                    le.addTerm(1,
                            studCourseSemBooleanVars[student][course][semester]);

                }
                String name = String.format(format, student, course, 0);

                model.addConstr(le, GRB.LESS_EQUAL, 1, name);
            }
        }
    }

    private void setObjective(GRBModel model, GRBVar courseSemClassSizeVar)
            throws GRBException {
        GRBLinExpr objective = new GRBLinExpr();
        objective.addTerm(1, courseSemClassSizeVar);

        model.setObjective(objective, GRB.MINIMIZE);
    }

    private void addCourseCapacityConstraintsToModel(GRBVar classSizeVar,
            GRBModel model, GRBVar[][][] yStudCourseSem) throws GRBException {

        // Objective = minimize class size
        for (int course = 1; course <= OMCSProgramDetails.getNumCourses(); ++course) {
            for (int semester = 1; semester <= OMCSProgramDetails
                    .getNumSemesters(); ++semester) {
                GRBLinExpr classSize = new GRBLinExpr();
                for (int student = 1; student <= numStudents; ++student) {
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
        for (int student = 1; student <= numStudents; ++student) {
            for (int semester = 1; semester <= OMCSProgramDetails
                    .getNumSemesters(); ++semester) {
                GRBLinExpr courseLoad = new GRBLinExpr();
                for (int course = 1; course <= OMCSProgramDetails
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

    private void addCoursePrerequisiteConstraintToModel(int prereq,
            int postreq, GRBModel model, GRBVar[][][] yStudCourseSem)
            throws GRBException {

        for (int student = 1; student <= numStudents; ++student) {
            for (int semester = 1; semester <= OMCSProgramDetails
                    .getNumSemesters(); ++semester) {
                GRBLinExpr coursePrereqLHS = new GRBLinExpr();
                for (int k = 1; k < semester; ++k) {
                    coursePrereqLHS.addTerm(2,
                            yStudCourseSem[student][prereq][k]);
                }
                String name = "Prereq_St" + student + "_C" + prereq + "_"
                        + postreq;
                model.addConstr(coursePrereqLHS, GRB.GREATER_EQUAL,
                        yStudCourseSem[student][postreq][semester], name);
            }
        }

        for (int student = 1; student <= numStudents; ++student) {
            for (StudentDemand sd : studentDemands) {
                if ((sd.getStudentID() == student)
                        && (sd.getCourseID() == postreq)) {
                    GRBLinExpr coursePrereqLHS = new GRBLinExpr();
                    GRBLinExpr coursePrereqRHS = new GRBLinExpr();

                    for (int semester = 1; semester <= OMCSProgramDetails
                            .getNumSemesters(); ++semester) {
                        coursePrereqLHS.addTerm(2 * semester,
                                yStudCourseSem[student][prereq][semester]);
                        coursePrereqRHS.addTerm(semester,
                                yStudCourseSem[student][postreq][semester]);

                    }
                    String name = "Prereq_St" + student + "_C" + prereq + "_"
                            + postreq;
                    model.addConstr(coursePrereqLHS, GRB.LESS_EQUAL,
                            coursePrereqRHS, name);
                    System.out.println("Added constr: " + name);
                }
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
        GRBVar[][][] yStudCourseSem = new GRBVar[numStudents + 1][OMCSProgramDetails
                .getNumCourses() + 1][OMCSProgramDetails.getNumSemesters() + 1];

        format = "St%0" + String.valueOf(numStudents).length() + "d_Co%0"
                + String.valueOf(OMCSProgramDetails.getNumCourses()).length()
                + "d_Se%0"
                + String.valueOf(OMCSProgramDetails.getNumSemesters()).length()
                + "d";

        for (int student = 1; student <= numStudents; ++student) {
            for (int course = 1; course <= OMCSProgramDetails.getNumCourses(); ++course) {
                for (int semester = 1; semester <= OMCSProgramDetails
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

    private void printStudCourseSemVariables(GRBVar[][][] yStudCourseSem)
            throws GRBException {

        for (int student = 1; student <= 1; ++student) {
            for (int semester = 1; semester <= OMCSProgramDetails
                    .getNumSemesters(); ++semester) {
                for (int course = 1; course <= OMCSProgramDetails
                        .getNumCourses(); ++course) {
                    if (yStudCourseSem[student][course][semester]
                            .get(GRB.DoubleAttr.X) > .5) {

                        System.out
                                .println(yStudCourseSem[student][course][semester]
                                        .get(GRB.StringAttr.VarName));
                    }
                }
            }
        }
    }

    private void addStudentDemandConstraintsToModel(
            List<StudentDemand> studentDemands, GRBModel model,
            GRBVar[][][] yStudCourseSem) throws GRBException {
        // Students must take each course that they're requesting
        for (StudentDemand sd : studentDemands) {
            int student = sd.getStudentID();
            int course = sd.getCourseID();

            GRBLinExpr studentMustTakeCourse = new GRBLinExpr();
            for (int semester = 1; semester <= OMCSProgramDetails
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
