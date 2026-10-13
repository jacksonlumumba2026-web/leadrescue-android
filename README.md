# LeadRescue

**Stop losing customers because you forgot to follow up.**

LeadRescue is a free Android app for small businesses in Kenya. Save every inquiry from WhatsApp, calls, walk-ins or social media. Set the next follow-up date, and LeadRescue reminds you on the day. One tap opens WhatsApp with a ready-made message. You read it and you press send.

## Features

| | |
|---|---|
| **Dashboard** | Total leads, follow-ups due today, overdue follow-ups, sales won (count and KSh), open pipeline value, and a "Follow up now" list |
| **Add / edit leads** | Customer name, phone, product or service, inquiry source, estimated value (KSh), notes, status and next follow-up date and time |
| **Leads list** | Search by name, product, notes or phone. Filter by All, Overdue, Due today, New, Contacted, Quoted, Won or Lost |
| **Statuses** | New → Contacted → Quoted → Won / Lost. Won and Lost leads stop sending reminders |
| **WhatsApp follow-up** | Opens WhatsApp (or WhatsApp Business) with a message that fits the lead's status. You review it, edit it if you like, and send it yourself |
| **Reminders** | A notification at each follow-up time, with an "Open WhatsApp" button, plus a summary at about 8:00 AM when anything is due or overdue |
| **Kenyan numbers** | Accepts `0712 345 678`, `712345678`, `+254 712 345 678`, `0110 123 456` and more, and saves them all as `+254…`. Other countries work if you type them with `+` |
| **Works offline** | Everything is stored on the phone (Room database). No account, no internet, no subscription |

**What LeadRescue does not do:** it does not read your WhatsApp chats and it never sends messages for you.

---

## Get the app on your Samsung phone

The APK (the Android install file) is built in the cloud by GitHub Actions every time code is pushed.

### 1. Download the APK

1. On your phone, open **github.com** in Chrome and sign in. You can also use the GitHub app, but downloads are easier in the browser.
2. Go to your repository **leadrescue-android** → tap **Actions**.
3. Tap the newest run of **Build Android APK** that has a **green tick ✅**.
4. Scroll down to **Artifacts** and tap **LeadRescue-debug-apk**. A `.zip` file downloads.
   - If Chrome shows the desktop page, zoom out to find the Artifacts section at the bottom.
5. Open the **My Files** app → **Downloads** → tap the zip → **Extract**. You get `LeadRescue-1.0.N-debug.apk`.

> Artifacts are kept for 30 days. To build a fresh one at any time: **Actions → Build Android APK → Run workflow**.

### 2. Install it

1. In **My Files**, tap the `.apk` file.
2. Android says installing from this source isn't allowed. Tap **Settings**, turn on **Allow from this source** for My Files, then go back.
3. Tap **Install**. If Google Play Protect warns you about an unknown app, tap **More details → Install anyway**. This is normal for apps that don't come from the Play Store.
4. Open **LeadRescue** and tap **Allow** when it asks to send notifications.

### 3. Make reminders reliable on Samsung (recommended)

Samsung's battery saver can delay or block reminders for apps it thinks are unused.

1. Long-press the LeadRescue icon → **App info** (or in the app: **Settings → Open App info**).
2. Tap **Battery** → choose **Unrestricted**.
3. Also check **Settings → Battery → Background usage limits** and make sure LeadRescue is **not** in "Sleeping apps" or "Deep sleeping apps".

Reminders use Android's WorkManager. They survive restarts and need no special alarm permission, but Android may still deliver one a few minutes late when the phone is idle.

---

## Keeping your data between updates (one-time GitHub setup)

Android only installs a new version over an old one if both are signed with the **same key**. By default every cloud build uses a new temporary key. You would then have to uninstall the old app first, which **deletes your saved leads**.

To avoid this, give GitHub a permanent signing key once:

1. Get the key text (`DEBUG_KEYSTORE_BASE64`) that Claude generated for you in this session, or make your own on a computer with Java:
   ```bash
   keytool -genkeypair -keystore ci-debug.keystore -storepass android -keypass android \
     -alias androiddebugkey -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=LeadRescue Debug"
   base64 -w0 ci-debug.keystore      # copy the output
   ```
2. On GitHub go to your repository → **Settings → Secrets and variables → Actions → New repository secret**.
3. Name: `DEBUG_KEYSTORE_BASE64`. Value: paste the text. Click **Add secret**.
4. Run a new build (**Actions → Build Android APK → Run workflow**).

From then on every APK installs as an update and keeps your leads. Uninstall the earlier, differently-signed version **once**, before installing the first build made with the secret.

The key lives only in GitHub Secrets. It is never committed (`*.keystore` is in `.gitignore`). It is a debug/test key, so don't use it to publish to the Play Store.

---

## For developers

**Stack:** Kotlin 2.1, Jetpack Compose (Material 3, BOM 2024.12.01), Room 2.6 (KSP), Navigation Compose, WorkManager 2.10, AGP 8.7, Gradle 8.11, JDK 17. `minSdk 26` (Android 8), `targetSdk/compileSdk 35`. It runs on Android 16.

```
app/src/main/java/com/leadrescue/app/
├── data/        Lead entity, DAO, Room database, repository, filters, settings
├── reminders/   ReminderScheduler (WorkManager), FollowUpWorker, DailyDigestWorker, Notifications
├── util/        KenyanPhone, Ksh, FollowUpDates, WhatsAppMessages  (pure Kotlin, unit-tested)
└── ui/          theme, shared components, view models, screens, navigation
```

Build locally (needs the Android SDK):

```bash
./gradlew testDebugUnitTest   # unit tests
./gradlew assembleDebug       # APK at app/build/outputs/apk/debug/
```

CI: `.github/workflows/android-build.yml` runs the unit tests, builds the debug APK and uploads it as the `LeadRescue-debug-apk` artifact on every push, on pull requests and on manual runs.
