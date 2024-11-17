
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        //Run Good Programs
        //Testing testGoodFiles = new Testing(false, "test_programs/error_free");
        //testGoodFiles.runTests();

        //Run Bad Programs
//        Testing testBadFiles = new Testing(false, "test_programs/error_prone");
//        testBadFiles.runTests();
        Testing testTypeChecking = new Testing(false, "test_programs/error_free");
        testTypeChecking.runTests();
    }
}

