package si.uni_lj.fri.veins3D.gui;

import java.awt.Frame;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import si.uni_lj.fri.veins3D.main.VeinsWindow;
import de.lessvoid.nifty.EndNotify;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.ButtonClickedEvent;
import de.lessvoid.nifty.controls.NiftyInputControl;
import de.lessvoid.nifty.effects.EffectEventId;
import de.lessvoid.nifty.effects.impl.Move;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.input.NiftyInputEvent;
import de.lessvoid.nifty.screen.DefaultScreenController;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.tools.SizeValue;
import de.lessvoid.nifty.tools.SizeValueType;

/*
 This class gets created by Nifty when loading XML
 */
public class NiftyScreenController extends DefaultScreenController
{
	Screen m_screen = null;;

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

	public void update()
	{

	}

	// Detects mouse down click (after it was up)
	public void onMouseLeftDownClicked()
	{
		// Hide all top menu drop down menus
		closeAllTopMenuDropDownMenus();
	}

	// This is called when the screen is created
	private void init()
	{
		// m_popupMenu_MainMenu_File = NiftyPopupMenus.CreatePopup_File(VeinsWindow.nifty);
		m_settingsSideMenu = new NiftySettingsSideMenu(m_screen);

		// ---------------------------------------
		// Top menu bars
		// ---------------------------------------
		m_panel_topMenu_DropDownMenus = new Element[2];
		m_panel_topMenu_DropDownMenus[TOPMENU_DROPDOWN_FILE] = nifty.getScreen("GScreen0").findElementById("TOP_MENU_FILE_DROP_DOWN_PANEL");
		m_panel_topMenu_DropDownMenus[TOPMENU_DROPDOWN_OPTIONS] = nifty.getScreen("GScreen0").findElementById("TOP_MENU_OPTIONS_DROP_DOWN_PANEL");

		closeAllTopMenuDropDownMenus();
	}

	static class Bla
	{

	}

	// ----------------------------------------------------
	// Minimize,maximize,close
	// ----------------------------------------------------
	public void onButton_TopMenu_Minimize(String a)
	{
		VeinsWindow.frame.setState(Frame.ICONIFIED);
	}

	public void onButton_TopMenu_Maximize(String a)
	{

		// VeinsWindow.frame.setExtendedState( VeinsWindow.frame.getExtendedState()|JFrame.MAXIMIZED_BOTH );
		VeinsWindow.veinsWindow.ResizeWindow(true);
	}

	public void onButton_TopMenu_Close(String a)
	{
		// VeinsWindow.frame.dispatchEvent(new WindowEvent(VeinsWindow.frame, WindowEvent.WINDOW_CLOSING));
		VeinsWindow.veinsWindow.exitProgram(0);

	}

	// ----------------------------------------------------
	// Top menu bar File,Options,.. buttons
	// ----------------------------------------------------
	public void onButton_TopMenu_File(String a)
	{
		closeAllTopMenuDropDownMenus();
		m_panel_topMenu_DropDownMenus[TOPMENU_DROPDOWN_FILE].setVisible(true);
	}
	public void onHover_TopMenu_File(String a)
	{
		tryOpeningDropDownOnHover(TOPMENU_DROPDOWN_FILE);	
	}

	IF FONT CHANGE ALL FONTS ARE CHANGED??
	
	public void onButton_TopMenu_Options(String a)
	{
		closeAllTopMenuDropDownMenus();
		m_panel_topMenu_DropDownMenus[TOPMENU_DROPDOWN_OPTIONS].setVisible(true);	
	}
	public void onHover_TopMenu_Options(String a)
	{
		tryOpeningDropDownOnHover(TOPMENU_DROPDOWN_OPTIONS);		
	}
	
	boolean isAnyTopMenuDropDownOpen()
	{
		boolean isVisible = false;
		for(int i=0;i<m_panel_topMenu_DropDownMenus.length;++i)
			isVisible = isVisible || m_panel_topMenu_DropDownMenus[i].isVisible();
		
		return isVisible;
	}
	
	void closeAllTopMenuDropDownMenus()
	{
		for(int i=0;i<m_panel_topMenu_DropDownMenus.length;++i)
			m_panel_topMenu_DropDownMenus[i].setVisible(false);
	}
	
	void tryOpeningDropDownOnHover(int TOPMENU_DROPDOWN)
	{
		// if this dropdown is closed and any other drop down is active while
		// we hover on this closed dropdown: close other & show this
		Element currentDropDown = m_panel_topMenu_DropDownMenus[TOPMENU_DROPDOWN];
		if(currentDropDown.isVisible() == false &&	isAnyTopMenuDropDownOpen() )
			{
				closeAllTopMenuDropDownMenus();
				currentDropDown.setVisible(true);
			}
	}
	
	// ----------------------------------------------------

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

		m_settingsSideMenu.OpenMenu();

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