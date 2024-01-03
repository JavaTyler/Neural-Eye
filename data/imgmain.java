package data;

import java.io.IOException;
import java.util.Arrays;

public class imgmain extends ImageProcessor{
    public static void main(String[] args) throws IOException { //Main to test ImageProcessor class
        int[][] resizedimg = pullData("/C:/Users/minot/Downloads/testimage.jpg");
        System.out.println(Arrays.deepToString(resizedimg));
    }
}
