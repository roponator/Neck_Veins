package si.uni_lj.fri.veins3D.gui;

import de.lessvoid.nifty.controls.Controller;
import de.lessvoid.nifty.controls.TreeBox;
import de.lessvoid.nifty.controls.TreeItem;
import de.lessvoid.nifty.controls.Window;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.render.NiftyImage;
import si.uni_lj.fri.veins3D.main.VeinsWindow;

public class NiftyOpenDialog
{
	de.lessvoid.nifty.elements.Element m_mainOpenDailogElement = null;
	NiftyFolderBrowser m_folderBrowser = null;

	public NiftyOpenDialog()
	{

		m_mainOpenDailogElement = NiftyScreenController.m_screen.findElementById("MY_OPEN_DIALOG_ID");

		TreeBox tb = m_mainOpenDailogElement.findNiftyControl("OPEN_DIALOG_FOLDER_TREE", TreeBox.class);
		m_folderBrowser = new NiftyFolderBrowser(tb);
	}

	public void OnCloseDialog()
	{
		m_mainOpenDailogElement.setVisible(false);
	}
}
