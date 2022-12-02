package br.com.tln.personalcard.usuario.fcm

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import br.com.tln.personalcard.usuario.DEVICE_NAME
import br.com.tln.personalcard.usuario.R
import br.com.tln.personalcard.usuario.entity.Device
import br.com.tln.personalcard.usuario.repository.SessionRepository
import br.com.tln.personalcard.usuario.ui.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.android.AndroidInjection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class MessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var sessionRepository: SessionRepository

    override fun onCreate() {
        super.onCreate()
        AndroidInjection.inject(this)
    }

    override fun onNewToken(token: String) {
        Timber.d("New token: $token")
        GlobalScope.launch(Dispatchers.IO) {
            sessionRepository.getDevice()?.let { device ->
                sessionRepository.update(device.copy(token = token))
            } ?: run {
                val device = Device(
                    name = DEVICE_NAME,
                    token = token
                )
                sessionRepository.create(device)
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        if (remoteMessage == null) {
            return
        }

        val stackBuilder = TaskStackBuilder.create(this)
        stackBuilder.addParentStack(MainActivity::class.java)
        stackBuilder.addNextIntent(Intent(this, MainActivity::class.java))

        val pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)

        val notification =
            NotificationCompat.Builder(applicationContext, getString(R.string.notification_channel_id_default))
                .setSmallIcon(R.mipmap.ic_status_bar)
                .setColor(ContextCompat.getColor(this, R.color.telenetColorPrimary))
                .setContentTitle(remoteMessage.data["title"])
                .setContentText(remoteMessage.data["body"])
                .setStyle(NotificationCompat.BigTextStyle().bigText(remoteMessage.data["body"]))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_VIBRATE or Notification.DEFAULT_SOUND or Notification.FLAG_SHOW_LIGHTS)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

        val mNotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        mNotificationManager.notify(1, notification)

    }
}