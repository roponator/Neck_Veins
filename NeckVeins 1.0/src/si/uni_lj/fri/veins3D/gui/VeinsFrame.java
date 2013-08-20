package si.uni_lj.fri.veins3D.gui;

import java.io.File;
import java.util.ResourceBundle;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

import si.uni_lj.fri.veins3D.gui.render.VeinsRenderer;
import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.FileSelector;
import de.matthiasmann.twl.FileSelector.Callback2;
import de.matthiasmann.twl.FileTable.Entry;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ListBox;
import de.matthiasmann.twl.ScrollPane;
import de.matthiasmann.twl.Scrollbar;
import de.matthiasmann.twl.TextArea;
import de.matthiasmann.twl.ToggleButton;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.model.JavaFileSystemModel;
import de.matthiasmann.twl.model.SimpleChangableListModel;
import de.matthiasmann.twl.textarea.SimpleTextAreaModel;

public class VeinsFrame extends Widget {
	private FileSelector fileSelector;
	private boolean isDialogOpened;
	private Button openButton;
	private Button exitButton;
	private Scrollbar stereoScrollbar;
	private ToggleButton stereoToggleButton;
	private ToggleButton helpButton;
	private TextArea helpTextArea;
	private ScrollPane helpScrollPane;
	private SimpleTextAreaModel stamHelp;
	private ToggleButton creditsButton;
	private SimpleTextAreaModel stamCredits;
	private Button displayModesButton;
	private int selectedResolution;
	private Button okayVideoSettingButton;
	private Button cancelVideoSettingButton;
	private ToggleButton fullscreenToggleButton;
	private ListBox<String> displayModeListBox;
	private String[] displayModeStrings;
	private DisplayMode[] displayModes;
	private DisplayMode currentDisplayMode;
	// 3Dmouse
	private static ToggleButton mouse3d;
	private Scrollbar sensitivityScrollbar;
	private ToggleButton strong;
	private ToggleButton lockRot;
	private ToggleButton lockTrans;
	private Button camObj;

	public VeinsFrame() throws LWJGLException {
		getDisplayModes();
		initGUI();
		setLanguageSpecific();
		setTheme("mainframe");
	}

	private void getDisplayModes() throws LWJGLException {
		displayModes = Display.getAvailableDisplayModes();
		currentDisplayMode = Display.getDisplayMode();
	}

	private void initGUI() {
		initFileSelector();
		initOpenButton();
		initExitButton();
		initStereoScrollBar();
		initStereoToggleButton();
		initHelpToggleButton();
		initCreditsToggleButton();
		initTextArea();
		initDisplayModesButton();
		initVideoSettingsButtons();
		initDisplayModeListBox();
		init3DmouseButtons();
		initThresholdScroll();
	}

	private void initThresholdScroll() {
		final Scrollbar gaussScrollbar = new Scrollbar(Scrollbar.Orientation.VERTICAL);
		gaussScrollbar.setTheme("vscrollbar");
		gaussScrollbar.setTooltipContent("Sets the sigma value.");
		gaussScrollbar.setMinMaxValue(1, 100);
		gaussScrollbar.setValue(50);
		gaussScrollbar.adjustSize();
		gaussScrollbar.setSize(20, 200);
		gaussScrollbar.setPosition(720, 300);
		final Button apply = new Button("apply");
		apply.setSize(30, 30);
		apply.setPosition(720, 510);
		apply.addCallback(new Runnable() {
			@Override
			public void run() {
				VeinsRenderer renderer = (VeinsRenderer) VeinsFrame.this.getGUI().getRenderer();
				renderer.changeModel(gaussScrollbar.getValue() / 100.0f);

			}
		});
		this.add(gaussScrollbar);
		this.add(apply);
	}

	private void initFileSelector() {
		fileSelector = new FileSelector();
		fileSelector.setTheme("fileselector");
		fileSelector.setVisible(false);
		JavaFileSystemModel fsm = JavaFileSystemModel.getInstance();
		fileSelector.setFileSystemModel(fsm);
		fileSelector.setUserWidgetBottom(initSegmentationOptions());
		fileSelector.getUserWidgetBottom().setEnabled(false);
		fileSelector.setCurrentFolder(new File("D:\\Faks\\Diploma\\workspace\\MHDReaderOpenCL"));
		Callback2 cb = new Callback2() {
			@Override
			public void filesSelected(Object[] files) {
				setButtonsEnabled(true);
				fileSelector.setVisible(false);
				File file = (File) files[0];
				System.out.println("\nOpening file: " + file.getAbsolutePath());

				Widget userWidgetBottom = fileSelector.getUserWidgetBottom();
				Scrollbar gaussScrollbar = (Scrollbar) userWidgetBottom.getChild(0);
				Scrollbar threshScrollbar = (Scrollbar) userWidgetBottom.getChild(3);
				double sigma = (gaussScrollbar.isEnabled()) ? gaussScrollbar.getValue()
						/ (double) gaussScrollbar.getMaxValue() : -1;
				double threshold = (threshScrollbar.isEnabled()) ? threshScrollbar.getValue()
						/ (double) threshScrollbar.getMaxValue() : -1;

				VeinsRenderer renderer = (VeinsRenderer) VeinsFrame.this.getGUI().getRenderer();
				renderer.loadModel(file.getAbsolutePath(), sigma, threshold);
			}

			@Override
			public void canceled() {
				setButtonsEnabled(true);
				fileSelector.setVisible(false);
			}

			@Override
			public void folderChanged(Object arg0) {
			}

			@Override
			public void selectionChanged(Entry[] arg0) {
				if (arg0[0].getExtension().equals("mhd")) {
					fileSelector.getUserWidgetBottom().setEnabled(true);
				} else {
					fileSelector.getUserWidgetBottom().setEnabled(false);
				}
			}
		};
		fileSelector.addCallback(cb);
		add(fileSelector);
	}

	private Widget initSegmentationOptions() {
		/* Gauss Options */
		final Scrollbar gaussScrollbar = new Scrollbar(Scrollbar.Orientation.HORIZONTAL);
		gaussScrollbar.setTheme("hscrollbar");
		gaussScrollbar.setTooltipContent("Sets the sigma value.");
		gaussScrollbar.setMinMaxValue(1, 100);
		gaussScrollbar.setValue(50);
		gaussScrollbar.adjustSize();
		gaussScrollbar.setSize(500, 20);

		final Label sigmaValue = new Label("0.50");
		sigmaValue.setTheme("value-label");
		sigmaValue.adjustSize();
		sigmaValue.setSize(40, 15);
		sigmaValue.setPosition(500, -1);

		final Button enableBtn = new Button("Disable");
		enableBtn.setSize(80, 15);
		enableBtn.setPosition(550, -1);

		gaussScrollbar.addCallback(new Runnable() {
			public void run() {
				sigmaValue.setText(Float.toString(gaussScrollbar.getValue() / 100.0f));
			}
		});
		enableBtn.addCallback(new Runnable() {
			public void run() {
				gaussScrollbar.setEnabled(enableBtn.getText().equals("Enable"));
				enableBtn.setText(enableBtn.getText().equals("Enable") ? "Disable" : "Enable");
			}
		});

		final Scrollbar threshScrollbar = new Scrollbar(Scrollbar.Orientation.HORIZONTAL);
		threshScrollbar.setTheme("hscrollbar");
		threshScrollbar.setTooltipContent("Sets the sigma value.");
		threshScrollbar.setMinMaxValue(1, 100);
		threshScrollbar.setValue(50);
		threshScrollbar.adjustSize();
		threshScrollbar.setSize(500, 20);
		threshScrollbar.setPosition(0, 25);

		final Label threshValue = new Label("0.50");
		threshValue.setTheme("value-label");
		threshValue.adjustSize();
		threshValue.setSize(40, 15);
		threshValue.setPosition(500, 24);
		threshScrollbar.addCallback(new Runnable() {
			public void run() {
				threshValue.setText(Float.toString(threshScrollbar.getValue() / 100.0f));
			}
		});

		final Button autoThreshBtn = new Button("Auto");
		autoThreshBtn.setSize(80, 15);
		autoThreshBtn.setPosition(550, 24);
		autoThreshBtn.addCallback(new Runnable() {
			public void run() {
				threshScrollbar.setEnabled(autoThreshBtn.getText().equals("Manual"));
				autoThreshBtn.setText(autoThreshBtn.getText().equals("Auto") ? "Manual" : "Auto");
			}
		});

		Widget fileOptions = new Widget();
		fileOptions.add(gaussScrollbar);
		fileOptions.add(sigmaValue);
		fileOptions.add(enableBtn);
		fileOptions.add(threshScrollbar);
		fileOptions.add(threshValue);
		fileOptions.add(autoThreshBtn);

		return fileOptions;
	}

	private void initOpenButton() {
		openButton = new Button("Open...");
		openButton.setTheme("button");
		openButton.setTooltipContent("Open the dialog with the file chooser to select an .obj file.");
		openButton.addCallback(new Runnable() {
			public void run() {
				openAFile();
			}
		});
		add(openButton);

	}

	private void initExitButton() {
		exitButton = new Button("Exit");
		exitButton.setTheme("button");
		exitButton.setTooltipContent("Terminates this program.");
		exitButton.addCallback(new Runnable() {
			public void run() {
				((VeinsWindow) VeinsFrame.this.getGUI().getParent()).exitProgram(0);
			}
		});
		add(exitButton);
	}

	private void initStereoScrollBar() {
		stereoScrollbar = new Scrollbar(Scrollbar.Orientation.HORIZONTAL);
		stereoScrollbar.setTheme("hscrollbar");
		stereoScrollbar.setTooltipContent("Sets the distance between eyes.");
		stereoScrollbar.setMinMaxValue(-1000, 1000);
		stereoScrollbar.setValue(VeinsWindow.settings.stereoValue);
		stereoScrollbar.addCallback(new Runnable() {
			public void run() {
				VeinsWindow.settings.stereoValue = stereoScrollbar.getValue();
			}
		});
		add(stereoScrollbar);
	}

	private void initStereoToggleButton() {
		stereoToggleButton = new ToggleButton("Stereo (3D)");
		stereoToggleButton.setTheme("togglebutton");
		stereoToggleButton.setTooltipContent("Toggles interlaced 3D picture. Requires an appropriate display.");
		stereoToggleButton.addCallback(new Runnable() {
			public void run() {
				VeinsWindow.settings.stereoEnabled = true;
				stereoScrollbar.setVisible(true);
				invalidateLayout();
				if (stereoToggleButton.isActive()) {
				} else {
					VeinsWindow.settings.stereoEnabled = false;
					stereoScrollbar.setVisible(false);
					invalidateLayout();
				}
			}
		});
		add(stereoToggleButton);

		if (VeinsWindow.settings.stereoEnabled) {
			stereoScrollbar.setVisible(true);
			stereoToggleButton.setActive(true);
		} else {
			stereoScrollbar.setVisible(false);
			stereoToggleButton.setActive(false);
		}
	}

	private void initHelpToggleButton() {
		helpButton = new ToggleButton("Help");
		helpButton.setTheme("togglebutton");
		helpButton.setTooltipContent("Shows controls.");
		helpButton.addCallback(new Runnable() {
			public void run() {
				if (helpButton.isActive()) {
					helpTextArea.setModel(stamHelp);
					helpScrollPane.setVisible(true);
					setButtonsEnabled(false);
					helpButton.setEnabled(true);
				} else {
					helpScrollPane.setVisible(false);
					setButtonsEnabled(true);
				}
			}
		});
		add(helpButton);
	}

	private void initCreditsToggleButton() {
		creditsButton = new ToggleButton("Licensing");
		creditsButton.setTheme("togglebutton");
		creditsButton.setTooltipContent("Shows authorship and licensing information.");
		creditsButton.addCallback(new Runnable() {
			public void run() {
				if (creditsButton.isActive()) {
					helpTextArea.setModel(stamCredits);
					helpScrollPane.setVisible(true);
					setButtonsEnabled(false);
					creditsButton.setEnabled(true);
				} else {
					helpScrollPane.setVisible(false);
					setButtonsEnabled(true);
				}
			}
		});
		add(creditsButton);
	}

	private void initTextArea() {
		helpTextArea = new TextArea();
		helpTextArea.setTheme("textarea");
		stamHelp = new SimpleTextAreaModel();
		stamCredits = new SimpleTextAreaModel();
		helpTextArea.setModel(stamHelp);

		helpScrollPane = new ScrollPane();
		helpScrollPane.setTheme("scrollpane");
		helpScrollPane.setVisible(false);
		add(helpScrollPane);
		helpScrollPane.setContent(helpTextArea);
	}

	/**
	 * 
	 */
	private void initDisplayModesButton() {
		displayModesButton = new Button("Display Modes...");
		displayModesButton.setTheme("button");
		displayModesButton.setTooltipContent("Open the list with the available display modes.");
		displayModesButton.addCallback(new Runnable() {
			public void run() {
				listDisplayModes();
			}
		});
		add(displayModesButton);
	}

	private void initVideoSettingsButtons() {
		okayVideoSettingButton = new Button("Okay");
		cancelVideoSettingButton = new Button("Cancel");
		fullscreenToggleButton = new ToggleButton("Toggle Fullscreen");

		okayVideoSettingButton.setVisible(false);
		cancelVideoSettingButton.setVisible(false);
		fullscreenToggleButton.setVisible(false);

		okayVideoSettingButton.setTheme("button");
		cancelVideoSettingButton.setTheme("button");
		fullscreenToggleButton.setTheme("togglebutton");
		okayVideoSettingButton.addCallback(new Runnable() {
			@Override
			public void run() {
				confirmVideoSetting();
			}
		});
		cancelVideoSettingButton.addCallback(new Runnable() {
			@Override
			public void run() {
				cancelVideoSetting();
			}
		});
		fullscreenToggleButton.addCallback(new Runnable() {
			@Override
			public void run() {
				if (fullscreenToggleButton.isActive()) {
					try {
						Display.setFullscreen(true);
						Display.setVSyncEnabled(true);
						VeinsWindow.settings.fullscreen = true;
					} catch (LWJGLException e) {
						e.printStackTrace();
					}
				} else {
					try {
						VeinsWindow.settings.fullscreen = false;
						Display.setFullscreen(false);
					} catch (LWJGLException e) {
						e.printStackTrace();
					}
				}
			}
		});
		if (VeinsWindow.settings.fullscreen) {
			try {
				Display.setFullscreen(true);
				Display.setVSyncEnabled(true);
				fullscreenToggleButton.setActive(true);
			} catch (LWJGLException e) {
				e.printStackTrace();
			}
		} else {
			try {
				Display.setFullscreen(false);
				fullscreenToggleButton.setActive(false);
			} catch (LWJGLException e) {
				e.printStackTrace();
			}
		}
		add(okayVideoSettingButton);
		add(cancelVideoSettingButton);
		add(fullscreenToggleButton);
	}

	private void initDisplayModeListBox() {
		displayModeStrings = new String[displayModes.length];
		displayModeListBox = new ListBox<String>();
		displayModeListBox.setTheme("listbox");
		displayModeListBox.setVisible(false);
		add(displayModeListBox);

		int i = 0;
		selectedResolution = -1;
		for (DisplayMode displayMode : displayModes) {
			if (displayMode.getWidth() == currentDisplayMode.getWidth()
					&& displayMode.getHeight() == currentDisplayMode.getHeight()
					&& displayMode.getBitsPerPixel() == currentDisplayMode.getBitsPerPixel()
					&& displayMode.getFrequency() == currentDisplayMode.getFrequency()) {
				selectedResolution = i;
			}
			displayModeStrings[i++] = String.format("%1$5d x%2$5d x%3$3dbit x%4$3dHz", displayMode.getWidth(),
					displayMode.getHeight(), displayMode.getBitsPerPixel(), displayMode.getFrequency());
		}

		SimpleChangableListModel<String> scListModel = new SimpleChangableListModel<String>();
		for (String str : displayModeStrings) {
			scListModel.addElement(str);
		}

		displayModeListBox.setModel(scListModel);
		if (selectedResolution != -1)
			displayModeListBox.setSelected(selectedResolution);
	}

	private void init3DmouseButtons() {
		mouse3d = new ToggleButton("3d Mouse");
		mouse3d.setTheme("togglebutton");
		mouse3d.setTooltipContent("3d Mouse settings");
		mouse3d.addCallback(new Runnable() {
			public void run() {
				mouse3d.setEnabled(VeinsWindow.joystick.connected());
				mouseSettingsVisible(mouse3d.isActive() && mouse3d.isEnabled());
			}
		});
		add(mouse3d);

		sensitivityScrollbar = new Scrollbar(Scrollbar.Orientation.HORIZONTAL);
		sensitivityScrollbar.setTheme("hscrollbar");
		sensitivityScrollbar.setTooltipContent("Sets the sensitivity of the mouse.");
		sensitivityScrollbar.setMinMaxValue(1, 200);
		sensitivityScrollbar.setValue(201 - VeinsWindow.settings.sensitivity);
		sensitivityScrollbar.addCallback(new Runnable() {
			public void run() {
				VeinsWindow.settings.sensitivity = (201 - sensitivityScrollbar.getValue());
			}
		});
		add(sensitivityScrollbar);

		if (VeinsWindow.settings.mSelected)
			camObj = new Button("Camera active");
		else
			camObj = new Button("Object active");
		camObj.setTheme("button");
		camObj.setTooltipContent("Toggle between camera or object");
		camObj.addCallback(new Runnable() {
			public void run() {
				if (VeinsWindow.settings.mSelected) {
					VeinsWindow.settings.mSelected = false;
					camObj.setText("Object active");
				} else {
					VeinsWindow.settings.mSelected = true;
					camObj.setText("Camera active");
				}
			}
		});
		add(camObj);

		strong = new ToggleButton("Strong axis");
		strong.setActive(VeinsWindow.settings.mStrong);
		strong.setTheme("togglebutton");
		strong.setTooltipContent("React only on strong axis of the mouse");
		strong.addCallback(new Runnable() {
			public void run() {
				VeinsWindow.settings.mStrong = strong.isActive();
			}
		});
		add(strong);

		lockRot = new ToggleButton("Rotations");
		lockRot.setActive(VeinsWindow.settings.mRot);
		lockRot.setTheme("togglebutton");
		lockRot.setTooltipContent("Locks rotational axis");
		lockRot.addCallback(new Runnable() {
			public void run() {
				VeinsWindow.settings.mRot = lockRot.isActive();
			}
		});
		add(lockRot);

		lockTrans = new ToggleButton("Translations");
		lockTrans.setActive(VeinsWindow.settings.mTrans);
		lockTrans.setTheme("togglebutton");
		lockTrans.setTooltipContent("Locks translationals axis");
		lockTrans.addCallback(new Runnable() {
			public void run() {
				VeinsWindow.settings.mTrans = lockTrans.isActive();
			}
		});
		add(lockTrans);

	}

	public void setButtonsEnabled(boolean enabled) {
		isDialogOpened = enabled;
		openButton.setEnabled(enabled);
		displayModesButton.setEnabled(enabled);
		helpButton.setEnabled(enabled);
		creditsButton.setEnabled(enabled);
	}

	public void mouseSettingsVisible(boolean visible) {
		strong.setVisible(visible);
		lockRot.setVisible(visible);
		lockTrans.setVisible(visible);
		camObj.setVisible(visible);
		sensitivityScrollbar.setVisible(visible);
	}

	/**
	 * @since 0.4
	 * @version 0.4
	 */
	public void openAFile() {
		fileSelector.setVisible(true);
		setButtonsEnabled(false);
	}

	/**
	 * @since 0.4
	 * @version 0.4
	 */
	public void listDisplayModes() {
		okayVideoSettingButton.setVisible(true);
		cancelVideoSettingButton.setVisible(true);
		displayModeListBox.setVisible(true);
		fullscreenToggleButton.setVisible(true);
		setButtonsEnabled(false);
		displayModeListBox.setSelected(selectedResolution);
	}

	/**
	 * @since 0.4
	 * @version 0.4
	 */
	public void confirmVideoSetting() {
		GUI gui = getGUI();
		VeinsRenderer renderer = (VeinsRenderer) gui.getRenderer();
		DisplayMode[] displayModes = ((VeinsWindow) gui.getParent()).getDisplayModes();
		DisplayMode currentDisplayMode = ((VeinsWindow) gui.getParent()).getCurrentDisplayMode();
		okayVideoSettingButton.setVisible(false);
		cancelVideoSettingButton.setVisible(false);
		displayModeListBox.setVisible(false);
		fullscreenToggleButton.setVisible(false);
		setButtonsEnabled(true);
		if (selectedResolution != displayModeListBox.getSelected()) {
			selectedResolution = displayModeListBox.getSelected();
			currentDisplayMode = displayModes[selectedResolution];
			try {
				Display.setDisplayMode(currentDisplayMode);
				VeinsWindow.settings.resWidth = currentDisplayMode.getWidth();
				VeinsWindow.settings.resHeight = currentDisplayMode.getHeight();
			} catch (LWJGLException e) {
				e.printStackTrace();
			}
			renderer.setupView();
			renderer.syncViewportSize();
			invalidateLayout();
		}
	}

	/**
	 * @since 0.4
	 * @version 0.4
	 */
	public void cancelVideoSetting() {
		okayVideoSettingButton.setVisible(false);
		cancelVideoSettingButton.setVisible(false);
		displayModeListBox.setVisible(false);
		fullscreenToggleButton.setVisible(false);
		setButtonsEnabled(true);
	}

	/**
	 * @since 0.4
	 * @version 0.4
	 */
	@Override
	protected void layout() {
		openButton.adjustSize();
		displayModesButton.adjustSize();
		stereoToggleButton.adjustSize();
		helpButton.adjustSize();
		creditsButton.adjustSize();
		exitButton.adjustSize();
		mouse3d.adjustSize();
		strong.adjustSize();
		lockRot.adjustSize();
		lockTrans.adjustSize();

		int openHeight = Math.max(25, VeinsWindow.settings.resHeight / 18);
		int widthBy7 = VeinsWindow.settings.resWidth / 7 + 1;
		openButton.setSize(widthBy7, openHeight);
		openButton.setPosition(0, 0);
		displayModesButton.setPosition(widthBy7, 0);
		displayModesButton.setSize(widthBy7, openHeight);

		if (VeinsWindow.settings.stereoEnabled) {
			stereoToggleButton.setPosition(widthBy7 * 2, 0);
			stereoToggleButton.setSize(widthBy7, openHeight / 2);
			stereoScrollbar.setPosition(widthBy7 * 2, openHeight / 2);
			stereoScrollbar.setSize(widthBy7, openHeight / 2);
			// stereoScrollbar.setMinSize(VeinsWindow.settings.resWidth/36,
			// openHeight);
		} else {
			stereoToggleButton.setPosition(widthBy7 * 2, 0);
			stereoToggleButton.setSize(widthBy7, openHeight);

			stereoScrollbar.setPosition(widthBy7 * 2, openHeight);
			stereoScrollbar.setSize(widthBy7, openHeight);
		}

		helpButton.setPosition(widthBy7 * 3, 0);
		helpButton.setSize(widthBy7, openHeight);
		creditsButton.setPosition(widthBy7 * 4, 0);
		creditsButton.setSize(widthBy7, openHeight);
		mouse3d.setPosition(widthBy7 * 5, 0);
		mouse3d.setSize(widthBy7, openHeight);
		exitButton.setPosition(widthBy7 * 6, 0);
		exitButton.setSize(VeinsWindow.settings.resWidth - widthBy7 * 6, openHeight);

		strong.setPosition(0, openHeight);
		strong.setSize(widthBy7 * 2, openHeight);
		lockRot.setPosition(0, openHeight * 2);
		lockRot.setSize(widthBy7, openHeight);
		lockTrans.setPosition(widthBy7, openHeight * 2);
		lockTrans.setSize(widthBy7, openHeight);
		camObj.setPosition(0, openHeight * 3);
		camObj.setSize(widthBy7 * 2, openHeight);
		sensitivityScrollbar.setPosition(0, openHeight * 4);
		sensitivityScrollbar.setSize(widthBy7 * 2, openHeight / 2);

		mouseSettingsVisible(mouse3d.isActive());
		mouse3d.setEnabled(VeinsWindow.joystick.connected());

		int rlWidth = VeinsWindow.settings.resWidth * 8 / 10;
		int rlHeight = VeinsWindow.settings.resHeight * 6 / 10;
		displayModeListBox.setSize(rlWidth, rlHeight);
		displayModeListBox.setPosition(VeinsWindow.settings.resWidth / 2 - rlWidth / 2,
				VeinsWindow.settings.resHeight / 6);

		fullscreenToggleButton.adjustSize();
		int fullToggleWidth = Math.max(fullscreenToggleButton.getWidth(), VeinsWindow.settings.resWidth / 6);
		fullscreenToggleButton.setSize(fullToggleWidth, openHeight);
		fullscreenToggleButton.setPosition(VeinsWindow.settings.resWidth / 2 - rlWidth / 2,
				VeinsWindow.settings.resHeight * 19 / 24);

		cancelVideoSettingButton.adjustSize();
		int cancelVideoSettingWidth = Math.max(cancelVideoSettingButton.getWidth(), VeinsWindow.settings.resWidth / 6);
		cancelVideoSettingButton.setSize(cancelVideoSettingWidth, openHeight);
		cancelVideoSettingButton.setPosition(VeinsWindow.settings.resWidth / 2 + rlWidth / 2 - cancelVideoSettingWidth,
				VeinsWindow.settings.resHeight * 19 / 24);

		okayVideoSettingButton.adjustSize();
		int okayVideoSettingWidth = Math.max(okayVideoSettingButton.getWidth(), VeinsWindow.settings.resWidth / 6);
		okayVideoSettingButton.setSize(okayVideoSettingWidth, openHeight);
		okayVideoSettingButton.setPosition(VeinsWindow.settings.resWidth / 2 + rlWidth / 2 - cancelVideoSettingWidth
				- okayVideoSettingWidth, VeinsWindow.settings.resHeight * 19 / 24);

		fileSelector.adjustSize();
		int fsHeight = VeinsWindow.settings.resHeight * 19 / 24 + openHeight - VeinsWindow.settings.resWidth / 2
				+ rlWidth / 2;
		fileSelector.setSize(rlWidth, fsHeight);
		fileSelector.setPosition(VeinsWindow.settings.resWidth / 2 - rlWidth / 2, VeinsWindow.settings.resHeight / 6);

		helpScrollPane.setSize(rlWidth, fsHeight);
		helpScrollPane.setPosition(VeinsWindow.settings.resWidth / 2 - rlWidth / 2, VeinsWindow.settings.resHeight / 6);
		helpTextArea.setSize(rlWidth, fsHeight);
	}

	public void setLanguageSpecific() {
		ResourceBundle labels = ResourceBundle.getBundle("inter/LabelsBundle", VeinsWindow.settings.locale);

		openButton.setText(labels.getString("openBtnLabel"));
		openButton.setTooltipContent(labels.getString("openBtnTooltip"));

		exitButton.setText(labels.getString("exitBtnLabel"));
		exitButton.setTooltipContent(labels.getString("exitBtnTooltip"));

		stereoScrollbar.setTooltipContent(labels.getString("stereoScrollbarTooltip"));
		stereoToggleButton.setText(labels.getString("stereoBtnLabel"));
		stereoToggleButton.setTooltipContent(labels.getString("stereoBtnTooltip"));

		helpButton.setText(labels.getString("helpBtnLabel"));
		helpButton.setTooltipContent(labels.getString("helpBtnTooltip"));
		creditsButton.setText(labels.getString("creditsBtnLabel"));
		creditsButton.setTooltipContent(labels.getString("creditsBtnTooltip"));

		displayModesButton.setText(labels.getString("displayModesBtnLabel"));
		displayModesButton.setTooltipContent(labels.getString("displayModesBtnTooltip"));

		okayVideoSettingButton.setText(labels.getString("okayBtnLabel"));
		cancelVideoSettingButton.setText(labels.getString("cancelBtnLabel"));
		fullscreenToggleButton.setText(labels.getString("fullscreenBtnLabel"));

		// TODO: Translate 3dMouse buttons
		mouse3d.setText(labels.getString("mouse3dBtnLabel"));
		mouse3d.setTooltipContent(labels.getString("mouse3dBtnTooltip"));

		strong.setText(labels.getString("strongBtnLabel"));
		lockRot.setText(labels.getString("lockRotBtnLabel"));
		lockTrans.setText(labels.getString("lockTransBtnLabel"));

		ResourceBundle credits = ResourceBundle.getBundle("inter/Credits", VeinsWindow.settings.locale);
		ResourceBundle help = ResourceBundle.getBundle("inter/Help", VeinsWindow.settings.locale);

		stamHelp.setText(help.getString("help"));
		stamCredits.setText(credits.getString("credits"));

		ResourceBundle.clearCache();
	}

	public boolean isDialogOpened() {
		return isDialogOpened;
	}

}
