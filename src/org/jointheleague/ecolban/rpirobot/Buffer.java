package org.jointheleague.ecolban.rpirobot;

/**
 * This class combines two classical examples: the ring buffer and the producer-consumer pattern. This class is thread
 * safe.
 * 
 * @author ecolban
 * 
 */
public final class Buffer implements BufferInterface {

	private final byte[] buffer;
	private final int capacity;
	private int first = 0;
	private int last = 0;
	private volatile boolean empty = true;
	private volatile boolean full = false;

	/**
	 * Constructs an instance.
	 * 
	 * @param capacity
	 *            the capacity of the buffer
	 */
	public Buffer(int capacity) {
		this.capacity = capacity;
		buffer = new byte[capacity];
	}

	/**
	 * Removes the first element from the buffer
	 * 
	 * @return the number that was removed
	 * 
	 * @throws InterruptedException
	 *             if interrupted
	 */
	public synchronized byte remove() throws InterruptedException {
		while (empty) {
			wait();
		}
		first = (first + 1) % capacity;
		empty = first == last;
		full = false;
		// System.out.println(this + "==>" + buffer[first]);
		notifyAll();
		return buffer[first];
	}

	/**
	 * Adds a number to the end of the buffer.
	 * 
	 * @param n
	 *            the number added
	 * 
	 * @throws InterruptedException
	 *             if interrupted
	 */
	public synchronized void add(byte n) throws InterruptedException {
		while (isFull()) {
			wait();
		}
		last = (last + 1) % capacity;
		buffer[last] = n;
		empty = false;
		full = first == last;
		notifyAll();
	}

	public synchronized int peek() throws InterruptedException {
		while (empty) {
			wait();
		}
		return buffer[(first + 1) % capacity];
	}

	/**
	 * Gets the count of elements in the buffer
	 * 
	 * @return the count
	 */
	public synchronized int getCount() {
		if (empty) {
			return 0;
		} else if (first < last) {
			return last - first;
		} else {
			return last - first + capacity;
		}
	}

	@Override
	public synchronized String toString() {
		if (empty)
			return "[] ";
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		int i = (first + 1) % capacity;
		while (i != last) {
			sb.append(buffer[i]);
			sb.append(", ");
			i = (i + 1) % capacity;
		}
		sb.append(buffer[i]);
		sb.append("] ");
		return sb.toString();
	}

	@Override
	public boolean isEmpty() {
		return empty;
	}

	@Override
	public boolean isFull() {
		return full;
	}
}