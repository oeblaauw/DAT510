package assignment2_RSA;

// Imported libraries, used only for GUI purpose
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class RSA extends JPanel implements ActionListener{
	private static final long serialVersionUID = 1L;
	
	// Create parameters
	int p, q, n, e, d, phi;
	
	JPanel instructions;
	JPanel input;
	JPanel output;
	JPanel settings;
	JPanel settingsWrap;
	JPanel buttons;
	JPanel gridPanel;
	
	JLabel inLabel;
	JLabel outLabel;
	JLabel pubKeyLabel;
	JLabel prKeyLabel;
	JLabel pLabel;
	JLabel qLabel;
	JLabel instLabel;
	JLabel settingsLabel;
	
	JTextField txtPubKey;
	JTextField txtPrKey;
	JTextField txtQ;
	JTextField txtP;
	
	JTextArea txtInput;
	JTextArea txtOutput;
	JTextArea txtInstruct;
	
	JButton btnSetKey;
	JButton btnReset;
	JButton btnEnc;
	JButton btnDec;
	
	// Creating the GUI
	public RSA() {

		// Set up the GUI
		prepareGUI();

		// Set up the key settings
		resetKey();
	}
	
	/**
	 * Method for preparing the GUI. 
	 */
	public void prepareGUI() {
		setLayout(new BorderLayout());

		// Input field
		input = new JPanel(new BorderLayout());
		inLabel = new JLabel("Input");
		txtInput = new JTextArea(6,20);
		txtInput.setLineWrap(true);
		input.add(inLabel, BorderLayout.NORTH);
		input.add(txtInput, BorderLayout.CENTER);
		input.setBorder(BorderFactory.createLineBorder(Color.black));
		
		// Output field
		output = new JPanel(new BorderLayout());
		outLabel = new JLabel("Output");
		txtOutput = new JTextArea(6,40);
		txtOutput.setEditable(false);
		txtOutput.setLineWrap(true);
		output.add(outLabel, BorderLayout.NORTH);
		output.add(txtOutput, BorderLayout.CENTER);
		output.setBorder(BorderFactory.createLineBorder(Color.black));
		
		// Instructions		
		instructions = new JPanel(new BorderLayout());
		instLabel = new JLabel("Instructions:");
		txtInstruct = new JTextArea(12,42);
		txtInstruct.setEditable(false);
		txtInstruct.setBackground(null);
		txtInstruct.setText("1) To encrypt a message, enter a message in plaintext into the 'Input field'." +
				"\nBinary input will not work, nor will foreign characters, like norwegian. " +
				"\nPress the 'Encrypt' button for encryption." +
				"\n\n2) To decrypt a message, enter the ciphertext into the 'Input field'. " +
				"\nThe format of this input should be the same as the output from the encryption. "+
				"\nPress the 'Decrypt' button for decryption." +
				"\n\n3) To edit the prime numbers, the private or public key, enter new values under 'Key Settings',"+
				"\nand press the 'Set values' button." +
				"\n\n4) To reset the key settings to default, press the 'Reset' button.");
		instructions.add(instLabel, BorderLayout.NORTH);
		instructions.add(txtInstruct, BorderLayout.CENTER);
		instructions.setBorder(BorderFactory.createLineBorder(Color.black));
		
		// Settings
		settingsWrap = new JPanel(new BorderLayout());
		settings = new JPanel(new GridLayout(5,2));
		
		txtPubKey = new JTextField(15);
		txtPubKey.setText(Integer.toString(getE()));
		txtPrKey = new JTextField(15);
		txtPrKey.setText(Integer.toString(getD()));
		txtQ= new JTextField(15);
		txtQ.setText(Integer.toString(getQ()));
		txtP = new JTextField(15);
		txtP.setText(Integer.toString(getP()));
		
		settingsLabel = new JLabel("Key settings");
		pubKeyLabel = new JLabel("Public Key (e):");
		prKeyLabel = new JLabel("Private Key (d):");
		pLabel = new JLabel("Prime #1 (p):");
		qLabel = new JLabel("Prime #2 (q):");
		
		btnReset = new JButton("Reset");
		btnReset.addActionListener(this);
		btnSetKey = new JButton("Set values");
		btnSetKey.addActionListener(this);
		
		settings.add(pubKeyLabel);
		settings.add(txtPubKey);
		settings.add(prKeyLabel);
		settings.add(txtPrKey);
		settings.add(pLabel);
		settings.add(txtP);
		settings.add(qLabel); 
		settings.add(txtQ);
		settings.add(btnReset);
		settings.add(btnSetKey);
		
		settingsWrap.add(settingsLabel, BorderLayout.NORTH);
		settingsWrap.add(settings, BorderLayout.CENTER);
		settingsWrap.setBorder(BorderFactory.createLineBorder(Color.black));
		
		// Buttons
		buttons = new JPanel();
		btnEnc = new JButton("Encrypt");
		btnEnc.addActionListener(this);
		btnDec = new JButton("Decrypt");
		btnDec.addActionListener(this);

		buttons.add(btnEnc);
		buttons.add(btnDec);
		
		// Adding panels to gridPanel
		gridPanel = new JPanel(new GridLayout(2,2));
		gridPanel.add(input);
		gridPanel.add(instructions);
		gridPanel.add(output);
		gridPanel.add(settingsWrap);
		
		// Adding gridPanel and buttons to layout
		add(gridPanel, BorderLayout.NORTH);
		add(buttons, BorderLayout.SOUTH);
	}
	
	/**
	 * Convert a string of text into an array of integers. Each char in the text is converted to its byte value
	 * @param text: Message input
	 * @return
	 * @throws Exception 
	 */
	public int[] textToNumbers(String text) throws Exception {
		char[] chars = text.toCharArray();
		int[] numbers = new int[chars.length];

		for(int i=0; i < chars.length; i++) {
			numbers[i] = (byte) chars[i];
			
			// Don't accept characters that outputs negative numbers. Typically foreign letters/special characters
			if(numbers[i] < 0) throw new Exception("Failed to recognize character: " + chars[i]);
		}
		return numbers;
	}
	
	/**
	 * Convert an array of integers into a string of plaintext. Each integer is converted to the char value of its 
	 * integer value.
	 * @param numbers
	 * @return
	 */
	public String numbersToText(int[] numbers) {
		String result = "";

		for (int i : numbers) {
			result += (char) i;
		}
		return result;
	}

	/**
	 * RSA Algorithm.
	 * Method that encrypts/decrypts an integer. See report section for implementation for more details.
	 * @param m: This is the "message", or the integer to be encrypted/decrypted
	 * @param exp: Equivalent to the key, e or d
	 * @return
	 */
	public int calculate(int m, int exp) {
		long result = 1;
		
		// If the exponent is not an even number
		if (exp % 2 != 0) {
			result = m % n;
		}
		
		// Calculation is divided into segments of m^2. Do this exp/2 times. 
		// If we would calculate m^exp directly, we would get a very big number, which is too big for Java.Long
		for (int i = 0; i < (exp/2); i++) {
			result *= m * m % n;
			
			// Modulo the result each time, in order to not getting a number thats too big.
			result %= n;
		}
		return (int)result;
	}
	
	public void encrypt() {
		// Get text from input
		String text = txtInput.getText();
		
		try {
			// Convert text to integer array (byte value of char)
			int[] message = textToNumbers(text);
			
			// Create a new array, will hold the cipher message
			int[] ciphertext = new int[message.length];
			
			// Loop through message, cipher each integer
			for (int i = 0; i < message.length; i++) {
				ciphertext[i] = calculate(message[i], e);
			}
			
			// Print result to user
			String result = "";

			for (int i : ciphertext) {
				result += i + "-";
			}
			txtOutput.setText(result);
		} catch (Exception exp) {
			JOptionPane.showMessageDialog(null, exp.getMessage());
		}
		
	}

	/**
	 * Method for decrypting a message. Input format should be numbers separated by dashes (-).
	 * In other words, the input should be in the same format as the output from the encryption function.
	 */
	public void decrypt() {
		// Get text from input
		String text = txtInput.getText();

		try {
			// Split string into array of strings, separated by "-"
			String[] ciphertext = text.split("-");

			// Create a new array, will hold the message
			int[] message = new int[ciphertext.length];

			// Loop through cipher message, decipher each integer
			for (int i = 0; i < ciphertext.length; i++) {
				int m = Integer.parseInt(ciphertext[i]);
				message[i] = calculate(m, d);
			}

			// Convert numbers to text, and print result to used
			String result = numbersToText(message);
			txtOutput.setText(result);
		} catch (Exception exp) {
			JOptionPane.showMessageDialog(null, "Error. Please check the input format");
		}
	}

	/**
	 * Method for updating the parameters in the GUI, e.g. the key settings.
	 */
	public void setKey() {
		setE(Integer.parseInt(txtPubKey.getText()));
		setD(Integer.parseInt(txtPrKey.getText()));
		setP(Integer.parseInt(txtP.getText()));
		setQ(Integer.parseInt(txtQ.getText()));
		setN();
		setPhi();
	}
	
	/**
	 * Resets the prime numbers, the private key and the public key to a set
	 * of values that works. 
	 */
	public void resetKey() {
		setP(89);
		setQ(107);
		setE(3);
		setD(6219);
		setN();
		setPhi();
		txtPubKey.setText(Integer.toString(getE()));
		txtPrKey.setText(Integer.toString(getD()));
		txtQ.setText(Integer.toString(getQ()));
		txtP.setText(Integer.toString(getP()));
	}
	
	/** Generated getters and setters for the different parameters **/
	public int getP() {
		return p;
	}

	public void setP(int p) {
		this.p = p;
	}

	public int getQ() {
		return q;
	}

	public void setQ(int q) {
		this.q = q;
	}
	
	public int getN() {
		return n;
	}

	// n should be calculated based on p and q, not determined by user input
	public void setN() {
		this.n = p * q;
	}

	public int getE() {
		return e;
	}

	public void setE(int e) {
		this.e = e;
	}

	public int getD() {
		return d;
	}

	public void setD(int d) {
		this.d = d;
	}

	public int getPhi() {
		return phi;
	}
	
	// phi should be calculated based on p and q, not determined by user input
	public void setPhi() {
		this.phi = (p - 1) * (q - 1);
		if(this.phi % e == 0 || e < 3) {
			JOptionPane.showMessageDialog(null, "Invalid value for public key e = " + e);
		}
	}

	// Main, start program
	public static void main(String[] args) {
		// Creating new application frame
		JFrame frame = new JFrame();
		
		// Creating new instance of RSA, add to frame
		RSA gui = new RSA();
		
		// Adjusting frame settings
		frame.add(gui);
		frame.setTitle("RSA");
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);	
	}

	// Action Listeners for buttons in GUI. Determine function based on button click
	@Override
	public void actionPerformed(ActionEvent ev) {
		if(ev.getSource() == btnEnc) encrypt();
		if(ev.getSource() == btnDec) decrypt();
		if(ev.getSource() == btnSetKey) setKey();
		if(ev.getSource() == btnReset) resetKey();
	}

}