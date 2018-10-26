package cs107KNN;

public class KNN {
	public static void main(String[] args) {
		byte b1 = 40; // 00101000
		byte b2 = 20; // 00010100
		byte b3 = 10; // 00001010
		byte b4 = 5; // 00000101

		// [00101000 | 00010100 | 00001010 | 00000101] = 672401925
		int result = extractInt(b1, b2, b3, b4);
		System.out.println(result);
		
		
		//test parseIDXimages
		byte[] imagesRaw = Helpers.readBinaryFile("datasets/10-per-digit_images_train");
		byte[][][] imagesTrain = parseIDXimages(imagesRaw);
		
		KNNTest.parsingTest();
		KNNTest.invertedSimilarityTest();
		
		System.out.println("Hi");
		/** System.out.println(labelsTrain.length); */
		
		KNNTest.electLabelTest();

		
	}

	/**
	 * Composes four bytes into an integer using big endian convention.
	 *
	 * @param bXToBY The byte containing the bits to store between positions X and Y
	 * 
	 * @return the integer having form [ b31ToB24 | b23ToB16 | b15ToB8 | b7ToB0 ]
	 */
	public static int extractInt(byte b31ToB24, byte b23ToB16, byte b15ToB8, byte b7ToB0) {
		// TODO: Implémenter
		int i = 0;
		int j = 0;
		String bytesAsString = Helpers.byteToBinaryString(b31ToB24) + Helpers.byteToBinaryString(b23ToB16) + Helpers.byteToBinaryString(b15ToB8) + Helpers.byteToBinaryString(b7ToB0);
		for (int c = bytesAsString.length(); c > 0; c--) {
			int num = bytesAsString.charAt(j) - '0';
			/** if (num == 48) {
				num = 0;
			}
			else if (num == 49) {
				num = 1;
			}*/
			i += (int) num * Math.pow(2, (c-1));
			j++;
		}
		return i;
	}

	/**
	 * Parses an IDX file containing images
	 *
	 * @param data the binary content of the file
	 *
	 * @return A tensor of images
	 */
	public static byte[][][] parseIDXimages(byte[] data) {
		
		int magicNumber = extractInt(data[0], data[1], data[2], data[3]);
		int nbImages = extractInt(data[4], data[5], data[6], data[7]);
		int hauteur = extractInt(data[8], data[9], data[10], data[11]);
		int largeur = extractInt(data[12], data[13], data[14], data[15]);
		
		byte [][][] img = new byte [nbImages][hauteur][largeur];
		
		
		if(magicNumber == 2051) {
			
			//initialisation d'un compteur parcourant data
			int n = 16;
		
				for(int i = 0; i<img.length; ++i) { // nb d'images
			
					for(int k = 0; k<img[0].length; ++k) { // parcourir hauteur
				
						for(int j = 0; j<img[0][0].length; ++j) { // parcourir largeur
						
							byte pixelValue = (byte) ((data[n]& 0xFF)-128); // interpretation du byte en signed
							img [i][k][j] = pixelValue;
							++n;
					
						}
					}
				}
			
			return img;
		}
		return null;
	}

	/**
	 * Parses an idx images containing labels
	 *
	 * @param data the binary content of the file
	 *
	 * @return the parsed labels
	 */
	public static byte[] parseIDXlabels(byte[] data) {
		int magicNumber = extractInt(data[0], data[1], data[2], data[3]);
		if (magicNumber == 2049) {
			int length = extractInt(data[4], data[5], data[6], data[7]);
			byte[] result = new byte[length];
			int j = 0;
			for (int i = 8; i < length + 8; ++i) {
				result[j] = data[i];
				++j;
			}
			return result;
		}
		else {
			return null;
		}
	}

	/**
	 * @brief Computes the squared L2 distance of two images
	 * 
	 * @param a, b two images of same dimensions
	 * 
	 * @return the squared euclidean distance between the two images
	 */
	public static float squaredEuclideanDistance(byte[][] a, byte[][] b) {
		// TODO: Implémenter
		float distance = 0;
		int h = a.length;
		int w = a[0].length;
		for (int i = 0; i < h-1; i++) {
			for (int j = 0; j < w-1; j++) {
				distance += (a[i][j] - b[i][j])*(a[i][j] - b[i][j]);
			}
		}
		return distance;
	}

	/**
	 * @brief Computes the inverted similarity between 2 images.
	 * 
	 * @param a, b two images of same dimensions
	 * 
	 * @return the inverted similarity between the two images
	 */
	public static float invertedSimilarity(byte[][] a, byte[][] b) {

		float aBarre = moyenne(a);
		float bBarre = moyenne(b);
		
		float num = 0;
		
		// calcul du numérateur
		
		for (int i = 0; i<a.length; ++i) {
			
			for(int j = 0; j<a[0].length; ++j) {
				
				num = num + (a[i][j]-aBarre)*(b[i][j]-bBarre);
				
			}
		}
		
		//si le numerateur est nul
		if(num == 0) { 
			return 2;
		}
		
		// si le numérateur n'est pas nul
		
		float denomin = 0;
		float denomin1 = 0;
		float denomin2 = 0;
		
		for (int i = 0; i<a.length; ++i) {
			
			for(int j = 0; j<a[0].length; ++j) {
				
				denomin1 = denomin1 + ((a[i][j]-aBarre)*(a[i][j]-aBarre));
				
			}
		}
		for (int i = 0; i<b.length; ++i) {
			
			for(int j = 0; j<b[0].length; ++j) {
				
				denomin2 = denomin2 + ((b[i][j]-bBarre)*(b[i][j]-bBarre));
				
			}
		}
		
		denomin = (float) Math.sqrt(denomin1*denomin2);
		float simi = 1-(num/denomin);
		
		return simi;
	}
	
	// methode auxiliaire pour calculer la moyenne d'une image I barre
	
	public static float moyenne (byte[][] a) {
		
		float partone = 1/(a.length * a[0].length);
		float parttwo = 0;
		
		for (int i = 0; i<a.length; ++i) {
			
			for(int j = 0; j<a[0].length; ++j) {
				
				parttwo = parttwo + a[i][j];
				
			}
		}
		
		float iBarre = partone*parttwo;
		return iBarre;
	}

	/**
	 * @brief Quicksorts and returns the new indices of each value.
	 * 
	 * @param values the values whose indices have to be sorted in non decreasing
	 *               order
	 * 
	 * @return the array of sorted indices
	 * 
	 *         Example: values = quicksortIndices([3, 7, 0, 9]) gives [2, 0, 1, 3]
	 */
	public static int[] quicksortIndices(float[] values) {
		int[] indices = new int[values.length];
		for (int i = 0; i < values.length; i++) {
			indices[i] = i;
		}
		quicksortIndices(values, indices, 0, values.length-1);
		return indices;
	}

	/**
	 * @brief Sorts the provided values between two indices while applying the same
	 *        transformations to the array of indices
	 * 
	 * @param values  the values to sort
	 * @param indices the indices to sort according to the corresponding values
	 * @param         low, high are the **inclusive** bounds of the portion of array
	 *                to sort
	 */
	public static void quicksortIndices(float[] values, int[] indices, int low, int high) {
		int l = low;
		int h = high;
		float pivot = values[l];
		while (l <= h) {
			if (values[l] < pivot) {
				++l;
			}
			else if (values[h] > pivot){
				--h;
			}
			else {
				swap(l, h, values, indices);
				++l;
				--h;
			}
		}
		if (low < h) {
			quicksortIndices(values, indices, low, h);
		}
		if (high > l) {
			quicksortIndices(values, indices, l, high);
		}
	}

	/**
	 * @brief Swaps the elements of the given arrays at the provided positions
	 * 
	 * @param         i, j the indices of the elements to swap
	 * @param values  the array floats whose values are to be swapped
	 * @param indices the array of ints whose values are to be swapped
	 */
	public static void swap(int i, int j, float[] values, int[] indices) {
		// TODO: Implémenter
		float a = values[j];
		values[j] = values[i];
		values[i] = a;
		int b = indices[j];
		indices[j] = indices[i];
		indices[i] = b;
	}

	/**
	 * @brief Returns the index of the largest element in the array
	 * 
	 * @param array an array of integers
	 * 
	 * @return the index of the largest integer
	 */
	public static int indexOfMax(int[] array) {
		// TODO: Implémenter
		int index = 0;
		for (int i = 0; i < array.length; i++) {
			if (array[i] > array[index]) {
				index = i;
			}
		}
		return index;
	}

	/**
	 * The k first elements of the provided array vote for a label
	 *
	 * @param sortedIndices the indices sorted by non-decreasing distance
	 * @param labels        the labels corresponding to the indices
	 * @param k             the number of labels asked to vote
	 *
	 * @return the winner of the election
	 */
	public static byte electLabel(int[] sortedIndices, byte[] labels, int k) {
		int[] indices = new int[10];
		for (int i = 0; i <= k && i < sortedIndices.length; i++) {
			int label = sortedIndices[i];
			indices[label] += 1;
		}
		
		int votedLabel = indexOfMax(indices);
		return labels[votedLabel];
	}

	/**
	 * Classifies the symbol drawn on the provided image
	 *
	 * @param image       the image to classify
	 * @param trainImages the tensor of training images
	 * @param trainLabels the list of labels corresponding to the training images
	 * @param k           the number of voters in the election process
	 *
	 * @return the label of the image
	 */
	public static byte knnClassify(byte[][] image, byte[][][] trainImages, byte[] trainLabels, int k) {
		// TODO: Implémenter
		return 0;
	}

	/**
	 * Computes accuracy between two arrays of predictions
	 * 
	 * @param predictedLabels the array of labels predicted by the algorithm
	 * @param trueLabels      the array of true labels
	 * 
	 * @return the accuracy of the predictions. Its value is in [0, 1]
	 */
	public static double accuracy(byte[] predictedLabels, byte[] trueLabels) {
		// TODO: Implémenter
		return 0d;
	}
}
