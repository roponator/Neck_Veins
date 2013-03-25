package main;

import java.io.File;
import java.io.Serializable;

import main.MainFrame.NeckVeinsSettings;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

import de.matthiasmann.twl.FileSelector.Callback;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.FileSelector;
import de.matthiasmann.twl.ListBox;
import de.matthiasmann.twl.ScrollPane;
import de.matthiasmann.twl.Scrollbar;
import de.matthiasmann.twl.TextArea;
import de.matthiasmann.twl.ToggleButton;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.model.ButtonModel;
import de.matthiasmann.twl.model.JavaFileSystemModel;
import de.matthiasmann.twl.model.SimpleChangableListModel;
import de.matthiasmann.twl.textarea.SimpleTextAreaModel;

public class MainFrameRefactored extends Widget{ 
    static class NeckVeinsSettings implements Serializable{//similar to global variables except it can be saved
        private int resWidth, resHeight, bitsPerPixel, frequency;
        private boolean isFpsShown, fullscreen, stereoEnabled;
        private int stereoValue=0;
    }
    
    private static NeckVeinsSettings settings;
	private FileSelector fileSelector;
	private boolean dialogOpened;
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
	private ListBox displayModeListBox;
	private String[] displayModeStrings;
	private DisplayMode[] displayModes;
	private DisplayMode currentDisplayMode;
    
    public MainFrameRefactored() {
    	initGUI();
    }

    /**
     * TODO refactor
     */
	private void initGUI() {
        try {
			displayModes=Display.getAvailableDisplayModes();
		} catch (LWJGLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        setTheme("mainframe");
        displayModeStrings=new String[displayModes.length];
        currentDisplayMode=Display.getDesktopDisplayMode();
        settings=new NeckVeinsSettings();
        settings.isFpsShown=false;
        settings.fullscreen=true;
        settings.stereoEnabled=false;
        settings.stereoValue=0;
        settings.resWidth=currentDisplayMode.getWidth();
        settings.resHeight=currentDisplayMode.getHeight();
        settings.bitsPerPixel=currentDisplayMode.getBitsPerPixel();
        settings.frequency=currentDisplayMode.getFrequency();
		initFileSelector();
		initOpenButton();
		initExitButton();
		initStereoScrollBar();
		initStereoToggleButton();
		initHelpToggleButton();
		initCreditsToggleButton();
		initTextArea();
		initDisplayModesButton();
		initVideoSettingButtons();
		initDisplayModeListBox();
	}



	/**
	 * 
	 */
	private void initFileSelector() {
	    fileSelector = new FileSelector();
        fileSelector.setTheme("fileselector");
        fileSelector.setVisible(false);
        JavaFileSystemModel fsm= JavaFileSystemModel.getInstance();
        fileSelector.setFileSystemModel(fsm);
        Callback cb = new Callback() {
            @Override
            public void filesSelected(Object[] files) {
                setButtonsEnabled(true);
                fileSelector.setVisible(false);
                File file= (File)files[0];
                System.out.println("\nOpening file: "+file.getAbsolutePath());
                // TODO loadModel();
                //loadModel(file.getAbsolutePath());
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
		open.addCallback(new Runnable(){
			public void run(){
				openAFile();
			}
		});
		add(open);
		
	}
	
	private void initExitButton() {
        exit = new Button("Exit");
        exit.setTheme("button");
        exit.setTooltipContent("Terminates this program.");
        exit.addCallback(new Runnable(){
           public void run(){
        	   // TODO exitProgram();
               exitProgram(0);
           }
        });
        add(exit);
	}
	
	private void initStereoScrollBar() {
		stereoScrollbar = new Scrollbar(Scrollbar.Orientation.HORIZONTAL);
		stereoScrollbar.setTheme("hscrollbar");
		stereoScrollbar.setTooltipContent("Sets the distance between eyes.");
		stereoScrollbar.setMinMaxValue(-1000, 1000);
		stereoScrollbar.setValue(settings.stereoValue);
		stereoScrollbar.addCallback(new Runnable(){
			public void run(){
				settings.stereoValue=stereoScrollbar.getValue();
			}
		});
		add(stereoScrollbar);
	}
	
	private void initStereoToggleButton() {
        stereoToggleButton = new ToggleButton("Stereo (3D)");
        stereoToggleButton.setTheme("togglebutton");
        stereoToggleButton.setTooltipContent("Toggles interlaced 3D picture. Requires an appropriate display.");
        stereoToggleButton.addCallback(new Runnable(){
           public void run(){
                   settings.stereoEnabled=true;
                   stereoScrollbar.setVisible(true);
                   invalidateLayout();
               if(stereoToggleButton.isActive()){
               }else{
                   settings.stereoEnabled=false;
                   stereoScrollbar.setVisible(false);
                   invalidateLayout();
               }
           }
        });
        add(stereoToggleButton);
        
        if(settings.stereoEnabled){
            stereoScrollbar.setVisible(true);
            stereoToggleButton.setActive(true);
        }else{
            stereoScrollbar.setVisible(false);
            stereoToggleButton.setActive(false);
        }
	}
	
	private void initHelpToggleButton() {
		 help = new ToggleButton("Help");
	     help.setTheme("togglebutton");
	     help.setTooltipContent("Shows controls.");
	     help.addCallback(new Runnable(){

			public void run(){
		         if(help.isActive()){
		               helpTextArea.setModel(stamHelp);
		               helpScrollPane.setVisible(true);
		               setButtonsEnabled(false);
		               help.setEnabled(true);
		           }else{
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
        credits.addCallback(new Runnable(){
           public void run(){
               if(credits.isActive()){
                   helpTextArea.setModel(stamCredits);
                   helpScrollPane.setVisible(true);
                   setButtonsEnabled(false);
                   credits.setEnabled(true);
               }else{
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
		stamHelp=new SimpleTextAreaModel(CreditsAndHelpTMP.helpString);
		stamCredits=new SimpleTextAreaModel(CreditsAndHelpTMP.creditsString);
		helpTextArea.setModel(stamHelp);
		
		helpScrollPane= new ScrollPane();
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
        displayModesButton.addCallback(new Runnable(){
           public void run(){
               listDisplayModes();
           }
        });
        add(displayModesButton);
	}
	
	/**
	 * TODO rename
	 */
	private void initVideoSettingButtons() {
		okayVideoSetting = new Button("Okay");
		cancelVideoSetting = new Button("Cancel");
		fullscreenToggle = new ToggleButton("Toggle Fullscreen");
		
		okayVideoSetting.setVisible(false);
		cancelVideoSetting.setVisible(false);
		fullscreenToggle.setVisible(false);
		
		okayVideoSetting.setTheme("button");
		cancelVideoSetting.setTheme("button");
		fullscreenToggle.setTheme("togglebutton");
		okayVideoSetting.addCallback(new Runnable(){
		    @Override
		    public void run() {
		    	// TODO
		 // confirmVideoSetting();
		    }
		});
		cancelVideoSetting.addCallback(new Runnable(){
		    @Override
		    public void run() {
		        //TODO
			//cancelVideoSetting();
		    }
		});
		fullscreenToggle.addCallback(new Runnable(){
		    @Override
		    public void run() {
		        if(fullscreenToggle.isActive()){
		            try {
		                Display.setFullscreen(true);
		                Display.setVSyncEnabled(true);
		                settings.fullscreen=true;
		            } catch(LWJGLException e) {
		                e.printStackTrace();
		            }
		        }else{
		            try {
		                settings.fullscreen=false;
		                Display.setFullscreen(false);
		            } catch(LWJGLException e) {
		                e.printStackTrace();
		            }
		        }
		    }
		});
		if(settings.fullscreen){
		    try {
		        Display.setFullscreen(true);
		        Display.setVSyncEnabled(true);
		        fullscreenToggle.setActive(true);
		    } catch(LWJGLException e) {
		        e.printStackTrace();
		    }
		}else{
		    try {
		        Display.setFullscreen(false);
		        fullscreenToggle.setActive(false);
		    } catch(LWJGLException e) {
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
		displayModeListBox = new ListBox<String>();
		displayModeListBox.setTheme("listbox");
		displayModeListBox.setVisible(false);
		add(displayModeListBox);
		
		int i=0;
		selectedResolution=-1;
		for(DisplayMode displayMode:displayModes){
		    if (displayMode.getWidth() == currentDisplayMode.getWidth() && displayMode.getHeight() == currentDisplayMode.getHeight() && displayMode.getBitsPerPixel() == currentDisplayMode.getBitsPerPixel() && displayMode.getFrequency() == currentDisplayMode.getFrequency()) {
		        selectedResolution=i;
		    }
		    displayModeStrings[i++]=String.format("%1$5d x%2$5d x%3$3dbit x%4$3dHz", displayMode.getWidth(), displayMode.getHeight(),displayMode.getBitsPerPixel(),displayMode.getFrequency());
		}
		
		SimpleChangableListModel<String> scListModel= new SimpleChangableListModel<String>();
		for(String str:displayModeStrings){
		    scListModel.addElement(str);
		}
		
		displayModeListBox.setModel(scListModel);
		if(selectedResolution!=-1)displayModeListBox.setSelected(selectedResolution);
	}
	
	
	
	public void setButtonsEnabled(boolean enabled){
	    dialogOpened=enabled;
	    open.setEnabled(enabled);
        displayModesButton.setEnabled(enabled);
        help.setEnabled(enabled);
        credits.setEnabled(enabled);
	}
	
	/**
     * @since 0.4
     * @version 0.4
     */
	public void openAFile(){ 
        fileSelector.setVisible(true);
        setButtonsEnabled(false);
    }
	
	public static void exitProgram(int n){
	   /* TODO
		saveSettings();
	    for(int i=0;i<numberOfShaderPrograms;i++){
	        glDetachShader(shaderPrograms[i], vertexShaders[i]);
	        glDeleteShader(vertexShaders[i]);
	        glDetachShader(shaderPrograms[i], fragmentShaders[i]);
	        glDeleteShader(fragmentShaders[i]);
	        glDeleteProgram(shaderPrograms[i]);
	    }
	    gui.destroy();
	    if(themeManager!=null)themeManager.destroy();
	    Display.destroy();
	    System.exit(n);
	    */
	}
	
	/**
     * @since 0.4
     * @version 0.4
     */
    public void listDisplayModes(){ 
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
	@Override
    protected void layout(){
	    open.adjustSize();
	    displayModesButton.adjustSize();
	    stereoToggleButton.adjustSize();
	    help.adjustSize();
	    credits.adjustSize();
	    exit.adjustSize();
        int openHeight=Math.max(25,settings.resHeight/18);
        int widthBy6=settings.resWidth/6+1;
        open.setSize(widthBy6, openHeight);
        open.setPosition(0, 0);
        displayModesButton.setPosition(widthBy6, 0);
        displayModesButton.setSize(widthBy6, openHeight);
        
        if(settings.stereoEnabled){
            stereoToggleButton.setPosition(widthBy6*2, 0);
            stereoToggleButton.setSize(widthBy6, openHeight/2);
            stereoScrollbar.setPosition(widthBy6*2, openHeight/2);
            stereoScrollbar.setSize(widthBy6, openHeight/2);
            //stereoScrollbar.setMinSize(settings.resWidth/36, openHeight);
        }else{
            stereoToggleButton.setPosition(widthBy6*2, 0);
            stereoToggleButton.setSize(widthBy6, openHeight);
            
            stereoScrollbar.setPosition(widthBy6*2, openHeight);
            stereoScrollbar.setSize(widthBy6, openHeight);
        }
        
        help.setPosition(widthBy6*3, 0);
        help.setSize(widthBy6, openHeight);
        credits.setPosition(widthBy6*4, 0);
        credits.setSize(widthBy6, openHeight);
        exit.setPosition(widthBy6*5, 0);
        exit.setSize(settings.resWidth-widthBy6*5, openHeight);
        
        
        int rlWidth=settings.resWidth*8/10;
        int rlHeight=settings.resHeight*6/10;
        displayModeListBox.setSize(rlWidth,rlHeight);
        displayModeListBox.setPosition(settings.resWidth/2-rlWidth/2, settings.resHeight/6);
        
        fullscreenToggle.adjustSize();
        int fullToggleWidth=Math.max(fullscreenToggle.getWidth(),settings.resWidth/6);
        fullscreenToggle.setSize(fullToggleWidth, openHeight);
        fullscreenToggle.setPosition(settings.resWidth/2-rlWidth/2, settings.resHeight*19/24);
        
        cancelVideoSetting.adjustSize();
        int cancelVideoSettingWidth=Math.max(cancelVideoSetting.getWidth(),settings.resWidth/6);
        cancelVideoSetting.setSize(cancelVideoSettingWidth, openHeight);
        cancelVideoSetting.setPosition(settings.resWidth/2+rlWidth/2-cancelVideoSettingWidth, settings.resHeight*19/24);
        
        okayVideoSetting.adjustSize();
        int okayVideoSettingWidth=Math.max(okayVideoSetting.getWidth(),settings.resWidth/6);
        okayVideoSetting.setSize(okayVideoSettingWidth, openHeight);
        okayVideoSetting.setPosition(settings.resWidth/2+rlWidth/2-cancelVideoSettingWidth-okayVideoSettingWidth, settings.resHeight*19/24);
        
        fileSelector.adjustSize();
        int fsHeight=settings.resHeight*19/24+openHeight-settings.resWidth/2+rlWidth/2;
        fileSelector.setSize(rlWidth,fsHeight);
        fileSelector.setPosition(settings.resWidth/2-rlWidth/2, settings.resHeight/6);
        
        helpScrollPane.setSize(rlWidth, fsHeight);
        helpScrollPane.setPosition(settings.resWidth/2-rlWidth/2, settings.resHeight/6);
        helpTextArea.setSize(rlWidth, fsHeight);
        
    }
    
    
}
