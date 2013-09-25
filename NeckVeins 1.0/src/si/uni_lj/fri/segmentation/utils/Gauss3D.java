package si.uni_lj.fri.segmentation.utils;
public class Gauss3D {
	private final static int AXIS_X = 0;
	private final static int AXIS_Y = 1;
	private final static int AXIS_Z = 2;

	private static double[] gauss1DKernel(int size, double sigma) {
		double[] kernel = new double[size];
		double sum = 0;
		for (int i = 0; i < size; i++) {
			double x = i - size / 2;
			kernel[i] = (1 / (Math.sqrt(2 * Math.PI) * sigma)) * Math.exp(-(x * x) / (2 * sigma * sigma));
			sum += kernel[i];
		}
		for (int i = 0; i < kernel.length; i++) {
			kernel[i] /= sum;
		}

		return kernel;
	}

	private static void convolution1D(double[] kernel, double[][][] ct, int axis) {
		if (axis == AXIS_X) {
			for (int i = 0; i < ct[0][0].length; i++) {
				for (int j = 0; j < ct.length; j++) {
					for (int k = 0; k < ct[0].length; k++) {
						double value = 0;
						for (int l = 0; l < kernel.length; l++) {
							if (k + l - (int) (kernel.length / 2) < 0
									|| k + l - (int) (kernel.length / 2) >= ct[0].length)
								continue;
							value += ct[j][k + l - (int) (kernel.length / 2)][i] * kernel[kernel.length - l - 1];
						}
						ct[j][k][i] = value;
					}
				}
			}
		} else if (axis == AXIS_Y) {
			for (int i = 0; i < ct[0][0].length; i++) {
				for (int j = 0; j < ct.length; j++) {
					for (int k = 0; k < ct[0].length; k++) {
						double value = 0;
						for (int l = 0; l < kernel.length; l++) {
							if (j + l - (int) (kernel.length / 2) < 0 || j + l - (int) (kernel.length / 2) >= ct.length)
								continue;
							value += ct[j + l - (int) (kernel.length / 2)][k][i] * kernel[kernel.length - l - 1];
						}
						ct[j][k][i] = value;
					}
				}
			}
		} else if (axis == AXIS_Z) {
			for (int i = 0; i < ct[0][0].length; i++) {
				for (int j = 0; j < ct.length; j++) {
					for (int k = 0; k < ct[0].length; k++) {
						double value = 0;
						for (int l = 0; l < kernel.length; l++) {
							if (i + l - (int) (kernel.length / 2) < 0
									|| i + l - (int) (kernel.length / 2) >= ct[0][0].length)
								continue;
							value += ct[j][k][i + l - (int) (kernel.length / 2)] * kernel[kernel.length - l - 1];
						}
						ct[j][k][i] = value;
					}
				}
			}
		}
	}

	public static void gauss3D(int sizeX, int sizeY, int sizeZ, double sigmaX, double sigmaY, double sigmaZ,
			double[][][] ct) {

		double[] kernelX = gauss1DKernel(sizeX, sigmaX);
		double[] kernelY = gauss1DKernel(sizeY, sigmaY);
		double[] kernelZ = gauss1DKernel(sizeZ, sigmaZ);

		convolution1D(kernelX, ct, AXIS_X);
		convolution1D(kernelY, ct, AXIS_Y);
		convolution1D(kernelZ, ct, AXIS_Z);
	}
}
