/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ImageProcessing;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author Yusuf
 */
public class Controller {
    private static final int imageSize = 505;
    
    private static BufferedImage bufferedImage;
    private static BufferedImage temporaryImage;
    private static String propertyText;
    private static Image displayedImage;
    private static HashMap<Integer, Integer> colorMap;
    
    /**
     * Set property text with the newText
     * @param newText 
     */
    public static void setPropertyText(String newText) {
        propertyText = newText;
    }
    
    /**
     * return property text
     * @return 
     */
    public static String getPropertyText() {
        return propertyText;
    }
    
    /**
     * Append newLine into property text
     * @param newLine 
     */
    public static void appendPropertyText(String newLine) {
        propertyText += "\n" + newLine;
    }
    
    public static Image getDisplayedImage() {
        return displayedImage;
    }
    
    /**
     * Open image file and store scaled image into displayed image
     * use getDisplayedImage() to get scaled image
     * @param file 
     */
    public static void openImageFile(File file) {
        propertyText = "Filename : " + file.getName();
        propertyText += "\nFile's path : "+ file.getAbsolutePath();

        try {
            bufferedImage = ImageIO.read(file);
            //bufferedImage = getEqualiser();
            propertyText += "\nWidth : " + bufferedImage.getWidth();
            propertyText += "\nHeight : " + bufferedImage.getHeight();
            // resize image
                if (bufferedImage.getWidth() >= imageSize ||
                        bufferedImage.getHeight() >= imageSize) {
                    float scaleW = (float)imageSize / (float)bufferedImage.getWidth();
                    float scaleH = (float)imageSize / (float)bufferedImage.getHeight();
                    if (scaleW > scaleH) {
                        int newWidth = (int)(scaleH * bufferedImage.getWidth());
                        int newHeight = (int)(scaleH * bufferedImage.getHeight());
                        displayedImage = bufferedImage.getScaledInstance(newWidth, newHeight, Image.SCALE_FAST);
                    } else {
                        int newWidth = (int)(scaleW * bufferedImage.getWidth());
                        int newHeight = (int)(scaleW * bufferedImage.getHeight());
                        displayedImage = bufferedImage.getScaledInstance(newWidth, newHeight, Image.SCALE_FAST);
                    }
                } else {
                    displayedImage = bufferedImage;
                }
        } catch (IOException ex) {
            Logger.getLogger(View.class.getName()).log(Level.SEVERE, null, ex);
            displayedImage = null;
            propertyText = "File not an image!";
        }
    }
    
    /**
     * Count color difference
     */
    public static void  colorDifference () {
        colorMap = new HashMap<>();
        if (bufferedImage != null) {
            for (int i=0; i<bufferedImage.getHeight(); i++) {
                for (int j=0; j<bufferedImage.getWidth(); j++) {
                    int rgb = bufferedImage.getRGB(j, i);
                    if (colorMap.containsKey(rgb)) {
                        colorMap.put (rgb, 1 + (int)colorMap.get(rgb));
                    } else {
                        colorMap.put (rgb, 1);
                    }
                }
            }
            propertyText += "\nDifferent Color : " + colorMap.size();
        }
    }
    
    public static void showHashKey () {
        if (colorMap != null) {
            Set<Integer> keySet = colorMap.keySet();
            for (Integer key : keySet) {
                propertyText += "\n" + Integer.toHexString(key);
            }
        }
    }
    
    public static ArrayList getGrayScale(){
        ArrayList<ArrayList> ret = new ArrayList();
        if (bufferedImage != null) {
            for (int i=0; i<bufferedImage.getHeight(); i++) {
                ArrayList tempGray  = new ArrayList();
                for (int j=0; j<bufferedImage.getWidth(); j++) {          
                    int rgb = bufferedImage.getRGB(j, i);
                    
                    int r = (rgb >> 16) & 0xFF;
                    int g = (rgb >> 8) & 0xFF;
                    int b = (rgb & 0xFF);

                    int grayLevel = (r + g + b) / 3;
                    
                    tempGray.add(grayLevel);
                }
                ret.add(tempGray);
            }
        }
        return ret;
    }
    
    private static ArrayList getColorFreq  (ArrayList<ArrayList> input){
        ArrayList ret = new ArrayList<>(); 
        for(int i=0 ; i<256 ; i++){
            ret.add(0);
        }
        for(int i=0;i<input.size();i++){
            ArrayList temp = input.get(i);
            //nambahin kemunculan warna
            for(int j=0;j<temp.size();j++){
                int col = (int) temp.get(j);
                ret.set(col, ((int)ret.get(col))+1);
            }
        }
        return ret;
    }
    
    private static ArrayList getCumulativeFreq(ArrayList colorFreq){
        ArrayList ret = new ArrayList<>();
        int cumulative = 0;
        for(int i=0;i<colorFreq.size();i++){
            cumulative += (int)colorFreq.get(i);
            ret.add(cumulative);
        }     
        return ret;
    }
    
    private static ArrayList getFeq(int cumulative){
        int feq = cumulative / 256;
        int rem = cumulative % 256;
        ArrayList ret = new ArrayList<>();
        for(int i=0 ; i<256 ; i++){
            if(rem>0){
                ret.add(feq+1);
                rem--;
            }
            else{
                ret.add(feq);
            }
        }
        return ret;
    }
    
    public static ArrayList getLookUpTable(){
        ArrayList<ArrayList> grayScale = getGrayScale();
        ArrayList colorFreq = getColorFreq(grayScale);
        ArrayList cumulativeFreq = getCumulativeFreq(colorFreq);
        ArrayList feq = getFeq((int) (cumulativeFreq.get(cumulativeFreq.size()-1)));
        ArrayList cumulativeFeq = getCumulativeFreq(feq);
        
        for(int i=0;i<cumulativeFeq.size();i++){
            System.out.println(cumulativeFeq.get(i));
        }
        
        ArrayList ret = new ArrayList();
        
        int indexCumulativeFeq = 0 ;
        for(int i=0;i<cumulativeFreq.size();i++){
            while((int)cumulativeFreq.get(i) > (int)cumulativeFeq.get(indexCumulativeFeq)){
                indexCumulativeFeq++;
            }
                ret.add(indexCumulativeFeq);
        }
        return ret;
    }
    
    public static BufferedImage getEqualiser(){
        BufferedImage ret = new BufferedImage(bufferedImage.getWidth(),bufferedImage.getHeight(),BufferedImage.TYPE_INT_RGB);
        ArrayList<ArrayList> grayScale = getGrayScale();
        
        ArrayList lookUpTable = getLookUpTable();
        
        for(int i=0;i<grayScale.size();i++){
            ArrayList temp = grayScale.get(i);
            for(int j=0;j<temp.size();j++){
                int map = (int)lookUpTable.get((int)temp.get(j));
                ret.setRGB(j, i, map << 16 | map << 8 | map);
            }
        }
        return ret;
    }
    
    /**
     * recognize all object using contour tracing
     * @param image input image
     * @return list of chain codes
     */
    public static ArrayList<String> analyzeImage(BufferedImage image) {
        ArrayList<String> chainCodes = new ArrayList();
        int direction;
        boolean isStop;
        StringBuilder chainCode;
        // iterate image
        for (int i=0; i<image.getHeight(); i++) {
            for (int j=0; j<image.getWidth(); j++) {
                // check if the current pixel is black and start tracing
                if (image.getRGB(j, i) == 0xFF000000) {
                    int a = j, b = i;
                    isStop = false;
                    direction = 7;
                    chainCode = new StringBuilder();
                    while (!isStop) {
                        // check neighbor and assign direction from 0 to 7
                        boolean isNextDirFound = false;
                        int nextDir = (direction + 3)%8;    // starting point
                        int sweepCount = 0;     // 8 in once radial sweep
                        while (!isNextDirFound) {
                            // check what is the nex direction
                            // may throw array index out of bound exception
                            try {
                                if (image.getRGB(xByDir(nextDir, a), 
                                        yByDir(nextDir, b)) == 0xFF000000) {
                                    a = xByDir(nextDir, a);
                                    b = yByDir(nextDir, b);
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
                        chainCode.append(direction);
                        
                        // check if tracing is done
                        if (a == j && b == i) {
                            isStop = true;
                        }
                    }
                    // post-tracing
                    boolean[][] mark = new boolean[image.getHeight()][image.getWidth()];
                    floodFill(image, mark, j, i, 0xFF000000, 0xFFFFFFFF);
                    chainCodes.add(chainCode.toString());
                }
            }
        }
        return chainCodes;
    }
    
    /**
     * call analyzeImage(BufferedImage) using it's displayedImage then append 
     * list of chain code to property text
     */
    public static void analyzeImage() {
        temporaryImage = new BufferedImage(displayedImage.getWidth(null), 
                displayedImage.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        temporaryImage.createGraphics().drawImage(displayedImage, 0, 0, null);
        ArrayList<String> cc = analyzeImage(temporaryImage);
        for (int i=0; i<cc.size(); i++) {
            propertyText += "\n" + i + ": " + cc.get(i);
        }
    }
    
    /**
     * four directional recursive flood fill
     * @param image input image
     * @param x x coordinate
     * @param y y coordinate
     * @param color fill color
     */
    public static void floodFill(BufferedImage image, boolean[][] mark, int x, 
            int y, int srcColor, int tgtColor) {
        // x and y inside the image
        if (x < 0) return;
        if (y < 0) return;
        if (x >= image.getWidth()) return;
        if (y >= image.getHeight()) return;
        
        // make sure this pixel hasn't been visited
        if (mark[y][x]) return;
        
        // make sure this pixel is right color
        if (image.getRGB(x, y) != srcColor) return;
        
        // fill pixel and mark it
        image.setRGB(x, y, tgtColor);
        mark[y][x] = true;
        
        // flood fill recursively
        floodFill(image, mark, x-1, y, srcColor, tgtColor);
        floodFill(image, mark, x+1, y, srcColor, tgtColor);
        floodFill(image, mark, x, y-1, srcColor, tgtColor);
        floodFill(image, mark, x, y+1, srcColor, tgtColor);
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
    
    /**
     * count total angle in an object
     * @param chainCode object's chain code
     * @return number of angle
     */
    private static int angleCount(String chainCode) {
        int nbAngle = 0;
        return nbAngle;
    }
    
    /**
     * count total edge in an object
     * @param chainCode object's chain code
     * @return number of edge
     */
    private static int edgeCount(String chainCode) {
        int nbEdge = 0;
        return nbEdge;
    }
    
    /**
     * convert chain code to relative chain code
     * relative chain code is simplifying chain code
     * ex : sequence of 01212101212 become 1
     * @param chainCode object's chain code
     * @return relative chain code
     */
    private static String relativeChainCode(String chainCode) {
        StringBuilder relCC = new StringBuilder();
        
        return relCC.toString();
    }
    
    /**
     * analyze object by it's statistic
     * @param chainCode object's chain code
     * @return type of object
     */
    private static String statisticalAnalyze(String chainCode) {
        StringBuilder property = new StringBuilder();
        int nbEdge = 1;
        int nbDominantNumber = 0;
        int sum = 0;
        int max = 0;
        int nbZeroOne = 0;
        int[] mapTable = new int[8];
        
        float ratioEdgeDom = 0.0f;
        float mean;
        //float variance = 0.0f;
        
        // init map table
        for (int v : mapTable) {
            v = 0;
        }
        
        // by iterate chain code, we can do : 
        // fill map table
        // count total turning
        for (int i=0; i<chainCode.length(); i++) {
            int val =chainCode.charAt(i)-48; 
            mapTable[val]++;
            
            if (i != 0) {
                if (chainCode.charAt(i) != chainCode.charAt(i-1)) {
                    nbEdge++;
                }
            }
        }
        
        // by iterate chain code, we can do : 
        // count mean, max, and variance
        // count map table that has value 0 or 1
        int sumSquare = 0;
        for (int i=0; i<mapTable.length; i++) {
           sum += mapTable[i];   
            if (mapTable[i] > max) {
                max = mapTable[i];
            }
            //sumSquare += mapTable[i] * mapTable[i];
            if (mapTable[i] == 0 || mapTable[i] == 1) {
                nbZeroOne++;
            }
        }
        mean = (float)sum / (float)mapTable.length;
        //variance = ((float)sumSquare / (float)mapTable.length) - (mean * mean);
        
        // count total dominant number
        for (int i=0; i<mapTable.length; i++) {
            if (mean <= (float)mapTable[i]) {
                nbDominantNumber++;
            }
        }
        
        property.append("\nClass: ").append(classifyObject(nbEdge, nbDominantNumber, nbZeroOne, max, mean));/*
                .append("\nNb Edge: ").append(nbEdge)
                .append("\nNb Dominant Edge: ").append(nbDominantNumber)
                //.append("\nVariance: ").append(variance)
                .append("\nMax: ").append(max)
                .append("\nMean: ").append(mean)
                .append("\nMax per average: ").append((float)max / mean)
                .append("\nNb zero and one : ").append(nbZeroOne);*/
        return property.toString();
    }
    
    /**
     * classify object depend on it's statistic
     * @param nbEdge number of edge
     * @param nbDomEdge number of dominant edge in map table
     * @param nbZeroOne number of zero and one in map table
     * @param max max value in map table
     * @param mean mean of map table
     * @param variance variance of map table
     * @return 
     */
    private static String classifyObject (int nbEdge, int nbDomEdge, 
            int nbZeroOne, int max, float mean) {
        String objectClass = "";
        float ratio = (float)nbEdge / (float)nbDomEdge;
        float maxPerMean = (float)max / mean;
        int domPlusZeroOne = nbDomEdge + nbZeroOne;
        
        if (nbZeroOne == 4) {
            if (ratio == 2) {
                objectClass = "45-deg Rotated Square";
            } else {
                if (Math.abs(maxPerMean - 2.0f) < 0.1f) {
                    objectClass = "Square";
                } else {
                    objectClass = "Rectangle";
                }
            }
        } else if (nbZeroOne == 0) {
            objectClass = "Circle";
        } else if (domPlusZeroOne == 8) {
            objectClass = "Triangle";
        } else if (nbDomEdge >=2 && nbDomEdge <=4 && nbZeroOne <=2) {
            objectClass = "Rotated Triangle";
        } else {
            objectClass = "Undefined";
        }
        return objectClass;
    }
    
    /**
     * call analyzeImage to get list of chain code  then analyze every
     * chain code with statisticalAnalyze(String) then append object's property
     * to property text
     */
    public static void statisticalAnalyze() {
        temporaryImage = new BufferedImage(displayedImage.getWidth(null), 
                displayedImage.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        temporaryImage.createGraphics().drawImage(displayedImage, 0, 0, null);
        ArrayList<String> cc = analyzeImage(temporaryImage);
        for (int i=0; i<cc.size(); i++) {
            propertyText += "\n" + statisticalAnalyze(cc.get(i));
        }
    }
}
