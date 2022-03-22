public class Calculator2 {

    private int lastRecordedResult;

    public int getLastRecordedResult() {
        return lastRecordedResult;
    }

    public void setLastRecordedResult(int lastRecordedResult) {
        this.lastRecordedResult = lastRecordedResult;
    }

    public int summa(int left, int right) {
        lastRecordedResult = left + right;
        if (lastRecordedResult == 0 && left == 0 && right == 0) {
            System.out.println("Получили много 0!");
        }
        return lastRecordedResult;
    }

    public int substraction(int left, int right) {
        lastRecordedResult = left - right;
        int i = 0;
        while (i < 10) {
            System.out.println("print");
            i++;
        }
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