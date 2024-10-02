import antlr.gen.output.MiniJavaLexer;
import antlr.gen.output.MiniJavaParser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import java.io.File;


public class Testing {
    private String[] fileNames;
    private final Boolean verbose;

    Testing(Boolean verbose) {
        this.verbose = verbose;
        loadFiles();
    }

    public void runTests() {
        try {
            for (String fileName : fileNames) {
                CharStream charStream = CharStreams.fromFileName("test_programs/" + fileName);
                MiniJavaLexer mjLexer = new MiniJavaLexer(charStream);
                CommonTokenStream commonTokenStream = new CommonTokenStream(mjLexer);
                MiniJavaParser miniJavaParser = new MiniJavaParser(commonTokenStream);

                ParseTree tree = miniJavaParser.goal();

                System.out.println("Ran a parse on: " + fileName);

                if(verbose)
                    System.out.println(tree.toStringTree(miniJavaParser));
            }
        } catch (Exception e) {
            System.out.println("Error loading test program from file system!");
        }
    }

    private void loadFiles() {
        File directory = new File("test_programs");

        if (directory.isDirectory()) {
            File[] files = directory.listFiles();

            if (files != null) {
                 fileNames = new String[files.length];

                for (int i = 0; i < files.length; i++) {
                    fileNames[i] = files[i].getName();
                }
            }
        }
    }
}
