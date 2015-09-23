package com.yugiri.tools;


/**
 * Black and White ChaincodeImage Class - (0,0) at top left
 * int x width 
 * int y height 
 * boolean data (WHITE=TRUE)
 * DIRECTION OF MOVEMENT:
 * 012
 * 7X3
 * 654
 */
public class ChaincodeImage {
	
	private int width;
	private int height;
	private boolean[][] data;
	
	//default CTOR. load arbitrary image.
	public ChaincodeImage(int width, int height, boolean stream[]) {
		this.width = width;
		this.height = height;
		data = new boolean[width][height];
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				data[i][j] = stream[(i*10)+j];
			}
		}
	}


	public ChaincodeImage(int width, int height, int stream[]){
		this.width = width;
		this.height = height;
		data = new boolean[width][height];
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				if(stream[(i*width) + j] == -16777216) {
					data[i][j] = true;
				}
			}
		}
	}
	
	//load image from file
//	public Image(String path) {
//		try {
//			System.out.println("reading " + path);
//			BufferedImage imgFile = ImageIO.read(new File(path));
//			width = imgFile.getWidth();
//			height = imgFile.getHeight();
//			data = new boolean[width][height];
//			for (int y = 0; y < height; y++) {
//				for (int x = 0; x < width; x++) {
//					if (imgFile.getRGB(x, y) == -16777216) { // if current pixel is black
//						data[x][y] = true;
//						System.out.print(" #");
//					} else {
//						data[x][y] = false;
//						System.out.print("  ");
//					}
//				}
//				System.out.print("\n");
//			}
//		} catch (IOException e) {
//			System.out.println("Unable to load image: " + e.getMessage());
//		}
//	}
	
	//recognize the pattern towards a pre-set rules.
	public int recognize() throws Exception {
		int retval = 0;
		System.out.println("recognizing...");
		String pattern = analyze(); // THIS IS THE CHAIN CODE!
		System.out.println("pattern is " + pattern);

		//TODO: do the recognition here. add your own rules and respective retvals
		int[] simplifiedChain = getSimplifiedChain(pattern);
		System.out.println("check");
		for(int i=0; i<10; i++){
			System.out.println(simplifiedChain[i]);
		}
		int[][] models = new int[10][];
		for(int i=0; i<10; i++){
			models[i] = new int[10];
		}
		models[0][0] = 0;
		models[0][1] = 1;
		models[0][2] = 0;
		models[0][3] = 1;
		models[0][4] = 0;
		models[0][5] = 1;
		models[0][6] = 0;
		models[0][7] = 1;
		models[0][8] = 0;
		models[0][9] = 0;
		models[1][0] = 0;
		models[1][1] = 1;
		models[1][2] = 0;
		models[1][3] = 3;
		models[1][4] = 0;
		models[1][5] = 1;
		models[1][6] = 0;
		models[1][7] = 3;
		models[1][8] = 0;
		models[1][9] = 0;
		models[2][0] = 1;
		models[2][1] = 132;
		models[2][2] = 1;
		models[2][3] = 52;
		models[2][4] = 1;
		models[2][5] = 132;
		models[2][6] = 1;
		models[2][7] = 52;
		models[2][8] = 0;
		models[2][9] = 0;
		models[3][0] = 1;
		models[3][1] = 65;
		models[3][2] = 0;
		models[3][3] = 27;
		models[3][4] = 0;
		models[3][5] = 65;
		models[3][6] = 1;
		models[3][7] = 25;
		models[3][8] = 0;
		models[3][9] = 0;
		models[4][0] = 1;
		models[4][1] = 54;
		models[4][2] = 0;
		models[4][3] = 73;
		models[4][4] = 1;
		models[4][5] = 53;
		models[4][6] = 1;
		models[4][7] = 72;
		models[4][8] = 0;
		models[4][9] = 0;
		models[5][0] = 1;
		models[5][1] = 132;
		models[5][2] = 1;
		models[5][3] = 52;
		models[5][4] = 1;
		models[5][5] = 132;
		models[5][6] = 1;
		models[5][7] = 52;
		models[5][8] = 0;
		models[5][9] = 0;
		models[6][0] = 0;
		models[6][1] = 55;
		models[6][2] = 0;
		models[6][3] = 53;
		models[6][4] = 1;
		models[6][5] = 54;
		models[6][6] = 0;
		models[6][7] = 54;
		models[6][8] = 0;
		models[6][9] = 0;
		models[7][0] = 1;
		models[7][1] = 54;
		models[7][2] = 0;
		models[7][3] = 54;
		models[7][4] = 0;
		models[7][5] = 55;
		models[7][6] = 0;
		models[7][7] = 53;
		models[7][8] = 0;
		models[7][9] = 0;
		models[8][0] = 0;
		models[8][1] = 1;
		models[8][2] = 0;
		models[8][3] = 1;
		models[8][4] = 0;
		models[8][5] = 1;
		models[8][6] = 0;
		models[8][7] = 1;
		models[8][8] = 0;
		models[8][9] = 0;
		models[9][0] = 1;
		models[9][1] = 54;
		models[9][2] = 0;
		models[9][3] = 54;
		models[9][4] = 0;
		models[9][5] = 55;
		models[9][6] = 0;
		models[9][7] = 53;
		models[9][8] = 0;
		models[9][9] = 0;
		retval = getClosestNumber(simplifiedChain, models);
		return retval;
	}

	private int[] getChainArray(String pattern){
		int[] chainArray = new int[10];
		char[] charPattern = new char[pattern.length()];
		pattern.getChars(0, pattern.length(), charPattern, 0);
		for(char c : charPattern){
			chainArray[Character.getNumericValue(c)]++;
		}
		return chainArray;
	}

	private int[] getSimplifiedChain(String pattern){
		int[] chainArray = getChainArray(pattern);
		int min = getMinNumber(chainArray);
		for(int i=0; i<10; i++){
			chainArray[i] /= min;
		}
		return chainArray;
	}

	private int getMinNumber(int[] chainArray){
		int min = Integer.MAX_VALUE;
		for(int i : chainArray){
			if(i < min && i > 0){
				min = i;
			}
		}
		return min;
	}

	private int getClosestNumber(int[] chainArray, int[][] models){
		int closestNumber = -1;
		int diff = Integer.MAX_VALUE;
		for(int i=0; i<10; i++){
			int tempDiff = getChainDiff(chainArray, models[i]);
			if(tempDiff < diff){
				closestNumber = i;
				diff = tempDiff;
			}
		}
		return closestNumber;
	}

	private int getChainDiff(int[] arr, int[] model){
		int diff = 0;
		for(int i=0; i<10; i++){
			diff += Math.abs(arr[i] - model[i]);
		}
		return diff;
	}
	
	//analyze the image for pattern
	private String analyze() throws Exception {
		String pattern = "";
		//begin scan...
		for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					if (data[x][y]) { // if current pixel is black
						//found first black. store current position.
						int startPos[] = {x,y};
						boolean keepSearching = true;
						int prev = 3;
						while (keepSearching) {
							//begin trace...
							//the pixels around the current pos. TRUE is BLACK.
							boolean d[] = {false, false, false, false, false, false, false, false};
							int c = 0;
							for (int a = 0; a<3; a++){ //y
								for (int b = 0; b<3; b++){ //x
									if((x > 0 || b > 0)&&(y > 0 || a > 0)) {
										if (data[x - 1 + b][y - 1 + a]) {
											if (a == 0) {
												if (b == 0) {
													d[0] = true;
												} else if (b == 1) {
													d[1] = true;
												} else if (b == 2) {
													d[2] = true;
												}
											} else if (a == 1) {
												if (b == 0) {
													d[7] = true;
												} else if (b == 2) {
													d[3] = true;
												}
											} else if (a == 2) {
												if (b == 0) {
													d[6] = true;
												} else if (b == 1) {
													d[5] = true;
												} else if (b == 2) {
													d[4] = true;
												}
											}
										}
									}
								}
							}
							int p[] = genScanPath(prev);
							if (d[p[0]]) {
//								System.out.println("pattern changed here");
								//wow we're dealing with concave vertex here. shit.
								if (!d[p[1]]) {
									prev = p[0];
									x = getNextX(x, p[0]);
									y = getNextY(y, p[0]);
									pattern = pattern + Integer.toString(p[0]);
								} else if (!d[p[2]]) {
									prev = p[1];
									x = getNextX(x, p[1]);
									y = getNextY(y, p[1]);
									pattern = pattern + Integer.toString(p[1]);
								} else if (!d[p[3]]) {
									prev = p[2];
									x = getNextX(x, p[2]);
									y = getNextY(y, p[2]);
									pattern = pattern + Integer.toString(p[2]);
								} else if (!d[p[4]]) {
									prev = p[0];
									x = getNextX(x, p[0]);
									y = getNextY(y, p[0]);
									pattern = pattern + Integer.toString(p[0]);
								} else if (!d[p[5]]) {
									prev = p[4];
									x = getNextX(x, p[4]);
									y = getNextY(y, p[4]);
									pattern = pattern + Integer.toString(p[4]);
								} else if (!d[p[6]]) {
									prev = p[5];
									x = getNextX(x, p[5]);
									y = getNextY(y, p[5]);
									pattern = pattern + Integer.toString(p[5]);
								} else if (!d[p[7]]) {
									keepSearching = false;
									throw(new Exception("Tracer encountered a strange situation. He's Giving up." + d[p[7]]));
								}
							} else if (d[p[1]]) {
								prev = p[1];
								x = getNextX(x, p[1]);
								y = getNextY(y, p[1]);
								pattern = pattern + Integer.toString(p[1]);
							} else if (d[p[2]]) {
								prev = p[2];
								x = getNextX(x, p[2]);
								y = getNextY(y, p[2]);
								pattern = pattern + Integer.toString(p[2]);
							} else if (d[p[3]]) {
								prev = p[3];
								x = getNextX(x, p[3]);
								y = getNextY(y, p[3]);
								pattern = pattern + Integer.toString(p[3]);
							} else if (d[p[4]]) {
								prev = p[4];
								x = getNextX(x, p[4]);
								y = getNextY(y, p[4]);
								pattern = pattern + Integer.toString(p[4]);
							} else if (d[p[5]]) {
								prev = p[5];
								x = getNextX(x, p[5]);
								y = getNextY(y, p[5]);
								pattern = pattern + Integer.toString(p[5]);
							} else if (d[p[6]]) {
								prev = p[6];
								x = getNextX(x, p[6]);
								y = getNextY(y, p[6]);
								pattern = pattern + Integer.toString(p[6]);
							} else if (d[p[7]]) {
								keepSearching = false;
								throw(new Exception("Tracer encountered a dead end pixel. He's Giving up."));
							}
							if ((startPos[0] == x) && (startPos[1] == y)) {
								// already back to origin
								keepSearching = false;
							}
//							System.out.println("scanning... now at " + x + "," + y);
						}
						//traced. now kill the scanner.
						y = height;
						x = width;
					}
				}
			}
		
		return pattern;
	}
	
	private int getNextY(int x, int dir) throws Exception {
		switch (dir){
				case 0: return x-1;
				case 1: return x-1;
				case 2: return x-1;
				case 3: return x;
				case 4: return x+1;
				case 5: return x+1;
				case 6: return x+1;
				case 7: return x;
		}
		throw(new Exception("strange getNextX direction."));
	}
	
	private int getNextX(int y, int dir) throws Exception {
		switch (dir){
				case 0: return y-1;
				case 1: return y;
				case 2: return y+1;
				case 3: return y+1;
				case 4: return y+1;
				case 5: return y;
				case 6: return y-1;
				case 7: return y-1;
		}
		throw(new Exception("strange getNextY direction."));
	}
	
	//generate scan path. radar style! yeah.
	private int[] genScanPath(int prev){
		int ret[] = {0,0,0,0,0,0,0,0};
		
		ret[0] = prev;
		ret[1] = plusDir(prev, 1);
		ret[2] = plusDir(prev, 2);
		ret[3] = plusDir(prev, 3);
		ret[4] = negaDir(prev, 1);
		ret[5] = negaDir(prev, 2);
		ret[6] = negaDir(prev, 3);
		ret[7] = negaDir(prev, 4);
		
		return ret;
	} 
	
	//rotational addition. Can be improved.
	private int plusDir(int a, int b){
		for(int i=0; i<b; i++) {
			a++;
			if(a==8) {
				a=0;
			}
		}
		return a;
	} 
	
	//rotational negation. Can be improved.
	private int negaDir(int a, int b){
		for(int i=0; i<b; i++) {
			a--;
			if(a==-1) {
				a=7;
			}
		}
		return a;
	} 

}
