package layers;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static data.MatrixUtility.add;
import static data.MatrixUtility.multiply;

public class ConvolutionLayer extends Layer {

    private long SEED;

    private List<double[][]> _filters;

    private List<double[][]> _lastInput;

    public ConvolutionLayer(int _filtersize, int _stepsize, int _inLength, int _inRows, int _inCols, long SEED, int numFilters, double learningRate) {
        this._filtersize = _filtersize;
        this._stepsize = _stepsize;
        this._inLength = _inLength;
        this._inRows = _inRows;
        this._inCols = _inCols;
        this.SEED = SEED;
        _learningRate = learningRate;
        generateRandomFilters(numFilters);

    }

    private void generateRandomFilters(int numFilters) {
        List<double[][]> filters = new ArrayList<>();
        Random r = new Random(SEED); // Seeded random

        for (int n = 0; n < numFilters; n++) { // For every filter
            double[][] newFilter = new double[_filtersize][_filtersize];

            for (int i = 0; i < _filtersize; i++) { // For every row of new filter
                for (int j = 0; j < _filtersize; j++) { // For every column of new filter
                    double value = r.nextGaussian(); // Gaussian random  = value
                    newFilter[i][j] = value; // Slot of filter replaced with value.
                }
            }

            filters.add(newFilter);

        }

        _filters = filters;

    }

    private int _filtersize;
    private int _stepsize; // step of pixels per operation

    private int _inLength;
    private int _inRows;
    private int _inCols;
    private double _learningRate;

    public List<double[][]> convolutionForwardPass(List<double[][]> list) {

        _lastInput = list;

        List<double[][]> output = new ArrayList<>();

        for (int m = 0; m < list.size(); m++) {
            for (double[][] filter : _filters) {
                output.add(convolve(list.get(m), filter, _stepsize));
            }
        }

        return output;

    }

    private double[][] convolve(double[][] input, double[][] filter, int stepSize) {
        int outRows = (input.length - filter.length)/stepSize + 1;
        int outCols = (input[0].length - filter[0].length)/stepSize + 1;

        int inRows = input.length;
        int inCols = input[0].length;

        int fRows = filter.length;
        int fCols = filter[0].length;

        double[][] output = new double[outRows][outCols];


        // For keeping track of O11, O12, O21, O22, etc (incremenet)
        int outRow = 0;
        int outCol;

        for (int i = 0; i <= inRows - fRows; i += stepSize) { // inRows - fRows accounts for the window shifting less than or equal to to account for the very most edge.

            outCol = 0;

            for (int j = 0; j <= inCols - fCols; j+= stepSize) {

                double sum = 0.0;

                //Apply filter around this position
                for (int x = 0; x < fRows; x++) {
                    for (int y = 0; y < fCols; y++) {
                        int inputRowIndex = i + x; // i index adjusted by position of x within filter.
                        int inputColIndex = j + y; // ditto above but accounting for position of y w/in filter.

                        double value = filter[x][y] * input[inputRowIndex][inputColIndex]; // O calculation
                        sum+= value;

                    }
                }

                output[outRow][outCol] = sum;
                outCol++; // GO through columns (Window/Filter over Image)

            }

            outRow++; // GO through rows (Window/Filter over Image)

        }

        return output;

    }

    public double[][] spaceArray(double[][] input) {
        if(_stepsize == 1) {
            return input;
        }

        int outRows = (input.length - 1) * _stepsize + 1;
        int outCols = (input[0].length - 1) * _stepsize + 1;

        double[][] output = new double[outRows][outCols];

        for(int i = 0; i < input.length; i++) {
            for (int j = 0; j < input[0].length; j++) {
                output[i * _stepsize][j * _stepsize] = input[i][j];
            }
        }

        return output;

    }
    @Override
    public double[] getOutput(List<double[][]> input) {
        List<double[][]> output = convolutionForwardPass(input);
        return _nextLayer.getOutput(output);
    }

    @Override
    public double[] getOutput(double[] input) {
        List<double[][]> matrixInput = vectorToMatrix(input, _inLength, _inRows, _inCols);
        return getOutput(matrixInput);
    }

    @Override
    public void backPropagation(double[] dldO) {
        List<double[][]> matrixInput = vectorToMatrix(dldO, _inLength, _inRows, _inCols);
        backPropagation(matrixInput);
    }

    @Override
    public void backPropagation(List<double[][]> dldO) {

        List<double[][]> filtersDelta = new ArrayList<>();
        List<double[][]> dldOPreviousLayer = new ArrayList<>();

        for (int f = 0; f < _filters.size(); f++) {
            filtersDelta.add(new double[_filtersize][_filtersize]);
        }

        for(int i = 0; i < _lastInput.size(); i++) {

            double[][] errorForInput = new double[_inRows][_inCols];

            for (int f = 0; f < _filters.size(); f++) {
                double[][] currfilter = _filters.get(f);
                double[][] error = dldO.get(i * _filters.size() + f);

                double[][] spacedError = spaceArray(error);
                double[][] dldF = convolve(_lastInput.get(i), spacedError, 1);

                double[][] delta = multiply(dldF, _learningRate*-1);
                double[][] newTotalDelta = add(filtersDelta.get(f), delta);
                filtersDelta.set(f, newTotalDelta); //Sums all filters errors for each filter and each input

                double[][] flippedError = flipArrayHorizontal(flipArrayVertical(spacedError));
                errorForInput = add(errorForInput, fullConvolve(currfilter, flippedError));

            }

            dldOPreviousLayer.add(errorForInput);
        }


        //update filters
        for (int f = 0; f < _filters.size(); f++) {
            double[][] modified = add(filtersDelta.get(f), _filters.get(f)); // Current filter added to our change (deltaFilter)
            _filters.set(f, modified);
        }

        if(_previousLayer != null) {
            _previousLayer.backPropagation(dldOPreviousLayer);
        }

    }

    public double[][] flipArrayHorizontal(double[][] array) {
        int rows = array.length;
        int cols = array[0].length;

        double[][] output = new double[rows][cols];

        for (int i = 0; i < rows; i ++) {
            for (int j = 0; j < cols; j++) {
                output[rows - i - 1][j] = array[i][j];
            }
        }
        return output;
    }

    public double[][] flipArrayVertical(double[][] array) {
        int rows = array.length;
        int cols = array[0].length;

        double[][] output = new double[rows][cols];

        for (int i = 0; i < rows; i ++) {
            for (int j = 0; j < cols; j++) {
                output[i][cols - j - 1] = array[i][j];
            }
        }
        return output;
    }

    private double[][] fullConvolve(double[][] input, double[][] filter) {
        int outRows = (input.length + filter.length)+ 1;
        int outCols = (input[0].length + filter[0].length) + 1;

        int inRows = input.length;
        int inCols = input[0].length;

        int fRows = filter.length;
        int fCols = filter[0].length;

        double[][] output = new double[outRows][outCols];


        // For keeping track of O11, O12, O21, O22, etc (incremenet)
        int outRow = 0;
        int outCol;

        for (int i = -fRows + 1; i < inRows; i++) { // inRows - fRows accounts for the window shifting less than or equal to to account for the very most edge.

            outCol = 0;

            for (int j = -fCols + 1; j < inCols; j++) {

                double sum = 0.0;

                //Apply filter around this position
                for (int x = 0; x < fRows; x++) {
                    for (int y = 0; y < fCols; y++) {
                        int inputRowIndex = i + x; // i index adjusted by position of x within filter.
                        int inputColIndex = j + y; // ditto above but accounting for position of y w/in filter.

                        if (inputRowIndex >= 0 && inputColIndex >= 0 && inputRowIndex < inRows && inputColIndex < inCols) {
                            double value = filter[x][y] + input[inputRowIndex][inputColIndex];
                            sum+= value;
                        }
                    }
                }

                output[outRow][outCol] = sum;
                outCol++; // GO through columns (Window/Filter over Image)

            }

            outRow++; // GO through rows (Window/Filter over Image)

        }

        return output;

    }

    @Override
    public int getOutputLength() {
        return _filters.size() * _inLength; // 8 filters, 10 images = 80 outputs.
    }

    @Override
    public int getOutputRows() {
        return (_inRows - _filtersize)/_stepsize + 1;
    }

    @Override
    public int getOutputCols() {
        return (_inCols - _filtersize)/_stepsize + 1;
    }

    @Override
    public int getOutputElements() {
        return getOutputLength() * getOutputRows() * getOutputCols();
    }
}
