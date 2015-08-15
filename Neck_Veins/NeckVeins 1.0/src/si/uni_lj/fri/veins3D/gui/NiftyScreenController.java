package si.uni_lj.fri.veins3D.gui;

import java.awt.Frame;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Locale;

import javax.swing.JFrame;

import org.lwjgl.input.Mouse;

import si.uni_lj.fri.veins3D.gui.NiftyFolderBrowser.MyTreeItem;
import si.uni_lj.fri.veins3D.main.VeinsWindow;
import de.lessvoid.nifty.EndNotify;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEvent;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.NiftyMouse;
import de.lessvoid.nifty.controls.ButtonClickedEvent;
import de.lessvoid.nifty.controls.Controller;
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
import de.lessvoid.nifty.effects.EffectEventId;
import de.lessvoid.nifty.effects.impl.Move;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.input.NiftyInputEvent;
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
	public static Screen m_screen = null;;

	public static String[] m_supportedFileTypes = new String[]{"mhd","obj"};
	
	// -----------------------------------
	// Top menu bar buttons
	// -----------------------------------
	static final int TOPMENU_DROPDOWN_FILE = 0;
	static final int TOPMENU_DROPDOWN_OPTIONS = 1;
	Element[] m_panel_topMenu_DropDownMenus;

	// -----------------------------------
	// Side menu
	// -----------------------------------
	NiftySettingsSideMenu m_settingsSideMenu = null;
	boolean m_isSettginsSideMenuOpened = false;

	// -----------------------------------
	// Sliders
	// -----------------------------------
	static HashMap<Element, String> m_sliderOutputTextFormat =new HashMap<Element, String>(); // stores how the slider value label is printed for every slider(eg one decimal, int ,...)
	
	// -----------------------------------
	// Open Dialog
	// -----------------------------------
	static NiftyOpenDialog m_openDialog = null;
	// -----------------------------------
		// u
		// -----------------------------------

	
	// This is called when the screen is created
	private void init()
	{
		m_settingsSideMenu = new NiftySettingsSideMenu();
		m_openDialog = new NiftyOpenDialog();
		
		InitSlider("sl1", -2.0f, 2.0f, 1.0f, 0.1f,"%.2f");
		InitSlider("sl2", 0.0f, 10.0f, 1.0f, 0.1f,"%.0f");
		
		 ListBox listBox = m_screen.findNiftyControl("myListBox", ListBox.class);
		  for(int i=0;i<10;++i)
			  listBox.addItem(Integer.toString(i));
		
		// ---------------------------------------
		// Top menu bars
		// ---------------------------------------
		m_panel_topMenu_DropDownMenus = new Element[2];
		m_panel_topMenu_DropDownMenus[TOPMENU_DROPDOWN_FILE] = nifty.getScreen("GScreen0").findElementById("TOP_MENU_FILE_DROP_DOWN_PANEL");
		m_panel_topMenu_DropDownMenus[TOPMENU_DROPDOWN_OPTIONS] = nifty.getScreen("GScreen0").findElementById("TOP_MENU_OPTIONS_DROP_DOWN_PANEL");

		prepareForSomeMenuOpen();
	
	}

	public void update()
	{

	}

	// Detects mouse down click (after it was up)
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
	// Minimize,maximize,close buttons
	// ----------------------------------------------------
	public void onButton_TopMenu_Minimize(String a)
	{
		prepareForSomeMenuOpen();
		VeinsWindow.frame.setState(Frame.ICONIFIED);
	}

	public void onButton_TopMenu_Maximize(String a)
	{

		prepareForSomeMenuOpen();
		VeinsWindow.veinsWindow.ResizeWindow(true);
	}

	public void onButton_TopMenu_Close(String a)
	{
		prepareForSomeMenuOpen();
		VeinsWindow.veinsWindow.exitProgram(0);

	}

	// ----------------------------------------------------
	// Top menu bar & its drop-down menu: File,Options,...
	// ----------------------------------------------------
	public void onButton_TopMenu_File(String a)
	{
		prepareForSomeMenuOpen();
		m_panel_topMenu_DropDownMenus[TOPMENU_DROPDOWN_FILE].setVisible(true);
	}

	public void onHover_TopMenu_File(String a)
	{
		tryOpeningDropDownOnHover(TOPMENU_DROPDOWN_FILE);
	}

	public void onButton_TopMenu_Options(String a)
	{
		prepareForSomeMenuOpen();
		m_panel_topMenu_DropDownMenus[TOPMENU_DROPDOWN_OPTIONS].setVisible(true);
	}

	public void onHover_TopMenu_Options(String a)
	{
		tryOpeningDropDownOnHover(TOPMENU_DROPDOWN_OPTIONS);
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
		prepareForSomeMenuOpen();
		m_settingsSideMenu.OpenMenu();
	}

	public void onButton_SettingsSideMenu_Close(String a)
	{
		m_settingsSideMenu.CloseMenu();
		prepareForSomeMenuOpen();
	}

	public void onEffectEnd_SettingsSideMenuClose()
	{
		m_settingsSideMenu.OnMenuCloseAnimationFinished();
	}

	public void onButton_SettingsSideMenu_BackPanelClicked(String a)
	{
		System.out.println("hhh");
	}

	// ----------------------------------------------------
	// Open dialog
	// ----------------------------------------------------
	public void On_OpenDialog_Close(String a)
	{
		m_openDialog.OnCloseDialog();
	}

	// ----------------------------------------------------
	// Treebox events
	// ----------------------------------------------------
	@NiftyEventSubscriber(id="tree-box")
    public void OnTreeboxSelectionChanged(String id, ListBoxSelectionChangedEvent<TreeItem<MyTreeItem>> event)
    {
		m_openDialog.OnTreeboxSelectionChanged(id,event);
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

		if (modifiedSlider.getId().compareTo("sl1") == 0)
		{

		}
		else
		{
			java.awt.Toolkit.getDefaultToolkit().beep();
			System.out.println("Error: onSliderChangedEvent: invalid slider, implement it");
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

	static void updateSliderValueLabel(Element slider, float value)
	{
		de.lessvoid.nifty.controls.Slider sliderControl = slider.findElementById("SLIDER_CONTROL").getControl(de.lessvoid.nifty.controls.slider.SliderControl.class);
		de.lessvoid.nifty.controls.Label label = slider.findElementById("SLIDER_VALUE_LABEL").getControl(LabelControl.class);
		String printFormat = m_sliderOutputTextFormat.get(slider);
		label.setText(String.format(Locale.US, printFormat, value));
	}

	public static void InitSlider(String mySliderControlId, float min, float max, float initialValue, float step, String valueLabelFormat)
	{
		
		Element mySliderElement = VeinsWindow.nifty.getScreen("GScreen0").findElementById(mySliderControlId);
		m_sliderOutputTextFormat.put(mySliderElement, valueLabelFormat);
		
		Element niftySliderElement = mySliderElement.findElementById("SLIDER_CONTROL");
		de.lessvoid.nifty.controls.Slider sliderControl = niftySliderElement.getControl(de.lessvoid.nifty.controls.slider.SliderControl.class);
		sliderControl.setup(min, max, initialValue, step, step);
		updateSliderValueLabel(mySliderElement, initialValue);
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