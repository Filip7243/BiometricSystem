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

## WHAT IS MEGAMATCHER SDK?
MegaMatcher technology is intended for large-scale AFIS and multi-biometric systems developers. 
The technology ensures high reliability and speed of biometric identification even when using large
databases.

MegaMatcher is available as a software development kit that allows development of large-scale
fingerprint, face or multi-biometric face-fingerprint identification products for Microsoft Windows 
and Linux platforms. 

## Licence

If you want to run the project you need to have MegaMatcherSDK licence activated. You can use the trial version here: [MegaMatcherSDK](https://www.neurotechnology.com/download.html#megamatcher_verifinger_verilook_verieye_sdk_trial). To active follow the instructions that are in documentation.

## How to run

You need to have MegaMatcherSDK licence activated. Then move Licenses directory (SDK_DIR\\BIN\\Licences) to parent directory of the project (..\\Licences).

To create database use this [DB_build_script](architecture/DB_build_script.sql)

After all execute command `gradle run` in project directory.

You also need some fingerprint scanner with drivers. I used optical [scanner](https://www.futronic-tech.com/pro-detail.php?pro_id=1543)

## GUI


