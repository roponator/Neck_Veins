package si.uni_lj.fri.Bloomenthal;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
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

import si.uni_lj.fri.MPUI_Utils.ImplicitInterface;
import si.uni_lj.fri.segmentation.utils.CLUtils;
import si.uni_lj.fri.segmentation.utils.FileUtils;
import si.uni_lj.fri.segmentation.utils.Graytresh;
import si.uni_lj.fri.segmentation.utils.Utils;

/**
 * TODO FIX execFindMax, REFACTOR
 * 
 * 
 */
public class BloomenthalGPU {
	// OpenCL variables
	private static CLContext context;
	private static CLPlatform platform;
	private static List<CLDevice> devices;
	private static CLCommandQueue queue;
	private static CLProgram program;
	private static float max = 0;
	private static CLMem[] staticMemory;
	private static ImplicitInterface globalImplicit;
	
	public BloomenthalGPU() {
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
	public static Object[] createModel(float cubeSize, ImplicitInterface gi) throws LWJGLException {
		globalImplicit = gi;
		clearOldProgram();
		initializeCL();
		program = CLUtils.createProgram("/opencl/bloomenthal.cls", context, devices);
		long t = Utils.startTime();
		Object[] output = execBloomenthal( cubeSize);
		Utils.endTime(t, "Max");
		System.out.println(max);
		globalImplicit.primitiveTreeConstruction();
		return output;
	}

	/**
	 * Called when changing threshold in main window
	 * 
	 * NOTE: because we are changing threshold of already loaded model, data is
	 * already on GPU
	 * 
	 * @param threshold
	 * @return
	 * @throws LWJGLException
	 */


	private static Object[] execBloomenthal(float cubeSize) {
		CLKernel marchingKernel = CL10.clCreateKernel(program, "bloomenthal", null);
		CLMem maxThreshMemory = CLUtils.locateMemory(new float[] {  cubeSize }, CL10.CL_MEM_READ_WRITE, queue,
				context);
		int size = (int) (1/cubeSize)+1;
		int outputSize = (int) size*size*size*4*9;
		
		
		CLMem trianglesMemory = CLUtils.locateMemory((int) (outputSize ), CL10.CL_MEM_WRITE_ONLY, context);
		CLMem normalsMemory = CLUtils.locateMemory((int) (outputSize ), CL10.CL_MEM_WRITE_ONLY, context);
		CLMem nTrianglesMemory = CLUtils.locateMemory(new int[1], CL10.CL_MEM_READ_WRITE, queue, context);
		CLMem lnBuff = CLUtils.locateMemory(globalImplicit.getLnBuff(), CL10.CL_MEM_READ_ONLY, queue, context);
		CLMem fnBuff = CLUtils.locateMemory(globalImplicit.getFnBuff(), CL10.CL_MEM_READ_ONLY, queue, context);
		CLMem otBuff = CLUtils.locateMemory(globalImplicit.getOtBuff(), CL10.CL_MEM_READ_ONLY, queue, context);
		CLMem crBuff = CLUtils.locateMemory(globalImplicit.getCrBuff(), CL10.CL_MEM_READ_ONLY, queue, context);
		
		
		Util.checkCLError(CL10.clFinish(queue));

		// Set the kernel parameters
		marchingKernel.setArg(0, maxThreshMemory);
		marchingKernel.setArg(1, trianglesMemory);
		marchingKernel.setArg(2, normalsMemory);
		marchingKernel.setArg(3, nTrianglesMemory);
		marchingKernel.setArg(4, lnBuff);
		marchingKernel.setArg(5, fnBuff);
		marchingKernel.setArg(6, otBuff);
		marchingKernel.setArg(7, crBuff);
		CLUtils.enqueueKernel(marchingKernel, new int[] { size, size, size}, queue);
		Util.checkCLError(CL10.clFinish(queue));

		IntBuffer nTrianglesBuff = BufferUtils.createIntBuffer(1);

		CL10.clEnqueueReadBuffer(queue, nTrianglesMemory, CL10.CL_TRUE, 0, nTrianglesBuff, null, null);
		FloatBuffer trianglesBuff = BufferUtils.createFloatBuffer(nTrianglesBuff.get(0) * 9);
		FloatBuffer normalsBuff = BufferUtils.createFloatBuffer(nTrianglesBuff.get(0) * 9);
		CL10.clEnqueueReadBuffer(queue, trianglesMemory, CL10.CL_TRUE, 0, trianglesBuff, null, null);
		CL10.clEnqueueReadBuffer(queue, normalsMemory, CL10.CL_TRUE, 0, normalsBuff, null, null);

		CLMem[] memObj = { maxThreshMemory, trianglesMemory, normalsMemory, nTrianglesMemory };
		CLKernel[] kernelObj = { marchingKernel };
		CLUtils.cleanCLResources(memObj, kernelObj, null);
		System.out.println(nTrianglesBuff.get(0)+"gg");
		return new Object[] { nTrianglesBuff, trianglesBuff, normalsBuff };

	}

	/**
	 * @throws LWJGLException
	 */
	public static void initializeCL() throws LWJGLException {
		IntBuffer errorBuf = BufferUtils.createIntBuffer(1);
		CL.create();
		platform = CLPlatform.getPlatforms().get(0);
		devices = platform.getDevices(CL10.CL_DEVICE_TYPE_GPU);
		context = CLContext.create(platform, devices, errorBuf);
		queue = CL10.clCreateCommandQueue(context, devices.get(0), CL10.CL_QUEUE_PROFILING_ENABLE, errorBuf);
		Util.checkCLError(errorBuf.get(0));
		System.out.println("Device: "+devices.get(0).getInfoString(CL10.CL_DEVICE_NAME)+devices.get(0).getInfoString(CL10.CL_DEVICE_MAX_WORK_ITEM_SIZES));
		
	}

	/**
	 * 
	 */
	private static void clearOldProgram() {
		if (staticMemory != null) {
			CLUtils.cleanCLResources(staticMemory, null, new CLProgram[] { program });
			CLUtils.destroyCL(queue, context);
		}
	}


}
