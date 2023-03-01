public class Token {
    private final Course course;
    private final Integer tokenCasted;
    private final Student student;
    public Token(Course course, Integer tokenCasted, Student student) {
        this.course = course;
        this.tokenCasted = tokenCasted;
        this.student = student;
    }

    public Integer getTokenCasted() {
        return tokenCasted;
    }

    public Course getCourse() {
        return course;
    }

    public Student getStudent() { return student; }

    public Double calculateUnhappy(double h) {
        Double u = (-100.0/h)*Math.log(1.0 - (Double.valueOf(tokenCasted)/100.0));
        return (u > 100.0) ? 100.0 : u;
    }
}
