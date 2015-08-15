package si.uni_lj.fri.veins3D.gui;

import de.lessvoid.nifty.controls.Controller;
import de.lessvoid.nifty.controls.ListBoxSelectionChangedEvent;
import de.lessvoid.nifty.controls.TreeBox;
import de.lessvoid.nifty.controls.TreeItem;
import de.lessvoid.nifty.controls.Window;
import de.lessvoid.nifty.controls.dropdown.DropDownControl;
import de.lessvoid.nifty.controls.listbox.ListBoxControl;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.render.NiftyImage;
import si.uni_lj.fri.veins3D.gui.NiftyFolderBrowser.MyTreeItem;
import si.uni_lj.fri.veins3D.main.VeinsWindow;

public class NiftyOpenDialog
{
	de.lessvoid.nifty.elements.Element m_mainOpenDailogElement = null;
	NiftyFolderBrowser m_folderBrowser = null;

	public NiftyOpenDialog()
	{
		// find open dialog in main gui
		m_mainOpenDailogElement = NiftyScreenController.m_screen.findElementById("MY_OPEN_DIALOG_ID");

		// get control panels and create folder browser
		Element treeboxParentPanel = m_mainOpenDailogElement.findElementById("OPEN_DIALOG_FOLDER_TREEBOX_PANEL_CONTAINER");
		Element fileListElement = m_mainOpenDailogElement.findElementById("OPEN_DIALOG_FILES_LIST_LISTBOX");
		Element fileTypeElement = m_mainOpenDailogElement.findElementById("OPEN_DIALOG_FILE_TYPE_DROPDOWN");
		m_folderBrowser = new NiftyFolderBrowser(treeboxParentPanel,fileListElement,fileTypeElement);
		
	}

	public void OnCloseDialog()
	{
		m_mainOpenDailogElement.setVisible(false);
	}
	
	public void OnTreeboxSelectionChanged(String id,ListBoxSelectionChangedEvent<TreeItem<MyTreeItem>> event)
	{
		m_folderBrowser.OnTreeboxSelectionChanged(id,event);
	}
}
