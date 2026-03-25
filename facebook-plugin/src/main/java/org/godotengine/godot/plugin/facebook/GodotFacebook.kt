package org.godotengine.godot.plugin.facebook

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.FacebookSdk
import com.facebook.GraphRequest
import com.facebook.appevents.AppEventsLogger
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.share.Sharer
import com.facebook.share.model.ShareLinkContent
import com.facebook.share.model.SharePhoto
import com.facebook.share.model.SharePhotoContent
import com.facebook.share.widget.ShareDialog
import org.godotengine.godot.Godot
import org.godotengine.godot.plugin.GodotPlugin
import org.godotengine.godot.plugin.SignalInfo
import org.godotengine.godot.plugin.UsedByGodot
import org.json.JSONObject
import java.io.File
import java.math.BigDecimal
import java.util.Currency

open class GodotFacebook(godot: Godot) : GodotPlugin(godot) {

    private val callbackManager: CallbackManager = CallbackManager.Factory.create()
    private lateinit var appEventsLogger: AppEventsLogger
    private lateinit var shareDialog: ShareDialog

    companion object {
        private const val TAG = "GodotFacebook"
    }

    override fun getPluginName(): String {
        return "GodotFacebook"
    }

    @Deprecated("Deprecated in Java")
    override fun getPluginMethods(): List<String> {
        return listOf(
            "login",
            "logout",
            "isLoggedIn",
            "getAccessToken",
            "getUserId",
            "shareLink",
            "sharePhoto",
            "getProfile",
            "getFriendsList",
            "logEvent",
            "logPurchase"
        )
    }

    override fun getPluginSignals(): MutableSet<SignalInfo> {
        val signals = mutableSetOf<SignalInfo>()
        // Auth signals
        signals.add(SignalInfo("login_success", String::class.java))
        signals.add(SignalInfo("login_cancelled"))
        signals.add(SignalInfo("login_failed", String::class.java))
        signals.add(SignalInfo("logout_success"))
        
        // Share signals
        signals.add(SignalInfo("share_success", String::class.java))
        signals.add(SignalInfo("share_cancelled"))
        signals.add(SignalInfo("share_error", String::class.java))
        
        // Graph API signals
        signals.add(SignalInfo("profile_received", String::class.java))
        signals.add(SignalInfo("profile_error", String::class.java))
        signals.add(SignalInfo("friends_received", String::class.java))
        signals.add(SignalInfo("friends_error", String::class.java))
        
        return signals
    }

    init {
        activity?.let {
//            FacebookSdk.sdkInitialize(it.applicationContext)  Not required anymore because the sdk initializes itself
            appEventsLogger = AppEventsLogger.newLogger(it)
            shareDialog = ShareDialog(it)

            LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    Log.d(TAG, "Facebook login success")
                    val token = result.accessToken
                    val tokenInfo = JSONObject().apply {
                        put("token", token.token)
                        put("userId", token.userId)
                        put("expires", token.expires.time)
                    }.toString()
                    emitSignal("login_success", tokenInfo)
                }

                override fun onCancel() {
                    Log.d(TAG, "Facebook login cancelled")
                    emitSignal("login_cancelled")
                }

                override fun onError(error: FacebookException) {
                    Log.e(TAG, "Facebook login error: ${error.message}")
                    emitSignal("login_failed", error.message ?: "Unknown error")
                }
            })

            shareDialog.registerCallback(callbackManager, object : FacebookCallback<Sharer.Result> {
                override fun onSuccess(result: Sharer.Result) {
                    Log.d(TAG, "Facebook share success")
                    emitSignal("share_success", result.postId ?: "")
                }

                override fun onCancel() {
                    Log.d(TAG, "Facebook share cancelled")
                    emitSignal("share_cancelled")
                }

                override fun onError(error: FacebookException) {
                    Log.e(TAG, "Facebook share error: ${error.message}")
                    emitSignal("share_error", error.message ?: "Unknown error")
                }
            })
        }
    }

    @UsedByGodot
    fun login(permissions: Array<String>) {
        activity?.let {
            LoginManager.getInstance().logInWithReadPermissions(it, permissions.toList())
        }
    }

    @UsedByGodot
    fun logout() {
        LoginManager.getInstance().logOut()
        emitSignal("logout_success")
    }

    @UsedByGodot
    fun isLoggedIn(): Boolean {
        val accessToken = AccessToken.getCurrentAccessToken()
        return accessToken != null && !accessToken.isExpired
    }

    @UsedByGodot
    fun getAccessToken(): String {
        return AccessToken.getCurrentAccessToken()?.token ?: ""
    }

    @UsedByGodot
    fun getUserId(): String {
        return AccessToken.getCurrentAccessToken()?.userId ?: ""
    }

    @UsedByGodot
    fun shareLink(url: String, quote: String) {
        if (ShareDialog.canShow(ShareLinkContent::class.java)) {
            val content = ShareLinkContent.Builder()
                .setContentUrl(Uri.parse(url))
                .setQuote(quote)
                .build()
            shareDialog.show(content)
        }
    }

    @UsedByGodot
    fun sharePhoto(imagePath: String) {
        val file = File(imagePath)
        if (!file.exists()) {
            emitSignal("share_error", "File does not exist: $imagePath")
            return
        }

        val bitmap = BitmapFactory.decodeFile(imagePath)
        if (bitmap == null) {
            emitSignal("share_error", "Failed to decode image: $imagePath")
            return
        }

        val photo = SharePhoto.Builder()
            .setBitmap(bitmap)
            .build()

        val content = SharePhotoContent.Builder()
            .addPhoto(photo)
            .build()

        if (ShareDialog.canShow(SharePhotoContent::class.java)) {
            shareDialog.show(content)
        }
    }

    @UsedByGodot
    fun getProfile() {
        val accessToken = AccessToken.getCurrentAccessToken()
        if (accessToken == null || accessToken.isExpired) {
            emitSignal("profile_error", "Not logged in or token expired")
            return
        }

        val request = GraphRequest.newMeRequest(accessToken) { jsonObject, response ->
            if (response?.error != null) {
                emitSignal("profile_error", response.error?.errorMessage ?: "Unknown error")
            } else {
                emitSignal("profile_received", jsonObject?.toString() ?: "{}")
            }
        }

        val parameters = Bundle()
        parameters.putString("fields", "id,name,email,picture")
        request.parameters = parameters
        request.executeAsync()
    }

    @UsedByGodot
    fun getFriendsList() {
        val accessToken = AccessToken.getCurrentAccessToken()
        if (accessToken == null || accessToken.isExpired) {
            emitSignal("friends_error", "Not logged in or token expired")
            return
        }

        val request = GraphRequest.newMyFriendsRequest(accessToken) { jsonArray, response ->
            if (response?.error != null) {
                emitSignal("friends_error", response.error?.errorMessage ?: "Unknown error")
            } else {
                emitSignal("friends_received", jsonArray?.toString() ?: "[]")
            }
        }
        request.executeAsync()
    }

    @UsedByGodot
    fun logEvent(eventName: String, paramsJson: String) {
        val params = Bundle()
        try {
            val json = JSONObject(paramsJson)
            val keys = json.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val value = json.get(key)
                when (value) {
                    is String -> params.putString(key, value)
                    is Int -> params.putInt(key, value)
                    is Long -> params.putLong(key, value)
                    is Double -> params.putDouble(key, value)
                    is Boolean -> params.putBoolean(key, value)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing event params: ${e.message}")
        }
        appEventsLogger.logEvent(eventName, params)
    }

    @UsedByGodot
    fun logPurchase(amount: Double, currency: String, paramsJson: String) {
        val params = Bundle()
        try {
            val json = JSONObject(paramsJson)
            val keys = json.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val value = json.get(key)
                when (value) {
                    is String -> params.putString(key, value)
                    is Int -> params.putInt(key, value)
                    is Long -> params.putLong(key, value)
                    is Double -> params.putDouble(key, value)
                    is Boolean -> params.putBoolean(key, value)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing purchase params: ${e.message}")
        }
        appEventsLogger.logPurchase(BigDecimal.valueOf(amount), Currency.getInstance(currency), params)
    }

    override fun onMainActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onMainActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }
}
