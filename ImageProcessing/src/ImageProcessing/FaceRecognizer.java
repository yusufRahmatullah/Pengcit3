/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ImageProcessing;

import java.awt.image.BufferedImage;

/**
 *
 * @author YusufR
 */
public class FaceRecognizer {
    // daftar koefisien untuk memudahkan perhitungan
    private static final int[][] dirs = {{-1,-1}, {0,-1}, {1,-1}, {-1,0}, 
        {1, 0}, {-1, 1}, {0, 1}, {1, 1}};
    private static final int[][] ops = {{0,7}, {1,6}, {2,5}, {3,4}};
    private static final int[][] position = {
        {-1,-1}, {0,-1}, {1,-1}, 
        {-1,0},  {0,0},  {1, 0}, 
        {-1, 1}, {0, 1}, {1, 1}};
    private static final int[][] operatorDirection = {
        {0,1,2,3,4,5,6,7,8},
        {1,2,5,0,4,8,3,6,7},
        {2,5,8,1,4,7,0,3,6},
        {5,8,7,2,4,6,1,0,3},
        {6,7,8,3,4,5,0,1,2},
        {3,6,7,0,4,8,1,2,5},
        {0,3,6,1,4,7,2,5,8},
        {1,0,3,2,4,6,5,8,7}};
    private static final int[][] robertPosition = {{4,5,7,8},{5,4,8,7}};
    
    private static final double sqrt2 = Math.sqrt(2);
    private static final double fcf = 1.0 / (2.0 * sqrt2);
    private static final double bbf = 1.0/9.0;
    private static final double gbf = 1.0/16.0;
    private static final double sobelf = 1.0/4.0;
    private static final double prewitf = 1.0/3.0;
    private static final double freichanf = 1.0/(2.0 + sqrt2);
    
    // daftar operator untuk edge detection
    public static final double[] SOBEL_OPERATOR = {-1 * sobelf,-2 * sobelf,-1 * sobelf,0,0,0,sobelf,2 * sobelf,1 * sobelf};
    public static final double[] SCHARR_OPERATOR = {-3 * gbf,-10 * gbf,-3 * gbf,0,0,0,3 * gbf,10 * gbf,3 * gbf};
    public static final double[] PREWIT_OPERATOR = {-1 * prewitf,-1 * prewitf,-1 * prewitf,0,0,0,prewitf,prewitf,prewitf};
    public static final int[] KIRSCH_OPERATOR = {-3, -3, -3, -3, 0, -3, 5, 5, 5};
    public static final double[] FREI_CHAN_OPERATOR = {-1*freichanf, -1*sqrt2*freichanf, -1*freichanf, 0, 0, 0, freichanf, freichanf*sqrt2, freichanf};
    public static final int[] ROBERT_CROSS_OPERATOR = {1, 0, 0, -1};
    // daftar operator untuk edge detection tanpa melakukan perputaran (sekali hitung)
    public static final int[] DIAGONAL_LAPLACIAN = {1, 0, -1, 0, 0, 0, -1, 0, 1};
    public static final int[] FOUR_DIRECTIONAL_LAPLACIAN = {0,1,0,1,-4,1,0,1,0};
    public static final int[] EIGHT_DIRECTIONAL_LAPLACIAN = {-1,-1,-1,-1,8,-1,-1,-1,-1};
    public static final int[] DIAMOND_LAPLACIAN_1 = {1, -2, 1, -2, 4, -2, 1, -2, 1};
    public static final int[] DIAMOND_LAPLACIAN_2 = {-2, 1, -2, 1, 4, 1, -2, 1, -2};
    
    // daftar operator untuk melakukan sharpening dan blur
    private static final int[] SHARPEN = {0, -1, 0, -1, 5, -1, 0, -1, 0};
    public static final double[] BOX_BLUR = {bbf, bbf, bbf, bbf, bbf, bbf, bbf, bbf, bbf};
    public static final double[] GAUSSIAN_BLUR = {gbf, 2*gbf, gbf, 2*gbf, 4*gbf, 2*gbf, gbf, 2*gbf, gbf};
    
    // penggunaan algoritma untuk derajat satu atau dua
    public static final int FIRST_ORDER = 1;    // first order menggunakan operatorPosition 6 dan 0
    public static final int SECOND_ORDER = 2;
    
    
    /**
     * mengubah gambar wajah agar mudah dibaca dengan mengubah setiap pixel dengan
     * selisih warna terbesar dengan tetangganya
     * @param image gambar wajah dalam grayscale
     * @return gambar wajah dengan edge yang mudah dibaca
     */
    public BufferedImage homogenDifference (BufferedImage image) {
        BufferedImage output = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        for (int i = 1; i < image.getHeight() - 1; i++) {
            for (int j = 1; j < image.getWidth() - 1; j++) {
                int dif = 0;
                int difIdx = 0;
                for (int k = 0; k < dirs.length; k++) {
                    int nPix = image.getRGB(j + dirs[k][0], i + dirs[k][1]) & 0x000000FF;
                    int curPix = image.getRGB(j, i) & 0x000000FF;
                    int tempDif = Math.abs(curPix - nPix);
                    if (tempDif > dif)
                    {
                        dif = tempDif;
                        difIdx = k;
                    }   
                }
                //output.setRGB(j, i, image.getRGB(j + dirs[difIdx][0], i + dirs[difIdx][1]));
                int result = 0xFF000000 + dif + (dif << 8) + (dif << 16);
                output.setRGB(j, i, result);
            }
        }
        return output;
    }
    
    /**
     * mengubah wajah agar mudah dibaca dengan mengubah setiap pixel dengan
     * selisih terbesar dari tetangga-tetangga yang saling bersilangan
     * @param image gambar wajah dalam grayscale
     * @return gambar wajah dengan edge yang mudah dibaca
     */
    public BufferedImage crossDifference (BufferedImage image) {
        BufferedImage output = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        for (int i = 1; i < image.getHeight() - 1; i++) {
            for (int j = 1; j < image.getWidth() - 1; j++) {
                int dif = 0;
                int difIdx = 0;
                for (int k = 0; k < ops.length; k++) {
                    int firstPix = image.getRGB(j + dirs[ops[k][0]][0], i + dirs[ops[k][0]][1]) & 0xFF;
                    int secondPix = image.getRGB(j + dirs[ops[k][1]][0], i + dirs[ops[k][1]][1]) & 0xFF;
                    int tempDif = Math.abs(firstPix - secondPix);
                    if (tempDif > dif) {
                        dif = tempDif;
                        difIdx = k;
                    }
                }
                int result = 0xFF000000 + (dif << 16) + (dif << 8) + dif;
                output.setRGB(j, i, result);
            }
        }
        return output;
    }
    
    /**
     * mendeteksi garis pada gambar menggunakan first order atau second order dengan
     * operator sobel, scharr, prewit, atau robert
     * @param image gambar dalam grayscale
     * @param order FaceRecognizer.FIRST_ORDER untuk first order dan 
     * FaceRecognizer.SECOND_ORDER untuk second order
     * @param operator FaceRecognizer.SOBEL_OPERATOR untuk sobel
     * FaceRecognizer.PREWIT_OPERATOR untuk prewit
     * FaceRecognizer.SCHARR_OPERATOR untuk scharr
     * FaceRecognizer.ROBERT_CROSS_OPERATOR untuk robert
     * @return gambar dengan garis yang terdeteksi
     */
    public BufferedImage edgeDetection(BufferedImage image, int order, int[] operator) {
        BufferedImage output = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        for (int i = 1; i < image.getHeight() - 1; i++) {
            for (int j = 1; j < image.getWidth() - 1; j++) {
                int newValue = 0;
                // perhitungan untuk Robert Cross Operator
                if (operator == ROBERT_CROSS_OPERATOR) {
                    int sumH = 0;
                    int sumV = 0;
                    // arah horisontal
                    for (int k = 0; k < 4; k++) {
                        int opdir = robertPosition[0][k];
                        sumH += (image.getRGB(j + position[opdir][0], i + position[opdir][1]) & 0xFF)
                                * operator[k];
                    }
                    // perhitungan untuk arah vertikal
                    for (int k = 0; k < 4; k++) {
                        int opdir = robertPosition[1][k];
                        sumV += (image.getRGB(j + position[opdir][0], i + position[opdir][1]) & 0xFF)
                                * operator[k];
                    }
                    //newValue = Math.max(sumH, sumV);
                    newValue = Math.max(Math.abs(sumH), Math.abs(sumV));
                }
                // perhitungan untuk first order (menggunakan arah 6 dan 0)
                else if (order == FIRST_ORDER) {
                    int sumH = 0;
                    int sumV = 0;
                    // perhitungan untuk arah horisontal
                    for (int k = 0; k < 9; k++) {
                        int opdir = operatorDirection[6][k];
                        sumH += (image.getRGB(j + position[opdir][0], i + position[opdir][1]) & 0xFF)
                                * operator[k];
                    }
                    // perhitungan untuk arah vertikal
                    for (int k = 0; k < 9; k++) {
                        int opdir = operatorDirection[0][k];
                        sumV += (image.getRGB(j + position[opdir][0], i + position[opdir][1]) & 0xFF)
                                * operator[k];
                    }
                    //newValue = Math.max(sumH, sumV);
                    newValue = Math.max(Math.abs(sumH/4), Math.abs(sumV/4));
                }
                // perhitungan untuk second order
                else if (order == SECOND_ORDER) {
                    int sumMax = 0;
                    for (int k = 0; k < operatorDirection.length; k++) {
                        int sum = 0;
                        for (int l = 0; l < operatorDirection[k].length; l++) {
                            int opdir = operatorDirection[k][l];
                            sum += (image.getRGB(j + position[opdir][0], i + position[opdir][1]) & 0xFF)
                                    * operator[l];
                        }
                        if (sum > sumMax) {
                            sumMax = sum;
                        }
                    }
                    newValue = sumMax;
                }
                
                // assign nilai pixel baru
                newValue = 0xFF000000 + (newValue << 16) + (newValue << 8) + newValue;
                output.setRGB(j, i, newValue);
            }
        }
        return output;
    }
    
    /**
     * mendeteksi garis pada gambar menggunakan first order atau second order dengan
     * operator frei-chan
     * @param image gambar dalam grayscale
     * @param order FaceRecognizer.FIRST_ORDER untuk first order dan 
     * @param operator FaceRecognizer.FREI_CHAN_OPERATOR untuk frei-chan
     * @return gambar dengan garis yang terdeteksi
     */
    public BufferedImage edgeDetection(BufferedImage image, int order, double[] operator) {
        BufferedImage output = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        for (int i = 1; i < image.getHeight() - 1; i++) {
            for (int j = 1; j < image.getWidth() - 1; j++) {
                int newValue = 0;
                // perhitungan untuk first order (menggunakan arah 6 dan 0)
                if (order == FIRST_ORDER) {
                    double sumH = 0;
                    double sumV = 0;
                    // perhitungan untuk arah horisontal
                    for (int k = 0; k < 9; k++) {
                        int opdir = operatorDirection[6][k];
                        sumH += (image.getRGB(j + position[opdir][0], i + position[opdir][1]) & 0xFF)
                                * operator[k];
                    }
                    // perhitungan untuk arah vertikal
                    for (int k = 0; k < 9; k++) {
                        int opdir = operatorDirection[0][k];
                        sumV += (image.getRGB(j + position[opdir][0], i + position[opdir][1]) & 0xFF)
                                * operator[k];
                    }
                    //newValue = (int) Math.round(Math.max(sumH, sumV));
                    newValue = (int) Math.round(Math.max(Math.abs(sumH), Math.abs(sumV)));
                }
                // perhitungan untuk second order
                else if (order == SECOND_ORDER) {
                    double sumMax = 0;
                    for (int k = 0; k < operatorDirection.length; k++) {
                        double sum = 0;
                        for (int l = 0; l < operatorDirection[k].length; l++) {
                            int opdir = operatorDirection[k][l];
                            sum += (image.getRGB(j + position[opdir][0], i + position[opdir][1]) & 0xFF)
                                    * operator[l];
                        }
                        if (sum > sumMax) {
                            sumMax = sum;
                        }
                    }
                    newValue = (int) Math.round(sumMax);
                }
                // assign nilai pixel baru
                newValue = 0xFF000000 + (newValue << 16) + (newValue << 8) + newValue;
                output.setRGB(j, i, newValue);
            }
        }
        return output;
    }
    
    /**
     * mendeteksi garis pada gambar menggunakan laplacian operator
     * @param image gambar dalam grayscale
     * @param operator operator laplacian
     * @return gambar dengan garis yang terdeteksi
     */
    public BufferedImage edgeDetection(BufferedImage image, int[] operator) {
        BufferedImage output = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        for (int i = 1; i < image.getHeight() - 1; i++) {
            for (int j = 1; j < image.getWidth() - 1; j++) {
                int newValue = 0;
                for (int k = 0; k < 9; k++) {
                    newValue += (image.getRGB(j + position[k][0], i + position[k][1]) & 0xFF)
                            * operator[k];
                }
                newValue = Math.abs(newValue);
                // assign nilai pixel baru
                newValue = 0xFF000000 + (newValue << 16) + (newValue << 8) + newValue;
                output.setRGB(j, i, newValue);
            }
        }
        return output;
    }
    
    /**
     * membuat gambar menjadi blur
     * @param image gambar dalam grayscale
     * @param blurOperator operator blur (BOX_BLUR atau GAUSSIAN_BLUR)
     * @return gambar setelah di-blur
     */
    public BufferedImage blurImage(BufferedImage image, double[] blurOperator) {
        BufferedImage output = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        for (int i = 1; i < image.getHeight() - 1; i++) {
            for (int j = 1; j < image.getWidth() - 1; j++) {
                int newValue = 0;
                double sum = 0;
                for (int k = 0; k < 9; k++) {
                    sum += (image.getRGB(j + position[k][0], i + position[k][1]) & 0xFF)
                            * blurOperator[k];
                }
                newValue = (int) Math.round(sum);
                // assign nilai pixel baru
                newValue = 0xFF000000 + (newValue << 16) + (newValue << 8) + newValue;
                output.setRGB(j, i, newValue);
            }
        }
        return output;
    }
    
    /**
     * menajamkan gambar
     * @param image gambar dalam grayscale
     * @return gambar yang telah dipertajam
     */
    public BufferedImage sharpenImage(BufferedImage image) {
        BufferedImage output = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        for (int i = 1; i < image.getHeight() - 1; i++) {
            for (int j = 1; j < image.getWidth() - 1; j++) {
                int newValue = 0;
                for (int k = 0; k < 9; k++) {
                    newValue += (image.getRGB(j + position[k][0], i + position[k][1]) & 0xFF)
                            * SHARPEN[k];
                }
                newValue = Math.abs(newValue);
                // assign nilai pixel baru
                newValue = 0xFF000000 + (newValue << 16) + (newValue << 8) + newValue;
                output.setRGB(j, i, newValue);
            }
        }
        return output;
    }
    
    
    /**
    //Cara penggunaan
    public static void main (String[] args) {
        FaceRecognizer fr = new FaceRecognizer();
        BufferedImage input = new BufferedImage(500, 500, BufferedImage.TYPE_INT_ARGB);
        BufferedImage output = fr.homogenDifference(input);
        output = fr.crossDifference(input);
        // face detection
        output = fr.edgeDetection(input, FaceRecognizer.FIRST_ORDER, FaceRecognizer.SOBEL_OPERATOR);
        output = fr.edgeDetection(input, FaceRecognizer.SECOND_ORDER, FaceRecognizer.SOBEL_OPERATOR);
        output = fr.edgeDetection(input, FaceRecognizer.FIRST_ORDER, FaceRecognizer.SCHARR_OPERATOR);
        output = fr.edgeDetection(input, FaceRecognizer.SECOND_ORDER, FaceRecognizer.SCHARR_OPERATOR);
        output = fr.edgeDetection(input, FaceRecognizer.FIRST_ORDER, FaceRecognizer.PREWIT_OPERATOR);
        output = fr.edgeDetection(input, FaceRecognizer.SECOND_ORDER, FaceRecognizer.PREWIT_OPERATOR);
        output = fr.edgeDetection(input, FaceRecognizer.FIRST_ORDER, FaceRecognizer.ROBERT_CROSS_OPERATOR);
        output = fr.edgeDetection(input, FaceRecognizer.FIRST_ORDER, FaceRecognizer.FREI_CHAN_OPERATOR);
        output = fr.edgeDetection(input, FaceRecognizer.SECOND_ORDER, FaceRecognizer.FREI_CHAN_OPERATOR);
        output = fr.edgeDetection(input, FaceRecognizer.FIRST_ORDER, FaceRecognizer.KIRSCH_OPERATOR);
        output = fr.edgeDetection(input, FaceRecognizer.SECOND_ORDER, FaceRecognizer.KIRSCH_OPERATOR);
        output = fr.edgeDetection(input, FaceRecognizer.DIAGONAL_LAPLACIAN);
        output = fr.edgeDetection(input, FaceRecognizer.DIAMOND_LAPLACIAN_1);
        output = fr.edgeDetection(input, FaceRecognizer.DIAMOND_LAPLACIAN_2);
        output = fr.edgeDetection(input, FaceRecognizer.FOUR_DIRECTIONAL_LAPLACIAN);
        output = fr.edgeDetection(input, FaceRecognizer.EIGHT_DIRECTIONAL_LAPLACIAN);
        // image refinement
        output = fr.blurImage(input, FaceRecognizer.BOX_BLUR);
        output = fr.blurImage(input, FaceRecognizer.GAUSSIAN_BLUR);
        output = fr.sharpenImage(input);
    }
    */
    
}
