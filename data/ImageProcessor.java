package data;

import java.awt.*;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;


public class ImageProcessor {

    // rgb(255,255,255) is white, expected background
    // Program to search for primary color range (mean estimate) if less than val<128 target is towards black (0,0,0)
    // If mean estimate shows more dark 128<val then

    // later convert to List<double[][]> data = new ArrayList<>() to pass in multiple images.
    private double[][] data;
    // ArrayLists are not necessary for this program's base model as we will only process one image at a time.
    // In the future everything will be multithreaded & working with ArrayLists(when needed); (maybe altered to multithreading with FILO structure)

    // later convert to take input as a scanner input;
    // later convert to get a list of images (overload?)
    public static int[][][] readImage(String filepath) throws IOException {

        //filepath = "/C:/Users/minot/Downloads/testimage.jpg";
        BufferedImage img = ImageIO.read(new File(filepath));
        // Convert from bufferedImage to intImage representation
        int[][][] intImg = biToInt(img); // Image of 3d array, where we have an x,y mapping for each R, G, B.
        // Convert from intImage to grayscaleIntImage representation via Average method.
        int[][][] gintImg = intImgToGrayscale(intImg);
        // Next problems:
        // II. How do we map out the values on the piece of paper?
        // Possible Soln II:
        // III. How do we size the mapping to a 28x28 grid to be processed by neural network.
        // Possible Soln III:

        return gintImg;

    }

    public static int[][] pullData(String filepath) throws IOException {

        int[][][] gintImg = readImage(filepath);
        //System.out.println(Arrays.deepToString(gintImg)); WORKS
        int[][] sGIntImg = gintImg[0]; // Pulls first 2d array of gintImg and convers to sGIntImg (small grayscale integer image) which is better since we really do not need a 3d array for this.
        //System.out.println(Arrays.deepToString(sGIntImg)); WORKS
        int[][] heatmap = pullNumbers(sGIntImg);
        //System.out.println(Arrays.deepToString(heatmap)); WORKS
        int[][] resizedheatmap = resize(heatmap);
        //System.out.println(Arrays.deepToString(resizedheatmap)); // BROKEN
        return resizedheatmap;

    }

    // Help attained through chat GPT.
    public void arr2d2CSV(int[][] resizedheatmap) {
        FileWriter csvWriter = null;
        try {
            csvWriter = new FileWriter("new.csv");

            for (int[] row : resizedheatmap) {
                String[] strRow = new String[row.length];
                for (int i = 0; i < row.length; i++) {
                    strRow[i] = String.valueOf(row[i]);
                }
                csvWriter.append(String.join(",", strRow));
                csvWriter.append("\n");
            }

            System.out.println("CSV file created successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int[][] resize(int[][] heatmap) {
        int[][][] temp = new int[3][heatmap.length][heatmap[0].length];
        for (int i = 0; i < heatmap.length; i++) {
            for (int j = 0; j < heatmap[0].length; j++) {
                temp[0][i][j] = heatmap[i][j];
                temp[1][i][j] = heatmap[i][j];
                temp[2][i][j] = heatmap[i][j]; //Store same value so as a BufferedImage of A-RGB it is still grayscale.

            }
        }
        BufferedImage temp2 = int2bi(temp);

        BufferedImage biresize;


            //System.out.println("reading image");
            //BufferedImage bi = ImageIO.read(new File("clown.png"));

           // System.out.println("resizing image to 1/4 the size of the original");
            biresize = new BufferedImage(temp2.getWidth() / 4, temp2.getHeight() / 4, temp2.getType());
            Graphics2D g2dresize = (Graphics2D)biresize.getGraphics();
            g2dresize.drawImage(temp2,  0,  0,  temp2.getWidth() / 4,  temp2.getHeight() / 4,  0,  0,  temp2.getWidth(), temp2.getHeight(), null, null);


        // System.out.println(Arrays.deepToString(temp)); WORKS

        /*BufferedImage temp2 = int2bi(temp);
        BufferedImage newImage = new BufferedImage(28, 28, BufferedImage.TYPE_INT_RGB);

        Graphics g = newImage.createGraphics();
        g.drawImage(temp2, 0, 0, 28, 28, null);
        g.dispose();

        int[][][] temp3 = biToInt(newImage);
        // FOUND THE ERROR HERE
        int[][] temp4 = new int[temp3[0].length][temp3[0][0].length];
        for (int i = 0; i < temp3.length; i++) {
            for (int j = 0; j < temp3[0].length; j++) {
                temp4[i][j] = temp3[0][i][j]; // Getting one color channel, doesn't matter since all vals are the same per color (Grayscale)
            }
        }
/*
        return temp4;*/
        int[][][] temp3 = biToInt(biresize);

        return new int[0][];

    }


    private static int[][] pullNumbers(int[][] sGIntImg) {

        // Step 1: Determine a significant value
        int[] significance = getSignificance(sGIntImg);

        // Step 2: Find values via significance.
        if (significance[1] == 0) { // Looking for dark value (significance) on light paper (noise)
            int key = significance[0];

            // We want to take the compliment so we can treat everything as 255 for significance, and 0 for insignificance rather than being represented as greyscale, we convert to 'heatmap.'

            for (int i = 0 ; i < sGIntImg.length; i++) {
                for (int j = 0; j < sGIntImg[0].length; i++) {
                    sGIntImg[i][j] -= 255;
                   sGIntImg[i][j] = Math.abs(sGIntImg[i][j]); // Converts to looking for light value on dark paper (heatmap).
                }
            }

        }

        // Step 3: We have heatmaps in both ways now, set all insignificant values to 0.
        // Note: Heatmaps are 255 for max significance, and 0 for least.

        for (int i = 0; i < sGIntImg.length; i++) {
            for (int j = 0; j < sGIntImg[0].length; j++) {

                if(sGIntImg[i][j] < significance[0]) { // If value of heatmap is insignificant. (Aids AI).
                    sGIntImg[i][j] = 0;
                }

            }
        }

        return sGIntImg;


    }

    private static int[] getSignificance(int[][] sGIntImg) {
        int sum = 0;
        for (int i = 0; i < sGIntImg.length; i++) { //For all rows
            for (int j = 0; j < sGIntImg[0].length; j++) { // For all columns (goes through all values of the row)
                sum += sGIntImg[i][j]; // Stores value of 0-255, sums
            }
        }

        int eval = sum/(sGIntImg.length * sGIntImg[0].length); // Will be some value [0, 255] likely leaning towards the background color (if white, then high values, if dark background, then low values).

        // Default: 0
        final int BIAS = 0; // Bias to account for potential coloration issues if a shadow is present in the image.
        // Default: 128
        final int PROMINENCE = 128; // Prominence is the value you set for determining if we have a lighter background or darker background (darker background means lighter colors are what we want, lighter background means darker colors are what we want).

        // PROMINENCE - BIAS = upper bound noise. (We have a light background).

        final int EVALBIAS = 16; // Account for variance

        // If background is lighter color (128 is gray, 144 is lighter gray deviating towards whtie (128 + 16 bias).
        if (eval > PROMINENCE - BIAS) {
            // We have a light background
            return new int[]{eval - EVALBIAS, 0}; // look for darker significance
        }
        else {
            // We have a dark background (less than prominence)
            return new int[]{eval + EVALBIAS, 1}; // look for lighter significance
        }

    }

    // Code Borrowed from Jared Castillo (Jared sourced from Dr. Reinhart CSC-410 (Image Processing)).
    private static int[][][] biToInt(BufferedImage i) {
        int[][][] intI = new int[3][i.getHeight()][i.getWidth()];
        for (int y = 0; y < i.getHeight(); y++) {
            for (int x = 0; x < i.getWidth(); x++) {
                int argb = i.getRGB(x, y);
                intI[0][y][x] = (argb >> 16) & 0xFF; // R
                intI[1][y][x] = (argb >> 8) & 0xFF; // G
                intI[2][y][x] = (argb >> 0) & 0xFF; // B
            }
        }

        return intI;

    }

    // Code given by Dr. Reinhart (for int2bi)
    public static BufferedImage int2bi(int[][][] intimg) {

        BufferedImage bi = new BufferedImage(intimg[0][0].length, intimg[0].length, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < bi.getHeight(); ++y) {

            for (int x = 0; x < bi.getWidth(); ++x) {

                int rgb = (intimg[0][y][x] << 16) |

                        (intimg[1][y][x] << 8) |

                        (intimg[2][y][x] << 0);

                bi.setRGB(x, y, rgb);

            }

        }

        return bi;

    }

    private static int[][][] intImgToGrayscale(int[][][] intImg) {
        int[][][] gIntImg = new int[1][intImg[0].length][intImg[0][0].length];
        for(int y = 0; y < intImg[0].length; y++) {
            for (int x = 0; x < intImg[0][0].length; x++) {
                // Sums all 3 RGB-int values and divides by 3 to generate a 'grayscale' representation
                // For a pixel at y,x.
                // (R + G + B)/3
                //gIntImg[0][y][x] += (intImg[0][y][x] + intImg[1][y][x] + intImg[2][y][x])/3; Older calculation; Less accurate to the real world.
                gIntImg[0][y][x] += (0.299 * intImg[0][y][x]) + (0.587 * intImg[1][y][x]) + (0.114 * intImg[2][y][x]);
                // Due to more green in world this is more accurate: (0.587)G + (0.299)R + (0.114)B
            }
        }

        return gIntImg;
    }

}