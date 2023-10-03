package layers;

import java.util.ArrayList;
import java.util.List;

public abstract class Layer {

    public Layer get_nextLayer() {
        return _nextLayer;
    }

    public void set_nextLayer(Layer _nextLayer) {
        this._nextLayer = _nextLayer;
    }

    public Layer get_previousLayer() {
        return _previousLayer;
    }

    public void set_previousLayer(Layer _previousLayer) {
        this._previousLayer = _previousLayer;
    }

    protected Layer _nextLayer;
    protected Layer _previousLayer;

    // polymorphism
    public abstract double[] getOutput(List<double[][]> input);

    public abstract double[] getOutput(double[] input);

    public abstract void backPropogation(double[] dldD);

    public abstract void backPropogation(List<double[][]> dldD);

    public abstract int getOutputLength(); // List length of matrices
    public abstract int getOutputRows(); // List of rows (how many rows)
    public abstract int getOutputCols(); // Lists columns (how many columns, from output).
    public abstract int getOutputElements(); // Length of 1d output, length * rows * cols

    public double[] matrixToVector(List<double[][]> input) {

        int length = input.size();
        int rows = input.get(0).length;
        int cols = input.get(0)[0].length;

        // all numbers (matrix/matrices), their rows, and their columns as a vector hence l * r * c
        double[] vector = new double[length * rows * cols];

        int i = 0;
        for (int l = 0; l < length; l++) { // Every matrix
            for (int r = 0; r < rows; r++) { // Every row of the matrix
                for (int c = 0; c < cols; c++) { // Every column of the matrix
                    vector[i] = input.get(l)[r][c]; // List<>; double[r][], double[][c];
                    i++;
                }
            }
        }

        return vector;
    }

    // 1D vector/array to Matrix
    List<double[][]> vectorToMatrix(double[] input, int length, int rows, int cols) {

        List<double[][]> out = new ArrayList<>();
        int i = 0;

        for(int l = 0; l < length; l++) {

            double[][] matrix = new double[rows][cols];

            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    matrix[r][c] = input[i];
                    i++;
                }
            }

            out.add(matrix);

        }

        return out;

    }

}
