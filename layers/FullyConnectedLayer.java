package layers;

import java.util.List;
import java.util.Random;

public class FullyConnectedLayer extends Layer{

    private long SEED;
    private final double leak = 0.01;
    private double[][] _weights;
    private int _inLength;
    private int _outLength;
    private double _learningrate;

    private double[] lastZ;
    private double[] lastX;

    public FullyConnectedLayer(int _inLength, int _outLength, long SEED, double _learningrate) {
        this._inLength = _inLength;
        this._outLength = _outLength;
        this.SEED = SEED;
        this._learningrate = _learningrate;

        _weights = new double[_inLength][_outLength];
        setRandomWeights();
    }

    // We have our x's being passed to our z's. Say 4 x's we will multiply that with the weights matrix
    // To get 3 z's. This is a forward pass. We're passing everything forward.
    // The idea is that z[j] will be summed by the products of the input data (x[i]) and weights (weights are stored in a 2d matrix)
    // z[j], in this example, have a summation of 4 products, multiplied by their corresponding weights.
    public double[] fullyConnectedForwardPass(double[] input) {

        lastX = input;

        double[] z = new double[_outLength]; // To next pass
        double[] out = new double[_outLength];

        for (int i = 0; i < _inLength; i++) { // Length of current neurons (going through input nodes)
            for (int j = 0; j < _outLength; j++) { // Length of next neurons (going through output nodes)
                z[j] += input[i] * _weights[i][j]; // input as vector, weights as matrix. (Makes sense due to x1-4 * w1-3 mapping (12 maps if 4/3 hence matrix).
            }
        }

        lastZ = z; // for back prop

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

    // derivative of reLu
    public double dreLu(double input) {
        if (input <= 0) {
            return leak; // Sometimes weights do not change at all, so we return a leak (a very small flaw).
        }
        else return 1;
    }



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


    // For back-propagation we want to get our Weights and subtract the Weight losses (dL/dw)
    // derivative of L / derivative of w. Derivative of L with respect to w. Essentially how did
    // The layer result get influenced by the weight.
    // We apply chain rule to find dL/dw... = dL/dO // Loss of the entire layer from the output
    // TIMES, dO/dZ how much the output depended on our Z inputs (function before reLu input node(i) * weights(i,j))
    // TIMES dZ/dw how much Z depended on w (weights).
    // dL/dw = (dL/dO) * (dO/dZ) * (dZ/dw)

    // Keep in mind the previous layer will give us dL/dOj
    // reLu function is 0 if z < 0 or z if z >= 0. Hence the derivative O' = 0 z < 0, and 1 if z >= 0.
    // reLu derivative = dOj/dZj (j is a subscript).
    // Once again Z is the calculation done to yield the result (input i * weight of i*j). // Weights are matrix because nodes are vertexes remember.


    // dZ/dw or dZj/ dwij where i,j are subscripts, = xi. Its just the input.
    // i are the rows, j are the columns.

    @Override
    public void backPropagation(double[] dldO) {

        double[] dldx = new double[_inLength];

        double dOdz; // derivative of 0 / derivative of z // Output affected by weights * input. (
        double dzdw; // xi
        double dldw; // Used to update weight.
        double dzdx; // wij

        for (int k = 0; k < _inLength; k++) { // For each of our input nodes

            double dldx_sum = 0;

            for (int j = 0; j < _outLength; j++) { // For each of our output nodes
                dOdz = dreLu(lastZ[j]);
                dzdw = lastX[k]; // dz/dw = xi
                dzdx = _weights[k][j]; // dz/dx = wkj

                dldw = dldO[j] * dOdz * dzdw;

                _weights[k][j] -= dldw*_learningrate;

                dldx_sum += dldO[j] * dOdz * dzdx; // Sum because for all non-previous nodes, we want to find
            }

            dldx[k] = dldx_sum;
        }

        if (_previousLayer != null) {
            _previousLayer.backPropagation(dldx);
        }

    }

    // How about finding an error for the previous layer? (We go all the way back hence, back propagation).
    // dL/dX = dL/dO * dO/dZ * dZ/dX
    // dZj / dXi = wij.
    // dL/dXi = sum(dL/dOj * dOj/dZj * dZj/dXi
    @Override
    public void backPropagation(List<double[][]> dldO) {
        double[] vector = matrixToVector(dldO);
        backPropagation(vector);
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
        return _outLength;
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
