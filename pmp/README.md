# Pro Media Player Ultimate

A standalone Android media-player repository inspired by modern media-player workflows.
It does **not** copy MX Player source code, branding, proprietary assets, or proprietary implementation.

## Included architecture

1. Video engine: Media3/ExoPlayer, hardware/device codecs, queues, resume foundation, playback speed.
2. Subtitle system: Media3 subtitle architecture and external-source integration foundation.
3. Gestures: seek, volume, brightness, double-tap seek.
4. Display: fit/crop foundation, fullscreen/PiP.
5. Audio: MediaSession, audio focus, Bluetooth/media-control foundation.
6. Music: MediaStore audio library and playback foundation.
7. File access: Android Storage Access Framework can be added for managed folders.
8. TV/large screens: responsive Android layout foundation.
9. PiP/background: Android PiP and MediaSession service.
10. Private vault: biometric authentication foundation.
11. Smart library: MediaStore + Room history/favorites/playlists.
12. Network: HTTP/HTTPS URL playback foundation.
13. External playback: Android media routing/MediaSession foundation.
14. Diagnostics: resolution, bitrate, audio/video format and buffer information.
15. Material 3 UI.
16. Room indexing and DataStore settings architecture.
17. Personalization settings architecture.
18. Error/recovery foundation through Media3.

## Important device-dependent features

4K/8K, HDR/Dolby Vision, AV1/HEVC profiles, hardware acceleration, frame stepping accuracy,
multichannel output, Android Auto, Chromecast, SMB/FTP/WebDAV, exact subtitle rendering,
equalizer/audio effects, external displays and some TV features depend on Android version,
device codecs, permissions, libraries, provider configuration and hardware.

## Build

Open in Android Studio and build `app:assembleDebug`.
The included GitHub workflow builds the debug APK with JDK 17.

Developer:
Sk. Kaushal
skgdrive932@gmail.com
+91 97793 71866


## Playback / Library fixes in this revision

- Video rows now show the real `MediaStore.DISPLAY_NAME` filename instead of metadata `TITLE`.
- Video thumbnails are generated asynchronously with `ContentResolver.loadThumbnail()` on Android 10+.
- Tapping a video opens the app's own Media3 `PlayerActivity` instead of launching an external `ACTION_VIEW` player.
- `PlayerActivity` is registered in the manifest.
- Player errors are surfaced to the user.
- Modern `content://` MediaStore URIs are used instead of the deprecated `_data` filesystem path.
