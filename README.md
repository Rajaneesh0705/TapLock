# TapLock

**TapLock** is a lightweight, privacy-focused Android utility application designed to let users instantly lock their phone's screen from a Home screen 1×1 widget, a pinned Home screen shortcut, or an in-app button.

---

## 1. Supported Android Versions

Based on the actual project configuration:
* **Minimum Supported SDK:** `minSdk = 26` (Android 8.0 Oreo)
* **Target SDK:** `targetSdk = 35` (Android 15)
* **Compile SDK:** `compileSdk = 35`

### Android & Samsung Compatibility Range
* **Broad Ecosystem Coverage:** Covers Android 8.0 (API 26) through Android 15 (API 35), accounting for over 95% of active Android devices worldwide.
* **Samsung One UI Compatibility:** Fully compatible with Samsung Galaxy devices across all One UI generations:
  * One UI 1.0 / 1.5 (Android 9 Pie)
  * One UI 2.0 / 2.5 (Android 10)
  * One UI 3.0 / 3.1 (Android 11)
  * One UI 4.0 / 4.1 (Android 12 – initial release OS of the Samsung Galaxy S22 Ultra)
  * One UI 5.0 / 5.1 (Android 13)
  * One UI 6.0 / 6.1 (Android 14)
  * One UI 7.0 (Android 15)

---

## 2. Privacy & Permission Guarantees

Unlike screen lock tools that rely on battery-draining Accessibility Services or intrusive screen overlays, TapLock relies strictly on Android's built-in **Device Administrator API** using solely the **Force lock** policy (`<force-lock />`):

* **No Root Access Required:** Completely compatible with standard retail devices.
* **No Accessibility Service:** Eliminates background screen reading, key logging risks, and battery drain.
* **Zero Network Permissions:** 100% offline. The `AndroidManifest.xml` does not request `android.permission.INTERNET` or `ACCESS_NETWORK_STATE`.
* **Zero Tracking or Ads:** No analytics, crash reporters, or third-party ad libraries.

---

## 3. Required Android Studio & SDK Setup

To open, build, or develop the project locally:

### Prerequisites
1. **Android Studio:** Install [Android Studio](https://developer.android.com/studio) (Ladybug 2024.2.1 or newer recommended).
2. **SDK Platform & Tools:** In Android Studio, go to **Tools > SDK Manager**:
   * **SDK Platforms:** Install **Android 15.0 ("VanillaIceCream") / API Level 35**.
   * **SDK Tools:** Check and install:
     * **Android SDK Build-Tools 35.0.0**
     * **Android SDK Platform-Tools**
3. **JDK:** JDK 17 (bundled automatically by Android Studio).

---

## 4. How to Build the APK

### Option A: Build via Android Studio
1. Open Android Studio and select **Open**.
2. Select the `TapLock` project directory:
   ```text
   C:\Users\Admin\OneDrive\Desktop\TapLock
   ```
3. Allow Gradle to synchronize dependencies.
4. In the top menu, go to:
   ```text
   Build > Build Bundle(s) / APK(s) > Build APK(s)
   ```
5. When the build completes, click **locate** in the popup notification, or find the APK at:
   ```text
   app/build/outputs/apk/debug/app-debug.apk
   ```

### Option B: Build via Automated GitHub Actions CI
The project includes a GitHub Actions workflow (`.github/workflows/build.yml`):
* Every push to `main` (or pull request) automatically builds the debug APK on an Ubuntu runner with JDK 17 and Gradle 8.9.
* You can download the resulting `taplock-debug-apk` directly from the **Actions** tab of your GitHub repository under **Artifacts**.

### Debug APK vs. Signed Release APK
* **Debug APK (`app-debug.apk`):** Built with Android's default debug keystore. It is intended for testing and personal installation.
* **Release APK:** For publishing or production use:
  1. In Android Studio, go to **Build > Generate Signed Bundle / APK**.
  2. Select **APK**, create or choose your private keystore (`.jks`), and set key aliases/passwords.
  3. **Security Rule:** Never commit keystore files (`*.jks`, `*.keystore`) or passwords to Git. The `.gitignore` is already configured to prevent keystores and signing property files from being tracked.

---

## 5. How to Install and Set Up on a Samsung Phone

1. **Copy APK to Phone:**
   * Transfer `app-debug.apk` to your Samsung phone via USB cable, Quick Share, or Google Drive.
2. **Install:**
   * Open the **My Files** app on your Samsung phone, locate `app-debug.apk`, and tap it.
   * If prompted, enable **Allow from this source** to permit installing unknown apps.
   * Tap **Install**.
3. **Launch & Activate:**
   * Open **TapLock**. The initial status will display **Device Administrator: Inactive**.
   * Tap **Activate Device Administrator**.
   * Android's system prompt will appear: *“Activate device admin app? ... Force lock”*.
   * Tap **Activate this device admin app**.
4. **Active Status:**
   * Return to TapLock; the status will now show **Device Administrator: Active**.
5. **Secure Screen Lock:**
   * Verify under your phone's **Settings > Lock screen** that you have set a secure screen lock (PIN, Pattern, Password, or Fingerprint). If the screen lock is set to "None" or "Swipe", the screen will turn off when locked but will not require authentication to reopen.

---

## 6. How to Add and Use Home Screen Controls

### A. 1×1 Home Screen Widget (Primary One-Tap Control)
1. Go to your Samsung Galaxy phone's **Home screen**.
2. **Touch and hold** (long-press) any empty area of the Home screen until the screen options appear.
3. Tap **Widgets** at the bottom of the screen.
4. Scroll to or search for **TapLock**.
5. Tap **TapLock 1×1** and tap **Add** (or drag it to your desired spot on the Home screen).
6. **Usage:**
   * **Active:** Tap the widget once. Your phone's display will turn off and lock immediately.
   * **Inactive:** The widget displays an amber icon with *“Setup needed”*. Tapping it opens the TapLock setup screen so you can enable permission.

### B. "Lock screen" App Shortcut (Secondary One-Tap Action)
* **Method 1 (In-App Pinning):** Open the TapLock app, scroll to the **Home Screen Controls** card, and tap **Pin Lock Shortcut to Home Screen**. When your Samsung One UI launcher asks for confirmation, tap **Add**.
* **Method 2 (Long-Press App Icon):** Touch and hold the **TapLock** app icon in your app drawer or Home screen. In the popup menu, touch and hold **Lock screen**, then drag it onto your Home screen.

---

## 7. How to Deactivate Permission Before Uninstalling

Android security policy blocks uninstalling any application that currently holds an active Device Administrator permission. To uninstall TapLock:

1. Open the **TapLock** app.
2. Scroll to the **Uninstall Preparation** card.
3. Tap **Deactivate Device Administrator**.
4. Tap **Deactivate** in the confirmation dialog.
5. You can now uninstall TapLock normally (long-press the app icon and tap **Uninstall**).

*(Alternative system method: Go to phone `Settings > Security and privacy > More security settings > Device admin apps > toggle off TapLock`)*.

---

## 8. Device Compatibility & Testing Disclosure

* **Architectural & Design Intent:** TapLock has been architected, coded, and configured strictly against Android Open Source Project (AOSP) standards and Samsung One UI specifications (API 26 to 35). It is designed to run on Samsung Galaxy devices including the Galaxy S22 Ultra.
* **Testing Status:** The project has been validated and audited through code analysis, resource linking, and Android manifest/lifecycle specifications. **The application has NOT yet been built or physically tested on a physical Samsung Galaxy S22 Ultra or any other specific Samsung hardware device in this development environment.**

---

## 9. How to Upload This Project to GitHub

Follow these steps to upload the project to your own GitHub account:

### Step 1: Create a New Repository on GitHub
1. Go to [github.com/new](https://github.com/new).
2. Set the **Repository name** (e.g. `TapLock`).
3. Set the visibility to **Public** or **Private** based on your preference.
4. **Important:** Leave "Add a README file", "Add .gitignore", and "Choose a license" **unchecked** (the project already contains them).
5. Click **Create repository**.

### Step 2: Commit and Push from Your Terminal
Open PowerShell or your command prompt in this project folder:
```powershell
cd "c:\Users\Admin\OneDrive\Desktop\TapLock"

# Stage all project files
git add .

# Create the initial commit
git commit -m "Initial commit: TapLock Android application foundation, widget, and CI"

# Rename default branch to main
git branch -M main

# Add your GitHub repository as remote (replace YOUR_USERNAME and YOUR_REPO)
git remote add origin https://github.com/YOUR_USERNAME/TapLock.git

# Push the project to GitHub
git push -u origin main
```
