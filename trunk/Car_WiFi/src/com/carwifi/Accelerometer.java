package com.carwifi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import com.carwifi.R;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

/**
 * @author João Lopes
 */

public class Accelerometer extends Activity implements AccelerometerListener {

	private final String ipAddress = "192.168.1.36"; // arduino ip
	private final int port = 1000;

	Socket kkSocket = null;
	PrintWriter out = null;
	BufferedReader in = null;

	private static Context CONTEXT;
	float acc;

	ToggleButton tb;
	Button left,right;
	Button exit;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.setFullscreen(); //change Activity to fullscreen
		
		setContentView(R.layout.main);
		CONTEXT = this;

		tb = (ToggleButton) findViewById(R.id.TogB);
		tb.setOnClickListener(tbListener());
		
		left = (Button) findViewById(R.id.left);
		left.setOnTouchListener(touchButton());
		
		right = (Button) findViewById(R.id.right);
		right.setOnTouchListener(touchButton());
		
		exit = (Button) findViewById(R.id.exit);
		exit.setOnClickListener(exit());

		try {
			kkSocket = new Socket(ipAddress, port);
		} catch (UnknownHostException e) {
			errorMessage("Unknown host" + ipAddress);
		} catch (IOException e) {
			errorMessage("Couldn't get I/O for the connection to: " + ipAddress);
		}

	}

	private OnClickListener tbListener() { // listeners for "on" button
		return new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (v.getId()==tb.getId()){
					if (!tb.isChecked()) {
						try {
							out = new PrintWriter(kkSocket.getOutputStream(), true);
							out.println((int) (0));
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}

		};
	}
	
	private OnClickListener exit() { // listener for "exit" button
		return new OnClickListener() {

			@Override
			public void onClick(View v) {
				onDestroy();
				finish();
			}

		};
	}
	
	private OnTouchListener touchButton() { // listeners for "left" and "right" buttons
		return new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent mEvent) {
				if(mEvent.getAction()==MotionEvent.ACTION_UP){ // if left out right buttons are up
					try {
						out = new PrintWriter(kkSocket.getOutputStream(), true);
						out.println("c");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				else{
					if(v.getId()==left.getId()){ // left button is down
						try {
							out = new PrintWriter(kkSocket.getOutputStream(), true);
							out.println("l");
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					else if(v.getId()==right.getId()){ // right button is down
						try {
							out = new PrintWriter(kkSocket.getOutputStream(), true);
							out.println("r");
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
				return false;
			}

		};
	}

	public void setFullscreen() { // function to set activity in fullscreen
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	private void errorMessage(String msg) {
		System.err.println("Error:" + msg);
	}

	protected void onResume() {
		super.onResume();
		
		if (AccelerometerManager.isSupported()) {
			AccelerometerManager.startListening(this);
		}
		tb.setChecked(false);

		if (kkSocket == null) {
			try {
				kkSocket = new Socket(ipAddress, port);
			} catch (UnknownHostException e) {
				errorMessage("Unknown host" + ipAddress);
			} catch (IOException e) {
				errorMessage("Couldn't get I/O for the connection to: "
						+ ipAddress);
			}
		}

	}

	protected void onDestroy() {
		super.onDestroy();
		if (AccelerometerManager.isListening()) {
			AccelerometerManager.stopListening();
		}
		
		tb.setChecked(false);

		if( kkSocket == null)
			return;
		
		try {
			out = new PrintWriter(kkSocket.getOutputStream(), true);
			out.println((int) (0));
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			kkSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// kkSocket=null;

	}

	public static Context getContext() {
		return CONTEXT;
	}

	/**
	 * onShake callback
	 */
	public void onShake(float force) {
		Toast.makeText(getContext(), "Phone shaked : " + force, 1000).show();
	}

	/**
	 * onAccelerationChanged callback
	 */
	public void onAccelerationChanged(float x, float y, float z) {
		((TextView) findViewById(R.id.x)).setText("x= " + String.valueOf(x));
		((TextView) findViewById(R.id.y)).setText("y= " + String.valueOf(y));
		((TextView) findViewById(R.id.z)).setText("z= " + String.valueOf(z));

		if (!tb.isChecked())
			return;

		acc = z;

		if (Math.abs(acc) > 1) { // if z is between -1 and 1, car is stopped
			try {
				out = new PrintWriter(kkSocket.getOutputStream(), true);
				out.println((int) (acc * 254) / 10);

			} catch (UnknownHostException e) {
				errorMessage("Unknown host" + ipAddress);
			} catch (IOException e) {
				errorMessage("Couldn't get I/O for the connection to: "+ ipAddress);
			}

		} else {
			try {
				out = new PrintWriter(kkSocket.getOutputStream(), true);
			} catch (IOException e) {
				e.printStackTrace();
			}
			out.println((int) (0));
		}

	}

}