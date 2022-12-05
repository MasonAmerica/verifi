Copy or push dog_300kb.png into sdcard folder first before running this app.
adb push dog_300kb.png sdcard

Select the test and interval of the test then click on "Start" to start the test.
The status of each test will be displayed on the Status window/page.
Click on "Stop" to stop the test

The data connection test is making HTTPS file (dog_300kb.png) upload request to: 
https://webhook.site/#!/470b12f5-3954-42c3-b986-1c0957f438c7/e5c97658-1db4-42bf-90ec-8a437e0ac4f7/2

Open that site to see the uploaded file and the interval of each upload.


To overcome the Doze mode, add the following sysconfig using Controller or Mason CLI:
apps:
  - name: verifi
    package_name: com.example.verifi
    version_code: latest

os:
  name: verifi_app_config
  version: 1
  configurations:
    mason-sysconfig:
      allow_in_power_save_packages:
        - com.example.verifi
      allow_unthrottled_location:
        - com.example.verifi        
