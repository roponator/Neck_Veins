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
	
	public static ArrayList<Float> delneKocke(float[][][] CTMatrix, float isolevel, int meja) {
		
		int MEJAGLOBINE = 3;
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
		
		
		Vertex[] cubeVer2 = new Vertex[8];
		Vertex[] cubeVer3 = new Vertex[8];
		
		for (int i = 0; i < cubesVertices.length; i++){
			cubeVer2[i] = new Vertex(0, 0, 0);
			cubeVer3[i] = new Vertex(0, 0, 0);
		}
		cubes.put("cubesVer2", cubeVer2);
		cubes.put("cubesVer3", cubeVer3);
		
		
		/*
		int pogoj2;
		int pogoj3;
		
		Vertex[] cubeVer2 = new Vertex[8];
		Vertex[] cubeVer3 = new Vertex[8];
		
		for (int i = 0; i < cubesVertices.length; i++){
			cubeVer2[i] = new Vertex(0, 0, 0);
			cubeVer3[i] = new Vertex(0, 0, 0);
		}
		*/
		
		int indeksi[] =  { 
			0, 2, 1, 2, 3, 1, 
			1, 3, 4, 3, 5, 4, 
			4, 5, 6, 5, 7, 6, 
			6, 7, 0, 7, 2, 0, 
			6, 0, 4, 0, 1, 4,
			2, 7, 3, 7, 5, 3 
		};
		
		int stevec =0;
		
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
					//GLOBINA = 0;
					
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
					
					if(pogoj != 0){
						//REKURZIJA
						
						//rekurzijaKocke(GLOBINA, MEJAGLOBINE, isolevel, indeksi, cubes,
						//		pogoji, indeksiZank, vertices);
						
						//DELUJE SAMO 0.5x0.5x0.5
						
						//GLOBINA = 1
						//NASLEDNJI = 2
						GLOBINA = 1;
						NASLEDNJI = GLOBINA + 1;
						
						for (indeksiZank.put("a"+NASLEDNJI, 0); indeksiZank.get("a"+NASLEDNJI) < cubes.get("cubesVer"+NASLEDNJI).length; indeksiZank.put("a"+NASLEDNJI,indeksiZank.get("a"+NASLEDNJI)+1)){
							stevec++;
							//if(indeksiZank.get("a"+NASLEDNJI) == 8){
							//	System.out.println(GLOBINA);
							//	System.out.println(NASLEDNJI);
							//}
							
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
							
							
							if(pogoji.get("pogoj"+NASLEDNJI) != 0){
								//GLOBINA = 2
								//NASLEDNJI = 3
								GLOBINA = 2;
								NASLEDNJI = GLOBINA + 1;
								
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
									
								}
								GLOBINA = 1;
								NASLEDNJI = GLOBINA + 1;
							}
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
						 
					}
				}
			}
		}
		System.out.println(stevec);
		return vertices;
	}
	
	private static void rekurzijaKocke(int GLOBINA, int MEJAGLOBINE, float isolevel,
			int indeksi[], Map<String, Vertex[]> cubes, Map<String, Integer> pogoji,
			Map<String, Integer> indeksiZank, ArrayList<Float> vertices) {
		
		if(GLOBINA >= MEJAGLOBINE){
			return;
		}
		
		int NASLEDNJI=GLOBINA+1;
		//System.out.println(indeksiZank.get("a"+NASLEDNJI));
		//System.out.println(cubes.get("cubesVer"+GLOBINA).length);
		//System.out.println(indeksiZank.put("a"+NASLEDNJI,indeksiZank.get("a"+NASLEDNJI)+1));
		//System.out.println(indeksiZank.get("a"+NASLEDNJI));
		//System.out.println(NASLEDNJI);
		//System.out.println();
		
		for (indeksiZank.get("a"+NASLEDNJI); indeksiZank.get("a"+NASLEDNJI) < cubes.get("cubesVer"+GLOBINA).length; indeksiZank.put("a"+NASLEDNJI,indeksiZank.get("a"+NASLEDNJI)+1)){
			//System.out.println(GLOBINA);
			//System.out.println(NASLEDNJI);
			for(int b = 0; b < cubes.get("cubesVer"+NASLEDNJI).length; b++){
				if(indeksiZank.get("a"+NASLEDNJI) != b){
					float pointX = (cubes.get("cubesVer"+GLOBINA)[indeksiZank.get("a"+NASLEDNJI)].x + cubes.get("cubesVer"+GLOBINA)[b].x)/2;
					float pointY = (cubes.get("cubesVer"+GLOBINA)[indeksiZank.get("a"+NASLEDNJI)].y + cubes.get("cubesVer"+GLOBINA)[b].y)/2;
					float pointZ = (cubes.get("cubesVer"+GLOBINA)[indeksiZank.get("a"+NASLEDNJI)].z + cubes.get("cubesVer"+GLOBINA)[b].z)/2;
					float pointValue = (cubes.get("cubesVer"+GLOBINA)[indeksiZank.get("a"+NASLEDNJI)].value + cubes.get("cubesVer"+GLOBINA)[b].value)/2;
					
					//tabela[b].setVertex(pointX, pointY,	pointZ, pointValue);
					cubes.get("cubesVer"+NASLEDNJI)[b].setVertex(pointX, pointY, pointZ, pointValue);
				}else{
					//tabela[b].setVertex(cubes.get("cubesVer"+GLOBINA)[b].x, cubes.get("cubesVer"+GLOBINA)[b].y,	cubes.get("cubesVer"+GLOBINA)[b].z, cubes.get("cubesVer"+GLOBINA)[b].value);
					cubes.get("cubesVer"+NASLEDNJI)[b].setVertex(cubes.get("cubesVer"+GLOBINA)[b].x, cubes.get("cubesVer"+GLOBINA)[b].y, cubes.get("cubesVer"+GLOBINA)[b].z, cubes.get("cubesVer"+GLOBINA)[b].value);
				}
			}
			
			for(int pog = 0; pog < cubes.get("cubesVer"+NASLEDNJI).length; pog++){
				if(cubes.get("cubesVer"+NASLEDNJI)[pog].value < isolevel){
					pogoji.put("pogoj"+NASLEDNJI, pogoji.get("pogoj"+NASLEDNJI)+1);
				}
			}
			
			if(pogoji.get("pogoj"+NASLEDNJI) == 0){
				//System.out.println("blabla");
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
			
			if(pogoji.get("pogoj"+NASLEDNJI) != 0 && GLOBINA < MEJAGLOBINE){
				GLOBINA++;
				rekurzijaKocke(GLOBINA, MEJAGLOBINE, isolevel, indeksi, cubes,
						pogoji, indeksiZank, vertices);
			}else{
				return;
			}
		}
	}

	public static ArrayList<Float> razcepiKocko() {		
		ArrayList<Float> vertices = new ArrayList<Float>();
		Vertex[] cubesVertices = new Vertex[8];
		
		for (int i = 0; i < cubesVertices.length; i++){
			cubesVertices[i] = new Vertex(0, 0, 0);
		}
		
		int indeksi[] =  { 
			0, 2, 1, 2, 3, 1, 
			1, 3, 4, 3, 5, 4, 
			4, 5, 6, 5, 7, 6, 
			6, 7, 0, 7, 2, 0, 
			6, 0, 4, 0, 1, 4,
			2, 7, 3, 7, 5, 3 
		};
		
		//indeksi
		cubesVertices[7].setVertex(0, 		0,		10, 	613); //7
		cubesVertices[5].setVertex(10, 		0,		10, 	526); //5
		cubesVertices[4].setVertex(10, 		10,		10, 	623); //4
		cubesVertices[6].setVertex(0, 		10,		10, 	454); //6
		cubesVertices[2].setVertex(0, 		0, 		0, 		501); //2
		cubesVertices[3].setVertex(10, 		0, 		0, 		511); //3
		cubesVertices[1].setVertex(10, 		10,		0, 		536); //1
		cubesVertices[0].setVertex(0, 		10,		0, 		450); //0
		
		/*double distanceX = Math.pow(cubesVertices[0].x - cubesVertices[1].x, 2);
		double distanceY = Math.pow(cubesVertices[0].y - cubesVertices[1].y, 2);
		double distanceZ = Math.pow(cubesVertices[0].z - cubesVertices[1].z, 2);
		
		double distance = Math.sqrt(distanceX + distanceY + distanceZ);
		float polovica = (float) distance/2;
		System.out.println(polovica);*/
		
		Vertex[] litleCubes = new Vertex[8];
		
		for (int i = 0; i < litleCubes.length; i++){
			litleCubes[i] = new Vertex(0, 0, 0);
		}
		
		int pogoj = 0;
		
		for (int a = 0; a < litleCubes.length; a++){
			for(int b = 0; b < litleCubes.length; b++){
				if(a != b){
					float pointX = (cubesVertices[a].x + cubesVertices[b].x)/2;
					float pointY = (cubesVertices[a].y + cubesVertices[b].y)/2;
					float pointZ = (cubesVertices[a].z + cubesVertices[b].z)/2;
					float pointValue = (cubesVertices[a].value + cubesVertices[b].value)/2;
				
					litleCubes[b].setVertex(pointX, pointY,	pointZ, pointValue);
				}else{
					litleCubes[b].setVertex(cubesVertices[b].x, cubesVertices[b].y,	cubesVertices[b].z, cubesVertices[b].value);
				}
			}
			
			/*for(int pog = 0; pog < cubesVertices.length; pog++){
				System.out.println(cubesVertices[pog].getVertex());
			}
			System.out.println();*/
			
			for(int pog = 0; pog < litleCubes.length; pog++){
				if(litleCubes[pog].value < 400){
					pogoj++;
				}
			}
			
			if(pogoj == 0){
				for (int n = 0; n < 36 ; n +=3) {
					Vertex first = litleCubes[indeksi[n]];
					//System.out.println(first.getVertex());
					Vertex second = litleCubes[indeksi[n + 1]];
					//System.out.println(second.getVertex());
					Vertex third = litleCubes[indeksi[n + 2]];
					//System.out.println(third.getVertex());
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
			//System.out.println();
		}
		
		return vertices;
	}
	
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
