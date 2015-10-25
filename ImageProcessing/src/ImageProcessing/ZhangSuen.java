/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ImageProcessing;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 *
 * @author YusufR
 */
public class ZhangSuen {
    final static int[][] nbrs = {{0,-1}, {1,-1}, {1,0}, 
           {1,1}, {0,1}, {-1,1}, 
           {-1,0}, {-1,-1}, {0,-1}};
    final static int[][][] nbrGroups = {{{0,2,4}, {2,4,6}}, {{0,2,6}, {0,4,6}}};
    private boolean[][] binaryImage;
    private ArrayList<Point> toWhite;
    
    /**
     * Buat ngelakuin thinning pake algoritma zhang suen.
     * hasil thinning ditaro dalam boolean[][] terus diubah
     * jadi image lagi oleh method buildImage
     * @param input image yang belu di-thinning
     * @return image hasil thinning
     */
    public BufferedImage thinImage(BufferedImage input) {
        buildBinaryImage(input);
        toWhite = new ArrayList<>();
        boolean hasChange, firstStep = false;
        int rows = input.getHeight();
        int cols = input.getWidth();
        int nn, nt;
        
        do {
            hasChange = false;
            firstStep = !firstStep;
            
            for (int r=1; r < rows - 1; r++) {
                for (int c=1; c < cols - 1; c++) {
                    nt = numTransition(r, c);
                    nn = numNeighbors(r, c);
                    if (binaryImage[r][c] == false)
                        continue;
                    if (nn < 2 || nn > 6) 
                        continue;
                    if (nt != 1)
                        continue;
                    if (!atLeastOneIsWhite(r, c, firstStep ? 0 : 1))
                        continue;
                    
                    toWhite.add(new Point(c, r));
                    hasChange = true;
                }
            }
            
            for (Point p : toWhite)
                binaryImage[p.y][p.x] = false;
            toWhite.clear();
        } while(hasChange || firstStep);
        
        return buildImage(input.getWidth(), input.getHeight(), input.getType());
    }

    /**
     * menghitung jumlah tetangga pada suatu titik pada baris r dan kolom c
     * @param r baris dari titik 
     * @param c kolom dari titik
     * @return jumlah tetangga yang bersesuaian
     */
    private int numNeighbors(int r, int c) {
        int count = 0;
        for (int i=0; i < nbrs.length - 1; i++) {
            if (binaryImage[r + nbrs[i][1]][c + nbrs[i][0]] == true)
                count++;
        }
        return count;
    }

    /**
     * menghitung jumlah transisi yang mungkin dari titik pada baris r dan kolom c
     * @param r baris dari titik
     * @param c kolom dari titik
     * @return 
     */
    private int numTransition(int r, int c) {
        int count = 0;
        for (int i = 0; i < nbrs.length - 1; i++) {
            if (binaryImage[r + nbrs[i][1]][c + nbrs[i][0]] == false) {
                if (binaryImage[r + nbrs[i+1][1]][c + nbrs[i+1][0]] == true)
                    count++;
            }
        }
        return count;
    }

    /**
     * mengembalikan true jika terdapat lebih dari 1 titik putih di sekitar 
     * titik pada baris r dan kolom c dengan tetangga yang ditentukan oleh step
     * @param r baris pada titik
     * @param c kolom dari titik
     * @param step tetangga yang bersesuaian, 0 atau 1
     * @return 
     */
    private boolean atLeastOneIsWhite(int r, int c, int step) {
        int count = 0;
        int[][] group = nbrGroups[step];
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < group[i].length; j++) {
                int[] nbr = nbrs[group[i][j]];
                if (binaryImage[r + nbr[1]][c + nbr[0]] == false) {
                    count++;
                    break;
                }
            }
        }
        return count > 1;
    }

    /**
     * Mengubah input image menjadi boolean[][]
     * @param input image input
     */
    private void buildBinaryImage(BufferedImage input) {
        binaryImage = new boolean[input.getHeight()][input.getWidth()];
        for (int i = 0; i<input.getHeight(); i++) {
            for (int j = 0; j<input.getWidth(); j++) {
                if (input.getRGB(j, i) == 0xFFFFFFFF) {
                    binaryImage[i][j] = false;
                } else {
                    binaryImage[i][j] = true;
                }
            }
        }
    }

    /**
     * mengubah kembali boolean[][] menjadi image
     * @param width lebar image
     * @param height tinggi image
     * @param type tipe image
     * @return 
     */
    private BufferedImage buildImage(int width, int height, int type) {
        BufferedImage output = new BufferedImage(width, height, type);
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (binaryImage[i][j]) {
                    output.setRGB(j, i, 0xFF000000);
                } else {
                    output.setRGB(j, i, 0xFFFFFFFF);
                }
            }
        }
        return output;
    }
    
    /**
     * cara pengunaan pada driver
     */
    /*
    public void main(String[] args) {
        BufferedImage input = new BuffreredImage(10,10,BufferedImage.TYPE_INT_ARGB);
        ZhangSuen zs = new ZhangSuen();
        BufferedImage output = zs.thinImage(input);
    }
    */
}