import antlr.gen.output.MiniJavaBaseListener;
import antlr.gen.output.MiniJavaParser;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import symbols.*;

import java.util.Set;

public class StaticTypeCheckingListener extends MiniJavaBaseListener {
    private final ParseTreeProperty<Scope> scopes;
    private final GlobalScope globals;
    private Scope currentScope;
    private final ParseTreeProperty<String> expressionTypes = new ParseTreeProperty<>();

    public StaticTypeCheckingListener(ParseTreeProperty<Scope> scopes, GlobalScope globals) {
        this.scopes = scopes;
        this.globals = globals;
    }

    @Override
    public void enterClassDeclaration(MiniJavaParser.ClassDeclarationContext ctx) {
        currentScope = scopes.get(ctx);
    }

    @Override
    public void exitClassDeclaration(MiniJavaParser.ClassDeclarationContext ctx) {
        currentScope = currentScope.getEnclosingScope();
    }

    @Override
    public void enterMethodDeclaration(MiniJavaParser.MethodDeclarationContext ctx) {
        currentScope = scopes.get(ctx);
    }

    @Override
    public void exitMethodDeclaration(MiniJavaParser.MethodDeclarationContext ctx) {
        currentScope = currentScope.getEnclosingScope();

        // Get the expression's evaluated type from the expressionTypes map
        String returnExprType = expressionTypes.get(ctx.expression());

        // Ensure current scope is a method to retrieve its return type
        if (currentScope instanceof MethodSymbol methodSymbol) {
            String methodReturnType = methodSymbol.getType();

            // Compare the types
            if(returnExprType != null) {
                if (!returnExprType.equals(methodReturnType)) {
                    System.err.println("Type Error at line " + ctx.getStart().getLine() +
                            ": Return type " + returnExprType + " does not match declared return type " + methodReturnType);
                }
            }

        }
    }

    @Override
    public void exitArithExpression(MiniJavaParser.ArithExpressionContext ctx) {
        String leftType = expressionTypes.get(ctx.expression(0));
        String rightType = expressionTypes.get(ctx.expression(1));

        // If one of the operands is a float, promote the other operand to float
        if(leftType != null && rightType != null) {
            if ("float".equals(leftType) || "float".equals(rightType)) {
                // Promote to float and handle mixed int-float scenarios
                if ("int".equals(leftType)) {
                    leftType = "float";
                    expressionTypes.put(ctx.expression(0), "float"); // Update to indicate promotion
                }
                if ("int".equals(rightType)) {
                    rightType = "float";
                    expressionTypes.put(ctx.expression(1), "float"); // Update to indicate promotion
                }

                expressionTypes.put(ctx, "float"); // Result of the expression is float
            } else if ("int".equals(leftType) && "int".equals(rightType)) {
                // Both operands are int, so result is int
                expressionTypes.put(ctx, "int");
            } else {
                // Error: incompatible types
                System.err.println("Type Error at line " + ctx.getStart().getLine() + ": Incompatible types for arithmetic operation: " + leftType + " and " + rightType);
            }
        }

    }

    @Override
    public void exitArrayAccessExpression(MiniJavaParser.ArrayAccessExpressionContext ctx) {
        String arrayType = expressionTypes.get(ctx.expression(0));
        String indexType = expressionTypes.get(ctx.expression(1));

        if (!"int[]".equals(arrayType) && !"float[]".equals(arrayType)) {
            System.err.println("Type Error at line " + ctx.getStart().getLine() + ": Expected array type for array access.");
        }
        if (!"int".equals(indexType)) {
            System.err.println("Type Error at line " + ctx.getStart().getLine() + ": Array index must be an int.");
        }

        // Set result type based on array type
        if ("int[]".equals(arrayType)) {
            expressionTypes.put(ctx, "int");
        } else if ("float[]".equals(arrayType)) {
            expressionTypes.put(ctx, "float");
        }
    }

    @Override
    public void exitArrayLengthExpression(MiniJavaParser.ArrayLengthExpressionContext ctx) {
        String arrayType = expressionTypes.get(ctx.expression());

        if (!"int[]".equals(arrayType) && !"float[]".equals(arrayType)) {
            System.err.println("Type Error at line " + ctx.getStart().getLine() + ": Only arrays have a length.");
        }
        expressionTypes.put(ctx, "int"); // length is always an integer
    }

    @Override
    public void exitIntLiteralExpression(MiniJavaParser.IntLiteralExpressionContext ctx) {
        expressionTypes.put(ctx, "int");
    }

    @Override
    public void exitFloatLiteralExpression(MiniJavaParser.FloatLiteralExpressionContext ctx) {
        expressionTypes.put(ctx, "float");
    }

    @Override
    public void exitTrueLiteralExpression(MiniJavaParser.TrueLiteralExpressionContext ctx) {
        expressionTypes.put(ctx, "boolean");
    }

    @Override
    public void exitFalseLiteralExpression(MiniJavaParser.FalseLiteralExpressionContext ctx) {
        expressionTypes.put(ctx, "boolean");
    }

    @Override
    public void exitIdentifierExpression(MiniJavaParser.IdentifierExpressionContext ctx) {
        String varName = ctx.identifier().getText();
        Symbol varSymbol = currentScope.resolve(varName);

        if (varSymbol == null) {
//            System.err.println("Resolution Error at line " + ctx.getStart().getLine() + ": Variable " + varName + " not found.");
        } else {
            expressionTypes.put(ctx, varSymbol.getType());
        }
    }

    @Override
    public void exitNewIntegerArrayExpression(MiniJavaParser.NewIntegerArrayExpressionContext ctx) {
        String indexType = expressionTypes.get(ctx.expression());

        if (!"int".equals(indexType)) {
            System.err.println("Type Error at line " + ctx.getStart().getLine() + ": Array size must be an int.");
        }
        expressionTypes.put(ctx, "int[]");
    }

    @Override
    public void exitNewFloatArrayExpression(MiniJavaParser.NewFloatArrayExpressionContext ctx) {
        String indexType = expressionTypes.get(ctx.expression());

        if (!"int".equals(indexType)) {
            System.err.println("Type Error at line " + ctx.getStart().getLine() + ": Array size must be an int.");
        }
        expressionTypes.put(ctx, "float[]");
    }

    @Override
    public void exitNewClassExpression(MiniJavaParser.NewClassExpressionContext ctx) {
        String className = ctx.identifier().getText();
        Symbol classSymbol = globals.resolve(className);

//        if (classSymbol == null) {
//            System.err.println("Resolution Error at line " + ctx.getStart().getLine() + ": Class " + className + " not found.");
//        }
        expressionTypes.put(ctx, className);
    }

    @Override
    public void exitNotExpression(MiniJavaParser.NotExpressionContext ctx) {
        String exprType = expressionTypes.get(ctx.expression());

        if (!"boolean".equals(exprType)) {
            System.err.println("Type Error at line " + ctx.getStart().getLine() + ": '!' operator requires a boolean expression.");
        }
        expressionTypes.put(ctx, "boolean");
    }

    @Override
    public void exitLpRpExpression(MiniJavaParser.LpRpExpressionContext ctx) {
        // Propagate the type from the contained expression
        expressionTypes.put(ctx, expressionTypes.get(ctx.expression()));
    }

    @Override
    public void exitVarAssignStatement(MiniJavaParser.VarAssignStatementContext ctx) {
        String varName = ctx.identifier().getText();
        Symbol var = currentScope.resolve(varName);

        String varType = null;
        if(var != null) {
            varType = var.getType();
        }

        String exprType = expressionTypes.get(ctx.expression());
        if(exprType != null) {
            if (varType == null) {
                System.err.println("Type Error at line " + ctx.getStart().getLine() + ": Variable " + varName + " is not defined.");
            } else if (!varType.equals(exprType)) {
                System.err.println("Type Error at line " + ctx.getStart().getLine() + ": Cannot assign " + exprType + " to " + varType + ".");
            }
        }
    }

    @Override
    public void exitArrayAssignStatement(MiniJavaParser.ArrayAssignStatementContext ctx) {
        String varName = ctx.identifier().getText();
        String varType = currentScope.resolve(varName).getType();

        // Check if the array index expression is an integer
        String indexType = expressionTypes.get(ctx.expression(0));
        if(indexType != null) {
            if (!"int".equals(indexType)) {
                System.err.println("Type Error at line " + ctx.getStart().getLine() + ": Array index must be an integer.");
            }
        }
        // Check that the assigned expression matches the array's element type
        String exprType = expressionTypes.get(ctx.expression(1));
        if(exprType != null) {
            if (varType == null) {
                System.err.println("Type Error at line " + ctx.getStart().getLine() + ": Variable " + varName + " is not defined.");
            } else if (!(varType.equals("int[]") || varType.equals("float[]"))) {
                System.err.println("Type Error at line " + ctx.getStart().getLine() + ": Variable " + varName + " is not an array.");
            } else if (!varType.replace("[]", "").equals(exprType)) {
                System.err.println("Type Error at line " + ctx.getStart().getLine() + ": Cannot assign " + exprType + " to array of type " + varType + ".");
            }
        }
    }

    @Override
    public void exitIfElseStatement(MiniJavaParser.IfElseStatementContext ctx) {
        String conditionType = expressionTypes.get(ctx.expression());

        if(conditionType != null) {
            if (!"boolean".equals(conditionType)) {
                System.err.println("Type Error at line " + ctx.getStart().getLine() + ": Condition in if statement must be a boolean.");
            }
        }
    }

    @Override
    public void exitWhileStatement(MiniJavaParser.WhileStatementContext ctx) {
        String conditionType = expressionTypes.get(ctx.expression());
        if(conditionType != null) {
            if (!"boolean".equals(conditionType)) {
                System.err.println("Type Error at line " + ctx.getStart().getLine() + ": Condition in while statement must be a boolean.");
            }
        }
    }

    @Override
    public void exitPrintStatement(MiniJavaParser.PrintStatementContext ctx) {
        String exprType = expressionTypes.get(ctx.expression());
        if(exprType != null) {
            if (!(exprType.equals("int") || exprType.equals("float") || exprType.equals("String"))) {
                System.err.println("Type Error at line " + ctx.getStart().getLine() + ": System.out.println cannot print type " + exprType + ".");
            }
        }
    }

    @Override
    public void exitMethodCallExpression(MiniJavaParser.MethodCallExpressionContext ctx) {
        String methodName = ctx.identifier().getText();
        String className;

        MiniJavaParser.ExpressionContext classExp = ctx.expression(0);

        ClassSymbol classSymbol = null;

        // Resolve the method symbol using the class and method name
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

        if (classSymbol != null) {
            // Find the method in the class symbol
            Symbol methodSymbol = classSymbol.resolve(methodName);
            if (methodSymbol instanceof MethodSymbol method) {
                // Compare argument types
                Set<String> keys = method.arguments.keySet();
                int i = 1;
                for (String key : keys) {
                    String paramType = method.arguments.get(key).getType();
                    String argType = expressionTypes.get(ctx.expression(i));  // Get the actual argument type

                    if (!paramType.equals(argType)) {
                        System.err.println("Error at line " + ctx.getStart().getLine() + ": Argument " + i + " in method call " + methodName + " expects " + paramType + ", but got " + argType);
                    }
                    i++;
                }
                String returnType = methodSymbol.getType();
                expressionTypes.put(ctx, returnType);
            }
        }


    }
}
