package assignment1;

import java.util.Scanner;

public class FeistelCipher {
	private int _key[];
	private int _blockSize;
	private int numberOfRounds;
	private int[][] LE, RE, LD, RD;

	@SuppressWarnings("resource")
	/**
	 * Initialize the instance of FeistelCipher. Asks user for input.
	 * @param keyString
	 */
	public FeistelCipher(String keyString) {
		_key = convertStringToArray(keyString, false);
		_blockSize = 64;
		numberOfRounds = 16;
		Scanner scan = new Scanner(System.in);

		System.out.println("Welcome to FeistelCipher. " +
				"Enter q for exiting the program.");

		do {
			System.out.println("\nEnter 'e' for encryption and 'd' for decryption,"
							+ " or 'q' for quit:");
			String command = scan.nextLine();

			if (command.equals("e")) {
				System.out.println("Enter message to encrypt in plaintext:");
				String message = scan.nextLine();

				// Check for empty plaintext
				if (message.length() == 0) {
					System.err.println("Failed to encrypt empty message. Please try again.");
					continue;
				}

				// Convert message string to array in binary format
				int[] messageBinary = convertStringToArray(message, false);

				// Run the encryption
				encrypt(messageBinary);

			} else if (command.equals("d")) {
				System.out.println("Enter ciphertext to decrypt in binary, in one line:");
				String decryptString = scan.nextLine();

				// Check for empty ciphertext
				if (decryptString.length() == 0) {
					System.err.println("Failed to decrypt empty message. Please try again.");
					continue;
				}
				
				// Remove line breaks from input:

				decryptString.replaceAll("[\n\r]", "");

				// Run the decryption
				int[] d = decrypt(convertStringToArray(decryptString, true));

				// Prints the decoded message in plain text
				if (d != null) {
					System.out.print("\nYour decoded message: \n");
					System.out.print(binaryToText(d));
				}

			} else if (command.equals("q")) {
				System.out.println("See you later!");
				break;

			} else {
				System.err.println("Input error. \nYou entered '" + command
						+ "'\nPlease try again.");
			}

		} while (true);
	}

	/**
	 * Main method. Creates an instance of the class FeistelCipher. Determine
	 * key here.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// Assign the key string
		String keyString = "bL4AuW8UBlKsCUb3";
		
		// Run a new instance of FeistelCipher
		new FeistelCipher(keyString);
	}

	/**
	 * Function to encrypt a message. Message input is an array, binary format
	 * Checks the message size, splits into blocks of blockSize if necessary.
	 * Pad with zeros if necessary Send blocks to encryptBlock function for
	 * encryption. This is further discussed in the report, under the Implementation part.
	 * 
	 * @param v
	 */
	public void encrypt(int v[]) {

		System.out.println("\nEncrypting Message:");

		/* Checks the length of the message, determine what to do */

		// If length of message (in binary) is not divisible by block size,
		// pad with zeros. Else, divide into blocks of block size, and send each
		// block to function encryptBlock for encryption.
		if (v.length % _blockSize != 0) {
			v = padZeros(v);
		}
		int inner = (v.length / _blockSize);
		int[][] blocks = new int[inner][_blockSize];

		// Dividing array v into blocks of block size
		// and adds the content from v
		int counter = 0;
		for (int a = 0; a < v.length; a += _blockSize) {
			for (int b = 0; b < _blockSize; b++) {
				blocks[counter][b] = v[b + _blockSize * counter];
			}
			counter++;
		}

		// Send each block to encryptBlock for encryption
		for (int[] pp : blocks) {
			encryptBlock(pp);
		}
	}

	/**
	 * Function that encrypts a block (array v) of size 64 (or block size)
	 * Returns encrypted array. This is further discussed in the report, under the Implementation part.
	 * 
	 * @param v
	 * @return
	 */
	public int[] encryptBlock(int v[]) {

		// Splits the block into two halves, LE and RE
		// Creates a 2dim array
		int[][] splittedBlocks = splitBlock(v);

		// Initialize LE
		// The outer array has a length of (number of rounds + 2),
		// one array for each round, one for init and one for final permutation
		LE = new int[numberOfRounds + 2][_blockSize / 2];

		// Initialize RE
		RE = new int[numberOfRounds + 2][_blockSize / 2];

		// Create arrays from the two halves, "round 0"
		int[] left = splittedBlocks[0];
		int[] right = splittedBlocks[1];

		// Sets the initial arrays
		LE[0] = left;
		RE[0] = right;
		
		
		// For each round
		for (int r = 0; r < numberOfRounds; r++) {
			// Send REx to LEx+1
			LE[r + 1] = RE[r];

			// Send REx to round function F, result = modRE
			int[] modRE = roundFunction(RE[r], r);

			// Perform XOR operation on LEr and modRE, send result to REx+1
			RE[r + 1] = booleanXORVectors(LE[r], modRE);
		}

		// Final permutation
		LE[numberOfRounds + 1] = RE[numberOfRounds];
		RE[numberOfRounds + 1] = LE[numberOfRounds];


		// Join the two halves into one array, and return
		int[] c = joinArrays(LE[numberOfRounds + 1], RE[numberOfRounds + 1]);

		for (int i : c) {
			System.out.print(i);
		}

		return c;
	}

	/**
	 * Function to decrypt a message. Message input is an array in binary
	 * format. Divides input into blocks (arrays) of block size, and sends each
	 * block to the function decryptBlock for decryption. This is further discussed 
	 * in the report, under the Implementation part.
	 * 
	 * @param v
	 * @return
	 */
	public int[] decrypt(int v[]) {
		System.out.println("\nDecrypting message..");

		// Should not be necessary to pad with zeros. But in case someone
		// has the wrong cipher text, it is better to pad with zeros, than
		// getting error.
		if (v.length % _blockSize != 0) {
			v = padZeros(v);
		}

		// Creates a 2dim array, where the outer array consists of 64 (or block
		// size) bits
		// The inner array holds all of these 64bit sized arrays
		int inner = (v.length / _blockSize);
		int[][] blocks = new int[inner][_blockSize];

		// Dividing array v into blocks of size 64 (or block size)
		// and adds content from v
		int counter = 0;
		for (int a = 0; a < v.length; a += _blockSize) {
			for (int b = 0; b < _blockSize; b++) {
				blocks[counter][b] = v[b + _blockSize * counter];
			}
			counter++;
		}

		int[] blockCopy = new int[inner * _blockSize];
		int count = 0;

		// Send each block to encryptBlock for encryption
		for (int[] pp : blocks) {
			int[] block = decryptBlock(pp);
			for (int x : block) {
				blockCopy[count] = x;
				count++;
			}
		}

		//Removes padded zeros
		blockCopy = removePaddedZeros(blockCopy);

		System.out.println("Decryption complete!");

		return blockCopy;
	}

	/**
	 * Function that decrypts a block (array v) of size 64 (or block size)
	 * Returns decrypted array. This is further discussed in the report, under the Implementation part.
	 * 
	 * @param v
	 * @return
	 */
	public int[] decryptBlock(int v[]) {
		// Splits the block into two halves, LD and RD
		// Creates a 2dim array
		int[][] splittedBlock = splitBlock(v);

		// Initialize LD
		// The outer array has a length of (number of rounds + 2),
		// one array for each round, one for init and one for final permutation
		LD = new int[numberOfRounds + 2][32];

		// Initialize RD
		RD = new int[numberOfRounds + 2][32];

		// Create arrays from the two halves, "round 0"
		int[] left = splittedBlock[0];
		int[] right = splittedBlock[1];

		// Sets the initial arrays, "round 0"
		LD[0] = left;
		RD[0] = right;

		// For each round:
		for (int r = 0; r < numberOfRounds; r++) {
			// Send RDx to LDx+1
			LD[r + 1] = RD[r];

			// Send RDx to round function F along with round number, result = modRD
			// In this case, round number should be opposite of encryption,
			// in order for the decryption to use the correct subkey.
			int[] modRD = roundFunction(RD[r], numberOfRounds - r - 1);

			// Perform XOR operation on LDr and modRD, send result to REx+1
			RD[r + 1] = booleanXORVectors(LD[r], modRD);
		}

		// Final permutation
		LD[numberOfRounds + 1] = RD[numberOfRounds];
		RD[numberOfRounds + 1] = LD[numberOfRounds];
	
		// Join the two halves into one array, and return
		return joinArrays(LD[numberOfRounds + 1], RD[numberOfRounds + 1]);
	}

	/**
	 * Splits a block (array v) of block size, into two halves. Return 2 dim
	 * array, containing the two halves.
	 * 
	 * @param v
	 * @return
	 */
	public int[][] splitBlock(int[] v) {
		int length = v.length / 2;
		int[] left = new int[length];
		int[] right = new int[length];

		for (int p = 0; p < length; p++) {
			left[p] = v[p];
			right[p] = v[p + length];
		}

		int[][] splits = { left, right };

		return splits;
	}

	/**
	 * SubKey Generator. Based on the round number during encryption/decryption,
	 * and _key, generate a subkey that is the same size as block size. This function is
	 * further explained in the report, under the section Implementation -> Sub key generation algorithm
	 * 
	 * @param roundNumber
	 * @return
	 */
	public int[] getSubKey(int roundNumber) {

		// The subkey array is initialized
		int[] subkey = new int[_blockSize];

		// Fills the subkey with values from _key. Shifting by roundNumber * 2
		System.arraycopy(_key, roundNumber*2, subkey, 0, _blockSize);

		return subkey;
	}

	/**
	 * Round function F, that performs a bitwise substitution based on array and
	 * round number. This is further explained in the report, under the section 
	 * Implementation -> Round Function (F)
	 * 
	 * @param rv
	 * @return
	 */
	public int[] roundFunction(int[] rv, int roundNumber) {
		// Creates a copy of array rv
		int[] modV = new int[rv.length];

		// Gets a subkey for this round
		int[] subKey = getSubKey(roundNumber);

		// Perform a substitution based on subkey
		for (int i = 0; i < rv.length; i++) {
			if (i % 2 == 0) {
				modV[i] = rv[i] & subKey[i];
			} else if (i % 3 == 0) {
				modV[i] = rv[i] | subKey[i];
			} else {
				modV[i] = rv[i] ^ subKey[i];
			}
		}
		return modV;
	}

	/**
	 * Function that performs the XOR operator on two vectors, bit by bit
	 * Paramters a and b must be of equal length.
	 * @param a
	 * @param b
	 * @return
	 */
	public int[] booleanXORVectors(int[] a, int[] b) {
		int length = a.length;
		if (length != b.length) {
			System.err.println("Error, the two arrays are not equal in length.");
			return null;
		}

		int[] result = new int[length];
		for (int i = 0; i < length; i++) {
			result[i] = a[i] ^ b[i];
		}

		return result;
	}

	/** Function that removes padded zeros behind an array. 
	 * The function starts from the back of the array, and counts the number
	 * of consecutive zeros. For each round of 8 consecutive zeros, remove that byte of zeros.
	 * Break if hit anything else than zero.
	 * @param v
	 * @return
	 */
	public int[] removePaddedZeros(int[] v) {
		int countZeros = 0;
		int countRemove = 0;
		
		// Start from the back of the array
		for (int i = v.length - 1; i >= 0; i--) {
			// If the value equals to zero, increase countZeros
			if (v[i] == 0) {
				countZeros++;
			} 
			// When hitting anything else than zero, break.
			else {
				break;
			}
		}
		// Calculate the number of bytes to remove.
		countRemove = countZeros / 8;

		// The new length of the array
		int length = v.length - 8 * countRemove;
		
		// Creates a new modified vector
		int[] modV = new int[length];
		
		// Copy values (except the padded zeros) into the new array, and return
		System.arraycopy(v, 0, modV, 0, length);

		return modV;
	}

	/**
	 * Function that pads a vector with zeros, in order for the array to be
	 * divisible by block size
	 * 
	 * @param v
	 * @return
	 */
	public int[] padZeros(int v[]) {

		// Calculates modulo
		int mod = (v.length % _blockSize);

		// Calculates the number of zeros to be added
		int numberOfZeros = _blockSize - mod;

		// Creates a new array of size v.length + the number of zeros to be
		// added
		// This results in an array that is divisible by 64 (or block size)
		int[] copy = new int[v.length + numberOfZeros];

		// Copying the vector
		for (int i = 0; i < v.length; i++) {
			copy[i] = v[i];
		}

		// Adding zeros, so that vector.size % _blockSize = 0
		for (int j = v.length; j < copy.length; j++) {
			copy[j] = 0;
		}

		return copy;
	}

	/**
	 * Function that join two arrays, a first, b last
	 * 
	 * @param a
	 * @param b
	 * @return c
	 */
	public int[] joinArrays(int[] a, int[] b) {
		int aLen = a.length;
		int bLen = b.length;
		int[] c = new int[aLen + bLen];
		// Copy array a into the first half of c
		System.arraycopy(a, 0, c, 0, aLen);
		// Copy array b into the last half of c
		System.arraycopy(b, 0, c, aLen, bLen);

		return c;
	}

	/**
	 * Function for converting a string of plaintext into a string in
	 * binary format
	 * 
	 * @param asciiString
	 * @return
	 */
	public String textToBinary(String plaintext) {
		byte[] bytes = plaintext.getBytes();
		StringBuilder binary = new StringBuilder();
		for (byte b : bytes) {
			int value = b;
			for (int i = 0; i < 8; i++) {
				binary.append((value & 128) == 0 ? 0 : 1);
				value <<= 1;
			}
		}
		return binary.toString();
	}

	/**
	 * Function for converting an array of binary numbers, to a string of
	 * plaintext
	 * 
	 * @param binary
	 * @return
	 */
	public String binaryToText(int[] binary) {
		StringBuilder strBuilder = new StringBuilder();
		for (int i = 0; i < binary.length; i++) {
			strBuilder.append(binary[i]);
		}

		String newString = strBuilder.toString();

		String result = "";
		char nextChar;

		// Loops through string, byte by byte
		for (int i = 0; i <= newString.length() - 8; i += 8) {
			nextChar = (char) Integer.parseInt(newString.substring(i, i + 8), 2);
			result += nextChar;
		}
		return result;
	}

	/**
	 * Function that converts a string into an array of integers 
	 * @param binaryInput: If true, the str is in binary format. If false, plaintext format.
	 * @param str
	 * @return
	 */
	public int[] convertStringToArray(String str, boolean binaryInput) {
		// Converts string to binary format
		if(!binaryInput) {
			str = textToBinary(str);
		}
		// The code removes the first entry, which is empty because of the split
		// method.
		String[] org = str.split("");
		int[] copy = new int[org.length - 1];

		for (int i = 1; i < org.length; i++) {
			copy[i - 1] = Integer.parseInt(org[i]);
		}
		return copy;
	}

	/**
	 * Function that prints all values from an array of integers to Console
	 * 
	 * @param array
	 */
	public void printArray(int[] array) {
		for (int i : array) {
			System.out.print(i);
		}
		System.out.println("");

	}
}
