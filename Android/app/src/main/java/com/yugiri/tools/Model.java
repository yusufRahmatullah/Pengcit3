package com.yugiri.tools;

/**
 * Created by gilang on 04/10/2015.
 */
public class Model {

	public boolean[] arrayGrid;
	public String character;

	public Model(boolean[] arrayGrid, String character){
		this.arrayGrid = arrayGrid;
		this.character = character;
	}

	public Model setArray(boolean[] arrayGrid){
		this.arrayGrid = arrayGrid;
		return this;
	}

	public Model setChar(String character){
		this.character = character;
		return this;
	}
}
