import antlr.gen.output.MiniJavaBaseListener;
import antlr.gen.output.MiniJavaParser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;
import symbols.*;

import java.io.*;
import java.util.Objects;
import java.util.Stack;

public class ByteCodeGen extends MiniJavaBaseListener implements Opcodes {
    private ClassWriter classWriter;
    private GeneratorAdapter methodGenerator;
    private FileOutputStream fileOutput;
    private final ParseTreeProperty<Scope> scopes;
    private final ParseTreeProperty<String> expressionTypes;
    private final GlobalScope globals;
    private Scope currentScope;
    private int currentId;
    private Stack<Label> labelStack = new Stack<Label>();

    public ByteCodeGen(ParseTreeProperty<Scope> scopes, ParseTreeProperty<String> expressionTypes, GlobalScope globals) {
        this.scopes = scopes;
        this.expressionTypes = expressionTypes;
        this.globals = globals;
    }

    @Override
    public void enterGoal(MiniJavaParser.GoalContext ctx) {
        super.enterGoal(ctx);
    }

    @Override
    public void exitGoal(MiniJavaParser.GoalContext ctx) {
        super.exitGoal(ctx);
    }

    @Override
    public void enterMainClass(MiniJavaParser.MainClassContext ctx) {
        String mainClassName = ctx.identifier(0).getText();

        classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        classWriter.visit(V1_1, ACC_PUBLIC, mainClassName, null, "java/lang/Object", null);

        methodGenerator = new GeneratorAdapter(ACC_PUBLIC, Method.getMethod("void <init> ()"),
                null, null, classWriter);

        methodGenerator.loadThis();
        methodGenerator.invokeConstructor(Type.getType(Object.class), Method.getMethod("void <init> ()"));
        methodGenerator.returnValue();
        methodGenerator.endMethod();

        methodGenerator = new GeneratorAdapter(ACC_PUBLIC + ACC_STATIC,
                org.objectweb.asm.commons.Method.getMethod("void main (String[])"),
                null, null, classWriter);

    }

    @Override
    public void exitMainClass(MiniJavaParser.MainClassContext ctx) {
        methodGenerator.returnValue();
        methodGenerator.endMethod();
        classWriter.visitEnd();

        String mainClassName = ctx.identifier(0).getText();

        try{
            fileOutput = new FileOutputStream("test_output/" + mainClassName + ".class");
            fileOutput.write(classWriter.toByteArray());
            fileOutput.close();
        }catch(FileNotFoundException fnfe){
            fnfe.printStackTrace();
            System.exit(1);
        }catch(IOException ioe){
            ioe.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void enterClassDeclaration(MiniJavaParser.ClassDeclarationContext ctx) {
        currentScope = scopes.get(ctx);

        ClassSymbol classSymbol = (ClassSymbol) globals.resolve(ctx.identifier(0).getText());

        String className = classSymbol.getScopeName();
        String superClassName;
        if(classSymbol.getParent() != null && !(classSymbol.getParent() instanceof GlobalScope)) {
            superClassName = classSymbol.getParent().getScopeName();
        } else {
            superClassName = "java/lang/Object";
        }

        classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        classWriter.visit(V1_1, ACC_PUBLIC, className, null, superClassName, null);

        methodGenerator = new GeneratorAdapter(ACC_PUBLIC, Method.getMethod("void <init> ()"),
                null, null, classWriter);
        methodGenerator.loadThis();
        methodGenerator.invokeConstructor(Type.getObjectType(superClassName),  Method.getMethod("void <init> ()"));
        methodGenerator.returnValue();
        methodGenerator.endMethod();
    }

    @Override
    public void exitClassDeclaration(MiniJavaParser.ClassDeclarationContext ctx) {
        classWriter.visitEnd();
        String className = ctx.identifier(0).getText();

        try{
            fileOutput = new FileOutputStream("test_output/" + className + ".class");
            fileOutput.write(classWriter.toByteArray());
            fileOutput.close();
        }catch(FileNotFoundException fnfe){
            fnfe.printStackTrace();
            System.exit(1);
        }catch(IOException ioe){
            ioe.printStackTrace();
            System.exit(1);
        }

        currentScope = currentScope.getEnclosingScope();
    }

    @Override
    public void exitVarDeclaration(MiniJavaParser.VarDeclarationContext ctx) {
        String varName = ctx.identifier().getText();
        Symbol varSymbol = currentScope.resolve(varName);
        Type type = ((VariableSymbol) varSymbol).getByteCodeType();

        if(currentScope instanceof ClassSymbol) {
            classWriter.visitField(ACC_PROTECTED, varName, type.getDescriptor(), null, null).visitEnd();
        } else if (currentScope instanceof LocalScope){
            varSymbol.setNumericalId(methodGenerator.newLocal(type)); //TODO: come back to this
        }
    }

    @Override
    public void enterMethodDeclaration(MiniJavaParser.MethodDeclarationContext ctx) {
        currentScope = scopes.get(ctx);

        MethodSymbol methodSymbol = (MethodSymbol) currentScope.getEnclosingScope();

        currentId = 0;

        //Handle method params
        for (int i = 1; i < ctx.identifier().size(); i++) {
            String paramName = ctx.identifier(i).getText();

            if(methodSymbol.resolveLocal(paramName) != null) {
                methodSymbol.resolveLocal(paramName).setNumericalId(currentId);
                currentId++;
            }

        }

        methodGenerator = new GeneratorAdapter(ACC_PUBLIC, methodSymbol.getByteCodeMethod(), null, null, classWriter);
    }

    @Override
    public void exitMethodDeclaration(MiniJavaParser.MethodDeclarationContext ctx) {
        methodGenerator.returnValue();
        methodGenerator.endMethod();

        currentScope = currentScope.getEnclosingScope();
    }

    @Override
    public void enterNestedStatement(MiniJavaParser.NestedStatementContext ctx) {
        super.enterNestedStatement(ctx);
    }

    @Override
    public void exitNestedStatement(MiniJavaParser.NestedStatementContext ctx) {
        super.exitNestedStatement(ctx);
    }

    @Override
    public void enterIfElseStatement(MiniJavaParser.IfElseStatementContext ctx) {
//        Label enterElse = methodGenerator.newLabel();
//        Label exitElse  = methodGenerator.newLabel();
//        labelStack.push(exitElse);
//        labelStack.push(enterElse);
//        labelStack.push(exitElse);
//        labelStack.push(enterElse);
//
//        methodGenerator.ifZCmp(GeneratorAdapter.EQ, labelStack.pop());
//        methodGenerator.mark(labelStack.pop());
    }

    @Override
    public void exitIfElseStatement(MiniJavaParser.IfElseStatementContext ctx) {
//        methodGenerator.goTo(labelStack.pop());
//        methodGenerator.mark(labelStack.pop());
    }

    @Override
    public void enterWhileStatement(MiniJavaParser.WhileStatementContext ctx) {
        super.enterWhileStatement(ctx);
    }

    @Override
    public void exitWhileStatement(MiniJavaParser.WhileStatementContext ctx) {
        super.exitWhileStatement(ctx);
    }

    @Override
    public void enterPrintStatement(MiniJavaParser.PrintStatementContext ctx) {
        methodGenerator.getStatic(Type.getType(System.class), "out", Type.getType(PrintStream.class));
    }

    @Override
    public void exitPrintStatement(MiniJavaParser.PrintStatementContext ctx) {
        String exprType = expressionTypes.get(ctx.expression());
        if(exprType.equals("int"))
            methodGenerator.invokeVirtual(Type.getType(PrintStream.class), org.objectweb.asm.commons.Method.getMethod("void println (int)"));
        else if(exprType.equals("float"))
            methodGenerator.invokeVirtual(Type.getType(PrintStream.class), org.objectweb.asm.commons.Method.getMethod("void println (float)"));
    }

    @Override
    public void enterVarAssignStatement(MiniJavaParser.VarAssignStatementContext ctx) {
        String varName = ctx.identifier().getText();

        VariableSymbol var = (VariableSymbol) currentScope.resolve(varName);
        if(var.getScope() instanceof ClassSymbol)
            methodGenerator.loadThis();
    }

    @Override
    public void exitVarAssignStatement(MiniJavaParser.VarAssignStatementContext ctx) {
        String varName = ctx.identifier().getText();

        VariableSymbol var = (VariableSymbol) currentScope.resolve(varName);

        Type type = var.getByteCodeType();

        if(var.getScope() instanceof ClassSymbol){
            Type encScopeByteCode =  ((ClassSymbol) var.getScope()).getByteCodeType();   //((ClassSymbol)currentScope.getEnclosingScope()).getByteCodeType();
            methodGenerator.putField(encScopeByteCode, var.getName(), type);
        }else if(var.getScope() instanceof MethodSymbol){
            methodGenerator.storeArg(var.getNumericalId());
        }else{
            methodGenerator.storeLocal(var.getNumericalId(), type);
        }
    }

    @Override
    public void enterArrayAssignStatement(MiniJavaParser.ArrayAssignStatementContext ctx) {
        String varName = ctx.identifier().getText();

        VariableSymbol var = (VariableSymbol) currentScope.resolve(varName);

        Type type = var.getByteCodeType();

        if(currentScope instanceof ClassSymbol){
            Type encScopeByteCode = ((ClassSymbol)currentScope.getEnclosingScope()).getByteCodeType();
            methodGenerator.loadThis();
            methodGenerator.getField(encScopeByteCode, var.getName(), type);
        }else if(currentScope instanceof MethodSymbol){
            methodGenerator.loadArg(var.getNumericalId());
        }else{
            methodGenerator.loadLocal(var.getNumericalId(), type);
        }
    }

    @Override
    public void exitArrayAssignStatement(MiniJavaParser.ArrayAssignStatementContext ctx) {
        String varName = ctx.identifier().getText();

        VariableSymbol var = (VariableSymbol) currentScope.resolve(varName);
        if(Objects.equals(var.getType(), "int[]"))
            methodGenerator.arrayStore(Type.INT_TYPE);
        else if(Objects.equals(var.getType(), "float[]"))
            methodGenerator.arrayStore(Type.FLOAT_TYPE);
    }

    @Override
    public void enterIdentifierExpression(MiniJavaParser.IdentifierExpressionContext ctx) {
        super.enterIdentifierExpression(ctx);
    }

    @Override
    public void exitIdentifierExpression(MiniJavaParser.IdentifierExpressionContext ctx) {
        String varName = ctx.identifier().getText();

        VariableSymbol var = (VariableSymbol) currentScope.resolve(varName);

        Type type = var.getByteCodeType();

        if(var.getScope() instanceof ClassSymbol){
            Type encScopeByteCode = ((ClassSymbol) var.getScope()).getByteCodeType(); //((ClassSymbol)currentScope.getEnclosingScope()).getByteCodeType();
            methodGenerator.loadThis();
            methodGenerator.getField(encScopeByteCode, var.getName(), type);
        }else if(var.getScope() instanceof MethodSymbol){
            methodGenerator.loadArg(var.getNumericalId());
        }else{
            methodGenerator.loadLocal(var.getNumericalId(), type);
        }
    }

    @Override
    public void enterIntLiteralExpression(MiniJavaParser.IntLiteralExpressionContext ctx) {
        methodGenerator.push(Integer.parseInt(ctx.getText()));
    }

    @Override
    public void exitIntLiteralExpression(MiniJavaParser.IntLiteralExpressionContext ctx) {
        super.exitIntLiteralExpression(ctx);
    }

    @Override
    public void enterMethodCallExpression(MiniJavaParser.MethodCallExpressionContext ctx) {
        super.enterMethodCallExpression(ctx);
    }

    @Override
    public void exitMethodCallExpression(MiniJavaParser.MethodCallExpressionContext ctx) {
        //String classType = expressionTypes.get(ctx);
        String classType = expressionTypes.get(ctx.expression(0));
        ClassSymbol classSymbol = (ClassSymbol) globals.resolve(classType);
        String methodName = ctx.identifier().getText();
        Method methodAsm = ((MethodSymbol) classSymbol.resolve(methodName)).getByteCodeMethod();

        methodGenerator.invokeVirtual(classSymbol.getByteCodeType(), methodAsm);
    }

    @Override
    public void exitNotExpression(MiniJavaParser.NotExpressionContext ctx) {
        methodGenerator.not();
    }

    @Override
    public void enterNewClassExpression(MiniJavaParser.NewClassExpressionContext ctx) {
        Type type = Type.getObjectType(ctx.identifier().getText());
        methodGenerator.newInstance(type);
        methodGenerator.dup();
        methodGenerator.invokeConstructor(type, Method.getMethod("void <init> ()"));
    }

    @Override
    public void enterFloatLiteralExpression(MiniJavaParser.FloatLiteralExpressionContext ctx) {
        float ex = Float.parseFloat(ctx.getText());
        methodGenerator.push(Float.parseFloat(ctx.getText()));
    }

    @Override
    public void exitArrayAccessExpression(MiniJavaParser.ArrayAccessExpressionContext ctx) {
        String type = expressionTypes.get(ctx);

        if("int[]".equals(type))
            methodGenerator.arrayLoad(Type.INT_TYPE);
        else if("float[]".equals(type))
            methodGenerator.arrayLoad(Type.FLOAT_TYPE);
    }

    @Override
    public void enterTrueLiteralExpression(MiniJavaParser.TrueLiteralExpressionContext ctx) {
        methodGenerator.push(Boolean.parseBoolean(ctx.getText()));
    }

    @Override
    public void exitThisClassExpression(MiniJavaParser.ThisClassExpressionContext ctx) {
        methodGenerator.loadThis();
    }

    @Override
    public void exitNewFloatArrayExpression(MiniJavaParser.NewFloatArrayExpressionContext ctx) {
        methodGenerator.newArray(Type.FLOAT_TYPE);
    }

    @Override
    public void exitArrayLengthExpression(MiniJavaParser.ArrayLengthExpressionContext ctx) {
        methodGenerator.arrayLength();
    }

    @Override
    public void exitNewIntegerArrayExpression(MiniJavaParser.NewIntegerArrayExpressionContext ctx) {
        methodGenerator.newArray(Type.INT_TYPE);
    }

    @Override
    public void exitArithExpression(MiniJavaParser.ArithExpressionContext ctx) {
        String leftType = expressionTypes.get(ctx.expression(0));
        String rightType = expressionTypes.get(ctx.expression(1));
        String operator = ctx.getChild(1).getText(); // The operator ('&&', '<', '+', '-', '*')

        if (leftType != null && rightType != null) {
            switch (operator) {
                case "+":
                    if ("float".equals(leftType))
                        methodGenerator.math(GeneratorAdapter.ADD, Type.FLOAT_TYPE);
                    else if ("int".equals(leftType))
                        methodGenerator.math(GeneratorAdapter.ADD, Type.INT_TYPE);
                    break;
                case "-":
                    if ("float".equals(leftType))
                        methodGenerator.math(GeneratorAdapter.SUB, Type.FLOAT_TYPE);
                    else if ("int".equals(leftType))
                        methodGenerator.math(GeneratorAdapter.SUB, Type.INT_TYPE);
                    break;
                case "*":
                    if ("float".equals(leftType))
                        methodGenerator.math(GeneratorAdapter.MUL, Type.FLOAT_TYPE);
                    else if ("int".equals(leftType))
                        methodGenerator.math(GeneratorAdapter.MUL, Type.INT_TYPE);
                    break;

                case "<":
                    Label trueLabel = methodGenerator.newLabel();
                    Label endLabel = methodGenerator.newLabel();

                    if ("int".equals(leftType) && "int".equals(rightType))
                        methodGenerator.ifCmp(Type.INT_TYPE, GeneratorAdapter.LT, trueLabel);
                    else if("float".equals(leftType) && "float".equals(rightType))
                        methodGenerator.ifCmp(Type.FLOAT_TYPE, GeneratorAdapter.LT, trueLabel);

                    methodGenerator.push(false);
                    methodGenerator.goTo(endLabel);
                    methodGenerator.mark(trueLabel);
                    methodGenerator.push(true);
                    methodGenerator.mark(endLabel);
                    break;

                case "&&":
                    // Logical AND operation
                    if ("boolean".equals(leftType) && "boolean".equals(rightType))
                        methodGenerator.math(GeneratorAdapter.AND, Type.BOOLEAN_TYPE);
                    break;

                default:
                    break;
            }
        } else {
            // Error: missing types (this shouldn't happen if type checking is properly enforced elsewhere)
            System.err.println("Type Error at line " + ctx.getStart().getLine() + ": Missing types for operands.");
        }
    }

    @Override
    public void enterFalseLiteralExpression(MiniJavaParser.FalseLiteralExpressionContext ctx) {
        methodGenerator.push(Boolean.parseBoolean(ctx.getText()));
    }
}
