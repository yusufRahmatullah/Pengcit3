package com.yugiri.tools;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Riady
 */
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Point {

    private int x = 0;
    private int y = 0;
    private int cluster_number = 0;

    public Point(int x, int y)
    {
        this.setX(x);
        this.setY(y);
    }
    
    public void setX(int x) {
        this.x = x;
    }
    
    public int getX()  {
        return this.x;
    }
    
    public void setY(int y) {
        this.y = y;
    }
    
    public int getY() {
        return this.y;
    }
    
    public void setCluster(int n) {
        this.cluster_number = n;
    }
    
    public int getCluster() {
        return this.cluster_number;
    }
    
    //Calculates the distance between two points.
    protected static double distance(Point p, Point centroid) {
        double y1 = (double)centroid.getY();
        double y2 = (double)p.getY();
        double x1 = (double)centroid.getX();
        double x2 = (double)p.getX();
        return Math.sqrt(Math.pow((y1 - y2), 2) + Math.pow((x1 - x2), 2));
    }
    
    //Creates random point
    
    
    public String toString() {
    	return "("+x+","+y+")";
    }
}
