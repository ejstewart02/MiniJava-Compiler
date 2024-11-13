package symbols;

public class VariableSymbol extends Symbol {
    boolean initialized;

    public VariableSymbol(String name, String type) {
        super(name, type);
        initialized = false;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    public boolean isInitialized() {
        return initialized;
    }


}
