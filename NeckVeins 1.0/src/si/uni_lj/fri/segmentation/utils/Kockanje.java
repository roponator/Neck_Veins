package si.uni_lj.fri.segmentation.utils;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public class Kockanje {
	
	public static ArrayList<Float> vseKocke(float[][][] CTMatrix, float isolevel) {
		ArrayList<Float> vertices = new ArrayList<Float>();
		Vertex[] cubesVertices = new Vertex[8];
		
		for (int i = 0; i < cubesVertices.length; i++)
			cubesVertices[i] = new Vertex(0, 0, 0);
		
		int indeksi[] =  { 
			0, 2, 1, 2, 3, 1, 
			1, 3, 4, 3, 5, 4, 
			4, 5, 6, 5, 7, 6, 
			6, 7, 0, 7, 2, 0, 
			6, 0, 4, 0, 1, 4,
			2, 7, 3, 7, 5, 3 
		};

		int pogoj;
		
		for (int z = 0; z < CTMatrix[0][0].length - 1; z++) {
			for (int y = 0; y < CTMatrix.length - 1; y++) {
				for (int x = 0; x < CTMatrix[0].length - 1; x++) {
					int left = x;
					int right = x + 1;
					int top = y;
					int bottom = y + 1;
					int back = z + 1;
					int front = z;

					pogoj = 0;
					
					cubesVertices[7].setVertex(left, top, back, CTMatrix[top][left][back]); //0
					cubesVertices[5].setVertex(right, top, back, CTMatrix[top][right][back]); //1
					cubesVertices[4].setVertex(right, bottom, back, CTMatrix[bottom][right][back]); //2
					cubesVertices[6].setVertex(left, bottom, back, CTMatrix[bottom][left][back]); //3
					cubesVertices[2].setVertex(left, top, front, CTMatrix[top][left][front]); //4
					cubesVertices[3].setVertex(right, top, front, CTMatrix[top][right][front]); //5
					cubesVertices[1].setVertex(right, bottom, front, CTMatrix[bottom][right][front]); //6
					cubesVertices[0].setVertex(left, bottom, front, CTMatrix[bottom][left][front]); //7
					
					//System.out.println("isolevel: "+isolevel+", "+cubesVertices[0].value);
					
					for(int pog = 0; pog < cubesVertices.length; pog++){
						if(cubesVertices[pog].value < isolevel){
							pogoj++;
						}
					}
					
					if(pogoj == 0){
						for (int n = 0; n < 36 ; n +=3) {
							Vertex first = cubesVertices[indeksi[n]];
							Vertex second = cubesVertices[indeksi[n + 1]];
							Vertex third = cubesVertices[indeksi[n + 2]];
							vertices.add(first.x);
							vertices.add(first.y);
							vertices.add(first.z);
							vertices.add(second.x);
							vertices.add(second.y);
							vertices.add(second.z);
							vertices.add(third.x);
							vertices.add(third.y);
							vertices.add(third.z);
						}
					}
				}
			}
		}	
		
		return vertices;
	}
	
	public static ArrayList<Float> delneKocke(float[][][] CTMatrix, float isolevel, int meja, int recursion) {
		
		int GLOBINA = 0;
		int NASLEDNJI = 0;
		
		Map<String, Vertex[]> cubes = new TreeMap<String, Vertex[]>();
		Map<String, Integer> pogoji = new TreeMap<String, Integer>();
		Map<String, Integer> indeksiZank = new TreeMap<String, Integer>();
		ArrayList<Float> vertices = new ArrayList<Float>();
		
		int pogoj;
		Vertex[] cubesVertices = new Vertex[8];
		for (int i = 0; i < cubesVertices.length; i++){
			cubesVertices[i] = new Vertex(0, 0, 0);
		}
		
		for(int i = 2; i <= recursion; i++){
			cubes.put("cubesVer"+i, new Vertex[8]);	
			for(int j=0; j < cubes.get("cubesVer"+i).length; j++){
				cubes.get("cubesVer"+i)[j] = new Vertex(0, 0, 0);
			}
		}
		
		int indeksi[] =  { 
			0, 2, 1, 2, 3, 1, 
			1, 3, 4, 3, 5, 4, 
			4, 5, 6, 5, 7, 6, 
			6, 7, 0, 7, 2, 0, 
			6, 0, 4, 0, 1, 4,
			2, 7, 3, 7, 5, 3 
		};
		
		for (int z = 0; z < meja; z++) {
			for (int y = 0; y < meja; y++) {
				for (int x = 0; x < meja; x++) {
					int left = x;
					int right = x + 1;
					int top = y;
					int bottom = y + 1;
					int back = z + 1;
					int front = z;

					pogoj = 0;
					
					cubesVertices[7].setVertex(left, top, back, CTMatrix[top][left][back]); //0
					cubesVertices[5].setVertex(right, top, back, CTMatrix[top][right][back]); //1
					cubesVertices[4].setVertex(right, bottom, back, CTMatrix[bottom][right][back]); //2
					cubesVertices[6].setVertex(left, bottom, back, CTMatrix[bottom][left][back]); //3
					cubesVertices[2].setVertex(left, top, front, CTMatrix[top][left][front]); //4
					cubesVertices[3].setVertex(right, top, front, CTMatrix[top][right][front]); //5
					cubesVertices[1].setVertex(right, bottom, front, CTMatrix[bottom][right][front]); //6
					cubesVertices[0].setVertex(left, bottom, front, CTMatrix[bottom][left][front]); //7
					
					cubes.put("cubesVer1", cubesVertices);
					
					for(int pog = 0; pog < cubesVertices.length; pog++){
						if(cubesVertices[pog].value < isolevel){
							pogoj++;
						}
					}
					
					if(pogoj == 0){
						for (int n = 0; n < 36 ; n +=3) {
							Vertex first = cubesVertices[indeksi[n]];
							Vertex second = cubesVertices[indeksi[n + 1]];
							Vertex third = cubesVertices[indeksi[n + 2]];
							vertices.add(first.x);
							vertices.add(first.y);
							vertices.add(first.z);
							vertices.add(second.x);
							vertices.add(second.y);
							vertices.add(second.z);
							vertices.add(third.x);
							vertices.add(third.y);
							vertices.add(third.z);
						}
					}
					
					if(pogoj != 0 && pogoj < 8){
						//REKURZIJA
						rekurzijaKocke(GLOBINA, NASLEDNJI, recursion, isolevel, indeksi, cubes,
								pogoji, indeksiZank, vertices);						 
					}
				}
			}
		}
		return vertices;
	}
	
	private static void rekurzijaKocke(int GLOBINA, int NASLEDNJI, int MEJAGLOBINE, float isolevel,
			int indeksi[], Map<String, Vertex[]> cubes, Map<String, Integer> pogoji,
			Map<String, Integer> indeksiZank, ArrayList<Float> vertices) {
		
		GLOBINA++;
		NASLEDNJI = GLOBINA + 1;
		
		if(GLOBINA >= MEJAGLOBINE){
			return;
		}
		
		for (indeksiZank.put("a"+NASLEDNJI, 0); indeksiZank.get("a"+NASLEDNJI) < cubes.get("cubesVer"+NASLEDNJI).length; indeksiZank.put("a"+NASLEDNJI,indeksiZank.get("a"+NASLEDNJI)+1)){	
			pogoji.put("pogoj"+NASLEDNJI, 0);
			for(int b = 0; b < cubes.get("cubesVer"+NASLEDNJI).length; b++){
				if(indeksiZank.get("a"+NASLEDNJI) != b){
					float pointX = (cubes.get("cubesVer"+GLOBINA)[indeksiZank.get("a"+NASLEDNJI)].x + cubes.get("cubesVer"+GLOBINA)[b].x)/2;
					float pointY = (cubes.get("cubesVer"+GLOBINA)[indeksiZank.get("a"+NASLEDNJI)].y + cubes.get("cubesVer"+GLOBINA)[b].y)/2;
					float pointZ = (cubes.get("cubesVer"+GLOBINA)[indeksiZank.get("a"+NASLEDNJI)].z + cubes.get("cubesVer"+GLOBINA)[b].z)/2;
					float pointValue = (cubes.get("cubesVer"+GLOBINA)[indeksiZank.get("a"+NASLEDNJI)].value + cubes.get("cubesVer"+GLOBINA)[b].value)/2;
				
					cubes.get("cubesVer"+NASLEDNJI)[b].setVertex(pointX, pointY, pointZ, pointValue);			
				}else{
					cubes.get("cubesVer"+NASLEDNJI)[b].setVertex(cubes.get("cubesVer"+GLOBINA)[b].x, cubes.get("cubesVer"+GLOBINA)[b].y, cubes.get("cubesVer"+GLOBINA)[b].z, cubes.get("cubesVer"+GLOBINA)[b].value);									
				}
			}
			
			for(int pog = 0; pog < cubes.get("cubesVer"+NASLEDNJI).length; pog++){
				if(cubes.get("cubesVer"+NASLEDNJI)[pog].value < isolevel){
					pogoji.put("pogoj"+NASLEDNJI, pogoji.get("pogoj"+NASLEDNJI)+1);
				}
			}
			
			if(pogoji.get("pogoj"+NASLEDNJI) == 0){
				for (int n = 0; n < 36 ; n +=3) {
					Vertex first = cubes.get("cubesVer"+NASLEDNJI)[indeksi[n]];
					Vertex second = cubes.get("cubesVer"+NASLEDNJI)[indeksi[n + 1]];
					Vertex third = cubes.get("cubesVer"+NASLEDNJI)[indeksi[n + 2]];
					vertices.add(first.x);
					vertices.add(first.y);
					vertices.add(first.z);
					vertices.add(second.x);
					vertices.add(second.y);
					vertices.add(second.z);
					vertices.add(third.x);
					vertices.add(third.y);
					vertices.add(third.z);
				}
			}
			
			
			if(pogoji.get("pogoj"+NASLEDNJI) != 0 && pogoji.get("pogoj"+NASLEDNJI) < 8){
				rekurzijaKocke(GLOBINA, NASLEDNJI, MEJAGLOBINE, isolevel, indeksi, cubes,
						pogoji, indeksiZank, vertices);
			}
		}
		GLOBINA--;
		NASLEDNJI = GLOBINA + 1;
	}
	
	private static int[] edgeTable = { 0x0, 0x109, 0x203, 0x30a, 0x406, 0x50f, 0x605, 0x70c, 0x80c, 0x905, 0xa0f,
		0xb06, 0xc0a, 0xd03, 0xe09, 0xf00, 0x190, 0x99, 0x393, 0x29a, 0x596, 0x49f, 0x795, 0x69c, 0x99c, 0x895,
		0xb9f, 0xa96, 0xd9a, 0xc93, 0xf99, 0xe90, 0x230, 0x339, 0x33, 0x13a, 0x636, 0x73f, 0x435, 0x53c, 0xa3c,
		0xb35, 0x83f, 0x936, 0xe3a, 0xf33, 0xc39, 0xd30, 0x3a0, 0x2a9, 0x1a3, 0xaa, 0x7a6, 0x6af, 0x5a5, 0x4ac,
		0xbac, 0xaa5, 0x9af, 0x8a6, 0xfaa, 0xea3, 0xda9, 0xca0, 0x460, 0x569, 0x663, 0x76a, 0x66, 0x16f, 0x265,
		0x36c, 0xc6c, 0xd65, 0xe6f, 0xf66, 0x86a, 0x963, 0xa69, 0xb60, 0x5f0, 0x4f9, 0x7f3, 0x6fa, 0x1f6, 0xff,
		0x3f5, 0x2fc, 0xdfc, 0xcf5, 0xfff, 0xef6, 0x9fa, 0x8f3, 0xbf9, 0xaf0, 0x650, 0x759, 0x453, 0x55a, 0x256,
		0x35f, 0x55, 0x15c, 0xe5c, 0xf55, 0xc5f, 0xd56, 0xa5a, 0xb53, 0x859, 0x950, 0x7c0, 0x6c9, 0x5c3, 0x4ca,
		0x3c6, 0x2cf, 0x1c5, 0xcc, 0xfcc, 0xec5, 0xdcf, 0xcc6, 0xbca, 0xac3, 0x9c9, 0x8c0, 0x8c0, 0x9c9, 0xac3,
		0xbca, 0xcc6, 0xdcf, 0xec5, 0xfcc, 0xcc, 0x1c5, 0x2cf, 0x3c6, 0x4ca, 0x5c3, 0x6c9, 0x7c0, 0x950, 0x859,
		0xb53, 0xa5a, 0xd56, 0xc5f, 0xf55, 0xe5c, 0x15c, 0x55, 0x35f, 0x256, 0x55a, 0x453, 0x759, 0x650, 0xaf0,
		0xbf9, 0x8f3, 0x9fa, 0xef6, 0xfff, 0xcf5, 0xdfc, 0x2fc, 0x3f5, 0xff, 0x1f6, 0x6fa, 0x7f3, 0x4f9, 0x5f0,
		0xb60, 0xa69, 0x963, 0x86a, 0xf66, 0xe6f, 0xd65, 0xc6c, 0x36c, 0x265, 0x16f, 0x66, 0x76a, 0x663, 0x569,
		0x460, 0xca0, 0xda9, 0xea3, 0xfaa, 0x8a6, 0x9af, 0xaa5, 0xbac, 0x4ac, 0x5a5, 0x6af, 0x7a6, 0xaa, 0x1a3,
		0x2a9, 0x3a0, 0xd30, 0xc39, 0xf33, 0xe3a, 0x936, 0x83f, 0xb35, 0xa3c, 0x53c, 0x435, 0x73f, 0x636, 0x13a,
		0x33, 0x339, 0x230, 0xe90, 0xf99, 0xc93, 0xd9a, 0xa96, 0xb9f, 0x895, 0x99c, 0x69c, 0x795, 0x49f, 0x596,
		0x29a, 0x393, 0x99, 0x190, 0xf00, 0xe09, 0xd03, 0xc0a, 0xb06, 0xa0f, 0x905, 0x80c, 0x70c, 0x605, 0x50f,
		0x406, 0x30a, 0x203, 0x109, 0x0 };

/**
 * Table from Paul Bourke's website
 * http://paulbourke.net/geometry/polygonise/
 */
private static int[][] triTable = { { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
		{ 0, 8, 3, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
		{ 0, 1, 9, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
		{ 1, 8, 3, 9, 8, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
		{ 1, 2, 10, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
		{ 0, 8, 3, 1, 2, 10, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
		{ 9, 2, 10, 0, 2, 9, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
		{ 2, 8, 3, 2, 10, 8, 10, 9, 8, -1, -1, -1, -1, -1, -1, -1 },
		{ 3, 11, 2, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
		{ 0, 11, 2, 8, 11, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
		{ 1, 9, 0, 2, 3, 11, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
		{ 1, 11, 2, 1, 9, 11, 9, 8, 11, -1, -1, -1, -1, -1, -1, -1 },
		{ 3, 10, 1, 11, 10, 3, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
		{ 0, 10, 1, 0, 8, 10, 8, 11, 10, -1, -1, -1, -1, -1, -1, -1 },
		{ 3, 9, 0, 3, 11, 9, 11, 10, 9, -1, -1, -1, -1, -1, -1, -1 },
		{ 9, 8, 10, 10, 8, 11, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
		{ 4, 7, 8, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
		{ 4, 3, 0, 7, 3, 4, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
		{ 0, 1, 9, 8, 4, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
		{ 4, 1, 9, 4, 7, 1, 7, 3, 1, -1, -1, -1, -1, -1, -1, -1 },
		{ 1, 2, 10, 8, 4, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
		{ 3, 4, 7, 3, 0, 4, 1, 2, 10, -1, -1, -1, -1, -1, -1, -1 },
		{ 9, 2, 10, 9, 0, 2, 8, 4, 7, -1, -1, -1, -1, -1, -1, -1 },
		{ 2, 10, 9, 2, 9, 7, 2, 7, 3, 7, 9, 4, -1, -1, -1, -1 },
		{ 8, 4, 7, 3, 11, 2, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
		{ 11, 4, 7, 11, 2, 4, 2, 0, 4, -1, -1, -1, -1, -1, -1, -1 },
		{ 9, 0, 1, 8, 4, 7, 2, 3, 11, -1, -1, -1, -1, -1, -1, -1 },
		{ 4, 7, 11, 9, 4, 11, 9, 11, 2, 9, 2, 1, -1, -1, -1, -1 },
		{ 3, 10, 1, 3, 11, 10, 7, 8, 4, -1, -1, -1, -1, -1, -1, -1 },
		{ 1, 11, 10, 1, 4, 11, 1, 0, 4, 7, 11, 4, -1, -1, -1, -1 },
		{ 4, 7, 8, 9, 0, 11, 9, 11, 10, 11, 0, 3, -1, -1, -1, -1 },
		{ 4, 7, 11, 4, 11, 9, 9, 11, 10, -1, -1, -1, -1, -1, -1, -1 },
		{ 9, 5, 4, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
		{ 9, 5, 4, 0, 8, 3, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
		{ 0, 5, 4, 1, 5, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
		{ 8, 5, 4, 8, 3, 5, 3, 1, 5, -1, -1, -1, -1, -1, -1, -1 },
		{ 1, 2, 10, 9, 5, 4, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
		{ 3, 0, 8, 1, 2, 10, 4, 9, 5, -1, -1, -1, -1, -1, -1, -1 },
		{ 5, 2, 10, 5, 4, 2, 4, 0, 2, -1, -1, -1, -1, -1, -1, -1 },
		{ 2, 10, 5, 3, 2, 5, 3, 5, 4, 3, 4, 8, -1, -1, -1, -1 },
		{ 9, 5, 4, 2, 3, 11, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
		{ 0, 11, 2, 0, 8, 11, 4, 9, 5, -1, -1, -1, -1, -1, -1, -1 },
		{ 0, 5, 4, 0, 1, 5, 2, 3, 11, -1, -1, -1, -1, -1, -1, -1 },
		{ 2, 1, 5, 2, 5, 8, 2, 8, 11, 4, 8, 5, -1, -1, -1, -1 },
		{ 10, 3, 11, 10, 1, 3, 9, 5, 4, -1, -1, -1, -1, -1, -1, -1 },
		{ 4, 9, 5, 0, 8, 1, 8, 10, 1, 8, 11, 10, -1, -1, -1, -1 },
		{ 5, 4, 0, 5, 0, 11, 5, 11, 10, 11, 0, 3, -1, -1, -1, -1 },
		{ 5, 4, 8, 5, 8, 10, 10, 8, 11, -1, -1, -1, -1, -1, -1, -1 },
		{ 9, 7, 8, 5, 7, 9, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
		{ 9, 3, 0, 9, 5, 3, 5, 7, 3, -1, -1, -1, -1, -1, -1, -1 },
		{ 0, 7, 8, 0, 1, 7, 1, 5, 7, -1, -1, -1, -1, -1, -1, -1 },
		{ 1, 5, 3, 3, 5, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
		{ 9, 7, 8, 9, 5, 7, 10, 1, 2, -1, -1, -1, -1, -1, -1, -1 },
		{ 10, 1, 2, 9, 5, 0, 5, 3, 0, 5, 7, 3, -1, -1, -1, -1 },
		{ 8, 0, 2, 8, 2, 5, 8, 5, 7, 10, 5, 2, -1, -1, -1, -1 },
		{ 2, 10, 5, 2, 5, 3, 3, 5, 7, -1, -1, -1, -1, -1, -1, -1 },
		{ 7, 9, 5, 7, 8, 9, 3, 11, 2, -1, -1, -1, -1, -1, -1, -1 },
		{ 9, 5, 7, 9, 7, 2, 9, 2, 0, 2, 7, 11, -1, -1, -1, -1 },
		{ 2, 3, 11, 0, 1, 8, 1, 7, 8, 1, 5, 7, -1, -1, -1, -1 },
		{ 11, 2, 1, 11, 1, 7, 7, 1, 5, -1, -1, -1, -1, -1, -1, -1 },
		{ 9, 5, 8, 8, 5, 7, 10, 1, 3, 10, 3, 11, -1, -1, -1, -1 },
		{ 5, 7, 0, 5, 0, 9, 7, 11, 0, 1, 0, 10, 11, 10, 0, -1 },
		{ 11, 10, 0, 11, 0, 3, 10, 5, 0, 8, 0, 7, 5, 7, 0, -1 },
		{ 11, 10, 5, 7, 11, 5, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
		{ 10, 6, 5, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
		{ 0, 8, 3, 5, 10, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
		{ 9, 0, 1, 5, 10, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
		{ 1, 8, 3, 1, 9, 8, 5, 10, 6, -1, -1, -1, -1, -1, -1, -1 },
		{ 1, 6, 5, 2, 6, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
		{ 1, 6, 5, 1, 2, 6, 3, 0, 8, -1, -1, -1, -1, -1, -1, -1 },
		{ 9, 6, 5, 9, 0, 6, 0, 2, 6, -1, -1, -1, -1, -1, -1, -1 },
		{ 5, 9, 8, 5, 8, 2, 5, 2, 6, 3, 2, 8, -1, -1, -1, -1 },
		{ 2, 3, 11, 10, 6, 5, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
		{ 11, 0, 8, 11, 2, 0, 10, 6, 5, -1, -1, -1, -1, -1, -1, -1 },
		{ 0, 1, 9, 2, 3, 11, 5, 10, 6, -1, -1, -1, -1, -1, -1, -1 },
		{ 5, 10, 6, 1, 9, 2, 9, 11, 2, 9, 8, 11, -1, -1, -1, -1 },
		{ 6, 3, 11, 6, 5, 3, 5, 1, 3, -1, -1, -1, -1, -1, -1, -1 },
		{ 0, 8, 11, 0, 11, 5, 0, 5, 1, 5, 11, 6, -1, -1, -1, -1 },
		{ 3, 11, 6, 0, 3, 6, 0, 6, 5, 0, 5, 9, -1, -1, -1, -1 },
		{ 6, 5, 9, 6, 9, 11, 11, 9, 8, -1, -1, -1, -1, -1, -1, -1 },
		{ 5, 10, 6, 4, 7, 8, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
		{ 4, 3, 0, 4, 7, 3, 6, 5, 10, -1, -1, -1, -1, -1, -1, -1 },
		{ 1, 9, 0, 5, 10, 6, 8, 4, 7, -1, -1, -1, -1, -1, -1, -1 },
		{ 10, 6, 5, 1, 9, 7, 1, 7, 3, 7, 9, 4, -1, -1, -1, -1 },
		{ 6, 1, 2, 6, 5, 1, 4, 7, 8, -1, -1, -1, -1, -1, -1, -1 },
		{ 1, 2, 5, 5, 2, 6, 3, 0, 4, 3, 4, 7, -1, -1, -1, -1 },
		{ 8, 4, 7, 9, 0, 5, 0, 6, 5, 0, 2, 6, -1, -1, -1, -1 },
		{ 7, 3, 9, 7, 9, 4, 3, 2, 9, 5, 9, 6, 2, 6, 9, -1 },
		{ 3, 11, 2, 7, 8, 4, 10, 6, 5, -1, -1, -1, -1, -1, -1, -1 },
		{ 5, 10, 6, 4, 7, 2, 4, 2, 0, 2, 7, 11, -1, -1, -1, -1 },
		{ 0, 1, 9, 4, 7, 8, 2, 3, 11, 5, 10, 6, -1, -1, -1, -1 },
		{ 9, 2, 1, 9, 11, 2, 9, 4, 11, 7, 11, 4, 5, 10, 6, -1 },
		{ 8, 4, 7, 3, 11, 5, 3, 5, 1, 5, 11, 6, -1, -1, -1, -1 },
		{ 5, 1, 11, 5, 11, 6, 1, 0, 11, 7, 11, 4, 0, 4, 11, -1 },
		{ 0, 5, 9, 0, 6, 5, 0, 3, 6, 11, 6, 3, 8, 4, 7, -1 },
		{ 6, 5, 9, 6, 9, 11, 4, 7, 9, 7, 11, 9, -1, -1, -1, -1 },
		{ 10, 4, 9, 6, 4, 10, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
		{ 4, 10, 6, 4, 9, 10, 0, 8, 3, -1, -1, -1, -1, -1, -1, -1 },
		{ 10, 0, 1, 10, 6, 0, 6, 4, 0, -1, -1, -1, -1, -1, -1, -1 },
		{ 8, 3, 1, 8, 1, 6, 8, 6, 4, 6, 1, 10, -1, -1, -1, -1 },
		{ 1, 4, 9, 1, 2, 4, 2, 6, 4, -1, -1, -1, -1, -1, -1, -1 },
		{ 3, 0, 8, 1, 2, 9, 2, 4, 9, 2, 6, 4, -1, -1, -1, -1 },
		{ 0, 2, 4, 4, 2, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
		{ 8, 3, 2, 8, 2, 4, 4, 2, 6, -1, -1, -1, -1, -1, -1, -1 },
		{ 10, 4, 9, 10, 6, 4, 11, 2, 3, -1, -1, -1, -1, -1, -1, -1 },
		{ 0, 8, 2, 2, 8, 11, 4, 9, 10, 4, 10, 6, -1, -1, -1, -1 },
		{ 3, 11, 2, 0, 1, 6, 0, 6, 4, 6, 1, 10, -1, -1, -1, -1 },
		{ 6, 4, 1, 6, 1, 10, 4, 8, 1, 2, 1, 11, 8, 11, 1, -1 },
		{ 9, 6, 4, 9, 3, 6, 9, 1, 3, 11, 6, 3, -1, -1, -1, -1 },
		{ 8, 11, 1, 8, 1, 0, 11, 6, 1, 9, 1, 4, 6, 4, 1, -1 },
		{ 3, 11, 6, 3, 6, 0, 0, 6, 4, -1, -1, -1, -1, -1, -1, -1 },
		{ 6, 4, 8, 11, 6, 8, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
		{ 7, 10, 6, 7, 8, 10, 8, 9, 10, -1, -1, -1, -1, -1, -1, -1 },
		{ 0, 7, 3, 0, 10, 7, 0, 9, 10, 6, 7, 10, -1, -1, -1, -1 },
		{ 10, 6, 7, 1, 10, 7, 1, 7, 8, 1, 8, 0, -1, -1, -1, -1 },
		{ 10, 6, 7, 10, 7, 1, 1, 7, 3, -1, -1, -1, -1, -1, -1, -1 },
		{ 1, 2, 6, 1, 6, 8, 1, 8, 9, 8, 6, 7, -1, -1, -1, -1 },
		{ 2, 6, 9, 2, 9, 1, 6, 7, 9, 0, 9, 3, 7, 3, 9, -1 },
		{ 7, 8, 0, 7, 0, 6, 6, 0, 2, -1, -1, -1, -1, -1, -1, -1 },
		{ 7, 3, 2, 6, 7, 2, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
		{ 2, 3, 11, 10, 6, 8, 10, 8, 9, 8, 6, 7, -1, -1, -1, -1 },
		{ 2, 0, 7, 2, 7, 11, 0, 9, 7, 6, 7, 10, 9, 10, 7, -1 },
		{ 1, 8, 0, 1, 7, 8, 1, 10, 7, 6, 7, 10, 2, 3, 11, -1 },
		{ 11, 2, 1, 11, 1, 7, 10, 6, 1, 6, 7, 1, -1, -1, -1, -1 },
		{ 8, 9, 6, 8, 6, 7, 9, 1, 6, 11, 6, 3, 1, 3, 6, -1 },
		{ 0, 9, 1, 11, 6, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
		{ 7, 8, 0, 7, 0, 6, 3, 11, 0, 11, 6, 0, -1, -1, -1, -1 },
		{ 7, 11, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
		{ 7, 6, 11, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
		{ 3, 0, 8, 11, 7, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
		{ 0, 1, 9, 11, 7, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
		{ 8, 1, 9, 8, 3, 1, 11, 7, 6, -1, -1, -1, -1, -1, -1, -1 },
		{ 10, 1, 2, 6, 11, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
		{ 1, 2, 10, 3, 0, 8, 6, 11, 7, -1, -1, -1, -1, -1, -1, -1 },
		{ 2, 9, 0, 2, 10, 9, 6, 11, 7, -1, -1, -1, -1, -1, -1, -1 },
		{ 6, 11, 7, 2, 10, 3, 10, 8, 3, 10, 9, 8, -1, -1, -1, -1 },
		{ 7, 2, 3, 6, 2, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
		{ 7, 0, 8, 7, 6, 0, 6, 2, 0, -1, -1, -1, -1, -1, -1, -1 },
		{ 2, 7, 6, 2, 3, 7, 0, 1, 9, -1, -1, -1, -1, -1, -1, -1 },
		{ 1, 6, 2, 1, 8, 6, 1, 9, 8, 8, 7, 6, -1, -1, -1, -1 },
		{ 10, 7, 6, 10, 1, 7, 1, 3, 7, -1, -1, -1, -1, -1, -1, -1 },
		{ 10, 7, 6, 1, 7, 10, 1, 8, 7, 1, 0, 8, -1, -1, -1, -1 },
		{ 0, 3, 7, 0, 7, 10, 0, 10, 9, 6, 10, 7, -1, -1, -1, -1 },
		{ 7, 6, 10, 7, 10, 8, 8, 10, 9, -1, -1, -1, -1, -1, -1, -1 },
		{ 6, 8, 4, 11, 8, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
		{ 3, 6, 11, 3, 0, 6, 0, 4, 6, -1, -1, -1, -1, -1, -1, -1 },
		{ 8, 6, 11, 8, 4, 6, 9, 0, 1, -1, -1, -1, -1, -1, -1, -1 },
		{ 9, 4, 6, 9, 6, 3, 9, 3, 1, 11, 3, 6, -1, -1, -1, -1 },
		{ 6, 8, 4, 6, 11, 8, 2, 10, 1, -1, -1, -1, -1, -1, -1, -1 },
		{ 1, 2, 10, 3, 0, 11, 0, 6, 11, 0, 4, 6, -1, -1, -1, -1 },
		{ 4, 11, 8, 4, 6, 11, 0, 2, 9, 2, 10, 9, -1, -1, -1, -1 },
		{ 10, 9, 3, 10, 3, 2, 9, 4, 3, 11, 3, 6, 4, 6, 3, -1 },
		{ 8, 2, 3, 8, 4, 2, 4, 6, 2, -1, -1, -1, -1, -1, -1, -1 },
		{ 0, 4, 2, 4, 6, 2, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
		{ 1, 9, 0, 2, 3, 4, 2, 4, 6, 4, 3, 8, -1, -1, -1, -1 },
		{ 1, 9, 4, 1, 4, 2, 2, 4, 6, -1, -1, -1, -1, -1, -1, -1 },
		{ 8, 1, 3, 8, 6, 1, 8, 4, 6, 6, 10, 1, -1, -1, -1, -1 },
		{ 10, 1, 0, 10, 0, 6, 6, 0, 4, -1, -1, -1, -1, -1, -1, -1 },
		{ 4, 6, 3, 4, 3, 8, 6, 10, 3, 0, 3, 9, 10, 9, 3, -1 },
		{ 10, 9, 4, 6, 10, 4, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
		{ 4, 9, 5, 7, 6, 11, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
		{ 0, 8, 3, 4, 9, 5, 11, 7, 6, -1, -1, -1, -1, -1, -1, -1 },
		{ 5, 0, 1, 5, 4, 0, 7, 6, 11, -1, -1, -1, -1, -1, -1, -1 },
		{ 11, 7, 6, 8, 3, 4, 3, 5, 4, 3, 1, 5, -1, -1, -1, -1 },
		{ 9, 5, 4, 10, 1, 2, 7, 6, 11, -1, -1, -1, -1, -1, -1, -1 },
		{ 6, 11, 7, 1, 2, 10, 0, 8, 3, 4, 9, 5, -1, -1, -1, -1 },
		{ 7, 6, 11, 5, 4, 10, 4, 2, 10, 4, 0, 2, -1, -1, -1, -1 },
		{ 3, 4, 8, 3, 5, 4, 3, 2, 5, 10, 5, 2, 11, 7, 6, -1 },
		{ 7, 2, 3, 7, 6, 2, 5, 4, 9, -1, -1, -1, -1, -1, -1, -1 },
		{ 9, 5, 4, 0, 8, 6, 0, 6, 2, 6, 8, 7, -1, -1, -1, -1 },
		{ 3, 6, 2, 3, 7, 6, 1, 5, 0, 5, 4, 0, -1, -1, -1, -1 },
		{ 6, 2, 8, 6, 8, 7, 2, 1, 8, 4, 8, 5, 1, 5, 8, -1 },
		{ 9, 5, 4, 10, 1, 6, 1, 7, 6, 1, 3, 7, -1, -1, -1, -1 },
		{ 1, 6, 10, 1, 7, 6, 1, 0, 7, 8, 7, 0, 9, 5, 4, -1 },
		{ 4, 0, 10, 4, 10, 5, 0, 3, 10, 6, 10, 7, 3, 7, 10, -1 },
		{ 7, 6, 10, 7, 10, 8, 5, 4, 10, 4, 8, 10, -1, -1, -1, -1 },
		{ 6, 9, 5, 6, 11, 9, 11, 8, 9, -1, -1, -1, -1, -1, -1, -1 },
		{ 3, 6, 11, 0, 6, 3, 0, 5, 6, 0, 9, 5, -1, -1, -1, -1 },
		{ 0, 11, 8, 0, 5, 11, 0, 1, 5, 5, 6, 11, -1, -1, -1, -1 },
		{ 6, 11, 3, 6, 3, 5, 5, 3, 1, -1, -1, -1, -1, -1, -1, -1 },
		{ 1, 2, 10, 9, 5, 11, 9, 11, 8, 11, 5, 6, -1, -1, -1, -1 },
		{ 0, 11, 3, 0, 6, 11, 0, 9, 6, 5, 6, 9, 1, 2, 10, -1 },
		{ 11, 8, 5, 11, 5, 6, 8, 0, 5, 10, 5, 2, 0, 2, 5, -1 },
		{ 6, 11, 3, 6, 3, 5, 2, 10, 3, 10, 5, 3, -1, -1, -1, -1 },
		{ 5, 8, 9, 5, 2, 8, 5, 6, 2, 3, 8, 2, -1, -1, -1, -1 },
		{ 9, 5, 6, 9, 6, 0, 0, 6, 2, -1, -1, -1, -1, -1, -1, -1 },
		{ 1, 5, 8, 1, 8, 0, 5, 6, 8, 3, 8, 2, 6, 2, 8, -1 },
		{ 1, 5, 6, 2, 1, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
		{ 1, 3, 6, 1, 6, 10, 3, 8, 6, 5, 6, 9, 8, 9, 6, -1 },
		{ 10, 1, 0, 10, 0, 6, 9, 5, 0, 5, 6, 0, -1, -1, -1, -1 },
		{ 0, 3, 8, 5, 6, 10, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
		{ 10, 5, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
		{ 11, 5, 10, 7, 5, 11, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
		{ 11, 5, 10, 11, 7, 5, 8, 3, 0, -1, -1, -1, -1, -1, -1, -1 },
		{ 5, 11, 7, 5, 10, 11, 1, 9, 0, -1, -1, -1, -1, -1, -1, -1 },
		{ 10, 7, 5, 10, 11, 7, 9, 8, 1, 8, 3, 1, -1, -1, -1, -1 },
		{ 11, 1, 2, 11, 7, 1, 7, 5, 1, -1, -1, -1, -1, -1, -1, -1 },
		{ 0, 8, 3, 1, 2, 7, 1, 7, 5, 7, 2, 11, -1, -1, -1, -1 },
		{ 9, 7, 5, 9, 2, 7, 9, 0, 2, 2, 11, 7, -1, -1, -1, -1 },
		{ 7, 5, 2, 7, 2, 11, 5, 9, 2, 3, 2, 8, 9, 8, 2, -1 },
		{ 2, 5, 10, 2, 3, 5, 3, 7, 5, -1, -1, -1, -1, -1, -1, -1 },
		{ 8, 2, 0, 8, 5, 2, 8, 7, 5, 10, 2, 5, -1, -1, -1, -1 },
		{ 9, 0, 1, 5, 10, 3, 5, 3, 7, 3, 10, 2, -1, -1, -1, -1 },
		{ 9, 8, 2, 9, 2, 1, 8, 7, 2, 10, 2, 5, 7, 5, 2, -1 },
		{ 1, 3, 5, 3, 7, 5, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
		{ 0, 8, 7, 0, 7, 1, 1, 7, 5, -1, -1, -1, -1, -1, -1, -1 },
		{ 9, 0, 3, 9, 3, 5, 5, 3, 7, -1, -1, -1, -1, -1, -1, -1 },
		{ 9, 8, 7, 5, 9, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
		{ 5, 8, 4, 5, 10, 8, 10, 11, 8, -1, -1, -1, -1, -1, -1, -1 },
		{ 5, 0, 4, 5, 11, 0, 5, 10, 11, 11, 3, 0, -1, -1, -1, -1 },
		{ 0, 1, 9, 8, 4, 10, 8, 10, 11, 10, 4, 5, -1, -1, -1, -1 },
		{ 10, 11, 4, 10, 4, 5, 11, 3, 4, 9, 4, 1, 3, 1, 4, -1 },
		{ 2, 5, 1, 2, 8, 5, 2, 11, 8, 4, 5, 8, -1, -1, -1, -1 },
		{ 0, 4, 11, 0, 11, 3, 4, 5, 11, 2, 11, 1, 5, 1, 11, -1 },
		{ 0, 2, 5, 0, 5, 9, 2, 11, 5, 4, 5, 8, 11, 8, 5, -1 },
		{ 9, 4, 5, 2, 11, 3, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
		{ 2, 5, 10, 3, 5, 2, 3, 4, 5, 3, 8, 4, -1, -1, -1, -1 },
		{ 5, 10, 2, 5, 2, 4, 4, 2, 0, -1, -1, -1, -1, -1, -1, -1 },
		{ 3, 10, 2, 3, 5, 10, 3, 8, 5, 4, 5, 8, 0, 1, 9, -1 },
		{ 5, 10, 2, 5, 2, 4, 1, 9, 2, 9, 4, 2, -1, -1, -1, -1 },
		{ 8, 4, 5, 8, 5, 3, 3, 5, 1, -1, -1, -1, -1, -1, -1, -1 },
		{ 0, 4, 5, 1, 0, 5, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
		{ 8, 4, 5, 8, 5, 3, 9, 0, 5, 0, 3, 5, -1, -1, -1, -1 },
		{ 9, 4, 5, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
		{ 4, 11, 7, 4, 9, 11, 9, 10, 11, -1, -1, -1, -1, -1, -1, -1 },
		{ 0, 8, 3, 4, 9, 7, 9, 11, 7, 9, 10, 11, -1, -1, -1, -1 },
		{ 1, 10, 11, 1, 11, 4, 1, 4, 0, 7, 4, 11, -1, -1, -1, -1 },
		{ 3, 1, 4, 3, 4, 8, 1, 10, 4, 7, 4, 11, 10, 11, 4, -1 },
		{ 4, 11, 7, 9, 11, 4, 9, 2, 11, 9, 1, 2, -1, -1, -1, -1 },
		{ 9, 7, 4, 9, 11, 7, 9, 1, 11, 2, 11, 1, 0, 8, 3, -1 },
		{ 11, 7, 4, 11, 4, 2, 2, 4, 0, -1, -1, -1, -1, -1, -1, -1 },
		{ 11, 7, 4, 11, 4, 2, 8, 3, 4, 3, 2, 4, -1, -1, -1, -1 },
		{ 2, 9, 10, 2, 7, 9, 2, 3, 7, 7, 4, 9, -1, -1, -1, -1 },
		{ 9, 10, 7, 9, 7, 4, 10, 2, 7, 8, 7, 0, 2, 0, 7, -1 },
		{ 3, 7, 10, 3, 10, 2, 7, 4, 10, 1, 10, 0, 4, 0, 10, -1 },
		{ 1, 10, 2, 8, 7, 4, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
		{ 4, 9, 1, 4, 1, 7, 7, 1, 3, -1, -1, -1, -1, -1, -1, -1 },
		{ 4, 9, 1, 4, 1, 7, 0, 8, 1, 8, 7, 1, -1, -1, -1, -1 },
		{ 4, 0, 3, 7, 4, 3, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
		{ 4, 8, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
		{ 9, 10, 8, 10, 11, 8, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
		{ 3, 0, 9, 3, 9, 11, 11, 9, 10, -1, -1, -1, -1, -1, -1, -1 },
		{ 0, 1, 10, 0, 10, 8, 8, 10, 11, -1, -1, -1, -1, -1, -1, -1 },
		{ 3, 1, 10, 11, 3, 10, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
		{ 1, 2, 11, 1, 11, 9, 9, 11, 8, -1, -1, -1, -1, -1, -1, -1 },
		{ 3, 0, 9, 3, 9, 11, 1, 2, 9, 2, 11, 9, -1, -1, -1, -1 },
		{ 0, 2, 11, 8, 0, 11, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
		{ 3, 2, 11, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
		{ 2, 3, 8, 2, 8, 10, 10, 8, 9, -1, -1, -1, -1, -1, -1, -1 },
		{ 9, 10, 2, 0, 9, 2, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
		{ 2, 3, 8, 2, 8, 10, 0, 1, 8, 1, 10, 8, -1, -1, -1, -1 },
		{ 1, 10, 2, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
		{ 1, 3, 8, 9, 1, 8, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
		{ 0, 9, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
		{ 0, 3, 8, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
		{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 } };
	
	public static ArrayList<Float> delneKockeMarching(float[][][] CTMatrix, float isolevel, int meja, int recursion) {
		
		int GLOBINA = 0;
		int NASLEDNJI = 0;
		
		Map<String, Vertex[]> cubes = new TreeMap<String, Vertex[]>();
		Map<String, Integer> pogoji = new TreeMap<String, Integer>();
		Map<String, Integer> indeksiZank = new TreeMap<String, Integer>();
		Map<String, Integer> indeksi =  new TreeMap<String, Integer>();
		ArrayList<Float> vertices = new ArrayList<Float>();
		
		Vertex[] cubesVertices = new Vertex[8];
		for (int i = 0; i < cubesVertices.length; i++){
			cubesVertices[i] = new Vertex(0, 0, 0);
		}
		
		for(int i = 2; i <= recursion; i++){
			cubes.put("cubesVer"+i, new Vertex[8]);
			indeksi.put("cubeIndex"+i, 0);
			for(int j=0; j < cubes.get("cubesVer"+i).length; j++){
				cubes.get("cubesVer"+i)[j] = new Vertex(0, 0, 0);
			}
		}
		
		for (int z = 0; z < meja; z++) {
			for (int y = 0; y < meja; y++) {
				for (int x = 0; x < meja; x++) {
					int left = x;
					int right = x + 1;
					int top = y;
					int bottom = y + 1;
					int back = z + 1;
					int front = z;
					
					cubesVertices[0].setVertex(left, bottom, back, CTMatrix[bottom][left][back]);
					cubesVertices[1].setVertex(right, bottom, back, CTMatrix[bottom][right][back]);
					cubesVertices[2].setVertex(right, bottom, front, CTMatrix[bottom][right][front]);
					cubesVertices[3].setVertex(left, bottom, front, CTMatrix[bottom][left][front]);
					cubesVertices[4].setVertex(left, top, back, CTMatrix[top][left][back]);
					cubesVertices[5].setVertex(right, top, back, CTMatrix[top][right][back]);
					cubesVertices[6].setVertex(right, top, front, CTMatrix[top][right][front]);
					cubesVertices[7].setVertex(left, top, front, CTMatrix[top][left][front]);
					
					cubes.put("cubesVer1", cubesVertices);
					
					int cubeIndex = 0;
					// 0
					if (cubesVertices[0].value < isolevel)
						cubeIndex |= 1;
					// 1
					if (cubesVertices[1].value < isolevel)
						cubeIndex |= 2;
					// 2
					if (cubesVertices[2].value < isolevel)
						cubeIndex |= 4;
					// 3
					if (cubesVertices[3].value < isolevel)
						cubeIndex |= 8;
					// 4
					if (cubesVertices[4].value < isolevel)
						cubeIndex |= 16;
					// 5
					if (cubesVertices[5].value < isolevel)
						cubeIndex |= 32;
					// 6
					if (cubesVertices[6].value < isolevel)
						cubeIndex |= 64;
					// 7
					if (cubesVertices[7].value < isolevel)
						cubeIndex |= 128;

					if (cubeIndex == 0)
						continue;

					Vertex[] v = new Vertex[12];
					if ((edgeTable[cubeIndex] & 1) == 1)
						v[0] = linearInterpolate(cubesVertices[0], cubesVertices[1], isolevel);
					if ((edgeTable[cubeIndex] & 2) == 2)
						v[1] = linearInterpolate(cubesVertices[1], cubesVertices[2], isolevel);
					if ((edgeTable[cubeIndex] & 4) == 4)
						v[2] = linearInterpolate(cubesVertices[2], cubesVertices[3], isolevel);
					if ((edgeTable[cubeIndex] & 8) == 8)
						v[3] = linearInterpolate(cubesVertices[3], cubesVertices[0], isolevel);
					if ((edgeTable[cubeIndex] & 16) == 16)
						v[4] = linearInterpolate(cubesVertices[4], cubesVertices[5], isolevel);
					if ((edgeTable[cubeIndex] & 32) == 32)
						v[5] = linearInterpolate(cubesVertices[5], cubesVertices[6], isolevel);
					if ((edgeTable[cubeIndex] & 64) == 64)
						v[6] = linearInterpolate(cubesVertices[6], cubesVertices[7], isolevel);
					if ((edgeTable[cubeIndex] & 128) == 128)
						v[7] = linearInterpolate(cubesVertices[7], cubesVertices[4], isolevel);
					if ((edgeTable[cubeIndex] & 256) == 256)
						v[8] = linearInterpolate(cubesVertices[0], cubesVertices[4], isolevel);
					if ((edgeTable[cubeIndex] & 512) == 512)
						v[9] = linearInterpolate(cubesVertices[1], cubesVertices[5], isolevel);
					if ((edgeTable[cubeIndex] & 1024) == 1024)
						v[10] = linearInterpolate(cubesVertices[2], cubesVertices[6], isolevel);
					if ((edgeTable[cubeIndex] & 2048) == 2048)
						v[11] = linearInterpolate(cubesVertices[3], cubesVertices[7], isolevel);

					// now build the triangles using triTable
					for (int n = 0; triTable[cubeIndex][n] != -1; n += 3) {
						Vertex first = v[triTable[cubeIndex][n]];						
						Vertex second = v[triTable[cubeIndex][n + 1]];
						Vertex third = v[triTable[cubeIndex][n + 2]];
						vertices.add(first.x);
						vertices.add(first.y);
						vertices.add(first.z);
						vertices.add(second.x);
						vertices.add(second.y);
						vertices.add(second.z);
						vertices.add(third.x);
						vertices.add(third.y);
						vertices.add(third.z);
					}

					//REKURZIJA
					rekurzijaKockeMarching(GLOBINA, NASLEDNJI, recursion, isolevel, indeksi, cubes,
							pogoji, indeksiZank, vertices);						 
				}
			}
		}
		return vertices;
	}
	
	private static Vertex linearInterpolate(Vertex first, Vertex second, float isolevel) {

		if (Math.abs(isolevel - first.value) < 0.00001)
			return (second);
		if (Math.abs(isolevel - second.value) < 0.00001)
			return (first);
		if (Math.abs(first.value - second.value) < 0.00001)
			return (first);
		double mu = (isolevel - first.value) / (second.value - first.value);
		float x = (float) (first.x + mu * (second.x - first.x));
		float y = (float) (first.y + mu * (second.y - first.y));
		float z = (float) (first.z + mu * (second.z - first.z));

		return new Vertex(x, y, z);
	}
	
	private static void rekurzijaKockeMarching(int GLOBINA, int NASLEDNJI, int MEJAGLOBINE, float isolevel,
			Map<String, Integer> indeksi, Map<String, Vertex[]> cubes, Map<String, Integer> pogoji,
			Map<String, Integer> indeksiZank, ArrayList<Float> vertices) {
		
		GLOBINA++;
		NASLEDNJI = GLOBINA + 1;
		
		if(GLOBINA >= MEJAGLOBINE){
			return;
		}

		for (indeksiZank.put("a"+NASLEDNJI, 0); indeksiZank.get("a"+NASLEDNJI) < cubes.get("cubesVer"+NASLEDNJI).length; indeksiZank.put("a"+NASLEDNJI,indeksiZank.get("a"+NASLEDNJI)+1)){	
			for(int b = 0; b < cubes.get("cubesVer"+NASLEDNJI).length; b++){
				if(indeksiZank.get("a"+NASLEDNJI) != b){
					float pointX = (cubes.get("cubesVer"+GLOBINA)[indeksiZank.get("a"+NASLEDNJI)].x + cubes.get("cubesVer"+GLOBINA)[b].x)/2;
					float pointY = (cubes.get("cubesVer"+GLOBINA)[indeksiZank.get("a"+NASLEDNJI)].y + cubes.get("cubesVer"+GLOBINA)[b].y)/2;
					float pointZ = (cubes.get("cubesVer"+GLOBINA)[indeksiZank.get("a"+NASLEDNJI)].z + cubes.get("cubesVer"+GLOBINA)[b].z)/2;
					float pointValue = (cubes.get("cubesVer"+GLOBINA)[indeksiZank.get("a"+NASLEDNJI)].value + cubes.get("cubesVer"+GLOBINA)[b].value)/2;
				
					cubes.get("cubesVer"+NASLEDNJI)[b].setVertex(pointX, pointY, pointZ, pointValue);			
				}else{
					cubes.get("cubesVer"+NASLEDNJI)[b].setVertex(cubes.get("cubesVer"+GLOBINA)[b].x, cubes.get("cubesVer"+GLOBINA)[b].y, cubes.get("cubesVer"+GLOBINA)[b].z, cubes.get("cubesVer"+GLOBINA)[b].value);									
				}
			}
			
			//int cubeIndex = 0;
			indeksi.put("cubeIndex"+NASLEDNJI,0);

			// 0
			if (cubes.get("cubesVer"+NASLEDNJI)[0].value < isolevel) {
				indeksi.put("cubeIndex"+NASLEDNJI,indeksi.get("cubeIndex"+NASLEDNJI) | 1);
			}
			// 1
			if (cubes.get("cubesVer"+NASLEDNJI)[1].value < isolevel)
				indeksi.put("cubeIndex"+NASLEDNJI,indeksi.get("cubeIndex"+NASLEDNJI) | 2);
			// 2
			if (cubes.get("cubesVer"+NASLEDNJI)[2].value < isolevel)
				indeksi.put("cubeIndex"+NASLEDNJI,indeksi.get("cubeIndex"+NASLEDNJI) | 4);
			// 3
			if (cubes.get("cubesVer"+NASLEDNJI)[3].value < isolevel)
				indeksi.put("cubeIndex"+NASLEDNJI,indeksi.get("cubeIndex"+NASLEDNJI) | 8);
			// 4
			if (cubes.get("cubesVer"+NASLEDNJI)[4].value < isolevel)
				indeksi.put("cubeIndex"+NASLEDNJI,indeksi.get("cubeIndex"+NASLEDNJI) | 16);
			// 5
			if (cubes.get("cubesVer"+NASLEDNJI)[5].value < isolevel)
				indeksi.put("cubeIndex"+NASLEDNJI,indeksi.get("cubeIndex"+NASLEDNJI) | 32);
			// 6
			if (cubes.get("cubesVer"+NASLEDNJI)[6].value < isolevel)
				indeksi.put("cubeIndex"+NASLEDNJI,indeksi.get("cubeIndex"+NASLEDNJI) | 64);
			// 7
			if (cubes.get("cubesVer"+NASLEDNJI)[7].value < isolevel)
				indeksi.put("cubeIndex"+NASLEDNJI,indeksi.get("cubeIndex"+NASLEDNJI) | 128);

			if (indeksi.get("cubeIndex"+NASLEDNJI) == 0)
				continue;
			//System.out.println(indeksi.get("cubeIndex"+NASLEDNJI));

			Vertex[] v = new Vertex[12];
			if ((edgeTable[indeksi.get("cubeIndex"+NASLEDNJI)] & 1) == 1)
				v[0] = linearInterpolate(cubes.get("cubesVer"+NASLEDNJI)[0], cubes.get("cubesVer"+NASLEDNJI)[1], isolevel);
			if ((edgeTable[indeksi.get("cubeIndex"+NASLEDNJI)] & 2) == 2)
				v[1] = linearInterpolate(cubes.get("cubesVer"+NASLEDNJI)[1], cubes.get("cubesVer"+NASLEDNJI)[2], isolevel);
			if ((edgeTable[indeksi.get("cubeIndex"+NASLEDNJI)] & 4) == 4)
				v[2] = linearInterpolate(cubes.get("cubesVer"+NASLEDNJI)[2], cubes.get("cubesVer"+NASLEDNJI)[3], isolevel);
			if ((edgeTable[indeksi.get("cubeIndex"+NASLEDNJI)] & 8) == 8)
				v[3] = linearInterpolate(cubes.get("cubesVer"+NASLEDNJI)[3], cubes.get("cubesVer"+NASLEDNJI)[0], isolevel);
			if ((edgeTable[indeksi.get("cubeIndex"+NASLEDNJI)] & 16) == 16)
				v[4] = linearInterpolate(cubes.get("cubesVer"+NASLEDNJI)[4], cubes.get("cubesVer"+NASLEDNJI)[5], isolevel);
			if ((edgeTable[indeksi.get("cubeIndex"+NASLEDNJI)] & 32) == 32)
				v[5] = linearInterpolate(cubes.get("cubesVer"+NASLEDNJI)[5], cubes.get("cubesVer"+NASLEDNJI)[6], isolevel);
			if ((edgeTable[indeksi.get("cubeIndex"+NASLEDNJI)] & 64) == 64)
				v[6] = linearInterpolate(cubes.get("cubesVer"+NASLEDNJI)[6], cubes.get("cubesVer"+NASLEDNJI)[7], isolevel);
			if ((edgeTable[indeksi.get("cubeIndex"+NASLEDNJI)] & 128) == 128)
				v[7] = linearInterpolate(cubes.get("cubesVer"+NASLEDNJI)[7], cubes.get("cubesVer"+NASLEDNJI)[4], isolevel);
			if ((edgeTable[indeksi.get("cubeIndex"+NASLEDNJI)] & 256) == 256)
				v[8] = linearInterpolate(cubes.get("cubesVer"+NASLEDNJI)[0], cubes.get("cubesVer"+NASLEDNJI)[4], isolevel);
			if ((edgeTable[indeksi.get("cubeIndex"+NASLEDNJI)] & 512) == 512)
				v[9] = linearInterpolate(cubes.get("cubesVer"+NASLEDNJI)[1], cubes.get("cubesVer"+NASLEDNJI)[5], isolevel);
			if ((edgeTable[indeksi.get("cubeIndex"+NASLEDNJI)] & 1024) == 1024)
				v[10] = linearInterpolate(cubes.get("cubesVer"+NASLEDNJI)[2], cubes.get("cubesVer"+NASLEDNJI)[6], isolevel);
			if ((edgeTable[indeksi.get("cubeIndex"+NASLEDNJI)] & 2048) == 2048)
				v[11] = linearInterpolate(cubes.get("cubesVer"+NASLEDNJI)[3], cubes.get("cubesVer"+NASLEDNJI)[7], isolevel);
			
			// now build the triangles using triTable
			for (int n = 0; triTable[indeksi.get("cubeIndex"+NASLEDNJI)][n] != -1; n += 3) {
				Vertex first = v[triTable[indeksi.get("cubeIndex"+NASLEDNJI)][n]];						
				Vertex second = v[triTable[indeksi.get("cubeIndex"+NASLEDNJI)][n + 1]];
				Vertex third = v[triTable[indeksi.get("cubeIndex"+NASLEDNJI)][n + 2]];
				vertices.add(first.x);
				vertices.add(first.y);
				vertices.add(first.z);
				vertices.add(second.x);
				vertices.add(second.y);
				vertices.add(second.z);
				vertices.add(third.x);
				vertices.add(third.y);
				vertices.add(third.z);
			}
			
			rekurzijaKockeMarching(GLOBINA, NASLEDNJI, MEJAGLOBINE, isolevel, indeksi, cubes,
					pogoji, indeksiZank, vertices);
		}
		GLOBINA--;
		NASLEDNJI = GLOBINA + 1;
	}
	
	
	
	//DELUJE; AMPAK NI REKURZIJE
	/*
	for (int a1 = 0; a1 < cubeVer2.length; a1++){
		pogoj2 = 0;
		for(int b = 0; b < cubeVer2.length; b++){
			if(a1 != b){
				float pointX = (cubesVertices[a1].x + cubesVertices[b].x)/2;
				float pointY = (cubesVertices[a1].y + cubesVertices[b].y)/2;
				float pointZ = (cubesVertices[a1].z + cubesVertices[b].z)/2;
				float pointValue = (cubesVertices[a1].value + cubesVertices[b].value)/2;
			
				cubeVer2[b].setVertex(pointX, pointY,	pointZ, pointValue);
			}else{
				cubeVer2[b].setVertex(cubesVertices[b].x, cubesVertices[b].y,	cubesVertices[b].z, cubesVertices[b].value);
			}
		}
		
		for(int pog = 0; pog < cubeVer2.length; pog++){
			if(cubeVer2[pog].value < isolevel){
				pogoj2++;
			}
		}
		
		if(pogoj2 == 0){
			for (int n = 0; n < 36 ; n +=3) {
				Vertex first = cubeVer2[indeksi[n]];
				Vertex second = cubeVer2[indeksi[n + 1]];
				Vertex third = cubeVer2[indeksi[n + 2]];
				vertices.add(first.x);
				vertices.add(first.y);
				vertices.add(first.z);
				vertices.add(second.x);
				vertices.add(second.y);
				vertices.add(second.z);
				vertices.add(third.x);
				vertices.add(third.y);
				vertices.add(third.z);
			}
		}
		
		
		if(pogoj2 != 0){
			for (int a2 = 0; a2 < cubeVer3.length; a2++){
				pogoj3 = 0;
				for(int b = 0; b < cubeVer3.length; b++){
					if(a2 != b){
						float pointX = (cubeVer2[a2].x + cubeVer2[b].x)/2;
						float pointY = (cubeVer2[a2].y + cubeVer2[b].y)/2;
						float pointZ = (cubeVer2[a2].z + cubeVer2[b].z)/2;
						float pointValue = (cubeVer2[a2].value + cubeVer2[b].value)/2;
					
						cubeVer3[b].setVertex(pointX, pointY,	pointZ, pointValue);
					}else{
						cubeVer3[b].setVertex(cubeVer2[b].x, cubeVer2[b].y,	cubeVer2[b].z, cubeVer2[b].value);
					}
				}
				
				for(int pog = 0; pog < cubeVer3.length; pog++){
					if(cubeVer3[pog].value < isolevel){
						pogoj3++;
					}
				}
				
				if(pogoj3 == 0){
					for (int n = 0; n < 36 ; n +=3) {
						Vertex first = cubeVer3[indeksi[n]];
						Vertex second = cubeVer3[indeksi[n + 1]];
						Vertex third = cubeVer3[indeksi[n + 2]];
						vertices.add(first.x);
						vertices.add(first.y);
						vertices.add(first.z);
						vertices.add(second.x);
						vertices.add(second.y);
						vertices.add(second.z);
						vertices.add(third.x);
						vertices.add(third.y);
						vertices.add(third.z);
					}
				}
			}
		}
	}
	*/
	
	
	
	
	public static class Vertex {
		public float x, y, z, value;

		public Vertex(float x, float y, float z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}

		public void setVertex(float x, float y, float z, float value) {
			this.x = x;
			this.y = y;
			this.z = z;
			this.value = value;
		}
		
		public String getVertex() {
			String deli = "X: "+x+", Y: "+y+", Z: "+z;
			return deli;
		}
	}
}
