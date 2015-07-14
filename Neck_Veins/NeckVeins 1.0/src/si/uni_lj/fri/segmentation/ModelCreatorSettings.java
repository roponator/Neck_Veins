package si.uni_lj.fri.segmentation;

/*
 * Contains settings that affects how models will be generated.
 */
public class ModelCreatorSettings {
	
	// If false marching cubes is used, if true MPUI is used
	public static boolean useMPUIForMeshFromVolumetricDataGeneration = false;
	
	// If larger than 0 subdivision is used
	public static int numSubdivisionsOfVoxels = 0;
	

}
