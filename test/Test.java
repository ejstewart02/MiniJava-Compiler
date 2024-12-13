import java.io.*;
import java.util.stream.*;

public class Test {
    private static final String SOURCE_DIR = "test_programs/minijava_test_programs";
    private static final String MINIJAVA_OUTPUT = "minijava_test_output";
    private static final String JAVA_OUTPUT = "java_test_output";
    private static final String MINIJAVA_COMPILER = "out/artifacts/CSC_444_PROJECT_jar/CSC-444-PROJECT.jar";
    private static final String JAVA_COMPILER = "javac";

    public static final String BLUE = "\u001B[34m";
    public static final String GREEN = "\u001B[32m";
    public static final String CYAN = "\u001B[36m";
    public static final String RED = "\u001B[31m";
    public static final String WHITE = "\u001B[0m";

    public static void main(String[] args) throws IOException, InterruptedException {
        File sourceDir = new File(SOURCE_DIR);
        File[] javaFiles = sourceDir.listFiles((dir, name) -> name.endsWith(".java"));

        if (javaFiles != null) {
            for (File javaFile : javaFiles) {
                String programName = javaFile.getName().replace(".java", "");
                
                System.out.println(WHITE + "Compiling: " + programName);
                
                compileWithJava(javaFile);
                compileWithMiniJava(javaFile);
                String javaOutput = runJavaProgram(programName);
                String minijavaOutput = runMiniJavaProgram(programName);

                // Compare the outputs
                if (javaOutput.equals(minijavaOutput) && !javaOutput.isEmpty()) {
                    System.out.println(BLUE + javaOutput);
                    System.out.println(CYAN + minijavaOutput);
                    System.out.println("\n" + GREEN + programName + ": Compiled Successfully");
                } else {
                    System.out.println("\n" + RED + programName + ": Compiled Unsuccessfully\n");
                }
            }
        }
    }
    
    private static void compileWithJava(File javaFile) throws IOException, InterruptedException {
        ProcessBuilder javacProcess = new ProcessBuilder(JAVA_COMPILER, "-d", JAVA_OUTPUT, javaFile.getPath());
        Process process = javacProcess.start();
        process.waitFor();
    }
    
    private static void compileWithMiniJava(File javaFile) throws IOException, InterruptedException {
        ProcessBuilder customCompilerProcess = new ProcessBuilder("java", "-jar", MINIJAVA_COMPILER, javaFile.getPath(), MINIJAVA_OUTPUT);
        Process process = customCompilerProcess.start();
        process.waitFor();
    }
    
    private static String runJavaProgram(String fileName) throws IOException, InterruptedException {
        String output;

        ProcessBuilder runMiniJava = new ProcessBuilder("java", fileName);
        runMiniJava.directory(new File(JAVA_OUTPUT));
        Process process = runMiniJava.start();

        process.waitFor();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        output = reader.lines().collect(Collectors.joining("\n"));



        return output;


    }

    private static String runMiniJavaProgram(String fileName) throws IOException, InterruptedException {
        String output;

        ProcessBuilder runMiniJava = new ProcessBuilder("java", fileName);
        runMiniJava.directory(new File(MINIJAVA_OUTPUT));
        Process process = runMiniJava.start();

        process.waitFor();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        output = reader.lines().collect(Collectors.joining("\n"));

        return output;
    }
}