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
import si.uni_lj.fri.veins3D.gui.settings.NeckVeinsSettings;
import si.uni_lj.fri.veins3D.main.VeinsWindow;

public class NiftyOpenDialog
{
	public static String METHOD_TYPE_MARCHING_CUBES = "Marhcing Cubes";
	public static String METHOD_TYPE_MPUI = "MPUI";
	public static String METHOD_TYPE_VOLUME_RENDER = "Volume Render";

	de.lessvoid.nifty.elements.Element m_mainOpenDailogElement = null;
	public NiftyFolderBrowser m_folderBrowser = null;
	public Element m_optionsDialogControlElement = null;
	public de.lessvoid.nifty.controls.DropDown<String> m_methodTypeDropdown = null;
	WindowControl m_openDialogWindowControl = null;
	WindowControl m_settingsDialogWindowControl = null;
	Element m_sliderGaussSigmaElement = null;
	Element m_sliderThreshold = null;

	public NiftyOpenDialog()
	{
		// find open dialog in main gui
		m_mainOpenDailogElement = NiftyScreenController.m_screen.findElementById("MY_OPEN_DIALOG_ID");
		m_openDialogWindowControl = m_mainOpenDailogElement.getAttachedInputControl().getControl(WindowControl.class);

		// get control panels and create folder browser
		Element treeboxParentPanel = m_mainOpenDailogElement.findElementById("OPEN_DIALOG_FOLDER_TREEBOX_PANEL_CONTAINER");
		Element fileListElement = m_mainOpenDailogElement.findElementById("OPEN_DIALOG_FILES_LIST_LISTBOX");
		Element fileTypeElement = m_mainOpenDailogElement.findElementById("OPEN_DIALOG_FILE_TYPE_DROPDOWN");
		m_folderBrowser = new NiftyFolderBrowser(treeboxParentPanel, fileListElement, fileTypeElement);

		// options dialog init
		m_optionsDialogControlElement = NiftyScreenController.m_screen.findElementById("MY_OPEN_DIALOG_OPTION_DIALOG_ID");
		m_settingsDialogWindowControl = m_optionsDialogControlElement.getAttachedInputControl().getControl(WindowControl.class);
		m_sliderGaussSigmaElement = m_settingsDialogWindowControl.getElement().findElementById("slGaussFilterSigma");
		m_sliderThreshold = m_settingsDialogWindowControl.getElement().findElementById("slThresholdLevel");
		NiftyScreenController.InitSlider(m_sliderGaussSigmaElement, 0.0f, 1.0f, 0.5f, 0.01f, "Gauss Filter Sigma", "%.2f");
		NiftyScreenController.InitSlider(m_sliderThreshold, 0.0f, 1.0f, 0.5f, 0.01f, "Threshold", "%.2f");

		m_methodTypeDropdown = m_optionsDialogControlElement.findElementById("OPEN_DIALOG_FILE_TYPE_DROPDOWN").getAttachedInputControl().getControl(DropDownControl.class);
		m_methodTypeDropdown.addItem(METHOD_TYPE_MARCHING_CUBES);
		m_methodTypeDropdown.addItem(METHOD_TYPE_MPUI);
		m_methodTypeDropdown.addItem(METHOD_TYPE_VOLUME_RENDER);
		m_methodTypeDropdown.selectItemByIndex(0);
		
		// restore control states
		restoreSettingsDialogGUIStates();
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
		if(m_optionsDialogControlElement.isVisible())
		{
			On_SettingsDialog_CloseOrCancel();
		}
		else
		{
			OnCloseDialog();
		}
	}

	public void ResetPosition()
	{
		m_mainOpenDailogElement.setConstraintX(new SizeValue(10, SizeValueType.PercentWidth));
		m_mainOpenDailogElement.setConstraintY(new SizeValue(10, SizeValueType.PercentHeight));
	}

	public void On_SettingsDialog_Open()
	{
		restoreSettingsDialogGUIStates();

		m_optionsDialogControlElement.setVisible(true);
		m_openDialogWindowControl.setEnabled(false);
		m_settingsDialogWindowControl.bringToFront();
		m_optionsDialogControlElement.setFocus();
	}

	public void On_SettingsDialog_CloseOrCancel()
	{
		updateAvalibleFileTypesBasedOnSelectedMethodType();
		
		m_optionsDialogControlElement.setVisible(false);
		m_openDialogWindowControl.setEnabled(true);
		
		restoreSettingsDialogGUIStates();
	}

	public void On_SettingsDialog_OK()
	{
		setStates();
		updateAvalibleFileTypesBasedOnSelectedMethodType();
		
		m_optionsDialogControlElement.setVisible(false);
		m_openDialogWindowControl.setEnabled(true);
	}
	
	public void updateAvalibleFileTypesBasedOnSelectedMethodType()
	{
		String selected = m_methodTypeDropdown.getSelection();
		if( selected == null)
			selected = "";
		m_folderBrowser.SetAvalibleFileTypesBasedOnMethodType(selected);
	}

	void restoreSettingsDialogGUIStates()
	{
		// restore states
		if (VeinsWindow.settings.selectedModelMethodIndex >= 0 && VeinsWindow.settings.selectedModelMethodIndex < m_methodTypeDropdown.itemCount())
			m_methodTypeDropdown.selectItemByIndex(VeinsWindow.settings.selectedModelMethodIndex);

		NiftyScreenController.ConvertElementToSlider(m_sliderGaussSigmaElement).setValue(VeinsWindow.settings.gaussSigma);
		NiftyScreenController.ConvertElementToSlider(m_sliderThreshold).setValue(VeinsWindow.settings.threshold);

	}
	
	void setStates()
	{
		VeinsWindow.settings.selectedModelMethodIndex = m_methodTypeDropdown.getSelectedIndex();

		VeinsWindow.settings.gaussSigma = NiftyScreenController.ConvertElementToSlider(m_sliderGaussSigmaElement).getValue();
		VeinsWindow.settings.threshold = NiftyScreenController.ConvertElementToSlider(m_sliderThreshold).getValue();
	}


}
