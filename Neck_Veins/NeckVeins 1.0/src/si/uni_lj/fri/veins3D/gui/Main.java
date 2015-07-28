package si.uni_lj.fri.veins3D.gui;

import java.io.File;

import javax.swing.JOptionPane;

import si.uni_lj.fri.veins3D.utils.OSValidator;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{		  
		//System.setProperty("org.lwjgl.librarypath", new File("native").getAbsolutePath());
		//setLWJGLNativeLibBindings(); // must be called first
		//System.setProperty("org.lwjgl.librarypath", new File("lwjgl-2.8.4/native").getAbsolutePath());	
		new VeinsWindow().mainLoop();
	}
	
	// sets the native bindings for LWJGL based on which OS this app is ran on
	/*static void setLWJGLNativeLibBindings()
	{
		if (OSValidator.isWindows()) 
		{
			System.out.println("OS: Windows");
			System.setProperty("org.lwjgl.librarypath", new File("lwjgl-2.8.4/native/windows").getAbsolutePath());		
		} 
		else if (OSValidator.isMac())
		{
			System.out.println("OS: Mac");
			System.setProperty("org.lwjgl.librarypath", new File("lwjgl-2.8.4/native/macosx").getAbsolutePath());		
		} 
		else if (OSValidator.isUnix()) 
		{
			System.out.println("OS: Linux");
			System.setProperty("org.lwjgl.librarypath", new File("lwjgl-2.8.4/native/linux").getAbsolutePath());		
		} 
		else if (OSValidator.isSolaris()) 
		{
			System.out.println("OS ERROR: Solaris is not supported: "+OSValidator.GetOsString());
			JOptionPane.showMessageDialog(null,"Solaris is not supported!", "Error" , JOptionPane.INFORMATION_MESSAGE);
		} 
		else
		{
			System.out.println("OS ERROR: your OS is not supported: "+OSValidator.GetOsString());
			JOptionPane.showMessageDialog(null,"Your OS is not supported!", "Error" , JOptionPane.INFORMATION_MESSAGE);
		}
		
	}*/
	
	
}
