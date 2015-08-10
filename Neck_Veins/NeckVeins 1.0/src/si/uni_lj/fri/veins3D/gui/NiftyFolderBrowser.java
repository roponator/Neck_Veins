package si.uni_lj.fri.veins3D.gui;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Iterator;

import si.uni_lj.fri.veins3D.main.VeinsWindow;
import si.uni_lj.fri.veins3D.utils.HelperFunctions;
import de.lessvoid.nifty.builder.ControlBuilder;
import de.lessvoid.nifty.builder.ScreenBuilder;
import de.lessvoid.nifty.controls.ListBoxSelectionChangedEvent;
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
	static class MyTreeItem
	{
		public String path;
		public String folderNameOnly;

		public MyTreeItem(String Path, String FolderNameOnly)
		{
			this.path = Path;
			this.folderNameOnly = FolderNameOnly;
		}

		@Override
		public String toString()
		{
			return folderNameOnly;
		}
	}
	
	TreeBox m_treebox = null;
	TreeItem<MyTreeItem> m_root = null;
	TreeItem<MyTreeItem> m_currentlySelectedItem = null;

	public NiftyFolderBrowser(Element parentPanel)
	{
		// nested annonymous inner classes black magic
		ControlBuilder builder = new ControlBuilder("aa")
		{
			{
				control(new ControlBuilder("sds")
				{
					{
						control(new TreeBoxBuilder("tree-box")
						{
							{
								displayItems(10);
							}
						});
					}
				});
			}
		};

		Element treeboxElement = builder.build(VeinsWindow.nifty, NiftyScreenController.m_screen, parentPanel);
		m_treebox = (TreeBox) treeboxElement.getAttachedInputControl().getController();

		m_root = new TreeItem<MyTreeItem>();

		createBranchesForFolder("C://", m_root);

		m_treebox.setTree(m_root);

	}

	// On selection changed

	public void OnTreeboxSelectionChanged(String id, ListBoxSelectionChangedEvent<TreeItem<MyTreeItem>> event)
	{
		if (event.getSelection().size() > 1)
			System.out.println("ERROR: NiftyFolderBrowser: OnTreeboxSelectionChanged: multiselection is not supported in treebox/list");

		deselectCurrentItem(); PROBLEM IN HERE

		if (event.getSelection().isEmpty() == false)
			selectItem(event.getSelection().get(0));

	}

	// removes the children of the given item, hiding them
	void deselectCurrentItem()
	{
		if (m_currentlySelectedItem == null)
			return;

		// remove all children
		ArrayList<TreeItem<MyTreeItem>> children = getAllChildrenForBranch(m_currentlySelectedItem);
		m_currentlySelectedItem.removeTreeItems(children);	
		
		m_currentlySelectedItem = null;
	}

	// select the given item, loading all of its folders and adding it to tree
	void selectItem(TreeItem<MyTreeItem> selectedTreeItem)
	{
		m_currentlySelectedItem = selectedTreeItem;
		int s1=m_treebox.getItems().size();
		/*MyTreeItem myTreeItem = selectedTreeItem.getValue();
		String selectedFolderPath = myTreeItem.path+"//"+myTreeItem.folderNameOnly;
		createBranchesForFolder(selectedFolderPath,m_currentlySelectedItem);	*/
		
		TreeItem<MyTreeItem> newTreeItem = new TreeItem<MyTreeItem>(new MyTreeItem("blabla", "bie"));
		newTreeItem.setExpanded(true);
		m_currentlySelectedItem.addTreeItem(newTreeItem);
		m_currentlySelectedItem.setExpanded(true);
	//	m_treebox.deselectItem(0); // this is for multi-selection, so we just deselect the first item since we only have 1
		
		//m_root.setValue(m_root.getValue());
		//m_treebox.removeItem(m_root);
	//	m_treebox.setTree(m_root);
		int s2=m_treebox.getItems().size();
		int x=s2;
		m_root.setExpanded(true);
		//m_treebox.deselectItem(0); // this is for multi-selection, so we just deselect the first item since we only have 1
		
		m_treebox.setTree(m_root);
	}
	
	static ArrayList<TreeItem<MyTreeItem>> getAllChildrenForBranch(TreeItem<MyTreeItem> branch)
	{
		Iterator<TreeItem<MyTreeItem>> iter = branch.iterator();
		ArrayList<TreeItem<MyTreeItem>> children = new ArrayList<TreeItem<MyTreeItem>>();
		while (iter.hasNext())
		{
			TreeItem<MyTreeItem> item = iter.next();
			children.add(item);
		}
		
		return children;
	}

	// Reads the given folder and sets all folders as children to the given branch
	static void createBranchesForFolder(String path, TreeItem<MyTreeItem> branchToAddTo)
	{
		String[] folderNames = HelperFunctions.GetDirectoriesInFolder(path);

		//for (int i = 0; i < folderNames.length; ++i)
		for (int i = 0; i < 2; ++i)
		{
			TreeItem<MyTreeItem> branch =  new TreeItem<MyTreeItem>(new MyTreeItem(path, folderNames[i]));
			branchToAddTo.addTreeItem(branch);
		}
	}

}
