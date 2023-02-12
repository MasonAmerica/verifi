ABOUT

Verifi is a test application to showcase the battery life performance of a Mason A4100 device
when performing certain features or combination of features.

The application is currently offering the following test feature:
1. GPS using either Android Location Manager or Qualcomm Fused Location Provider
2. Sensor using either Heart rate, off body, and ECG sensor.
3. Data connection using either cellular or Wifi connectivity.

More test features will be added in the future.

The application allows users to select the test feature(s) and specify the time interval
to periodically perform the test.

The status of each test will be shown on the status screen and displayed as a running list of status
updates along with the timestamp of when the test is performed.

To measure battery life, users need to charge the battery to 100% then start the test.
Take note of the time when the first test is started and the time when the battery level is near 0%.

The following android permissions are requested and required to be granted:
1. Location 
2. Sensors
3. External storage access

To start and stop the test:
Select the test feature and interval of the test then click on "Start" to start the test.
The status of each test will be displayed on the Status screen.
Click on "Stop" to stop the test

For GPS testing, the device needs to be located in an open sky area to avoid blockage from 
satellite signals.

For sensor tests such as heart rate and off body detection, the device needs to be placed on the 
wrist or in contact with the user's fingers. For ECG sensor test, the user's thumbs must be gently 
placed on each of the sensor plates under the device and a finger must be placed on the silver 
button on the side of the device. Avoid movement and do not press too hard.

For data connection test, the app will post HTTPS file upload request to an URL provided by
webhook.site, for example:
https://webhook.site/27e920d2-c12f-4a73-9a21-2bd6ed2aef72

This webhook.site URL will expire after 30 days of first use or after 500 requests.
To get a new URL address, go to https://webhook.site from your web browser and
copy the "post" URL address into the code.
The new URL address should be pasted to the URL_TEST_ADDRESS string in the DataConnTest.java file.

For testing data connection using cellular, disable Wifi because its the default connectivity. 

BUILD APK

To compile and generate APK:
1. Install and open Verifi folder using Android Studio Dolphin or later version
2. The minimum SDK is 27. Target and Compile SDK are 30
3. Rebuild project and install the apk under app/build/intermediates/apk folder using adb
4. or Select "Run Mason_Verifi"


MASON CONFIG

To overcome the Android Doze mode limitation, add the following config using
Mason Controller or CLI:
apps:
- name: verifi
  package_name: com.mason.verifi
  version_code: latest

os:
name: verifi_app_config
version: 1
configurations:
mason-sysconfig:
allow_in_power_save_packages:
- com.mason.verifi
allow_unthrottled_location:
- com.mason.verifi


ADD NEW TEST

To add a new test:
1. Add new test feature class file (NewTest.java)
2. Add new test alarm class file (NewTestAlarm.java) if needed for periodicity

And edit the following files:

1. fragment_configure.xml - add new test widgets.
2. ConfigureFragment.java - add new test setting initialization and register widget event listener.
3. MainActivity.java      - add runtime permission request if needed.
4. MainService.java       - add a new test alarm (if needed) and start/stop test caller.
5. TestPreference.java    - add new test preference variables and get/set functions.
6. TestScheduler.java     - add new test option type, object creation, and function handlers.


SUPPORT

Send questions to: erichandojo@bymason.com 