package cs107KNN;

import java.util.Set;
import java.util.HashSet;
import java.util.Random;
import java.util.ArrayList;

public class KMeansClustering {
	public static void main(String[] args) {
		//int K = 5000;
		//int maxIters = 20;

		// TODO: Adaptez les parcours
		byte[][][] images = KNN.parseIDXimages(Helpers.readBinaryFile("datasets/10-per-digit_images_train"));
		byte[] labels = KNN.parseIDXlabels(Helpers.readBinaryFile("datasets/10-per-digit_labels_train"));
		encodeIDXlabels(labels);
		encodeIDXimages(images);
		System.out.print("\n");
		
		/*byte[] labels2 = (Helpers.readBinaryFile("datasets/10-per-digit_labels_train"));
		for (int i = 0; i < labels2.length; i++) {
			System.out.print(labels2[i]);
		}
		

		byte[][][] reducedImages = KMeansReduce(images, K, maxIters);

		byte[] reducedLabels = new byte[reducedImages.length];
		for (int i = 0; i < reducedLabels.length; i++) {
			reducedLabels[i] = KNN.knnClassify(reducedImages[i], images, labels, 5);
			System.out.println("Classified " + (i + 1) + " / " + reducedImages.length);
		}

		Helpers.writeBinaryFile("datasets/reduced10Kto1K_images", encodeIDXimages(reducedImages));
		Helpers.writeBinaryFile("datasets/reduced10Kto1K_labels", encodeIDXlabels(reducedLabels));*/
	}

    /**
     * @brief Encodes a tensor of images into an array of data ready to be written on a file
     * 
     * @param images the tensor of image to encode
     * 
     * @return the array of byte ready to be written to an IDX file
     */
	public static byte[] encodeIDXimages(byte[][][] images) {
		
		int n=0;
		int magicNumber = 2051;
		byte[] data = new byte [images.length*images[0].length*images[0][0].length+16];
		
		encodeInt(magicNumber,data,0);//magicnumber
		encodeInt(images.length,data,4);//nb d'images
		encodeInt(images[0].length,data,8);//hauteur
		encodeInt(images[0][0].length,data,12);//largeur
		
		for(int i = 0; i<images.length; ++i) { // nb d'images
			
			for(int k = 0; k<images[0].length; ++k) { // parcourir hauteur
		
				for(int j = 0; j<images[0][0].length; ++j) { // parcourir largeur
				
					data [n+16] = images[i][k][j];
					++n;
			
				}
			}
		}
		/*for (int i = 250; i < 300; i++) {
			System.out.print(data[i]);
			System.out.print(" ");
		
		}*/
		
		return data;
	}

    /**
     * @brief Prepares the array of labels to be written on a binary file
     * 
     * @param labels the array of labels to encode
     * 
     * @return the array of bytes ready to be written to an IDX file
     */
	public static byte[] encodeIDXlabels(byte[] labels) {
		int magicNumber = 2049;
		byte[] encoded = new byte[labels.length + 8];
		encodeInt(magicNumber, encoded, 0);
		encodeInt(labels.length, encoded, 4);
		for (int i = 0; i < labels.length; i++) {
			encoded[i + 8] = labels[i];
		}
		/*for (int i = 0; i < encoded.length; i++) {
			System.out.print(encoded[i]);
		}*/
		return encoded;
	}

    /**
     * @brief Decomposes an integer into 4 bytes stored consecutively in the destination
     * array starting at position offset
     * 
     * @param n the integer number to encode
     * @param destination the array where to write the encoded int
     * @param offset the position where to store the most significant byte of the integer,
     * the others will follow at offset + 1, offset + 2, offset + 3
     */
	public static void encodeInt(int n, byte[] destination, int offset) {
		String numString = "";
		
		//convertir n en un nombre binaire sous forme de String
		
		for (int i = 31; i  >= 0; --i) {
			int power = (int) Math.pow(2, i);
			int reste = n/power;
			numString += String.valueOf(reste);
			n = n - (reste * power);;
		}
	
		int k = 0;
		for (int i = 0; i < numString.length() - 7; i+=8) {
			
			//stocker le nombre binaire n dans "destination" sous forme de 4 bytes
			
			byte newbyte = Helpers.binaryStringToByte(numString.substring(i, i + 8)); 
			destination[offset + k] = newbyte;
			k += 1;
		}

	}

    /**
     * @brief Runs the KMeans algorithm on the provided tensor to return size elements.
     * 
     * @param tensor the tensor of images to reduce
     * @param size the number of images in the reduced dataset
     * @param maxIters the number of iterations of the KMeans algorithm to perform
     * 
     * @return the tensor containing the reduced dataset
     */
	public static byte[][][] KMeansReduce(byte[][][] tensor, int size, int maxIters) {
		int[] assignments = new Random().ints(tensor.length, 0, size).toArray();
		byte[][][] centroids = new byte[size][][];
		initialize(tensor, assignments, centroids);

		int nIter = 0;
		while (nIter < maxIters) {
			// Step 1: Assign points to closest centroid
			recomputeAssignments(tensor, centroids, assignments);
			System.out.println("Recomputed assignments");
			// Step 2: Recompute centroids as average of points
			recomputeCentroids(tensor, centroids, assignments);
			System.out.println("Recomputed centroids");

			System.out.println("KMeans completed iteration " + (nIter + 1) + " / " + maxIters);

			nIter++;
		}

		return centroids;
	}

   /**
     * @brief Assigns each image to the cluster whose centroid is the closest.
     * It modifies.
     * 
     * @param tensor the tensor of images to cluster
     * @param centroids the tensor of centroids that represent the cluster of images
     * @param assignments the vector indicating to what cluster each image belongs to.
     *  if j is at position i, then image i belongs to cluster j
     */
	public static void recomputeAssignments(byte[][][] tensor, byte[][][] centroids, int[] assignments) {
		
		for(int i =0; i<tensor.length;++i) {
			
			float[] distance = new float[centroids.length]; // tab distance qui stock la distance entre l'img i et les centroids
			
			for(int j = 0; i<centroids.length;++j) {
				
				distance[i] = KNN.squaredEuclideanDistance(tensor[i], centroids[j]);
				assignments[i]=choose(distance); // assigne à l'image i le centroid qui est le plus proche d'elle
				
			}
		}
		
	}
	
	public static byte choose(float[] distance) { // choisi le numero du centroid le plus proche 
		
			int n = 0;
			for(int i = 0; i<distance.length;++i) {
				
				if(distance[n]> distance[i]) {
					n = i;
				}
			}
			
			return (byte)n;
	}

    /**
     * @brief Computes the centroid of each cluster by averaging the images in the cluster
     * 
     * @param tensor the tensor of images to cluster
     * @param centroids the tensor of centroids that represent the cluster of images
     * @param assignments the vector indicating to what cluster each image belongs to.
     *  if j is at position i, then image i belongs to cluster j
     */
	public static void recomputeCentroids(byte[][][] tensor, byte[][][] centroids, int[] assignments) {
		for (int i = 0; i < tensor.length; i++) {
			ArrayList<byte[][]> cluster = new ArrayList<byte[][]>();
			for (int j = 0; i < assignments.length; ++j) {
				if (assignments[j] == i) {
							cluster.add(tensor[j]);
						
					}
				}
			for (int x = 0; x < tensor[i].length; x++) {
				int n = cluster.size();
				for (int a = 0; a < n; a++) {
					 
				}
			}
			}
		}
	
	

    /**
     * Initializes the centroids and assignments for the algorithm.
     * The assignments are initialized randomly and the centroids
     * are initialized by randomly choosing images in the tensor.
     * 
     * @param tensor the tensor of images to cluster
     * @param assignments the vector indicating to what cluster each image belongs to.
     * @param centroids the tensor of centroids that represent the cluster of images
     *  if j is at position i, then image i belongs to cluster j
     */
	public static void initialize(byte[][][] tensor, int[] assignments, byte[][][] centroids) {
		Set<Integer> centroidIds = new HashSet<>();
		Random r = new Random("cs107-2018".hashCode());
		while (centroidIds.size() != centroids.length)
			centroidIds.add(r.nextInt(tensor.length));
		Integer[] cids = centroidIds.toArray(new Integer[] {});
		for (int i = 0; i < centroids.length; i++)
			centroids[i] = tensor[cids[i]];
		for (int i = 0; i < assignments.length; i++)
			assignments[i] = cids[r.nextInt(cids.length)];
	}
}
