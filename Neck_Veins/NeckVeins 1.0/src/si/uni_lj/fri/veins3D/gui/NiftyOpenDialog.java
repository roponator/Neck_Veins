package si.uni_lj.fri.veins3D.gui;

import de.lessvoid.nifty.controls.Controller;
import de.lessvoid.nifty.controls.DropDownSelectionChangedEvent;
import de.lessvoid.nifty.controls.ListBoxSelectionChangedEvent;
import de.lessvoid.nifty.controls.TreeBox;
import de.lessvoid.nifty.controls.TreeItem;
import de.lessvoid.nifty.controls.Window;
import de.lessvoid.nifty.controls.dropdown.DropDownControl;
import de.lessvoid.nifty.controls.listbox.ListBoxControl;
import de.lessvoid.nifty.controls.slider.SliderControl;
import de.lessvoid.nifty.controls.window.WindowControl;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.render.NiftyImage;
import de.lessvoid.nifty.tools.SizeValue;
import de.lessvoid.nifty.tools.SizeValueType;
import si.uni_lj.fri.veins3D.gui.NiftyFolderBrowser.MyTreeFolderItem;
import si.uni_lj.fri.veins3D.main.VeinsWindow;

public class NiftyOpenDialog
{
	public static String METHOD_TYPE_MARCHING_CUBES = "Marhcing Cubes";
	public static String METHOD_TYPE_MPUI = "MPUI";
	public static String METHOD_TYPE_VOLUME_RENDER = "Volume Render";
	
	de.lessvoid.nifty.elements.Element m_mainOpenDailogElement = null;
	public NiftyFolderBrowser m_folderBrowser = null;
	public Element m_optionsDialogControlElement = null;
	WindowControl m_openDialogWindowControl = null;
	WindowControl m_settingsDialogWindowControl = null;
		
	public NiftyOpenDialog()
	{
		// find open dialog in main gui
		m_mainOpenDailogElement = NiftyScreenController.m_screen.findElementById("MY_OPEN_DIALOG_ID");
		m_openDialogWindowControl = m_mainOpenDailogElement.getAttachedInputControl().getControl(WindowControl.class);
		
		// get control panels and create folder browser
		Element treeboxParentPanel = m_mainOpenDailogElement.findElementById("OPEN_DIALOG_FOLDER_TREEBOX_PANEL_CONTAINER");
		Element fileListElement = m_mainOpenDailogElement.findElementById("OPEN_DIALOG_FILES_LIST_LISTBOX");
		Element fileTypeElement = m_mainOpenDailogElement.findElementById("OPEN_DIALOG_FILE_TYPE_DROPDOWN");
		m_folderBrowser = new NiftyFolderBrowser(treeboxParentPanel,fileListElement,fileTypeElement);
		
		// options dialog init
		m_optionsDialogControlElement = NiftyScreenController.m_screen.findElementById("MY_OPEN_DIALOG_OPTION_DIALOG_ID");
		m_settingsDialogWindowControl = m_optionsDialogControlElement.getAttachedInputControl().getControl(WindowControl.class);
		Element sliderGaussSigmaElement = m_settingsDialogWindowControl.getElement().findElementById("slGaussFilterSigma");
		Element sliderThreshold = m_settingsDialogWindowControl.getElement().findElementById("slThresholdLevel");
		NiftyScreenController.InitSlider(sliderGaussSigmaElement, 0.0f, 1.0f, 0.5f, 0.01f, "Gauss Filter Sigma",  "%.2f");
		NiftyScreenController.InitSlider(sliderThreshold, 0.0f, 1.0f, 0.5f, 0.01f, "Threshold",  "%.2f");
		
		de.lessvoid.nifty.controls.DropDown<String> methodTypeDropdown = m_optionsDialogControlElement.findElementById("OPEN_DIALOG_FILE_TYPE_DROPDOWN").getAttachedInputControl().getControl(DropDownControl.class);
		methodTypeDropdown.addItem(METHOD_TYPE_MARCHING_CUBES);
		methodTypeDropdown.addItem(METHOD_TYPE_MPUI);
		methodTypeDropdown.addItem(METHOD_TYPE_VOLUME_RENDER);
		methodTypeDropdown.selectItemByIndex(0);
	}
	
	public void OnOpenDialog()
	{
		m_mainOpenDailogElement.setVisible(true);
	}

	public void OnCloseDialog()
	{
		m_mainOpenDailogElement.setVisible(false);
	}
	
	public void ResetPosition()
	{
		m_mainOpenDailogElement.setConstraintX(new SizeValue(10, SizeValueType.PercentWidth));		
		m_mainOpenDailogElement.setConstraintY(new SizeValue(10, SizeValueType.PercentHeight));	
	}
	
	public void On_SettingsDialog_Open()
	{
		m_optionsDialogControlElement.setVisible(true);
		m_openDialogWindowControl.setEnabled(false);
		m_settingsDialogWindowControl.bringToFront();
		m_optionsDialogControlElement.setFocus();		
	}
	
	public void On_SettingsDialog_CloseOrCancel()
	{
		m_optionsDialogControlElement.setVisible(false);
		m_openDialogWindowControl.setEnabled(true);
	}
	
	public void On_SettingsDialog_OK()
	{		
		m_optionsDialogControlElement.setVisible(false);
		m_openDialogWindowControl.setEnabled(true);
	}
	
}
