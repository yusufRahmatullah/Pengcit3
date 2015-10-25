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
    private boolean[][] floodFillImage;
    private ArrayList<Point> toWhite;
    private ArrayList<Point> coloredImage;
    private ArrayList<Box> boxes;
    private String result = null;
    
    /**
     * cara pengunaan pada driver
     */
    /*
    public void main(String[] args) {
        BufferedImage input = new BuffreredImage(10,10,BufferedImage.TYPE_INT_ARGB);
        ZhangSuen zs = new ZhangSuen();
        BufferedImage output = zs.thinImage(input);
        String result = zs.toString();
    }
    */
    
    /**
     * Buat ngelakuin thinning pake algoritma zhang suen.
     * hasil thinning ditaro dalam boolean[][]
     * setiap objek dicari rectnya dan di-thinning dengan manggil method thinImageRect()
     * ubah jadi image lagi oleh method buildImage()
     * @param input image yang belum di-thinning
     * @return image hasil thinning
     */
    public BufferedImage thinImage(BufferedImage image) {
        buildBinaryImage(image);
        toWhite = new ArrayList<>();
        coloredImage = new ArrayList<>();
        boolean isStop;
        int direction, minX, maxX, minY, maxY;
        boxes = new ArrayList<>();
         
        // copy ke thinnedImage
        floodFillImage = new boolean[binaryImage.length][binaryImage[0].length];
        for (int i = 0; i < binaryImage.length; i++) {
            for (int j = 0; j < binaryImage[0].length; j++) {
                floodFillImage[i][j] = binaryImage[i][j];
            }
        }
        // cari box setiap objek
        // iterate image
        for (int j=0; j<floodFillImage[0].length; j++) {
            for (int i=0; i<floodFillImage.length; i++) {
                // check if the current pixel is black and start tracing
                if (floodFillImage[i][j]) {
                    int a = j, b = i;
                    isStop = false;
                    direction = 7;
                    maxX = j; minX = j;
                    maxY = i; minY = i;
                    while (!isStop) {
                        // check neighbor and assign direction from 0 to 7
                        boolean isNextDirFound = false;
                        int nextDir = (direction + 3)%8;    // starting point
                        int sweepCount = 0;     // 8 in once radial sweep
                        while (!isNextDirFound) {
                            // check what is the nex direction
                            // may throw array index out of bound exception
                            try {
                                if (floodFillImage[yByDir(nextDir, b)][xByDir(nextDir, a)]) {
                                    a = xByDir(nextDir, a);
                                    b = yByDir(nextDir, b);
                                    if(a > maxX)
                                            maxX = a;
                                    if(a < minX)
                                            minX = a;
                                    if(b > maxY)
                                            maxY = b;
                                    if(b < minY)
                                            minY = b;
                                    isNextDirFound = true;
                                    sweepCount = 0;
                                } else {
                                    nextDir = (nextDir + 7) % 8;
                                    sweepCount++;
                                }
                            } catch (Exception e) {
                                System.out.println(e.getMessage());
                                nextDir = (nextDir + 7) % 8;
                                sweepCount++;
                            } finally {
                                if (sweepCount > 7) {
                                    sweepCount = 0;
                                    isStop = true;
                                    isNextDirFound = true;
                                }
                            }
                        }
                        // after get nextDir
                        // update direction
                        // assign direction to chaincode
                        direction = nextDir;

                        // check if tracing is done
                        if (a == j && b == i) {
                                isStop = true;
                        }
                    }
                    // post-tracing
                    whiteFloodFill(i, j);
                    boxes.add(new Box(minX, maxX, minY, maxY));
                }
            }
        }
                       
        // thinning seluruh gambar dalam boxes dan menuliskan hasil
        String temp = "";
        for (Box b : boxes) {
            temp += thinImageAndGetNumber(b.minX - 1, b.minY - 1, 
                    b.maxX - b.minX + 2, b.maxY - b.minY + 2) + " ";
        }
        //thinImageRect(1, 1, image.getWidth()-2, image.getHeight()-2);
        
        // menuliskan hasil
        result = new String(temp);
        return buildImage(image.getWidth(), image.getHeight(), image.getType());
    }

    @Override
    public String toString() {
        return result;
    }
    
    public String getResult() {
        return result;
    }
    
    /**
     * melakukan thinning pada rect titik x,y dengan ukuran wxh
     * @param x titik awal x
     * @param y titik awal y
     * @param w lebar rect
     * @param h tinggi rect
     * @return angka yang terbaca dari hasil thinning
     * mengembalikan -1 jika tidak terdefinisi
     */
    public int thinImageAndGetNumber(int x, int y, int w, int h) {
        boolean hasChange, firstStep = false;
        int nt, nn;
        do {
            hasChange = false;
            firstStep = !firstStep;
            for (int r = y; r < y+h; r++) {
                for (int c = x; c < x+w; c++) {
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
        } while (hasChange || firstStep);
        
        // extracting feature
        return extractFeature(x, y, w, h);
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
     * menghitung jumlah titik tetangga yang tidak bertetanggaan
     * dari titik pada baris r dan kolom c
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
        /*
        int[][] adirs = {{-1,-1} , {-2, -2}, {-1, 1}, 
            {-2, 2}, {1, -1}, {2, -2}, {1, 1}, {2, 2}, {0,0}};
        for (Point p : coloredImage) {
            for (int i = 0; i < adirs.length; i++) {
                output.setRGB(p.x + adirs[i][0], p.y + adirs[i][1], 0xFFFF0000);
            }
        }
        for (Box b : boxes) {
            for (int i = 0; i < adirs.length; i++) {
                output.setRGB(b.minX + adirs[i][0], b.minY + adirs[i][1], 0xFF00FF00);
            }
        }
        */
        return output;
    }
    
    /**
     * mengembalikan seluruh node rect pada titik paling kiri atas x,y dengan ukuran wxh
     * @param x titik awal x
     * @param y titik awal y
     * @param w lebar rect
     * @param h tinggi rect
     * @return seluruh node pada rect
     */
    private int extractFeature(int x, int y, int w, int h) {
        ArrayList<Point> nodes = new ArrayList<>();
        ArrayList<Point> edges = new ArrayList<>();
        
        // cari setiap objek 
        for (int i = y; i < y+h; i++) {
            for (int j = x; j < x+w; j++) {
                if (binaryImage[i][j]) {
                    // cek jumlah tetangga yang ngga saling bertetanggaan
                    //int nn = numNeighbors(i, j);
                    int nt = numTransition(i, j);
                    // tambahin ke daftar warna
                    if (nt > 2){
                        coloredImage.add(new Point(j, i));
                        nodes.add(new Point(j, i));
                    } else if (nt == 1) {
                        coloredImage.add(new Point(j, i));
                        edges.add(new Point(j, i));
                    }
                }
            }
        }
        
        // analisis fitur
        /*
        0 : 0 nodes 0 edges x
        1 : 0 nodes 2 edges x
        2 : 0 nodes 2 edges x
        3 : 1 nodes 3 edges x
        4 : 1 nodes 3 edges x
        5 : 0 nodes 2 edges x
        6 : 1 nodes 1 edges x
        7 : 0 nodes 2 edges x
        8 : 2 nodes 0 edges x
        9 : 1 nodes 1 edges x
        */
        int n = nodes.size();
        int e = edges.size();
        int ex = -1;
        if (n == 0 && e == 0) {
            ex = 0;
        } else if (n == 2 && e == 0) {
            ex = 8;
        } else if (n == 0 && e == 2) {
            // 1, 2, 5, atau 7
            int dX = Math.abs(edges.get(0).x - edges.get(1).x);
            int dY = Math.abs(edges.get(0).y - edges.get(1).y);
            float ratio = dY/(dX+1);
            if (ratio >= 8.0F) {
                ex = 1;
            } else if (Math.abs(ratio - 2.0f) <= 0.5f) {
                if (edges.get(0).x < edges.get(1).x) {
                    ex = 2;
                } else {
                    ex = 5;
                }
            } else {
                ex = 7;
            }
        } else if (n == 1 && e == 3) {
            // 3, 4
            int dX = Math.abs(nodes.get(0).x - edges.get(0).x);
            int dY = Math.abs(nodes.get(0).y - edges.get(0).y);
            float ratio = dY/(dX+1);
            if (ratio > 1.5F) {
                ex = 4;
            } else {
                ex = 3;
            }
        } else if (n == 1 && e == 1) {
            // 6, 9
            if (nodes.get(0).y > edges.get(0).y) {
                ex = 6;
            } else {
                ex = 9;
            }
        }
        return ex;
    }
    
    private void whiteFloodFill(int r, int c) {
        if (floodFillImage[r][c])
            floodFillImage[r][c] = false;
        for (int i=0; i<nbrs.length - 1; i++) {
            int x = r + nbrs[i][0];
            int y = c + nbrs[i][1];
            if (floodFillImage[x][y])
                whiteFloodFill(x,y);
        }
    }
    
    private void printFeature() {
        int[][] adirs = {{-1, -1}, {0, -1}, {1, -1}, 
            {-1,0}, {0,0}, {1, 0}, 
            {-1, 1}, {0, -1}, {1, 1}};
        for (Point p : coloredImage) {
            System.out.println("i: "+p.x+", j:"+p.y);
            for (int i=0; i<adirs.length; i++) {
                if (i % 3 == 0)
                    System.out.println("");
                char c = 49;
                if (!binaryImage[p.y + adirs[i][0]][p.x + adirs[i][1]]) 
                    c = 48;
                System.out.print(c);
            }
            System.out.println("\n");
        }
    }
    
    /**
	 * get next x coordinate for check next direction
	 * @param curDir current direction
	 * @param curX current x coordinate
	 * @return x coordinate for check next direction
	 */
	private static int xByDir (int curDir, int curX) {
		switch(curDir) {
			case 0 : return curX+1;
			case 1 : return curX+1;
			case 3 : return curX-1;
			case 4 : return curX-1;
			case 5 : return curX-1;
			case 7 : return curX+1;
			default : return curX;
		}
	}

    /**
     * get next y coordinate for check next direction
     * @param curDir current direction
     * @param curY current y coordinate
     * @return y coordinate for check next direction
     */
    private static int yByDir (int curDir, int curY) {
            switch(curDir) {
                    case 1 : return curY-1;
                    case 2 : return curY-1;
                    case 3 : return curY-1;
                    case 5 : return curY+1;
                    case 6 : return curY+1;
                    case 7 : return curY+1;
                    default : return curY;
            }
    }
}