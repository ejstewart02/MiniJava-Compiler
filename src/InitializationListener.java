import antlr.gen.output.MiniJavaBaseListener;
import antlr.gen.output.MiniJavaParser;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import symbols.*;

import java.util.Objects;

public class InitializationListener extends MiniJavaBaseListener {
    ParseTreeProperty<Scope> scopes;
    GlobalScope globals;
    Scope currentScope;

    public InitializationListener(GlobalScope globals, ParseTreeProperty<Scope> scopes) {
        this.scopes = scopes;
        this.globals = globals;
        currentScope = globals;
    }

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
    }

    @Override
    public void enterVarAssignStatement(MiniJavaParser.VarAssignStatementContext ctx) {
        String varName = ctx.identifier().getText();

        VariableSymbol variableSymbol = (VariableSymbol) currentScope.resolve(varName);
        if(variableSymbol != null) {
            variableSymbol.setInitialized(true);
        }

        //System.out.println("Initialized: " + varName + ", In Scope: " + currentScope.getScopeName());
    }
}
