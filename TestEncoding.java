public class TestEncoding {
    public static void main(String[] args) {
        System.out.println("Default charset: " + System.getProperty("file.encoding"));
        System.out.println("Console charset: " + System.getProperty("console.encoding"));
        System.out.println("Russian text: Привет мир!");
        System.out.println("English text: Hello world!");
    }
}
