package si.uni_lj.fri.segmentation.utils;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferShort;
import java.awt.image.Raster;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.nio.channels.FileChannel;

import javax.imageio.ImageIO;

import si.uni_lj.fri.MPU_Implicits.Configuration;
import si.uni_lj.fri.segmentation.MHDReader;
import si.uni_lj.fri.veins3D.main.VeinsWindow;

public class FileUtils {

	/**
	 * Reads OpenCL file
	 * 
	 * @param name
	 * @return
	 */
	public static String loadText(String name) {
		if (!name.endsWith(".cls")) {
			name += ".cls";
		}
		BufferedReader br = null;
		String resultString = null;
		try {
			br = new BufferedReader(new InputStreamReader(VeinsWindow.class.getResourceAsStream(name)));
			String line = null;
			StringBuilder result = new StringBuilder();
			while ((line = br.readLine()) != null) {
				result.append(line);
				result.append("\n");
			}
			resultString = result.toString();
		} catch (NullPointerException npe) {
			System.err.println("Error retrieving OpenCL source file: ");
			npe.printStackTrace();
		} catch (IOException ioe) {
			System.err.println("Error reading OpenCL source file: ");
			ioe.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException ex) {
				System.err.println("Error closing OpenCL source file");
				ex.printStackTrace();
			}
		}

		return resultString;
	}

	/**
	 * Currently is reading just shorts
	 * 
	 * TODO - read different number types written in MHD
	 * 
	 * @param fileName
	 * @return
	 */
	public static float[] readFile(String fileName) {
		MHDReader.readMHD(fileName);
		ShortBuffer shorts = fastFileRead();
		return bufferAs1DMatrix(shorts);
	}

	/**
	 * Currently is reading just shorts
	 * 
	 * TODO - read different number types written in MHD
	 * 
	 * @param fileName
	 * @return
	 */
	public static float[][][] readFile3D(String fileName) {
		System.out.println(fileName.substring(fileName.length() -4).toLowerCase());
		if(fileName.substring(fileName.length() -4).toLowerCase().equals(".png")){
			 return convertImagingToRaw(fileName);
		}if(fileName.substring(fileName.length() -4).toLowerCase().equals(".raw")){
			return imagingToMatrix(fileName);
		}else{
			MHDReader.readMHD(fileName);
			ShortBuffer shorts = fastFileRead();
			return bufferAs3DMatrix(shorts);
		}
	}

	private static ShortBuffer fastFileRead() {
		System.out.println("Reading raw file " + MHDReader.rawFile + "...");
		File file = new File(MHDReader.rawFile);
		byte[] fileData = new byte[(int) file.length()];
		try {
			DataInputStream dis = new DataInputStream(new FileInputStream(file));
			dis.readFully(fileData);
			dis.close();
		} catch (IOException e) {
			System.out.println(e.toString());
			e.printStackTrace();
			System.exit(-1);
		}
		ByteBuffer buffer = ByteBuffer.wrap(fileData);
		buffer.order(ByteOrder.LITTLE_ENDIAN);

		ShortBuffer shorts = buffer.asShortBuffer();

		// Dereference variables
		file = null;
		fileData = null;
		buffer = null;

		return shorts;
	}

	private static float[] bufferAs1DMatrix(ShortBuffer buff) {
		float[] matrix = new float[buff.capacity()];
		for (int i = 0; i < buff.capacity(); i++) {
			matrix[i] = buff.get(i);
		}
		buff = null;
		return matrix;
	}

	private static float[][][] bufferAs3DMatrix(ShortBuffer buff) {
		float[][][] matrix = new float[MHDReader.Ny][MHDReader.Nx][MHDReader.Nz];
		for (int i = 0; i < MHDReader.Nz; i++) {
			for (int j = 0; j < MHDReader.Ny; j++) {
				for (int k = 0; k < MHDReader.Nx; k++) {
					matrix[j][k][i] = buff.get();
				}
			}
		}
		buff = null;
		
		float[][][] newMatrix = matrix;
		if(Configuration.__TAKE_SAMPLE){
			newMatrix = new float[Configuration.__SAMPLE_SIZE][Configuration.__SAMPLE_SIZE][Configuration.__SAMPLE_SIZE];
			for (int i = 0; i < Configuration.__SAMPLE_SIZE; i++) {
				for (int j = 0; j < Configuration.__SAMPLE_SIZE; j++) {
					for (int k = 0; k < Configuration.__SAMPLE_SIZE; k++) {
						newMatrix[i][j][k] = matrix[i][j][k];
					}
				}
			}
		}
		return newMatrix;
	}
	
	private static float[][][] convertImagingToRaw(String filename){
		String base = filename.substring(0, filename.length() - 7);
		String slice;
		BufferedImage image;
		short[][][] matrix = new short[96][48][48];
		short[] sBuffer;
		String rawFilename = base+"volumetric.raw";
		FileChannel out;
		
		try {
			
		out = new FileOutputStream(rawFilename).getChannel();

		slice = String.format("%03d", 0);
		
			for(int z = 0; z<96; z++){
				slice = String.format("%03d", (z-1)/2);
				try {
					image = ImageIO.read(new File(base+slice+".png"));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					break;
				}
	
				// Write to matrix
				Raster raster = image.getRaster();
				for(int x = 0; x< raster.getHeight(); x++){
					for(int y = 0; y< raster.getWidth(); y++){
						if(y < 2 ||x < 2 || z < 2 || Math.abs(z) > 93)
							matrix[z][x][y] = 0;
						else{
							int[] pixel = null;
							pixel = raster.getPixel(x, y, pixel);
							matrix[z][x][y] = (short)(pixel[0] >> 1);
						}
					}
					
					// Write binary data to .raw
					ByteBuffer myByteBuffer = ByteBuffer.allocate(raster.getWidth()*2);
					myByteBuffer.order(ByteOrder.LITTLE_ENDIAN);
					
					ShortBuffer myShortBuffer = myByteBuffer.asShortBuffer();
					myShortBuffer.put(matrix[z][x]);
					out.write(myByteBuffer);
					
				}
	
			}

			out.close();

		
		} catch (IOException e) {
			e.printStackTrace();
		}
		return imagingToMatrix(rawFilename);
	}
	
	
	
	
	
	private static float[][][] imagingToMatrix(String filename){
		FileChannel in;
		float[][][] mat = new float[96][48][48];
		try {
			in = new FileInputStream(filename).getChannel();


		short[] temp = new short[48];
		//branje
		for(int z = 0; z<96; z++){

			// Write to matrix
			
			for(int x = 0; x< 48; x++){
				ByteBuffer myByteBuffer = ByteBuffer.allocate(96);
				myByteBuffer.order(ByteOrder.LITTLE_ENDIAN);
				 
				in.read(myByteBuffer);
				myByteBuffer.flip();
				
				
				ShortBuffer myShortBuffer = myByteBuffer.asShortBuffer();
				myShortBuffer.get(temp);
				for(int i = 0; i < temp.length; i++){
					mat[z][x][i] = (float) ((int)temp[i]);
					
				
				}

			}
		
		}
		in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return mat;

	}
}
