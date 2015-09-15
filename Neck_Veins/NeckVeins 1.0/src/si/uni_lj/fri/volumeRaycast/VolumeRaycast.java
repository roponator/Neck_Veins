package si.uni_lj.fri.volumeRaycast;

/*
 * Contains all code needed to load a mhd, init openCL and render the volume
 */

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.PointerBuffer;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opencl.*;
import org.lwjgl.opencl.Util;
import org.lwjgl.opencl.api.Filter;
import org.lwjgl.opengl.*;
import org.lwjgl.util.Color;
import org.lwjgl.util.ReadableColor;
import org.lwjgl.util.vector.Vector3f;

import de.lessvoid.nifty.render.batch.spi.GL;
import si.uni_lj.fri.segmentation.MHDReader;
import si.uni_lj.fri.veins3D.gui.NiftyScreenController;
import si.uni_lj.fri.veins3D.gui.NiftySettingsSideMenu;
import si.uni_lj.fri.veins3D.gui.render.Camera;
import si.uni_lj.fri.veins3D.gui.render.VeinsRenderer;
import si.uni_lj.fri.veins3D.main.VeinsWindow;
import si.uni_lj.fri.veins3D.math.Quaternion;
import sun.nio.ch.DirectBuffer;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import static java.lang.Math.*;
import static org.lwjgl.opencl.CL10.*;
import static org.lwjgl.opencl.CL10GL.*;
import static org.lwjgl.opencl.KHRGLEvent.*;
import static org.lwjgl.opengl.AMDDebugOutput.*;
import static org.lwjgl.opengl.ARBCLEvent.*;
import static org.lwjgl.opengl.ARBDebugOutput.*;
import static org.lwjgl.opengl.ARBSync.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL21.*;

public class VolumeRaycast
{
	public enum RenderMethod
	{
		ISO,
		ALPHA,
		MAX_PROJECTION
	}
	
	static RenderMethod renderMethod = RenderMethod.ISO;
	
	private Set<String> params;

	public static boolean m_enableSSAO = false;
	public static float m_ssaoStrength = 1.0f;
	
	public static boolean m_enableDOF = false;
	public static float m_dofFocus = 20.0f;
	public static float m_dofStrength = 0.015f;
	
	public static float m_keyboardMoveX = 0.0f;
	public static float m_keyboardMoveY = 0.0f;
	public static float m_keyboardMoveZ = 0.0f;
	public static Quaternion m_keyboardRotation = new Quaternion();
	
	private CLContext clContext;
	private CLCommandQueue queue;
	static private CLKernel kernelISO = null;
	static private CLKernel kernelAlpha = null;
	static private CLKernel kernelAlphaWithMip = null;
	static private CLKernel currentlyActiveKernel = null;
	private CLKernel kernelFirstPass;
	private CLKernel kernelSecondPass;
	private CLProgram program;

	private CLKernel[] kernelsConvolution = new CLKernel[3];
	private static final int KERNEL_CONVOLUTION = 0;
	private static final int KERNEL_CONVOLUTION_X = 1;
	private static final int KERNEL_CONVOLUTION_Y = 2;
	private CLMem convolutionMask;
	private int convolutionMaskSize;

	private CLKernel[] kernelsEffects = new CLKernel[3];
	private static final int KERNEL_COPY = 0;
	private static final int KERNEL_DOF = 1;
	private static final int KERNEL_SSAO = 2;

	private static final int BUFFER_1 = 0;
	private static final int BUFFER_2 = 1;
	private static final int BUFFER_3 = 2;
	private static final int BUFFER_DEPTH_1 = 3;
	private static final int BUFFER_DEPTH_2 = 4;
	private int glBuffersCount = 5;
	private CLMem[] glBuffers = new CLMem[glBuffersCount];
	private IntBuffer glIDs;
	private int numTransferFunctionSamples = 0; // read from gradient file
	
	private CLMem clTransferFunction;

	private CLMem matrix;
	private CLMem matrix2; // for gauss
	private CLMem octree;
	private int octreeLevels;

	private boolean useTextures;

	// Texture rendering
	private int dlist = -1;
	private int vsh;
	private int fsh;
	private int glProgram;

	private final PointerBuffer kernel2DGlobalWorkSize;
	private final PointerBuffer kernel3DGlobalWorkSize;

	private int width = 0;
	private int height = 0;

	private int mouseX;
	private int mouseY;

	// timing variables
	private long tPrev = 0;
	private long tNow = 0;

	// private Vector3f camPos = new Vector3f(68.02019f, 124.51997f, -22.897635f); // mrt16_angio
	// private Vector3f camDir = new Vector3f(0.3290509f, -0.10898647f, 0.9380018f);
	// private Vector3f camRight = new Vector3f(-0.94362277f, 0.0f, 0.33102274f);
	// private Vector3f camUp = new Vector3f(0.036077f, 0.99404323f, 0.102842115f);
	// private float camAngle = (float) Math.PI;
	// private float camZAngle = 0;
	// private float camSpeed = 100.f;
	private float fov = (float) Math.tan(Math.PI / 3);
	private float asr = 0.0f;
	// private float threshold = 0.03f;
	public static float threshold = 0.025f; // mrt16_angio
	private int lin = 1;

	private boolean doublePrecision = true;
	private boolean buffersInitialized;
	private boolean rebuild;

	private boolean isRunning = true;

	// EVENT SYNCING

	private final PointerBuffer syncBuffer = BufferUtils.createPointerBuffer(1);

	private boolean syncGLtoCL; // true if we can make GL wait on events generated from CL queues.
	private CLEvent clEvent;
	private GLSync clSync;

	private boolean syncCLtoGL; // true if we can make CL wait on sync objects generated from GL.
	private GLSync glSync;
	private CLEvent glEvent;

	static VolumeRaycast me;
	
	public VolumeRaycast(int width, int height)
	{
		me = this;
		
		this.width = width;
		this.height = height;
		asr = width / (float) height;

		params = new HashSet<String>();
		kernel2DGlobalWorkSize = BufferUtils.createPointerBuffer(2);
		kernel3DGlobalWorkSize = BufferUtils.createPointerBuffer(3);
	}

	/*
	 * public static void main(String args[]) { /*VolumeRaycast demo = new VolumeRaycast(args); demo.init(); demo.run(); }
	 */

	public void MainInit(String filepath,Camera camera)
	{
		try
		{
			CL.create();

			NiftyScreenController.UpdateLoadingBarDialog("Loading model...", 10.0f);
			VeinsWindow.veinsWindow.RenderSingleFrameWithoutModel();

		}
		catch (LWJGLException e)
		{
			throw new RuntimeException(e);
		}

		try
		{
			initCL(Display.getDrawable());
			NiftyScreenController.UpdateLoadingBarDialog("Loading model...", 30.0f);
			VeinsWindow.veinsWindow.RenderSingleFrameWithoutModel();
		}
		catch (Exception e)
		{
			System.out.println("VolumeRaycast: " + e.toString());
			throw new RuntimeException(e);
		}

		glDisable(GL_DEPTH_TEST);
		// glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

		initView(width, height);

		initGLObjects();
		glFinish();

		NiftyScreenController.UpdateLoadingBarDialog("Loading model...", 80.0f);
		VeinsWindow.veinsWindow.RenderSingleFrameWithoutModel();

		long t;

		// load data into memory
		System.out.print("Reading file...");
		t = System.currentTimeMillis();
		String workingDir = System.getProperty("user.dir");
	
		//float[] data = readFile("C://Users//ropo//Desktop//Zile//Pat2_3D-DSA.mhd"); // TODO
		float[] data = readFile(filepath); // TODO

		matrix = locateMemory(data, CL_MEM_READ_WRITE, queue, clContext);
		t = System.currentTimeMillis() - t;
		System.out.println(t + "ms");

		System.out.print("Execing gauss...");
		t = System.currentTimeMillis();
		matrix2 = locateMemory(data, CL_MEM_READ_WRITE, queue, clContext);
		execGauss3D(VeinsWindow.settings.VOLUME_RENDER_GAUSS_SIGMA);
		t = System.currentTimeMillis() - t;
		System.out.println(t + "ms");

		System.out.print("Constructing octree...");
		t = System.currentTimeMillis();
		float[] octreefloats = constructOctree(data);
		octree = locateMemory(octreefloats, CL_MEM_READ_ONLY, queue, clContext);
		t = System.currentTimeMillis() - t;
		System.out.println(t + "ms");

		System.out.print("Reading transfer function...");
		LoadGradFromFileAndCreateCLMemory(VeinsWindow.defaultGradientFile);
		//float[] gradientFromFile = 	loadGradient();
		//t = System.currentTimeMillis();
		//readFloatsFromFile("gradient", transferFunction);
		//clTransferFunction = locateMemory(gradientFromFile, CL_MEM_READ_ONLY, queue, clContext);*/
		t = System.currentTimeMillis() - t;
		System.out.println(t + "ms");

		System.out.print("Computing convolution mask...");
		t = System.currentTimeMillis();
		convolutionMaskSize = 10;
		float[] mask = getGauss1DKernel(convolutionMaskSize, convolutionMaskSize * 0.5);
		convolutionMask = locateMemory(mask, CL_MEM_READ_ONLY, queue, clContext);
		t = System.currentTimeMillis() - t;
		System.out.println(t + "ms");

		System.out.println();

		setKernelConstants();
		
		setInitialRenderMethod();
		
		VeinsWindow.renderer.resetCameraPositionAndOrientation();

	}
	
	public void LoadGradFromFileAndCreateCLMemory(String gradientFile)
	{
		//System.out.println("g1");
		if(clTransferFunction != null)
		{
			freeBuffer(clTransferFunction);
		}
		//System.out.println("g2");
		float[] gradientFromFile = 	loadGradient(gradientFile);
		//System.out.println("g3");
		//readFloatsFromFile("gradient", transferFunction);
		clTransferFunction = locateMemory(gradientFromFile, CL_MEM_READ_ONLY, queue, clContext);
		//System.out.println("g4");
		
		setKernelConstants();
	}
	
	// returns null if failed to load
	float[] loadGradient(String gradientFile)
	{
		float[] gradRawArray=null;
	
		ClassLoader classLoader = getClass().getClassLoader();
		//URI uri;
		try
		{
			//Scanner s1 = new Scanner(new FileInputStream(f));
			//Scanner s2 = new Scanner(new FileInputStream(f2));
			//InputStream ss= classLoader.getResourceAsStream(gradientFile);
			//URL url = classLoader.getResource(gradientFile);
			//uri = url.toURI();
			//uri = f.toURI();
		
			String d=System.getProperty("user.dir");

			//File f1=new File(gradientFile);
			//boolean b4=new File("gradient/defaultGrad.grad").canRead();
			//boolean b5=new File("gradient//defaultGrad.grad").canRead();
			//boolean b6=new File("gradient\\defaultGrad.grad").canRead();
			
			Scanner s = new Scanner(new File(gradientFile));
			ArrayList<Float> gradient = new ArrayList<Float>();
			while(s.hasNextLine())
			{
				String line=s.nextLine();
				String[] splitted = line.split("\\s+");
			
				for(int i=0;i<4;++i)
				{
					if(splitted[i].length()>0)
					{
						gradient.add(Float.parseFloat(splitted[i]));
					}
				}
			}
					
			gradRawArray=new float[gradient.size()];
			for(int i=0;i<gradient.size();++i)
			{
				gradRawArray[i]=gradient.get(i);
			}
			
			numTransferFunctionSamples = gradRawArray.length / 4; // divide by 4 because of float -> float4
			
			s.close();		
		}
		catch (Exception e)
		{
			System.out.println("loadGradient: "+e.toString());
			e.printStackTrace();
		}
		finally
		{
			
		}
	
		return gradRawArray;
	}
	
	void setInitialRenderMethod()
	{
		if(VeinsWindow.settings.volumeRenderMethod ==0)
		{
			VolumeRaycast.SetRenderMethod(VolumeRaycast.RenderMethod.ISO);
		}
		else if(VeinsWindow.settings.volumeRenderMethod==1)
		{
			VolumeRaycast.SetRenderMethod(VolumeRaycast.RenderMethod.ALPHA);
		}
		else if(VeinsWindow.settings.volumeRenderMethod==2)
		{
			VolumeRaycast.SetRenderMethod(VolumeRaycast.RenderMethod.MAX_PROJECTION);
		}
		else
		{
			System.out.println("Error: invalid volume render method: "+VeinsWindow.settings.volumeRenderMethod);
		}
	}

	public static void SetRenderMethod(RenderMethod method)
	{
		if(method == RenderMethod.ISO)
			currentlyActiveKernel = kernelISO;
		else if(method == RenderMethod.ALPHA)
			currentlyActiveKernel = kernelAlpha;
		else if(method == RenderMethod.MAX_PROJECTION)
			currentlyActiveKernel = kernelAlphaWithMip;
		else
			System.out.println("VolumeRaycast: invalid render method: "+method.toString());
		
		if(currentlyActiveKernel != null)
			me.setKernelConstants();
	}
	
	public void SetNewResolution(int width, int height)
	{
		this.width = width;
		this.height = height;

		try
		{
			initCL(Display.getDrawable());
		}
		catch (Exception e)
		{
			System.out.println("Error: VolumeRaycast: SetNewResolution: " + e.toString());
			e.printStackTrace();

		}
	}

	public void ReleaseResources()
	{
		releaseTexturesAndBuffers();

		clReleaseProgram(program);
		clReleaseCommandQueue(queue);
		clReleaseContext(clContext);
	}

	private static void initView(int width, int height)
	{
		glViewport(0, 0, width, height);

		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();

		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		glOrtho(0.0, width, 0.0, height, 0.0, 1.0);
	}

	private void initCL(Drawable drawable) throws Exception
	{
		// Find a platform
		List<CLPlatform> platforms = CLPlatform.getPlatforms();
		if (platforms == null)
			throw new RuntimeException("No OpenCL platforms found.");

		final CLPlatform platform = platforms.get(0); // just grab the first one

		// Find devices with GL sharing support
		final Filter<CLDevice> glSharingFilter = new Filter<CLDevice>()
		{
			public boolean accept(final CLDevice device)
			{
				final CLDeviceCapabilities caps = CLCapabilities.getDeviceCapabilities(device);
				return caps.CL_KHR_gl_sharing;
			}
		};
		int device_type = params.contains("forceCPU") ? CL_DEVICE_TYPE_CPU : CL_DEVICE_TYPE_GPU;
		List<CLDevice> devices = platform.getDevices(device_type, glSharingFilter);
		if (devices == null)
		{
			device_type = CL_DEVICE_TYPE_CPU;
			devices = platform.getDevices(device_type, glSharingFilter);
			if (devices == null)
				throw new RuntimeException("No OpenCL devices found with KHR_gl_sharing support.");
		}

		// Create the context
		clContext = CLContext.create(platform, devices, new CLContextCallback()
		{
			protected void handleMessage(final String errinfo, final ByteBuffer private_info)
			{
				System.out.println("[CONTEXT MESSAGE] " + errinfo);
			}
		}, drawable, null);

		// create command queues for every GPU, setup colormap and init kernels
		// queues = new CLCommandQueue[slices];
		// kernels = new CLKernel[slices];

		queue = clCreateCommandQueue(clContext, devices.get(0), CL_QUEUE_PROFILING_ENABLE, null);
		queue.checkValid();

		// check if we have 64bit FP support on all devices
		// if yes we can use only one program for all devices + one kernel per device.
		// if not we will have to create (at least)one program for 32 and one for 64bit devices.
		// since there are different vendor extensions for double FP we use one program per device.
		// (OpenCL spec is not very clear about this usecases)
		boolean all64bit = true;
		for (CLDevice device : devices)
		{
			if (!isDoubleFPAvailable(device))
			{
				all64bit = false;
				break;
			}
		}

		// load program(s)
		// programs = new CLProgram[all64bit ? 1 : slices];

		final ContextCapabilities caps = GLContext.getCapabilities();

		if (!caps.OpenGL20)
			throw new RuntimeException("OpenGL 2.0 is required to run this demo.");
		else if (device_type == CL_DEVICE_TYPE_CPU && !caps.OpenGL21)
			throw new RuntimeException("OpenGL 2.1 is required to run this demo.");

		if (params.contains("debugGL"))
		{
			if (caps.GL_ARB_debug_output)
				glDebugMessageCallbackARB(new ARBDebugOutputCallback());
			else if (caps.GL_AMD_debug_output)
				glDebugMessageCallbackAMD(new AMDDebugOutputCallback());
		}

		if (device_type == CL_DEVICE_TYPE_GPU)
			System.out.println("OpenCL Device Type: GPU (Use -forceCPU to use CPU)");
		else
			System.out.println("OpenCL Device Type: CPU");
		for (int i = 0; i < devices.size(); i++)
			System.out.println("OpenCL Device #" + (i + 1) + " supports KHR_gl_event = " + CLCapabilities.getDeviceCapabilities(devices.get(i)).CL_KHR_gl_event);

		System.out.println("\nOpenGL caps.GL_ARB_sync = " + caps.GL_ARB_sync);
		System.out.println("OpenGL caps.GL_ARB_cl_event = " + caps.GL_ARB_cl_event);

		// Use PBO if we're on a CPU implementation
		useTextures = device_type == CL_DEVICE_TYPE_GPU && (!caps.OpenGL21 || !params.contains("forcePBO"));
		if (useTextures)
		{
			System.out.println("\nCL/GL Sharing method: TEXTURES (use -forcePBO to use PBO + DrawPixels)");
			System.out.println("Rendering method: Shader on a fullscreen quad");
		}
		else
		{
			System.out.println("\nCL/GL Sharing method: PIXEL BUFFER OBJECTS");
			System.out.println("Rendering method: DrawPixels");
		}

		buildPrograms();

		// Detect GLtoCL synchronization method
		syncGLtoCL = caps.GL_ARB_cl_event; // GL3.2 or ARB_sync implied
		if (syncGLtoCL)
		{
			System.out.println("\nGL to CL sync: Using OpenCL events");
		}
		else
			System.out.println("\nGL to CL sync: Using clFinish");

		// Detect CLtoGL synchronization method
		syncCLtoGL = caps.OpenGL32 || caps.GL_ARB_sync;
		if (syncCLtoGL)
		{
			for (CLDevice device : devices)
			{
				if (!CLCapabilities.getDeviceCapabilities(device).CL_KHR_gl_event)
				{
					syncCLtoGL = false;
					break;
				}
			}
		}
		if (syncCLtoGL)
		{
			System.out.println("CL to GL sync: Using OpenGL sync objects");
		}
		else
			System.out.println("CL to GL sync: Using glFinish");

		if (useTextures)
		{
			createQuad();

			vsh = glCreateShader(GL_VERTEX_SHADER);
			glShaderSource(vsh, "varying vec2 texCoord;\n" + "\n" + "void main(void) {\n" + "\tgl_Position = ftransform();\n" + "\ttexCoord = gl_MultiTexCoord0.xy;\n" + "}");
			glCompileShader(vsh);

			fsh = glCreateShader(GL_FRAGMENT_SHADER);
			glShaderSource(fsh, "uniform sampler2D mandelbrot;\n" + "\n" + "varying vec2 texCoord;\n" + "\n" + "void main(void) {\n" + "\tgl_FragColor = texture2D(mandelbrot, texCoord);" + "}");
			glCompileShader(fsh);

			// must preserve old state (nifty has problems) (?? NOT 100%) TODO REMOVE THIS PUSH??
			// glPushAttrib(GL_ALL_ATTRIB_BITS);
			glProgram = glCreateProgram();
			glAttachShader(glProgram, vsh);
			glAttachShader(glProgram, fsh);
			glLinkProgram(glProgram);

			glDetachShader(glProgram, vsh);
			glDetachShader(glProgram, fsh);
			// glPopAttrib();
		}

		System.out.println("");
	}

	void createQuad()
	{
		releaseGenLists();

		dlist = glGenLists(1);

		glNewList(dlist, GL_COMPILE);
		glBegin(GL_QUADS);
		{
			glTexCoord2f(0.0f, 0.0f);
			glVertex2f(0, 0);

			glTexCoord2f(0.0f, 1.0f);
			glVertex2i(0, height);

			glTexCoord2f(1.0f, 1.0f);
			glVertex2f(width, height);

			glTexCoord2f(1.0f, 0.0f);
			glVertex2f(width, 0);
		}
		glEnd();
		glEndList();
	}

	// rendering cycle

	public void MainRender(Camera camera,Quaternion mouseRotation, float stereoOffset)
	{
		
		initView(width, height);
		// handleIO();
		display(camera,mouseRotation,stereoOffset);

		glMatrixMode(GL_MODELVIEW); // no idea why but it has to be here, otherwise a bunch of state error (underflow)

	}

	public void cleanCL()
	{
		clReleaseContext(clContext);

		if (useTextures)
		{
			glDeleteProgram(glProgram);
			glDeleteShader(fsh);
			glDeleteShader(vsh);

			glDeleteLists(dlist, 1);
		}

		CL.destroy();
		Display.destroy();
	}

	public void display(Camera camera,Quaternion mouseRotation, float stereoOffset)
	{
		// TODO: Need to clean-up events, test when ARB_cl_events & KHR_gl_event are implemented.

		// make sure GL does not use our objects before we start computing
		if (syncCLtoGL && glEvent != null)
		{
			clEnqueueWaitForEvents(queue, glEvent);
		}
		else
			glFinish();

		if (!buffersInitialized)
		{
			initGLObjects();
		}

		if (rebuild)
		{
			buildPrograms();			
		}
		
		setKernelConstants();

		// set program and stuff
		glUseProgram(glProgram);
		glUniform1i(glGetUniformLocation(glProgram, "mandelbrot"), 0);

		compute(doublePrecision, camera, mouseRotation, stereoOffset);	
		
		render();	
	}

	private void initGLObjects()
	{

		if (glBuffers[0] == null)
		{
			glIDs = BufferUtils.createIntBuffer(glBuffersCount);
		}
		else
		{
			releaseTexturesAndBuffers();
		}

		if (useTextures)
		{
			glGenTextures(glIDs);

			for (int i = 0; i < glBuffersCount; i++)
			{
				glBindTexture(GL_TEXTURE_2D, glIDs.get(i));
				glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, (ByteBuffer) null);
				glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
				glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
				IntBuffer errorBuf = BufferUtils.createIntBuffer(1);
				glBuffers[i] = clCreateFromGLTexture2D(clContext, CL_MEM_READ_WRITE, GL_TEXTURE_2D, 0, glIDs.get(i), errorBuf);
				int glerr = glGetError();
				if (glBuffers[i].isValid() == false)
				{
					int errInt = errorBuf.get(0); // This increments the buffer position!
					System.out.println("Error: VolumeRaycast: initGLObjects: " + errInt + ": " + VeinsRenderer.GetOpenCLErrorString(errInt));
					Util.checkCLError(errorBuf.get(0));
				}

			}

			glBindTexture(GL_TEXTURE_2D, 0);
		}
		else
		{
			glGenBuffers(glIDs);

			for (int i = 0; i < glBuffersCount; i++)
			{
				glBindBuffer(GL_PIXEL_UNPACK_BUFFER, glIDs.get(i));
				glBufferData(GL_PIXEL_UNPACK_BUFFER, width * height * 4, GL_STREAM_DRAW);

				glBuffers[i] = clCreateFromGLBuffer(clContext, CL_MEM_READ_WRITE, glIDs.get(i), null);
			}

			glBindBuffer(GL_PIXEL_UNPACK_BUFFER, 0);
		}

		buffersInitialized = true;
	}

	void releaseGenLists()
	{
		if (dlist != -1) // free old
			glDeleteLists(dlist, 1);
	}

	void releaseTexturesAndBuffers()
	{
		if (useTextures)
			glBindTexture(GL_TEXTURE_2D, 0);
		else
			glBindBuffer(GL_PIXEL_UNPACK_BUFFER, 0);

		for (int i = 0; i < glBuffersCount; i++)
		{
			clReleaseMemObject(glBuffers[i]);
		}

		if (useTextures)
			glDeleteTextures(glIDs);
		else
			glDeleteBuffers(glIDs);
	}

	private void buildPrograms()
	{
		/*
		 * workaround: The driver keeps using the old binaries for some reason. to solve this we simple create a new program and release the old. however rebuilding programs should be possible -> remove when drivers are fixed. (again: the spec is not very clear about this kind of usages)
		 */
		if (program != null)
		{
			clReleaseProgram(program);
		}

		try
		{
			createPrograms();
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}

		final CLDevice device = queue.getCLDevice();

		final StringBuilder options = new StringBuilder(useTextures ? "-D USE_TEXTURE" : "");
		final CLDeviceCapabilities caps = CLCapabilities.getDeviceCapabilities(device);
		if (doublePrecision && isDoubleFPAvailable(device))
		{
			// cl_khr_fp64
			options.append(" -D DOUBLE_FP");

			// amd's verson of double precision floating point math
			if (!caps.CL_KHR_fp64 && caps.CL_AMD_fp64)
				options.append(" -D AMD_FP");
		}

		// options.append(" -w ");

		System.out.println("\nOpenCL COMPILER OPTIONS: " + options);

		try
		{
			clBuildProgram(program, device, options, null);
		}
		finally
		{
			String buildLog = program.getBuildInfoString(device, CL_PROGRAM_BUILD_LOG);
			if (buildLog != null && !buildLog.equals("\n"))
			{
				System.err.println("BUILD LOG: " + buildLog);
				// System.exit(1); // we can have warnings but not errors
			}
		}

		rebuild = false;

		kernelISO = clCreateKernel(program, "raycast", null);
		kernelAlpha = clCreateKernel(program, "raycastAlpha_NO_MIP", null);
		kernelAlphaWithMip = clCreateKernel(program, "raycastAlpha_WITH_MIP", null);
		
		currentlyActiveKernel = kernelISO;
		
		kernelFirstPass = clCreateKernel(program, "raycastPass1", null);
		kernelSecondPass = clCreateKernel(program, "raycastPass2", null);

		kernelsConvolution[KERNEL_CONVOLUTION] = clCreateKernel(program, "imageConvolution", null);
		kernelsConvolution[KERNEL_CONVOLUTION_X] = clCreateKernel(program, "imageConvolutionX", null);
		kernelsConvolution[KERNEL_CONVOLUTION_Y] = clCreateKernel(program, "imageConvolutionY", null);

		kernelsEffects[KERNEL_COPY] = clCreateKernel(program, "imageCopy", null);
		kernelsEffects[KERNEL_DOF] = clCreateKernel(program, "depthOfField", null);
		kernelsEffects[KERNEL_SSAO] = clCreateKernel(program, "ssao", null);
	}

	// OpenCL

	private void setKernelConstants()
	{
		currentlyActiveKernel.setArg(0, glBuffers[BUFFER_1]).setArg(1, glBuffers[BUFFER_2]).setArg(2, glBuffers[BUFFER_DEPTH_1]).setArg(3, glBuffers[BUFFER_DEPTH_2]).setArg(18, matrix).setArg(25, octree).setArg(27, clTransferFunction);
		kernelFirstPass.setArg(0, glBuffers[BUFFER_2]).setArg(1, glBuffers[BUFFER_1]).setArg(2, glBuffers[BUFFER_DEPTH_2]).setArg(3, glBuffers[BUFFER_DEPTH_1]).setArg(18, matrix).setArg(25, octree).setArg(27, clTransferFunction);
		kernelSecondPass.setArg(0, glBuffers[BUFFER_1]).setArg(1, glBuffers[BUFFER_2]).setArg(2, glBuffers[BUFFER_DEPTH_1]).setArg(3, glBuffers[BUFFER_DEPTH_2]).setArg(18, matrix).setArg(25, octree).setArg(27, clTransferFunction);
	}

	private void compute(final boolean is64bit, Camera camera,Quaternion mouseRotation, float stereoOffset)
	{
		kernel2DGlobalWorkSize.put(0, width).put(1, height);

		float sint = (float) Math.sin(System.currentTimeMillis() / 1000.0);
		
		// acquire GL objects, and enqueue a kernel with a probe from the list
		for (int i = 0; i < glBuffersCount; i++)
		{
			clEnqueueAcquireGLObjects(queue, glBuffers[i], null, null);
		}
		clFinish(queue);
	
		// create camera matrix, must also rotate by 180 degrees on Y axis for proper initial orientation
		Quaternion rotY = Quaternion.quaternionFromAngleAndRotationAxis(Math.PI, new double[]
		{ 0, 1, 0 });
	
		double dx = MHDReader.dx;
		double dy = MHDReader.dy;
		double dz = MHDReader.dz;
		
		float xOff = 100;
		float yOff = 100;
		float zOff = 100;
		
		float camPosX = -camera.cameraX + xOff;
		float camPosY = -camera.cameraY + yOff;
		float camPosZ = -camera.cameraZ + zOff-200;
		
		Quaternion rotQuatToUse = null;
		
		// Different computatation if in camera move mode or in model move mode
		if(VeinsWindow.settings.useModelMoveMode==false)
		{
			// CAMERA WORKS, BUT NOT MOUSE SPHERE MODEL ROTATION
			//rotQuatToUse = Quaternion.quaternionReciprocal(camera.cameraOrientation);
			float moveFactor = 5.0f;

			//Quaternion rotY2 = Quaternion.quaternionFromAngleAndRotationAxis(Math.PI, new double[]{0,1,0});
			rotQuatToUse =  Quaternion.quaternionReciprocal(camera.cameraOrientation);
			//FloatBuffer rotMatrix =  Quaternion.quaternionMultiplication(worldOrientation, rotY).getRotationMatrix(true);
			
			
			//double[] rotatedMovement = rotQuatToUse.rotateVector3d(new double[]{m_keyboardMoveX*moveFactor,m_keyboardMoveZ*moveFactor,m_keyboardMoveY*moveFactor});
			
			double[] rotatedStereoVec = camera.cameraOrientation.rotateVector3d(new double[]{stereoOffset,0,0});
			camPosX = camera.cameraX+(float)rotatedStereoVec[0];
			camPosY = camera.cameraY+(float)rotatedStereoVec[1];
			camPosZ = camera.cameraZ+(float)rotatedStereoVec[2];			
			
			
			// DOESNT WORK
			/*Quaternion mouseAndCameraRot = Quaternion.quaternionMultiplication(camera.cameraOrientation,Quaternion.quaternionReciprocal(mouseRotation));
			
			rotQuatToUse = mouseAndCameraRot;
			float moveFactor = 5.0f;
			
			Quaternion posChangeQuat = Quaternion.quaternionReciprocal(mouseRotation);
			double[] rotCameraPos = posChangeQuat.rotateVector3d(new double[]{0,0,-200.0f});
			double[] rotatedMovement = posChangeQuat.rotateVector3d(new double[]{m_keyboardMoveX*moveFactor,m_keyboardMoveZ*moveFactor,-m_keyboardMoveY*moveFactor});
			camPosX = (float)rotCameraPos[0] + (float)rotatedMovement[0] + xOff;
			camPosY = (float)rotCameraPos[1] + (float)rotatedMovement[1] + yOff;
			camPosZ = (float)rotCameraPos[2] + (float)rotatedMovement[2] + zOff;
			
			Quaternion yRot = Quaternion.quaternionFromAngleAndRotationAxis(Math.PI, new double[]{0,1,0});
			Quaternion invQuatToUse = Quaternion.quaternionReciprocal(rotQuatToUse);
			rotQuatToUse = Quaternion.quaternionMultiplication(yRot,rotQuatToUse);*/
		}
		else
		{
			Quaternion mouseAndKeyboardRot = Quaternion.quaternionMultiplication(m_keyboardRotation,Quaternion.quaternionReciprocal(mouseRotation));
			
			rotQuatToUse = mouseAndKeyboardRot;
			float moveFactor = 5.0f;
			
			double[] rotCameraPos = rotQuatToUse.rotateVector3d(new double[]{stereoOffset,0,-200.0f});
			double[] rotatedMovement = rotQuatToUse.rotateVector3d(new double[]{m_keyboardMoveX*moveFactor,m_keyboardMoveZ*moveFactor,-m_keyboardMoveY*moveFactor});
			camPosX = (float)rotCameraPos[0] + (float)rotatedMovement[0] + xOff;
			camPosY = (float)rotCameraPos[1] + (float)rotatedMovement[1] + yOff;
			camPosZ = (float)rotCameraPos[2] + (float)rotatedMovement[2] + zOff;
			
			Quaternion yRot = Quaternion.quaternionFromAngleAndRotationAxis(Math.PI, new double[]{0,1,0});
			Quaternion invQuatToUse = Quaternion.quaternionReciprocal(rotQuatToUse);
			rotQuatToUse = Quaternion.quaternionMultiplication(yRot,invQuatToUse);
		}
		
		// different xyz permutation for keyboard move
		/*float moveFactor = 5.0f;
		
		double[] rotCameraPos = rotQuatToUse.rotateVector3d(new double[]{0,0,-200.0f});
		double[] rotatedMovement = rotQuatToUse.rotateVector3d(new double[]{m_keyboardMoveX*moveFactor,m_keyboardMoveZ*moveFactor,-m_keyboardMoveY*moveFactor});
		camPosX = (float)rotCameraPos[0] + (float)rotatedMovement[0] + xOff;
		camPosY = (float)rotCameraPos[1] + (float)rotatedMovement[1] + yOff;
		camPosZ = (float)rotCameraPos[2] + (float)rotatedMovement[2] + zOff;
		*/
		//FloatBuffer rotMatrixKeyboard = Quaternion.quaternionReciprocal(m_keyboardRotation).getRotationMatrix(true);

	
	
		FloatBuffer finalCamRot =  rotQuatToUse.getRotationMatrix(true) ;
	
		//rotMatrixModelOnly = rotMatrix;
		// start computation
		currentlyActiveKernel.setArg(4, camPosX). // offsets neeed for proper camera position
				setArg(5, camPosY).setArg(6, camPosZ).setArg(7, finalCamRot.get(4 * 1 + 0))
				. // up
				setArg(8, finalCamRot.get(4 * 1 + 1)).setArg(9, finalCamRot.get(4 * 1 + 2)).setArg(10, -finalCamRot.get(4 * 2 + 0))
				. // forward
				setArg(11, -finalCamRot.get(4 * 2 + 1)).setArg(12, -finalCamRot.get(4 * 2 + 2)).setArg(13, finalCamRot.get(4 * 0 + 0))
				. // right
				setArg(14, finalCamRot.get(4 * 0 + 1)).setArg(15, finalCamRot.get(4 * 0 + 2)).setArg(16, fov).setArg(17, asr).setArg(19, MHDReader.Nx).setArg(20, MHDReader.Ny).setArg(21, MHDReader.Nz).setArg(22, (float) dx).setArg(23, (float) dy).setArg(24, (float) dz)
				.setArg(26, octreeLevels).setArg(28, numTransferFunctionSamples).setArg(29, threshold).setArg(30, lin);
	
		clEnqueueNDRangeKernel(queue, currentlyActiveKernel, 2, null, kernel2DGlobalWorkSize, null, null, null);// */
		clFinish(queue);
		// adaptive sampling pass 1
		/*
		 * kernelFirstPass.setArg(4, camPos.x).setArg(5, camPos.y).setArg(6, camPos.z) .setArg(7, camUp.x).setArg(8, camUp.y).setArg(9, camUp.z) .setArg(10, camDir.x).setArg(11, camDir.y).setArg(12, camDir.z) .setArg(13, camRight.x).setArg(14, camRight.y).setArg(15, camRight.z) .setArg(16,
		 * fov).setArg(17, asr) .setArg(19, MHDReader.Nx).setArg(20, MHDReader.Ny).setArg(21, MHDReader.Nz) .setArg(22, (float) dx).setArg(23, (float) dy).setArg(24, (float) dz) .setArg(26, octreeLevels).setArg(28, transferFunctionSamples) .setArg(29,
		 * threshold).setArg(30, lin);
		 * 
		 * clEnqueueNDRangeKernel(queue, kernelFirstPass, 2, null, kernel2DGlobalWorkSize, null, null, null);
		 * 
		 * // adaptive sampling pass 2
		 * 
		 * kernelSecondPass.setArg(4, camPos.x).setArg(5, camPos.y).setArg(6, camPos.z) .setArg(7, camUp.x).setArg(8, camUp.y).setArg(9, camUp.z) .setArg(10, camDir.x).setArg(11, camDir.y).setArg(12, camDir.z) .setArg(13, camRight.x).setArg(14, camRight.y).setArg(15, camRight.z) .setArg(16,
		 * fov).setArg(17, asr) .setArg(19, MHDReader.Nx).setArg(20, MHDReader.Ny).setArg(21, MHDReader.Nz) .setArg(22, (float) dx).setArg(23, (float) dy).setArg(24, (float) dz) .setArg(26, octreeLevels).setArg(28, transferFunctionSamples) .setArg(29,
		 * threshold).setArg(30, lin);
		 * 
		 * clEnqueueNDRangeKernel(queue, kernelSecondPass, 2, null, kernel2DGlobalWorkSize, null, null, null);//
		 */
	
		if (lin == 1 && m_enableSSAO)
		{
			ssao(glBuffers[BUFFER_1], glBuffers[BUFFER_DEPTH_1], glBuffers[BUFFER_2]);
			copyImage(glBuffers[BUFFER_2], glBuffers[BUFFER_1]);
		}

		if (lin == 1 && m_enableDOF)
		{
			convolve(glBuffers[BUFFER_1], glBuffers[BUFFER_2], glBuffers[BUFFER_3]);
			depthOfField(glBuffers[BUFFER_1], glBuffers[BUFFER_3], glBuffers[BUFFER_DEPTH_1], glBuffers[BUFFER_2], m_dofFocus, m_dofStrength);
			copyImage(glBuffers[BUFFER_2], glBuffers[BUFFER_1]);
		}

		for (int i = 0; i < glBuffersCount; i++)
		{
			clEnqueueReleaseGLObjects(queue, glBuffers[i], null, syncGLtoCL ? syncBuffer : null);
		}
		clFinish(queue);
		
		long t = System.currentTimeMillis();
		if (syncGLtoCL)
		{
			clEvent = queue.getCLEvent(syncBuffer.get(0));
			clSync = glCreateSyncFromCLeventARB(queue.getParent(), clEvent, 0);
		}
		
		// block until done (important: finish before doing further gl work)
		if (!syncGLtoCL)
		{
			clFinish(queue);
		}
	
		t = System.currentTimeMillis() - t;
		// System.out.println(t + "ms");
	}

	// OpenGL

	private void render()
	{
		//glClear(GL_COLOR_BUFFER_BIT);
		glDisable(GL_CULL_FACE);

		if (syncGLtoCL)
		{
			glWaitSync(clSync, 0, 0);
		}

		// draw slices
		int sliceWidth = width;

		if (useTextures)
		{
			glBindTexture(GL_TEXTURE_2D, glIDs.get(0));
			glCallList(dlist);
		}
		else
		{
			glBindBuffer(GL_PIXEL_UNPACK_BUFFER, glIDs.get(0));
			glRasterPos2i(0, 0);

			glDrawPixels(sliceWidth, height, GL_RGBA, GL_UNSIGNED_BYTE, 0);

			glBindBuffer(GL_PIXEL_UNPACK_BUFFER, 0);
		}

		if (syncCLtoGL)
		{
			glSync = glFenceSync(GL_SYNC_GPU_COMMANDS_COMPLETE, 0);
			//glEvent = clCreateEventFromGLsyncKHR(clContext, glSync, null);
			//clReleaseEvent(glEvent);
		}
	}

	private void handleIO()
	{
		/*
		 * tPrev = tNow; tNow = System.currentTimeMillis(); float dt = (tNow - tPrev) / 1000.f; if (Keyboard.getNumKeyboardEvents() != 0) { while (Keyboard.next() && !Keyboard.getEventKeyState()) { switch (Keyboard.getEventKey()) { case Keyboard.KEY_ESCAPE: isRunning = false; break; case
		 * Keyboard.KEY_SPACE: lin = lin == 0 ? 1 : 0; break; } } }
		 * 
		 * if (Keyboard.isKeyDown(Keyboard.KEY_W)) { Vector3f camDirPrev = new Vector3f(camDir); camDir.scale(camSpeed * dt); Vector3f.add(camPos, camDir, camPos); camDir = camDirPrev; }
		 * 
		 * if (Keyboard.isKeyDown(Keyboard.KEY_S)) { Vector3f camDirPrev = new Vector3f(camDir); camDir.scale(camSpeed * dt); Vector3f.sub(camPos, camDir, camPos); camDir = camDirPrev; }
		 * 
		 * if (Keyboard.isKeyDown(Keyboard.KEY_A)) { Vector3f camRightPrev = new Vector3f(camRight); camRight.scale(camSpeed * dt); Vector3f.sub(camPos, camRight, camPos); camRight = camRightPrev; }
		 * 
		 * if (Keyboard.isKeyDown(Keyboard.KEY_D)) { Vector3f camRightPrev = new Vector3f(camRight); camRight.scale(camSpeed * dt); Vector3f.add(camPos, camRight, camPos); camRight = camRightPrev; }
		 * 
		 * while (Mouse.next()) { final int x = VeinsWindow.GetMouseX()(); final int y = VeinsWindow.GetMouseY()();
		 * 
		 * if (Mouse.getEventButton() == 2 && Mouse.getEventButtonState()) { lin = lin == 0 ? 1 : 0; System.out.println("click!"); }
		 * 
		 * if (Mouse.getEventButton() == 1 && Mouse.getEventButtonState()) { System.out.println("saved!");
		 * 
		 * try { PrintWriter pr = new PrintWriter(new File("cam.txt")); pr.println("pos: " + camPos.x + " " + camPos.y + " " + camPos.z); pr.println("dir: " + camDir.x + " " + camDir.y + " " + camDir.z); pr.println("rgt: " + camRight.x + " " + camRight.y + " " + camRight.z); pr.println("cup: " +
		 * camUp.x + " " + camUp.y + " " + camUp.z); pr.close(); } catch (FileNotFoundException e) { // TODO Auto-generated catch block e.printStackTrace(); } }
		 * 
		 * if (!Mouse.isButtonDown(0)) { int offsetX = x - mouseX; int offsetY = y - mouseY;
		 * 
		 * float spd = 0.005f; float limit = (float) (Math.PI / 2);
		 * 
		 * camAngle += offsetX * spd; camZAngle += offsetY * spd; camZAngle = camZAngle < -limit ? -limit : (camZAngle > limit ? limit : camZAngle);
		 * 
		 * float ca = (float) Math.cos(camAngle); float sa = (float) Math.sin(camAngle); float cza = (float) Math.cos(camZAngle); float sza = (float) Math.sin(camZAngle); camDir.set(ca * cza, sza, sa * cza); camUp.set(-ca * sza, cza, -sa * sza); Vector3f.cross(camDir, camUp, camRight); }
		 * 
		 * mouseX = x; mouseY = y; }
		 * 
		 * float wheelSpeed = 0.001f; int wheel = Mouse.getDWheel(); float step = 0.0001f; if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) { step = 0.01f; } if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) { step = 0.1f; } if (wheel != 0) { threshold = Math.max(Math.min(threshold + step * (float) wheel *
		 * wheelSpeed, 1.f), 0.f); System.out.println("New threshold: " + threshold); }
		 */
	}

	private static boolean isDoubleFPAvailable(CLDevice device)
	{
		final CLDeviceCapabilities caps = CLCapabilities.getDeviceCapabilities(device);
		return caps.CL_KHR_fp64 || caps.CL_AMD_fp64;
	}

	private void createPrograms() throws IOException
	{
		final String source = getProgramSource("/shaders/Raycast.cl"); // double slashes fail??
		program = clCreateProgramWithSource(clContext, source, null);
	}

	private String getProgramSource(final String file) throws IOException
	{
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(VeinsRenderer.class.getResourceAsStream(file)));		

		final StringBuilder sb = new StringBuilder();
		String line;
		try
		{
			while ((line = reader.readLine()) != null)
				sb.append(line).append("\n");
		}
		finally
		{
			reader.close();
		}

		return sb.toString();
	}

	private static Buffer fastFileRead()
	{
		// System.out.println("Reading raw file " + MHDReader.rawFile + "...");
		File file = new File(MHDReader.rawFile);
		byte[] fileData = new byte[(int) file.length()];
		try
		{
			DataInputStream dis = new DataInputStream(new FileInputStream(file));
			dis.readFully(fileData);
			dis.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			System.exit(-1);
		}
		ByteBuffer buffer = ByteBuffer.wrap(fileData);
		buffer.order(ByteOrder.LITTLE_ENDIAN);

		Buffer returnBuffer = null;
		if (MHDReader.elementType.equals("MET_BYTE"))
			returnBuffer = buffer;
		else if (MHDReader.elementType.equals("MET_SHORT"))
			returnBuffer = buffer.asShortBuffer();

		// Dereference variables
		file = null;
		fileData = null;
		buffer = null;

		return returnBuffer;
	}

	private static float[] bufferAs1DMatrix(Buffer buff)
	{
		float[] matrix = new float[buff.capacity()];
		if (MHDReader.elementType.equals("MET_BYTE"))
		{
			ByteBuffer b = (ByteBuffer) buff;
			for (int i = 0; i < b.capacity(); i++)
			{
				matrix[i] = b.get(i) / 255.f;
			}
		}
		else if (MHDReader.elementType.equals("MET_SHORT"))
		{
			ShortBuffer b = (ShortBuffer) buff;
			for (int i = 0; i < b.capacity(); i++)
			{
				matrix[i] = b.get(i) / 65535.f;
			}
		}
		buff = null;
		return matrix;
	}

	public static float[] readFile(String fileName)
	{
		MHDReader.readMHD(fileName);
		
		Buffer shorts = fastFileRead();
		return bufferAs1DMatrix(shorts);
	}

	public static CLMem locateMemory(float[] data, int flags, CLCommandQueue queue, CLContext context)
	{
		//System.out.println("g31");
		IntBuffer errorBuff = BufferUtils.createIntBuffer(1);
		FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
		buffer.put(data);
		buffer.rewind();
		//System.out.println("g32");
		// System.out.println(buffer.capacity());
		CLMem memory = CL10.clCreateBuffer(context, flags, buffer.capacity() * 4, errorBuff);
		CL10.clEnqueueWriteBuffer(queue, memory, CL10.CL_TRUE, 0, buffer, null, null);
		Util.checkCLError(errorBuff.get(0));
		//System.out.println("g33");
		// Dereference buffer
		buffer = null;

		return memory;
	}

	public static void freeBuffer(CLMem mem)
	{
		if(mem != null)
		{
			CL10.clReleaseMemObject(mem);
		}
	}
	
	private static void writeMemory(float[] data, int flags, CLCommandQueue queue, CLContext context, CLMem memory)
	{
		IntBuffer errorBuff = BufferUtils.createIntBuffer(1);
		FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
		buffer.put(data);
		buffer.rewind();
		CL10.clEnqueueWriteBuffer(queue, memory, CL10.CL_TRUE, 0, buffer, null, null);
		Util.checkCLError(errorBuff.get(0));

		// Dereference buffer
		buffer = null;
	}

	private void execGauss3D(double sigma)
	{
		kernel3DGlobalWorkSize.put(0, MHDReader.Nx).put(1, MHDReader.Ny).put(2, MHDReader.Nz);

		// Init kernels
		CLKernel gaussX = CL10.clCreateKernel(program, "gaussX", null);
		CLKernel gaussY = CL10.clCreateKernel(program, "gaussY", null);
		CLKernel gaussZ = CL10.clCreateKernel(program, "gaussZ", null);

		double dx = MHDReader.dx;
		double dy = MHDReader.dy;
		double dz = MHDReader.dz;
		
		
		// MATRIX
		int size = (int) (2 * Math.ceil(3 * sigma / dx) + 1);
		CLMem kernel = locateMemory(getGauss1DKernel(size, sigma), CL10.CL_MEM_READ_ONLY, queue, clContext);
		Util.checkCLError(CL10.clFinish(queue));
		

		// GAUSS 3D
		gaussX.setArg(0, matrix2).setArg(1, MHDReader.Nx).setArg(2, MHDReader.Ny).setArg(3, MHDReader.Nz).setArg(4, size).setArg(5, kernel).setArg(6, matrix);
		clEnqueueNDRangeKernel(queue, gaussX, 3, null, kernel3DGlobalWorkSize, null, null, null);
		Util.checkCLError(CL10.clFinish(queue));
		gaussY.setArg(0, matrix).setArg(1, MHDReader.Nx).setArg(2, MHDReader.Ny).setArg(3, MHDReader.Nz).setArg(4, size).setArg(5, kernel).setArg(6, matrix2);
		clEnqueueNDRangeKernel(queue, gaussY, 3, null, kernel3DGlobalWorkSize, null, null, null);
		Util.checkCLError(CL10.clFinish(queue));
		gaussZ.setArg(0, matrix2).setArg(1, MHDReader.Nx).setArg(2, MHDReader.Ny).setArg(3, MHDReader.Nz).setArg(4, size).setArg(5, kernel).setArg(6, matrix);
		clEnqueueNDRangeKernel(queue, gaussZ, 3, null, kernel3DGlobalWorkSize, null, null, null);
		Util.checkCLError(CL10.clFinish(queue));
	}

	public float[] getGauss1DKernel(int size, double sigma)
	{
		float[] kernel = new float[size];
		double sum = 0;
		for (int i = 0; i < size; i++)
		{
			double x = i - size / 2;
			kernel[i] = (float) ((1 / (Math.sqrt(2 * Math.PI) * sigma)) * Math.exp(-(x * x) / (2 * sigma * sigma)));
			sum += kernel[i];
		}
		for (int i = 0; i < kernel.length; i++)
		{
			kernel[i] /= sum;
		}

		return kernel;
	}

	public float[] constructOctree(float[] matrix)
	{
		int nx = MHDReader.Nx;
		int ny = MHDReader.Ny;
		int nz = MHDReader.Nz;
		int v = Math.max(Math.max(nx, ny), nz) - 1;

		// log2(next power of 2)
		int res = 0;
		for (int i = 31; i >= 0; i--)
		{
			if ((v & (1 << i)) != 0)
			{
				res = i + 1;
				break;
			}
		}

		octreeLevels = Math.max(res - 3, 0);
		int numberOfNodes = 1;
		int pow8 = 1;
		for (int i = 0; i < octreeLevels; i++)
		{
			pow8 *= 8;
			numberOfNodes += pow8;
		}

		int res2 = (1 << res);
		OctreeNode root = new OctreeNode(matrix, MHDReader.Nx, MHDReader.Ny, MHDReader.Nz, 0, 0, 0, res2, res2, res2, octreeLevels);
		float[] octree = new float[numberOfNodes * 3]; // min, max, avg
		root.storeInto(octree, 0);

		return octree;
	}

	public void readFloatsFromFile(String filename, float[] buffer)
	{
		try
		{
			Scanner sc = new Scanner(new File(filename));
			for (int i = 0; i < buffer.length && sc.hasNext(); i++)
			{
				buffer[i] = Float.parseFloat(sc.next());
				if (i % 4 == 3)
					sc.next(); // skip time attribute
			}
			sc.close();
		}
		catch (FileNotFoundException e)
		{
			System.out.print("File " + filename + " not found. Building linear ramp...");
			for (int i = 0; i < buffer.length; i += 4)
			{
				buffer[i] = 1.f;
				buffer[i + 1] = 0.f;
				buffer[i + 2] = 0.f;
				buffer[i + 3] = i / (float) buffer.length;
			}
		}
	}

	public void convolve(CLMem b1, CLMem b2, CLMem b3)
	{
		kernelsConvolution[KERNEL_CONVOLUTION_X].setArg(0, b1).setArg(1, b2).setArg(2, convolutionMask).setArg(3, convolutionMaskSize);
		kernelsConvolution[KERNEL_CONVOLUTION_Y].setArg(0, b2).setArg(1, b3).setArg(2, convolutionMask).setArg(3, convolutionMaskSize);
		clEnqueueNDRangeKernel(queue, kernelsConvolution[KERNEL_CONVOLUTION_X], 2, null, kernel2DGlobalWorkSize, null, null, null);
		clEnqueueNDRangeKernel(queue, kernelsConvolution[KERNEL_CONVOLUTION_Y], 2, null, kernel2DGlobalWorkSize, null, null, null);
	}

	public void depthOfField(CLMem clear, CLMem blurred, CLMem depthBuffer, CLMem output, float focus, float strength)
	{
		kernelsEffects[KERNEL_DOF].setArg(0, clear).setArg(1, blurred).setArg(2, depthBuffer).setArg(3, output).setArg(4, focus).setArg(5, strength);
		clEnqueueNDRangeKernel(queue, kernelsEffects[KERNEL_DOF], 2, null, kernel2DGlobalWorkSize, null, null, null);
	}

	public void ssao(CLMem image, CLMem depthBuffer, CLMem output)
	{
		kernelsEffects[KERNEL_SSAO].setArg(0, image).setArg(1, depthBuffer).setArg(2, output).setArg(3,m_ssaoStrength);
		clEnqueueNDRangeKernel(queue, kernelsEffects[KERNEL_SSAO], 2, null, kernel2DGlobalWorkSize, null, null, null);
	}

	public void copyImage(CLMem in, CLMem out)
	{
		kernelsEffects[KERNEL_COPY].setArg(0, in).setArg(1, out);
		clEnqueueNDRangeKernel(queue, kernelsEffects[KERNEL_COPY], 2, null, kernel2DGlobalWorkSize, null, null, null);
	}
}
