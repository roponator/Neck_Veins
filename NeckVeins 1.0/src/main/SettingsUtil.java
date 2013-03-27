package main;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SettingsUtil {

	public static void saveSettings(NeckVeinsSettings settings, String title) {
		boolean saveSuccesful = false;
		String saveFileName = title + ".save";
		try {
			FileOutputStream fos = new FileOutputStream(saveFileName, false);
			try {
				ObjectOutputStream saveOutStream = new ObjectOutputStream(fos);
				saveOutStream.writeObject(settings);
				saveOutStream.close();
				saveSuccesful = true;
			} catch (IOException ee) {
			}
		} catch (FileNotFoundException e) {
		}
		if (!saveSuccesful)
			System.out.println("SaveUnsuccesful");

	}

	@SuppressWarnings("resource")
	public static NeckVeinsSettings loadSettings(String title) {
		NeckVeinsSettings settings = null;
		boolean loadSuccesful = false;
		String saveFileName = title + ".save";
		try {
			FileInputStream fis = new FileInputStream(saveFileName);
			ObjectInputStream saveInStream = new ObjectInputStream(fis);
			Object aSave = saveInStream.readObject();
			if (aSave instanceof NeckVeinsSettings) {
				settings = (NeckVeinsSettings) aSave;
				loadSuccesful = true;
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (!loadSuccesful) {
			System.out.println("Loading settings file failed. Using default/current settings instead.");
			return null;
		} else {
			System.out.println("Loading settings file succeded.");
			return settings;
		}
	}

}