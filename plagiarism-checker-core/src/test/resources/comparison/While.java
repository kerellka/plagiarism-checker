public class While {

    public void doWhile() {
        int lastRecordedResult = 101;
        int i = 0;
        while (i < 10) {
            System.out.println("Hello!");
            System.out.println(lastRecordedResult);
            int l = 10;
            i++;
        }
    }

}