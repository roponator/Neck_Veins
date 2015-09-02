package si.uni_lj.fri.veins3D.gui;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import si.uni_lj.fri.veins3D.gui.render.VeinsRenderer;
import si.uni_lj.fri.veins3D.gui.render.VeinsRenderer.ModelType;
import si.uni_lj.fri.veins3D.main.VeinsWindow;
import si.uni_lj.fri.volumeRaycast.VolumeRaycast;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.checkbox.CheckboxControl;
import de.lessvoid.nifty.controls.dropdown.DropDownControl;
import de.lessvoid.nifty.controls.label.LabelControl;
import de.lessvoid.nifty.effects.EffectEventId;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.tools.SizeValue;
import de.lessvoid.nifty.tools.SizeValueType;

// The side menu at the left side of the screen which slides in and out
public class NiftySettingsSideMenu
{

	class MyShaderItem
	{
		public int shaderId;
		public String text;

		public MyShaderItem(int ShaderId, String Text)
		{
			shaderId = ShaderId;
			text = Text;
		}

		@Override
		public String toString()
		{
			return text;
		}
	};

	static final String VOLUME_RENDER_METHOD_ISO = "ISO";
	static final String VOLUME_RENDER_METHOD_ALPHA = "Alpha";
	static final String VOLUME_RENDER_METHOD_MAXMIMUM_PROJECTIOn = "Maxmimum Projection";
	
	boolean m_isOpen = false;
	Element m_menuPanel = null;
	Element m_closedMenuButtonPanel = null; // the thin button panel that is shown when this is closed
	Element m_sliderMinTriangles;

	Element m_controlContainer = null;

	// main control elements
	Element m_marchingCubesControl = null;
	Element m_objControl = null;
	Element m_volumeRendererControl = null;

	// Marching cubes
	SizeValue m_marchingCubesControlHeight = null;

	// Obj
	public de.lessvoid.nifty.controls.DropDown<MyShaderItem> m_objShaderTypeDropdown = null;
	public de.lessvoid.nifty.controls.CheckBox m_wireframeCheckbox = null;
	SizeValue m_objControlHeight = null;

	// Volume renderer
	SizeValue m_volmueRendererControlHeight = null;
	public de.lessvoid.nifty.controls.CheckBox m_volumeCheckboxSSAO = null;
	public de.lessvoid.nifty.controls.CheckBox m_volumeCheckboxDof = null;

	public NiftySettingsSideMenu()
	{
		m_menuPanel = NiftyScreenController.m_screen.findElementById("PANEL_SIDE_SETTINGS_MENU_LEFT");
		m_closedMenuButtonPanel = NiftyScreenController.m_screen.findElementById("PANEL_SIDE_SETTINGS_MENU_RIGHT_HOLDER");

		// doesn't work in xml???
		m_menuPanel.setRenderOrder(1100);
		m_closedMenuButtonPanel.setRenderOrder(4000);
		m_menuPanel.setVisible(false);
		m_closedMenuButtonPanel.setVisible(true);

		m_controlContainer = m_menuPanel.findElementById("MENU_SETTINGS_CONTROL_CONTAINER");

		// init marching cubes control
		m_marchingCubesControl = m_controlContainer.findElementById("SIDE_SETTINGS_MARCHING_CUBES_CONTROL");
		m_marchingCubesControlHeight = m_marchingCubesControl.getConstraintHeight();

		Element sliderThreshold = m_marchingCubesControl.findElementById("SLIDER_MODEL_MESH_THRESHOLD");
		m_sliderMinTriangles = m_marchingCubesControl.findElementById("SLIDER_MODEL_MESH_MIN_TRIANGLES");
		NiftyScreenController.InitSlider(sliderThreshold, 0.0f, 1.0f, 0.5f, 0.01f, "Threshold", "%.2f");
		NiftyScreenController.InitSlider(m_sliderMinTriangles, 0.0f, 200000.0f, 0.0f, 1.0f, "Min Triangles", "%.0f");

		// init obj control
		m_objControl = m_controlContainer.findElementById("SIDE_SETTINGS_MARCHING_OBJ_CONTROL");
		m_objControlHeight = m_objControl.getConstraintHeight();
		m_wireframeCheckbox = m_controlContainer.findElementById("WIREFRAME_CHECKBOX_ID").getAttachedInputControl().getControl(CheckboxControl.class);
		m_controlContainer.findElementById("WIREFRAME_CHECKBOX_ID").findElementById("checkboxLabel").getAttachedInputControl().getControl(LabelControl.class).setText("Wireframe");
		m_objShaderTypeDropdown = m_objControl.findElementById("OBJ_SHADER_DROPDOWN").getAttachedInputControl().getControl(DropDownControl.class);
		m_objShaderTypeDropdown.addItem(new MyShaderItem(VeinsRenderer.SIMPLE_SHADER, "Simple Shader"));
		m_objShaderTypeDropdown.addItem(new MyShaderItem(VeinsRenderer.SIMPLE_SHADER_NORM_INTERP, "Norm Interp"));
		m_objShaderTypeDropdown.addItem(new MyShaderItem(VeinsRenderer.SIMPLE_SHADER_NORM_INTERP_AMBIENT_L, "Norm Interp Ambient"));
		m_objShaderTypeDropdown.addItem(new MyShaderItem(VeinsRenderer.SIMPLE_SHADER_NORM_INTERP_AMBIENT_L_SPEC_BLINN_PHONG, "Ambient Blinn Phong"));
		m_objShaderTypeDropdown.addItem(new MyShaderItem(VeinsRenderer.SIMPLE_SHADER_NORM_INTERP_AMBIENT_L_SPEC_PHONG, "Ambient Phong"));
		m_objShaderTypeDropdown.addItem(new MyShaderItem(VeinsRenderer.SHADER_6, "Shader 6"));
		m_objShaderTypeDropdown.addItem(new MyShaderItem(VeinsRenderer.SHADER_7, "Shader 7"));
		m_objShaderTypeDropdown.addItem(new MyShaderItem(VeinsRenderer.SHADER_8, "Shader 8"));
		m_objShaderTypeDropdown.addItem(new MyShaderItem(VeinsRenderer.FIXED_PIPELINE, "Fixed Pipeline"));

		// init volume renderer control
		m_volumeRendererControl = m_controlContainer.findElementById("SIDE_SETTINGS_MARCHING_VOLUME_RENDERER_CONTROL");
		m_volmueRendererControlHeight = m_volumeRendererControl.getConstraintHeight();

		m_volumeCheckboxSSAO = m_controlContainer.findElementById("ENABLE_SSAO_CHECKBOX").getAttachedInputControl().getControl(CheckboxControl.class);
		m_volumeCheckboxSSAO.getElement().findElementById("checkboxLabel").getAttachedInputControl().getControl(LabelControl.class).setText("SSAO");

		m_volumeCheckboxDof = m_controlContainer.findElementById("ENABLE_DOF_CHECKBOX").getAttachedInputControl().getControl(CheckboxControl.class);
		m_volumeCheckboxDof.getElement().findElementById("checkboxLabel").getAttachedInputControl().getControl(LabelControl.class).setText("Depth of Field");

		NiftyScreenController.InitSlider(m_controlContainer.findElementById("ssao_strength"), 0.0f, 1.0f,1.0f, 0.1f, "SSAO Strength", "%.1f");
		NiftyScreenController.InitSlider(m_controlContainer.findElementById("dof_focus"), 0.0f, 400.0f, 20.0f, 0.1f, "DOF Focus", "%.1f");
		NiftyScreenController.InitSlider(m_controlContainer.findElementById("dof_strength"), 0.0f, 0.02f,0.015f, 0.001f, "DOF Strength", "%.4f");
		
		// Gradient
		de.lessvoid.nifty.controls.CheckBox gradCheckbox = m_controlContainer.findElementById("OVERRIDE_GRADIENT_CHECKBOX").getAttachedInputControl().getControl(CheckboxControl.class);
		gradCheckbox.getElement().findElementById("checkboxLabel").getAttachedInputControl().getControl(LabelControl.class).setText("Override gradient");

		float initialGradVal = 0.463838f;
		VolumeRaycast.m_gradXCustom = initialGradVal;
		VolumeRaycast.m_gradYCustom = initialGradVal;
		VolumeRaycast.m_gradZCustom = initialGradVal;
		
		float initialIsoThreshVal = 0.025f;
		VolumeRaycast.threshold = initialIsoThreshVal;
		NiftyScreenController.InitSlider(m_controlContainer.findElementById("volume_iso_threshold"), 0.0001f, 0.1f, initialIsoThreshVal, 0.00001f, "Threshold (ISO Render Method) ", "%.6f");
		NiftyScreenController.InitSlider(m_controlContainer.findElementById("volume_grad_x"), -1.0f, 1.0f, initialGradVal, 0.00001f, "Grad X", "%.6f");
		NiftyScreenController.InitSlider(m_controlContainer.findElementById("volume_grad_y"), -1.0f, 1.0f, initialGradVal, 0.00001f, "Grad Y", "%.6f");
		NiftyScreenController.InitSlider(m_controlContainer.findElementById("volume_grad_z"), -1.0f, 1.0f, initialGradVal, 0.00001f, "Grad Z", "%.6f");

		
		 de.lessvoid.nifty.controls.DropDown renderMethodType = m_volumeRendererControl.findElementById("VOLUME_RENDERMETHOD_DROPDOWN").getAttachedInputControl().getControl(DropDownControl.class);
		 renderMethodType.addItem(VOLUME_RENDER_METHOD_ISO);
		 renderMethodType.addItem(VOLUME_RENDER_METHOD_ALPHA);
		 renderMethodType.addItem(VOLUME_RENDER_METHOD_MAXMIMUM_PROJECTIOn);
		 renderMethodType.selectItemByIndex(VeinsWindow.settings.volumeRenderMethod);

		 // hide all at start
		showMarchingCubesControl(false);
		showObjControl(false);
		showVolumeRendererControl(false);
	}

	// If menu is open and mouse click is outside this: close
	public void OnMouseClick()
	{
		m_controlContainer.layoutElements(); // refresh because controls get changed

		if (m_isOpen)
			if (m_menuPanel.isMouseInsideElement(Mouse.getX(), Mouse.getY()) == false)
				CloseMenu();
	}

	// Slides menu to right, opening it
	public void OpenMenu()
	{
		// Set visible controls based on last opened model type
		if (VeinsWindow.renderer.veinsModel == null)
		{
			showVolumeRendererControl(false);
			showMarchingCubesControl(false);
			showObjControl(false);
		}
		else if (VeinsWindow.renderer.WasLastModelLoadedFromObj)
		{
			showMarchingCubesControl(false);
			showVolumeRendererControl(false);
			showObjControl(true);
		}
		else if (VeinsWindow.renderer.LastLoadedModelType == ModelType.MARCHING_CUBES)
		{
			showMarchingCubesControl(true);
			showVolumeRendererControl(false);
			showObjControl(false);
		}
		else if (VeinsWindow.renderer.LastLoadedModelType == ModelType.VOLUME_RENDER)
		{
			showVolumeRendererControl(true);
			showMarchingCubesControl(false);
			showObjControl(false);
		}
		else
		{
			showVolumeRendererControl(false);
			showMarchingCubesControl(false);
			showObjControl(false);
		}

		if (m_isOpen)
			return;

		m_isOpen = true;
		m_menuPanel.startEffect(EffectEventId.onCustom, null, "openSettingsSideMenu");
		m_menuPanel.setVisible(true);
		m_closedMenuButtonPanel.setVisible(false);
	}

	// Slides menu to left, closing it
	public void CloseMenu()
	{
		if (m_isOpen == false)
			return;

		m_isOpen = false;
		m_menuPanel.startEffect(EffectEventId.onCustom, null, "closeSettingsSideMenu");
		m_closedMenuButtonPanel.setVisible(true);

	}

	public void OnMenuCloseAnimationFinished()
	{
		m_menuPanel.stopEffect(EffectEventId.onCustom); // must stop otherwise it pops for one frame or dissapears, strange stuff
		m_menuPanel.setVisible(false);
	}

	void showMarchingCubesControl(boolean show)
	{
		if (show == false)
		{
			m_marchingCubesControl.setVisible(false);
			m_marchingCubesControl.setConstraintHeight(new SizeValue(0, SizeValueType.Pixel));
		}
		else
		{
			m_marchingCubesControl.setVisible(true);
			m_marchingCubesControl.setConstraintHeight(m_marchingCubesControlHeight);
		}

		m_controlContainer.layoutElements();
	}

	void showVolumeRendererControl(boolean show)
	{
		if (show == false)
		{
			m_volumeRendererControl.setVisible(false);
			m_volumeRendererControl.setConstraintHeight(new SizeValue(0, SizeValueType.Pixel));
		}
		else
		{
			m_volumeRendererControl.setVisible(true);
			m_volumeRendererControl.setConstraintHeight(m_volmueRendererControlHeight);
		}

		m_controlContainer.layoutElements();
	}

	void showObjControl(boolean show)
	{
		if (show == false)
		{
			m_objControl.setVisible(false);
			m_objControl.setConstraintHeight(new SizeValue(0, SizeValueType.Pixel));
		}
		else
		{
			m_objControl.setVisible(true);
			m_objControl.setConstraintHeight(m_objControlHeight);
		}

		m_controlContainer.layoutElements();
	}
}
