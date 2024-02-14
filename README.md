# Room access control system based on finger biometrics

The project aims to develop an access control system for company premises by utilizing scans of new employees' fingers. Upon employment, four fingers of the hand are scanned, and the employee is assigned to specific areas. To gain access to a particular room, an employee randomly scans one of the four fingers, which are then compared to the database. The system identifies the employee with the highest matching score and verifies their access. This solution aims to monitor employee presence and enhance security by ensuring access only to authorized individuals. Additionally, it enables quick response in emergency situations by immediately restricting access to certain areas of the company. It may also prove useful in security audits and internal investigations.

System only simulates the entrance to the rooms, but it is an ideal basis for developing this project in the context of creating a real system. 

## Used tools

- Java 17
- MegaMatcherSDK (Biometric SDK)
- Java Swing
- JDBC
- XAMMP
- MySQL
- Gradle

I used in project GUI template from MegaMatcherSDK and modified it.
I removed unnecessary windows and replace them with mine.

## What is MegaMatcherSDK?

MegaMatcher technology is intended for large-scale AFIS and multi-biometric systems developers. 
The technology ensures high reliability and speed of biometric identification even when using large
databases.

MegaMatcher is available as a software development kit that allows development of large-scale
fingerprint, face or multi-biometric face-fingerprint identification products for Microsoft Windows 
and Linux platforms. 

## Licence

If you want to run the project you need to have MegaMatcherSDK licence activated. You can use the trial version here: [MegaMatcherSDK](https://www.neurotechnology.com/download.html#megamatcher_verifinger_verilook_verieye_sdk_trial). To active licence follow the instructions that are in documentation.

## How to run

You need to have MegaMatcherSDK licence activated. Then move Licenses directory (SDK_DIR\\BIN\\Licences) to parent directory of the project (..\\Licences).

To create database with tables use this [script](architecture/DB_build_script.sql).

After all execute command `gradle run` in project directory.

You also need some fingerprint scanner with drivers. I used optical [scanner](https://www.futronic-tech.com/pro-detail.php?pro_id=1543)

If you want to know more about biometrics and project look at the [documentation](architecture/DOCS_PL.pdf) (Polish language).

# UseCase Diagram

![UseCase_Diagram](architecture/UseCase_Diagram.png?raw=true)

# ERD Diagram

![ERD_DIAGRAM](architecture/DB_diagram.png?raw=true)

## GUI

![GUI_1](architecture/GUI_1.jpg?raw=true)
![GUI_1](architecture/GUI_2.jpg?raw=true)
