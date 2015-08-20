package si.uni_lj.fri.veins3D.gui;

import java.awt.Frame;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Locale;

import javax.swing.JFrame;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

import si.uni_lj.fri.veins3D.gui.NiftyFolderBrowser.MyFileExtensionItem;
import si.uni_lj.fri.veins3D.gui.NiftyFolderBrowser.MyTreeFolderItem;
import si.uni_lj.fri.veins3D.gui.NiftyFolderBrowser.MyTreeFolderItem;
import si.uni_lj.fri.veins3D.gui.NiftyFolderBrowser.SelectedFile;
import si.uni_lj.fri.veins3D.main.VeinsWindow;
import de.lessvoid.nifty.EndNotify;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEvent;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.NiftyMouse;
import de.lessvoid.nifty.controls.ButtonClickedEvent;
import de.lessvoid.nifty.controls.Controller;
import de.lessvoid.nifty.controls.DropDownSelectionChangedEvent;
import de.lessvoid.nifty.controls.Label;
import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.controls.ListBoxSelectionChangedEvent;
import de.lessvoid.nifty.controls.NiftyInputControl;
import de.lessvoid.nifty.controls.Slider;
import de.lessvoid.nifty.controls.SliderChangedEvent;
import de.lessvoid.nifty.controls.TreeItem;
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

	public static Screen m_screen = null;;
	public static String[] m_supportedFileTypes = new String[]
	{ "mhd", "obj" };

	// -----------------------------------
	// GUI state
	// -----------------------------------
	public enum GUI_STATE
	{
		DEFAULT, DIALOG_OPEN, // if this state is enabled then all clicks except for that dialog are disabled, only one dialog can be open at a time?
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
	// Mouse
	// -----------------------------------

	// -----------------------------------
	// Top menu bar buttons
	// -----------------------------------
	static final int TOPMENU_DROPDOWN_FILE = 0;
	static final int TOPMENU_DROPDOWN_OPTIONS = 1;
	static final int TOPMENU_DROPDOWN_HELP = 2;
	Element[] m_panel_topMenu_DropDownMenus;

	// -----------------------------------
	// Side menu
	// -----------------------------------
	NiftySettingsSideMenu m_settingsSideMenu = null;
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
	static Element m_aboutDialog = null;
	static Element m_inputOptionsDialog = null;
	static Element m_licenseDialog = null;

	static Element m_darkeningPanelForDialog = null;

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

		m_aboutDialog = nifty.getScreen("GScreen0").findElementById("MY_ABOUT_DIALOG");
		m_inputOptionsDialog = nifty.getScreen("GScreen0").findElementById("MY_INPUT_OPTIONS_DIALOG");
		m_licenseDialog = nifty.getScreen("GScreen0").findElementById("MY_LICENSE_DIALOG");

		m_darkeningPanelForDialog = nifty.getScreen("GScreen0").findElementById("DARKENING_PANEL_FOR_DIALOG");

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
		// HTML
		// ---------------------------------------
		m_htmlGenerator = new NiftyHtmlGenerator(nifty);
		m_htmlGenerator.setDefaultFont("fonts/aurulent-sans-16.fnt");
		m_htmlGenerator.setDefaultBoldFont("fonts/aurulent-sans-16-bold.fnt");
		
		Element userManualElement = nifty.getScreen("GScreen0").findElementById("MY_USER_MANUAL_DIALOG");
		WindowControl userManualControl = userManualElement.getAttachedInputControl().getControl(WindowControl.class);
		Element htmlElement = userManualControl.getElement().findElementById("BLABLA");
		try
		{
			JUST ONE TEXT IS BUILD BY GENERATOR? MAYBE WRONG VERSION OF NIFTY, TRY THE 1.4 BRANCH
			String htmlFileContent = readHTMLFile("/html/test-22.html");
			m_htmlGenerator.generate(htmlFileContent, m_screen, htmlElement);
		}
		catch (Exception e)
		{
			System.out.println("Error HTML: "+e.getMessage()+", "+e.toString());
			e.printStackTrace();
		}
		
		prepareForSomeMenuOpen();

	}
	
	private static String readHTMLFile(final String filename) throws IOException
{
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				VeinsWindow.class.getResourceAsStream(filename)));
	  //  InputStreamReader reader = new InputStreamReader(new FileInputStream(filename), "ISO-8859-1");
	    StringBuffer result = new StringBuffer();
	    char[] buffer = new char[1024];
	    int read = -1;
	    while ((read = reader.read(buffer)) > 0) {
	      result.append(buffer, 0, read);
	    }
	    return result.toString();
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

	// is called when some menu will open (so you can close others etc..)
	void prepareForSomeMenuOpen()
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
	// Navigation widgets
	// ----------------------------------------------------

	public void onButton_NavigationWidgetWASD_OnMouseOver(Element element, NiftyMouseInputEvent event)
	{
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
		}
	}

	public void onButton_NavigationWidgetUDLR_OnMouseOver(Element element, NiftyMouseInputEvent event)
	{
		// process widget input and move camera
		NAVIGATION_WIDGET_BUTTON pressedButton = processNavWidgetInput(m_navWidgetUDLR, event);
		switch (pressedButton)
		{
		case CENTER_CIRCLE_DOWN:
			VeinsWindow.renderer.getCamera().moveBackwards();
			break;
		case CENTER_CIRCLE_UP:
			VeinsWindow.renderer.getCamera().moveForward();
			break;
		case CENTER_CIRCLE_LEFT:
			VeinsWindow.renderer.getCamera().moveLeft();
			break;
		case CENTER_CIRCLE_RIGHT:
			VeinsWindow.renderer.getCamera().moveRight();
			break;
		case SIDE_CIRCLE_LEFT:
			VeinsWindow.renderer.getCamera().moveDown();
			break;
		case SIDE_CIRCLE_RIGHT:
			VeinsWindow.renderer.getCamera().moveUp();
			break;
		}
	}

	// returns the hold down button
	public static NAVIGATION_WIDGET_BUTTON processNavWidgetInput(NavigationWidget widget, NiftyMouseInputEvent event)
	{

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

			if (dm == null)
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

	void closeAllTopMenuDropDownMenus()
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
		setState(GUI_STATE.DEFAULT);
		m_openDialog.OnCloseDialog();
	}

	public void onButton_OpenDialog_Cancel()
	{
		On_OpenDialog_Close("");
	}

	public void onButton_OpenDialog_Open()
	{
		SelectedFile file = m_openDialog.m_folderBrowser.TryOpeningSelectedFile();

		On_OpenDialog_Close("");

		if (file.extensionOnly.compareTo("mhd") == 0)
		{
			double sigma = 0.5f;
			double threshold = 0.5f;

			// try loading on gpu, then cpu
			try
			{
				VeinsWindow.renderer.loadModelRaw(file.fullFilePathAndName, sigma, threshold);
			}
			catch (LWJGLException e)
			{
				System.out.println("onButton_OpenDialog_Open: exceptions: " + e.getMessage());
				e.printStackTrace();

				// try safe mode
				VeinsWindow.renderer.loadModelRawSafeMode(file.fullFilePathAndName, sigma, threshold);
			}
		}
		else if (file.extensionOnly.compareTo("obj") == 0)
		{

		}
		else
			System.out.println("onButton_OpenDialog_Open: invalid file extensions: " + file.extensionOnly + ", file: " + file.fullFilePathAndName);
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
		m_openDialog.m_folderBrowser.OnTreeboxSelectionChanged(id, event);
	}

	// ----------------------------------------------------
	// Drop-down list events
	// ----------------------------------------------------

	@NiftyEventSubscriber(id = "OPEN_DIALOG_FILE_TYPE_DROPDOWN")
	public void OnFileTypeDropdownSelectionChanged(String id, DropDownSelectionChangedEvent<MyFileExtensionItem> event)
	{
		m_openDialog.m_folderBrowser.OnFileTypeSelectionChanged(event);
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

		/*
		 * if (modifiedSlider.getId().compareTo("sl1") == 0) {
		 * 
		 * } else { java.awt.Toolkit.getDefaultToolkit().beep(); System.out.println("Error: onSliderChangedEvent: invalid slider, implement it"); }
		 */
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

	// ----------------------------------------------------
	// State handling
	// ----------------------------------------------------
	GUI_STATE getState()
	{
		return __m_guiState_doNotAccessThisDirectly;
	}

	void setState(GUI_STATE state)
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