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
import si.uni_lj.fri.veins3D.gui.settings.NeckVeinsSettings;
import si.uni_lj.fri.veins3D.main.VeinsWindow;

public class NiftySaveDialog
{

	de.lessvoid.nifty.elements.Element m_mainOpenDailogElement = null;
	public NiftyFolderBrowserSave m_folderBrowser = null;
	WindowControl m_openDialogWindowControl = null;

	public NiftySaveDialog()
	{
		// find open dialog in main gui
		m_mainOpenDailogElement = NiftyScreenController.m_screen.findElementById("MY_SAVE_DIALOG_ID");
		m_openDialogWindowControl = m_mainOpenDailogElement.getAttachedInputControl().getControl(WindowControl.class);

		// get control panels and create folder browser
		Element treeboxParentPanel = m_mainOpenDailogElement.findElementById("SAVE_DIALOG_FOLDER_TREEBOX_PANEL_CONTAINER");
		Element fileListElement = m_mainOpenDailogElement.findElementById("SAVE_DIALOG_FILES_LIST_LISTBOX");
		Element textFieldElement = m_mainOpenDailogElement.findElementById("OPEN_DIALOG_FILE_TYPE_TEXTFIELD");
		m_folderBrowser = new NiftyFolderBrowserSave(treeboxParentPanel, fileListElement, textFieldElement);

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

	public void ResetPosition()
	{
		m_mainOpenDailogElement.setConstraintX(new SizeValue(10, SizeValueType.PercentWidth));
		m_mainOpenDailogElement.setConstraintY(new SizeValue(10, SizeValueType.PercentHeight));
	}


	void restoreSettingsDialogGUIStates()
	{
		// restore states
		//if (VeinsWindow.settings.selectedModelMethodIndex >= 0 && VeinsWindow.settings.selectedModelMethodIndex < m_methodTypeDropdown.itemCount())
		//	m_methodTypeDropdown.selectItemByIndex(VeinsWindow.settings.selectedModelMethodIndex);

		//NiftyScreenController.ConvertElementToSlider(m_sliderGaussSigmaElement).setValue(VeinsWindow.settings.gaussSigma);
		//NiftyScreenController.ConvertElementToSlider(m_sliderThreshold).setValue(VeinsWindow.settings.threshold);

	}
	
	void setStates()
	{
		//VeinsWindow.settings.selectedModelMethodIndex = m_methodTypeDropdown.getSelectedIndex();

		//VeinsWindow.settings.gaussSigma = NiftyScreenController.ConvertElementToSlider(m_sliderGaussSigmaElement).getValue();
		//VeinsWindow.settings.threshold = NiftyScreenController.ConvertElementToSlider(m_sliderThreshold).getValue();
	}


}
