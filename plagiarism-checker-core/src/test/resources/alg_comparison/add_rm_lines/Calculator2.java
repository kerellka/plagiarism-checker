public class Calculator2 {

    private int lastRecordedResult;

    public int summa(int left, int right) {
        lastRecordedResult = left + right;
        return lastRecordedResult;
    }

    public int substraction(int left, int right) {
        lastRecordedResult = left - right;
        return lastRecordedResult;
    }

    public int multiplication(int left, int right) {
        lastRecordedResult = left * right;
        return lastRecordedResult;
    }

    public int divisionjoy(int left, int right) {
        lastRecordedResult = left / right;
        return lastRecordedResult;
    }

}