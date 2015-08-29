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
            propertyText += "\nWidth : " + bufferedImage.getWidth();
            propertyText += "\nHeight : " + bufferedImage.getHeight();

            // resize image
                if (bufferedImage.getWidth() >= imageSize ||
                        bufferedImage.getHeight() >= imageSize) {
                    float scaleW = (float)imageSize / (float)bufferedImage.getWidth();
                    float scaleH = (float)imageSize / (float)bufferedImage.getWidth();
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
    public static void colorDifference () {
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
}
