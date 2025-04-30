# Project_pepper

README

# Requirement for running Pepper SDK

PepperSDK (QiSDK)
Android Studio Bumblebee | 2021.1.1 Patch 3 April 7, 2022

📌 Project Overview

PepperProject is an Android-based application designed for the SoftBank Robotics Pepper Robot. It integrates with Pepper Simulator 7.0 to enable testing without a physical robot. This guide provides a step-by-step setup, build instructions, and troubleshooting tips.

 

📂 Project Structure


```

PepperProject/
│-- app/                    # Main Android app source code
│-- gradle/                 # Gradle wrapper files
│-- .gradle/                # Gradle cache (DO NOT MODIFY)
│-- build/                  # Compiled files
│-- settings.gradle         # Project settings
│-- build.gradle            # Root Gradle build file
│-- local.properties        # Local environment variables
│-- pepper_simulator/       # Pepper Simulator 7.0 integration
│-- gradlew                 # Gradle wrapper script (Linux/Mac)
│-- gradlew.bat             # Gradle wrapper script (Windows)
🔧 Setup Instructions
```
1️⃣ Prerequisites

Before starting, ensure you have the following installed:

✅ Java Development Kit (JDK) Version 1.8 or Higher
✅ Android Studio Bumblebee | 2021.1.1 Patch 3 April 7, 2022
✅ Gradle (Built-in with Android Studio)
✅ Git (for version control)
✅ Pepper Simulator 7.0 (SoftBank Robotics)

Check installation versions:



sh
java -version 

gradle -v 

2️⃣ Cloning the Project

Clone the repository from GitHub:



sh
git clone https://github.com/009Rambo/Project_pepper.git cd PepperProject 

3️⃣ Open Project in Android Studio

Open Android Studio

Click "Open", navigate to the PepperProject folder, and select it.

Wait for Gradle sync to complete.

🤖 Setting Up Pepper Simulator 7.0

Pepper Simulator 7.0 allows testing without a physical robot.

📥 Installing Pepper Simulator

Download Pepper Simulator 7.0 from SoftBank Robotics:
SoftBank Developer Portal

Install using:

Windows: Run the .exe installer

Mac/Linux: Run the .sh installer

🚀 Running the Simulator

Start Pepper Simulator with:



sh
qisim --verbose 

This should launch a virtual Pepper Robot in a simulated environment.

📡 Connecting Android App to Pepper Simulator (optional)

Open Pepper Simulator

Note the virtual robot's IP address (shown in the simulator)

In MainActivity.java, update the connection settings:



java
String pepperIp = "192.168.X.X";  // Replace with the IP from the simulator Robot myRobot = new Robot(pepperIp); 

Run the app using:



sh
./gradlew assembleDebug adb install app/build/outputs/apk/debug/app-debug.apk 

⚙️ Building and Running

4️⃣ Clean and Sync Gradle

If the project fails to sync, run:



sh
./gradlew clean 

./gradlew build 

OR inside Android Studio:

File > Sync Project with Gradle Files

If errors occur, try File > Invalidate Caches & Restart

5️⃣ Running the App

To run the app on an emulator or a connected device:

Select a device in the Device Manager

Click the Run (▶️) button in Android Studio

OR use:



sh
./gradlew assembleDebug adb install app/build/outputs/apk/debug/app-debug.apk 

🛠️ Troubleshooting Guide

🔴 Gradle Sync Issues

If you see errors like "Project directory is not part of the build", try:

Open settings.gradle and ensure it includes:



gradle
rootProject.name = "PepperProject" include ':app' 

Delete .gradle and .idea folders:



sh
rm -rf .gradle .idea 

Restart Android Studio and sync again.

🔴 Emulator Not Starting

If the emulator fails to start:

Go to AVD Manager (Tools > Device Manager)

Delete the existing emulator and create a new one

Ensure Intel HAXM is installed (SDK Manager > SDK Tools)

🔴 Pepper Simulator Not Connecting

If the app can't connect to Pepper Simulator 7.0:

Ensure both Android device and Pepper Simulator are on the same network

Try restarting the simulator using:



sh
qisim --verbose 

 

Confirm Pepper's IP address and update the app's connection settings.

📜 Useful Gradle Commands

Command

Description

./gradlew build

Build the project

./gradlew clean

Clean the project files

./gradlew assembleDebug

Create a debug APK

./gradlew assembleRelease

Create a release APK

./gradlew dependencies

List all project dependencies

./gradlew --refresh-dependencies

Refresh Gradle dependencies

📦 Dependencies

All dependencies are defined inside app/build.gradle:



gradle
dependencies {     implementation 'com.android.support:appcompat-v7:28.0.0'     

implementation 'com.aldebaran.qi:sdk:7.0.0'  // Pepper SDK     

implementation 'com.google.code.gson:gson:2.8.9'

 } 

To add new dependencies, update this file and run:



sh
./gradlew build 

📌 Deployment

Generating a Signed APK

Go to Build > Generate Signed Bundle / APK

Select APK

Create or select an existing Keystore

Choose release build and finish

Alternatively, use:



sh
./gradlew assembleRelease 

The APK will be located in:



arduino
app/build/outputs/apk/release/app-release.apk 

📜 Version Control (Git)

💾 Saving Changes



sh
git add . git commit -m "Updated PepperProject" git push origin main 

⬇️ Pulling Updates



sh
git pull origin main 

👨‍💻 Contributors

Anton lahtinen

Srijana Poudel

Anushka Paudel

Ramesh Pandey

 

📜 License

This project is licensed under the MIT License.

Add label
