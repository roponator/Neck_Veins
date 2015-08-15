package si.uni_lj.fri.veins3D.gui;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.io.FilenameUtils;
import org.newdawn.slick.util.ResourceLoader;

import si.uni_lj.fri.veins3D.main.VeinsWindow;
import si.uni_lj.fri.veins3D.utils.HelperFunctions;
import de.lessvoid.nifty.builder.ControlBuilder;
import de.lessvoid.nifty.builder.ScreenBuilder;
import de.lessvoid.nifty.controls.ListBoxSelectionChangedEvent;
import de.lessvoid.nifty.controls.Scrollbar;
import de.lessvoid.nifty.controls.TreeBox;
import de.lessvoid.nifty.controls.TreeItem;
import de.lessvoid.nifty.controls.listbox.ListBoxControl;
import de.lessvoid.nifty.controls.treebox.TreeBoxControl;
import de.lessvoid.nifty.controls.treebox.builder.TreeBoxBuilder;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.render.NiftyImage;
import de.lessvoid.nifty.tools.SizeValue;
import de.lessvoid.nifty.tools.SizeValueType;

public class NiftyFolderBrowser
{
	// Represents a folder in the folder treebox
	static class MyTreeItem
	{
		public String path;
		public String folderNameOnly;

		public MyTreeItem(String Path, String FolderNameOnly)
		{
			this.path = Path;
			this.folderNameOnly = FolderNameOnly;
		}

		// This is used ny niftyGUI to display a string
		@Override
		public String toString()
		{
			return folderNameOnly;
		}
	}

	// Represents one file extension in the file extensions dropdown list
	static class MyFileExtensionItem
	{
		public String extensionOnly; // just the extensions, eg 'mdh', 'obj'
		
		public MyFileExtensionItem(String ext)
		{
			extensionOnly = ext;
		}
		
		// This is used ny niftyGUI to display a string
		@Override
		public String toString()
		{
			return " ."+extensionOnly; // adds an indent so its prettier, problematic with xml gui...
		}
	}
	
	de.lessvoid.nifty.controls.ListBox m_fileListboxControl = null;
	de.lessvoid.nifty.controls.DropDown<MyFileExtensionItem> m_fileTypeDropdownControl = null;

	TreeBox m_treebox = null;
	TreeItem<MyTreeItem> m_root = null;
	TreeItem<MyTreeItem> m_currentlySelectedItem = null;

	// The constructor param elements are the one that actualy contain the control(er).
	public NiftyFolderBrowser(Element folderBrowserParentPanel, Element fileListboxElement, Element fileTypeElement)
	{
		// create treebox for folders, nested anonymous inner classes black magic
		ControlBuilder builder = new ControlBuilder("")
		{
			{
				control(new ControlBuilder("")
				{
					{
						control(new TreeBoxBuilder("tree-box")
						{
							{
								displayItems(10);
								margin("2%,2%,2%,2%");
								width(new SizeValue(96, SizeValueType.Percent));
								height(new SizeValue(96, SizeValueType.Percent));
							}
						});
					}
				});
			}
		};

		// get/create controls
		m_fileListboxControl = (de.lessvoid.nifty.controls.ListBox) fileListboxElement.getAttachedInputControl().getController();
		m_fileTypeDropdownControl = (de.lessvoid.nifty.controls.DropDown<MyFileExtensionItem>) fileTypeElement.getAttachedInputControl().getController();

		Element treeboxElement = builder.build(VeinsWindow.nifty, NiftyScreenController.m_screen, folderBrowserParentPanel);
		m_treebox = (TreeBox) treeboxElement.getAttachedInputControl().getController();

		// clear lists & fill file type list box
		m_fileListboxControl.clear();
		m_fileTypeDropdownControl.clear();

		String space = " ";
		String[] supportedFileFormats = NiftyScreenController.m_supportedFileTypes;
		for (int i = 0; i < supportedFileFormats.length; ++i)
			m_fileTypeDropdownControl.addItem(new MyFileExtensionItem(supportedFileFormats[i]));

		// init foldertree
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
			if (m_currentlySelectedItem != selectedTreeItem && selectedTreeItem.isExpanded() == false)
			{
				unexpandAllChildren(selectedTreeItem.getParentItem());
				selectItem(selectedTreeItem);
				changeFileListboxContent(selectedTreeItem);
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

		m_treebox.setTree(m_root); // update tree
	}

	void changeFileListboxContent(TreeItem<MyTreeItem> selectedTreeItem)
	{
		// get files with correct type in the selected folder
		MyTreeItem myTreeItem = selectedTreeItem.getValue();
		String selectedFolderPath = myTreeItem.path + "//" + myTreeItem.folderNameOnly;

		// get which file type is selected, select all in case none is selected
		String[] selectedExtensions = NiftyScreenController.m_supportedFileTypes; // select all by default

		if (m_fileTypeDropdownControl.getSelection() != null) // if an extension is selected, show only those file types
		{
			MyFileExtensionItem selectedExtension = m_fileTypeDropdownControl.getSelection();
			selectedExtensions = new String[]{selectedExtension.extensionOnly};
		}

		// get files with the proper extension(s)
		String[] filesWithCorrectExtensions = HelperFunctions.GetFilesInFolder(selectedFolderPath, selectedExtensions);

		// add files to listbox
		m_fileListboxControl.clear();
		if (filesWithCorrectExtensions != null)
		{
			for (int i = 0; i < filesWithCorrectExtensions.length; ++i)
				m_fileListboxControl.addItem(filesWithCorrectExtensions[i]);
		}
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
