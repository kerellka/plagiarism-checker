public class Calculator1 {

    private int lastResult;

    public int sum(int a, int b) {
        lastResult = a + b;
        System.out.println("noise line");
        return lastResult;
    }

    public int sub(int a, int b) {
        lastResult = a - b;
        return lastResult;
    }

    public int mul(int a, int b) {
        int variable = 10;
        lastResult = a * b;
        System.out.println("noise line");
        return lastResult;
    }

    public int div(int a, int b) {
        lastResult = a / b;
        return lastResult;
    }

}