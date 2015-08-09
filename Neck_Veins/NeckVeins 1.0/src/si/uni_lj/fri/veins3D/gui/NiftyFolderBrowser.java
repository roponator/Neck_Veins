package si.uni_lj.fri.veins3D.gui;

import si.uni_lj.fri.veins3D.main.VeinsWindow;
import de.lessvoid.nifty.builder.ControlBuilder;
import de.lessvoid.nifty.builder.ScreenBuilder;
import de.lessvoid.nifty.controls.TreeBox;
import de.lessvoid.nifty.controls.TreeItem;
import de.lessvoid.nifty.controls.treebox.TreeBoxControl;
import de.lessvoid.nifty.controls.treebox.builder.TreeBoxBuilder;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.render.NiftyImage;
import de.lessvoid.nifty.tools.SizeValue;
import de.lessvoid.nifty.tools.SizeValueType;

public class NiftyFolderBrowser
{
	public NiftyFolderBrowser(Element parentPanel)
	{
		// nested annonymous inner classes black magic
		ControlBuilder builder = new ControlBuilder("aa")
              {{
					control(new ControlBuilder("sds") 
					{{ 
						control(new TreeBoxBuilder("tree-box") {{ displayItems(10); }});
					}});
              }};
              
           now get the on click event for tree element and load folders 
              
         Element treeboxElement =builder.build(VeinsWindow.nifty, NiftyScreenController.m_screen, parentPanel);
        TreeBox treebox = (TreeBox)treeboxElement.getAttachedInputControl().getController();
	

		TreeItem<String> treeRoot = new TreeItem<String>();
		TreeItem<String> branch1 = new TreeItem<String>("hello1");
		TreeItem<String> branch11 = new TreeItem<String>("hello2");
		TreeItem<String> branch12 = new TreeItem<String>("hello3");
		branch1.addTreeItem(branch11);
		branch1.addTreeItem(branch12);
		TreeItem<String> branch2 = new TreeItem<String>("hello4");
		TreeItem<String> branch21 = new TreeItem<String>("hello5");
		TreeItem<String> branch211 = new TreeItem<String>("hello6");
		branch2.addTreeItem(branch21);
		branch21.addTreeItem(branch211);
		treeRoot.addTreeItem(branch1);
		treeRoot.addTreeItem(branch2);
		TreeItem<String> branch3 = new TreeItem<String>("hello6 23");
		treeRoot.addTreeItem(branch3);

		treebox.setTree(treeRoot);
		//ee.setConstraintHeight(new SizeValue(200, SizeValueType.Pixel));

	}
}
