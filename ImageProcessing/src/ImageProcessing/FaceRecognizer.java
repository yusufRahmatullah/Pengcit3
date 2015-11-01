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
    private static final int[][] dirs = {{-1,-1}, {0,-1}, {1,-1}, {-1,0}, 
        {1, 0}, {-1, 1}, {0, 1}, {1, 1}};
    private static final int[][] ops = {{0,7}, {1,6}, {2,5}, {3,4}};
    
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
     * Cara penggunaan
    public static void main (String[] args) {
        FaceRecognizer fr = new FaceRecognizer();
        BufferedImage output = fr.homogenDifference(input);
        output = fr.crossDifference(input);
    }
    * */
}
