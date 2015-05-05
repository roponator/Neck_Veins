package si.uni_lj.fri.veins3D.gui;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.vector.Vector3f;

import si.uni_lj.fri.veins3D.gui.render.models.Mesh;
import si.uni_lj.fri.veins3D.gui.render.models.VeinsModel;
import si.uni_lj.fri.veins3D.utils.Tools;
import si.uni_lj.fri.veins3D.gui.render.VeinsRendererInterface;

import com.tpxl.GL.ArrayBuffer;
import com.tpxl.GL.Camera;
import com.tpxl.GL.ElementBuffer;
import com.tpxl.GL.FragmentShader;
import com.tpxl.GL.Framebuffer;
import com.tpxl.GL.OrthoCamera;
import com.tpxl.GL.Program;
import com.tpxl.GL.Shader;
import com.tpxl.GL.Texture;
import com.tpxl.GL.Transform;
import com.tpxl.GL.Utility;
import com.tpxl.GL.VertexArrayObject;
import com.tpxl.GL.VertexShader;
import com.tpxl.GL.exception.GLFramebufferException;
import com.tpxl.GL.exception.GLProgramLinkException;
import com.tpxl.GL.exception.GLShaderCompileException;

public class XRayProjectionModule extends VeinsRendererInterface{
	private static final String resourceLocation = "res//";
	
	private boolean showWireframe,
					dirtyProjectionCamera,
					dirtyViewCamera,
					showScreen;
	private float screenTransparency;
	public Camera 	projectionCamera,	//Make private + getters/setters
				  	viewCamera;
	
	private Camera activeCamera;
	
	private VertexArrayObject model,
							  screen;
	public Transform 	modelTransform,	//Make private + getters/setters
						screenTransform;
	public Program programProject,	//Make private, no need for setters/getters
					programTextureProject,
					programDepth;
	
	private Framebuffer depthBuffer;
	private Texture projectionTexture;
	private FloatBuffer floatBuffer;
	
	private ArrayBuffer screenVertexBuffer,
				screenNormalBuffer,
				screenUVBuffer;
	private ElementBuffer screenElementBuffer;
	
	private VeinsModel veinsModel;
	
	public void flipCamera()
	{
		if(activeCamera == viewCamera)
			activeCamera = projectionCamera;
		else
			activeCamera = viewCamera;
	}
	
	XRayProjectionModule() throws FileNotFoundException, IOException, GLShaderCompileException, GLProgramLinkException, GLFramebufferException, LWJGLException
	{
		super();
		
		showWireframe = false;
		dirtyProjectionCamera = true;
		dirtyViewCamera = true;
		showScreen = true;
		
		screenTransparency = 0.3f;
		
		screenTransform = new Transform();
		modelTransform = new Transform();
		
		//viewCamera = new PerspectiveCamera();
		viewCamera = new OrthoCamera(-100, 100, -100, 100, -200, 200);
		viewCamera.translate(new Vector3f(0, 0, 100));
		
		//projectionCamera = new OrthoCamera(-100, 100, -100, 100, -100, 100);
		//projectionCamera = new PerspectiveCamera(viewCamera);
		projectionCamera = new OrthoCamera(viewCamera);
		((OrthoCamera)projectionCamera).setOrtho(-100, 100, -100, 100, -200, 200);
		
		activeCamera = viewCamera;
		model = new VertexArrayObject();
		depthBuffer = Framebuffer.getDepthFramebuffer(1024, 768);

		
		projectionTexture = XRayProjectionModule.getTexture(resourceLocation + "imgs//Pat12_2D_DSA_AP.jpg");
		//projectionTexture = XRayProjectionModule.getTexture("resources//tex.bmp");
		floatBuffer = BufferUtils.createFloatBuffer(16);
		
		veinsModel = new VeinsModel();
		veinsModel.constructVBOFromObjFile(resourceLocation + "obj//square.obj");
		
		screen = new VertexArrayObject();
		
		screenVertexBuffer = new ArrayBuffer(GL15.GL_STATIC_DRAW);
		screenNormalBuffer = new ArrayBuffer(GL15.GL_STATIC_DRAW);
		screenUVBuffer = new ArrayBuffer(GL15.GL_STATIC_DRAW);
		screenElementBuffer = new ElementBuffer(GL15.GL_STATIC_DRAW);

		Mesh m = veinsModel.getMeshes().get(0);
		m.getVertices();
		FloatBuffer tmpFloatBuffer = Tools.arrayListToBuffer(m.getVertices(), null);
		screenVertexBuffer.setData(tmpFloatBuffer);
		tmpFloatBuffer.clear();
		tmpFloatBuffer.put(Mesh.getNormals(m.getVertices(), m.getFaces()));
		screenNormalBuffer.setData(tmpFloatBuffer);
		IntBuffer tmpIBuffer = Tools.arrayListToBuffer(m.getFaces(), null);
		screenElementBuffer.setData(tmpIBuffer);
		
		screen.bind();
		screen.setElementBuffer(screenElementBuffer);
		screen.enableVertexAttrib(screenVertexBuffer, 0, 3, GL11.GL_FLOAT, false, 0, 0);
		screen.enableVertexAttrib(screenNormalBuffer, 1, 3, GL11.GL_FLOAT, false, 0, 0);
		screen.unbind();
		
		screen.setCount(m.getFaces().size());
		screen.setType(GL11.GL_UNSIGNED_INT);
		screen.unbind();
		programDepth = loadShader(resourceLocation + "shaders//depth");
		
		programProject = loadShader(resourceLocation + "shaders//projection");
		GL20.glUseProgram(programProject.getProgramID());
		programProject.setUniform1i("depthmap", 0);
		programProject.setUniform1i("projectionTexture", 1);
		
		programTextureProject = loadShader(resourceLocation + "shaders//simpleProject");
		GL20.glUseProgram(programTextureProject.getProgramID());
		programTextureProject.setUniform1i("projectionTexture", 1);
		//programTextureProject.setUniform1i("projectionTexture", 0);	//depthmap
		GL20.glUseProgram(0);
	}
	
	private Program loadShader(String name) throws FileNotFoundException, IOException, GLShaderCompileException, GLProgramLinkException
	{
		Shader s1 = new FragmentShader();
		Shader s2 = new VertexShader();
		s1.load(name + ".frag");
		s2.load(name + ".vert");
		s1.compile();
		s2.compile();
		Program program = new Program();
		program.attachShader(s1);
		program.attachShader(s2);
		program.link();
		program.detachShader(s1);
		program.detachShader(s2);
		s1.delete();
		s2.delete();
		return program;
	}
	
	public void cleanup()
	{
		programProject.delete();
		programDepth.delete();
		programTextureProject.delete();
		
		depthBuffer.delete();
		projectionTexture.delete();
		
		screenVertexBuffer.delete();
		screenNormalBuffer.delete();
		screenUVBuffer.delete();
		screenElementBuffer.delete();
	}
	
	public void translateViewCamera(Vector3f offset)
	{
		dirtyViewCamera = true;
		viewCamera.translate(offset);
	}
	
	public void translateProjectionCamera(Vector3f offset)
	{
		dirtyProjectionCamera = true;
		projectionCamera.translate(offset);
	}
	
	public void rotateViewCamera(Vector3f eulerAngles)
	{
		dirtyViewCamera = true;
		viewCamera.rotate(eulerAngles);
	}
	
	public void rotateProjectionCamera(Vector3f eulerAngles)
	{
		dirtyProjectionCamera = true;
		projectionCamera.rotate(eulerAngles);
	}
	
	public void setModel(VertexArrayObject model)
	{
		this.model = model;
	}
	
	public void tick()
	{
		if(dirtyProjectionCamera)
		{
			depthBuffer.bind();
			GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
			GL20.glUseProgram(programDepth.getProgramID());
			
			programDepth.setUniformMatrix4f("M", false, modelTransform.viewToFloatBuffer(floatBuffer));
			programDepth.setUniformMatrix4f("V", false, projectionCamera.viewToFloatBuffer(floatBuffer));
			programDepth.setUniformMatrix4f("P", false, projectionCamera.projectionToFloatBuffer(floatBuffer));
			
			GL11.glEnable(GL11.GL_CULL_FACE);
			GL11.glCullFace(GL11.GL_FRONT);
			model.bind();
			GL11.glDrawElements(GL11.GL_TRIANGLES, model.getCount(), model.getType(), 0);
			model.unbind();
			GL11.glCullFace(GL11.GL_BACK);
			GL11.glDisable(GL11.GL_CULL_FACE);
			GL20.glUseProgram(0);
			
			depthBuffer.unbind();
			dirtyProjectionCamera = false;
			dirtyViewCamera = true; //need to refresh the screen because shadows change
		}
		if(dirtyViewCamera)
		{
			dirtyViewCamera = false;
			
			GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_COLOR_BUFFER_BIT);
			GL20.glUseProgram(programProject.getProgramID());
			GL13.glActiveTexture(GL13.GL_TEXTURE0);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, depthBuffer.getTextureID());
			GL13.glActiveTexture(GL13.GL_TEXTURE1);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, projectionTexture.getTextureID());

			programProject.setUniformMatrix4f("V_camera", false, activeCamera.viewToFloatBuffer(floatBuffer));
			programProject.setUniformMatrix4f("P_camera", false, activeCamera.projectionToFloatBuffer(floatBuffer));
			programProject.setUniformMatrix4f("V_projector", false, projectionCamera.viewToFloatBuffer(floatBuffer));
			programProject.setUniformMatrix4f("P_projector", false, projectionCamera.projectionToFloatBuffer(floatBuffer));
			programProject.setUniformMatrix4f("M", false, modelTransform.viewToFloatBuffer(floatBuffer));
			
			model.bind();
			if(showWireframe)
			{
				GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
				GL11.glDrawElements(GL11.GL_TRIANGLES, model.getCount(), model.getType(), 0);
				GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
				GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
			}
			GL11.glDrawElements(GL11.GL_TRIANGLES, model.getCount(), model.getType(), 0);
			model.unbind();
			
			if(showScreen)
			{
				GL20.glUseProgram(programTextureProject.getProgramID());
				screen.bind();
				programTextureProject.setUniform1f("transparency", screenTransparency);
				programTextureProject.setUniformMatrix4f("M", false, screenTransform.viewToFloatBuffer(floatBuffer));
				programTextureProject.setUniformMatrix4f("V_camera", false, activeCamera.viewToFloatBuffer(floatBuffer));
				programTextureProject.setUniformMatrix4f("P_camera", false, activeCamera.projectionToFloatBuffer(floatBuffer));
				programTextureProject.setUniformMatrix4f("V_projector", false, projectionCamera.viewToFloatBuffer(floatBuffer));
				programTextureProject.setUniformMatrix4f("P_projector", false, projectionCamera.projectionToFloatBuffer(floatBuffer));
				
				GL11.glDrawElements(GL11.GL_TRIANGLES, screen.getCount(), screen.getType(), 0);
				screen.unbind();
			}
			GL20.glUseProgram(0);
		}
	}
	
	private static Texture getTexture(String filename) 
	{
		BufferedImage im=null;
		try {
			im = ImageIO.read(new File(filename));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		ByteBuffer imb = BufferUtils.createByteBuffer(im.getWidth()*im.getHeight()*3);
		imb.clear();
		for(int i=0; i < im.getHeight(); i++)
			for(int j=0; j<  im.getWidth(); j++)
			{
				imb.put((byte) ((im.getRGB(j, i)>>16)&0xff));
				imb.put((byte) ((im.getRGB(j, i)>>8)&0xff));
				imb.put((byte) ((im.getRGB(j, i))&0xff));
			}
		imb.rewind();
		
		Texture ret = new Texture(im.getWidth(), im.getHeight(), GL11.GL_RGB, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, imb);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		Utility.printGLError();
		return ret;
	}

	@Override
	public void render() {
		tick();
	}

	@Override
	public void handleKeyboardInputPresses() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleKeyboardInputContinuous() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleMouseInput(int dx, int dy, int dz, HUD hud,
			VeinsWindow veinsWindow) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setupView() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resetView() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void loadShaders() throws IOException {
		
	}

	@Override
	public void loadModelObj(String absolutePath) {
		if(veinsModel == null)
			veinsModel = new VeinsModel();
		else
			veinsModel.deleteMeshes();
		veinsModel.constructVBOFromObjFile(absolutePath);
	}
}
