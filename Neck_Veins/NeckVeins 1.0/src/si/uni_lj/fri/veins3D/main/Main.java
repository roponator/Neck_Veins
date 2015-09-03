package si.uni_lj.fri.veins3D.main;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.SplashScreen;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JWindow;
import javax.swing.SwingConstants;

import com.sun.javafx.applet.Splash;

import si.uni_lj.fri.veins3D.utils.OSValidator;

public class Main
{


	void ShowSplash()
	{
		try
		{
			ClassLoader classLoader = getClass().getClassLoader();
			URI uri = classLoader.getResource("imgs/pngs720p/Med3D-17.png").toURI();
			URL url = uri.toURL();

			JWindow window = new JWindow();
			ImageIcon image = new ImageIcon(url);
			window.getContentPane().add(new JLabel("",image , SwingConstants.CENTER));
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			window.setBounds(screenSize.width / 2 -  image.getIconWidth()/2, screenSize.height / 2 -  image.getIconHeight()/2, image.getIconWidth(), image.getIconHeight());
			window.setVisible(true);
			try
			{
				Thread.sleep(2000);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
			window.setVisible(false);

		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	public static void main(String[] args)
	{
		
		// System.setProperty("org.lwjgl.librarypath", new File("native").getAbsolutePath());
		// setLWJGLNativeLibBindings(); // must be called first
		// System.setProperty("org.lwjgl.librarypath", new File("lwjgl-2.8.4/native").getAbsolutePath());

		// SplashWindow1 sw = new SplashWindow1("imgs/pngs720p/Med3D-17.png");
		
		Main main=new Main();
		//main.ShowSplash();

		new VeinsWindow().mainLoop();
	}

	// sets the native bindings for LWJGL based on which OS this app is ran on
	/*
	 * static void setLWJGLNativeLibBindings() { if (OSValidator.isWindows()) { System.out.println("OS: Windows"); System.setProperty("org.lwjgl.librarypath", new File("lwjgl-2.8.4/native/windows").getAbsolutePath()); } else if (OSValidator.isMac()) { System.out.println("OS: Mac");
	 * System.setProperty("org.lwjgl.librarypath", new File("lwjgl-2.8.4/native/macosx").getAbsolutePath()); } else if (OSValidator.isUnix()) { System.out.println("OS: Linux"); System.setProperty("org.lwjgl.librarypath", new File("lwjgl-2.8.4/native/linux").getAbsolutePath()); } else if
	 * (OSValidator.isSolaris()) { System.out.println("OS ERROR: Solaris is not supported: "+OSValidator.GetOsString()); JOptionPane.showMessageDialog(null,"Solaris is not supported!", "Error" , JOptionPane.INFORMATION_MESSAGE); } else {
	 * System.out.println("OS ERROR: your OS is not supported: "+OSValidator.GetOsString()); JOptionPane.showMessageDialog(null,"Your OS is not supported!", "Error" , JOptionPane.INFORMATION_MESSAGE); }
	 * 
	 * }
	 */

}
