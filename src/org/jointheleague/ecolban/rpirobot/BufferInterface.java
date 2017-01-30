package org.jointheleague.ecolban.rpirobot;

/**
 * 
 * @author ecolban
 * 
 */
public interface BufferInterface {

	/**
	 * Removes the first element from the buffer
	 * 
	 * @return the number that was removed
	 * 
	 * @throws InterruptedException
	 *             if interrupted
	 */
	public byte remove() throws InterruptedException;

	/**
	 * Adds a number to the end of the buffer.
	 * 
	 * @param x
	 *            the number added
	 * @throws InterruptedException
	 *             if interrupted
	 */
	public void add(byte x) throws InterruptedException;

	/**
	 * 
	 * @return the first element in the buffer without removing it.
	 * @throws InterruptedException
	 */
	public int peek() throws InterruptedException;

	/**
	 * Gets the count of elements in the buffer
	 * 
	 * @return the number of elements in the buffer
	 */
	public int getCount();

	/**
	 * 
	 * @return true if the buffer is empty, false otherwise
	 */
	public boolean isEmpty();

	/**
	 * 
	 * @return true if the buffer is full, false otherwise
	 */
	public boolean isFull();

}