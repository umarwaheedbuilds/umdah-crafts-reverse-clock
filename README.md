# Umdah Crafts — A Reverse Clock

An analog clock widget for Android that runs **backwards**. The hands turn counter-clockwise and the numbers go the other way round the dial: 12 at the top, then 1 where 11 normally sits, 2 where 10 sits, and so on.

**[⬇ Download the latest APK](../../releases/latest)**

## Features

- **Two home-screen widgets**
  - **Reverse Clock**: hour, minute and seconds hands
  - **Reverse Clock Minimal**: hours and minutes only
- **Your photo as the background:** pick any photo and it's cropped to the dial and darkened at the edges so the numbers stay readable.
- **Smooth full-screen clock** inside the app, with a sweeping seconds hand
- **Uses essentially no battery:** no background service, no alarms, no periodic updates. Your launcher moves the hands with Android's built-in clock, the same way it runs the stock clock widget.
- **No permissions at all**
- **Tiny:** about 45 KB
- Works on **Android 9 and newer**

## Install

1. On your phone, open the **[Releases page](../../releases/latest)** and tap the `.apk` file to download it.
2. Open the downloaded file (from the download notification, or from **Files → Downloads**).
3. If Android says *"For your security, your phone is not allowed to install unknown apps from this source"*, tap **Settings**, turn on **Allow from this source**, then go back and tap **Install**.
4. If Google Play Protect shows a prompt about an unrecognized app, tap **More details → Install anyway**. It appears because the app isn't distributed through the Play Store. The app requests no permissions and has no internet access.

> **Updating from an older test build?** Uninstall the old version first. Builds signed with a different key can't install over each other.

## Add the widget

**From the app:** open **Umdah Crafts** and tap **Add widget**, then confirm.

**From the home screen:**

1. Long-press an empty spot on your home screen.
2. Tap **Widgets**.
3. Scroll to **Umdah Crafts** and pick **Reverse Clock** or **Reverse Clock Minimal**.
4. Drag it onto your home screen. Long-press it afterwards to resize.

## Use a photo as the background

1. Open the **Umdah Crafts** app.
2. Tap **Photo** and choose an image. Square-ish photos with the subject in the middle look best.
3. The app and every widget on your home screen update right away.
4. Tap **Remove** to go back to the plain dark dial.

Tapping the widget opens the app.

## Reading a reverse clock

Everything is mirrored, so read it just like a normal clock seen in a mirror:

- The **short hand** points at the hour number.
- The **long hand** reads minutes, with each number worth 5 minutes. For example, pointing at **3**, which sits where 9 normally is, means **:15**.
- Like any real analog clock, the minute hand moves continuously. At 10:30:50 it is already most of the way to :31 while your phone's status bar still says 10:30.

## FAQ

**Why doesn't the widget's seconds hand glide smoothly like it does in the app?**
Android doesn't let home-screen widgets run free animations. The launcher ticks the seconds hand once per second, and that's what keeps the widget battery-free. The full-screen clock in the app glides smoothly, and only while it's open.

**Will it drain my battery?**
No. Once the widget is placed, the app itself never runs. The launcher redraws the clock only while your home screen is visible.

**Why isn't it on the Play Store?**
Not yet. For now it's distributed as a signed APK on this page.

## Build it yourself

Requirements: Android Studio (or JDK 17+ and the Android SDK 36).

```bash
git clone https://github.com/umarwaheedbuilds/umdah-crafts-reverse-clock.git
cd umdah-crafts-reverse-clock
./gradlew assembleRelease
```

The APK lands in `app/build/outputs/apk/release/`. Without the original signing key it's signed with your local debug key, which is fine for personal use.

### Project layout

| File | What it does |
|---|---|
| `ClockFace.kt` | Draws the dial: reversed numerals, minute dots, photo background, handle |
| `ClockView.kt` | Full-screen, smoothly animated clock inside the app |
| `AntiClockWidget.kt` | Widget providers. They hand the dial to the launcher once, then stay idle. |
| `Background.kt` | Picks, crops and stores the background photo |
| `res/layout/widget_clock*.xml` | Widget layouts. A mirrored `AnalogClock` (`scaleX="-1"`) makes the hands run backwards. |
| `res/drawable/hand_*.xml` | Vector hands |

## Credits

Made by **Umdah Crafts**. Instagram: [@umdah_crafts](https://instagram.com/umdah_crafts)
