class Duplicates {
    public static void main(String[] args) {
        System.out.println(1);
    }
}

class D {
    int field;
    int field; //dup field

    public int f() { return 1; }
    public int f() { return 2; } //dup func
    public int g() {
        int x;
        int x; //dup type
        boolean x; //dup type

        return 0;
    }
    public int h(int x, int x, int y, int x) { //TODO: 3 dup types
        int y; //TODO: dup type

        return y;
    }
}

class D { //dup class
    public boolean f() { return  true; }
    public boolean f() { return false; } //dup func
}
