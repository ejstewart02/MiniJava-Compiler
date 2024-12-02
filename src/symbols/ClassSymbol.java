package symbols;

import org.objectweb.asm.Type;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class ClassSymbol extends Symbol implements Scope {
    Map<String, Symbol> arguments = new LinkedHashMap<>();
    Scope enclosingScope;
    ClassSymbol superClass;

    public ClassSymbol(String name, Scope enclosingScope, ClassSymbol superClass) {
        super(name);
        this.enclosingScope = enclosingScope;
        this.superClass = superClass;
    }

    public Symbol resolve(String name) {
        Symbol s = arguments.get(name);
        if (s != null)
            return s;
        // if not here, check just the superclass chain
        if (superClass != null) {
            return superClass.resolve(name);
        }
        return null; // not found
    }

    public Symbol resolveInSuper(String name) {
        if (superClass != null) {
            return superClass.resolve(name);
        }
        return null;
    }


    @Override
    public Symbol resolveLocal(String name) {
        return arguments.get(name);
    }

    public void setParent(Symbol parent) {
        this.superClass = (ClassSymbol) parent;
    }

    public void define(Symbol sym) {
        arguments.put(sym.name, sym);
        sym.scope = this; // track the scope in each symbol
    }

    public Scope getEnclosingScope() {
        return enclosingScope;
    }

    public Scope getParent() {
        if (superClass == null)
            return enclosingScope; // globals
        return superClass; // if not root object, return super
    }

    public boolean hasCyclicInheritance() {
        Set<ClassSymbol> visitedClasses = new HashSet<>();
        ClassSymbol current = this.superClass;

        while (current != null) {
            if (!visitedClasses.add(current)) {
                return true;
            }

            current = current.superClass;
        }

        return false;
    }

    public String getScopeName() {
        return name;
    }

    public String toString() {
        return "class: " + super.toString() + ":" + arguments.values();
    }

    //BYTECODE STUFF//
    public Type getByteCodeType() {
        return Type.getType("L" + this.type + ";");
    }
}