package symbols;


public class Symbol { // A generic programming language symbol
    String name;      // All symbols at least have a name
    String type;
    Scope scope;      // All symbols know what scope contains them.

    public Symbol(String name) {
        this.name = name;
    }

    public Symbol(String name, String type) {
        this(name); this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String toString() {
        if(type != null)
            return '<' + name + ":" + type + '>';
        return '<' + name + '>';
    }
}
