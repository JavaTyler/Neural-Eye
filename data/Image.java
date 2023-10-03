/**
 * @version 1.0.0
 * This class is utilized for the creation of an image object, suited for a square entry image.
 * (Ex X by Y where X == Y).
 * To-Do:
 * Add constraints as algorithm is developed.
 */

package data;

public class Image {

    private double[][] data; //data is in 2d array; double in case of input type (in case not int)
    private int label;

    public Image(double[][] data, int label) {
        this.data = data;
        this.label = label;
    }

    // getters
    public double[][] getData() {
        return data;
    }

    public int getLabel() {
        return label;
    }

    public String toString() {
        String s = label + ", \n";

        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[0].length; j++) { // j is referencing the length of the first row length (row 0, all columns) doesn't matter can be 0...27 as it is 28x28.
                s += data[i][j] + ", ";
            }
            s += "\n";
        }
        return s;
    }
}
