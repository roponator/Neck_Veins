package si.uni_lj.fri.veins3D.gui;

import java.awt.Color;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.filechooser.FileSystemView;

import org.apache.commons.io.FilenameUtils;
import org.newdawn.slick.util.ResourceLoader;

import si.uni_lj.fri.veins3D.gui.NiftyFolderBrowser.MyFileExtensionItem;
import si.uni_lj.fri.veins3D.gui.NiftyFolderBrowser.MyTreeFolderItem;
import si.uni_lj.fri.veins3D.gui.NiftyFolderBrowser.SelectedFile;
import si.uni_lj.fri.veins3D.main.VeinsWindow;
import si.uni_lj.fri.veins3D.utils.HelperFunctions;
import de.lessvoid.nifty.builder.ControlBuilder;
import de.lessvoid.nifty.builder.ScreenBuilder;
import de.lessvoid.nifty.controls.DropDownSelectionChangedEvent;
import de.lessvoid.nifty.controls.ListBoxSelectionChangedEvent;
import de.lessvoid.nifty.controls.Scrollbar;
import de.lessvoid.nifty.controls.TreeBox;
import de.lessvoid.nifty.controls.TreeItem;
import de.lessvoid.nifty.controls.label.LabelControl;
import de.lessvoid.nifty.controls.listbox.ListBoxControl;
import de.lessvoid.nifty.controls.treebox.TreeBoxControl;
import de.lessvoid.nifty.controls.treebox.builder.TreeBoxBuilder;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.render.NiftyImage;
import de.lessvoid.nifty.tools.SizeValue;
import de.lessvoid.nifty.tools.SizeValueType;

public class NiftyFolderBrowserSave
{
	
	de.lessvoid.nifty.controls.ListBox m_fileListboxControl = null;
	de.lessvoid.nifty.controls.TextField m_fileNameTextField = null;

	TreeBox m_treebox = null;
	TreeItem<MyTreeFolderItem> m_root = null;
	TreeItem<MyTreeFolderItem> m_currentlySelectedFolder = null;

	public static String m_lastOpenedFilePath = null; // stores the path to the folder where the last opened file is (if any)
	
	// The constructor param elements are the one that actualy contain the control(er).
	public NiftyFolderBrowserSave(Element folderBrowserParentPanel, Element fileListboxElement, Element textFieldElement)
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
		m_fileNameTextField = (de.lessvoid.nifty.controls.TextField) textFieldElement.getAttachedInputControl().getController();

		Element treeboxElement = builder.build(VeinsWindow.nifty, NiftyScreenController.m_screen, folderBrowserParentPanel);
		m_treebox = (TreeBox) treeboxElement.getAttachedInputControl().getController();

		// clear lists & fill file type list box
		m_fileListboxControl.clear();
		m_fileNameTextField.setText("");

	

		// init foldertree
		String currentPath = ".."; // current dir
		m_root = new TreeItem<MyTreeFolderItem>();
		m_root.setValue(new MyTreeFolderItem(".", "."));
		createBranchesForFolder(m_treebox,currentPath, m_root);
		m_treebox.setTree(m_root);
	}
	
	public void OnOpenDialog()
	{
		changeFileListboxContent(m_currentlySelectedFolder);
	}
	
	// Returns null if nothing is selected
	public SelectedFile TryGettingSelectedFile()
	{
		TreeItem<MyTreeFolderItem> saveLocation = m_currentlySelectedFolder;
		
		if(saveLocation==null)
			saveLocation = m_root;
		
		return getSelectedFileToSave(saveLocation);
	
	}

	// Converts the tree item to file path
	SelectedFile getSelectedFileToSave(TreeItem<MyTreeFolderItem> saveLocation)
	{
		String textfieldText = m_fileNameTextField.getText();
		if(textfieldText.length()<1)
			return null;
		
		if(textfieldText.indexOf(".obj")<0)
			textfieldText += ".obj";
		
		String fullPath = saveLocation.getValue().path+"//"+saveLocation.getValue().folderNameOnly+"//"+textfieldText;
		String extension = FilenameUtils.getExtension(fullPath);
		
		return new SelectedFile(fullPath, extension);
	}
	
	// On selection changed
	public void OnTreeboxSelectionChanged(String id, ListBoxSelectionChangedEvent<TreeItem<MyTreeFolderItem>> event)
	{
		if (event.getSelection().size() > 1)
			System.out.println("ERROR: NiftyFolderBrowser: OnTreeboxSelectionChanged: multiselection is not supported in treebox/list");

		// if something was selected
		if (event.getSelection().isEmpty() == false)
		{
			TreeItem<MyTreeFolderItem> selectedTreeItem = event.getSelection().get(0);

			// save scrollbar value, is restored later so it doesn't jump because the treebox was update
			float currentScrollbarValue = m_treebox.getVerticalScrollbar().getValue();

			// if a new item is selected, expand it (expand only if the item is not already selected or expanded)
			if (m_currentlySelectedFolder != selectedTreeItem && selectedTreeItem.isExpanded() == false)
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
				
				changeFileListboxContent(selectedTreeItem); // must show contents 
			}

			// restore scrollbar value so it doesn't jump after nodes have been added
			m_treebox.getVerticalScrollbar().setValue(currentScrollbarValue);
		}

	}
	
	// On listbox file selected
	public void OnListboxSelectionChanged(String id,ListBoxSelectionChangedEvent event)
	{
		// when clicked on list item set textbox filename
		if(event.getSelection() != null)
		{
			List sel = event.getSelection();
			if(sel.size()==1 && sel.get(0) != null)
				m_fileNameTextField.setText( sel.get(0).toString());		
		}
		
	}
	
	// select the given item, loading all of its folders and adding it to tree
	void selectItem(TreeItem<MyTreeFolderItem> selectedTreeItem)
	{
		m_currentlySelectedFolder = selectedTreeItem;
		m_currentlySelectedFolder.setExpanded(true);

		MyTreeFolderItem myTreeItem = selectedTreeItem.getValue();
		String selectedFolderPath = myTreeItem.path + "//" + myTreeItem.folderNameOnly;
		removeAllChildrenFromBranch(m_currentlySelectedFolder);
		createBranchesForFolder(m_treebox, selectedFolderPath, m_currentlySelectedFolder);

		m_treebox.setTree(m_root); // update tree
	}

	void changeFileListboxContent(TreeItem<MyTreeFolderItem> selectedTreeItem)
	{
		m_fileListboxControl.clear();
		
		if(selectedTreeItem==null)
			return;
		
		// get files with correct type in the selected folder
		MyTreeFolderItem myTreeItem = selectedTreeItem.getValue();
		String selectedFolderPath = myTreeItem.path + "//" + myTreeItem.folderNameOnly;

		// get which file type is selected, select all in case none is selected
		String[] selectedExtensions = new String[]{"obj"}; // select all by default

		// get files with the proper extension(s)
		String[] filesWithCorrectExtensions = HelperFunctions.GetFilesInFolder(selectedFolderPath, selectedExtensions);

		// add files to listbox
		if (filesWithCorrectExtensions != null)
		{
			for (int i = 0; i < filesWithCorrectExtensions.length; ++i)
				m_fileListboxControl.addItem(filesWithCorrectExtensions[i]);
		}
		
		if(filesWithCorrectExtensions!=null && filesWithCorrectExtensions.length>0)
			m_lastOpenedFilePath=selectedTreeItem.getValue().path+"//"+selectedTreeItem.getValue().folderNameOnly;
	}

	static ArrayList<TreeItem<MyTreeFolderItem>> getAllChildrenForBranch(TreeItem<MyTreeFolderItem> branch)
	{
		Iterator<TreeItem<MyTreeFolderItem>> iter = branch.iterator();
		ArrayList<TreeItem<MyTreeFolderItem>> children = new ArrayList<TreeItem<MyTreeFolderItem>>();
		while (iter.hasNext())
		{
			TreeItem<MyTreeFolderItem> item = iter.next();
			children.add(item);
		}

		return children;
	}

	static void removeAllChildrenFromBranch(TreeItem<MyTreeFolderItem> branch)
	{
		ArrayList<TreeItem<MyTreeFolderItem>> children = getAllChildrenForBranch(branch);
		branch.removeTreeItems(children);
	}

	// Reads the given folder and sets all folders as children to the given branch
	static void createBranchesForFolder(TreeBox treebox,String path, TreeItem<MyTreeFolderItem> branchToAddTo)
	{
		String[] folderNames = HelperFunctions.GetDirectoriesInFolder(path);

		if (folderNames == null) // if no subfolders
			return;

		for (int i = 0; i < folderNames.length; ++i)
		// for (int i = 0; i < 2; ++i)
		{
			TreeItem<MyTreeFolderItem> branch = new TreeItem<MyTreeFolderItem>(new MyTreeFolderItem(path, folderNames[i]));
			branchToAddTo.addTreeItem(branch);
		}
		
		//setColorRecursive(treebox.getElement());
		//treebox.getElement().getParent().layoutElements(); // must be called otherwise 'setConstraintWidth doesn't set the new width
		
	}
	
	// recursively unexpand all children of this branch
	static void unexpandAllChildren(TreeItem<MyTreeFolderItem> parent)
	{
		Iterator<TreeItem<MyTreeFolderItem>> iter = parent.iterator();
		ArrayList<TreeItem<MyTreeFolderItem>> children = new ArrayList<TreeItem<MyTreeFolderItem>>();
		while (iter.hasNext())
		{
			TreeItem<MyTreeFolderItem> child = iter.next();
			child.setExpanded(false);
			unexpandAllChildren(child);
		}
	}

}
