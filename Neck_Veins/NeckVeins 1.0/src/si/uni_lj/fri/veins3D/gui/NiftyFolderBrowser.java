package si.uni_lj.fri.veins3D.gui;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Iterator;

import org.newdawn.slick.util.ResourceLoader;

import si.uni_lj.fri.veins3D.main.VeinsWindow;
import si.uni_lj.fri.veins3D.utils.HelperFunctions;
import de.lessvoid.nifty.builder.ControlBuilder;
import de.lessvoid.nifty.builder.ScreenBuilder;
import de.lessvoid.nifty.controls.ListBoxSelectionChangedEvent;
import de.lessvoid.nifty.controls.Scrollbar;
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
								margin("2%,2%,2%,2%");
								width(new SizeValue(96,SizeValueType.Percent));
								height(new SizeValue(96,SizeValueType.Percent));
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

		// if something was selected
		if (event.getSelection().isEmpty() == false)
		{		
			TreeItem<MyTreeItem> selectedTreeItem = event.getSelection().get(0);

			// save scrollbar value, is restored later so it doesn't jump because the treebox was update
			float currentScrollbarValue = m_treebox.getVerticalScrollbar().getValue(); 
			
			// if a new item is selected, expand it (expand only if the item is not already selected or expanded)
			if(m_currentlySelectedItem != selectedTreeItem && selectedTreeItem.isExpanded() == false) 
			{
				unexpandAllChildren(selectedTreeItem.getParentItem());	
				selectItem(selectedTreeItem);			
			}
			else
			{
				// in case the same item was clicked, collapse it or expand it, depending on its current state
				boolean isCurrentlyExpanded = selectedTreeItem.isExpanded();
				selectedTreeItem.setExpanded(!isCurrentlyExpanded);
				m_treebox.setTree(m_root); // update tree
			}
			
			// restore scrollbar value so it doesn't jump after nodes have been added
			m_treebox.getVerticalScrollbar().setValue(currentScrollbarValue); 
		}
		
	}

	// select the given item, loading all of its folders and adding it to tree
	void selectItem(TreeItem<MyTreeItem> selectedTreeItem)
	{
		m_currentlySelectedItem = selectedTreeItem;
		m_currentlySelectedItem.setExpanded(true);

		MyTreeItem myTreeItem = selectedTreeItem.getValue();
		String selectedFolderPath = myTreeItem.path + "//" + myTreeItem.folderNameOnly;
		removeAllChildrenFromBranch(m_currentlySelectedItem);
		createBranchesForFolder(selectedFolderPath, m_currentlySelectedItem);

		m_treebox.setTree(m_root);  // update tree
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

	static void removeAllChildrenFromBranch(TreeItem<MyTreeItem> branch)
	{
		ArrayList<TreeItem<MyTreeItem>> children = getAllChildrenForBranch(branch);
		branch.removeTreeItems(children);
	}
	
	// Reads the given folder and sets all folders as children to the given branch
	static void createBranchesForFolder(String path, TreeItem<MyTreeItem> branchToAddTo)
	{
		String[] folderNames = HelperFunctions.GetDirectoriesInFolder(path);

		if (folderNames == null) // if no subfolders
			return;

		for (int i = 0; i < folderNames.length; ++i)
		// for (int i = 0; i < 2; ++i)
		{
			TreeItem<MyTreeItem> branch = new TreeItem<MyTreeItem>(new MyTreeItem(path, folderNames[i]));
			branchToAddTo.addTreeItem(branch);
		}
	}

	// recursively unexpand all children of this branch
	static void unexpandAllChildren(TreeItem<MyTreeItem> parent)
	{
		Iterator<TreeItem<MyTreeItem>> iter = parent.iterator();
		ArrayList<TreeItem<MyTreeItem>> children = new ArrayList<TreeItem<MyTreeItem>>();
		while (iter.hasNext())
		{
			TreeItem<MyTreeItem> child = iter.next();
			child.setExpanded(false);
			unexpandAllChildren(child);
		}
	}

}
