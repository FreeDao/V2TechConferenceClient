package com.v2tech.util;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.v2tech.R;
import com.v2tech.view.PublicIntent;
import com.v2tech.view.StartupActivity;

public class Notificator {
	
	
	static long lastNotificatorTime = 0;
	
	public static void  updateSystemNotification(Context context, String title, String content, int tone, Intent trigger) {
		if (tone > 0 && ((System.currentTimeMillis() /1000) - lastNotificatorTime) >2 ) {
			Uri notification = RingtoneManager
					.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			Ringtone r = RingtoneManager.getRingtone(context,
					notification);
			r.play();
			lastNotificatorTime = System.currentTimeMillis() /1000;
		}
		

		NotificationCompat.Builder builder = new NotificationCompat.Builder(
				context).setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle(title)
				.setContentText(content);

		
		Intent startupActivity = new Intent(context, StartupActivity.class);

		// Creates the PendingIntent
		PendingIntent notifyPendingIntent = PendingIntent.getActivities(
				context, 0, new Intent[] {startupActivity, trigger},
				PendingIntent.FLAG_ONE_SHOT);

		// Puts the PendingIntent into the notification builder
		builder.setContentIntent(notifyPendingIntent);

		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		// mId allows you to update the notification later on.
		mNotificationManager.notify(PublicIntent.MESSAGE_NOTIFICATION_ID,
				builder.build());
	}

	
	
	
	public  static void cancelSystemNotification(Context context) {
		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		// mId allows you to update the notification later on.
		mNotificationManager.cancel(PublicIntent.MESSAGE_NOTIFICATION_ID);
	}
}