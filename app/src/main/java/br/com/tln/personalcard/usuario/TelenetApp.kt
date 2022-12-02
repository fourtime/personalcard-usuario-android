package br.com.tln.personalcard.usuario

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import br.com.tln.personalcard.usuario.di.AppInjector
import br.com.tln.personalcard.usuario.preferences.AppPreferences
import br.com.tln.personalcard.usuario.preferences.SessionPreferences
import br.com.tln.personalcard.usuario.timber.CrashlyticsTree
import com.jakewharton.threetenabp.AndroidThreeTen
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import dagger.android.HasBroadcastReceiverInjector
import dagger.android.HasServiceInjector
import timber.log.Timber
import javax.inject.Inject

class TelenetApp : Application(), HasActivityInjector, HasServiceInjector,
    HasBroadcastReceiverInjector {

    @Inject
    lateinit var dispatchingActivityInjector: DispatchingAndroidInjector<Activity>
    @Inject
    lateinit var dispatchingServiceInjector: DispatchingAndroidInjector<Service>
    @Inject
    lateinit var dispatchingBroadcastReceiverInjector: DispatchingAndroidInjector<BroadcastReceiver>
    @Inject
    lateinit var appPreferences: AppPreferences
    @Inject
    lateinit var sessionPreferences: SessionPreferences

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashlyticsTree())
        }

        AppInjector.init(this)
        AndroidThreeTen.init(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            configureNotificationChannels()
        }
    }

    override fun activityInjector() = dispatchingActivityInjector

    override fun serviceInjector() = dispatchingServiceInjector

    override fun broadcastReceiverInjector() = dispatchingBroadcastReceiverInjector

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun configureNotificationChannels() {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        configureNotificationChannel(notificationManager, NotificationChannel.DEFAULT)
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun configureNotificationChannel(
        notificationManager: NotificationManager,
        notificationChannel: NotificationChannel
    ) {
        notificationManager.createNotificationChannel(
            buildNotificationChannel(
                this,
                notificationChannel
            )
        )
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    fun buildNotificationChannel(
        context: Context,
        notificationChannel: NotificationChannel
    ): android.app.NotificationChannel {

        val name = notificationChannel.getName(context)
        val importance = notificationChannel.getImportance(context)
        val description = notificationChannel.getDescription(context)

        val shouldShowLights = notificationChannel.shouldShowLights(context)
        val lightColor = if (shouldShowLights) notificationChannel.getLightColor(context) else null

        val shouldVibrate = notificationChannel.shouldVibrate(context)
        val vibrationPattern = notificationChannel.getVibrationPattern(context)

        val shouldPlayAudio = notificationChannel.shouldPlayAudio(context)
        val audioUri = if (shouldPlayAudio) notificationChannel.getAudioUri(context) else null
        val audioAttributes =
            if (audioUri != null) notificationChannel.getAudioAttributes(context) else null

        val canShowBadge = notificationChannel.canShowBadge(context)

        val canBypassDoNotDisturbMode = notificationChannel.canBypassDoNotDisturbMode(context)

        val group = notificationChannel.getGroup(context)

        val lockScreenVisibility = notificationChannel.getLockScreenVisibility(context)

        val channel = NotificationChannel(notificationChannel.getId(context), name, importance)

        channel.description = description

        channel.enableLights(shouldShowLights)
        if (shouldShowLights && lightColor != null) {
            channel.lightColor = lightColor
        }

        channel.enableVibration(shouldVibrate)
        if (shouldVibrate && vibrationPattern != null) {
            channel.vibrationPattern = vibrationPattern
        }

        if (shouldPlayAudio) {
            channel.setSound(audioUri, audioAttributes)
        }

        channel.setShowBadge(canShowBadge)

        channel.setBypassDnd(canBypassDoNotDisturbMode)

        if (group != null) {
            channel.group = group
        }

        channel.lockscreenVisibility = lockScreenVisibility

        return channel
    }

    enum class NotificationChannel {
        DEFAULT {
            override fun getId(context: Context): String {
                return context.getString(R.string.notification_channel_id_default)
            }

            override fun getName(context: Context): String {
                return context.getString(R.string.notification_channel_name_default)
            }

            @RequiresApi(api = Build.VERSION_CODES.N)
            override fun getImportance(context: Context): Int {
                return NotificationManager.IMPORTANCE_HIGH
            }

            override fun getDescription(context: Context): String {
                return context.getString(R.string.notification_channel_description_default)
            }

            override fun shouldShowLights(context: Context): Boolean {
                return false
            }

            override fun getLightColor(context: Context): Int? {
                return null
            }

            override fun shouldVibrate(context: Context): Boolean {
                return true
            }

            override fun getVibrationPattern(context: Context): LongArray? {
                return null
            }

            override fun shouldPlayAudio(context: Context): Boolean {
                return true
            }

            override fun getAudioUri(context: Context): Uri? {
                return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            override fun getAudioAttributes(context: Context): AudioAttributes? {
                return AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_UNKNOWN)
                    .build()
            }

            override fun canShowBadge(context: Context): Boolean {
                return true
            }

            override fun canBypassDoNotDisturbMode(context: Context): Boolean {
                return false
            }

            override fun getGroup(context: Context): String? {
                return null
            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            override fun getLockScreenVisibility(context: Context): Int {
                return Notification.VISIBILITY_PUBLIC
            }
        };

        abstract fun getId(context: Context): String

        abstract fun getName(context: Context): String

        @RequiresApi(api = Build.VERSION_CODES.N)
        abstract fun getImportance(context: Context): Int

        abstract fun getDescription(context: Context): String

        abstract fun shouldShowLights(context: Context): Boolean

        abstract fun getLightColor(context: Context): Int?

        abstract fun shouldVibrate(context: Context): Boolean

        abstract fun getVibrationPattern(context: Context): LongArray?

        abstract fun shouldPlayAudio(context: Context): Boolean

        abstract fun getAudioUri(context: Context): Uri?

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        abstract fun getAudioAttributes(context: Context): AudioAttributes?

        abstract fun canShowBadge(context: Context): Boolean

        abstract fun canBypassDoNotDisturbMode(context: Context): Boolean

        abstract fun getGroup(context: Context): String?

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        abstract fun getLockScreenVisibility(context: Context): Int
    }

}