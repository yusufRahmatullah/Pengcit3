package com.yugiri.tools;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.Image;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gilang on 03/10/2015.
 */
public class Tools {

	public static final int GRID_DIMENSION = 5;
	public static Bitmap image;
	public static List<Model> model = new ArrayList<>();


	/**
	 * recognize all object using contour tracing
	 * @param image input image
	 * @return list of chain codes
	 */
	public static List<ImageGrid> getGrids(Bitmap image) {
		int minX, maxX, minY, maxY;
		List<ImageGrid> grids = new ArrayList<>();
		int direction;
		boolean isStop;
		Bitmap tempImage = image.copy(image.getConfig(), true);
		// iterate image
		for (int j=0; j<image.getWidth(); j++) {
			for (int i=0; i<image.getHeight(); i++) {
				// check if the current pixel is black and start tracing
				if (image.getPixel(j, i) == 0xFF000000) {
					int a = j, b = i;
					isStop = false;
					direction = 7;
					maxX = j; minX = j;
					maxY = i; minY = i;
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
									if(a > maxX)
										maxX = a;
									if(a < minX)
										minX = a;
									if(b > maxY)
										maxY = b;
									if(b < minY)
										minY = b;
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

						// check if tracing is done
						if (a == j && b == i) {
							isStop = true;
						}
					}
					// post-tracing
					boolean[][] mark = new boolean[image.getHeight()][image.getWidth()];
					floodFill(image, mark, j, i, 0xFF000000, 0xFFFFFFFF);
					grids.add(new ImageGrid(minX, maxX, minY, maxY, GRID_DIMENSION));
				}
			}
		}
		fillGrid(tempImage, grids);
		return grids;
	}

	public static void learn(Bitmap bitmap, String characters){
		Bitmap temp = bitmap.copy(bitmap.getConfig(), true);
		List<ImageGrid> grids = getGrids(temp);
		String[] chars = characters.split(",");
		int counter = 0;
		for(ImageGrid grid : grids){
//			grid.printDebug();
			grid.printArray(); // <--- gak guna
			String c;
			if(counter >= chars.length)
				c = "null";
			else
				c = chars[counter];
			model.add(new Model(grid.gridArray, c));
			counter++;
		}
	}

	public static Bitmap invertImage(Bitmap image){
		Bitmap bitmap = image.copy(image.getConfig(), true);
		for(int i=0; i<bitmap.getHeight(); i++){
			for(int j=0; j<bitmap.getWidth(); j++){
				if(bitmap.getPixel(j, i) == 0xFF000000){
					bitmap.setPixel(j, i, 0xFFFFFFFF);
				}else{
					bitmap.setPixel(j, i, 0xFF000000);
				}
			}
		}
		return bitmap;
	}

	public static void fillGrid(Bitmap bitmap, List<ImageGrid> grids){
		int blackCounter;
		int whiteCounter;
		int counter = 0;
		// iterate through each characters
		for(ImageGrid grid : grids){
			int regionCounter = 0;
			for(ImageGrid region : grid.getRegions()) {
				blackCounter = 0; whiteCounter = 0;
				// iterate through each pixels in region
				for (int i = region.getMinY(); i < region.getMaxY(); i++) {
					for (int j = region.getMinX(); j < region.getMaxX(); j++) {
						int pixel = bitmap.getPixel(j, i);
						if (pixel == 0xFF000000)
							blackCounter++;
						else if (pixel == 0xFFFFFFFF)
							whiteCounter++;
					}
				}

				System.out.print(counter + " " + regionCounter + " " + (float)blackCounter + " " +
						(float)(blackCounter + whiteCounter) + " " + ((float)blackCounter  /
						(float) (blackCounter + whiteCounter)));
				if((float)blackCounter  / (float) (blackCounter + whiteCounter) > 0.35f) {
					System.out.println(" true");
					grid.gridArray[regionCounter] = true;
				}else{
					grid.gridArray[regionCounter] = false;
					System.out.println(" false");
				}
				regionCounter++;
			}
			counter++;
		}
	}

	public static Bitmap drawGrid(Bitmap image, List<ImageGrid> grids){
		if(grids == null)
			return image;
		Bitmap newImage = image.copy(image.getConfig(), true);
		for(ImageGrid grid : grids){
			System.out.println("count " + grid.getRegionCount());
			if(grid.getRegionCount() > 0) {
				int regionCounter = 0;
				for(ImageGrid child : grid.getRegions()) {
					int color = Color.GREEN;
					if(grid.gridArray[regionCounter])
						color = Color.RED;
					for (int i = child.getMinY(); i <= child.getMaxY(); i++) {
						for (int j = child.getMinX(); j <= child.getMaxX(); j++) {
							if (i == child.getMinY() || i == child.getMaxY() || j == child.getMinX() || j
									== child.getMaxX()) {
								newImage.setPixel(j, i, color);
							}
						}
					}
					regionCounter++;
				}
			}
		}
		return newImage;
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

	public static void floodFill(Bitmap image, boolean[][] mark, int x,
								 int y, int srcColor, int tgtColor) {
		Tools.image = image;
		floodFill(mark, x, y, srcColor, tgtColor);
	}

	/**
	 * four directional recursive flood fill
	 * @param x x coordinate
	 * @param y y coordinate
	 * @param tgtColor fill color
	 */
	public static void floodFill(boolean[][] mark, int x,
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
		floodFill(mark, x-1, y, srcColor, tgtColor);
		floodFill(mark, x + 1, y, srcColor, tgtColor);
		floodFill(mark, x, y - 1, srcColor, tgtColor);
		floodFill(mark, x, y + 1, srcColor, tgtColor);
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

	public static String getStringInGrids(List<ImageGrid> grids){
		StringBuilder builder = new StringBuilder();
		for(ImageGrid grid : grids){
			builder.append(getClosestChar(grid) + ", ");
		}
		return builder.toString();
	}

	public static String getClosestChar(ImageGrid grid){
		try {
			int minIdx = -1;
			int minDiff = Integer.MAX_VALUE;
			int diff;
			for (int i = 0; i < model.size(); i++) {
				Model m = model.get(i);
				diff = getGridDifference(m.arrayGrid, grid.gridArray);
				if (diff < minDiff) {
					minDiff = diff;
					minIdx = i;
				}
			}
			return model.get(minIdx).character;
		}catch (Exception e){
			return "null";
		}
	}

	public static int getGridDifference(boolean[] arrayGrid1, boolean[] arrayGrid2){
		int diff = 0;
		for(int i=0; i<arrayGrid1.length; i++){
			if(arrayGrid1[i] != arrayGrid2[i])
				diff++;
		}
		return diff;
	}

	public static Bitmap blur(Bitmap sentBitmap, int radius) {
		int scale = 256 / sentBitmap.getHeight();
		int width = sentBitmap.getWidth() * scale;
		int height = sentBitmap.getHeight() * scale;
		sentBitmap = Bitmap.createScaledBitmap(sentBitmap, width, height, false);

		Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

		if (radius < 1) {
			return (null);
		}

		int w = bitmap.getWidth();
		int h = bitmap.getHeight();

		int[] pix = new int[w * h];
		bitmap.getPixels(pix, 0, w, 0, 0, w, h);

		int wm = w - 1;
		int hm = h - 1;
		int wh = w * h;
		int div = radius + radius + 1;

		int r[] = new int[wh];
		int g[] = new int[wh];
		int b[] = new int[wh];
		int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
		int vmin[] = new int[Math.max(w, h)];

		int divsum = (div + 1) >> 1;
		divsum *= divsum;
		int dv[] = new int[256 * divsum];
		for (i = 0; i < 256 * divsum; i++) {
			dv[i] = (i / divsum);
		}

		yw = yi = 0;

		int[][] stack = new int[div][3];
		int stackpointer;
		int stackstart;
		int[] sir;
		int rbs;
		int r1 = radius + 1;
		int routsum, goutsum, boutsum;
		int rinsum, ginsum, binsum;

		for (y = 0; y < h; y++) {
			rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
			for (i = -radius; i <= radius; i++) {
				p = pix[yi + Math.min(wm, Math.max(i, 0))];
				sir = stack[i + radius];
				sir[0] = (p & 0xff0000) >> 16;
				sir[1] = (p & 0x00ff00) >> 8;
				sir[2] = (p & 0x0000ff);
				rbs = r1 - Math.abs(i);
				rsum += sir[0] * rbs;
				gsum += sir[1] * rbs;
				bsum += sir[2] * rbs;
				if (i > 0) {
					rinsum += sir[0];
					ginsum += sir[1];
					binsum += sir[2];
				} else {
					routsum += sir[0];
					goutsum += sir[1];
					boutsum += sir[2];
				}
			}
			stackpointer = radius;

			for (x = 0; x < w; x++) {

				r[yi] = dv[rsum];
				g[yi] = dv[gsum];
				b[yi] = dv[bsum];

				rsum -= routsum;
				gsum -= goutsum;
				bsum -= boutsum;

				stackstart = stackpointer - radius + div;
				sir = stack[stackstart % div];

				routsum -= sir[0];
				goutsum -= sir[1];
				boutsum -= sir[2];

				if (y == 0) {
					vmin[x] = Math.min(x + radius + 1, wm);
				}
				p = pix[yw + vmin[x]];

				sir[0] = (p & 0xff0000) >> 16;
				sir[1] = (p & 0x00ff00) >> 8;
				sir[2] = (p & 0x0000ff);

				rinsum += sir[0];
				ginsum += sir[1];
				binsum += sir[2];

				rsum += rinsum;
				gsum += ginsum;
				bsum += binsum;

				stackpointer = (stackpointer + 1) % div;
				sir = stack[(stackpointer) % div];

				routsum += sir[0];
				goutsum += sir[1];
				boutsum += sir[2];

				rinsum -= sir[0];
				ginsum -= sir[1];
				binsum -= sir[2];

				yi++;
			}
			yw += w;
		}
		for (x = 0; x < w; x++) {
			rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
			yp = -radius * w;
			for (i = -radius; i <= radius; i++) {
				yi = Math.max(0, yp) + x;

				sir = stack[i + radius];

				sir[0] = r[yi];
				sir[1] = g[yi];
				sir[2] = b[yi];

				rbs = r1 - Math.abs(i);

				rsum += r[yi] * rbs;
				gsum += g[yi] * rbs;
				bsum += b[yi] * rbs;

				if (i > 0) {
					rinsum += sir[0];
					ginsum += sir[1];
					binsum += sir[2];
				} else {
					routsum += sir[0];
					goutsum += sir[1];
					boutsum += sir[2];
				}

				if (i < hm) {
					yp += w;
				}
			}
			yi = x;
			stackpointer = radius;
			for (y = 0; y < h; y++) {
				// Preserve alpha channel: ( 0xff000000 & pix[yi] )
				pix[yi] = ( 0xff000000 & pix[yi] ) | ( dv[rsum] << 16 ) | ( dv[gsum] << 8 ) | dv[bsum];

				rsum -= routsum;
				gsum -= goutsum;
				bsum -= boutsum;

				stackstart = stackpointer - radius + div;
				sir = stack[stackstart % div];

				routsum -= sir[0];
				goutsum -= sir[1];
				boutsum -= sir[2];

				if (x == 0) {
					vmin[y] = Math.min(y + r1, hm) * w;
				}
				p = x + vmin[y];

				sir[0] = r[p];
				sir[1] = g[p];
				sir[2] = b[p];

				rinsum += sir[0];
				ginsum += sir[1];
				binsum += sir[2];

				rsum += rinsum;
				gsum += ginsum;
				bsum += binsum;

				stackpointer = (stackpointer + 1) % div;
				sir = stack[stackpointer];

				routsum += sir[0];
				goutsum += sir[1];
				boutsum += sir[2];

				rinsum -= sir[0];
				ginsum -= sir[1];
				binsum -= sir[2];

				yi += w;
			}
		}

		bitmap.setPixels(pix, 0, w, 0, 0, w, h);

		return (bitmap);
	}


}
