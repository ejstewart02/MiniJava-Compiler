
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        Testing testTypeChecking = new Testing(false, "test_programs/type_error_programs");
        testTypeChecking.runTests();
    }
}

