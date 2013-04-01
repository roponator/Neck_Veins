package si.uni_lj.fri.veins3D.gui;

import java.io.File;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

import si.uni_lj.fri.veins3D.gui.render.VeinsRenderer;
import tmp.CreditsAndHelpTMP;
import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.FileSelector;
import de.matthiasmann.twl.FileSelector.Callback;
import de.matthiasmann.twl.GUI;
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

	public VeinsFrame() throws LWJGLException {
		getDisplayModes();
		initGUI();
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
	}

	private void initFileSelector() {
		fileSelector = new FileSelector();
		fileSelector.setTheme("fileselector");
		fileSelector.setVisible(false);
		JavaFileSystemModel fsm = JavaFileSystemModel.getInstance();
		fileSelector.setFileSystemModel(fsm);
		Callback cb = new Callback() {
			@Override
			public void filesSelected(Object[] files) {
				setButtonsEnabled(true);
				fileSelector.setVisible(false);
				File file = (File) files[0];
				System.out.println("\nOpening file: " + file.getAbsolutePath());
				VeinsRenderer renderer = (VeinsRenderer) VeinsFrame.this.getGUI().getRenderer();
				renderer.loadModel(file.getAbsolutePath());
			}

			@Override
			public void canceled() {
				setButtonsEnabled(true);
				fileSelector.setVisible(false);
			}
		};
		fileSelector.addCallback(cb);
		add(fileSelector);
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
		stamHelp = new SimpleTextAreaModel(CreditsAndHelpTMP.helpString);
		stamCredits = new SimpleTextAreaModel(CreditsAndHelpTMP.creditsString);
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

	public void setButtonsEnabled(boolean enabled) {
		isDialogOpened = enabled;
		openButton.setEnabled(enabled);
		displayModesButton.setEnabled(enabled);
		helpButton.setEnabled(enabled);
		creditsButton.setEnabled(enabled);
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
		int openHeight = Math.max(25, VeinsWindow.settings.resHeight / 18);
		int widthBy6 = VeinsWindow.settings.resWidth / 6 + 1;
		openButton.setSize(widthBy6, openHeight);
		openButton.setPosition(0, 0);
		displayModesButton.setPosition(widthBy6, 0);
		displayModesButton.setSize(widthBy6, openHeight);

		if (VeinsWindow.settings.stereoEnabled) {
			stereoToggleButton.setPosition(widthBy6 * 2, 0);
			stereoToggleButton.setSize(widthBy6, openHeight / 2);
			stereoScrollbar.setPosition(widthBy6 * 2, openHeight / 2);
			stereoScrollbar.setSize(widthBy6, openHeight / 2);
			// stereoScrollbar.setMinSize(VeinsWindow.settings.resWidth/36,
			// openHeight);
		} else {
			stereoToggleButton.setPosition(widthBy6 * 2, 0);
			stereoToggleButton.setSize(widthBy6, openHeight);

			stereoScrollbar.setPosition(widthBy6 * 2, openHeight);
			stereoScrollbar.setSize(widthBy6, openHeight);
		}

		helpButton.setPosition(widthBy6 * 3, 0);
		helpButton.setSize(widthBy6, openHeight);
		creditsButton.setPosition(widthBy6 * 4, 0);
		creditsButton.setSize(widthBy6, openHeight);
		exitButton.setPosition(widthBy6 * 5, 0);
		exitButton.setSize(VeinsWindow.settings.resWidth - widthBy6 * 5, openHeight);

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

	public boolean isDialogOpened() {
		return isDialogOpened;
	}

}
