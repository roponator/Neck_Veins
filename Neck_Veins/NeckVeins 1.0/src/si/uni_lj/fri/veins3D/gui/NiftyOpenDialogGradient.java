package si.uni_lj.fri.veins3D.gui;


import de.lessvoid.nifty.controls.Controller;
import de.lessvoid.nifty.controls.DropDownSelectionChangedEvent;
import de.lessvoid.nifty.controls.ListBoxSelectionChangedEvent;
import de.lessvoid.nifty.controls.TreeBox;
import de.lessvoid.nifty.controls.TreeItem;
import de.lessvoid.nifty.controls.Window;
import de.lessvoid.nifty.controls.checkbox.CheckboxControl;
import de.lessvoid.nifty.controls.dropdown.DropDownControl;
import de.lessvoid.nifty.controls.label.LabelControl;
import de.lessvoid.nifty.controls.listbox.ListBoxControl;
import de.lessvoid.nifty.controls.slider.SliderControl;
import de.lessvoid.nifty.controls.window.WindowControl;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.render.NiftyImage;
import de.lessvoid.nifty.tools.SizeValue;
import de.lessvoid.nifty.tools.SizeValueType;
import si.uni_lj.fri.veins3D.gui.NiftyFolderBrowser.MyTreeFolderItem;
import si.uni_lj.fri.veins3D.gui.settings.NeckVeinsSettings;
import si.uni_lj.fri.veins3D.main.VeinsWindow;

public class NiftyOpenDialogGradient
{

	de.lessvoid.nifty.elements.Element m_mainOpenDailogElement = null;
	public NiftyFolderBrowser m_folderBrowser = null;
	WindowControl m_openDialogWindowControl = null;


	public NiftyOpenDialogGradient()
	{
		// find open dialog in main gui
		m_mainOpenDailogElement = NiftyScreenController.m_screen.findElementById("MY_OPEN_DIALOG_FOR_GRADIENT_ID");
		m_openDialogWindowControl = m_mainOpenDailogElement.getAttachedInputControl().getControl(WindowControl.class);
		
		// get control panels and create folder browser
		Element treeboxParentPanel = m_mainOpenDailogElement.findElementById("OPEN_DIALOG_FOR_GRADIENT_FOLDER_TREEBOX_PANEL_CONTAINER");
		Element fileListElement = m_mainOpenDailogElement.findElementById("OPEN_DIALOG_FILES_LIST_LISTBOX");;
		m_folderBrowser = new NiftyFolderBrowser(treeboxParentPanel, fileListElement, null,new String[]{"grad"});
	}

	public void OnOpenDialog()
	{
		m_folderBrowser.OnOpenDialog(); // refreshes files list box content, otherwise some strange not-item selected error
		m_mainOpenDailogElement.setVisible(true);
	}

	public void OnCloseDialog()
	{
		m_mainOpenDailogElement.setVisible(false);
	}

	public void OnEscapeKeyPressed()
	{		
		OnCloseDialog();
	}

	public void ResetPosition()
	{
		m_mainOpenDailogElement.setConstraintX(new SizeValue(10, SizeValueType.PercentWidth));
		m_mainOpenDailogElement.setConstraintY(new SizeValue(10, SizeValueType.PercentHeight));
	}

	public void On_SettingsDialog_Open()
	{
		m_openDialogWindowControl.setEnabled(false);
	}

	
}
