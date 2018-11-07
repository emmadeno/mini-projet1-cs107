package cs107KNN;

import java.util.Arrays;

public class KNN {
	public static void main(String[] args) {
		/*byte b1 = 40; // 00101000
		byte b2 = 20; // 00010100
		byte b3 = 10; // 00001010
		byte b4 = 5; // 00000101

		// [00101000 | 00010100 | 00001010 | 00000101] = 672401925
		int result = extractInt(b1, b2, b3, b4);
		System.out.println(result);
		
		//KNNTest.parsingTest();
		//KNNTest.invertedSimilarityTest();
		
		System.out.println("Hi");
		/** System.out.println(labelsTrain.length); */
		
		//KNNTest.electLabelTest();
		//KNNTest.quicksortTest();
		//KNNTest.indexOfMaxTest();
		//KNNTest.knnClassifyTest();
		//KNNTest.accuracyTest();*/
		
		byte[][][] imagesTrain = KNN.parseIDXimages(Helpers.readBinaryFile("datasets/5000-per-digit_images_train"));
		byte[] labelsTrain = KNN.parseIDXlabels(Helpers.readBinaryFile("datasets/5000-per-digit_labels_train"));

		byte[][][] imagesTest = KNN.parseIDXimages(Helpers.readBinaryFile("datasets/10k_images_test"));
		byte[] labelsTest = KNN.parseIDXlabels(Helpers.readBinaryFile("datasets/10k_labels_test"));
		
	
		//temps d'execution
		
		int TESTS = 1000;
		byte[] predictions = new byte [TESTS];
		long start = System.currentTimeMillis();
		for(int i = 0; i<TESTS; ++i) {
			
			predictions[i] = knnClassify(imagesTest[i], imagesTrain, labelsTrain, 7);
			
		}
		long end = System.currentTimeMillis();
		double time = (end-start)/1000d;
		System.out.println("Accuracy = "+ accuracy(predictions, Arrays.copyOfRange(labelsTest, 0, TESTS)) + " %");
		System.out.println("Time = "+ time + " seconds");
		System.out.println("Time per test image = "+(time/TESTS));
		

		
	}

	/**
	 * Composes four bytes into an integer using big endian convention.
	 *
	 * @param bXToBY The byte containing the bits to store between positions X and Y
	 * 
	 * @return the integer having form [ b31ToB24 | b23ToB16 | b15ToB8 | b7ToB0 ]
	 */
	public static int extractInt(byte b31ToB24, byte b23ToB16, byte b15ToB8, byte b7ToB0) {

		int numConverti = 0;  
		int j = 0;
		
		//conversion des 4 bytes en 1 string
		String bytesAsString = Helpers.byteToBinaryString(b31ToB24) + Helpers.byteToBinaryString(b23ToB16) + Helpers.byteToBinaryString(b15ToB8) + Helpers.byteToBinaryString(b7ToB0);
		
		//parcourir le string de droit à gauche avec i et de gauche à droite avec j
		for (int i = bytesAsString.length(); i > 0; i--) {
			
			int num = bytesAsString.charAt(j) - '0';        //convertir le nombre à l'index j en int
			numConverti += (int) num * Math.pow(2, (i-1));  // multiplier ce nombre par la puissance de 2 correspondante
			j++;
		}
		return numConverti;
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
		else{
				return null;
		}
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
		
		//tester si le magic number correspond bien
		if (magicNumber == 2049) {
			
			
			int length = extractInt(data[4], data[5], data[6], data[7]);
			
			//créer un tableau dont la longueur est celle du nombre d'étiquettes
			byte[] result = new byte[length];
			
			int j = 0;
			for (int i = 8; i < length + 8; ++i) {   
				result[j] = data[i];                 //stocker les étiquettes dans un tableau
				++j;
			}
			return result;                           //retourne le tableau d'étiquettes
		}
		else {
			return null;                            //retourne null si le magic number ne correspond pas
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
		float distance = 0;  //initialisation de la distance qui va être incrémentée
		int h = a.length;    
		int w = a[0].length;
		for (int i = 0; i < h-1; i++) {
			for (int j = 0; j < w-1; j++) {
				distance += (a[i][j] - b[i][j])*(a[i][j] - b[i][j]);  //formule
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

		float[] abBarre = moyenne(a,b); // retourne A barre et B barre sous forme de tableau
		
		float num = 0; //numérateur
		
		// calcul du numérateur
		
		for (int i = 0; i<a.length; ++i) {
			
			for(int j = 0; j<a[0].length; ++j) {
				
				num = num + (a[i][j]-abBarre[0])*(b[i][j]-abBarre[1]);
				
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
				
				denomin1 = denomin1 + ((a[i][j]-abBarre[0])*(a[i][j]-abBarre[0]));
				
			}
		}
		for (int i = 0; i<b.length; ++i) {
			
			for(int j = 0; j<b[0].length; ++j) {
				
				denomin2 = denomin2 + ((b[i][j]-abBarre[1])*(b[i][j]-abBarre[1]));
				
			}
		}
		
		denomin = (float) Math.sqrt(denomin1*denomin2); // convertion du double sqrt en float
		float simi = 1-(num/denomin);
		
		return simi;
	}
	
	
	// methode auxiliaire pour rentrer les valeurs des moyennes dans un tableau
	public static float[] moyenne (byte[][] a, byte [][] b) { 
		
		float[] iBarre = new float [2];
		iBarre[0]= barre(a);
		iBarre[1]= barre(b);
		return iBarre;
	}
	
	// methode auxiliaire pour calculer la moyenne d'une image I barre
	public static float barre (byte[][]i) {
		
		float partone = 1/(i.length * i[0].length);
		float parttwo = 0;
		
		for (int k = 0; k<i.length; ++k) {
			
			for(int j = 0; j<i[0].length; ++j) {
				
				parttwo = parttwo + i[k][j];
				
			}
		}
		
		
		float iBarre=partone*parttwo;
		
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
		
		// utilise l'algorithme quicksort pour trier les valeurs du tableau "values" du plus petit au plus grand
		// et fait correspondre les indices de ces valeurs qui se trouvent dans "indices"
		
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
		
		//permet d'échanger les valeurs aux indices i et j des tableaux "values" et "indices"
		
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
		
		// retourne l'indice de la plus grande valeur dans un tableau
		
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
		
		//permet aux k étiquettes les plus proches de voter pour le chiffre qu'ils représentent
		
		for (int i = 0; i < k && i < sortedIndices.length; i++) {
			int labelIndex = sortedIndices[i];
			int label = labels[labelIndex];
			indices[label] += 1;
		}
		
		//retourne l'étiquette avec le plus de votes
		
		int votedLabel = indexOfMax(indices);
		return (byte) votedLabel;
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
		
		float [] classify = new float [trainImages.length];
		
		// remplissage du tableau contennant les niveaux de similarité de 2 images
		for(int i = 0; i<trainImages.length; i++) {
			
			classify[i] = squaredEuclideanDistance(image, trainImages[i]);
			//classify[i] = invertedSimilarity(image, trainImages[i]);
		}
		
		int [] indices = quicksortIndices(classify);
		//int max = indexOfMax(indices);
		byte resultat = electLabel(indices,trainLabels,k);
		
		
		return resultat;
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
		
		double a = 0.0;
		
		for(int i = 0; i<trueLabels.length; ++i) {
			
			if(predictedLabels[i]==trueLabels[i]) {
				a = a+1.0;
			}
			else {	a = a+ 0.0;	}
		}
		
		a = a/trueLabels.length;
		
		return a * 100;
		
	}
}
