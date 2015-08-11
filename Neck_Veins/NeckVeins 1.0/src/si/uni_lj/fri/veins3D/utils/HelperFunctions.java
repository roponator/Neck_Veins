package si.uni_lj.fri.veins3D.utils;

import java.io.File;
import java.io.FilenameFilter;

public class HelperFunctions
{
	// Returns all directories in the given folder, 
	// returns null if no directories exist
	public static String[] GetDirectoriesInFolder(String path)
	{
		File file = new File(path);
		String[] directories = file.list(new FilenameFilter()
		{
			@Override
			public boolean accept(File current, String name)
			{
				return new File(current, name).isDirectory();
			}
		});
		
		return directories;
	}
}
