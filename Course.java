import java.util.ArrayList;
import java.util.List;

public class Course {
    private final String name;
    private final Integer capacity;
    private List<Student> studentList;
    private Integer minToken;

    public Course(String name, Integer capacity) {
        this.name = name;
        this.capacity = capacity;
        this.studentList =  new ArrayList<>();
        this.minToken = 100;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public String getName() {
        return name;
    }

    public void registerStudent(Student student, Integer token) {
        studentList.add(student);
        minToken = token;
    }

    public List<Student> getStudentList() {
        return studentList;
    }

    public void ClearStudents() {
        minToken = 100;
        studentList.clear();
    }

    public Integer getMinToken() {
        return minToken;
    }

}
