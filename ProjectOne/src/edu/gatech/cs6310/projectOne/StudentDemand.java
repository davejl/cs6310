package edu.gatech.cs6310.projectOne;

public class StudentDemand {

	private final int studentID;
	private final int courseID;
	private final int semesterID;

	public StudentDemand(int studentID, int courseID, int semesterID) {
		this.studentID = studentID;
		this.courseID = courseID;
		this.semesterID = semesterID;
	}

	public int getStudentID() {
		return studentID;
	}

	public int getCourseID() {
		return courseID;
	}

	public int getSemesterID() {
		return semesterID;
	}

}
