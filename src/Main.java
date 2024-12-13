import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("Usage: java Main <filename>");
            System.exit(1);
        }

        parseAndCheckFile(args[0], args[1]);
    }

    public static void parseAndCheckFile(String inputFile, String outputFile) throws IOException {
        System.err.flush();
        System.out.flush();
        //System.setErr(System.out);
        System.setOut(System.err);

        CharStream charStream = CharStreams.fromFileName(inputFile);
        MiniJavaLexer mjLexer = new MiniJavaLexer(charStream);
        CommonTokenStream commonTokenStream = new CommonTokenStream(mjLexer);
        MiniJavaParser miniJavaParser = new MiniJavaParser(commonTokenStream);

        //Add our error handling
        miniJavaParser.removeErrorListeners();
        miniJavaParser.addErrorListener(new UnderlineListener());

        ParseTree tree = miniJavaParser.goal();

        ParseTreeWalker walker = new ParseTreeWalker();

        SymbolDefinitionListener defListener = new SymbolDefinitionListener();
        walker.walk(defListener, tree);

        InheritanceListener inListener = new InheritanceListener(defListener.globals, defListener.scopes);
        walker.walk(inListener, tree);

        InitializationListener initListener = new InitializationListener(inListener.globals, inListener.scopes);
        walker.walk(initListener, tree);

        SymbolResolutionListener resolutionListener = new SymbolResolutionListener(initListener.globals, initListener.scopes);
        walker.walk(resolutionListener, tree);

        StaticTypeCheckingListener typeChecker = new StaticTypeCheckingListener(resolutionListener.scopes, resolutionListener.globals);
        walker.walk(typeChecker, tree);

        ByteCodeGen codeGen = new ByteCodeGen(typeChecker.scopes, typeChecker.expressionTypes, typeChecker.globals, outputFile);
        walker.walk(codeGen, tree);
    }

}

