package si.uni_lj.fri.veins3D.gui;

import de.lessvoid.nifty.effects.EffectEventId;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.screen.Screen;

// The side menu at the left side of the screen which slides in and out
public class NiftySettingsSideMenu
{
	boolean m_isOpen = false;
	Element m_menuPanel = null;
	
	public NiftySettingsSideMenu(Screen screen)
	{
		 m_menuPanel = screen.findElementById("PANEL_SIDE_SETTINGS_MENU");
		 
	}
	
	// Slides menu to right, opening it
	public void OpenMenu()
	{
		if(m_isOpen)
			return;
		
		m_isOpen = true;	
		m_menuPanel.startEffect(EffectEventId.onCustom,null,"openSettingsSideMenu");
	
	}
	
	// Slides menu to left, closing it
	public void CloseMenu()
	{
		if(m_isOpen == false)
			return;
		
		m_isOpen = false;	
		m_menuPanel.startEffect(EffectEventId.onCustom,null,"closeSettingsSideMenu");
	}
}
