package si.uni_lj.fri.veins3D.gui;


import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.Menu;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.tools.SizeValue;
import de.lessvoid.nifty.tools.SizeValueType;

/*
 * Contains all popup menus
 */
public class NiftyPopupMenus
{
	// MenuItem holder (eg "Save As...")
	public static class MenuItem
	{
		String m_id = "";
		public MenuItem(String id)
		{
			m_id = id;
		}
	}
	
	// Main menu bar: File
	public static Element CreatePopup_File(Nifty nifty) 
	{
		 Element popup = nifty.createPopup("niftyPopupMenu"); // 'niftyPopupMenu' is a "type" NOT name!
		
		  Menu<MenuItem> popupMenu = popup.findNiftyControl("#menu", Menu.class);
		  popupMenu.setWidth(new SizeValue("250px")); // this is required and is not happening automatically!
		  popupMenu.addMenuItem("MenuItem 1", "menu/listen.png", new MenuItem("Something"));
		 
		  popupMenu.addMenuItem("MenuItem 2", "menu/something.png", new MenuItem("Something Else"));
		  popupMenu.addMenuItemSeparator();
		  popupMenu.addMenuItem("MenuItem 3", "menu/something.png", new MenuItem("Something Else"));
		  
		 
		  popup.setConstraintX(new SizeValue(0, SizeValueType.Pixel));
		  popup.setConstraintY(new SizeValue(0, SizeValueType.Pixel));
		  popup.layoutElements();
		 
		 return popup;
	}

}
