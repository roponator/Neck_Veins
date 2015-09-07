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

	Element m_mcControl = null;
	Element m_mpuiControl = null;
	Element m_volumeControl = null;

	SizeValue m_marchingCubesControlHeight = null;
	SizeValue m_mpuiControlHeight = null;
	SizeValue m_VolumeControlHeight = null;

	Element m_sliderMCGaussSigmaElement = null;
	Element m_sliderMCThreshold = null;

	Element m_sliderMPUIAlpha = null;
	Element m_sliderMPUIError = null;
	Element m_sliderMPUIResolution = null;
	Element m_sliderMPUISampleSize = null;

	Element m_sliderVolumeGauss = null;

	CheckboxControl m_mpuiUsePointCloudCheckbox = null;

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

		m_optionsDialogControlElement = NiftyScreenController.m_screen.findElementById("MY_OPEN_DIALOG_OPTION_DIALOG_ID");
		m_settingsDialogWindowControl = m_optionsDialogControlElement.getAttachedInputControl().getControl(WindowControl.class);

		// marching cubes
		m_mcControl = m_settingsDialogWindowControl.getElement().findElementById("MY_OPEN_MENU_OPTIONS_MC_ID");
		m_marchingCubesControlHeight = m_mcControl.getConstraintHeight();
		m_sliderMCGaussSigmaElement = m_mcControl.findElementById("slGaussFilterSigma");
		m_sliderMCThreshold = m_mcControl.findElementById("slThresholdLevel");
		NiftyScreenController.InitSlider(m_sliderMCGaussSigmaElement, 0.0f, 1.0f, 0.5f, 0.01f, "Gauss Filter Sigma", "%.2f");
		NiftyScreenController.InitSlider(m_sliderMCThreshold, 0.0f, 1.0f, 0.5f, 0.01f, "Threshold", "%.2f");

		// mpui
		m_mpuiControl = m_settingsDialogWindowControl.getElement().findElementById("MY_OPEN_MENU_OPTIONS_MPUI_ID");
		m_mpuiControlHeight = m_mpuiControl.getConstraintHeight();
		m_sliderMPUIAlpha = m_mpuiControl.findElementById("openSettings_mpuiAlpha");
		m_sliderMPUIError = m_mpuiControl.findElementById("openSettings_mpuiError");
		m_sliderMPUIResolution = m_mpuiControl.findElementById("openSettings_mpuiResolution");
		m_sliderMPUISampleSize = m_mpuiControl.findElementById("openSettings_mpuiSampleSize");
		NiftyScreenController.InitSlider(m_sliderMPUIAlpha, 0.0f, 5.0f, VeinsWindow.settings.MPUI__APLHA, 0.01f, "Alpha", "%.2f");
		NiftyScreenController.InitSlider(m_sliderMPUIError, 0.0f, 0.01f, VeinsWindow.settings.MPUI__ERROR, 0.001f, "Error", "%.4f");
		NiftyScreenController.InitSlider(m_sliderMPUIResolution, 0.0f, 0.1f, VeinsWindow.settings.MPUI__RESOLUTION, 0.001f, "Resolution", "%.4f");
		NiftyScreenController.InitSlider(m_sliderMPUISampleSize, 0.0f, 400.0f, VeinsWindow.settings.MPUI__SAMPLE_SIZE, 1.0f, "Sample Size", "%.0f");

		m_mpuiUsePointCloudCheckbox = m_mpuiControl.findElementById("checkbox_mpuiPointCloud").getAttachedInputControl().getControl(CheckboxControl.class);
		m_mpuiUsePointCloudCheckbox.getElement().findElementById("checkboxLabel").getAttachedInputControl().getControl(LabelControl.class).setText("Point Cloud");
		m_mpuiUsePointCloudCheckbox.setChecked(VeinsWindow.settings.MPUI__POINT_CLOUD);

		// volume
		m_volumeControl = m_settingsDialogWindowControl.getElement().findElementById("MY_OPEN_MENU_OPTIONS_VOLUME_ID");
		m_VolumeControlHeight = m_volumeControl.getConstraintHeight();
		m_sliderVolumeGauss = m_volumeControl.findElementById("volumeRendererGaussFilterSigma");
		NiftyScreenController.InitSlider(m_sliderVolumeGauss, 0.0f, 2.0f, VeinsWindow.settings.VOLUME_RENDER_GAUSS_SIGMA, 0.1f, "Gauss Sigma", "%.1f");

		// method type
		m_methodTypeDropdown = m_optionsDialogControlElement.findElementById("OPEN_DIALOG_FILE_TYPE_DROPDOWN").getAttachedInputControl().getControl(DropDownControl.class);
		m_methodTypeDropdown.addItem(METHOD_TYPE_MARCHING_CUBES);
		m_methodTypeDropdown.addItem(METHOD_TYPE_MPUI);
		m_methodTypeDropdown.addItem(METHOD_TYPE_VOLUME_RENDER);
		m_methodTypeDropdown.selectItemByIndex(0);
		showSpecificControlsForSelectedRenderMethod(0);
		
		// restore control states
		restoreSettingsDialogGUIStates();

		// hide all
		showMarchingCubesControl(false);
		showMPUIControl(false);
		showVolumeControl(false);
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
		if (m_optionsDialogControlElement.isVisible())
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
		if (selected == null)
			selected = "";
		m_folderBrowser.SetAvalibleFileTypesBasedOnMethodType(selected);
	}

	void restoreSettingsDialogGUIStates()
	{
		// restore states
		if (VeinsWindow.settings.selectedModelMethodIndex >= 0 && VeinsWindow.settings.selectedModelMethodIndex < m_methodTypeDropdown.itemCount())
			m_methodTypeDropdown.selectItemByIndex(VeinsWindow.settings.selectedModelMethodIndex);

		NiftyScreenController.ConvertElementToSlider(m_sliderMCGaussSigmaElement).setValue(VeinsWindow.settings.gaussSigma);
		NiftyScreenController.ConvertElementToSlider(m_sliderMCThreshold).setValue(VeinsWindow.settings.threshold);

		NiftyScreenController.ConvertElementToSlider(m_sliderMPUIAlpha).setValue(VeinsWindow.settings.MPUI__APLHA);
		NiftyScreenController.ConvertElementToSlider(m_sliderMPUIError).setValue(VeinsWindow.settings.MPUI__ERROR);
		NiftyScreenController.ConvertElementToSlider(m_sliderMPUIResolution).setValue(VeinsWindow.settings.MPUI__RESOLUTION);
		NiftyScreenController.ConvertElementToSlider(m_sliderMPUISampleSize).setValue(VeinsWindow.settings.MPUI__SAMPLE_SIZE);

		NiftyScreenController.ConvertElementToSlider(m_sliderVolumeGauss).setValue(VeinsWindow.settings.VOLUME_RENDER_GAUSS_SIGMA);

	}

	void setStates()
	{
		VeinsWindow.settings.selectedModelMethodIndex = m_methodTypeDropdown.getSelectedIndex();

		VeinsWindow.settings.gaussSigma = NiftyScreenController.ConvertElementToSlider(m_sliderMCGaussSigmaElement).getValue();
		VeinsWindow.settings.threshold = NiftyScreenController.ConvertElementToSlider(m_sliderMCThreshold).getValue();

		VeinsWindow.settings.MPUI__APLHA = NiftyScreenController.ConvertElementToSlider(m_sliderMPUIAlpha).getValue();
		VeinsWindow.settings.MPUI__ERROR = NiftyScreenController.ConvertElementToSlider(m_sliderMPUIError).getValue();
		VeinsWindow.settings.MPUI__RESOLUTION = NiftyScreenController.ConvertElementToSlider(m_sliderMPUIResolution).getValue();
		VeinsWindow.settings.MPUI__SAMPLE_SIZE = (int) NiftyScreenController.ConvertElementToSlider(m_sliderMPUISampleSize).getValue();
		VeinsWindow.settings.MPUI__POINT_CLOUD = m_mpuiUsePointCloudCheckbox.isChecked();

		VeinsWindow.settings.VOLUME_RENDER_GAUSS_SIGMA = NiftyScreenController.ConvertElementToSlider(m_sliderVolumeGauss).getValue();

	}

	public void showMarchingCubesControl(boolean show)
	{
		if (show == false)
		{
			m_mcControl.setVisible(false);
			m_mcControl.setConstraintHeight(new SizeValue(0, SizeValueType.Pixel));
		}
		else
		{
			m_mcControl.setVisible(true);
			m_mcControl.setConstraintHeight(m_marchingCubesControlHeight);
		}

		m_settingsDialogWindowControl.getElement().layoutElements();
	}

	public void showMPUIControl(boolean show)
	{
		if (show == false)
		{
			m_mpuiControl.setVisible(false);
			m_mpuiControl.setConstraintHeight(new SizeValue(0, SizeValueType.Pixel));
		}
		else
		{
			m_mpuiControl.setVisible(true);
			m_mpuiControl.setConstraintHeight(m_mpuiControlHeight);
		}

		m_settingsDialogWindowControl.getElement().layoutElements();
	}

	public void showVolumeControl(boolean show)
	{
		if (show == false)
		{
			m_volumeControl.setVisible(false);
			m_volumeControl.setConstraintHeight(new SizeValue(0, SizeValueType.Pixel));
		}
		else
		{
			m_volumeControl.setVisible(true);
			m_volumeControl.setConstraintHeight(m_VolumeControlHeight);
		}

		m_settingsDialogWindowControl.getElement().layoutElements();
	}

	public void showSpecificControlsForSelectedRenderMethod(int methodDropdownBoxIndex)
	{
		if (methodDropdownBoxIndex == 0)
		{		
			showMPUIControl(false);
			showVolumeControl(false);
			showMarchingCubesControl(true);
		}

		if (methodDropdownBoxIndex == 1)
		{
			showMarchingCubesControl(false);		
			showVolumeControl(false);
			showMPUIControl(true);
		}

		if (methodDropdownBoxIndex == 2)
		{
			showMarchingCubesControl(false);
			showMPUIControl(false);
			showVolumeControl(true);
		}
	}
}
