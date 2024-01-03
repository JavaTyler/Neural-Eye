package data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class DataReader {

    // 28x28 pixels; mnist data follows that format.
    private final int rows = 28;
    private final int cols = 28;

    //filepath as input (string format)
    public List<Image> readData(String path) {

        List<Image> images = new ArrayList<>();

        try(BufferedReader dataReader = new BufferedReader(new FileReader(path))){

            String line;

            while ((line = dataReader.readLine()) != null) { // Continue till end of file (no more lines to read.

                //Split by all the commas in the csv/read-in lines
                String[] lineItems = line.split(","); // Split by comma

                double[][] data = new double[rows][cols];
                int label = Integer.parseInt(lineItems[0]); // The first number in each row is the label, aka what the data represents as image.
                                                            // parseInt() because it is a string being read into an int.
                                                            // Generics employed for autoboxing/unboxing.

                int i = 1; // Ignore i = 0 as that is the label index.

                for (int row = 0; row < rows; row++) {
                    for (int col = 0; col < cols; col++) {
                        data[row][col] = (double) Integer.parseInt(lineItems[i]);
                        i++;
                    }
                }

                images.add(new Image(data, label)); // adds the image (data), and the label (what it represents).
            }

        } catch(Exception e) {

        }

        return images;
    }
}
