import antlr.gen.output.MiniJavaLexer;
import antlr.gen.output.MiniJavaParser;
import org.antlr.v4.gui.TreeViewer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;


public class Testing {
    private String fileLoc;
    private String[] fileNames;
    private final Boolean displayTrees;
    private ArrayList<TreeViewer> visualTrees = new ArrayList<>();

    Testing(Boolean displayTrees, String fileLoc) {
        this.displayTrees = displayTrees;
        this.fileLoc = fileLoc;
        loadFiles();
    }

    public void runTests() {
        try {
            for (String fileName : fileNames) {
                CharStream charStream = CharStreams.fromFileName(fileLoc + "/" + fileName);
                MiniJavaLexer mjLexer = new MiniJavaLexer(charStream);
                CommonTokenStream commonTokenStream = new CommonTokenStream(mjLexer);
                MiniJavaParser miniJavaParser = new MiniJavaParser(commonTokenStream);

                //Add our error handling
                miniJavaParser.removeErrorListeners();
                miniJavaParser.addErrorListener(new UnderlineListener());

                ParseTree tree = miniJavaParser.goal();

                System.out.println("Ran a parse on: " + fileName);

                if (displayTrees) {
                    TreeViewer viewer = new TreeViewer(Arrays.asList(
                            miniJavaParser.getRuleNames()), tree);
                    viewer.setName(fileName);
                    visualTrees.add(viewer);
                }
            }
        } catch (Exception e) {
            System.out.println("Error loading test program from file system!");
        }

        if(displayTrees)
            displayTrees();
    }

    private void loadFiles() {
        File directory = new File(fileLoc);

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

    private void displayTrees() {
        JFrame frame = new JFrame("Parse Trees at:" + fileLoc);
        JTabbedPane tabbedPane = new JTabbedPane();

        for (TreeViewer viewer : visualTrees) {
            viewer.setScale(1);

            viewer.addMouseWheelListener(e -> {
                if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
                    double scale = viewer.getScale();
                    int scrollVector = e.getWheelRotation();
                    if (scrollVector < 0) {
                        scale *= 1.1;
                    } else {
                        scale /= 1.1;
                    }
                    viewer.setScale(scale);
                    viewer.repaint();
                }
            });

            JPanel panel = new JPanel(new BorderLayout());
            JScrollPane scrollPane = new JScrollPane(viewer);

            panel.add(scrollPane, BorderLayout.CENTER);
            tabbedPane.add(viewer.getName(), panel);
        }

        frame.add(tabbedPane);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setVisible(true);
    }
}


