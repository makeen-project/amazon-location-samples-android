package software.amazon.location.sample.helper

import android.content.Context
import aws.sdk.kotlin.services.iot.IotClient
import aws.sdk.kotlin.services.iot.model.AttachPolicyRequest
import aws.sdk.kotlin.services.cognitoidentity.model.Credentials
import com.amazonaws.services.iot.client.AWSIotMessage
import com.amazonaws.services.iot.client.AWSIotMqttClient
import com.amazonaws.services.iot.client.AWSIotQos
import com.amazonaws.services.iot.client.AWSIotTopic
import com.amazonaws.services.iot.client.auth.CredentialsProvider
import com.amazonaws.services.iot.client.auth.StaticCredentialsProvider
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import software.amazon.location.sample.BuildConfig
import software.amazon.location.sample.R
import software.amazon.location.sample.data.EvaluateGeofenceData
import software.amazon.location.sample.utils.Constant.TIME_OUT
import software.amazon.location.tracking.util.Logger

class MqttHelper(
    private val context: Context,
    private val credentials: Credentials?,
    private val identityId: String,
) {

    private var mqttClient: AWSIotMqttClient? = null
    private var lastNotificationId: Int = 1211

    fun startMqttManager() {
        if (mqttClient != null) stopMqttManager()

        val clientEndpoint = BuildConfig.MQTT_END_POINT

        val credentials = createCredentialsProvider()
        mqttClient = AWSIotMqttClient(clientEndpoint, identityId, credentials, identityId.split(":")[0])

        try {
            mqttClient?.connect()
            subscribeTopic(identityId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createCredentialsProvider(): CredentialsProvider {
        if (credentials?.accessKeyId == null || credentials.sessionToken == null) throw Exception("Credentials not found")
        return StaticCredentialsProvider(
            com.amazonaws.services.iot.client.auth.Credentials(
                credentials.accessKeyId,
                credentials.secretKey,
                credentials.sessionToken,
            )
        )
    }

    private fun subscribeTopic(identityId: String) {
        try {
            val topicName = "$identityId/${BuildConfig.TOPIC_TRACKER}"
            val qos = AWSIotQos.QOS0
            val topic = object : AWSIotTopic(topicName, qos) {
                override fun onMessage(message: AWSIotMessage?) {
                    message?.let {
                        val payloadBytes = it.payload
                        val stringData = String(payloadBytes)
                        if (stringData.isNotEmpty()) {
                            val notificationData =
                                Gson().fromJson(stringData, EvaluateGeofenceData::class.java)
                            context.let { context ->
                                if (notificationData.trackerEventType.equals("ENTER", true)) {
                                    val subTitle = context.getString(
                                        R.string.label_tracker_entered_in_geofence_id,
                                        notificationData.geofenceId,
                                    )
                                    NotificationHelper().showNotification(
                                        context,
                                        context.getString(R.string.label_geofence_is_breached),
                                        subTitle,
                                        lastNotificationId,
                                    )
                                    lastNotificationId++
                                }
                            }
                        }
                    }
                }
            }
            mqttClient?.subscribe(topic, TIME_OUT)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopMqttManager() {
        if (mqttClient != null) {
            try {
                mqttClient?.unsubscribe("${identityId}/${BuildConfig.TOPIC_TRACKER}")
            } catch (e: Exception) {
                Logger.log("stopMqttManager unsubscribe", e)
            }

            try {
                mqttClient?.disconnect()
            } catch (e: Exception) {
                Logger.log("stopMqttManager disconnect", e)
            }
            mqttClient = null
        }
    }

    fun setIotPolicy() {
        CoroutineScope(Dispatchers.IO).launch {
            val attachPolicyRequest = AttachPolicyRequest {
                policyName = BuildConfig.POLICY_NAME
                target = identityId
            }

            val iotClient = IotClient {
                region = identityId.split(":")[0]
                credentialsProvider = createCredentialsProviderForPolicy()
            }

            try {
                iotClient.attachPolicy(attachPolicyRequest)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun createCredentialsProviderForPolicy(): aws.smithy.kotlin.runtime.auth.awscredentials.CredentialsProvider {
        if (credentials?.accessKeyId == null || credentials.sessionToken == null || credentials.secretKey == null) throw Exception("Credentials not found")
        return aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider(
            aws.smithy.kotlin.runtime.auth.awscredentials.Credentials.invoke(
                accessKeyId = credentials.accessKeyId!!,
                secretAccessKey = credentials.secretKey!!,
                sessionToken = credentials.sessionToken,
            )
        )
    }
}
