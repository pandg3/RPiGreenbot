package org.jointheleague.ecolban.rpirobot;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This class represents the communication channel between the computer (e.g.,
 * Raspberry Pi) and the iRobot. At most one instance of this class may be
 * instantiated. Use the {@link #getInstance(boolean) } method to get that
 * instance.
 * 
 * @author Erik Colban &copy; 2016
 * 
 */
public final class SerialConnection {

	private static final int OI_MODE_PASSIVE = 1;
	private static final int SENSORS_OI_MODE = 35;
	private static final int SENSOR_COMMAND = 142;
	private static final int BAUD_RATE = 115200;
	private InputStream input;
	private OutputStream output;
	private boolean debug = false;
	private byte[] uartBuffer = new byte[1000]; // buffer used in read and write
	private CommPort commPort = null;
	// operations
	private static final int MAX_COMMAND_SIZE = 26; // max number of bytes that
													// can be sent in 15 ms at
													// baud rate 19,200.
	private static final int COMMAND_START = 128; // Starts the OI. Must be the
													// first command sent.
	private static final SerialConnection theConnection = new SerialConnection();

	// Constructor of a serial connection between the iRobot and the IOIO board
	private SerialConnection() {
	}

	/**
	 * Gets a default serial connection to the iRobot. This method returns after
	 * a connection between the IOIO and the iRobot has been established.
	 * 
	 * @param ioio
	 *            the ioio instance used to connect to the iRobot
	 * @param debug
	 *            if true establishes a connection that prints out debugging
	 *            information.
	 * @return a serial connection to the iRobot
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public static SerialConnection getInstance(boolean debug)
			throws InterruptedException, IOException {
		if (debug) {
			System.out.println("Trying to connect.");
		}
		theConnection.debug = debug;
		boolean connected = false;
		int maxTries = 2;
		for (int numTries = 0; !connected && numTries < maxTries; numTries++) {
			try {
				theConnection.connectToIRobot();
				connected = true;
			} catch (IOException e) {
				if (numTries >= maxTries) {
					throw e;
				}
				if (debug) {
					System.out
							.println("Try connecting one more time in case user forgot to turn on the iRobot");
				}
				Thread.sleep(2500);
			}
		}
		return theConnection;

	}

	// Sends the start command to the iRobot
	private void connectToIRobot() throws IOException {

		CommPortIdentifier portIdentifier;
		try {
			portIdentifier = CommPortIdentifier
					.getPortIdentifier("/dev/ttyUSB0");
			commPort = portIdentifier.open(this.getClass().getName(), 10000);

			SerialPort serialPort = (SerialPort) commPort;
			serialPort.setSerialPortParams(BAUD_RATE, SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

			input = serialPort.getInputStream();
			output = serialPort.getOutputStream();

		} catch (NoSuchPortException | PortInUseException
				| UnsupportedCommOperationException e) {
			System.out.println(e.getClass().getName());
			System.out.println(e.getMessage());
		}

		// final int numberOfStartsToSend = MAX_COMMAND_SIZE;
		final int numberOfStartsToSend = 1;
		for (int i = 0; i < numberOfStartsToSend; i++) {
			uartBuffer[i] = (byte) COMMAND_START;
		}
		writeBytes(uartBuffer, 0, numberOfStartsToSend);
		if (debug) {
			System.out
					.println("Waiting for the iRobot to get into passive mode");
		}
		boolean waitingForAckFromIRobot = true;
		while (waitingForAckFromIRobot) {
			uartBuffer[0] = (byte) SENSOR_COMMAND;
			uartBuffer[1] = (byte) SENSORS_OI_MODE;
			writeBytes(uartBuffer, 0, 2);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
			int mode = readUnsignedByte();
			if (mode == OI_MODE_PASSIVE) {
				waitingForAckFromIRobot = false;
			}
		}
	}

	/**
	 * The maximum number of bytes that can be transmitted in a command to the
	 * iRobot
	 * 
	 * @return the max size in bytes
	 */
	public int getMaxCommandSize() {
		return MAX_COMMAND_SIZE;
	}

	/**
	 * Sets the debugging mode
	 * 
	 * @param debug
	 *            if true generates printouts to Log.
	 */
	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	/**
	 * Reads a byte received from the iRobot over the serial connection and
	 * interprets it as a signed byte, i.e., value is in the range -128 - 127.
	 * 
	 * @return the value as an int
	 * @throws IOException
	 * 
	 * 
	 */
	public int readSignedByte() throws IOException {
		int result = input.read();
		if (result > 0x7F) {
			result -= 0x100;
		}
		if (debug) {
			System.out.println(String.format("Read signed byte: %d", result));
		}
		return result;
	}

	/**
	 * Reads a byte received from the iRobot over the serial connection and
	 * interprets it as an unsigned byte, i.e., value is in range 0 - 255.
	 * 
	 * @return the value as an int
	 * @throws IOException
	 * 
	 */
	public int readUnsignedByte() throws IOException {
		int result = input.read();
		if (debug) {
			System.out.println(String.format("Read unsigned byte: %d", result));
		}
		return result;
	}

	/**
	 * Reads 2 bytes received from the iRobot over the serial connection and
	 * interprets them as a signed word, i.e., value is in range -32768 - 32767.
	 * 
	 * @return the value as an int
	 * @throws IOException
	 * 
	 */
	public int readSignedWord() throws IOException {
		int high = input.read(); // 0 <= high <= 0xFF
		int low = input.read(); // 0 <= low <= 0xFF
		int signed = (high << 8) | low; // 0 <= signed <= 0xFFFF
		// Convert signed to a signed value:
		if (signed > 0x7FFF) {
			signed -= 0x10000;
		}
		if (debug) {
			System.out.println(String.format("Read signed word: %d|%d = %d",
					high, low, signed));
		}
		return signed;
	}

	/**
	 * Reads several bytes received from the iRobot over the serial connection
	 * and interprets each as an unsigned byte, i.e., each value is in the range
	 * 0 - 255.
	 * 
	 * @param buffer
	 *            an array to store the read bytes
	 * @param start
	 *            offset into buffer
	 * @param length
	 *            the maximum bytes to read
	 * @return the number of bytes received
	 * @throws IOException
	 * 
	 */
	public int readUnsignedBytes(int[] buffer, int start, int length)
			throws IOException {
		if (debug) {
			System.out
					.println(String.format("Read unsigned bytes: %d", length));
		}
		if (length > uartBuffer.length) {
			uartBuffer = new byte[length];
		}
		int readCount = input.read(uartBuffer, 0, length);
		for (int i = 0; i < length; i++) {
			buffer[start + i] = uartBuffer[i] & 0xFF;
		}
		if (debug) {
			for (int i = 0; i < length; i++) {
				System.out.println(String.format("[%d] = %d", i, buffer[start
						+ i]));
			}
		}
		return readCount;
	}

	/**
	 * Reads 2 bytes received from the iRobot over the serial connection and
	 * interprets them as an unsigned word, i.e., value is in range 0 - 65535.
	 * 
	 * @return the value as an int
	 * @throws IOException
	 * 
	 */
	public int readUnsignedWord() throws IOException {
		int high = input.read(); // 0 <= high <= 0xFF
		int low = input.read(); // 0 <= low <= 0xFF
		int unsigned = (high << 8) | low; // 0 <= unsigned <= 0xFFFF
		if (debug) {
			System.out.println(String.format("Read unsigned word: %d|%d = %s",
					high, low, unsigned));
		}
		return unsigned;
	}

	/**
	 * Sends a byte over the serial connection to the iRobot.
	 * 
	 * @param b
	 *            the byte sent
	 * @throws IOException
	 * 
	 */
	public void writeByte(int b) throws IOException {
		if (debug) {
			System.out.println(String.format("Sending byte: %d", b));
		}
		output.write(b);
	}

	/**
	 * Sends several bytes over the serial connection to the iRobot
	 * 
	 * @param bytes
	 *            an array of bytes
	 * @param start
	 *            the position of first byte to be sent in the array
	 * @param length
	 *            the number of bytes sent.
	 * @throws IOException
	 * 
	 */
	public void writeBytes(byte[] bytes, int start, int length)
			throws IOException {
		if (debug) {
			System.out.println(String.format("Sending bytes byte[] length: %d",
					length));
			for (int i = 0; i < length; i++) {
				System.out.println(String.format("[%d] = %d", i, bytes[start
						+ i] & 0xFF));
			}
		}
		output.write(bytes, start, length);
	}

	/**
	 * Sends several bytes over the serial connection to the iRobot
	 * 
	 * @param ints
	 *            an array of ints that are cast to byte before sending
	 * @param start
	 *            the position of first byte to be sent in the array
	 * @param length
	 *            the number of bytes sent.
	 * @throws IOException
	 * 
	 */
	public void writeBytes(int[] ints, int start, int length)
			throws IOException {
		if (debug) {
			System.out.println(String.format("Sending bytes byte[] length: %d",
					length));
			for (int i = 0; i < length; i++) {
				System.out.println(String.format("[%d] = %d", i,
						ints[start + i]));
			}
		}
		if (length > uartBuffer.length) {
			uartBuffer = new byte[length];
		}
		for (int i = 0; i < length; i++) {
			uartBuffer[i] = (byte) ints[start + i];
		}
		output.write(uartBuffer, 0, length);
	}

	/**
	 * Sends a signed word to the iRobot over the serial connection as two
	 * bytes, high byte first.
	 * 
	 * @param value
	 *            an int in the range -32768 - 32767.
	 * @throws IOException
	 * 
	 */
	public void writeSignedWord(int value) throws IOException {
		// Java bit representation is already two's complement
		uartBuffer[0] = (byte) (value >> 8);
		uartBuffer[1] = (byte) (value & 0xFF);
		output.write(uartBuffer, 0, 2);
		if (debug) {
			System.out.println("Sending signed word: " + value);
		}
	}

	/**
	 * Sends an unsigned word to the iRobot over the serial connection as two
	 * bytes, high byte first.
	 * 
	 * @param value
	 *            an int in the range 0 - 65535.
	 * @throws IOException
	 * 
	 */
	public void writeUnsignedWord(int value) throws IOException {
		uartBuffer[0] = (byte) (value >> 8);
		uartBuffer[1] = (byte) (value & 0xFF);
		output.write(uartBuffer, 0, 2);
		if (debug) {
			System.out.println("Sending unsigned word: " + value);
		}
	}

	/**
	 * Closes the serial connection
	 */
	public void close() {
		if (debug) {
			System.out.println("Closing connection");
		}
		if (commPort != null) {
			commPort.close();
		}
		commPort = null;
	}
}
