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

public class KMeans {

    //Number of Clusters. This metric should be related to the number of points
    private int NUM_CLUSTERS = 4;    
    //Number of Points
    private int NUM_POINTS = 15;
    //Min and Max X and Y
    private static final int MIN_COORDINATE = 0;
    private static final int MAX_COORDINATE = 10;
    
    private List <Point> points;
    public List <Cluster> clusters;
    
    public KMeans(List<Point> _points) {
    	this.points = _points;
    	this.clusters = new ArrayList();    	
    }
    
    public static void main(String[] args) {
    	
    	KMeans kmeans = new KMeans(new ArrayList<Point>());
    	kmeans.init();
    	kmeans.calculate();
    }

    public void init(List<Point> centroids){
        System.out.println("Init centroid");
        int id = 0;
        for(Point p : centroids){
            Cluster cluster = new Cluster(id);
            cluster.setCentroid(p);
            clusters.add(cluster);
            id++;
        }
        plotClusters();
    }

    //Initializes the process
    public void init() {
    	//Create Points
    	
    	//Create Clusters
    	//Set Random Centroids
        
        int maxX = 0;
        int maxY = 0;
        
    	for (int i = 0; i < NUM_CLUSTERS; i++) {
               // System.out.println(i);
    		Cluster cluster = new Cluster(i);
    		
                for(int j=0;j<points.size();j++){
                    if(points.get(j).getX()>maxX){
                        maxX=points.get(j).getX();
                    }
                    if(points.get(j).getY()>maxY){
                        maxY=points.get(j).getY();
                    }
                }
                
                //jadiin centroid awal
                
                boolean done = false;
                for(int j=0;j<points.size() && !done ;j++){
                 //   System.out.println("j="+j);
                    if(i==0){
                        if(points.get(j).getX()<maxX/2 && points.get(j).getY()<maxY/2){
                            cluster.setCentroid(points.get(j));
                            clusters.add(cluster);
                            done = true;
                        }
                    }
                    else if(i==1){
                        if(points.get(j).getX()>maxX/2 && points.get(j).getY()<maxY/2){
                            cluster.setCentroid(points.get(j));
                            clusters.add(cluster);
                            done = true;
                        }
                    }
                    else if(i==2){
                        if(points.get(j).getY()>maxY/2 && points.get(j).getY()< 3* maxY/4){
                            cluster.setCentroid(points.get(j));
                            clusters.add(cluster);
                            done = true;
                        }
                    }
                    else if(i==3){
                        if(points.get(j).getY()> 3 * maxY/4){
                            cluster.setCentroid(points.get(j));
                            clusters.add(cluster);
                            done = true;
                        }
                    }
                }
                

    	}
    	
    	//Print Initial state
    	plotClusters();
    }

    private void plotClusters() {
    	for (int i = 0; i < NUM_CLUSTERS; i++) {
    		Cluster c = clusters.get(i);
    		c.plotCluster();
    	}
    }
    
	//The process to calculate the K Means, with iterating method.
    public void calculate() {
        boolean finish = false;
        int iteration = 0;
        
        // Add in new data, one at a time, recalculating centroids with each new one. 
        while(!finish) {
        	//Clear cluster state
        	clearClusters();
        	
        	List <Point> lastCentroids = getCentroids();
        	
        	//Assign points to the closer cluster
        	assignCluster();
            
            //Calculate new centroids.
        	calculateCentroids();
        	
        	iteration++;
        	
        	List <Point> currentCentroids = getCentroids();
        	
        	//Calculates total distance between new and old Centroids
        	double distance = 0;
        	for(int i = 0; i < lastCentroids.size(); i++) {
        		distance += Point.distance(lastCentroids.get(i),currentCentroids.get(i));
        	}
                System.out.println("distance = "+distance);
        	        	
        	if(distance == 0) {
        		finish = true;
        	}
        }
    }
    
    private void clearClusters() {
    	for(Cluster cluster : clusters) {
    		cluster.clear();
    	}
    }
    
    private List getCentroids() {
    	List centroids = new ArrayList(NUM_CLUSTERS);
    	for(Cluster cluster : clusters) {
    		Point aux = cluster.getCentroid();
    		Point point = new Point(aux.getX(),aux.getY());
    		centroids.add(point);
    	}
    	return centroids;
    }
    
    private void assignCluster() {
        double max = Double.MAX_VALUE;
        double min = max; 
        int cluster = 0;                 
        double distance = 0.0; 
        
        for(Point point : points) {
        	min = max;
            for(int i = 0; i < NUM_CLUSTERS; i++) {
            	Cluster c = clusters.get(i);
                distance = Point.distance(point, c.getCentroid());
                if(distance < min){
                    min = distance;
                    cluster = i;
                }
            }
            point.setCluster(cluster);
            clusters.get(cluster).addPoint(point);
        }
    }
    
    private void calculateCentroids() {
        for(Cluster cluster : clusters) {
            int sumX = 0;
            int sumY = 0;
            List <Point> list = cluster.getPoints();
            int n_points = list.size();
            
            for(Point point : list) {
            	sumX += point.getX();
                sumY += point.getY();
            }
            
            Point centroid = cluster.getCentroid();
            if(n_points > 0) {
            	int newX = sumX / n_points;
            	int newY = sumY / n_points;
                centroid.setX(newX);
                centroid.setY(newY);
            }
        }
    }
}
