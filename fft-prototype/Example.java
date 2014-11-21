import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.FileReader;

public class Example {

    private static final int FRAME_SIZE = 1024;

    public static ArrayList<Double> readData( int i ) {
        ArrayList<Double> ret = new ArrayList<Double>();

        try {
            BufferedReader in = new BufferedReader(new FileReader("mock_data/input_" + i + ".txt")); 

            for (int j = 0; j < FRAME_SIZE; j ++) {
                ret.add(Double.parseDouble(in.readLine()));
            }
            in.close();
        } catch(Exception e) {
            // die
        }

        return ret;
    }

    public static boolean checkResult( int i, double probableFrequency ) {
        double answer = -101010;
        try {
            BufferedReader in = new BufferedReader(new FileReader("mock_data/output_" + i + ".txt")); 
            answer = Double.parseDouble(in.readLine());
            in.close();
        } catch(Exception e) {
            // die
        }

        boolean result = Math.abs(answer - probableFrequency) <= 1e-12;
        if (!result) {
            System.out.println(" *** ERROR *** " + probableFrequency + " " + answer + " " + result);
        }
        return result;
    }

    public static void main(String[] args) {
        for (int i = 0; i < 500; i ++ ) {
            ArrayList<Double> data = readData(i);
            FrequencyAnalyzer fa = new FrequencyAnalyzer(data);
            double probableFrequency = fa.getMostProbableFrequency(19000, 21000, 0.25);
            checkResult(i, probableFrequency);
        }
    }
}
