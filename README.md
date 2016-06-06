#RPiGreenbot

This is a Java implementation of the _iRobot Create 2 Open Interface_ that uses the USB-serial cable that comes with the Create 2. The USB side of the cable can be plugged into any computer running Java 7. The software has been tested with success with a Raspberry Pi 2. 

To use, [RXTX](http://rxtx.qbang.org/pub/rxtx/rxtx-2.1-7-bins-r2.zip) needs to be installed on the computer communicating with the Create 2. In addition, RXTXComm.jar, which is included in the above referenced zip file, needs to be included in the class path for the code to compile. 

[CleverRPiRobot](https://github.com/jointheleague/CleverRPiRobot) provides an example of usage of this library project. Note that the CleverRPiRobot project references the _source_ of the RPiGreenbot project. 