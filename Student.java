import java.util.ArrayList;

public class Student {
    private final String studentId;
    private Double unhappiness;
    private ArrayList<Course> courseArrayList;

    public Student(String studentId) {
        this.studentId = studentId;
        this.unhappiness = Double.valueOf(0);
        this.courseArrayList = new ArrayList<>();
    }

    public String getStudentId() {
        return studentId;
    }

    public Double getUnhappiness() {

        return unhappiness;
    }

    public void addUnhappy(double u) {
        unhappiness += u;
    }

    public void squareUnhappy() {
        unhappiness = unhappiness*unhappiness;
    }

    public void Clear() {
        unhappiness = Double.valueOf(0);
        courseArrayList.clear();
    }

    public void registerCourse(Course course) {
        courseArrayList.add(course);
    }
    public ArrayList<Course> getCourseArrayList() {
        return courseArrayList;
    }
}
