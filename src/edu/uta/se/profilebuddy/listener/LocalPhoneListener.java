package edu.uta.se.profilebuddy.listener;

import java.lang.reflect.Method;

import android.content.Context;
import android.os.Binder;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import edu.uta.se.profilebuddy.R;
import edu.uta.se.profilebuddy.service.ActivityRecognitionService;

/**
 * Custom phone state change listener to support Driving mode. Takes pre-defined
 * actions on phone calls.
 */
public class LocalPhoneListener extends PhoneStateListener
{

	private static final String TAG = LocalPhoneListener.class.getName();

	// -- constants
	private static String TEXT_MESSAGE = "I'm driving at the moment, I'll call you back later.";
	private static final int SLEEP_INTERVAL = 5000; // -- ms

	// -- singleton instance
	public static volatile LocalPhoneListener instance;

	// -- instance variables
	private TelephonyManager telephony;
	private ActivityRecognitionInvoker invoker;
	private boolean listenerActivated = false;
	private boolean ringing = false;
	private Context context;

	private LocalPhoneListener(Context context)
	{
		super();
		this.context = context;
		TEXT_MESSAGE = this.context.getResources().getString(R.string.text_message_driving_mode);
	}

	/**
	 * Creates and returns singleton instance of the Class.
	 * 
	 * @param context
	 *            - The {@link Context} object.
	 * @return - {@link LocalPhoneListener} instance.
	 */
	public static LocalPhoneListener getInstance(Context context)
	{
		if (instance == null)
		{
			instance = new LocalPhoneListener(context);
		}
		return instance;
	}

	public static LocalPhoneListener getInstance()
	{
		return instance;
	}

	/**
	 * Wrapper - Registers {@link TelephonyManager} listener and initializes
	 * {@link ActivityRecognitionInvoker} object.
	 */
	public void register()
	{
		Log.i(TAG, "Registering phoneListener");
		if (!listenerActivated)
		{
			telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			telephony.listen(instance, PhoneStateListener.LISTEN_CALL_STATE);
			invoker = ActivityRecognitionInvoker.getInstance(context);
			listenerActivated = true;
		}
	}

	/**
	 * Wrapper - Removes {@link TelephonyManager} listener and removes
	 * {@link ActivityRecognitionInvoker} object reference.
	 */
	public void unregister()
	{
		Log.i(TAG, "Un-registering phoneListener");
		if (listenerActivated)
		{
			if (invoker != null)
			{
				invoker.suspend();
			}
			telephony.listen(instance, LISTEN_NONE);
			listenerActivated = false;
		}
	}

	@Override
	public void onCallStateChanged(int state, String incomingNumber)
	{
		Log.i(TAG, "Entering onCallStateChanged - { state:" + state + " }");
		switch (state)
		{
		case TelephonyManager.CALL_STATE_RINGING:
			ringing = true;
			createWaitingThread(incomingNumber);
			collectUserActivity();
			break;
		case TelephonyManager.CALL_STATE_OFFHOOK:
			ringing = false;
			break;
		case TelephonyManager.CALL_STATE_IDLE:
			ringing = false;
			break;
		}
	}

	/**
	 * Calls invoker on {@link ActivityRecognitionInvoker}, which starts the
	 * service in its implementation.
	 */
	private void collectUserActivity()
	{
		Log.i(TAG, "Entering collectUserActivity");
		invoker.connect();
		Log.i(TAG, "Exiting collectUserActivity");
	}

	/**
	 * Custom code to disconnect a call. Reference from -
	 * http://stackoverflow.com/questions/20965702/end-incoming
	 * -call-programmatically
	 */
	private void disconnectCall()
	{
		try
		{
			String serviceManagerName = "android.os.ServiceManager";
			String serviceManagerNativeName = "android.os.ServiceManagerNative";
			String telephonyName = "com.android.internal.telephony.ITelephony";

			Class<?> telephonyClass = Class.forName(telephonyName);
			Class<?> telephonyStubClass = telephonyClass.getClasses()[0];
			Class<?> serviceManagerClass = Class.forName(serviceManagerName);
			Class<?> serviceManagerNativeClass = Class.forName(serviceManagerNativeName);

			Method getService = serviceManagerClass.getMethod("getService", String.class);
			Method tempInterfaceMethod = serviceManagerNativeClass.getMethod("asInterface",
					IBinder.class);

			Binder tmpBinder = new Binder();
			tmpBinder.attachInterface(null, "fake");
			Object serviceManagerObject = tempInterfaceMethod.invoke(null, tmpBinder);
			IBinder retbinder = (IBinder) getService.invoke(serviceManagerObject, "phone");
			Method serviceMethod = telephonyStubClass.getMethod("asInterface", IBinder.class);
			Object telephonyObject = serviceMethod.invoke(null, retbinder);
			Method telephonyEndCall = telephonyClass.getMethod("endCall");
			telephonyEndCall.invoke(telephonyObject);

		}
		catch (Exception exe)
		{
			Log.e(TAG, "Exception while disconnecting call - " + exe.getMessage());
		}
	}

	/**
	 * Utility function to send text message to the Caller.
	 * 
	 * @param phoneNumber
	 *            - The phone number to which the message needs to be send.
	 */
	private void sendMessage(String phoneNumber)
	{
		try
		{
			SmsManager smsManager = SmsManager.getDefault();
			smsManager.sendTextMessage(phoneNumber, null, TEXT_MESSAGE, null, null);
		}
		catch (Exception exe)
		{
			Log.i(TAG, "Exception while sending message - " + exe.getMessage());
		}
	}

	/**
	 * Creates a new thread and leaves the execution of Driving mode related
	 * actions with it. Sleeps for specified specified interval, if the phone
	 * state is still ringing, disconnects the call and sends text message.
	 * 
	 * @param phoneNumber
	 *            - The phoneNumber required for taking above mentioned actions.
	 */
	private void createWaitingThread(final String phoneNumber)
	{
		Runnable sleepingThread = new Runnable()
		{

			@Override
			public void run()
			{
				try
				{
					Thread.sleep(SLEEP_INTERVAL);
					startAction();
				}
				catch (InterruptedException ie)
				{
					Log.e(TAG, "Thread sleep failed - " + ie.getMessage());
				}
			}

			/**
			 * Wrapper - which combines Driving mode related actions.
			 */
			private void startAction()
			{
				// -- reading user activity and confidence
				String activity = ActivityRecognitionService.ACTIVITY;
				int confidence = ActivityRecognitionService.CONFIDENCE;
				Log.i(TAG, "Reading user activity - { activity:" + activity + ", confidence:"
						+ confidence + " }");
				if (invoker != null)
				{
					invoker.suspend();
				}

				Log.i(TAG, "Is phone still ringing - { status: " + ringing + " }");
				if (ringing && ("in_vehicle".equalsIgnoreCase(activity) && confidence > 50))
				{
					disconnectCall();
					sendMessage(phoneNumber);
				}
			}
		};
		new Thread(sleepingThread).start();
	}

}
