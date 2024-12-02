package symbols;

import org.objectweb.asm.commons.Method;

import java.util.LinkedHashMap;
import java.util.Map;

public class MethodSymbol extends Symbol implements Scope {
    public Map<String, Symbol> arguments = new LinkedHashMap<String, Symbol>();
    Scope enclosingScope;

    public MethodSymbol(String name, String retType, Scope enclosingScope) {
        super(name, retType);
        this.enclosingScope = enclosingScope;
    }

    public Symbol resolve(String name) {
        Symbol s = arguments.get(name);
        if(s!=null )
            return s;
        // if not here, check any enclosing scope
        if(getEnclosingScope() != null) {
            return getEnclosingScope().resolve(name);
        }
        return null; // not found
    }

    @Override
    public Symbol resolveLocal(String name) {
        return arguments.get(name);
    }


    public void define(Symbol sym) {
        arguments.put(sym.name, sym);
        sym.scope = this; // track the scope in each symbol
    }

    public Scope getEnclosingScope() {
        return enclosingScope;
    }

    public String getScopeName() {
        return name;
    }

    public String toString() {
        return "method: " + super.toString() + ":" + arguments.values();
    }

    //---------CODE GEN---------//
    public String buildName(){
        StringBuilder fullName = new StringBuilder(this.type + " " + this.name);
        ///fullName = fullName.substring(0, fullName.length()-1);

        if(!arguments.isEmpty()) {
            for(Symbol parameter : arguments.values()){
                fullName.append(parameter.name).append(", ");
            }

            fullName = new StringBuilder(fullName.substring(0, fullName.length() - 2));
        }
        fullName.append(")");

        return fullName.toString();
    }


    public Method getByteCodeMethod(){
        return Method.getMethod(this.buildName(), true);

    }

//    public static String getMethodSignature(MinijavaParser.MethodDeclarationContext ctx){
//        return ctx.identifier().getText() + "()";
//    }
}