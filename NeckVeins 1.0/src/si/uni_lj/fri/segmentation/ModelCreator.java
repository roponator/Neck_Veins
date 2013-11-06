package si.uni_lj.fri.segmentation;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opencl.CL;
import org.lwjgl.opencl.CL10;
import org.lwjgl.opencl.CLCommandQueue;
import org.lwjgl.opencl.CLContext;
import org.lwjgl.opencl.CLDevice;
import org.lwjgl.opencl.CLKernel;
import org.lwjgl.opencl.CLMem;
import org.lwjgl.opencl.CLPlatform;
import org.lwjgl.opencl.CLProgram;
import org.lwjgl.opencl.Util;

import si.uni_lj.fri.segmentation.utils.FileUtils;
import si.uni_lj.fri.segmentation.utils.Graytresh;
import si.uni_lj.fri.segmentation.utils.Utils;

/**
 * TODO RENAME, REFACTOR - move OpenCL utils methods to some Utils class
 * 
 */
public class ModelCreator {
	private static final int MATRIX_DATA = 0;
	private static final int DIMENSIONS_DATA = 1;

	private static final int FIND_MAX_LOCAL_SIZE = 512;
	private static final int HISTOGRAM_LOCAL_SIZE = 64;

	// OpenCL variables
	public static CLContext context;
	public static CLPlatform platform;
	public static List<CLDevice> devices;
	public static CLCommandQueue queue;
	public static CLProgram program;
	private static int matrixSize = 0;
	private static float max = 0;
	private static CLMem[] staticMemory;

	public ModelCreator() {
	}

	private static void createProgram(String kernelFile) {
		program = CL10.clCreateProgramWithSource(context, FileUtils.loadText(kernelFile), null);
		int error = CL10.clBuildProgram(program, devices.get(0), "", null);
		if (error == CL10.CL_BUILD_PROGRAM_FAILURE) {
			PointerBuffer bbuff_size = BufferUtils.createPointerBuffer(1);
			bbuff_size.put(0, 10000);
			ByteBuffer bbuff = BufferUtils.createByteBuffer(10000);
			CL10.clGetProgramBuildInfo(program, devices.get(0), CL10.CL_PROGRAM_BUILD_LOG, bbuff, bbuff_size);
			bbuff.rewind();
			byte[] bytearr = new byte[bbuff.remaining()];
			bbuff.get(bytearr);
			String s = new String(bytearr);
			System.out.println(s);
		}
		Util.checkCLError(error);
	}

	/**
	 * Called when loading from file
	 * 
	 * @param fileName
	 * @param sigma
	 * @param threshold
	 * @return
	 * @throws LWJGLException
	 */
	public static Object[] createModel(String fileName, double sigma, double threshold) throws LWJGLException {
		if (staticMemory != null) {
			cleanCLResources(staticMemory, new CLKernel[] {}, new CLProgram[] { program }, true);
		}

		initializeCL();
		createProgram("/opencl/segmentation.cls");

		long startTime = System.currentTimeMillis();
		float[] floatCTMatrix = FileUtils.readFile(fileName);
		matrixSize = floatCTMatrix.length;
		System.out.println("Reading matrix done! Time: " + (System.currentTimeMillis() - startTime) / 1000.0f + "s");
		System.out.println("Size of matrix - " + MHDReader.Nx + "x" + MHDReader.Ny + "x" + MHDReader.Nz);

		// GPU part
		startTime = System.currentTimeMillis();
		System.out.println("Marching cubes on GPU...");
		IntBuffer errorBuff = BufferUtils.createIntBuffer(1);

		int size = (int) (2 * Math.ceil(3 * sigma / MHDReader.dx) + 1);
		sigma = (float) (sigma / MHDReader.dx);
		int[] dimensions = new int[] { MHDReader.Nx, MHDReader.Ny, MHDReader.Nz, size };
		CLMem dimensionsMemory = locateMemory(dimensions, errorBuff, CL10.CL_MEM_READ_ONLY);
		CLMem matrixMemory = locateMemory(floatCTMatrix.length * 4, errorBuff, CL10.CL_MEM_READ_WRITE);
		staticMemory = new CLMem[] { matrixMemory, dimensionsMemory };
		Util.checkCLError(CL10.clFinish(queue));

		if (sigma > 0)
			execGauss3D(errorBuff, floatCTMatrix, sigma, size);
		else
			staticMemory[MATRIX_DATA] = locateMemory(floatCTMatrix, errorBuff, CL10.CL_MEM_READ_WRITE);

		max = execFindMax(staticMemory, errorBuff);
		if (threshold < 0)
			threshold = execOtsuThreshold(staticMemory, floatCTMatrix, max, errorBuff);

		Object[] output = execMarchingCubes(staticMemory, matrixSize, max, (float) threshold, errorBuff);

		System.out.println("GPU full time: " + (System.currentTimeMillis() - startTime) / 1000.0f + "s");

		return output;
	}

	/**
	 * Called when changing threshold in main window
	 * 
	 * @param threshold
	 * @return
	 * @throws LWJGLException
	 */
	public static Object[] changeModel(double threshold) throws LWJGLException {
		// GPU part
		long startTime = System.currentTimeMillis();
		System.out.println("Marching cubes on GPU...");
		IntBuffer errorBuff = BufferUtils.createIntBuffer(1);

		Object[] output = execMarchingCubes(staticMemory, matrixSize, max, (float) threshold, errorBuff);

		System.out.println("GPU full time: " + (System.currentTimeMillis() - startTime) / 1000.0f + "s");

		return output;
	}

	private static void execGauss3D(IntBuffer errorBuff, float[] floatCTMatrix, double sigma, int size) {
		// Init kernels
		CLKernel gaussX = CL10.clCreateKernel(program, "gaussX", null);
		CLKernel gaussY = CL10.clCreateKernel(program, "gaussY", null);
		CLKernel gaussZ = CL10.clCreateKernel(program, "gaussZ", null);

		// MATRIX
		CLMem kernel = locateMemory(Utils.getGauss1DKernel(size, sigma), errorBuff, CL10.CL_MEM_READ_ONLY);
		CLMem srcMatrixMemory = locateMemory(floatCTMatrix, errorBuff, CL10.CL_MEM_READ_WRITE);
		Util.checkCLError(CL10.clFinish(queue));

		// GAUSS 3D
		long gaussTime = measureTime(-1, "");
		gaussX.setArg(0, srcMatrixMemory);
		gaussX.setArg(1, staticMemory[DIMENSIONS_DATA]);
		gaussX.setArg(2, kernel);
		gaussX.setArg(3, staticMemory[MATRIX_DATA]);
		enqueueKernel(gaussX, new int[] { MHDReader.Nx, MHDReader.Ny, MHDReader.Nz });
		Util.checkCLError(CL10.clFinish(queue));
		gaussY.setArg(0, staticMemory[MATRIX_DATA]);
		gaussY.setArg(1, staticMemory[DIMENSIONS_DATA]);
		gaussY.setArg(2, kernel);
		gaussY.setArg(3, srcMatrixMemory);
		enqueueKernel(gaussY, new int[] { MHDReader.Nx, MHDReader.Ny, MHDReader.Nz });
		Util.checkCLError(CL10.clFinish(queue));
		gaussZ.setArg(0, srcMatrixMemory);
		gaussZ.setArg(1, staticMemory[DIMENSIONS_DATA]);
		gaussZ.setArg(2, kernel);
		gaussZ.setArg(3, staticMemory[MATRIX_DATA]);
		enqueueKernel(gaussZ, new int[] { MHDReader.Nx, MHDReader.Ny, MHDReader.Nz });
		Util.checkCLError(CL10.clFinish(queue));

		cleanCLResources(new CLMem[] { srcMatrixMemory, kernel }, new CLKernel[] { gaussX, gaussY, gaussZ },
				new CLProgram[] {}, false);
		Util.checkCLError(CL10.clFinish(queue));
		measureTime(gaussTime, "Gauss");

	}

	private static float execFindMax(CLMem[] staticMemory, IntBuffer errorBuff) {
		ByteBuffer deviceInfoBuff = BufferUtils.createByteBuffer(8);
		CL10.clGetDeviceInfo(devices.get(0), CL10.CL_DEVICE_MAX_WORK_GROUP_SIZE, deviceInfoBuff, null);
		int maxWorkGroupSize = (int) deviceInfoBuff.getLong(0);
		int nWorkItems = (int) (Math.ceil((MHDReader.Nx * MHDReader.Ny * MHDReader.Nz) / maxWorkGroupSize
				/ (double) FIND_MAX_LOCAL_SIZE) * FIND_MAX_LOCAL_SIZE);

		// localGroupSize - Se ne uporablja pri zagonu kernela
		// TODO - popravi localGroupSize za hardware neodvisnost - implementiraj
		// podobno kot pri execOtsuHistogram
		int localGroupSize = FIND_MAX_LOCAL_SIZE;
		int nWorkGroups = nWorkItems / localGroupSize + 1;

		CLKernel findMax = CL10.clCreateKernel(program, "findMax", null);
		CLMem maxMemory = locateMemory(nWorkGroups * 4, errorBuff, CL10.CL_MEM_WRITE_ONLY);

		findMax.setArg(0, staticMemory[MATRIX_DATA]);
		findMax.setArg(1, staticMemory[DIMENSIONS_DATA]);
		CL10.clSetKernelArg(findMax, 2, localGroupSize * 4);
		findMax.setArg(3, maxMemory);

		// Local groups size is not defined
		enqueueKernel(findMax, new int[] { nWorkItems });
		Util.checkCLError(CL10.clFinish(queue));

		FloatBuffer buffer = BufferUtils.createFloatBuffer(nWorkGroups);
		CL10.clEnqueueReadBuffer(queue, maxMemory, CL10.CL_TRUE, 0, buffer, null, null);
		float max = 0;
		for (int i = 0; i < buffer.capacity(); i++) {
			if (buffer.get(i) > max)
				max = buffer.get(i);
		}

		Util.checkCLError(CL10.clReleaseKernel(findMax));
		Util.checkCLError(CL10.clReleaseMemObject(maxMemory));
		Util.checkCLError(CL10.clFinish(queue));

		return max;
	}

	private static float execOtsuThreshold(CLMem[] staticMemory, float[] floatCTMatrix, float max, IntBuffer errorBuff) {
		long start = measureTime(-1, "");
		IntBuffer histogram = execOtsuHistogram(staticMemory, floatCTMatrix, max, errorBuff);
		double threshold = Graytresh.thresholdFromHistogram(histogram, MHDReader.Nx * MHDReader.Ny * MHDReader.Nz);
		measureTime(start, "Histogram");
		return (float) threshold;
	}

	private static IntBuffer execOtsuHistogram(CLMem[] staticMemory, float[] floatCTMatrix, float max,
			IntBuffer errorBuff) {
		int localGroupSize = HISTOGRAM_LOCAL_SIZE;

		CLKernel otsuHistogram = CL10.clCreateKernel(program, "otsuHistogram", null);
		CLMem maxValueMemory = locateMemory(new float[] { max }, errorBuff, CL10.CL_MEM_READ_ONLY);
		CLMem otsuHistogramMemory = locateMemory(new int[256], errorBuff, CL10.CL_MEM_READ_WRITE);
		Util.checkCLError(CL10.clFinish(queue));

		otsuHistogram.setArg(0, staticMemory[MATRIX_DATA]);
		otsuHistogram.setArg(1, staticMemory[DIMENSIONS_DATA]);
		otsuHistogram.setArg(2, maxValueMemory);
		otsuHistogram.setArg(3, otsuHistogramMemory);

		enqueueKernel(otsuHistogram, new int[] { localGroupSize * 10000 }, new int[] { localGroupSize });
		Util.checkCLError(CL10.clFinish(queue));

		IntBuffer histogram = BufferUtils.createIntBuffer(256);
		CL10.clEnqueueReadBuffer(queue, otsuHistogramMemory, CL10.CL_TRUE, 0, histogram, null, null);
		Util.checkCLError(CL10.clReleaseKernel(otsuHistogram));
		Util.checkCLError(CL10.clReleaseMemObject(maxValueMemory));
		Util.checkCLError(CL10.clReleaseMemObject(otsuHistogramMemory));
		Util.checkCLError(CL10.clFinish(queue));

		return histogram;
	}

	private static Object[] execMarchingCubes(CLMem[] staticMemory, int totalSize, float max, float threshold,
			IntBuffer errorBuff) {
		CLKernel marchingKernel = CL10.clCreateKernel(program, "marchingCubes", null);
		CLMem maxThreshMemory = locateMemory(new float[] { max, threshold }, errorBuff, CL10.CL_MEM_READ_WRITE);
		CLMem trianglesMemory = locateMemory((int) (totalSize / 1.25f), errorBuff, CL10.CL_MEM_WRITE_ONLY);
		CLMem normalsMemory = locateMemory((int) (totalSize / 1.25f), errorBuff, CL10.CL_MEM_WRITE_ONLY);
		CLMem nTrianglesMemory = locateMemory(new int[1], errorBuff, CL10.CL_MEM_READ_WRITE);
		Util.checkCLError(CL10.clFinish(queue));

		// Set the kernel parameters
		marchingKernel.setArg(0, staticMemory[MATRIX_DATA]);
		marchingKernel.setArg(1, staticMemory[DIMENSIONS_DATA]);
		marchingKernel.setArg(2, maxThreshMemory);
		marchingKernel.setArg(3, trianglesMemory);
		marchingKernel.setArg(4, normalsMemory);
		marchingKernel.setArg(5, nTrianglesMemory);
		enqueueKernel(marchingKernel, new int[] { MHDReader.Nx, MHDReader.Ny, MHDReader.Nz });
		Util.checkCLError(CL10.clFinish(queue));

		IntBuffer nTrianglesBuff = BufferUtils.createIntBuffer(1);
		CL10.clEnqueueReadBuffer(queue, nTrianglesMemory, CL10.CL_TRUE, 0, nTrianglesBuff, null, null);
		FloatBuffer trianglesBuff = BufferUtils.createFloatBuffer(nTrianglesBuff.get(0) * 9);
		FloatBuffer normalsBuff = BufferUtils.createFloatBuffer(nTrianglesBuff.get(0) * 9);
		CL10.clEnqueueReadBuffer(queue, trianglesMemory, CL10.CL_TRUE, 0, trianglesBuff, null, null);
		CL10.clEnqueueReadBuffer(queue, normalsMemory, CL10.CL_TRUE, 0, normalsBuff, null, null);

		CLMem[] memObj = { maxThreshMemory, trianglesMemory, normalsMemory, nTrianglesMemory };
		CLKernel[] kernelObj = { marchingKernel };
		CLProgram[] programObj = {};
		cleanCLResources(memObj, kernelObj, programObj, false);

		return new Object[] { nTrianglesBuff, trianglesBuff, normalsBuff, threshold };

	}

	public static void initializeCL() throws LWJGLException {
		IntBuffer errorBuf = BufferUtils.createIntBuffer(1);
		CL.create();
		platform = CLPlatform.getPlatforms().get(0);
		devices = platform.getDevices(CL10.CL_DEVICE_TYPE_GPU);
		context = CLContext.create(platform, devices, errorBuf);
		queue = CL10.clCreateCommandQueue(context, devices.get(0), CL10.CL_QUEUE_PROFILING_ENABLE, errorBuf);
		Util.checkCLError(errorBuf.get(0));
	}

	private static void cleanCLResources(CLMem[] memObj, CLKernel[] kernelObj, CLProgram[] programObj, boolean destroyCL) {
		for (int i = 0; i < kernelObj.length; i++) {
			CL10.clReleaseKernel(kernelObj[i]);
		}
		for (int i = 0; i < memObj.length; i++) {
			CL10.clReleaseMemObject(memObj[i]);
		}
		for (int i = 0; i < programObj.length; i++) {
			CL10.clReleaseProgram(programObj[i]);
		}
		if (destroyCL) {
			staticMemory = null;
			destroyCL();
		}
	}

	public static void destroyCL() {
		CL10.clReleaseCommandQueue(queue);
		CL10.clReleaseContext(context);
		CL.destroy();
	}

	private static CLMem locateMemory(float[] data, IntBuffer errorBuff, int flags) {
		FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
		buffer.put(data);
		buffer.rewind();
		CLMem memory = CL10.clCreateBuffer(context, flags, buffer.capacity() * 4, errorBuff);
		CL10.clEnqueueWriteBuffer(queue, memory, CL10.CL_TRUE, 0, buffer, null, null);
		Util.checkCLError(errorBuff.get(0));

		// Dereference buffer
		buffer = null;

		return memory;
	}

	private static CLMem locateMemory(int[] data, IntBuffer errorBuff, int flags) {
		IntBuffer buffer = BufferUtils.createIntBuffer(data.length);
		buffer.put(data);
		buffer.rewind();
		CLMem memory = CL10.clCreateBuffer(context, flags, buffer.capacity() * 4, errorBuff);
		CL10.clEnqueueWriteBuffer(queue, memory, CL10.CL_TRUE, 0, buffer, null, null);
		Util.checkCLError(errorBuff.get(0));

		// Dereference buffer
		buffer = null;

		return memory;
	}

	private static CLMem locateMemory(int size, IntBuffer errorBuff, int flags) {
		CLMem memory = CL10.clCreateBuffer(context, flags, size, errorBuff);
		Util.checkCLError(errorBuff.get(0));
		return memory;
	}

	private static void enqueueKernel(CLKernel kernel, int[] dimensions) {
		final int dim = dimensions.length;
		PointerBuffer globalWorkSize = BufferUtils.createPointerBuffer(dim);
		for (int i = 0; i < dim; i++) {
			globalWorkSize.put(i, dimensions[i]);
		}
		CL10.clEnqueueNDRangeKernel(queue, kernel, dim, null, globalWorkSize, null, null, null);
	}

	private static void enqueueKernel(CLKernel kernel, int[] globalWorkDimensions, int[] localWorkDimension) {
		final int dim = globalWorkDimensions.length;
		PointerBuffer globalWorkSize = BufferUtils.createPointerBuffer(dim);
		for (int i = 0; i < dim; i++) {
			globalWorkSize.put(i, globalWorkDimensions[i]);
		}
		PointerBuffer localWorkSize = BufferUtils.createPointerBuffer(localWorkDimension.length);
		for (int i = 0; i < localWorkDimension.length; i++) {
			localWorkSize.put(i, localWorkDimension[i]);
		}
		CL10.clEnqueueNDRangeKernel(queue, kernel, dim, null, globalWorkSize, localWorkSize, null, null);
	}

	private static long measureTime(long start, String measureName) {
		if (start < 0)
			return System.currentTimeMillis();
		long measuredTime = System.currentTimeMillis() - start;
		System.out.println(measureName + " time: " + measuredTime / 1000.0f + "s");
		return 0;
	}
}
