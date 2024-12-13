import org.antlr.v4.runtime.tree.ParseTreeProperty;

public class InheritanceListener extends MiniJavaBaseListener {
    ParseTreeProperty<Scope> scopes;
    GlobalScope globals;
    Scope currentScope;

    InheritanceListener(GlobalScope globals, ParseTreeProperty<Scope> scopes) {
        this.scopes = scopes;
        this.globals = globals;
        currentScope = globals;
    }

    @Override
    public void enterClassDeclaration(MiniJavaParser.ClassDeclarationContext ctx) {
        currentScope = scopes.get(ctx);

        ClassSymbol superClass = null;

        String superClassName;

        //If we have: extends X
        if(ctx.identifier(1) != null) {
            //Get our superclass name and symbol
            superClassName = ctx.identifier(1).getText();
            superClass = (ClassSymbol) globals.resolve(superClassName);

            //Get our current class symbol
            ClassSymbol currentClass = (ClassSymbol) currentScope;

            //If superclass exists, add as parent to current class
            if(superClass != null) {
                currentClass.setParent(superClass);
            } else {
                System.err.println("Inheritance Error: cannot inherit class: " + superClassName +
                        " into class: " + currentClass.getName() + ", it might not exist.");
            }
        }
    }

    @Override
    public void exitClassDeclaration(MiniJavaParser.ClassDeclarationContext ctx) {
        currentScope = currentScope.getEnclosingScope();
    }
}
