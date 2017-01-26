package org.jointheleague.ecolban.rpirobot;

import java.io.IOException;
import java.util.Arrays;

import com.pi4j.io.serial.Baud;
import com.pi4j.io.serial.DataBits;
import com.pi4j.io.serial.FlowControl;
import com.pi4j.io.serial.Parity;
import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialConfig;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataEventListener;
import com.pi4j.io.serial.SerialFactory;
import com.pi4j.io.serial.SerialPort;
import com.pi4j.io.serial.StopBits;
import com.pi4j.system.SystemInfo.BoardType;

/**
 * This class represents the communication channel between the computer (e.g., Raspberry Pi) and the iRobot. At most one
 * instance of this class may be instantiated. Use the {@link #getInstance(boolean) } method to get that instance.
 * 
 * @author Erik Colban &copy; 2016
 * 
 */
public final class SerialConnection {

	private static final int OI_MODE_PASSIVE = 1;
	private static final int SENSORS_OI_MODE = 35;
	private static final int SENSOR_COMMAND = 142;
	private static final Baud BAUD_RATE = Baud._115200;
	private Serial serial;
	private boolean debug = true;
	private byte[] writeBuffer = new byte[128];
	private byte[] receiveBuffer;
	// operations
	private static final int MAX_COMMAND_SIZE = 26; // max number of bytes that
													// can be sent in 15 ms at
													// baud rate 19,200.
	private static final int COMMAND_START = 128; // Starts the OI. Must be the
													// first command sent.

	// Constructor of a serial connection between the iRobot and the IOIO board
	private SerialConnection() {

	}

	static SerialConnection theConnection = new SerialConnection();

	public static SerialConnection getInstance() throws IOException, InterruptedException {
		theConnection.initialize();
		return theConnection;
	}

	private void initialize() throws IOException, InterruptedException {
		serial = SerialFactory.createInstance();

		// create serial config object
		SerialConfig config = new SerialConfig();

		// set default serial settings (device, baud rate, flow control,
		// etc)
		//
		// by default, use the DEFAULT com port on the Raspberry Pi (exposed
		// on GPIO header)
		// NOTE: this utility method will determine the default serial port
		// for the
		// detected platform and board/model. For all Raspberry Pi models
		// except the 3B, it will return "/dev/ttyAMA0". For Raspberry Pi
		// model 3B may return "/dev/ttyS0" or "/dev/ttyAMA0" depending on
		// environment configuration.
		config.device(SerialPort
				.getDefaultPort(BoardType.RaspberryPi_2B))
				.baud(BAUD_RATE).dataBits(DataBits._8)
				.parity(Parity.NONE).stopBits(StopBits._1)
				.flowControl(FlowControl.NONE);

		System.out.println("config = " + config);

		// create and register the serial data listener
		serial.addListener(new SerialDataEventListener() {

			@Override
			public void dataReceived(SerialDataEvent event) {
				try {
					receiveBytes(event.getBytes());
				} catch (IOException e) {
					System.out.println(e);
				}

			}
		});

		serial.open(config);

		// display connection details
		System.out.println("Connecting to: " + config.toString());

		if (debug) {
			System.out.println("Trying to connect.");
		}
		boolean connected = false;
		int maxTries = 2;
		for (int numTries = 0; !connected && numTries < maxTries; numTries++) {
			try {
				connectToIRobot();
				connected = true;
			} catch (IOException e) {
				if (numTries >= maxTries) {
					throw e;
				}
				if (debug) {
					System.out.println("Try connecting one more time in case user forgot to turn on the iRobot");
				}
				Thread.sleep(2500);
			}
		}

	}

	private synchronized void receiveBytes(byte[] bytes) {
		receiveBuffer = bytes;
		System.out.print("RX: ");
		for (int i = 0; i < bytes.length; i++) {
			System.out.print((int) bytes[i]);
			System.out.print(", ");
		}
		System.out.println();
		System.out.println("RX: " + Arrays.toString(receiveBuffer));
		notifyAll();
	}

	/**
	 * Gets a default serial connection to the iRobot. This method returns after a connection between the IOIO and the
	 * iRobot has been established.
	 * 
	 * @param ioio
	 *            the ioio instance used to connect to the iRobot
	 * @param debug
	 *            if true establishes a connection that prints out debugging information.
	 * @return a serial connection to the iRobot
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public static SerialConnection getInstance(boolean debug) throws InterruptedException, IOException {
		System.out.println("Debugging = " + debug);
		theConnection.initialize();
		theConnection.debug = debug;
		return theConnection;
	}

	// Sends the start command to the iRobot
	private void connectToIRobot() throws IOException, InterruptedException {

		// final int numberOfStartsToSend = MAX_COMMAND_SIZE;
		final int numberOfStartsToSend = 1;
		for (int i = 0; i < numberOfStartsToSend; i++) {
			writeBuffer[i] = (byte) COMMAND_START;
		}
		serial.write(writeBuffer, 0, numberOfStartsToSend);
		if (debug) {
			System.out.println("Waiting for the iRobot to get into passive mode");
		}
		boolean waitingForAckFromIRobot = true;
		while (waitingForAckFromIRobot) {
			writeBuffer[0] = (byte) SENSOR_COMMAND;
			writeBuffer[1] = (byte) SENSORS_OI_MODE;
			serial.write(writeBuffer, 0, 2);
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
	 * The maximum number of bytes that can be transmitted in a command to the iRobot
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
	 * Reads a byte received from the iRobot over the serial connection and interprets it as a signed byte, i.e., value
	 * is in the range -128 - 127.
	 * 
	 * @return the value as an int
	 * @throws IOException
	 * @throws InterruptedException
	 * 
	 * 
	 */
	public synchronized int readSignedByte() throws IOException {

		while (receiveBuffer == null) {
			try {
				wait();
			} catch (InterruptedException e) {
			}
		}
		int result = receiveBuffer[0];
		receiveBuffer = null;
		if (debug) {
			System.out.println(String.format("Read signed byte: %d", result));
		}
		return result;
	}

	/**
	 * Reads a byte received from the iRobot over the serial connection and interprets it as an unsigned byte, i.e.,
	 * value is in range 0 - 255.
	 * 
	 * @return the value as an int
	 * @throws IOException
	 * @throws InterruptedException
	 * 
	 */
	public synchronized int readUnsignedByte() throws IOException {
		while (receiveBuffer == null) {
			try {
				wait();
			} catch (InterruptedException e) {
			}
		}
		int result = receiveBuffer[0] & 0xFF;
		receiveBuffer = null;
		if (debug) {
			System.out.println(String.format("Read unsigned byte: %d", result));
		}
		return result;
	}

	/**
	 * Reads 2 bytes received from the iRobot over the serial connection and interprets them as a signed word, i.e.,
	 * value is in range -32768 - 32767.
	 * 
	 * @return the value as an int
	 * @throws IOException
	 * @throws InterruptedException
	 * 
	 */
	public synchronized int readSignedWord() throws IOException {
		while (receiveBuffer == null) {
			try {
				wait();
			} catch (InterruptedException e) {
			}
		}
		if (receiveBuffer.length < 2)
			return 0;
		int result = receiveBuffer[0] << 8 | receiveBuffer[1];
		receiveBuffer = null;
		if (debug) {
			System.out.println(String.format("Read signed word: %d", result));
		}
		return result;
	}

	/**
	 * Reads 2 bytes received from the iRobot over the serial connection and interprets them as an unsigned word, i.e.,
	 * value is in range 0 - 65535.
	 * 
	 * @return the value as an int
	 * @throws IOException
	 * @throws InterruptedException
	 * 
	 */
	public synchronized int readUnsignedWord() throws IOException {
		while (receiveBuffer == null) {
			try {
				wait();
			} catch (InterruptedException e) {
			}
		}
		if (receiveBuffer.length < 2)
			return 0;
		int result = (receiveBuffer[0] << 8 | receiveBuffer[1]) & 0xFFFF;
		receiveBuffer = null;
		if (debug) {
			System.out.println(String.format("Read unsigned word = %d", result));
		}
		return result;
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
		serial.write((byte) b);
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
	public void writeBytes(int[] ints, int start, int length) throws IOException {
		if (debug) {
			System.out.println(String.format("Sending bytes byte[] length: %d", length));
			for (int i = 0; i < length; i++) {
				System.out.println(String.format("[%d] = %d", i, ints[start + i]));
				writeBuffer[i] = (byte) ints[start + i];
			}
		}
		serial.write(writeBuffer, 0, length);
	}

	/**
	 * Sends a signed word to the iRobot over the serial connection as two bytes, high byte first.
	 * 
	 * @param value
	 *            an int in the range -32768 - 32767.
	 * @throws IOException
	 * 
	 */
	public void writeSignedWord(int value) throws IOException {
		// Java bit representation is already two's complement
		writeBuffer[0] = (byte) (value >> 8);
		writeBuffer[1] = (byte) (value & 0xFF);
		serial.write(writeBuffer, 0, 2);
		if (debug) {
			System.out.println("Sending signed word: " + value);
		}
	}

	/**
	 * Sends an unsigned word to the iRobot over the serial connection as two bytes, high byte first.
	 * 
	 * @param value
	 *            an int in the range 0 - 65535.
	 * @throws IOException
	 * 
	 */
	public void writeUnsignedWord(int value) throws IOException {
		// Java bit representation is already two's complement
		writeBuffer[0] = (byte) (value >> 8);
		writeBuffer[1] = (byte) (value & 0xFF);
		serial.write(writeBuffer, 0, 2);
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
		if (serial.isClosed())
			return;
		try {
			serial.flush();
			serial.close();
		} catch (IllegalStateException | IOException e) {
			System.out.println(e.toString());
		}

	}
}
