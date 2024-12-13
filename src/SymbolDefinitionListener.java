import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

public class SymbolDefinitionListener extends MiniJavaBaseListener {
    ParseTreeProperty<Scope> scopes = new ParseTreeProperty<Scope>();
    GlobalScope globals;
    Scope currentScope;

    SymbolDefinitionListener() {
        globals = new GlobalScope(null);
        currentScope = globals;
    }

    @Override
    public void enterGoal(MiniJavaParser.GoalContext ctx) {
        super.enterGoal(ctx);
    }

    @Override
    public void exitGoal(MiniJavaParser.GoalContext ctx) {
        //System.out.println(globals);
    }

    @Override
    public void enterClassDeclaration(MiniJavaParser.ClassDeclarationContext ctx) {
        String className = ctx.identifier(0).getText();
        ClassSymbol classSymbol = new ClassSymbol(className, currentScope, null);

        if(currentScope.resolve(className) != null) {
            System.err.println( "Def Error at line " + ctx.getStart().getLine() + ": Class already declared: " + className);
        } else {
            currentScope.define(classSymbol);
        }

        saveScope(ctx, classSymbol);
        currentScope = classSymbol;
    }

    void saveScope(ParserRuleContext ctx, Scope s) { scopes.put(ctx, s); }

    @Override
    public void exitClassDeclaration(MiniJavaParser.ClassDeclarationContext ctx) {
        //System.out.println(currentScope);

        currentScope = currentScope.getEnclosingScope();

    }

    @Override
    public void enterVarDeclaration(MiniJavaParser.VarDeclarationContext ctx) {
        super.enterVarDeclaration(ctx);
    }

    @Override
    public void exitVarDeclaration(MiniJavaParser.VarDeclarationContext ctx) {
        String varName = ctx.identifier().getText();
        String varType = ctx.type().getText();

        //Check that var is not already defined in scope, otherwise, define it
        Symbol res = currentScope.resolveLocal(ctx.identifier().getText());

        if(currentScope instanceof LocalScope) {
            res = currentScope.getEnclosingScope().resolve(ctx.identifier().getText());
        }

        if (res != null) {
            System.err.println( "Def Error at line " + ctx.getStart().getLine() + ": Variable already declared: " + varName);
        } else {
            VariableSymbol var = new VariableSymbol(varName, varType);
            var.setScope(currentScope);
            currentScope.define(var);
        }

    }

    @Override
    public void enterMethodDeclaration(MiniJavaParser.MethodDeclarationContext ctx) {
        String methodName = ctx.identifier(0).getText();
        String returnType = ctx.type(0).getText();

        MethodSymbol methodSymbol = new MethodSymbol(methodName, returnType, currentScope);
        for (int i = 1; i < ctx.identifier().size(); i++) {
            VariableSymbol var = new VariableSymbol(ctx.identifier(i).getText(), ctx.type(i).getText());
            
            if(methodSymbol.resolveLocal(ctx.identifier(i).getText()) != null)
                System.err.println( "Def Error at line " + ctx.getStart().getLine() + ": Method parameter already declared: " + ctx.identifier(i).getText());
            else
                methodSymbol.define(var);
        }

        if(currentScope.resolve(methodName) != null) {
            System.err.println( "Def Error at line " + ctx.getStart().getLine() + ": Method already declared: " + methodName);
        } else {
            currentScope.define(methodSymbol);
        }

        saveScope(ctx, methodSymbol);
        currentScope = methodSymbol;

        currentScope = new LocalScope(currentScope);
        saveScope(ctx, currentScope);
    }

    @Override
    public void exitMethodDeclaration(MiniJavaParser.MethodDeclarationContext ctx) {
        //System.out.println(currentScope);
        currentScope = currentScope.getEnclosingScope();
        //System.out.println(currentScope);
        currentScope = currentScope.getEnclosingScope();
    }
}
