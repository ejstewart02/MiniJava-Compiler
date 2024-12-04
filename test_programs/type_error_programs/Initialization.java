class Initialization {
    public static void main(String[] args) {
        System.out.println(new I().run(0));
    }
}

class I {
    public int run(int i) {
        int x; //not initialized
        int y; //not initialized

        return x + y;
    }
}
