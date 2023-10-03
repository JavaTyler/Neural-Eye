package layers;

import java.util.List;
import java.util.Random;

public class FullyConnectedLayer extends Layer{

    private long SEED;
    private double[][] _weights;

    public FullyConnectedLayer(int _inLength, int _outLength, long SEED) {
        this._inLength = _inLength;
        this._outLength = _outLength;
        this.SEED = SEED;

        _weights = new double[_inLength][_outLength];
        setRandomWeights();
    }

    // We have our x's being passed to our z's. Say 4 x's we will multiply that with the weights matrix
    // To get 3 z's. This is a forward pass. We're passing everything forward.
    // The idea is that z[j] will be summed by the products of the input data (x[i]) and weights (weights are stored in a 2d matrix)
    // z[j], in this example, have a summation of 4 products, multiplied by their corresponding weights.
    public double[] fullyConnectedForwardPass(double[] input) {

        double[] z = new double[_outLength]; // To next pass
        double[] out = new double[_outLength];

        for (int i = 0; i < _inLength; i++) { // Length of current neurons (going through input nodes)
            for (int j = 0; j < _outLength; j++) { // Length of next neurons (going through output nodes)
                z[j] += input[i] * _weights[i][j]; // input as vector, weights as matrix. (Makes sense due to x1-4 * w1-3 mapping (12 maps if 4/3 hence matrix).
            }
        }

        // We go through everything again; reLu activation function on all outputs, this
        // helps get rid of anything less than 0 by setting it to 0 so we don't have negative
        // weight. This helps improve the gradient propogation (wont saturate in both directions)
        // Much more efficient than sigmoid function, gets rid of unncessary calculations.
        // Pretty much the neural network standard as of 2017 for the activation function.
        for (int j = 0; j < _outLength; j++) {
            out[j] = reLu(z[j]);
        }

        return out;

    }

    public double reLu(double input) {
        if (input <= 0) {
            return 0;
        }
        else return input;
    }

    private int _inLength;
    private int _outLength;

    @Override
    public double[] getOutput(List<double[][]> input) {
        double[] vector = matrixToVector(input);
        return getOutput(vector);
    }

    @Override
    public double[] getOutput(double[] input) {
        double[] forwardPass = fullyConnectedForwardPass(input);
        if ( _nextLayer != null) {
            return _nextLayer.getOutput(forwardPass); // Recursive (go forward forward forward till no more == result.
        } else {
            return forwardPass;
        }
    }

    @Override
    public void backPropogation(double[] dldD) {

    }

    @Override
    public void backPropogation(List<double[][]> dldD) {

    }

    @Override
    public int getOutputLength() {
        return 0;
    }

    @Override
    public int getOutputRows() {
        return 0;
    }

    @Override
    public int getOutputCols() {
        return 0;
    }

    @Override
    public int getOutputElements() {
        return 0;
    }

    public void setRandomWeights() {
        Random rand = new Random(SEED);

        for (int i = 0; i < _inLength; i++) {
            for (int j = 0; j < _outLength; j++) {
                _weights[i][j] = rand.nextGaussian();
            }
        }
    }

}
