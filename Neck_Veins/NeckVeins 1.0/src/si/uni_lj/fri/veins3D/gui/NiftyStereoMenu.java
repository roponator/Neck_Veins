package si.uni_lj.fri.veins3D.gui;

import si.uni_lj.fri.veins3D.main.VeinsWindow;
import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.controls.checkbox.CheckboxControl;
import de.lessvoid.nifty.controls.label.LabelControl;
import de.lessvoid.nifty.controls.listbox.ListBoxControl;
import de.lessvoid.nifty.controls.slider.SliderControl;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.tools.SizeValue;
import de.lessvoid.nifty.tools.SizeValueType;


public class NiftyStereoMenu
{
	
	Element m_controlElement = null;

	public NiftyStereoMenu(Element elementWithStereoControl)
	{
		m_controlElement = elementWithStereoControl;
		
		// slider label & values
		Element mySliderContainer = m_controlElement.findElementById("sliderC");
		NiftyScreenController.InitSlider(mySliderContainer, 0.0f, 0.2f, 0.05f, 0.01f, "Disparity", "%.2f");
		
		// checkbox label
		LabelControl checkboxLabel = m_controlElement.findElementById("enabledCheckbox").findElementById("checkboxLabel").getAttachedInputControl().getControl(LabelControl.class);
		checkboxLabel.setText("Enabled");
	}

	// Clears old and sets new

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
		boolean isEnabled = m_controlElement.findElementById("enabledCheckbox").getAttachedInputControl().getControl(CheckboxControl.class).isChecked();
		
		
		// close this menu
		OnButton_CloseOrCancel();
	}
	
	public void ResetPosition()
	{
		m_controlElement.setConstraintX(new SizeValue(10, SizeValueType.PercentWidth));		
		m_controlElement.setConstraintY(new SizeValue(10, SizeValueType.PercentHeight));	
	}

}
