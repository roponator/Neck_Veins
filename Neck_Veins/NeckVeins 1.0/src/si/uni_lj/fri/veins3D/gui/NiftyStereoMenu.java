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
	float m_prevSliderValue = 0.0f;
	
	public NiftyStereoMenu(Element elementWithStereoControl)
	{
		m_controlElement = elementWithStereoControl;
		
		// slider label & values
		m_prevSliderValue = VeinsWindow.settings.stereoValue;
		Element mySliderContainer = m_controlElement.findElementById("disparitySlider");
		NiftyScreenController.InitSlider(mySliderContainer, -100.0f, 100, m_prevSliderValue, 1, "Disparity", "%.0f");
		
		// checkbox label
		LabelControl checkboxLabel = m_controlElement.findElementById("enabledCheckbox").findElementById("checkboxLabel").getAttachedInputControl().getControl(LabelControl.class);
		checkboxLabel.setText("Enabled");
		m_controlElement.findElementById("enabledCheckbox").getAttachedInputControl().getControl(CheckboxControl.class).setChecked(VeinsWindow.settings.stereoEnabled);
		
	}

	// Clears old and sets new

	public void OnButton_ShowDialog()
	{
		m_controlElement.setVisible(true);
	}

	public void OnButton_CloseOrCancel()
	{
		m_controlElement.setVisible(false);
		
		Element niftySliderElement = m_controlElement.findElementById("disparitySlider").findElementById("SLIDER_CONTROL");
		de.lessvoid.nifty.controls.Slider sliderControl = niftySliderElement.getControl(de.lessvoid.nifty.controls.slider.SliderControl.class);
		sliderControl.setValue(m_prevSliderValue);
	}

	public void OnButton_OK()
	{
		boolean isEnabled = m_controlElement.findElementById("enabledCheckbox").getAttachedInputControl().getControl(CheckboxControl.class).isChecked();
		
		VeinsWindow.settings.stereoEnabled = isEnabled;
		
		m_prevSliderValue = VeinsWindow.settings.stereoValue ;
		
		// close this menu
		OnButton_CloseOrCancel();
	}
	
	public void ResetPosition()
	{
		m_controlElement.setConstraintX(new SizeValue(10, SizeValueType.PercentWidth));		
		m_controlElement.setConstraintY(new SizeValue(10, SizeValueType.PercentHeight));	
	}

}
