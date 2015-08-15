package si.uni_lj.fri.veins3D.utils;

import java.io.File;
import java.io.FilenameFilter;

import org.apache.commons.io.FilenameUtils;

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

	// Returns only the files with the given extensions, null if none
	public static String[] GetFilesInFolder(String path, String[] allowedExtensions)
	{
		File file = new File(path);
		String[] directories = file.list(new FilenameFilter()
		{
			@Override
			public boolean accept(File current, String name)
			{
				File f = new File(current, name);

				boolean hasCorrectExtension = false;
				if (f.isFile())
				{

					for (int i = 0; i < allowedExtensions.length; ++i)
					{
						String ext = FilenameUtils.getExtension(f.getAbsolutePath());
						if (ext.compareTo(allowedExtensions[i]) == 0)
							hasCorrectExtension = true;
					}
				}

				return f.isFile() && hasCorrectExtension;
			}
		});

		return directories;
	}
}
