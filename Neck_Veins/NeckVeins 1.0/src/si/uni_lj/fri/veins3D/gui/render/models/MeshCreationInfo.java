package si.uni_lj.fri.veins3D.gui.render.models;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;

/*
 * Stores info about how the mesh was created.
 * (used for generating the name of obj files)
 */
public class MeshCreationInfo
{
	public enum MeshCreationMethod
	{
		MARCHING_CUBES,
		MPUI
	}
	
	// TODO: INCREASE/DECREASE NUM DECIMALS?
	static final String m_decimalFormat= "#.############";

	// ----------------------------------------------
	// Common base class for info, method infos derive from this eg marching cubes or MPUI
	// ----------------------------------------------
	public static class MeshInfo
	{
		// ctor sets method type
		public MeshInfo(String originalFileNameWithoutExtension, MeshCreationMethod method)
		{
			m_originalFileNameWithoutExtension = originalFileNameWithoutExtension;
			m_method = method;
		}
		
		// Returns full obj file name
		public String GetObjFilePath()
		{
			return toString()+".obj";
		}
		
		// Returns creation params as a string
		@Override
		public String toString(){return "";}
		
		protected String m_originalFileNameWithoutExtension = "";
		protected MeshCreationMethod m_method = MeshCreationMethod.MARCHING_CUBES;
		public int m_numSubdivisions = 0;
	}

	// ----------------------------------------------
	// Derives from InfoBase, stores marching cubes creation data
	// ----------------------------------------------
	public static class InfoMarchingCubes extends MeshInfo
	{
		public InfoMarchingCubes(String originalFileNameWithoutExtension,double sigma,double threshold)
		{
			super(originalFileNameWithoutExtension,MeshCreationMethod.MARCHING_CUBES);
			
			m_sigma = sigma;
			m_threshold = threshold;
		}

		@Override
		public String toString()
		{
			DecimalFormat df = new DecimalFormat(m_decimalFormat);
			return  m_originalFileNameWithoutExtension+
					"_pMethod_"+m_method.toString()+
					"_pSigma_"+df.format(m_sigma)+
					"_pThreshold_"+df.format(m_threshold)+
					"_pNumSubdivisions_"+Integer.toString(m_numSubdivisions)+
					"_";
		}
		
		double m_sigma = 0.0;
		double m_threshold = 0.0;
		
	}
	
	// ----------------------------------------------
	// Derives from InfoBase, stores MPUI cubes creation data
	// ----------------------------------------------
	public static class InfoMPUI extends MeshInfo
	{
		public InfoMPUI(String originalFileNameWithoutExtension,double sigma,double threshold,float alpha, float lambda, float error, float resolution, boolean pointCloud)
		{
			super(originalFileNameWithoutExtension,MeshCreationMethod.MPUI);
			
			m_sigma = sigma;
			m_threshold = threshold;
			m_alpha = alpha;
			m_lambda = lambda;
			m_error = error;
			m_resolution = resolution;
			m_pointCloud = pointCloud;			
		}

		@Override
		public String toString()
		{
			DecimalFormat df = new DecimalFormat(m_decimalFormat);
			return m_originalFileNameWithoutExtension+
					"_pMethod_"+m_method.toString()+
					"_pSigma_"+df.format(m_sigma)+
					"_pThreshold_"+df.format(m_threshold)+
					"_pAlpha_"+df.format(m_alpha)+
					"_pLambda_"+df.format(m_lambda)+
					"_pError_"+df.format(m_error)+
					"_pResolution_"+df.format(m_resolution)+
					"_pPointCloud_"+Boolean.toString(m_pointCloud)+
					"_pNumSubdivisions_"+Integer.toString(m_numSubdivisions)+
					"_";
		}
		
		double m_sigma = 0.0;
		double m_threshold = 0.0;
		float m_alpha;
		float m_lambda;
		float m_error;
		float m_resolution;
		boolean m_pointCloud;
		
	}
	
	// Returns "myFile" from "//blabla//myFile.type": it removes path and extension.
	public static String GetFileNameOnlyFromPath(String fullFilePath)
	{
		Path p = Paths.get(fullFilePath);
		String fileNameOnly = p.getFileName().toString();
		fileNameOnly = fileNameOnly.substring(0, fileNameOnly.lastIndexOf("."));
		return fileNameOnly;
	}
	
}
