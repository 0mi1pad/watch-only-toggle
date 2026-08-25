# Watch Only — one-tap toggle

Taps through Settings -> Battery -> Watch only -> Turn on, automatically, so you
don't have to hunt through menus on the watch's tiny screen.

Built for Galaxy Watch 6 / 6 Classic (One UI Watch 5), English UI.

## What's in this project

- `app/` — the actual Android app source code (an Activity + an Accessibility Service)
- `.github/workflows/build.yml` — a recipe that tells GitHub's free servers to compile
  the app into a `.apk` file automatically, any time you upload code

## Step 1 — Get a compiled APK (no PC needed)

1. Create a free account at github.com if you don't have one.
2. Create a new repository (any name, public or private).
3. On the repo page: **Add file -> Upload files**, then drag in *everything* from
   this project folder (keep the folder structure — `.github`, `app`, `build.gradle`,
   `settings.gradle`, `gradle.properties`, `.gitignore`, this `README.md`).
4. Commit the upload. This alone triggers the build.
5. Click the **Actions** tab. You'll see a run in progress — wait for the green checkmark
   (usually 3–6 minutes for the very first run, since it's downloading the Android
   toolchain fresh; faster after that).
6. Click into the finished run, scroll down to **Artifacts**, and download
   `WatchOnlyToggle-apk` — this is a zip containing `app-debug.apk`. Unzip it to get
   the actual APK.

If the Actions run fails (red X), open it, copy the error text from whichever step
failed, and send it back — the workflow can be adjusted without needing to touch
the app code itself.

## Step 2 — Get the APK onto the watch

Using AnExplorer (as you already planned):

1. Install **AnExplorer** from the Play Store on both your phone and your watch.
2. Put `app-debug.apk` somewhere on your phone (e.g. Downloads).
3. In AnExplorer on the phone, find the file and use its "send to nearby device /
   watch" transfer feature over WiFi.
4. On the watch, accept the transfer, then open the file in AnExplorer to install it.
   - First time only: the watch will ask to allow "Install unknown apps" for
     AnExplorer — a standard one-time Android toggle.

## Step 3 — One-time enable (on the watch)

1. Open the new **Watch Only** app on the watch once.
   - Since it's sideloaded, Android may show it grayed out in Accessibility settings
     at first. If so, tap the app's name, then the 3-dot menu (or long-press,
     depending on watch UI) -> **Allow restricted setting** -> then the toggle
     becomes tappable.
2. Turn the accessibility service on.
3. Open the **Watch Only** app again — this time it runs the automation instead of
   sending you to Accessibility settings.

## Everyday use

Tap the **Watch Only** app icon. It opens Settings, taps through to Battery ->
Watch only -> Turn on by itself, and the screen goes black as Watch Only mode
engages — same as doing it manually, just automatic.

## If it stalls partway

This is the most likely thing to need a small fix, since I built this from
screenshots rather than a live device. If the app opens Settings but stops
partway through (e.g. sits on the Battery page, or the Watch Only detail page,
without tapping the next thing), tell me exactly **which screen it stopped on**
and I'll adjust either the search text or the timing in
`WatchOnlyAccessibilityService.kt` — no rebuild-from-scratch needed, just a
one-line edit and a re-upload to GitHub (which auto-rebuilds).

One specific possibility worth knowing about: Samsung sometimes blocks
automated (non-human) taps on certain sensitive settings, as an anti-malware
protection. If everything up through opening the "Watch only" detail page works
but the final "Turn on" tap doesn't do anything, that's likely what's happening,
and it isn't fixable through this app's code — the OS itself is refusing the
synthetic tap on that specific button. In that case you'd still save the
navigation, just not the very last tap.

## Turning Watch Only back off

This app only turns it **on** (as requested). Turning it off is the same manual
step Samsung already has: press and hold the Home key, or place the watch on its
charger.
