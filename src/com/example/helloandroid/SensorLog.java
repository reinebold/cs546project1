package com.example.helloandroid;




import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

/**
 * Collects and labels sensor data.
 * @author Jay
 *
 */
public class SensorLog extends Activity implements SensorEventListener, OnClickListener, Runnable {
    /** Called when the activity is first created. */
    
    private double accX = 0.0;
    private double accY = 0.0;
    private double accZ = 0.0;
    
    private double oriX = 0.0;
    private double oriY = 0.0;
    private double oriZ = 0.0;
    
    private EditText filenameEdit;
    private EditText activityEdit;
    private EditText locationEdit;
    
    private Button timeButton;
    
    
    private double light = 0.0;
    
    private SensorManager mySensorManager;
    
    private Handler myHandler;
    
    private boolean looping = false;
    
    /**
     * Create the UI.  Start listening to the sensors.
     */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        setTitle("DATA LOGGER");
        
        filenameEdit = (EditText) findViewById(R.id.filename);
        activityEdit = (EditText) findViewById(R.id.activity);
        locationEdit = (EditText) findViewById(R.id.location);
        
        timeButton = (Button) findViewById(R.id.time);
        
        timeButton.setOnClickListener(this);
        
        
        
        
        mySensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);

        mySensorManager.registerListener(this, mySensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_NORMAL);
        mySensorManager.registerListener(this, mySensorManager.getDefaultSensor(Sensor.TYPE_LIGHT), SensorManager.SENSOR_DELAY_NORMAL);
        mySensorManager.registerListener(this, mySensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        
        
        this.myHandler = new Handler();
        
    }
   

    /**
     * Not implemented.
     */
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Update sensor knowledge.
	 */
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
			light = event.values[0];
		} else if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
			oriX = event.values[0];
			oriY = event.values[1];
			oriZ = event.values[2];
		} else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			accX = event.values[0];
			accY = event.values[1];
			accZ = event.values[2];
		}
	}
	
	/**
	 * Check if we can use the SD card.
	 * @return
	 */
	public boolean mediaAvailable() {
		boolean mExternalStorageAvailable = false;
		boolean mExternalStorageWriteable = false;
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
		    // We can read and write the media
		    mExternalStorageAvailable = mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
		    // We can only read the media
		    mExternalStorageAvailable = true;
		    mExternalStorageWriteable = false;
		} else {
		    // Something else is wrong. It may be one of many other states, but all we need
		    //  to know is we can neither read nor write
		    mExternalStorageAvailable = mExternalStorageWriteable = false;
		}
		return mExternalStorageAvailable && mExternalStorageWriteable;
	}
	
	/**
	 * Create a file and write the header.
	 * Header:
	 * filename : activityType : locationType\n
	 */
	public void startFile() {
		if (this.mediaAvailable()) {
			File root = Environment.getExternalStorageDirectory();
			String filename = "" + filenameEdit.getText();
			File me = new File(root, filename);
			try {
				System.out.println("trying to start a new file.");
				BufferedWriter out = new BufferedWriter(new FileWriter(me));
				String header = filename + " : " + activityEdit.getText() + " : " + locationEdit.getText();
				out.write(header + "\n");
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Add a line to the datalog.
	 * @param s
	 */
	public void appendString(String s) {
		if (this.mediaAvailable()) {
			File root = Environment.getExternalStorageDirectory();
			String filename = "" + filenameEdit.getText();
			File me = new File(root, filename);
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter(me, true));
				out.write(s + "\n");
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Stop adding to the file.
	 */
	public void endFile() {
		this.looping = false;
	}

	/**
	 * If the user clicks, either start or stop collecting data depending on the state we are in.
	 */
	public void onClick(View v) {
		if (this.timeButton.getText().equals("Start Collection")) {
			//begin collecting data
			this.filenameEdit.setEnabled(false);
			this.activityEdit.setEnabled(false);
			this.locationEdit.setEnabled(false);
			this.startFile();
			this.looping = true;
			this.myHandler.postDelayed(this, 250);
			this.timeButton.setText("End Collection");
		} else {
			//stop collecting data
			this.endFile();
			this.filenameEdit.setEnabled(true);
			this.activityEdit.setEnabled(true);
			this.locationEdit.setEnabled(true);
			this.timeButton.setText("Start Collection");
		}
	}

	/**
	 * Called to start data collection.
	 * Repost every 250 ms.
	 */
	public void run() {
		if (this.looping) {
			String s = "" + System.currentTimeMillis() + " " + accX + " " + accY + " " + accY + " " + oriX + " " + oriY + " " + oriZ + " " + light;
			this.appendString(s);
			this.myHandler.postDelayed(this, 250);
		}
	}
}