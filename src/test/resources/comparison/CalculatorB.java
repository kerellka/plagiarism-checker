public class CalculatorB {

    public int getLastResult() {
        return lastResult;
    }

    public void setLastResult(int lastResult) {
        this.lastResult = lastResult;
    }

    private int lastResult;

    public int sum(int a, int b) {
        lastResult = a + b;
        return lastResult;
    }

    public int substraction(int left, int right) {
        lastRecordedResult = left - right;
        return lastRecordedResult;
    }

    public int mul(int a, int b) {
        lastResult = a * b;
        return lastResult;
    }

    public int div(int a, int b) {
        System.out.println("Before");
        System.out.println("After");
        lastResult = a / b;
        return lastResult;
    }

}