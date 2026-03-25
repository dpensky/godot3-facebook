# Godot Facebook Integration Module (Godot 3.x)

A robust and maintainable Android plugin for Godot 3.x that integrates the Facebook SDK. This module allows you to easily add Facebook Login, Social Sharing, Graph API requests, and App Events (Analytics) to your Godot games on Android.

## 🚀 Features

*   **Authentication**: Full Facebook Login flow with customizable permissions and session management.
*   **Social Sharing**: Share links with quotes or local photos directly to the Facebook app using native dialogs.
*   **Graph API**: Retrieve user profile data (ID, name, email, picture) and friends lists.
*   **Analytics**: Log custom app events and purchases to track user engagement and monetization.
*   **Vibrant UI**: Includes a Material 3 test application built with Jetpack Compose for verifying integration.

## 📋 Prerequisites

*   **Godot Engine 3.5+** (with Android Build Template installed).
*   **Android Studio** (to build the plugin).
*   **Facebook App ID & Client Token**: Obtain these from the [Meta for Developers Dashboard](https://developers.facebook.com/).

## 🛠️ Installation & Setup

### 1. Build the Plugin
Open the project in Android Studio and run the following command in the terminal:
```bash
./gradlew :facebook-plugin:assembleDebug
```
Alternatively, use `assembleRelease` for production builds.

### 2. Copy Files to Godot
1.  Navigate to `facebook-plugin/build/outputs/aar/`.
2.  Copy `facebook-plugin-debug.aar` (or release) to your Godot project's `res://android/plugins/` directory.
3.  Copy `facebook-plugin/facebook-plugin.gdap` to the same directory.

### 3. Configure Facebook Credentials
Open `facebook-plugin/src/main/res/values/strings.xml` and replace the following placeholders with your actual Facebook credentials:
```xml
<string name="facebook_app_id">YOUR_FACEBOOK_APP_ID</string>
<string name="facebook_client_token">YOUR_FACEBOOK_CLIENT_TOKEN</string>
```
*Note: In Godot, you can also manage these via custom build strings or by modifying the exported Android manifest.*

### 4. Enable the Plugin in Godot
In the Godot Editor, go to **Project -> Export -> Android**. Under the **Plugins** section, check the box for **GodotFacebook**. Ensure **Use Custom Build** is enabled.

---

## 📖 Godot API Reference

Access the plugin in GDScript using the `Engine.get_singleton("GodotFacebook")` method.

### Methods

| Method | Description |
| :--- | :--- |
| `login(permissions: Array)` | Launches the Facebook login dialog with requested permissions (e.g., `["public_profile", "email"]`). |
| `logout()` | Logs the user out and clears the current session. |
| `isLoggedIn() -> bool` | Returns `true` if a valid, non-expired access token exists. |
| `getAccessToken() -> String` | Returns the raw Facebook access token string. |
| `getUserId() -> String` | Returns the Facebook User ID. |
| `shareLink(url: String, quote: String)` | Opens the native Share Dialog for a URL with a text quote. |
| `sharePhoto(imagePath: String)` | Shares a photo from a local file path (e.g., `user://screenshot.png`). |
| `getProfile()` | Fetches user profile data via Graph API. Results are sent via signals. |
| `getFriendsList()` | Fetches the list of friends who also use the app. |
| `logEvent(name: String, paramsJson: String)` | Logs a custom event with a JSON string of parameters. |
| `logPurchase(amount: float, currency: String, paramsJson: String)` | Logs a purchase event with amount and currency. |

### Signals

| Signal | Description |
| :--- | :--- |
| `login_success(tokenInfo: String)` | Emitted on successful login. `tokenInfo` is a JSON string. |
| `login_cancelled` | Emitted if the user cancels the login flow. |
| `login_failed(error: String)` | Emitted if an error occurs during login. |
| `logout_success` | Emitted when the user logs out. |
| `share_success(postId: String)` | Emitted on successful share. |
| `share_cancelled` | Emitted if the user cancels sharing. |
| `share_error(error: String)` | Emitted if sharing fails. |
| `profile_received(profileJson: String)` | JSON string containing user profile data. |
| `profile_error(error: String)` | Emitted if profile fetching fails. |
| `friends_received(friendsJson: String)` | JSON string containing friends list. |
| `friends_error(error: String)` | Emitted if friends list fetching fails. |

---

## 📄 License

This project is open-source and available under the MIT License.
