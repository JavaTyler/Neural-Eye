package layers;

import java.util.ArrayList;
import java.util.List;

public class MaxPoolLayer extends Layer {

    private int _stepSize;
    private int _windowSize;

    private int _inLength;
    private int _inRows;
    private int _inCols;

    // for backProp
    List<int[][]> _lastMaxRow;
    List<int[][]> _lastMaxCol;

    public MaxPoolLayer(int _stepSize, int _windowSize, int _inLength, int _inRows, int _inCols) {
        this._stepSize = _stepSize;
        this._windowSize = _windowSize;
        this._inLength = _inLength;
        this._inRows = _inRows;
        this._inCols = _inCols;
    }

    // input is a list of 2d arrays (various input Matrixes)
    public List<double[][]> maxPoolForwardPass(List<double[][]> input) {

        List<double[][]> output = new ArrayList<>();
        _lastMaxRow = new ArrayList<>();
        _lastMaxCol = new ArrayList<>();

        // every l is a matrix, size is all the matrices.
        for (int l = 0; l < input.size(); l++) {
            output.add(pool(input.get(l)));
        }

        return output;

    }

    // pool deals with 1 matrix at a time.
    public double[][] pool(double[][] input) {

        // Output rows
        double[][] output = new double[getOutputRows()][getOutputCols()]; // Output matrix (where mapped vals are) of the 'dense regions' of some region of pixels in the generated image.

        // for backProp
        int[][] maxRows = new int[getOutputRows()][getOutputCols()];
        int[][] maxCols = new int[getOutputRows()][getOutputCols()];

        for (int r = 0; r < getOutputRows(); r+= _stepSize) {
            for (int c = 0; c < getOutputCols(); c+= _stepSize) {
                double max = 0.0;

                // for backProp
                maxRows[r][c] = -1; // If we do not find a maximum, it is -1.
                maxCols[r][c] = -1;

                // Now that we are over the window (2x2 for example) look at the elements.
                // Iterate through the window
                for (int x = 0; x < _windowSize; x++) {
                    for (int y = 0; y < _windowSize; y++) {
                        if (max < input[r + x][c + y]) {
                            max = input[r + x][c + y];
                            maxRows[r][c] = r + x; // for backProp
                            maxCols[r][c] = c + y; // for backProp
                        }
                    }
                }
                output[r][c] = max;
            }
        }

        _lastMaxRow.add(maxRows);
        _lastMaxCol.add(maxCols);

        return output;

    }

    @Override
    public double[] getOutput(List<double[][]> input) {
        List<double[][]> outputPool = maxPoolForwardPass(input);
        return _nextLayer.getOutput(outputPool);
    }

    @Override
    public double[] getOutput(double[] input) {
        List<double[][]> matrixList = vectorToMatrix(input, _inLength, _inRows, _inCols);
        return getOutput(matrixList);
    }

    @Override
    public void backPropagation(double[] dldO) {

        List<double[][]> matrixList = vectorToMatrix(dldO, getOutputLength(), getOutputRows(), getOutputCols());
        backPropagation(matrixList);

    }

    @Override
    public void backPropagation(List<double[][]> dldO) {

        List<double[][]> dxdl = new ArrayList<>();
        int l = 0; // How far throughout our lists we are.
        for (double[][] array: dldO) {
            double[][] error = new double[_inRows][_inCols];

            for (int r = 0; r < getOutputRows(); r++) {
                for (int c = 0; c < getOutputCols(); c++) {
                    int max_i = _lastMaxRow.get(l)[r][c];
                    int max_j = _lastMaxCol.get(l)[r][c];

                    if (max_i != -1) {
                        error[max_i][max_j] += array[r][c];
                    }
                }
            }

            dxdl.add(error);
            l++;
        }

        if (_previousLayer != null) {
            _previousLayer.backPropagation(dxdl);
        }

    }

    @Override
    public int getOutputLength() {
        return _inLength;
    }

    @Override
    public int getOutputRows() {
        return (_inRows - _windowSize)/_stepSize + 1;
    }

    @Override
    public int getOutputCols() {
        return (_inCols - _windowSize)/_stepSize + 1;
    }

    @Override
    public int getOutputElements() {
        return _inLength*getOutputRows()*getOutputCols();
    }
}