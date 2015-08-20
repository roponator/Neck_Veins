package si.uni_lj.fri.veins3D.gui;

import java.util.List;

import org.lwjgl.opengl.DisplayMode;

import si.uni_lj.fri.veins3D.main.VeinsWindow;
import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.controls.checkbox.CheckboxControl;
import de.lessvoid.nifty.controls.label.LabelControl;
import de.lessvoid.nifty.controls.listbox.ListBoxControl;
import de.lessvoid.nifty.elements.Element;

public class NiftyResolutionMenu
{
	// Represents one resolution line
	public static class ResolutionItem
	{
		public DisplayMode displayMode;

		public ResolutionItem(DisplayMode DisplayMode)
		{
			displayMode = DisplayMode;
		}

		@Override
		public String toString()
		{
			return displayMode.toString();
		}
	}

	Element m_controlElement = null;
	ListBox<ResolutionItem> m_listboxControl = null;

	public NiftyResolutionMenu(Element elementWithResolutionControl)
	{
		m_controlElement = elementWithResolutionControl;

		Element listbox = m_controlElement.findElementById("myListBox");
		m_listboxControl = listbox.getAttachedInputControl().getControl(ListBoxControl.class);

		// rename checkbox label
		LabelControl checkboxLabel = m_controlElement.findElementById("checkboxFullscreen").findElementById("checkboxLabel").getAttachedInputControl().getControl(LabelControl.class);
		checkboxLabel.setText("Fullscreen");
	}

	// Clears old and sets new
	public void SetResolutions(DisplayMode[] displayModes)
	{
		m_listboxControl.clear();
		for (int i = 0; i < displayModes.length; ++i)
			m_listboxControl.addItem(new ResolutionItem(displayModes[i]));

	}

	public void OnButton_ShowDialog()
	{
		m_controlElement.setVisible(true);
	}

	public void OnButton_CloseOrCancel()
	{
		m_controlElement.setVisible(false);
	}

	public void OnButton_OK()
	{
		// check if any resolution is selected
		List<ResolutionItem> selectedItems = m_listboxControl.getSelection();
		if (selectedItems.size() == 1) // set only if exactly one item is selected
		{
			ResolutionItem selectedResItem = selectedItems.get(0);
			boolean isFullscreen = m_controlElement.findElementById("checkboxFullscreen").getAttachedInputControl().getControl(CheckboxControl.class).isChecked();
			VeinsWindow.ResizeWindow(selectedResItem.displayMode, isFullscreen);
		}

		// close this menu
		OnButton_CloseOrCancel();

	}

}
