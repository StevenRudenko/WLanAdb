WLanAdb
=======

WLanAdb is Wireless Android Development Bridge that allows to use standard ADB commands without connecting Android device to desktop by USB cable, installing drivers and without need to root it.

How to start using WLanAdb
-------------------

1. Install Android client from Google Play to any number of Android devices you want use with WLanAdb.
2. Get desktop client from Downloads section.
3. Check that desktop client and mobile device belong to one subnetwork.
4. Launch WLanAdbTerminal devices from command line and check output.

    ```
    $ WLanAdbTerminal devices
    1 device(s) found:
    1) 39334809547D00EC My device - Nexus S (4.1.1)
    ```

    ! If you don't see your device on the list, check that it really belongs to same subnetwork as your desktop.

5. If there is device on list you are ready to use these ADB commands: logcat, push, install.

    ! Try WLanAdbTerminal help from command line to find out more details.

Security
-------------------

1. PIN: allows to set keyword to protect from unauthorized connection to your mobile device.
2. Trusted Hostpots: it is a list of trusted hotspots where WLanAdb service will work. All other hotspots will be ignored. This allows you to enable WLanAdb for home and work networks ignoring public hotspots.


Download application
-------------------
- Application for Android is published on the Play Store: [WLanAdb][1].
- Desktop clients can be downloaded from http://wlanadb.com/


-------------------------------------------------------------------------------

Developed By
============

* Steven Rudenko - <steven.rudenko@gmail.com>

License
=======

    Copyright 2013 Steven Rudenko

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
    
 [1]: https://play.google.com/store/apps/details?id=com.wlanadb
 
