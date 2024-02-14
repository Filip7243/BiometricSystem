# Room access control system based on finger biometrics

The project aims to develop an access control system for company premises by utilizing scans of new employees' fingers. Upon employment, four fingers of the hand are scanned, and the employee is assigned to specific areas. To gain access to a particular room, an employee randomly scans one of the four fingers, which are then compared to the database. The system identifies the employee with the highest matching score and verifies their access. This solution aims to monitor employee presence and enhance security by ensuring access only to authorized individuals. Additionally, it enables quick response in emergency situations by immediately restricting access to certain areas of the company. It may also prove useful in security audits and internal investigations.

## Used tools

- Java 17
- MegaMatcherSDK (Biometric SDK)
- Java Swing
- JDBC
- MySQL
- Gradle

I used in project GUI template from MegaMatcherSDK and modified it.
I removed unnecessary windows and replace them with mine. 

## Licence

If you want to run the project you need to have MegaMatcherSDK licence active. You can use the trial version here: [MegaMatcherSDK](https://www.neurotechnology.com/download.html#megamatcher_verifinger_verilook_verieye_sdk_trial)
