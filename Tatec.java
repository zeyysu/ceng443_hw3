import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class Tatec
{
    private static final int CORRECT_TOTAL_TOKEN_PER_STUDENT = 100;
    private static final String OUT_TATEC_UNHAPPY = "unhappyOutTATEC.txt";
    private static final String OUT_TATEC_ADMISSION = "admissionOutTATEC.txt";
    private static final String OUT_RAND_UNHAPPY = "unhappyOutRANDOM.txt";
    private static final String OUT_RAND_ADMISSION = "admissionOutRANDOM.txt";

    public static void main(String args[]) {
        if (args.length < 4) {
            System.err.println("Not enough arguments!");
            return;
        }

        // File Paths
        String courseFilePath = args[0];
        String studentIdFilePath = args[1];
        String tokenFilePath = args[2];
        double h;

        try {
            h = Double.parseDouble(args[3]);
        } catch (NumberFormatException ex) {
            System.err.println("4th argument is not a double!");
            return;
        }

        // TODO: Rest is up to you

        //get readers
        BufferedReader course_reader;
        BufferedReader student_reader;
        BufferedReader token_reader;
        try {
            course_reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(courseFilePath))));
            student_reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(studentIdFilePath))));
            token_reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(tokenFilePath))));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        //get list of courses
        List<Course> courses = course_reader.lines().map((line) -> {
            String[] p = line.split(",");
            return new Course(p[0], Integer.parseInt(p[1]));
        }).collect(Collectors.toList());

        try {
            course_reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //get list of students
        List<Student> students = student_reader.lines()
                .map((line) -> new Student(line)).collect(Collectors.toList());

        try {
            student_reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Iterator<Student> studentIterator = students.stream().iterator();

        //get list of tokens
        List<Token> tokenList = token_reader.lines().map((line) -> {
            List<Integer> tokens = Arrays.stream(line.split(","))
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
            Student student = studentIterator.next();
            Iterator<Course> courseIterator = courses.stream().iterator();
            return tokens.stream().map((tc) ->
                new Token(courseIterator.next(), tc, student)
            ).collect(Collectors.toList());
        }).reduce(new ArrayList<>(), (list1, list2) -> {list1.addAll(list2); return list1;});

        try {
            token_reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //check if there are any student whose total token is not correct
        if(tokenList.stream()
                .parallel()
                .collect(Collectors.groupingBy(Token::getStudent, Collectors.summingInt(Token::getTokenCasted)))
                .entrySet()
                .stream()
                .filter((m) -> m.getValue() != CORRECT_TOTAL_TOKEN_PER_STUDENT)
                .findAny()
                .orElse(null) != null) {
                    System.out.println("Token sum is not equal to " + CORRECT_TOTAL_TOKEN_PER_STUDENT);
                    return;
        }

        //assign according to tokens
        courses.stream().parallel().forEach((course) -> {
            tokenList.stream()
                    .filter((token) -> token.getCourse() == course && token.getTokenCasted() > 0)
                    .sorted((t1,t2) -> t2.getTokenCasted().compareTo(t1.getTokenCasted()))
                    .limit(course.getCapacity())
                    .forEach((token) -> {
                        course.registerStudent(token.getStudent(), token.getTokenCasted());
                        token.getStudent().registerCourse(course);
                    });
            tokenList.stream()
                    .filter((token) -> token.getCourse() == course && token.getTokenCasted() > 0)
                    .sorted((t1,t2) -> t2.getTokenCasted().compareTo(t1.getTokenCasted()))
                    .skip(course.getCapacity())
                    .forEach((token) -> {
                        if(token.getTokenCasted() == token.getCourse().getMinToken()) {
                            course.registerStudent(token.getStudent(), token.getTokenCasted());
                            token.getStudent().registerCourse(course);
                        }
                        else token.getStudent().addUnhappy(token.calculateUnhappy(h));
                    });
        });

        //square unhappy if no wanted course is taken
        students.stream().parallel()
                .filter((student) ->
                    student.getCourseArrayList().isEmpty()
                )
                .forEach(Student::squareUnhappy);

        //calculate average unhappiness
        Double averageUnhappiness = students.stream()
                .mapToDouble((student) -> student.getUnhappiness()).average().orElse(0.0);

        //write to tatec admission file
        try {
            BufferedWriter tatecOutWriter = new BufferedWriter(new FileWriter(OUT_TATEC_ADMISSION));
            courses.stream()
                    .forEach((course) -> {
                        try {
                            tatecOutWriter.write(course.getName());
                            course.getStudentList().stream()
                                    .forEach((student) -> {
                                        try {
                                            tatecOutWriter.write("," + student.getStudentId());
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    });
                            tatecOutWriter.newLine();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
            tatecOutWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        //write to tatec unhappy file
        try {
            BufferedWriter tatecUnHappyWriter = new BufferedWriter(new FileWriter(OUT_TATEC_UNHAPPY));
            tatecUnHappyWriter.write(averageUnhappiness.toString());
            tatecUnHappyWriter.newLine();
            students.stream()
                    .forEach((student) -> {
                        try {
                            tatecUnHappyWriter.write(student.getUnhappiness().toString());
                            tatecUnHappyWriter.newLine();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
            tatecUnHappyWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        //clear for Random
        courses.stream().parallel().forEach(Course::ClearStudents);
        students.stream().parallel().forEach(Student::Clear);

        //Random assignment
        courses.stream().parallel().forEach((course) -> {
            Random rand = new Random();
            List<Student> studentList = tokenList.stream()
                    .filter((token) -> token.getTokenCasted() > 0 && token.getCourse() == course)
                    .map(Token::getStudent).collect(Collectors.toList());
            while(course.getStudentList().size() < course.getCapacity() && !studentList.isEmpty()){
                /*Integer index = rand.nextInt(students.size());
                Student student = students.get(index);
                if(!course.getStudentList().contains(student)){
                    Integer count = tokenList.stream()
                            .filter((token) -> token.getTokenCasted() > 0 && token.getStudent() == student)
                            .collect(Collectors.toList()).size();
                    if(student.getCourseArrayList().size() < count){
                        course.registerStudent(student, 100);
                        student.registerCourse(course);
                    }
                }
                i++;
                 */
                Integer index = rand.nextInt(studentList.size());
                Student student = studentList.get(index);
                if(!course.getStudentList().contains(student)){
                        course.registerStudent(student, 100);
                        student.registerCourse(course);
                        studentList.remove(student);
                }
            }
        });

        //calculate unhappiness random
        tokenList.stream().forEach((token) -> {
            if(token.getTokenCasted() >0 && !token.getStudent().getCourseArrayList().contains(token.getCourse()))
                token.getStudent().addUnhappy(token.calculateUnhappy(h));
        });

        //square unhappiness
        students.stream().parallel()
                .filter((student) ->
                    tokenList.stream()
                            .filter((token) -> token.getTokenCasted() > 0
                                    && token.getStudent() == student
                                    && token.getCourse().getStudentList().contains(student))
                            .collect(Collectors.toList()).isEmpty()
                ).forEach(Student::squareUnhappy);

        //find average unhappiness on random
        averageUnhappiness = students.stream()
                .mapToDouble((student) -> student.getUnhappiness()).average().orElse(0.0);



        //write random admission file
        try {
            BufferedWriter RandomOutWriter = new BufferedWriter(new FileWriter(OUT_RAND_ADMISSION));
            courses.stream()
                    .forEach((course) -> {
                        try {
                            RandomOutWriter.write(course.getName());
                            course.getStudentList().stream()
                                    .forEach((student) -> {
                                        try {
                                            RandomOutWriter.write("," + student.getStudentId());
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    });
                            RandomOutWriter.newLine();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
            RandomOutWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        //write random unhappy file
        try {
            BufferedWriter randomUnHappyWriter = new BufferedWriter(new FileWriter(OUT_RAND_UNHAPPY));
            randomUnHappyWriter.write(averageUnhappiness.toString());
            randomUnHappyWriter.newLine();
            students.stream()
                    .forEach((student) -> {
                        try {
                            randomUnHappyWriter.write(student.getUnhappiness().toString());
                            randomUnHappyWriter.newLine();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
            randomUnHappyWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}
