
package org.jointheleague.ecolban.rpirobot;

import java.io.IOException;

/**
 * A concrete class that provides a default implementation of the
 * IRobotInterface. It is a convenience class intended to be extended in order
 * to define customized implementations of the IRobotInterface.
 * 
 * @author Erik Colban &copy; 2016
 */
    public class IRobotAdapter implements IRobotInterface {

    /**
     * The decorated instance. All calls on the methods of the IRobotInterface
     * are forwarded to this instance.
     */
    protected final IRobotInterface delegate;

    /**
     * Makes a new instance from an IRobotInterface implementation instance.
     *
     * @param delegate
     *            a non-null instance of IRobotInterface to which method calls
     *            are forwarded.
     */
    public IRobotAdapter(IRobotInterface delegate) {
        if (delegate == null) {
            throw new IllegalArgumentException("Argument must be non-null");
        }
        this.delegate = delegate;
    }

    @Override
    public void drive(int velocity, int radius) throws IOException {
        delegate.drive(velocity, radius);
    }

    @Override
    public void driveDirect(int leftVelocity, int rightVelocity) throws IOException {
        delegate.driveDirect(leftVelocity, rightVelocity);
    }

    @Override
    public void full() throws IOException {
        delegate.full();
    }

    @Override
    public int getAngle() {
        return delegate.getAngle();
    }

    @Override
    public int getBatteryCapacity() {
        return delegate.getBatteryCapacity();
    }

    @Override
    public int getBatteryCharge() {
        return delegate.getBatteryCharge();
    }

    @Override
    public int getBatteryTemperature() {
        return delegate.getBatteryTemperature();
    }

    @Override
    public int getChargingState() {
        return delegate.getChargingState();
    }

    @Override
    public int getCliffSignalLeftFront() {
        return delegate.getCliffSignalLeftFront();
    }

    @Override
    public int getCliffSignalRightFront() {
        return delegate.getCliffSignalRightFront();
    }

    @Override
    public int getCliffSignalLeft() {
        return delegate.getCliffSignalLeft();
    }

    @Override
    public int getCliffSignalRight() {
        return delegate.getCliffSignalRight();
    }

    @Override
    public int getCurrent() {
        return delegate.getCurrent();
    }

    @Override
    public int getDistance() {
        return delegate.getDistance();
    }

    @Override
    public int getInfraredByte() {
        return delegate.getInfraredByte();
    }

    @Override
    public int getInfraredByteLeft() {
        return delegate.getInfraredByteLeft();
    }

    @Override
    public int getInfraredByteRight() {
        return delegate.getInfraredByteRight();
    }

    @Override
    public int getOiMode() {
        return delegate.getOiMode();
    }

    @Override
    public int getRequestedVelocityLeft() {
        return delegate.getRequestedVelocityLeft();
    }

    @Override
    public int getRequestedRadius() {
        return delegate.getRequestedRadius();
    }

    @Override
    public int getRequestedVelocityRight() {
        return delegate.getRequestedVelocityRight();
    }

    @Override
    public int getRequestedVelocity() {
        return delegate.getRequestedVelocity();
    }

    @Override
    public int getSongNumber() {
        return delegate.getSongNumber();
    }

    @Override
    public int getVoltage() {
        return delegate.getVoltage();
    }

    @Override
    public int getWallSignal() {
        return delegate.getWallSignal();
    }

    @Override
    public boolean isBumpLeft() {
        return delegate.isBumpLeft();
    }

    @Override
    public boolean isBumpRight() {
        return delegate.isBumpRight();
    }

    @Override
    public boolean isCliffFrontLeft() {
        return delegate.isCliffFrontLeft();
    }

    @Override
    public boolean isCliffFrontRight() {
        return delegate.isCliffFrontRight();
    }

    @Override
    public boolean isCliffLeft() {
        return delegate.isCliffLeft();
    }

    @Override
    public boolean isCliffRight() {
        return delegate.isCliffRight();
    }

    @Override
	public boolean isCleanButtonDown() {
		return delegate.isCleanButtonDown();
	}

	@Override
    public boolean isHomeBaseChargerAvailable() {
        return delegate.isHomeBaseChargerAvailable();
    }

    @Override
    public boolean isInternalChargerAvailable() {
        return delegate.isInternalChargerAvailable();
    }

    @Override
    public boolean isLeftWheelOvercurrent() {
        return delegate.isLeftWheelOvercurrent();
    }

    @Override
	public boolean isLightBump() {
		return delegate.isLightBump();
	}

	@Override
    public boolean isWheelOvercurrentMainBrush() {
        return delegate.isWheelOvercurrentMainBrush();
    }

    @Override
    public boolean isSpotButtonDown() {
        return delegate.isSpotButtonDown();
    }

    @Override
    public boolean isRightWheelOvercurrent() {
        return delegate.isRightWheelOvercurrent();
    }

    @Override
    public boolean isSongPlaying() {
        return delegate.isSongPlaying();
    }

    @Override
    public boolean isVirtualWall() {
        return delegate.isVirtualWall();
    }

    @Override
    public boolean isWall() {
        return delegate.isWall();
    }

    @Override
    public boolean isWheelDropLeft() {
        return delegate.isWheelDropLeft();
    }

    @Override
    public boolean isWheelDropRight() {
        return delegate.isWheelDropRight();
    }

    @Override
	public boolean isWheelOvercurrentSideBrush() {
		return delegate.isWheelOvercurrentSideBrush();
	}

	@Override
    public void leds(int powerColor, int powerIntensity, boolean spotLedOn) throws IOException {
        delegate.leds(powerColor, powerIntensity, spotLedOn);
    }

    @Override
	public void ledsToggle(boolean togglePower) throws IOException {
		delegate.ledsToggle(togglePower);
		
	}

	@Override
    public void playSong(int songNumber) throws IOException {
        delegate.playSong(songNumber);
    }

    @Override
    public void readSensors(int sensorId) throws IOException {
        delegate.readSensors(sensorId);
    }

    @Override
    public void reset() throws IOException {
        delegate.reset();
    }

    @Override
    public void safe() throws IOException {
        delegate.safe();
    }

    @Override
    public void song(int songNumber, int[] notesAndDurations) throws IOException {
        delegate.song(songNumber, notesAndDurations);
    }

    @Override
    public void song(int songNumber, int[] notesAndDurations, int startIndex, int length) throws IOException {
        delegate.song(songNumber, notesAndDurations, startIndex, length);
    }

    @Override
    public void stop() throws IOException {
        delegate.stop();
    }

    @Override
    public void waitButtonPressed(boolean beep) throws IOException {
        delegate.waitButtonPressed(beep);
    }

    @Override
    public void closeConnection() {
        delegate.closeConnection();
    }

    @Override
    public int getEncoderCountLeft() {
        return delegate.getEncoderCountLeft();
    }

    @Override
    public int getEncoderCountRight() {
        return delegate.getEncoderCountRight();
    }

    @Override
    public int getMotorCurrentLeft() {
        return delegate.getMotorCurrentLeft();
    }

    @Override
    public int getMotorCurrentRight() {
        return delegate.getMotorCurrentRight();
    }

    @Override
    public boolean isStasis() {
        return delegate.isStasis();
    }

    @Override
    public int[] getLightBumps() {
        return delegate.getLightBumps();
    }
}
