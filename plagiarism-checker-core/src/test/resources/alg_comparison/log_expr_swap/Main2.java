public class Main2 {
    public static void main(String[] args) {
        int x = 10;
        int y = 1010;

        boolean bl1 = x > 0 && x < y;
        boolean bl2 = x == 10 || x > y ;

        if (x > 0 && x < y) {
            System.out.println("first branch");
        }

        if (x == 10 || x > y) {
            System.out.println("second branch");
        }
    }
}










