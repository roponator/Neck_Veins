package si.uni_lj.fri.veins3D.gui;

import java.io.File;


import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

import si.uni_lj.fri.veins3D.gui.render.VeinsRenderer;
import si.uni_lj.fri.veins3D.utils.ModelLoaderUtil;
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
	private Button open;
	private Button exit;
	private Scrollbar stereoScrollbar;
	private ToggleButton stereoToggleButton;
	private ToggleButton help;
	private TextArea helpTextArea;
	private ScrollPane helpScrollPane;
	private SimpleTextAreaModel stamHelp;
	private ToggleButton credits;
	private SimpleTextAreaModel stamCredits;
	private Button displayModesButton;
	private int selectedResolution;
	private Button okayVideoSetting;
	private Button cancelVideoSetting;
	private ToggleButton fullscreenToggle;
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
				ModelLoaderUtil.loadModel(file.getAbsolutePath(), renderer);
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
		open = new Button("Open...");
		open.setTheme("button");
		open.setTooltipContent("Open the dialog with the file chooser to select an .obj file.");
		open.addCallback(new Runnable() {
			public void run() {
				openAFile();
			}
		});
		add(open);

	}

	private void initExitButton() {
		exit = new Button("Exit");
		exit.setTheme("button");
		exit.setTooltipContent("Terminates this program.");
		exit.addCallback(new Runnable() {
			public void run() {
				((VeinsWindow) VeinsFrame.this.getGUI().getParent()).exitProgram(0);
			}
		});
		add(exit);
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
		help = new ToggleButton("Help");
		help.setTheme("togglebutton");
		help.setTooltipContent("Shows controls.");
		help.addCallback(new Runnable() {
			public void run() {
				if (help.isActive()) {
					helpTextArea.setModel(stamHelp);
					helpScrollPane.setVisible(true);
					setButtonsEnabled(false);
					help.setEnabled(true);
				} else {
					helpScrollPane.setVisible(false);
					setButtonsEnabled(true);
				}
			}
		});
		add(help);
	}

	private void initCreditsToggleButton() {
		credits = new ToggleButton("Licensing");
		credits.setTheme("togglebutton");
		credits.setTooltipContent("Shows authorship and licensing information.");
		credits.addCallback(new Runnable() {
			public void run() {
				if (credits.isActive()) {
					helpTextArea.setModel(stamCredits);
					helpScrollPane.setVisible(true);
					setButtonsEnabled(false);
					credits.setEnabled(true);
				} else {
					helpScrollPane.setVisible(false);
					setButtonsEnabled(true);
				}
			}
		});
		add(credits);
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

	/**
	 * TODO rename
	 */
	private void initVideoSettingsButtons() {
		okayVideoSetting = new Button("Okay");
		cancelVideoSetting = new Button("Cancel");
		fullscreenToggle = new ToggleButton("Toggle Fullscreen");

		okayVideoSetting.setVisible(false);
		cancelVideoSetting.setVisible(false);
		fullscreenToggle.setVisible(false);

		okayVideoSetting.setTheme("button");
		cancelVideoSetting.setTheme("button");
		fullscreenToggle.setTheme("togglebutton");
		okayVideoSetting.addCallback(new Runnable() {
			@Override
			public void run() {
				confirmVideoSetting();
			}
		});
		cancelVideoSetting.addCallback(new Runnable() {
			@Override
			public void run() {
				cancelVideoSetting();
			}
		});
		fullscreenToggle.addCallback(new Runnable() {
			@Override
			public void run() {
				if (fullscreenToggle.isActive()) {
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
				fullscreenToggle.setActive(true);
			} catch (LWJGLException e) {
				e.printStackTrace();
			}
		} else {
			try {
				Display.setFullscreen(false);
				fullscreenToggle.setActive(false);
			} catch (LWJGLException e) {
				e.printStackTrace();
			}
		}
		add(okayVideoSetting);
		add(cancelVideoSetting);
		add(fullscreenToggle);
	}

	/**
	 * TODO rename, refactor a little bit
	 */
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
		open.setEnabled(enabled);
		displayModesButton.setEnabled(enabled);
		help.setEnabled(enabled);
		credits.setEnabled(enabled);
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
		okayVideoSetting.setVisible(true);
		cancelVideoSetting.setVisible(true);
		displayModeListBox.setVisible(true);
		fullscreenToggle.setVisible(true);
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
		okayVideoSetting.setVisible(false);
		cancelVideoSetting.setVisible(false);
		displayModeListBox.setVisible(false);
		fullscreenToggle.setVisible(false);
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
		okayVideoSetting.setVisible(false);
		cancelVideoSetting.setVisible(false);
		displayModeListBox.setVisible(false);
		fullscreenToggle.setVisible(false);
		setButtonsEnabled(true);
	}

	/**
	 * @since 0.4
	 * @version 0.4
	 */
	@Override
	protected void layout() {
		open.adjustSize();
		displayModesButton.adjustSize();
		stereoToggleButton.adjustSize();
		help.adjustSize();
		credits.adjustSize();
		exit.adjustSize();
		int openHeight = Math.max(25, VeinsWindow.settings.resHeight / 18);
		int widthBy6 = VeinsWindow.settings.resWidth / 6 + 1;
		open.setSize(widthBy6, openHeight);
		open.setPosition(0, 0);
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

		help.setPosition(widthBy6 * 3, 0);
		help.setSize(widthBy6, openHeight);
		credits.setPosition(widthBy6 * 4, 0);
		credits.setSize(widthBy6, openHeight);
		exit.setPosition(widthBy6 * 5, 0);
		exit.setSize(VeinsWindow.settings.resWidth - widthBy6 * 5, openHeight);

		int rlWidth = VeinsWindow.settings.resWidth * 8 / 10;
		int rlHeight = VeinsWindow.settings.resHeight * 6 / 10;
		displayModeListBox.setSize(rlWidth, rlHeight);
		displayModeListBox.setPosition(VeinsWindow.settings.resWidth / 2 - rlWidth / 2,
				VeinsWindow.settings.resHeight / 6);

		fullscreenToggle.adjustSize();
		int fullToggleWidth = Math.max(fullscreenToggle.getWidth(), VeinsWindow.settings.resWidth / 6);
		fullscreenToggle.setSize(fullToggleWidth, openHeight);
		fullscreenToggle.setPosition(VeinsWindow.settings.resWidth / 2 - rlWidth / 2,
				VeinsWindow.settings.resHeight * 19 / 24);

		cancelVideoSetting.adjustSize();
		int cancelVideoSettingWidth = Math.max(cancelVideoSetting.getWidth(), VeinsWindow.settings.resWidth / 6);
		cancelVideoSetting.setSize(cancelVideoSettingWidth, openHeight);
		cancelVideoSetting.setPosition(VeinsWindow.settings.resWidth / 2 + rlWidth / 2 - cancelVideoSettingWidth,
				VeinsWindow.settings.resHeight * 19 / 24);

		okayVideoSetting.adjustSize();
		int okayVideoSettingWidth = Math.max(okayVideoSetting.getWidth(), VeinsWindow.settings.resWidth / 6);
		okayVideoSetting.setSize(okayVideoSettingWidth, openHeight);
		okayVideoSetting.setPosition(VeinsWindow.settings.resWidth / 2 + rlWidth / 2 - cancelVideoSettingWidth
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
