/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yugiri.tools;

import android.content.Context;
import android.graphics.Bitmap;

import com.yugiri.pengcit.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author YusufR
 */
public class FacesLocationDetector {

//    private final static String colorTableLocation = "C:\\Data\\Kuliah\\Semester 7\\Pengcit\\Tugas\\colortable.txt;
    private static List<Integer> colorTable;
    private final static int[][] fxfPosition = {
        {-2,-2},{-1,-2},{0,-2},{1,-2},{2,-2},
        {-2,-1},{-1,-1},{0,-1},{1,-1},{2,-1},
        {-2,0}, {-1,0}, {0,0}, {1,0}, {2,0},
        {-2,1}, {-1,1}, {0,1}, {1,1}, {2,1},
        {-2,2}, {-1,2}, {0,2}, {1,2}, {2,2}};
    private final static int[] gaussianMask = {
        2, 4, 5, 4, 2,
        4, 9,12, 9, 4,
        5,12,15,12, 5,
        4, 9,12, 9, 4,
        2, 4, 5, 4, 2};
    private final static int gaussianDivider = 115;
    private final static int faceThreshold = 100;
    private static boolean[][] faceMap;

    public static Bitmap gaussianBlur(Bitmap input) {
        Bitmap output = Bitmap.createBitmap(input);
        for (int i = 2; i < input.getHeight() - 2; i++) {
            for (int j = 2;j < input.getWidth() - 2; j++) {
                int sum = 0;
                for (int k = 0; k < fxfPosition.length; k++) {
                    sum += (input.getPixel(j + fxfPosition[k][0], i + fxfPosition[k][1]) & 0xFF) * gaussianMask[k];
                }
                sum /= gaussianDivider;
                output.setPixel(j, i, 0xFF000000 + (sum << 16) + (sum << 8) + sum);
            }
        }
        return output;
    }

    public static Bitmap skinDetection(Bitmap input, int threshold, Context context) {
        Bitmap output = Bitmap.createBitmap(input);
        faceMap = new boolean[input.getHeight()][input.getWidth()];
        ArrayList<Box> boxes = new ArrayList<>();

        // load color Table
        loadColorTable(context);

        /**
         * copy image input ke output
         */
        for (int i = 0; i < input.getHeight(); i++) {
            for (int j = 0; j < input.getWidth(); j++) {
                output.setPixel(j, i, input.getPixel(j, i));
            }
        }

        /**
         * bikin faceMap, kalo warna putih, false
         * kalo warnanya masuk ke dalem color table, ubah facemap(i, j) jadi true
         * selain itu, false
         */
        for (int i = 0; i < input.getHeight(); i++) {
            for (int j = 0; j < input.getWidth(); j++) {
                int cur = input.getPixel(j, i);
                if (cur == 0xFFFFFFFF) {
                    faceMap[i][j] = false;
                }
                else if(isOnColorTable(cur, threshold)) {
                    faceMap[i][j] = true;
                } else {
                    faceMap[i][j] = false;
                }
            }
        }

        /**
         * udah dapet faceMap, cek apakah daerah tersebut wajah atau bukan,
         * kalau bukan, ngapain dimasukin
         * kalau bener, ya masukin lah
         */
        for (int i = 0; i < faceMap.length; i++){
            for (int j = 0; j < faceMap[0].length; j++) {
                if (faceMap[i][j]) {
                    Box candidate = boxCandidate(j, i);
                    if (candidate != null) {
                        for (int b = candidate.minX; b <= candidate.maxX; b++) {
                            output.setPixel(b, candidate.minY, 0xFF00FF00);
                            output.setPixel(b, candidate.maxY, 0xFF00FF00);
                        }
                        for (int b = candidate.minY; b <= candidate.maxY; b++) {
                            output.setPixel(candidate.minX, b, 0xFF00FF00);
                            output.setPixel(candidate.maxX, b, 0xFF00FF00);
                        }
                    }
                }
            }
        }


        // debug
        /*
        for (int i = 0; i < faceMap.length; i++) {
            for (int j = 0; j < faceMap[0].length; j++) {
                if (faceMap[i][j])
                    output.setPixel(j, i, 0xFFFFFFFF);
            }
        }
                */

        return output;
     }

    private static void loadColorTable(Context context) {
        try {
			InputStream inputStream = context.getResources().openRawResource(R.raw.colortable);

			InputStreamReader inputreader = new InputStreamReader(inputStream);
			BufferedReader reader = new BufferedReader(inputreader);
            colorTable = new ArrayList<>();
			String line;
			while((line = reader.readLine()) != null){
				colorTable.add((int)Long.parseLong(line, 16));
			}
        } catch (IOException ex) {
            Logger.getLogger(FacesLocationDetector.class.getName()).log(Level.SEVERE, null, ex);
        }
     }

    private static boolean isOnColorTable(int color, int threshold) {
        int i = 0;
        boolean found = false;
        while (!found && i < colorTable.size()) {
            int dr = Math.abs((color & 0xFF0000) - (colorTable.get(i) & 0xFF0000));
            int dg = Math.abs((color & 0xFF00) - (colorTable.get(i) & 0xFF00));
            int db = Math.abs((color & 0xFF) - (colorTable.get(i) & 0xFF));
            int total = dr+dg+db;
            //int total = (dr+dg+db) / 3;
            //int total = dr*dr + dg*dg + db*db;
            if (total <= threshold) {
            //if(dr <= threshold && dg <= threshold && db <= threshold) {
                found = true;
            }
            i++;
        }
        return found;
    }

    private static Box boxCandidate(int i, int j) {
        int counter = 1;
        Box faceBox = null;
        ArrayList<Point> lists = new ArrayList<>();
        int xmin = i, xmax = i, ymin = j, ymax = j;

        lists.add(new Point(i,j));
        while(!lists.isEmpty()) {
            int a = lists.get(0).getX();
            int b = lists.get(0).getY();
            lists.remove(0);
            if (b > 0 && faceMap[b-1][a]) {
                faceMap[b-1][a] = false;
                lists.add(new Point(a, b-1));
                counter++;
                if (ymin > b-1)
                    ymin = b-1;
            }
            if (b < faceMap.length - 2 && faceMap[b+1][a]) {
                faceMap[b+1][a] = false;
                lists.add(new Point(a, b+1));
                counter++;
                if (ymax < b+1)
                    ymax = b+1;
            }
            if (a > 0 && faceMap[b][a-1]) {
                faceMap[b][a-1] = false;
                lists.add(new Point(a-1, b));
                counter++;
                if (xmin > a-1)
                    xmin = a-1;
            }
            if (a < faceMap[0].length - 2 && faceMap[b][a+1]) {
                faceMap[b][a+1] = false;
                lists.add(new Point(a+1, b));
                counter++;
                if (xmax < a+1)
                    xmax = a+1;
            }
        }

        if (counter >= faceThreshold) {
            faceBox = new Box(xmin, xmax, ymin, ymax);
        }

        return faceBox;
    }

    /**
    public static void main(String[] args) {
        FacesLocationDetector fld = new FacesLocationDetector();
        int threshold = 300;    // bisa diatur thresholdnya, sejauh ini yang 300 udah bagus
        // misalkan ada input image dengan variabel input
        Bitmap output = fld.skinDetection(input, threshold);
    }
    * */
}
