package si.uni_lj.fri.veins3D.gui;

import de.lessvoid.nifty.controls.label.LabelControl;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.tools.SizeValue;
import de.lessvoid.nifty.tools.SizeValueType;

public class NiftyLoadingBarDialog
{
	Element m_controlElement = null;
	Element m_progressBarPanel = null;
	LabelControl m_labelTextControl = null;
	LabelControl m_labelProgressControl = null;
	
	public NiftyLoadingBarDialog(Element controlElement)
	{
		m_controlElement = controlElement;
		
		m_labelTextControl = controlElement.findElementById("LABEL_ID").getAttachedInputControl().getControl(LabelControl.class);
		m_labelProgressControl = controlElement.findElementById("LABEL_PROGRESS_ID").getAttachedInputControl().getControl(LabelControl.class);
		m_progressBarPanel = controlElement.findElementById("LABEL_PROGRESS_BAR_PANEL");
	}
	
	// percentProgress is in [0,100] range
	public void ShowAndUpdate(String text,float percentProgress)
	{
		if(percentProgress<0.0f)
			percentProgress = 0.0f;
		if(percentProgress > 100.0f)
			percentProgress = 100.0f;
		
		m_controlElement.setVisible(true);
		m_labelTextControl.setText(text);
		m_labelProgressControl.setText((int)percentProgress+" %");
		m_progressBarPanel.setConstraintWidth(new SizeValue((int)percentProgress, SizeValueType.PercentWidth));
		m_progressBarPanel.getParent().layoutElements(); // must be called otherwise 'setConstraintWidth doesn't set the new width
		
		
	}
	
	public void Hide()
	{
		m_controlElement.setVisible(false);
	}
}
