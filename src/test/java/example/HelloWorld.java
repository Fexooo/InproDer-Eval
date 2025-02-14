class HelloWorld {
    public HelloWorld() {

    }

    public static void main(String[] args) {
        System.out.println("Hello, World!");

        HelloWorld helloWorld = new HelloWorld();
        String name = "John";
        if(name == "John") {
            System.out.println("Hello John");
        }
        String email = helloWorld.getEmailFromName(name);
        String testEmail = email + ".com";
        helloWorld.outputEmail(email);
        String musterName = name + " Mustermann";
        helloWorld.sendMail(email);
    }

    public String getEmailFromName(String name) {
        String email = name + "@gmail.com";
        String returnMail = email + "\n";
        String test = name + "2";
        try {
            test(test);
        } catch (Exception e) {
            System.out.println("Error");
        }
        return returnMail;
    }

    public String test(String email) {
        return "test " + email;
    }

    public void sendMail(String email) {
        String title = "Hello!";
        String targetEmail = email;
        String message = "Hello user with email " + targetEmail;
        System.out.println(title + "\n" + "Sent to: " + targetEmail + "\n" + message);
    }

    public void outputEmail(String email) {
        System.out.println(email);
    }
}