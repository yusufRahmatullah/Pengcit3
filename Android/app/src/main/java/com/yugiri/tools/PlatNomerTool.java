/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yugiri.tools;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;


/**
 *
 * @author Yusuf
 */
public class PlatNomerTool {
    private static final int imageSize = 505;

    private static Bitmap bufferedImage;
    private static Bitmap temporaryImage;
    private static String propertyText;
    private static Bitmap displayedImage;
    private static HashMap<Integer, Integer> colorMap;
    public static List<String> chainCodeList;
    public static List<String> kodeBelokList;

    public static final String[] model = {"0754321",
            "060642021",
            "076506421234520",
            "076545076542012340123420",
            "06064642421",
            "06460765432012342",
            "0645607654321",
            "064242",
            "076576543213210",
            "0765420124321"};

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
    
    public static Bitmap getDisplayedImage() {
        return displayedImage;
    }
    
    /**
     * Count color difference
     */
    public static void  colorDifference () {
        colorMap = new HashMap<>();
        if (bufferedImage != null) {
            for (int i=0; i<bufferedImage.getHeight(); i++) {
                for (int j=0; j<bufferedImage.getWidth(); j++) {
                    int rgb = bufferedImage.getPixel(j, i);
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
    
    /**
     * recognize all object using contour tracing
     * @param image input image
     * @return list of chain codes
     */
    public static ArrayList<String> getChainCodes(Bitmap image) {
        ArrayList<String> chainCodes = new ArrayList();
        int direction;
        boolean isStop;
        StringBuilder chainCode;
        // iterate image
        for (int i=0; i<image.getHeight(); i++) {
            for (int j=0; j<image.getWidth(); j++) {
                // check if the current pixel is black and start tracing
                if (image.getPixel(j, i) == 0xFF000000) {
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
                                if (image.getPixel(xByDir(nextDir, a),
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
                    if(chainCode.toString().length() > 300)
                        chainCodes.add(normalizeChaincode(chainCode.toString()));
                }
            }
        }
        return chainCodes;
    }
    
    /**
     * call getChainCodes(BufferedImage) using it's displayedImage then append
     * list of chain code to property text
     */
    public static void analyzeImage(Bitmap image) {
        ArrayList<String> cc = getChainCodes(image);
        for (int i=0; i<cc.size(); i++) {
            propertyText += "\n" + i + ": " + cc.get(i);
        }
    }
    
    /**
     * four directional recursive flood fill
     * @param image input image
     * @param x x coordinate
     * @param y y coordinate
     * @param tgtColor fill color
     */
    public static void floodFill(Bitmap image, boolean[][] mark, int x,
            int y, int srcColor, int tgtColor) {
        // x and y inside the image
        if (x < 0) return;
        if (y < 0) return;
        if (x >= image.getWidth()) return;
        if (y >= image.getHeight()) return;
        
        // make sure this pixel hasn't been visited
        if (mark[y][x]) return;
        
        // make sure this pixel is right color
        if (image.getPixel(x, y) != srcColor) return;
        
        // fill pixel and mark it
        image.setPixel(x, y, tgtColor);
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
     * call getChainCodes to get list of chain code  then analyze every
     * chain code with statisticalAnalyze(String) then append object's property
     * to property text
     */
    public static String statisticalAnalyze(Bitmap bitmap) {
        StringBuilder detectedNumberBuilder = new StringBuilder();
        ArrayList<String> chainCodes = getChainCodes(bitmap);
        chainCodes = new ArrayList<>();
        chainCodeList.addAll(chainCodes);
        for (int i=0; i<chainCodes.size(); i++) {
            String normalizedChainCode = normalizeChaincode(chainCodes.get(i));
            String direction = getDirection(normalizedChainCode);
            detectedNumberBuilder.append(analyzeNumber(direction) + " ");
//            propertyText += "\n" + statisticalAnalyze(chainCodes.get(i));
        }
        return detectedNumberBuilder.toString();
    }

    public static int[] invertPixels(int[] pixels){
        int[] newPixels = new int[pixels.length];
        for(int i=0; i<pixels.length; i++){
            if(pixels[i] == 0xFF000000){
                newPixels[i] = 0xFFFFFFFF;
            }else{
                newPixels[i] = 0xFF000000;
            }
        }
        return newPixels;
    }

    public static Bitmap getBinaryImage(Bitmap bitmap){
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        ArrayList<ArrayList> grayScale = AndroidEqualizer.getGrayScale(pixels, width, height);
        Bitmap greyScaledBitmap = AndroidEqualizer.getGrayScaledBitmap(bitmap, grayScale);
        int[] grayScaledHistogram = AndroidEqualizer.getGrayScaledColorFreq(grayScale);
        int threshold = getThreshold(width, height, grayScaledHistogram);
        greyScaledBitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        Bitmap ret = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        for(int i=0; i<pixels.length; i++){
            int b = (pixels[i] & 0xFF);
            if(b > threshold){
                pixels[i] = 0xFF000000;
            }else{
                pixels[i] = 0xFFFFFFFF;
            }
        }
        ret.setPixels(pixels, 0, width, 0, 0, width, height);
        return ret;
    }

    public static Bitmap scaleBitmap(Bitmap bitmap){
        int maxWidth = 1024;
        int maxHeight = 1024;
        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int newWidth = -1;
        int newHeight = -1;
        float multFactor = -1.0F;
        if(originalHeight > originalWidth) {
            newHeight = maxHeight;
            multFactor = (float) originalWidth/(float) originalHeight;
            newWidth = (int) (newHeight*multFactor);
        } else if(originalWidth > originalHeight) {
            newWidth = maxWidth;
            multFactor = (float) originalHeight/ (float)originalWidth;
            newHeight = (int) (newWidth*multFactor);
        } else if(originalHeight == originalWidth) {
            newHeight = maxHeight;
            newWidth = maxWidth;
        }
        Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false);
        return newBitmap;
    }

    private static int getThreshold(int width, int height, int[] histogram) {

        int total = width * height;

        float sum = 0;
        for(int i=0; i<256; i++) sum += i * histogram[i];

        float sumB = 0;
        int wB = 0;
        int wF = 0;

        float varMax = 0;
        int threshold = 0;

        for(int i=0 ; i<256 ; i++) {
            wB += histogram[i];
            if(wB == 0) continue;
            wF = total - wB;

            if(wF == 0) break;

            sumB += (float) (i * histogram[i]);
            float mB = sumB / wB;
            float mF = (sum - sumB) / wF;

            float varBetween = (float) wB * (float) wF * (mB - mF) * (mB - mF);

            if(varBetween > varMax) {
                varMax = varBetween;
                threshold = i;
            }
        }

        return threshold;

    }

    public static String normalizeChaincode(String chaincode){
        char lastChar, replacement;
        int startIndex;
        int counter;
        int threshold = getThreshold(chaincode);
        char[] code = chaincode.toCharArray();
        lastChar = code[0];
        replacement = code[0];
        startIndex = 0;
        for(int i=1; i<code.length; i++){
            if(code[i] != lastChar){
                counter = 0;
                int j = i;
                lastChar = code[i];
                startIndex = i;
                while (code[j] == lastChar) {
                    counter++;
                    j++;
                    if(j>=code.length)
                        break;
                }
                if (counter < threshold) {
                    for (int n = startIndex; n < j; n++) {
                        code[n] = replacement;
                    }
                    i = j - 1;
                }else{
                    if(j < code.length) {
                        replacement = code[j-1];
                    }
                }
            }
        }

        System.out.println("ChainCode \n" + chaincode);
        System.out.println("Threshold " + threshold);
        System.out.println("Normalized " + String.valueOf(code));
        return String.valueOf(code);
    }

    public static int getThreshold(String chainCode){
        int counter = 1;
        char lastChar;
        char[] code = chainCode.toCharArray();
        List<Integer> lengths = new ArrayList<>();
        lastChar = code[0];
        for(int i=1; i<code.length; i++){
            if(code[i] != lastChar){
                lastChar = code[i];
                if(counter > 3)
                    lengths.add(counter);
                counter = 1;
            }else{
                counter++;
            }
        }
        int sum = 0;
        int n = 0;
        for(Integer i : lengths){
            sum += i;
            n++;
        }
        return sum / n;
    }

//    public static String normalizeChaincode(String chaincode){
//        String ret = "";
//        ret += chaincode.charAt(0);
//        for(int i=1;i<chaincode.length()-2;i++){
//            if(ret.charAt(i-1)!=chaincode.charAt(i) && chaincode.charAt(i+1)!=chaincode.charAt(i)){
//                ret += ret.charAt(i-1);
//            }
//            else{
//                ret += chaincode.charAt(i);
//            }
//        }
//		if(chaincode.length() >= 2)
//			ret += chaincode.charAt(chaincode.length()-2);
//        return ret;
//    }

    public static String getDirection(String chaincode){
        String ret = "";
        ret+=chaincode.charAt(0);
        for(int i=1;i<chaincode.length();i++){
            if(chaincode.charAt(i)!=chaincode.charAt(i-1)){
                ret+=chaincode.charAt(i);
            }
        }
        return ret;
    }

    public static int getDiffPoint(String direction, String curModel){

        if(direction.length() == 0 ){
            return curModel.length();
        }
        else if (curModel.length() == 0){
            return direction.length();
        }
        else{
            int add = getDiffPoint(direction,curModel.substring(0, curModel.length()-1)) + 1;
            int del = getDiffPoint(direction.substring(0, direction.length() - 1), curModel) + 1;
            int rep = getDiffPoint(direction.substring(0, direction.length()-1),curModel.substring(0, curModel.length()-1));

            if(add <= del && add <= rep){
                return add;
            }
            else if(del <= add && del <= rep){
                return del;
            }
            else{
                if(direction.charAt(direction.length()-1) == curModel.charAt(curModel.length()-1)){
                    return rep;
                }
                else{
                    return rep+1;
                }
            }
        }
    }

    public static void analyzeCodeBelok(){
        kodeBelokList = new ArrayList<>();
        for(String chainCode : chainCodeList){
            String kodeBelok = getKodeBelok(chainCode);
            kodeBelokList.add(kodeBelok);
        }
    }

    public static String getKodeBelok(String chaincode){
        String kodeBelok = "";
        for(int i=1;i<chaincode.length();i++){
            if((chaincode.charAt(i) - chaincode.charAt(i-1)) > 0 && ((chaincode.charAt(i) - chaincode.charAt(i-1)) < 4) || (chaincode.charAt(i) - chaincode.charAt(i-1)) > -8 && ((chaincode.charAt(i) - chaincode.charAt(i-1)) < -4)){
                kodeBelok += '0';
            }
            else if((chaincode.charAt(i) - chaincode.charAt(i-1)) < 0 && ((chaincode.charAt(i) - chaincode.charAt(i-1)) > -4) || (chaincode.charAt(i) - chaincode.charAt(i-1)) < 8 && ((chaincode.charAt(i) - chaincode.charAt(i-1))  > 4)){
                kodeBelok += '1';
            }
        }
        return kodeBelok;
    }

    public static int analyzeNumber(String direction){
        int [] point = new int[10];
        for(int i=0;i<10;i++){
            point[i] = direction.compareTo(model[i]);
        }
        int ret = 0;
        for(int i=1;i<10;i++){
            if(point[ret]>point[i]){
                ret = i;
            }
        }

        return ret;
    }
}
