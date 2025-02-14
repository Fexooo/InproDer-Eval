public class Student {
    double id;
    String name;

    Student() {
        this.id = studentread();
        this.name = "John Doe";
    }

    public double studentread() {
        return 1.0;
    }
}