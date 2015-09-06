///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package com.yugiri.tools;
//
//import java.awt.Image;
//import java.awt.image.BufferedImage;
//import java.io.File;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Set;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import javax.imageio.ImageIO;
//
///**
// *
// * @author Yusuf
// */
//public class Equalizer {
//    private static final int imageSize = 505;
//
//    private static BufferedImage bufferedImage;
//    private static String propertyText;
//    private static Image displayedImage;
//    private static HashMap<Integer, Integer> colorMap;
//
//    /**
//     * Set property text with the newText
//     * @param newText
//     */
//    public static void setPropertyText(String newText) {
//        propertyText = newText;
//    }
//
//    /**
//     * return property text
//     * @return
//     */
//    public static String getPropertyText() {
//        return propertyText;
//    }
//
//    /**
//     * Append newLine into property text
//     * @param newLine
//     */
//    public static void appendPropertyText(String newLine) {
//        propertyText += "\n" + newLine;
//    }
//
//    public static Image getDisplayedImage() {
//        return displayedImage;
//    }
//
//    /**
//     * Open image file and store scaled image into displayed image
//     * use getDisplayedImage() to get scaled image
//     * @param file
//     */
//    public static void openImageFile(File file) {
//        propertyText = "Filename : " + file.getName();
//        propertyText += "\nFile's path : "+ file.getAbsolutePath();
//
//        try {
//            bufferedImage = ImageIO.read(file);
//            bufferedImage = getEqualiser();
//            propertyText += "\nWidth : " + bufferedImage.getWidth();
//            propertyText += "\nHeight : " + bufferedImage.getHeight();
//
//            // resize image
//                if (bufferedImage.getWidth() >= imageSize ||
//                        bufferedImage.getHeight() >= imageSize) {
//                    float scaleW = (float)imageSize / (float)bufferedImage.getWidth();
//                    float scaleH = (float)imageSize / (float)bufferedImage.getWidth();
//                    if (scaleW > scaleH) {
//                        int newWidth = (int)(scaleH * bufferedImage.getWidth());
//                        int newHeight = (int)(scaleH * bufferedImage.getHeight());
//                        displayedImage = bufferedImage.getScaledInstance(newWidth, newHeight, Image.SCALE_FAST);
//                    } else {
//                        int newWidth = (int)(scaleW * bufferedImage.getWidth());
//                        int newHeight = (int)(scaleW * bufferedImage.getHeight());
//                        displayedImage = bufferedImage.getScaledInstance(newWidth, newHeight, Image.SCALE_FAST);
//                    }
//                } else {
//                    displayedImage = bufferedImage;
//                }
//        } catch (IOException ex) {
//            Logger.getLogger(View.class.getName()).log(Level.SEVERE, null, ex);
//            displayedImage = null;
//            propertyText = "File not an image!";
//        }
//    }
//
//    /**
//     * Count color difference
//     */
//    public static void  colorDifference () {
//        colorMap = new HashMap<>();
//        if (bufferedImage != null) {
//            for (int i=0; i<bufferedImage.getHeight(); i++) {
//                for (int j=0; j<bufferedImage.getWidth(); j++) {
//                    int rgb = bufferedImage.getRGB(j, i);
//                    if (colorMap.containsKey(rgb)) {
//                        colorMap.put (rgb, 1 + (int)colorMap.get(rgb));
//                    } else {
//                        colorMap.put (rgb, 1);
//                    }
//                }
//            }
//            propertyText += "\nDifferent Color : " + colorMap.size();
//        }
//    }
//
//    public static void showHashKey () {
//        if (colorMap != null) {
//            Set<Integer> keySet = colorMap.keySet();
//            for (Integer key : keySet) {
//                propertyText += "\n" + Integer.toHexString(key);
//            }
//        }
//    }
//
//    public static ArrayList getGrayScale(){
//        ArrayList<ArrayList> ret = new ArrayList();
//        if (bufferedImage != null) {
//            for (int i=0; i<bufferedImage.getHeight(); i++) {
//                ArrayList tempGray  = new ArrayList();
//                for (int j=0; j<bufferedImage.getWidth(); j++) {
//                    int rgb = bufferedImage.getRGB(j, i);
//
//                    int r = (rgb >> 16) & 0xFF;
//                    int g = (rgb >> 8) & 0xFF;
//                    int b = (rgb & 0xFF);
//
//                    int grayLevel = (r + g + b) / 3;
//
//                    tempGray.add(grayLevel);
//                }
//                ret.add(tempGray);
//            }
//        }
//        return ret;
//    }
//
//    private static ArrayList getColorFreq  (ArrayList<ArrayList> input){
//        ArrayList ret = new ArrayList<>();
//        for(int i=0 ; i<256 ; i++){
//            ret.add(0);
//        }
//        for(int i=0;i<input.size();i++){
//            ArrayList temp = input.get(i);
//            //nambahin kemunculan warna
//            for(int j=0;j<temp.size();j++){
//                int col = (int) temp.get(j);
//                ret.set(col, ((int)ret.get(col))+1);
//            }
//        }
//        return ret;
//    }
//
//    private static ArrayList getCumulativeFreq(ArrayList colorFreq){
//        ArrayList ret = new ArrayList<>();
//        int cumulative = 0;
//        for(int i=0;i<colorFreq.size();i++){
//            cumulative += (int)colorFreq.get(i);
//            ret.add(cumulative);
//        }
//        return ret;
//    }
//
//    private static ArrayList getFeq(int cumulative){
//        int feq = cumulative / 256;
//        int rem = cumulative % 256;
//        ArrayList ret = new ArrayList<>();
//        for(int i=0 ; i<256 ; i++){
//            if(rem>0){
//                ret.add(feq+1);
//                rem--;
//            }
//            else{
//                ret.add(feq);
//            }
//        }
//        return ret;
//    }
//
//    public static ArrayList getLookUpTable(){
//        ArrayList<ArrayList> grayScale = getGrayScale();
//        ArrayList colorFreq = getColorFreq(grayScale);
//        ArrayList cumulativeFreq = getCumulativeFreq(colorFreq);
//        ArrayList feq = getFeq((int) (cumulativeFreq.get(cumulativeFreq.size()-1)));
//        ArrayList cumulativeFeq = getCumulativeFreq(feq);
//
//        for(int i=0;i<cumulativeFeq.size();i++){
//            System.out.println(cumulativeFeq.get(i));
//        }
//
//        ArrayList ret = new ArrayList();
//
//        int indexCumulativeFeq = 0 ;
//        for(int i=0;i<cumulativeFreq.size();i++){
//            while((int)cumulativeFreq.get(i) > (int)cumulativeFeq.get(indexCumulativeFeq)){
//                indexCumulativeFeq++;
//            }
//                ret.add(indexCumulativeFeq);
//        }
//        return ret;
//    }
//
//    public static BufferedImage getEqualiser(){
//        BufferedImage ret = new BufferedImage(bufferedImage.getWidth(),bufferedImage.getHeight(),BufferedImage.TYPE_INT_RGB);
//        ArrayList<ArrayList> grayScale = getGrayScale();
//
//        ArrayList lookUpTable = getLookUpTable();
//
//        for(int i=0;i<grayScale.size();i++){
//            ArrayList temp = grayScale.get(i);
//            for(int j=0;j<temp.size();j++){
//                int map = (int)lookUpTable.get((int)temp.get(j));
//                ret.setRGB(j, i, map << 16 | map << 8 | map);
//            }
//        }
//        return ret;
//    }
//
//}
