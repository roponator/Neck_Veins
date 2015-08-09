package si.uni_lj.fri.veins3D.gui;

import org.lwjgl.input.Mouse;

import si.uni_lj.fri.veins3D.main.VeinsWindow;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.effects.EffectEventId;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.screen.Screen;

// The side menu at the left side of the screen which slides in and out
public class NiftySettingsSideMenu
{
	boolean m_isOpen = false;
	Element m_menuPanel = null;
	Element m_closedMenuButtonPanel = null; // the thin button panel that is shown when this is closed

	public NiftySettingsSideMenu()
	{
		m_menuPanel = NiftyScreenController.m_screen.findElementById("PANEL_SIDE_SETTINGS_MENU_LEFT");
		m_closedMenuButtonPanel =  NiftyScreenController.m_screen.findElementById("PANEL_SIDE_SETTINGS_MENU_RIGHT_HOLDER");

		// doesn't work in xml???
		m_menuPanel.setRenderOrder(4000);
		m_closedMenuButtonPanel.setRenderOrder(1100);
		m_menuPanel.setVisible(false);
		m_closedMenuButtonPanel.setVisible(true);
		
		
	}

	// If menu is open and mouse click is outside this: close
	public void OnMouseClick()
	{
		if(m_isOpen)
			if (m_menuPanel.isMouseInsideElement(Mouse.getX(), Mouse.getY()) == false)
				CloseMenu();		
	}

	// Slides menu to right, opening it
	public void OpenMenu()
	{
		if (m_isOpen)
			return;

		m_isOpen = true;
		m_menuPanel.startEffect(EffectEventId.onCustom, null, "openSettingsSideMenu");
		m_menuPanel.setVisible(true);
		m_closedMenuButtonPanel.setVisible(false);
	}

	// Slides menu to left, closing it
	public void CloseMenu()
	{
		if (m_isOpen == false)
			return;

		m_isOpen = false;
		m_menuPanel.startEffect(EffectEventId.onCustom, null, "closeSettingsSideMenu");
		m_closedMenuButtonPanel.setVisible(true);
	}

	public void OnMenuCloseAnimationFinished()
	{
		m_menuPanel.stopEffect(EffectEventId.onCustom); // must stop otherwise it pops for one frame or dissapears, strange stuff
		m_menuPanel.setVisible(false);
	}
}
