package com.yugiri.tools;

/**
 * Created by gilang on 03/10/2015.
 */
public class Box {

	protected int minX;
	protected int maxX;
	protected int minY;
	protected int maxY;
	protected int width;
	protected int height;

	public Box(){
		minX = -1;
		maxX = -1;
		minY = -1;
		maxY = -1;
		width = maxX - minX + 1;
		height = maxY - minY + 1;
	}

	public Box(int minX, int maxX, int minY, int maxY){
		this.minX = minX;
		this.minY = minY;
		this.maxX = maxX;
		this.maxY = maxY;
		width = maxX - minX + 1;
		height = maxY - minY + 1;
	}

	public int getMinX() {
		return minX;
	}

	public int getMaxX() {
		return maxX;
	}

	public int getMinY() {
		return minY;
	}

	public int getMaxY() {
		return maxY;
	}
}
