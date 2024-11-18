class Instantiation {
    public static void main(String[] args) {
        System.out.println(new I().run(0));
    }
}

class I {
    public int run(int i) {
        int x;
        int y;
        
        while (i < 0) { //not initialized
            x = 1;
            y = i - 1; //not initialized
        }

        if (i < 0) { //not initialized
            x = 1;
        } else {
            y = i - 1; //not initialized
        }

        return x + y;
    }
}
