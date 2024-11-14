import antlr.gen.output.MiniJavaBaseListener;
import antlr.gen.output.MiniJavaParser;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import symbols.*;

import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.Set;

public class SymbolResolutionListener extends MiniJavaBaseListener {
    ParseTreeProperty<Scope> scopes;
    GlobalScope globals;
    Scope currentScope;

    SymbolResolutionListener(GlobalScope globals, ParseTreeProperty<Scope> scopes) {
        this.scopes = scopes;
        this.globals = globals;
        currentScope = globals;
    }

    @Override
    public void enterClassDeclaration(MiniJavaParser.ClassDeclarationContext ctx) {
        currentScope = scopes.get(ctx);

        // Get the class name and resolve its symbol
        String className = ctx.identifier(0).getText();
        ClassSymbol classSymbol = (ClassSymbol) currentScope.getEnclosingScope().resolve(className);

        // Check for cyclic inheritance
        if (classSymbol != null && classSymbol.hasCyclicInheritance()) {
            System.err.println("Inheritance Error at line " + ctx.getStart().getLine() + ": Cyclic inheritance detected in class: " + className);
        }
    }

    @Override
    public void exitClassDeclaration(MiniJavaParser.ClassDeclarationContext ctx) {
        currentScope = currentScope.getEnclosingScope();
    }

    @Override
    public void enterMethodDeclaration(MiniJavaParser.MethodDeclarationContext ctx) {
        currentScope = scopes.get(ctx);

        String methodName = ctx.identifier(0).getText();
        String returnType = ctx.type(0).getText();

        ClassSymbol classSymbol = (ClassSymbol) currentScope.getEnclosingScope().getEnclosingScope();

        Symbol method = classSymbol.resolveInSuper(methodName);

        if (method != null) {
            Set<String> superParams = ((MethodSymbol) method).arguments.keySet();
            LinkedHashMap<String, String> childParams = new LinkedHashMap<>();

            for (int i = 1; i < ctx.identifier().size(); i++) {
                String paramName = ctx.identifier(i).getText();
                String paramType = ctx.type(i).getText();
                childParams.put(paramName, paramType);
            }

            if (!superParams.equals(childParams.keySet())) {
                System.err.println("Override error at line " + ctx.getStart().getLine() + ": Differing parameter names!");
            }

            for (String paramName : superParams) {
                String superParamType = ((MethodSymbol) method).arguments.get(paramName).getType();
                String childParamType = childParams.get(paramName);

                if (!superParamType.equals(childParamType)) {
                    System.err.println("Override error at line " + ctx.getStart().getLine() + ": Parameter type mismatch for " + paramName +
                            ": expected " + superParamType + ", but got " + childParamType);
                }
            }

            if (!Objects.equals(method.getType(), returnType)) {
                System.err.println("Override error at line " + ctx.getStart().getLine() + ": Differing return types: expected " + method.getType() + ", but got " + returnType);
            }
        }
    }


    @Override
    public void exitMethodDeclaration(MiniJavaParser.MethodDeclarationContext ctx) {
        currentScope = currentScope.getEnclosingScope();
    }

    @Override
    public void enterVarDeclaration(MiniJavaParser.VarDeclarationContext ctx) {
        String varName = ctx.identifier().getText();
        String varType = ctx.type().getText();

        if(currentScope instanceof ClassSymbol) {
            Symbol varSymbol = ((ClassSymbol) currentScope).resolveInSuper(varName);

            if(varSymbol != null && !Objects.equals(varSymbol.getType(), varType)) {
                System.err.println("Type error at line " + ctx.getStart().getLine() + ": Differing variable types:" + varType + ", " + varSymbol.getType());
            }
        }
    }

    @Override
    public void exitVarDeclaration(MiniJavaParser.VarDeclarationContext ctx) {
        String varName = ctx.identifier().getText();
        MiniJavaParser.IdentifierContext varType = ctx.type().identifier();

        //If we have an identifier as our var type, check that the class is in scope.
        if(varType != null) {
            String className = varType.getText();

            Symbol classSymbol = globals.resolve(className);

            if(classSymbol == null) {
                System.err.println( "Res Error at line " + ctx.getStart().getLine() + ": no such class: " + className + " for variable: " + varName);
            }
        }
    }

    @Override
    public void exitIdentifierExpression(MiniJavaParser.IdentifierExpressionContext ctx) {
        String varName = ctx.identifier().getText();

        Symbol var = currentScope.resolve(varName);
        if ( var==null ) {
            System.err.println( "Res Error at line " + ctx.getStart().getLine() + ": no such variable: " + varName);
        }
        if (var instanceof ClassSymbol || var instanceof MethodSymbol) {
            System.err.println("Res Error at line " + ctx.getStart().getLine() + ": " + varName + " is not a variable");
        }
    }

    @Override
    public void exitMethodCallExpression(MiniJavaParser.MethodCallExpressionContext ctx) {
        String methodName = ctx.identifier().getText();
        String className;
        int numArgs = ctx.expression().size() - 1;

        MiniJavaParser.ExpressionContext classExp = ctx.expression(0);

        ClassSymbol classSymbol = null;

        // If we are instancing a new class, look in global scope for that class
        // Else, we are using a variable, so find that var in the scope
        //      if we found it, try to resolve its class type in the global scope
        if (classExp instanceof MiniJavaParser.NewClassExpressionContext newClassCtx) {
            className = newClassCtx.identifier().getText();
            classSymbol = (ClassSymbol) globals.resolve(className);
        } else if(classExp instanceof MiniJavaParser.ThisClassExpressionContext) {
            classSymbol = (ClassSymbol) currentScope.getEnclosingScope().getEnclosingScope();
        } else {
            className = classExp.getText();
            Symbol classVar = currentScope.resolve(className);

            if(classVar != null)
                classSymbol = (ClassSymbol) globals.resolve(classVar.getType());
        }



        // If we couldn't find class anywhere, panic, otherwise, try to find the method call.
        if(classSymbol != null) {
            Symbol methodSymbol = classSymbol.resolve(methodName);

            if (methodSymbol == null)
                System.err.println("Res Error at line " + ctx.getStart().getLine() + ": no such method: " + methodName);
            if (methodSymbol instanceof VariableSymbol || methodSymbol instanceof ClassSymbol)
                System.err.println("Res Error at line " + ctx.getStart().getLine() + ": " + methodName + " is not a method");
            if(methodSymbol instanceof MethodSymbol && ((MethodSymbol) methodSymbol).arguments.size() != numArgs) {
                System.err.println("Res Error at line " + ctx.getStart().getLine() + ": Number of arguments mismatch for: " + methodName);
            }
        }
    }

    @Override
    public void exitNewClassExpression(MiniJavaParser.NewClassExpressionContext ctx) {
        String className = ctx.identifier().getText();

        Symbol classSymbol = globals.resolve(className);
        if (classSymbol == null) {
            System.err.println("Res Error at line " + ctx.getStart().getLine() + ": no such class: " + className);
        }
        if (classSymbol instanceof VariableSymbol || classSymbol instanceof MethodSymbol) {
            System.err.println("Res Error at line " + ctx.getStart().getLine() + ": " + className + " is not a class");
        }
    }
}
