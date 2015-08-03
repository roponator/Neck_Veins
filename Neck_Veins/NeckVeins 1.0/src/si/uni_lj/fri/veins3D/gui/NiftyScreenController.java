package si.uni_lj.fri.veins3D.gui;

import si.uni_lj.fri.veins3D.main.VeinsWindow;
import de.lessvoid.nifty.EndNotify;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.ButtonClickedEvent;
import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.effects.EffectEventId;
import de.lessvoid.nifty.effects.impl.Move;
import de.lessvoid.nifty.elements.Element;
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

	//Element m_popupMenu_MainMenu_File = null;;
	NiftySettingsSideMenu m_settingsSideMenu = null;
	
	
	// This is called when the screen is created
	private void init()
	{
		//m_popupMenu_MainMenu_File = NiftyPopupMenus.CreatePopup_File(VeinsWindow.nifty);
		m_settingsSideMenu = new NiftySettingsSideMenu(m_screen);
	}

	static class Bla
	{
		
	}
	public void onBtn(String bla)
	{
		/*m_popupMenu_MainMenu_File.setConstraintX(new SizeValue(300, SizeValueType.Pixel));
		  m_popupMenu_MainMenu_File.setConstraintY(new SizeValue(300, SizeValueType.Pixel));
		  m_popupMenu_MainMenu_File.layoutElements();
	
		// System.out.println("BTN***************");
		nifty.showPopup(m_screen, m_popupMenu_MainMenu_File.getId(), null);
		
		m_popupMenu_MainMenu_File.setConstraintX(new SizeValue(300, SizeValueType.Pixel));
		  m_popupMenu_MainMenu_File.setConstraintY(new SizeValue(300, SizeValueType.Pixel));
		  m_popupMenu_MainMenu_File.layoutElements();*/
		
		/*ListBox lb = m_screen.findNiftyControl("GListBox0",ListBox.class);
		 lb.enable();
		  lb.addItem("a");
		  lb.addItem("b");
		  lb.addItem("c");*/
		
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
		
		//System.out.println("BIND***************");
	}

	@Override
	public void onStartScreen()
	{
		//System.out.println("START***************");
	}

	@Override
	public void onEndScreen()
	{
		//System.out.println("END***************");
	}

	public void gotoScreen(final String screenId)
	{
		nifty.gotoScreen(screenId);
	}
}