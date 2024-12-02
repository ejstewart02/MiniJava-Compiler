package symbols;

import org.objectweb.asm.Type;

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

    //BYTECODE STUFF//
    public Type getByteCodeType() {
        if(this.type.equals("int")){
            return Type.INT_TYPE;
        }else if(this.type.equals("boolean")){
            return Type.BOOLEAN_TYPE;
        }else if(this.type.equals("int[]")){
            return Type.getType(int[].class);
        }else if(this.type.equals("float[]")) {
            return Type.getType(float[].class);
        }else{
            return Type.getType("L" + this.type + ";");
        }
    }
}
