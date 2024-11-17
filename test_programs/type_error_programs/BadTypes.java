class BadTypes {
    public static void main(String[] args) {
        System.out.println(new BT().run(true)); //cant pass in true, should be int
    }
}

class BT {
    public int run(int n) {
        if (!n) { //cant say !n, must be bool, n is an int
            n = n - true; //cant subtract true form int
        } else {
            n[1] = n; //n is not an array
        }

        return false; //cant return false, should be int
    }

    public int away() {
        int x;
        int[] y;
        x = this.run(5) - true; //cant subtract true from false. cant set int to bool

        y = new int[4];
        y[true] = false; // cant set int array to a bool, cant index with bool either

        return new BT(); //wrong return type
    }
}
