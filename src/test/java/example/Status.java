public class Status {
    double id;
    String name;

    Status(String name) {
        Student student = new Student();
        this.id = student.id;
        this.name = name;
    }

    public static String findResult(String name) {
        return "Result is: " + encode(name);
    }

    public static String calculate(String name) {
        Status s = new Status(name);
        return String.valueOf("Out: " + s.id + s.name);
    }

    public static String encode(String name) {
        return String.valueOf(calculate(name).hashCode());
    }

    public void printName() {
        System.out.println(name);
        encode(name);
    }

    public static void main(String args[]) {
        System.out.println(findResult(args[0]));
    }
}