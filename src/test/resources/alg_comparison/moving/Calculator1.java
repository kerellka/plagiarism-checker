public class Calculator1 {

    private int lastResult;

    public int getLastResult() {
        return lastResult;
    }

    public int sub(int a, int b) {
        lastResult = a - b;
        return lastResult;
    }

    public int sum(int a, int b) {
        lastResult = a + b;
        return lastResult;
    }

    public void setLastResult(int lastResult) {
        this.lastResult = lastResult;
    }

    public int div(int a, int b) {
        lastResult = a / b;
        return lastResult;
    }

    public int mul(int a, int b) {
        lastResult = a * b;
        return lastResult;
    }

}