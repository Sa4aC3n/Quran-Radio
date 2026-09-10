package com.example.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.util.Log
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability

const val REQUEST_CODE_APP_UPDATE = 8801

sealed interface AppUpdateUiState {
    object Idle : AppUpdateUiState
    object Checking : AppUpdateUiState
    data class UpdateAvailable(
        val appUpdateInfo: AppUpdateInfo?,
        val availableVersionCode: Int? = null
    ) : AppUpdateUiState
    data class UpToDate(
        val currentVersionName: String,
        val currentVersionCode: Long
    ) : AppUpdateUiState
    data class NetworkError(
        val message: String
    ) : AppUpdateUiState
    data class PlayStoreFallback(
        val message: String
    ) : AppUpdateUiState
    object UpdateDownloaded : AppUpdateUiState
}

object AppUpdateHelper {

    private const val TAG = "AppUpdateHelper"

    /**
     * Checks if active internet network connection is available
     */
    fun isNetworkAvailable(context: Context): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return false
            val network = cm.activeNetwork ?: return false
            val capabilities = cm.getNetworkCapabilities(network) ?: return false
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (e: Exception) {
            Log.w(TAG, "Error checking network capability: ${e.message}")
            false
        }
    }

    /**
     * Retrieves dynamic version information of the currently running application
     */
    fun getAppVersionInfo(context: Context): Pair<String, Long> {
        return try {
            val pInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            val vName = pInfo.versionName ?: com.example.BuildConfig.VERSION_NAME
            val vCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                pInfo.versionCode.toLong()
            }
            Pair(vName, vCode)
        } catch (e: Exception) {
            Pair(com.example.BuildConfig.VERSION_NAME, com.example.BuildConfig.VERSION_CODE.toLong())
        }
    }

    /**
     * Safely opens the official application page on Google Play Store
     * Uses the current dynamic application package name
     */
    fun openPlayStore(context: Context) {
        val packageName = context.packageName
        try {
            val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(marketIntent)
        } catch (e: Exception) {
            try {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(webIntent)
            } catch (e2: Exception) {
                Log.e(TAG, "Failed to open Play Store via browser: ${e2.message}")
            }
        }
    }

    /**
     * Finds the parent Activity from the given context
     */
    fun findActivity(context: Context): Activity? {
        var ctx = context
        while (ctx is ContextWrapper) {
            if (ctx is Activity) return ctx
            ctx = ctx.baseContext
        }
        return null
    }

    /**
     * Queries Google Play In-App Updates API to check if a newer published version is available
     */
    fun checkForAppUpdate(
        context: Context,
        appUpdateManager: AppUpdateManager,
        onStateChange: (AppUpdateUiState) -> Unit
    ) {
        onStateChange(AppUpdateUiState.Checking)

        if (!isNetworkAvailable(context)) {
            onStateChange(
                AppUpdateUiState.NetworkError(
                    "يرجى التحقق من اتصال الإنترنت والمحاولة مرة أخرى."
                )
            )
            return
        }

        try {
            val appUpdateInfoTask = appUpdateManager.appUpdateInfo

            appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
                val availability = appUpdateInfo.updateAvailability()
                Log.d(TAG, "Update availability: $availability, availableVersionCode: ${appUpdateInfo.availableVersionCode()}")

                when (availability) {
                    UpdateAvailability.UPDATE_AVAILABLE -> {
                        onStateChange(
                            AppUpdateUiState.UpdateAvailable(
                                appUpdateInfo = appUpdateInfo,
                                availableVersionCode = appUpdateInfo.availableVersionCode()
                            )
                        )
                    }
                    UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> {
                        onStateChange(
                            AppUpdateUiState.UpdateAvailable(
                                appUpdateInfo = appUpdateInfo,
                                availableVersionCode = appUpdateInfo.availableVersionCode()
                            )
                        )
                    }
                    UpdateAvailability.UPDATE_NOT_AVAILABLE -> {
                        val (vName, vCode) = getAppVersionInfo(context)
                        onStateChange(
                            AppUpdateUiState.UpToDate(
                                currentVersionName = vName,
                                currentVersionCode = vCode
                            )
                        )
                    }
                    else -> {
                        // UNKNOWN or non-store build
                        val (vName, vCode) = getAppVersionInfo(context)
                        onStateChange(
                            AppUpdateUiState.UpToDate(
                                currentVersionName = vName,
                                currentVersionCode = vCode
                            )
                        )
                    }
                }
            }.addOnFailureListener { exception ->
                Log.w(TAG, "Google Play In-App Updates check failed: ${exception.message}", exception)

                if (!isNetworkAvailable(context)) {
                    onStateChange(
                        AppUpdateUiState.NetworkError(
                            "يرجى التحقق من اتصال الإنترنت والمحاولة مرة أخرى."
                        )
                    )
                } else {
                    // Google Play In-App Updates service not available on device or test build
                    // Provide safe fallback allowing user to open Google Play Store directly
                    onStateChange(
                        AppUpdateUiState.PlayStoreFallback(
                            "تعذر التحقق عبر خدمات Google Play التلقائية. يمكنك فتح صفحة التطبيق على المتجر للتحقق يدوياً."
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during check: ${e.message}", e)
            if (!isNetworkAvailable(context)) {
                onStateChange(
                    AppUpdateUiState.NetworkError(
                        "يرجى التحقق من اتصال الإنترنت والمحاولة مرة أخرى."
                    )
                )
            } else {
                onStateChange(
                    AppUpdateUiState.PlayStoreFallback(
                        "تعذر التحقق عبر خدمات Google Play. يمكنك فحص التحديثات مباشرة على متجر Google Play."
                    )
                )
            }
        }
    }

    /**
     * Starts the In-App Update flow.
     * Prefers Immediate update, falls back to Flexible update, or opens Google Play directly.
     */
    fun startUpdate(
        context: Context,
        appUpdateManager: AppUpdateManager,
        appUpdateInfo: AppUpdateInfo?,
        onDismiss: () -> Unit
    ) {
        val activity = findActivity(context)

        if (activity != null && appUpdateInfo != null) {
            try {
                val updateType = when {
                    appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) -> AppUpdateType.IMMEDIATE
                    appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE) -> AppUpdateType.FLEXIBLE
                    else -> null
                }

                if (updateType != null) {
                    val options = AppUpdateOptions.newBuilder(updateType).build()
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        activity,
                        options,
                        REQUEST_CODE_APP_UPDATE
                    )
                    onDismiss()
                    return
                }
            } catch (e: Exception) {
                Log.w(TAG, "startUpdateFlowForResult failed: ${e.message}, falling back to Play Store", e)
            }
        }

        // Fallback: Open Google Play Store page directly
        openPlayStore(context)
        onDismiss()
    }
}
