package data;

import java.util.List;

public class mainapp {
    public static void main(String[] args) {
        List<Image> images = new DataReader().readData("data/mnist_test.csv");
        System.out.printf(images.get(0).toString());
    }
}
