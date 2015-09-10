package si.uni_lj.fri.veins3D.gui;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.swing.JFrame;

import org.lwjgl.LWJGLException;
import org.lwjgl.LWJGLUtil;
import org.lwjgl.input.Mouse;
import org.lwjgl.opencl.CL;
import org.lwjgl.opencl.CLCapabilities;
import org.lwjgl.opencl.CLPlatform;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

import si.uni_lj.fri.veins3D.gui.NiftyFolderBrowser.MyFileExtensionItem;
import si.uni_lj.fri.veins3D.gui.NiftyFolderBrowser.MyTreeFolderItem;
import si.uni_lj.fri.veins3D.gui.NiftyFolderBrowser.MyTreeFolderItem;
import si.uni_lj.fri.veins3D.gui.NiftyFolderBrowser.SelectedFile;
import si.uni_lj.fri.veins3D.gui.render.VeinsRenderer.ModelType;
import si.uni_lj.fri.veins3D.gui.render.models.Mesh;
import si.uni_lj.fri.veins3D.gui.render.models.VeinsModelMesh;
import si.uni_lj.fri.veins3D.main.VeinsWindow;
import si.uni_lj.fri.volumeRaycast.VolumeRaycast;
import si.uni_lj.fri.volumeRaycast.VolumeRaycast.RenderMethod;
import de.lessvoid.nifty.EndNotify;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEvent;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.NiftyMouse;
import de.lessvoid.nifty.controls.ButtonClickedEvent;
import de.lessvoid.nifty.controls.CheckBoxStateChangedEvent;
import de.lessvoid.nifty.controls.Controller;
import de.lessvoid.nifty.controls.DropDownSelectionChangedEvent;
import de.lessvoid.nifty.controls.Label;
import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.controls.ListBoxSelectionChangedEvent;
import de.lessvoid.nifty.controls.NiftyInputControl;
import de.lessvoid.nifty.controls.Slider;
import de.lessvoid.nifty.controls.SliderChangedEvent;
import de.lessvoid.nifty.controls.TextFieldChangedEvent;
import de.lessvoid.nifty.controls.TreeItem;
import de.lessvoid.nifty.controls.checkbox.CheckboxControl;
import de.lessvoid.nifty.controls.dropdown.DropDownControl;
import de.lessvoid.nifty.controls.label.LabelControl;
import de.lessvoid.nifty.controls.slider.SliderControl;
import de.lessvoid.nifty.controls.slider.SliderImpl;
import de.lessvoid.nifty.controls.window.WindowControl;
import de.lessvoid.nifty.effects.EffectEventId;
import de.lessvoid.nifty.effects.impl.Move;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.ImageRenderer;
import de.lessvoid.nifty.html.NiftyHtmlGenerator;
import de.lessvoid.nifty.input.NiftyInputEvent;
import de.lessvoid.nifty.input.NiftyMouseInputEvent;
import de.lessvoid.nifty.screen.DefaultScreenController;
import de.lessvoid.nifty.screen.KeyInputHandler;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.tools.SizeValue;
import de.lessvoid.nifty.tools.SizeValueType;

/*
 This class gets created by Nifty when loading XML
 */
public class NiftyScreenController extends DefaultScreenController
{
	// CUSTOM CONTROL IMPORTANT INSTRUCTION: SEE COMMENT IN FILE myNavigationWidgetControl_WASD.xml

	static final String INPUT_SETTINGS_MOVE_CAMERA = "Move Camera";
	static final String INPUT_SETTINGS_MOVE_MODEL = "Move Model";

	static final String INPUT_SETTINGS_INPUT_TYPE_NORMAL = "Default";
	static final String INPUT_SETTINGS_INPUT_TYPE_3DMOUSE = "3D Mouse";
	static final String INPUT_SETTINGS_INPUT_TYPE_LEAPMOTION = "Leap Motion";

	public static Screen m_screen = null;
	public static final String m_supportedType_MHD = "mhd";
	public static final String m_supportedType_OBJ = "obj";
	public static String[] m_supportedFileTypes = new String[]
	{ m_supportedType_MHD, m_supportedType_OBJ };

	// -----------------------------------
	// GUI state
	// -----------------------------------
	public enum GUI_STATE
	{
		DEFAULT, //
		DIALOG_OPEN, // if this state is enabled then all clicks except for that dialog are disabled, only one dialog can be open at a time?
		DIALOG_SUBDIALOG_OPEN, // if a dialog is already open and we open another dialog in it (eg the Options dialog in the Open File dialog)
	};

	public enum NAVIGATION_WIDGET_BUTTON
	{
		NONE, CENTER_CIRCLE_LEFT, CENTER_CIRCLE_RIGHT, CENTER_CIRCLE_UP, CENTER_CIRCLE_DOWN, SIDE_CIRCLE_LEFT, SIDE_CIRCLE_RIGHT, CLOSE_CIRCLE, MOVE_WIDGET_CIRCLE
	}

	// use getState/setState functions, do not access this directly!
	public static GUI_STATE __m_guiState_doNotAccessThisDirectly = GUI_STATE.DEFAULT;

	// -----------------------------------
	// HTML
	// -----------------------------------
	private NiftyHtmlGenerator m_htmlGenerator;

	// -----------------------------------
	// Loading bar dialog
	// -----------------------------------
	static NiftyLoadingBarDialog m_loadingBarDialog = null;

	// -----------------------------------
	// Top menu bar buttons
	// -----------------------------------
	static final int TOPMENU_DROPDOWN_FILE = 0;
	static final int TOPMENU_DROPDOWN_OPTIONS = 1;
	static final int TOPMENU_DROPDOWN_HELP = 2;
	static Element[] m_panel_topMenu_DropDownMenus;

	// -----------------------------------
	// Side menu
	// -----------------------------------
	public static NiftySettingsSideMenu m_settingsSideMenu = null;
	boolean m_isSettginsSideMenuOpened = false;

	// -----------------------------------
	// Resolution menu
	// -----------------------------------
	NiftyResolutionMenu m_resolutionMenu = null;
	// DisplayMode m_lastWindowedResoltuon = null;

	// -----------------------------------
	// Stereo menu
	// -----------------------------------
	NiftyStereoMenu m_stereoMenu = null;

	// -----------------------------------
	// Navigation widgets
	// -----------------------------------
	static class NavigationWidget
	{
		public Element m_navigationWidget = null;

		boolean m_moveNavigationWidget = false;

		public NavigationWidget(Element element)
		{
			m_navigationWidget = element;
		}

		public boolean IsWidgetInMoveState()
		{
			return m_moveNavigationWidget;
		}

		public void StartMoving()
		{
			m_moveNavigationWidget = true;
		}

		public void Move()
		{
			if (m_moveNavigationWidget)
			{
				WindowControl wc = this.m_navigationWidget.getControl(de.lessvoid.nifty.controls.window.WindowControl.class);
				NiftyMouse mouse = VeinsWindow.nifty.getNiftyMouse();
				wc.drag(mouse.getX(), mouse.getY());
			}
		}

		public void StopMoving()
		{
			m_moveNavigationWidget = false;
		}

	}

	public static NavigationWidget m_navWidgetWASD = null; // the one with WASD
	public static NavigationWidget m_navWidgetUDLR = null; // the one with arrows

	// -----------------------------------
	// Sliders
	// -----------------------------------
	static HashMap<Element, String> m_sliderOutputTextFormat = new HashMap<Element, String>(); // stores how the slider value label is printed for every slider(eg one decimal, int ,...)

	// -----------------------------------
	// Dialogs
	// -----------------------------------
	static NiftyOpenDialog m_openDialog = null;
	static NiftySaveDialog m_saveDialog = null;
	static NiftyOpenDialogGradient m_openDialogForGradient = null;
	static Element m_aboutDialog = null;
	static Element m_inputOptionsDialog = null;
	static Element m_licenseDialog = null;
	static Element m_userManualDialog = null;
	static Element m_darkeningPanelForDialog = null;

	// -----------------------------------
	// Input settings controls
	// -----------------------------------
	de.lessvoid.nifty.controls.DropDown m_inputSettingsInputMethodTypeDropdown = null;
	de.lessvoid.nifty.controls.DropDown m_inputSettingsMoveTypeDropdown = null;
	Element m_inputSettingsInputSensitivitySlider = null;
	Element m_inputSettingsLeapMotionSlider = null;
	float m_input_LeapMotionSensitivity_LastSliderValue = 0.5f; // temp values, in case Cancel is clicked
	float m_input_sensitivity_LastSliderValue = 0.5f;
	de.lessvoid.nifty.controls.CheckBox m_navWidgetWASDCheckbox = null;
	de.lessvoid.nifty.controls.CheckBox m_navWidgetARROWSCheckbox = null;

	// -----------------------------------
	// Init
	// -----------------------------------

	// This is called when the screen is created
	private void init()
	{
		m_settingsSideMenu = new NiftySettingsSideMenu();

		// InitSlider("sl1", -2.0f, 2.0f, 1.0f, 0.1f, "%.2f");
		// InitSlider("sl2", 0.0f, 10.0f, 1.0f, 0.1f, "%.0f");

		ListBox listBox = m_screen.findNiftyControl("myListBox", ListBox.class);
		for (int i = 0; i < 10; ++i)
			listBox.addItem(Integer.toString(i));

		// ---------------------------------------
		// Create/get dialogs
		// ---------------------------------------
		m_openDialog = new NiftyOpenDialog();
		m_saveDialog = new NiftySaveDialog();
		m_openDialogForGradient = new NiftyOpenDialogGradient();
		
		m_aboutDialog = nifty.getScreen("GScreen0").findElementById("MY_ABOUT_DIALOG");
		m_inputOptionsDialog = nifty.getScreen("GScreen0").findElementById("MY_INPUT_OPTIONS_DIALOG");
		m_licenseDialog = nifty.getScreen("GScreen0").findElementById("MY_LICENSE_DIALOG");

		m_darkeningPanelForDialog = nifty.getScreen("GScreen0").findElementById("DARKENING_PANEL_FOR_DIALOG");

		// ---------------------------------------
		// Loading bar dialog
		// ---------------------------------------
		m_loadingBarDialog = new NiftyLoadingBarDialog(nifty.getScreen("GScreen0").findElementById("MY_LOADING_BAR_DIALOG_CONTROL_ID"));

		// ---------------------------------------
		// Stereo menu
		// ---------------------------------------
		m_stereoMenu = new NiftyStereoMenu(nifty.getScreen("GScreen0").findElementById("MY_STEREO_OPTIONS_DIALOG"));

		// ---------------------------------------
		// Resolution menu
		// ---------------------------------------
		m_resolutionMenu = new NiftyResolutionMenu(nifty.getScreen("GScreen0").findElementById("MY_RESOLUTUION_OPTIONS_DIALOG"));

		// ---------------------------------------
		// Nav widgets
		// ---------------------------------------
		m_navWidgetWASD = new NavigationWidget(nifty.getScreen("GScreen0").findElementById("NAVIGATION_WIDGET_WASD"));
		m_navWidgetUDLR = new NavigationWidget(nifty.getScreen("GScreen0").findElementById("NAVIGATION_WIDGET_UDLR"));

		// place them to the right edge of the screen
		int widgetWidthInPixels = m_navWidgetWASD.m_navigationWidget.getConstraintWidth().getValueAsInt(1.0f);
		int widgetXPos = VeinsWindow.currentDisplayMode.getWidth() - widgetWidthInPixels - 20;

		m_navWidgetWASD.m_navigationWidget.setConstraintX(new SizeValue(widgetXPos, SizeValueType.Pixel));
		m_navWidgetUDLR.m_navigationWidget.setConstraintX(new SizeValue(widgetXPos, SizeValueType.Pixel));

		// must call this to actualy apply the new position
		m_navWidgetWASD.m_navigationWidget.getControl(de.lessvoid.nifty.controls.window.WindowControl.class).dragStop();
		m_navWidgetUDLR.m_navigationWidget.getControl(de.lessvoid.nifty.controls.window.WindowControl.class).dragStop();

		// ---------------------------------------
		// Top menu bars
		// ---------------------------------------
		m_panel_topMenu_DropDownMenus = new Element[3];
		m_panel_topMenu_DropDownMenus[TOPMENU_DROPDOWN_FILE] = nifty.getScreen("GScreen0").findElementById("TOP_MENU_FILE_DROP_DOWN_PANEL");
		m_panel_topMenu_DropDownMenus[TOPMENU_DROPDOWN_OPTIONS] = nifty.getScreen("GScreen0").findElementById("TOP_MENU_OPTIONS_DROP_DOWN_PANEL");
		m_panel_topMenu_DropDownMenus[TOPMENU_DROPDOWN_HELP] = nifty.getScreen("GScreen0").findElementById("TOP_MENU_HELP_DROP_DOWN_PANEL");

		// ---------------------------------------
		// User manual HTML
		// ---------------------------------------
		m_htmlGenerator = new NiftyHtmlGenerator(nifty);
		m_htmlGenerator.setDefaultFont("fonts/aurulent-sans-16.fnt");
		m_htmlGenerator.setDefaultBoldFont("fonts/aurulent-sans-16-bold.fnt");

		m_userManualDialog = nifty.getScreen("GScreen0").findElementById("MY_USER_MANUAL_DIALOG");
		WindowControl userManualControl = m_userManualDialog.getAttachedInputControl().getControl(WindowControl.class);

		try
		{
			Element htmlElementLeft = userManualControl.getElement().findElementById("HTML_LEFT");
			Element htmlElementRight = userManualControl.getElement().findElementById("HTML_RIGHT");

			m_htmlGenerator.generate(readHTMLFile("/html/test-22.html"), m_screen, htmlElementLeft);
			m_htmlGenerator.generate(readHTMLFile("/html/bla.html"), m_screen, htmlElementRight);
		}
		catch (Exception e)
		{
			System.out.println("Error HTML: " + e.getMessage() + ", " + e.toString());
			e.printStackTrace();
		}

		// ---------------------------------------
		// Input menu
		// ---------------------------------------
		m_inputSettingsInputMethodTypeDropdown = m_inputOptionsDialog.findElementById("INPUT_METHOD_DROPDOWN").getAttachedInputControl().getControl(DropDownControl.class);
		m_inputSettingsInputSensitivitySlider = m_inputOptionsDialog.findElementById("INPUT_inputSens");
		m_inputSettingsLeapMotionSlider = m_inputOptionsDialog.findElementById("INPUT_leapSens");
		m_inputSettingsMoveTypeDropdown = m_inputOptionsDialog.findElementById("MOVE_TYPE_METHOD_DROPDOWN").getAttachedInputControl().getControl(DropDownControl.class);

		m_input_sensitivity_LastSliderValue = VeinsWindow.settings.sensitivity;
		m_input_LeapMotionSensitivity_LastSliderValue = VeinsWindow.settings.leapSensitivity;
		InitSlider(m_inputSettingsInputSensitivitySlider, 0.0f, 100.0f, m_input_sensitivity_LastSliderValue, 0.1f, "Input Sensitivity", "%.1f");
		InitSlider(m_inputSettingsLeapMotionSlider, 0.0f, 100.0f, m_input_LeapMotionSensitivity_LastSliderValue, 0.1f, "Input Sensitivity", "%.1f");

		m_inputSettingsMoveTypeDropdown.addItem(INPUT_SETTINGS_MOVE_CAMERA);
		m_inputSettingsMoveTypeDropdown.addItem(INPUT_SETTINGS_MOVE_MODEL);
		m_inputSettingsMoveTypeDropdown.selectItemByIndex(VeinsWindow.settings.useModelMoveMode ? 1 : 0);

		m_inputSettingsInputMethodTypeDropdown.addItem(INPUT_SETTINGS_INPUT_TYPE_NORMAL);
		m_inputSettingsInputMethodTypeDropdown.addItem(INPUT_SETTINGS_INPUT_TYPE_3DMOUSE);
		m_inputSettingsInputMethodTypeDropdown.addItem(INPUT_SETTINGS_INPUT_TYPE_LEAPMOTION);
		m_inputSettingsInputMethodTypeDropdown.selectItemByIndex(0);

		// checkbox label
		m_navWidgetWASDCheckbox = m_inputOptionsDialog.findElementById("checkbox_NavWASD").getAttachedInputControl().getControl(CheckboxControl.class);
		m_navWidgetWASDCheckbox.getElement().findElementById("checkboxLabel").getAttachedInputControl().getControl(LabelControl.class).setText("Show rotation widget");
		m_navWidgetWASDCheckbox.setChecked(true);

		m_navWidgetARROWSCheckbox = m_inputOptionsDialog.findElementById("checkbox_NavARROWS").getAttachedInputControl().getControl(CheckboxControl.class);
		m_navWidgetARROWSCheckbox.getElement().findElementById("checkboxLabel").getAttachedInputControl().getControl(LabelControl.class).setText("Show translation widget");
		m_navWidgetARROWSCheckbox.setChecked(true);

		// ---------------------------------------
		// Prepare gui for show
		// ---------------------------------------
		prepareForSomeMenuOpen();

	}

	private static String readHTMLFile(final String filename) throws IOException
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(VeinsWindow.class.getResourceAsStream(filename)));
		// InputStreamReader reader = new InputStreamReader(new FileInputStream(filename), "ISO-8859-1");
		StringBuffer result = new StringBuffer();
		char[] buffer = new char[1024];
		int read = -1;
		while ((read = reader.read(buffer)) > 0)
		{
			result.append(buffer, 0, read);
		}
		return result.toString();
	}

	// -----------------------------------
	// Top panel dragging
	// -----------------------------------

	int m_dragPosStartX = -1;
	int m_dragPosStartY = -1;
	public static boolean m_dragWindow = false;

	public void onClickMainWindowPanel()
	{
		m_dragWindow = true;
	}

	public void onDragMainWindowPanel()
	{
		if (Mouse.isButtonDown(0) == false)
		{
			m_dragPosStartX = -1;
			m_dragPosStartY = -1;
			m_dragWindow = false;
		}

		if (m_dragWindow)
		{
			int mx = Mouse.getX();
			int my = Mouse.getY();

			if (m_dragPosStartX == -1)
				m_dragPosStartX = mx;

			if (m_dragPosStartY == -1)
				m_dragPosStartY = my;

			int dx = Mouse.getX() - m_dragPosStartX;
			int dy = Mouse.getY() - m_dragPosStartY;

			int newPosX = VeinsWindow.frame.getLocation().x + dx;
			int newPosY = VeinsWindow.frame.getLocation().y - dy;

			// prevent window going out of screen
			if (newPosX < 0)
				newPosX = 0;

			if (newPosY < 0)
				newPosY = 0;

			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

			if ((newPosX + VeinsWindow.currentDisplayMode.getWidth()) > screenSize.width)
				newPosX = screenSize.width - VeinsWindow.currentDisplayMode.getWidth();

			if ((newPosY + VeinsWindow.currentDisplayMode.getHeight()) > screenSize.height)
				newPosY = screenSize.height - VeinsWindow.currentDisplayMode.getHeight();

			VeinsWindow.frame.setLocation(newPosX, newPosY);

			// prepare for next frame
			m_dragPosStartX = mx - dx;
			m_dragPosStartY = my - dy;
		}
	}

	// -----------------------------------
	// Mouse common
	// -----------------------------------

	// Detects mouse down click (after it was up), so clicks
	public void onMouseLeftDownClicked()
	{

		// Hide all top menu drop down menus if mouse clicked on none of them
		if (isMouseOnAnyDropDownMenu() == false)
			closeAllTopMenuDropDownMenus();

		m_settingsSideMenu.OnMouseClick(); // closes it if mouse is clicked outside
	}

	// ----------------------------------------------------
	// Common
	// ----------------------------------------------------

	public void OnEscapeKeyPressed()
	{
		m_openDialog.OnEscapeKeyPressed();
		On_SaveDialog_Close("");
		onButton_SettingsSideMenu_Close("");
		On_InputOptionsDialog_Close();
		On_ResolutionDialog_Close();
		On_AboutDialog_Close();
		On_UserManualDialog_Close();
		On_LicenseDialog_Close();
		On_StereoDialog_Close();

	}

	// is called when some menu will open (so you can close others etc..)
	static void prepareForSomeMenuOpen()
	{
		closeAllTopMenuDropDownMenus();
		m_settingsSideMenu.CloseMenu();
	}

	// Checks if mouse is on this element or any of its children recursive search through all children also
	public static boolean IsMouseInsideOfElementAndAnyOfItsChildren(Element e)
	{
		NiftyMouse mouse = VeinsWindow.nifty.getNiftyMouse();
		boolean isInside = e.isMouseInsideElement(mouse.getX(), mouse.getY());

		for (int i = 0; i < e.getChildrenCount(); ++i)
			isInside = isInside || IsMouseInsideOfElementAndAnyOfItsChildren(e.getChildren().get(i));

		return isInside;
	}

	// Searches if this element has the given parent, null if not
	public static Element FindParentById(Element e, String parentId)
	{
		System.out.println("FindParentById: not tested");
		Element p = e.getParent();
		if (p != null)
		{
			if (p.getId().compareTo(parentId) == 0)
				return p;
			else
				return FindParentById(p, parentId);
		}
		else
			return null;
	}

	// ----------------------------------------------------
	// On resize: fix widgets that need to be handled on resize in here
	// ----------------------------------------------------
	public void OnResize(DisplayMode displayMode)
	{
		m_openDialog.ResetPosition(); // this one definitely needs this, to be safe for others also
		m_saveDialog.ResetPosition();
		m_resolutionMenu.ResetPosition();
		m_stereoMenu.ResetPosition();

		int percX = 10;
		int percY = 10;

		m_aboutDialog.setConstraintX(new SizeValue(percX, SizeValueType.PercentWidth));
		m_aboutDialog.setConstraintY(new SizeValue(percY, SizeValueType.PercentHeight));
		m_licenseDialog.setConstraintX(new SizeValue(percX, SizeValueType.PercentWidth));
		m_licenseDialog.setConstraintY(new SizeValue(percY, SizeValueType.PercentHeight));

		// reposition navigation widgets
		int navWidgetWithd = m_navWidgetUDLR.m_navigationWidget.getConstraintWidth().getValueAsInt(1.0f);
		int navWidgetHeight = m_navWidgetUDLR.m_navigationWidget.getConstraintHeight().getValueAsInt(1.0f);
		int newXPos = displayMode.getWidth() - navWidgetWithd - 10;

		m_navWidgetUDLR.m_navigationWidget.setConstraintX(new SizeValue(newXPos, SizeValueType.Pixel));
		m_navWidgetUDLR.m_navigationWidget.setConstraintY(new SizeValue(navWidgetHeight, SizeValueType.Pixel));

		m_navWidgetWASD.m_navigationWidget.setConstraintX(new SizeValue(newXPos, SizeValueType.Pixel));
		m_navWidgetWASD.m_navigationWidget.setConstraintY(new SizeValue(navWidgetHeight * 2 + 50, SizeValueType.Pixel));
	}

	// ----------------------------------------------------
	// Loading bar dialog
	// ----------------------------------------------------

	// Call it manually when needed
	public static void UpdateLoadingBarDialog(String text, float percentProgress)
	{
		setState(GUI_STATE.DIALOG_OPEN);
		m_loadingBarDialog.ShowAndUpdate(text, percentProgress);
	}

	// Call it manually when you're done with showing progress
	public static void On_LoadingDialog_CloseOrCancel()
	{
		setState(GUI_STATE.DEFAULT);
		m_loadingBarDialog.Hide();
	}

	// ----------------------------------------------------
	// Navigation widgets
	// ----------------------------------------------------

	public void onButton_NavigationWidgetWASD_OnMouseOver(Element element, NiftyMouseInputEvent event)
	{
		if (getState() != GUI_STATE.DEFAULT || m_dragWindow)
			return;

		// process widget input and move camera
		NAVIGATION_WIDGET_BUTTON pressedButton = processNavWidgetInput(m_navWidgetWASD, event);

		switch (pressedButton)
		{
		case CENTER_CIRCLE_DOWN:
			VeinsWindow.renderer.getCamera().lookDown();
			break;
		case CENTER_CIRCLE_UP:
			VeinsWindow.renderer.getCamera().lookUp();
			break;
		case CENTER_CIRCLE_LEFT:
			VeinsWindow.renderer.getCamera().lookRight();
			break;
		case CENTER_CIRCLE_RIGHT:
			VeinsWindow.renderer.getCamera().lookLeft();
			break;
		case SIDE_CIRCLE_LEFT:
			VeinsWindow.renderer.getCamera().rotateCounterClockwise();
			break;
		case SIDE_CIRCLE_RIGHT:
			VeinsWindow.renderer.getCamera().rotateClockwise();
			break;
		case CLOSE_CIRCLE:
			m_navWidgetWASDCheckbox.setChecked(false);
			break;
		}
	}

	public void onButton_NavigationWidgetUDLR_OnMouseOver(Element element, NiftyMouseInputEvent event)
	{
		if (getState() != GUI_STATE.DEFAULT || m_dragWindow)
			return;
		
		float moveDeltaFactor = VeinsWindow.deltaTime * (float)VeinsWindow.settings.sensitivity;
		float maxMoveSpeed = 200.0f;
		if (moveDeltaFactor > maxMoveSpeed)
			moveDeltaFactor = maxMoveSpeed;


		// process widget input and move camera
		NAVIGATION_WIDGET_BUTTON pressedButton = processNavWidgetInput(m_navWidgetUDLR, event);
		switch (pressedButton)
		{
		case CENTER_CIRCLE_DOWN:
			VeinsWindow.renderer.getCamera().moveBackwards(moveDeltaFactor);
			break;
		case CENTER_CIRCLE_UP:
			VeinsWindow.renderer.getCamera().moveForward(moveDeltaFactor);
			break;
		case CENTER_CIRCLE_LEFT:
			VeinsWindow.renderer.getCamera().moveLeft(moveDeltaFactor);
			break;
		case CENTER_CIRCLE_RIGHT:
			VeinsWindow.renderer.getCamera().moveRight(moveDeltaFactor);
			break;
		case SIDE_CIRCLE_LEFT:
			VeinsWindow.renderer.getCamera().moveDown(moveDeltaFactor);
			break;
		case SIDE_CIRCLE_RIGHT:
			VeinsWindow.renderer.getCamera().moveUp(moveDeltaFactor);
			break;
		case CLOSE_CIRCLE:
			m_navWidgetARROWSCheckbox.setChecked(false);
			break;
		}
	}

	// returns the hold down button
	public static NAVIGATION_WIDGET_BUTTON processNavWidgetInput(NavigationWidget widget, NiftyMouseInputEvent event)
	{

		// Disable object rotation if click on nav widget
		VeinsWindow.canModelBeRotatedByMouse = false;

		// move widget, must be done outside the buttons if-else thingy
		if (widget.IsWidgetInMoveState())
			widget.Move();

		NAVIGATION_WIDGET_BUTTON pressedButton = GetNavigationWidgetPressedButton(widget.m_navigationWidget);
		NAVIGATION_WIDGET_BUTTON resultButton = NAVIGATION_WIDGET_BUTTON.NONE;

		// handle input based on click/drag
		if (event.isButton0Release()) // button click
		{
			NiftyMouse mouse = VeinsWindow.nifty.getNiftyMouse();

			widget.StopMoving();
			// System.out.println("cll");
			// close if close circled was pressed
			if (pressedButton == NAVIGATION_WIDGET_BUTTON.CLOSE_CIRCLE)
				widget.m_navigationWidget.setVisible(false);
		}
		else if (event.isButton0Down()) // drag
		{
			// start moving widget if clicked on move circle
			if (pressedButton == NAVIGATION_WIDGET_BUTTON.MOVE_WIDGET_CIRCLE)
			{
				// System.out.println("drag");
				widget.StartMoving();
			}
			else
			{
				resultButton = pressedButton;
			}

		}
		else if (event.isButton0Release())
		{
			widget.StopMoving();
		}

		return resultButton;
	}

	// computes which button was pressed
	public static NAVIGATION_WIDGET_BUTTON GetNavigationWidgetPressedButton(Element navWidgetElement)
	{
		float widgetXPos = navWidgetElement.getConstraintX().getValueAsInt(VeinsWindow.currentDisplayMode.getWidth());
		float widgetYPos = navWidgetElement.getConstraintY().getValueAsInt(VeinsWindow.currentDisplayMode.getHeight());

		// System.out.println(widgetXPos + ", " + widgetYPos);

		// the image local coords have origin in top-left corner
		NiftyMouse mouse = VeinsWindow.nifty.getNiftyMouse();
		ImageRenderer imgRenderer = ((ImageRenderer) navWidgetElement.findElementById("IMAGE_ELEMENT").getElementRenderer()[0]);
		float mouseLocalX = (float) (mouse.getX() - widgetXPos);
		float mouseLocalY = (float) (mouse.getY() - widgetYPos);
		float imageWidth = (float) imgRenderer.getImage().getWidth();
		float imageHeight = (float) imgRenderer.getImage().getHeight();
		float imageScaleFactor = imageWidth / 110.0f; // how much larger this image is compared to the 720p version

		// these are computed from 720p and scaled up based on image size, so no need for different values if gui image changes size.
		float centerCircleRadius = 39.0f * imageScaleFactor;
		float sideCircleRadius = 27.0f * imageScaleFactor;
		float diagonalCircleRadius = 15.0f * imageScaleFactor;

		float centerCircleDiagonalWidth = (float) Math.sqrt(centerCircleRadius * centerCircleRadius * 0.5f);

		// in coords relative to top-left corner
		float centerCircleXPos = imageWidth * 0.5f;
		float centerCircleYPos = imageHeight * 0.5f;

		float sideCircleLeftXPos = imageWidth * 0.5f - centerCircleRadius;
		float sideCircleLeftYPos = imageHeight * 0.5f;

		float sideCircleRightXPos = imageWidth * 0.5f + centerCircleRadius;
		float sideCircleRightYPos = imageHeight * 0.5f;

		float closeCircleXPos = imageWidth * 0.5f + centerCircleDiagonalWidth;
		float closeCircleYPos = imageHeight * 0.5f - centerCircleDiagonalWidth;

		float moveWidgetButtonXPos = imageWidth * 0.5f + centerCircleDiagonalWidth;
		float moveWidgetButtonYPos = imageHeight * 0.5f + centerCircleDiagonalWidth;

		// compute clicks by priority, to prevent clicking on circles below other circles:
		// the center circle first, then side circles, then diagonal circles

		// center circle
		{
			float dx = mouseLocalX - centerCircleXPos;
			float dy = mouseLocalY - centerCircleYPos;
			if ((dx * dx + dy * dy) <= centerCircleRadius * centerCircleRadius)
			{
				float angleInDegrees = (float) Math.atan2(-dy, dx) * 180.0f / (float) Math.PI;

				if (angleInDegrees <= 45.0f && angleInDegrees >= -45.0f)
				{
					// System.out.println("inner circle: right");
					return NAVIGATION_WIDGET_BUTTON.CENTER_CIRCLE_RIGHT; // prevent click through to circles below
				}
				else if (angleInDegrees >= 45.0f && angleInDegrees <= 135.0f)
				{
					// System.out.println("inner circle: up");
					return NAVIGATION_WIDGET_BUTTON.CENTER_CIRCLE_UP; // prevent click through to circles below
				}

				else if (angleInDegrees <= -45.0f && angleInDegrees >= -135.0f)
				{
					// System.out.println("inner circle: down");
					return NAVIGATION_WIDGET_BUTTON.CENTER_CIRCLE_DOWN; // prevent click through to circles below
				}
				else
				{
					// System.out.println("inner circle: left");
					return NAVIGATION_WIDGET_BUTTON.CENTER_CIRCLE_LEFT; // prevent click through to circles below
				}
			}
		}

		// left side circle
		{
			float dx = mouseLocalX - sideCircleLeftXPos;
			float dy = mouseLocalY - sideCircleLeftYPos;
			if ((dx * dx + dy * dy) <= sideCircleRadius * sideCircleRadius)
			{
				// System.out.println("side circle left");
				return NAVIGATION_WIDGET_BUTTON.SIDE_CIRCLE_LEFT; // prevent click through to circles below
			}
		}

		// right side circle
		{
			float dx = mouseLocalX - sideCircleRightXPos;
			float dy = mouseLocalY - sideCircleRightYPos;
			if ((dx * dx + dy * dy) <= sideCircleRadius * sideCircleRadius)
			{
				// System.out.println("side circle right");
				return NAVIGATION_WIDGET_BUTTON.SIDE_CIRCLE_RIGHT; // prevent click through to circles below
			}
		}

		// close circle
		{
			float dx = mouseLocalX - closeCircleXPos;
			float dy = mouseLocalY - closeCircleYPos;
			if ((dx * dx + dy * dy) <= diagonalCircleRadius * diagonalCircleRadius)
			{
				// System.out.println("close circle");
				return NAVIGATION_WIDGET_BUTTON.CLOSE_CIRCLE; // prevent click through to circles below
			}
		}

		// move circle
		{
			float dx = mouseLocalX - moveWidgetButtonXPos;
			float dy = mouseLocalY - moveWidgetButtonYPos;
			if ((dx * dx + dy * dy) <= diagonalCircleRadius * diagonalCircleRadius)
			{
				// System.out.println("move circle");
				return NAVIGATION_WIDGET_BUTTON.MOVE_WIDGET_CIRCLE; // prevent click through to circles below
			}
		}

		return NAVIGATION_WIDGET_BUTTON.NONE;
	}

	// ----------------------------------------------------
	// Minimize,maximize,close buttons
	// ----------------------------------------------------
	public void onButton_TopMenu_Minimize(String a)
	{
		if (getState() != GUI_STATE.DEFAULT)
			return;

		prepareForSomeMenuOpen();
		VeinsWindow.frame.setState(Frame.ICONIFIED);
	}

	public void onButton_TopMenu_Maximize(String a)
	{
		if (getState() != GUI_STATE.DEFAULT)
			return;

		prepareForSomeMenuOpen();

		// get display mode with largest height

		// maximize it or make it smaller
		if (VeinsWindow.IsMaximized() == false)
		{
			VeinsWindow.veinsWindow.ResizeWindow(VeinsWindow.GetLargestDisplayMode(), true);
		}
		else
		{
			// get a half smaller resolution if clicked on a maxmize button when maximize
			// and no last resolution is saved
			DisplayMode dm = VeinsWindow.m_lastWindowedResoltuon;
			DisplayMode largestDM = VeinsWindow.GetLargestDisplayMode();

			// if no display mode to use or if the windowed display mode is actualy same as fullscreen size:
			// find a smaller windowed mode
			if (dm == null || (dm.getWidth() == largestDM.getWidth() && dm.getHeight() == largestDM.getHeight()))
			{
				dm = VeinsWindow.GetLargestDisplayMode();

				try
				{
					DisplayMode displayModes[] = Display.getAvailableDisplayModes();
					for (int i = 0; i < displayModes.length; ++i)
					{
						if (displayModes[i].getWidth() / 2 < dm.getWidth())
						{
							dm = displayModes[i];
							break;
						}
					}

				}
				catch (LWJGLException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
					dm = VeinsWindow.currentDisplayMode;
				}
			}

			VeinsWindow.veinsWindow.ResizeWindow(dm, false);
		}
	}

	public void onButton_TopMenu_Close(String a)
	{
		if (getState() != GUI_STATE.DEFAULT)
			return;

		prepareForSomeMenuOpen();
		VeinsWindow.veinsWindow.exitProgram(0);

	}

	// ----------------------------------------------------
	// Top menu bar & its drop-down menu: File,Options,...
	// ----------------------------------------------------
	public void onButton_TopMenu_File()
	{
		if (getState() != GUI_STATE.DEFAULT)
			return;

		prepareForSomeMenuOpen();
		m_panel_topMenu_DropDownMenus[TOPMENU_DROPDOWN_FILE].setVisible(true);
	}

	public void onHover_TopMenu_File()
	{
		if (getState() != GUI_STATE.DEFAULT)
			return;

		tryOpeningDropDownOnHover(TOPMENU_DROPDOWN_FILE);
	}

	public void onButton_TopMenu_Options()
	{
		if (getState() != GUI_STATE.DEFAULT)
			return;

		prepareForSomeMenuOpen();
		m_panel_topMenu_DropDownMenus[TOPMENU_DROPDOWN_OPTIONS].setVisible(true);
	}

	public void onHover_TopMenu_Options()
	{
		if (getState() != GUI_STATE.DEFAULT)
			return;

		tryOpeningDropDownOnHover(TOPMENU_DROPDOWN_OPTIONS);
	}

	public void onButton_TopMenu_Help()
	{
		if (getState() != GUI_STATE.DEFAULT)
			return;

		prepareForSomeMenuOpen();
		m_panel_topMenu_DropDownMenus[TOPMENU_DROPDOWN_HELP].setVisible(true);
	}

	public void onHover_TopMenu_Help()
	{
		if (getState() != GUI_STATE.DEFAULT)
			return;

		tryOpeningDropDownOnHover(TOPMENU_DROPDOWN_HELP);
	}

	boolean isAnyTopMenuDropDownOpen()
	{
		boolean isVisible = false;
		for (int i = 0; i < m_panel_topMenu_DropDownMenus.length; ++i)
			isVisible = isVisible || m_panel_topMenu_DropDownMenus[i].isVisible();

		return isVisible;
	}

	static void closeAllTopMenuDropDownMenus()
	{
		for (int i = 0; i < m_panel_topMenu_DropDownMenus.length; ++i)
			m_panel_topMenu_DropDownMenus[i].setVisible(false);
	}

	void tryOpeningDropDownOnHover(int TOPMENU_DROPDOWN)
	{
		// if this dropdown is closed and any other drop down is active while
		// we hover on this closed dropdown: close other & show this
		Element currentDropDown = m_panel_topMenu_DropDownMenus[TOPMENU_DROPDOWN];
		if (currentDropDown.isVisible() == false && isAnyTopMenuDropDownOpen())
		{
			closeAllTopMenuDropDownMenus();
			currentDropDown.setVisible(true);
		}
	}

	boolean isMouseOnAnyDropDownMenu()
	{
		boolean isInside = false;
		for (int i = 0; i < m_panel_topMenu_DropDownMenus.length; ++i)
			isInside = isInside || IsMouseInsideOfElementAndAnyOfItsChildren(m_panel_topMenu_DropDownMenus[i]);

		return isInside;
	}

	// ----------------------------------------------------
	// Settings side menu
	// ----------------------------------------------------
	public void onButton_SettingsSideMenu_Open(String a)
	{
		if (getState() != GUI_STATE.DEFAULT)
			return;

		prepareForSomeMenuOpen();
		m_settingsSideMenu.OpenMenu();
	}

	public void onButton_SettingsSideMenu_Close(String a)
	{
		if (getState() != GUI_STATE.DEFAULT)
			return;

		m_settingsSideMenu.CloseMenu();
		prepareForSomeMenuOpen();
	}

	public void onEffectEnd_SettingsSideMenuClose()
	{
		m_settingsSideMenu.OnMenuCloseAnimationFinished();
	}

	@NiftyEventSubscriber(id = "WIREFRAME_CHECKBOX_ID")
	public void OnSettingsSideMenuWireframeCheckboxChanged(String id, CheckBoxStateChangedEvent event)
	{
		VeinsWindow.wire = event.isChecked();
	}

	public void onButton_ObjIncreaseSubdivLevel()
	{
		VeinsWindow.increaseSubdivLevel = true;
	}

	public void onButton_ObjDecreaseSubdivLevel()
	{
		VeinsWindow.decreaseSubdivLevel = true;
	}

	@NiftyEventSubscriber(id = "ENABLE_SSAO_CHECKBOX")
	public void OnSettingsSideMenuSSAOCheckbox(String id, CheckBoxStateChangedEvent event)
	{
		VolumeRaycast.m_enableSSAO = event.isChecked();
	}

	public void onButton_SettingsVolume_StartGradEditor()
	{
		try
		{
			Process ps;
			ps = Runtime.getRuntime().exec(new String[]
			{ "java", "-jar", "GradientConstructor.jar" });
		}
		catch (Exception e)
		{
			System.out.println("Gradient editor error: " + e.toString());
			e.printStackTrace();
		}

	}

	public void onButton_SettingsVolume_ReloadGradFile()
	{
		if (VeinsWindow.renderer.getVeinsModel() != null)
		{
			VeinsWindow.renderer.getVeinsModel().reloadVolumeGradient(VeinsWindow.defaultGradientFile);
		}
	}

	@NiftyEventSubscriber(id = "ENABLE_DOF_CHECKBOX")
	public void OnSettingsSideMenuDOFCheckbox(String id, CheckBoxStateChangedEvent event)
	{
		VolumeRaycast.m_enableDOF = event.isChecked();
	}

	// render methods
	@NiftyEventSubscriber(id = "VOLUME_RENDERMETHOD_DROPDOWN")
	public void OnVolumeRendererMethodTypeDropdown(String id, DropDownSelectionChangedEvent event)
	{
		if (event.getSelection() != null)
		{
			VeinsWindow.settings.volumeRenderMethod = event.getSelectionItemIndex();

			String sel = (String) event.getSelection();
			if (sel.compareTo(NiftySettingsSideMenu.VOLUME_RENDER_METHOD_ISO) == 0)
			{
				VolumeRaycast.SetRenderMethod(VolumeRaycast.RenderMethod.ISO);
			}
			else if (sel.compareTo(NiftySettingsSideMenu.VOLUME_RENDER_METHOD_ALPHA) == 0)
			{
				VolumeRaycast.SetRenderMethod(VolumeRaycast.RenderMethod.ALPHA);
			}
			else if (sel.compareTo(NiftySettingsSideMenu.VOLUME_RENDER_METHOD_MAXMIMUM_PROJECTIOn) == 0)
			{
				VolumeRaycast.SetRenderMethod(VolumeRaycast.RenderMethod.MAX_PROJECTION);
			}
			else
			{
				System.out.println("Error: invalid volume render method: " + sel);
			}
		}
	}
	
	public void onButton_SettingsVolume_OpenGradFile()
	{

		setState(GUI_STATE.DIALOG_OPEN);
		m_openDialogForGradient.OnOpenDialog();
	}

	// ----------------------------------------------------
	// Open dialog
	// ----------------------------------------------------
	public void onButton_TopMenu_File_Open()
	{
		setState(GUI_STATE.DIALOG_OPEN);
		m_openDialog.OnOpenDialog();
	}

	public void On_OpenDialog_Close(String a)
	{
		if (getState() == GUI_STATE.DIALOG_SUBDIALOG_OPEN)
			return;

		setState(GUI_STATE.DEFAULT);
		m_openDialog.OnCloseDialog();
	}

	public void onButton_OpenDialog_Cancel()
	{
		On_OpenDialog_Close("");
	}

	public void onButton_OpenDialog_Open()
	{
		System.out.println("onButton_OpenDialog_Open");
		SelectedFile file = m_openDialog.m_folderBrowser.TryOpeningSelectedFile();

		On_OpenDialog_Close("");

		UpdateLoadingBarDialog("Loading model...", 0.0f);
		VeinsWindow.veinsWindow.RenderSingleFrameWithoutModel();

		// Marching cubes, MPUI or volume render (obj is used if the file was already loaded for MC or MPUI)
		if (file != null && file.extensionOnly.compareTo("mhd") == 0)
		{
			String selectedMethod = m_openDialog.m_methodTypeDropdown.getSelection();

			// Convert string model type to enum
			ModelType modelType = ModelType.MARCHING_CUBES;
			if (selectedMethod == null || selectedMethod.compareTo(NiftyOpenDialog.METHOD_TYPE_MARCHING_CUBES) == 0)
			{
				modelType = ModelType.MARCHING_CUBES;
			}
			else if (selectedMethod.compareTo(NiftyOpenDialog.METHOD_TYPE_MPUI) == 0)
			{
				modelType = ModelType.MPUI;
			}
			else if (selectedMethod.compareTo(NiftyOpenDialog.METHOD_TYPE_VOLUME_RENDER) == 0)
			{
				modelType = ModelType.VOLUME_RENDER;
			}
			else
			{
				System.out.println("WARNING: onButton_OpenDialog_Open: Invalid model type");
				return;
			}

			// Try loading file
			try
			{
				// try fast methods (gpu marching cubes)
				VeinsWindow.renderer.loadModelRaw(file.fullFilePathAndName, modelType, false);
			}
			catch (Exception e)
			{
				// try fallback methods (cpu marching cubes, slower)
				System.out.println("WARNING: onButton_OpenDialog_Open: trying to load the model in safe mode, GPU mode probably failed");
				try
				{
					VeinsWindow.renderer.loadModelRaw(file.fullFilePathAndName, modelType, true);
				}
				catch (LWJGLException e1)
				{
					System.out.println("WARNING: onButton_OpenDialog_Open: can't load model, problem with the file you try to load?");
				}
			}
		}

		// OBJ load
		if (file != null && file.extensionOnly.compareTo("obj") == 0)
		{
			VeinsWindow.renderer.loadModelObj(file.fullFilePathAndName);
		}

		VeinsWindow.veinsWindow.RenderSingleFrameWithoutModel();

		UpdateLoadingBarDialog("Loading model...", 100.0f);
		On_LoadingDialog_CloseOrCancel();

		VeinsWindow.renderer.resetCameraPositionAndOrientation();
	}

	// Options subdialog dialog callbacks
	public void onButton_OpenDialog_OptionsDialog_Open()
	{
		if (getState() == GUI_STATE.DIALOG_SUBDIALOG_OPEN)
			return;

		setState(GUI_STATE.DIALOG_SUBDIALOG_OPEN);
		m_openDialog.On_SettingsDialog_Open();
	}

	public void On_OpenDialog_OptionsDialog_Close()
	{
		setState(GUI_STATE.DIALOG_OPEN);
		m_openDialog.On_SettingsDialog_CloseOrCancel();
	}

	public void On_OpenDialog_OptionsDialog_OK()
	{
		setState(GUI_STATE.DIALOG_OPEN);
		m_openDialog.On_SettingsDialog_OK();
	}

	// ----------------------------------------------------
	// Save dialog
	// ----------------------------------------------------
	public void onButton_TopMenu_File_SaveAs()
	{
		setState(GUI_STATE.DIALOG_OPEN);
		m_saveDialog.OnOpenDialog();
	}

	public void On_SaveDialog_Close(String a)
	{
		if (getState() == GUI_STATE.DIALOG_SUBDIALOG_OPEN)
			return;

		setState(GUI_STATE.DEFAULT);
		m_saveDialog.OnCloseDialog();
	}

	public void onButton_SaveDialog_Cancel()
	{
		On_SaveDialog_Close("");
	}

	public void onButton_SaveDialog_Save()
	{
		// System.out.println("onButton_SaveDialog_Save");
		SelectedFile file = m_saveDialog.m_folderBrowser.TryGettingSelectedFile();
		if (file == null)
		{
			System.out.println("You must specify the file & path to save to");
			return;
		}

		// Can only save meshes to obj, not volume
		if (VeinsWindow.renderer.veinsModel != null && VeinsWindow.renderer.veinsModel instanceof VeinsModelMesh)
		{
			VeinsModelMesh model = (VeinsModelMesh) VeinsWindow.renderer.veinsModel;
			for (int i = 0; i < model.meshes.size(); ++i)
			{
				Mesh mesh = model.meshes.get(i);
				String fullPath = file.fullFilePathAndName;

				// if more than one mesh add the mesh index before the .obj
				if (model.meshes.size() > 1)
				{
					int chIdx = fullPath.indexOf(".obj");
					if (chIdx > 0)
						fullPath = insert(fullPath, Integer.toString(i), chIdx);
				}

				mesh.SaveToFile(fullPath);
			}
		}
		else
		{
			System.out.println("Warning: Cannot save a volume model. You can only save models created with Marching Cubes or MPUI to .obj files");
		}

		On_SaveDialog_Close("");

		UpdateLoadingBarDialog("Saving model...", 10.0f);

		VeinsWindow.veinsWindow.RenderSingleFrameWithoutModel();

		UpdateLoadingBarDialog("Saving model...", 100.0f);
		On_LoadingDialog_CloseOrCancel();
	}

	@NiftyEventSubscriber(id = "SAVE_DIALOG_FILES_LIST_LISTBOX")
	public void OnTextFieldTextChanged(String id, ListBoxSelectionChangedEvent event)
	{
		m_saveDialog.m_folderBrowser.OnListboxSelectionChanged(id, event);
	}

	public static String insert(String bag, String marble, int index)
	{
		String bagBegin = bag.substring(0, index);
		String bagEnd = bag.substring(index);
		return bagBegin + marble + bagEnd;
	}

	// ----------------------------------------------------
	// Open gradient file dialog
	// ----------------------------------------------------

	public void onButton_OpenDialogForGradient_Close(String a)
	{
		setState(GUI_STATE.DEFAULT);
		m_openDialogForGradient.OnCloseDialog();
	}

	public void onButton_OpenDialogForGradient_Cancel()
	{
		onButton_OpenDialogForGradient_Close("");
	}

	public void onButton_OpenDialogForGradient_Open()
	{
		//System.out.println("onButton_OpenDialog_Open");
		SelectedFile file = m_openDialogForGradient.m_folderBrowser.TryOpeningSelectedFile();

		onButton_OpenDialogForGradient_Close("");


		// Marching cubes, MPUI or volume render (obj is used if the file was already loaded for MC or MPUI)
		if (file != null )
		{
			if (VeinsWindow.renderer.getVeinsModel() != null)
			{
				VeinsWindow.renderer.getVeinsModel().reloadVolumeGradient(file.fullFilePathAndName);
			}

		}
	}

	// ----------------------------------------------------
	// About dialog
	// ----------------------------------------------------
	public void onButton_TopMenu_Help_About()
	{
		setState(GUI_STATE.DIALOG_OPEN);
		m_aboutDialog.setVisible(true);
	}

	public void On_AboutDialog_Close()
	{
		setState(GUI_STATE.DEFAULT);
		m_aboutDialog.setVisible(false);
	}

	// ----------------------------------------------------
	// Input options dialog
	// ----------------------------------------------------
	public void onButton_TopMenu_Options_Inputs()
	{
		setState(GUI_STATE.DIALOG_OPEN);
		m_inputOptionsDialog.setVisible(true);
	}

	public void On_InputOptionsDialog_Close()
	{
		setState(GUI_STATE.DEFAULT);
		m_inputOptionsDialog.setVisible(false);
	}

	public void onButton_InputSettingsDialog_Cancel()
	{
		On_InputOptionsDialog_Close();
	}

	public void onButton_InputSettingsDialog_OK()
	{
		VeinsWindow.settings.sensitivity = (int) m_input_sensitivity_LastSliderValue;
		VeinsWindow.settings.leapSensitivity = (int) m_input_LeapMotionSensitivity_LastSliderValue;

		if (m_inputSettingsMoveTypeDropdown.getSelection() != null)
		{
			String sel = (String) m_inputSettingsMoveTypeDropdown.getSelection();

			boolean newCameraMode = sel.compareTo(INPUT_SETTINGS_MOVE_MODEL) == 0;
			if (newCameraMode != VeinsWindow.settings.useModelMoveMode)
			{
				VeinsWindow.renderer.resetCameraPositionAndOrientation(); // must be done or volume renderer fails
			}
			VeinsWindow.settings.useModelMoveMode = newCameraMode;
		}

		On_InputOptionsDialog_Close();
	}

	@NiftyEventSubscriber(id = "checkbox_NavWASD")
	public void OnCheckboxInputNavWidgetWASD(String id, CheckBoxStateChangedEvent event)
	{
		m_navWidgetWASD.m_navigationWidget.setVisible(event.isChecked());
	}

	@NiftyEventSubscriber(id = "checkbox_NavARROWS")
	public void OnCheckboxInputNavWidgetARROWS(String id, CheckBoxStateChangedEvent event)
	{
		m_navWidgetUDLR.m_navigationWidget.setVisible(event.isChecked());

	}

	// ----------------------------------------------------
	// License options dialog
	// ----------------------------------------------------
	public void onButton_TopMenu_Help_License()
	{
		setState(GUI_STATE.DIALOG_OPEN);
		m_licenseDialog.setVisible(true);
	}

	public void On_LicenseDialog_Close()
	{
		setState(GUI_STATE.DEFAULT);
		m_licenseDialog.setVisible(false);
	}

	// ----------------------------------------------------
	// User manual dialog
	// ----------------------------------------------------
	public void onButton_TopMenu_UserManual()
	{
		setState(GUI_STATE.DIALOG_OPEN);
		m_userManualDialog.setVisible(true);
	}

	public void On_UserManualDialog_Close()
	{
		setState(GUI_STATE.DEFAULT);
		m_userManualDialog.setVisible(false);
	}

	// ----------------------------------------------------
	// Resolution options dialog
	// ----------------------------------------------------
	public void onButton_TopMenu_Options_Resolution()
	{
		setState(GUI_STATE.DIALOG_OPEN);
		m_resolutionMenu.SetResolutions(VeinsWindow.displayModes);
		m_resolutionMenu.OnButton_ShowDialog();
	}

	public void On_ResolutionDialog_Close()
	{
		setState(GUI_STATE.DEFAULT);
		m_resolutionMenu.OnButton_CloseOrCancel();
	}

	public void onButton_ResolutionDialog_Cancel()
	{
		On_ResolutionDialog_Close();
	}

	public void onButton_ResolutionDialog_OK()
	{
		m_resolutionMenu.OnButton_OK();
		setState(GUI_STATE.DEFAULT);
	}

	// ----------------------------------------------------
	// Stereo options dialog
	// ----------------------------------------------------
	public void onButton_TopMenu_Options_Stereo()
	{
		setState(GUI_STATE.DIALOG_OPEN);
		m_stereoMenu.OnButton_ShowDialog();
	}

	public void On_StereoDialog_Close()
	{
		setState(GUI_STATE.DEFAULT);
		m_stereoMenu.OnButton_CloseOrCancel();
	}

	public void OnButton_StereoMenu_Cancel()
	{
		On_StereoDialog_Close();
	}

	public void OnButton_StereoMenu_OK()
	{
		m_stereoMenu.OnButton_OK();
		setState(GUI_STATE.DEFAULT);
	}

	// ----------------------------------------------------
	// Other top menu buttons
	// ----------------------------------------------------
	public void onButton_TopMenu_File_Exit()
	{
		onButton_TopMenu_Close("");
	}

	// ----------------------------------------------------
	// Treebox events
	// ----------------------------------------------------
	@NiftyEventSubscriber(id = "tree-box")
	public void OnTreeboxSelectionChanged(String id, ListBoxSelectionChangedEvent<TreeItem<MyTreeFolderItem>> event)
	{
		if (event.getListBox().getElement().getParent().getId().compareTo("SAVE_DIALOG_FOLDER_TREEBOX_PANEL_CONTAINER") == 0)
			m_saveDialog.m_folderBrowser.OnTreeboxSelectionChanged(id, event);	
		else if (event.getListBox().getElement().getParent().getId().compareTo("OPEN_DIALOG_FOLDER_TREEBOX_PANEL_CONTAINER") == 0)
			m_openDialog.m_folderBrowser.OnTreeboxSelectionChanged(id, event);
		else if (event.getListBox().getElement().getParent().getId().compareTo("OPEN_DIALOG_FOR_GRADIENT_FOLDER_TREEBOX_PANEL_CONTAINER") == 0)
			m_openDialogForGradient.m_folderBrowser.OnTreeboxSelectionChanged(id, event);
	}

	// ----------------------------------------------------
	// Drop-down list events
	// ----------------------------------------------------

	@NiftyEventSubscriber(id = "OPEN_DIALOG_FILE_TYPE_DROPDOWN")
	public void OnFileTypeDropdownSelectionChanged(String id, DropDownSelectionChangedEvent<MyFileExtensionItem> event)
	{
		m_openDialog.m_folderBrowser.OnFileTypeSelectionChanged(event);

		m_openDialog.showSpecificControlsForSelectedRenderMethod(event.getSelectionItemIndex());
	}

	@NiftyEventSubscriber(id = "OBJ_SHADER_DROPDOWN")
	public void OnObjShaderTypeDropdownSelectionChanged(String id, DropDownSelectionChangedEvent<MyFileExtensionItem> event)
	{
		if (m_settingsSideMenu.m_objShaderTypeDropdown.getSelection() != null)
		{
			VeinsWindow.renderer.setActiveShaderProgram(m_settingsSideMenu.m_objShaderTypeDropdown.getSelection().shaderId);
		}
	}

	// ----------------------------------------------------
	// Sliders
	// ----------------------------------------------------

	@NiftyEventSubscriber(id = "SLIDER_CONTROL")
	public void onSliderChangedEvent(final String id, final SliderChangedEvent event)
	{
		Element modifiedSlider = FindMySliderParent(event.getSlider().getElement());

		// modifty the value of the changed slider
		updateSliderValueLabel(modifiedSlider, event.getValue());

		// specific callbacks
		if (modifiedSlider.getId().compareTo("SLIDER_MODEL_MESH_THRESHOLD") == 0)
		{
			if (VeinsWindow.renderer.getVeinsModel() != null)
			{
				VeinsWindow.renderer.getVeinsModel().changeThreshold(event.getValue());
				GetSliderFromElement(m_settingsSideMenu.m_sliderMinTriangles).setValue(0.0f);
			}
		}
		if (modifiedSlider.getId().compareTo("SLIDER_MODEL_MESH_MIN_TRIANGLES") == 0)
		{
			if (VeinsWindow.renderer.getVeinsModel() != null)
			{
				VeinsWindow.renderer.getVeinsModel().changeMinTriangles((int) event.getValue());
			}
		}

		// Volume renderer
		if (modifiedSlider.getId().compareTo("ssao_strength") == 0)
		{
			VolumeRaycast.m_ssaoStrength = event.getValue();
		}
		if (modifiedSlider.getId().compareTo("dof_focus") == 0)
		{
			VolumeRaycast.m_dofFocus = event.getValue();
		}
		if (modifiedSlider.getId().compareTo("dof_strength") == 0)
		{
			VolumeRaycast.m_dofStrength = event.getValue();
		}

		// threhold
		if (modifiedSlider.getId().compareTo("volume_iso_threshold") == 0)
		{
			VolumeRaycast.threshold = event.getValue();
		}

		// Input dialog
		if (modifiedSlider.getId().compareTo("INPUT_inputSens") == 0)
		{
			m_input_LeapMotionSensitivity_LastSliderValue = event.getValue();
		}

		if (modifiedSlider.getId().compareTo("INPUT_leapSens") == 0)
		{
			m_input_sensitivity_LastSliderValue = event.getValue();
		}

		// Stereo
		if (modifiedSlider.getId().compareTo("disparitySlider") == 0)
		{
			VeinsWindow.settings.stereoValue = (int) event.getValue();
		}

	}

	// A hacky way to get slider events to work: the slider inside my control makes an event, this function
	// moves up until it detects the first control element, which is the instance of my slider control in some gui xml.
	public static Element FindMySliderParent(Element e)
	{
		Element p = e.getParent();
		if (p != null && p != m_screen.getRootElement())
		{
			if (p.getElementType() instanceof de.lessvoid.nifty.loaderv2.types.ControlType)
			{
				return p;
			}
			else
				return FindMySliderParent(p);
		}
		else
			return null;
	}

	@NiftyEventSubscriber(id = "sl1*")
	public void onSliderChangedEvent2(final String id, final SliderChangedEvent event)
	{
		Element slider = nifty.getScreen("GScreen0").findElementById(id);
		updateSliderValueLabel(slider, event.getValue());
	}

	public de.lessvoid.nifty.controls.Slider GetSliderFromElement(Element e)
	{
		return e.findElementById("SLIDER_CONTROL").getControl(de.lessvoid.nifty.controls.slider.SliderControl.class);
	}

	static void updateSliderValueLabel(Element slider, float value)
	{
		de.lessvoid.nifty.controls.Slider sliderControl = slider.findElementById("SLIDER_CONTROL").getControl(de.lessvoid.nifty.controls.slider.SliderControl.class);
		de.lessvoid.nifty.controls.Label label = slider.findElementById("SLIDER_VALUE_LABEL").getControl(LabelControl.class);
		String printFormat = m_sliderOutputTextFormat.get(slider);
		label.setText(String.format(Locale.US, printFormat, value));
	}

	// Sets all slider values, format example: "%.2f", "%.0f" (how many decimals are printed)
	public static void InitSlider(Element mySliderControl, float min, float max, float initialValue, float step, String sliderLabel, String valueLabelFormat)
	{
		m_sliderOutputTextFormat.put(mySliderControl, valueLabelFormat);

		// slider
		Element niftySliderElement = mySliderControl.findElementById("SLIDER_CONTROL");
		de.lessvoid.nifty.controls.Slider sliderControl = niftySliderElement.getControl(de.lessvoid.nifty.controls.slider.SliderControl.class);
		sliderControl.setup(min, max, initialValue, step, step);

		// label
		Label labelControl = mySliderControl.findElementById("SLIDER_TEXT_LABEL").getAttachedInputControl().getControl(LabelControl.class);
		labelControl.setText(sliderLabel);

		updateSliderValueLabel(mySliderControl, initialValue);
	}

	public static de.lessvoid.nifty.controls.Slider ConvertElementToSlider(Element mySliderControl)
	{
		Element niftySliderElement = mySliderControl.findElementById("SLIDER_CONTROL");
		return niftySliderElement.getControl(de.lessvoid.nifty.controls.slider.SliderControl.class);
	}

	// ----------------------------------------------------
	// State handling
	// ----------------------------------------------------
	public GUI_STATE getState()
	{
		return __m_guiState_doNotAccessThisDirectly;
	}

	static void setState(GUI_STATE state)
	{
		if (state == GUI_STATE.DEFAULT)
		{
			m_darkeningPanelForDialog.setVisible(false);
		}
		else if (state == GUI_STATE.DIALOG_OPEN)
		{
			prepareForSomeMenuOpen();
			m_darkeningPanelForDialog.setVisible(true);
		}
		else if (state == GUI_STATE.DIALOG_SUBDIALOG_OPEN)
		{
			prepareForSomeMenuOpen();
			m_darkeningPanelForDialog.setVisible(true);
		}
		else
		{
			// warning in case a new state is added and you forget to implement it
			System.out.println("NiftyScreenController::setState: invalid GUI_STATE: " + state.toString());
		}

		__m_guiState_doNotAccessThisDirectly = state;
	}

	// ----------------------------------------------------
	// Some implementation helpers
	// ----------------------------------------------------

	public void onBtn(String bla)
	{
		/*
		 * m_popupMenu_MainMenu_File.setConstraintX(new SizeValue(300, SizeValueType.Pixel)); m_popupMenu_MainMenu_File.setConstraintY(new SizeValue(300, SizeValueType.Pixel)); m_popupMenu_MainMenu_File.layoutElements();
		 * 
		 * // System.out.println("BTN***************"); nifty.showPopup(m_screen, m_popupMenu_MainMenu_File.getId(), null);
		 * 
		 * m_popupMenu_MainMenu_File.setConstraintX(new SizeValue(300, SizeValueType.Pixel)); m_popupMenu_MainMenu_File.setConstraintY(new SizeValue(300, SizeValueType.Pixel)); m_popupMenu_MainMenu_File.layoutElements();
		 */

		/*
		 * ListBox lb = m_screen.findNiftyControl("GListBox0",ListBox.class); lb.enable(); lb.addItem("a"); lb.addItem("b"); lb.addItem("c");
		 */

		System.out.println("onBtn");
		// m_settingsSideMenu.OpenMenu();

	}

	@NiftyEventSubscriber(id = "exit")
	public void exit(final String id, final ButtonClickedEvent event)
	{
		nifty.exit();
	}

	@Override
	public void bind(final Nifty nifty, final Screen screen)
	{
		this.nifty = nifty;
		m_screen = screen;

		init();

		System.out.println("BIND***************");
	}

	@Override
	public void onStartScreen()
	{
		System.out.println("START***************");
	}

	@Override
	public void onEndScreen()
	{
		System.out.println("END***************");
	}

	public void gotoScreen(final String screenId)
	{
		nifty.gotoScreen(screenId);
	}
}