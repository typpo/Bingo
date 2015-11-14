package com.project.judymou.bingo;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.project.judymou.bingo.data.FirebaseHelper;

public class NotificationService extends Service {
	private NotificationManager mNM;
	private FirebaseHelper firebaseHelper;

	/**
	 * Class for clients to access.  Because we know this service always
	 * runs in the same process as its clients, we don't need to deal with
	 * IPC.
	 */
	public class NotificationServiceBinder extends Binder {
		NotificationService getService() {
			return NotificationService.this;
		}
	}

	@Override
	public void onCreate() {
		mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		firebaseHelper = FirebaseHelper.getInstance();
		FirebaseHelper.getInstance().getRef().child("boards").addListenerForSingleValueEvent(
				new ValueEventListener() {
					@Override
					public void onDataChange(DataSnapshot snapshot) {
						for (DataSnapshot s : snapshot.getChildren()) {
							String name = (String) s.getKey();
							listenOnBoard(name);
						}
					}

					@Override
					public void onCancelled(FirebaseError firebaseError) {
					}
				});
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		// Tell the user we stopped.
		Toast.makeText(this, "Crab service stopped.", Toast.LENGTH_SHORT).show();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	// This is the object that receives interactions from clients.  See
	// RemoteService for a more complete example.
	private final IBinder mBinder = new NotificationServiceBinder();

	private void listenOnBoard(String boardName) {
		firebaseHelper.getRef()
				.child("scores/" + boardName + "/" + firebaseHelper.getOtherUserName() + "/newstatus")
				.addChildEventListener(new ChildEventListener() {
					@Override
					public void onChildAdded(DataSnapshot dataSnapshot, String s) {
						NotificationCompat.Builder mBuilder =
								new NotificationCompat.Builder(NotificationService.this)
										.setContentTitle("Crabby")
										.setSmallIcon(R.drawable.ic_drawer)
										.setContentText(getDisplayName() + " just made a new move!");

						int mNotificationId = 001;
						mNM.notify(mNotificationId, mBuilder.build());
						firebaseHelper.removeStatus("Thanksgiving Edition");
					}

					@Override
					public void onChildChanged(DataSnapshot dataSnapshot, String s) {
					}

					@Override
					public void onChildRemoved(DataSnapshot dataSnapshot) {

					}

					@Override
					public void onChildMoved(DataSnapshot dataSnapshot, String s) {

					}

					@Override
					public void onCancelled(FirebaseError firebaseError) {

					}
				});
	}

	private String getDisplayName() {
		if (firebaseHelper.getOtherUserName().equals("smallCrab")) {
			return "Small crab";
		}
		return "Large crab";
	}
}