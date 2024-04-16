package software.amazon.location.sample.helper

import android.content.Context
import com.amazonaws.auth.CognitoCredentialsProvider
import com.amazonaws.mobile.auth.core.internal.util.ThreadUtils
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos
import com.amazonaws.regions.Region
import software.amazon.location.sample.data.EvaluateGeofenceData
import software.amazon.location.tracking.util.Logger
import com.amazonaws.services.iot.AWSIotClient
import com.amazonaws.services.iot.model.AttachPolicyRequest
import com.google.gson.Gson
import software.amazon.location.sample.BuildConfig
import software.amazon.location.sample.R

class MqttHelper(
    private val context: Context,
    private val mCognitoCredentialsProvider: CognitoCredentialsProvider?
) {

    private var mqttManager: AWSIotMqttManager? = null
    private var lastNotificationId: Int = 1211
    fun startMqttManager() {
        if (mqttManager != null) stopMqttManager()
        mqttManager =
            AWSIotMqttManager(
                mCognitoCredentialsProvider?.identityId,
                BuildConfig.MQTT_END_POINT,
            )
        mqttManager?.isAutoReconnect = false
        mqttManager?.keepAlive = 60
        mqttManager?.setCleanSession(true)
        try {
            mqttManager?.connect(mCognitoCredentialsProvider) { status, throwable ->
                ThreadUtils.runOnUiThread {
                    when (status) {
                        AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus.Connecting -> {
                            Logger.log("AWSIotMqttClientStatus.Connecting")
                        }

                        AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus.Connected -> {
                            Logger.log("AWSIotMqttClientStatus.Connected")
                            mCognitoCredentialsProvider?.identityId?.let { subscribeTopic(it) }
                        }

                        AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus.Reconnecting -> {
                            Logger.log("AWSIotMqttClientStatus.Reconnecting")
                        }

                        AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus.ConnectionLost -> {
                            Logger.log("AWSIotMqttClientStatus.ConnectionLost")
                            throwable?.printStackTrace()
                        }

                        else -> {}
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun subscribeTopic(identityId: String) {
        try {
            mqttManager?.subscribeToTopic(
                "$identityId/${BuildConfig.TOPIC_TRACKER}",
                AWSIotMqttQos.QOS0,
            ) { _, data ->
                val stringData = String(data)
                if (stringData.isNotEmpty()) {
                    val notificationData =
                        Gson().fromJson(stringData, EvaluateGeofenceData::class.java)
                    context.let {
                        if (notificationData.trackerEventType.equals("ENTER", true)) {
                            Logger.log("subscribeTopic ENTER")
                            val subTitle = context.getString(
                                R.string.label_tracker_entered_in_geofence_id,
                                notificationData.geofenceId,
                            )
                            NotificationHelper().showNotification(
                                it,
                                it.getString(R.string.label_geofence_is_breached),
                                subTitle,
                                lastNotificationId,
                            )
                            lastNotificationId++
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopMqttManager() {
        if (mqttManager != null) {
            try {
                mqttManager?.unsubscribeTopic("${mCognitoCredentialsProvider?.identityId}/${BuildConfig.TOPIC_TRACKER}")
            } catch (e: Exception) {
                Logger.log("stopMqttManager unsubscribeTopic", e)
            }

            try {
                mqttManager?.disconnect()
            } catch (e: Exception) {
                Logger.log("stopMqttManager disconnect", e)
            }
            mqttManager = null
        }
    }

    fun setIotPolicy() {
        val identityId: String? = mCognitoCredentialsProvider?.identityId

        val attachPolicyReq = AttachPolicyRequest().withPolicyName(BuildConfig.POLICY_NAME)
            .withTarget(identityId)
        val mIotAndroidClient = AWSIotClient(mCognitoCredentialsProvider)
        val region = identityId?.split(":")?.get(0)

        mIotAndroidClient.setRegion(Region.getRegion(region))
        mIotAndroidClient.attachPolicy(attachPolicyReq)
    }
}
