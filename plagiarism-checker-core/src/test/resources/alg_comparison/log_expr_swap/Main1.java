public class Main1 {
    public static void main(String[] args) {

        int x = 10;
        int y = 1010;

        boolean bl1 = x < y && x > 0;
        boolean bl2 = x > y || x == 10;

        if (x < y && x > 0) {
            System.out.println("first branch");
        }

        if (x > y || x == 10) {
            System.out.println("second branch");
        }
    }
}






