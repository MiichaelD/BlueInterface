/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.Bluetooth;

import com.example.android.Bluetooth.R;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This is the main Activity that displays the current chat session.
 */
public class Interface extends Activity {
    // Debugging
    private static final String TAG = "BlueInterface";
    private static final boolean D = true;
    
    public static int currentContentView=0;

    // Message types sent from the BlueInterfaceService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BlueInterfaceService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    // Layout Views
    private TextView mTitle;
    private EditText mOutEditText;
    private Button mSendButton;
    private ImageView im=null;
    private TextView tv=null;
    private Button head;
    private Button back;
    private Button left;
    private Button right;
    private boolean[] inCycle= {false,false};
    public Thread leftThread=null;
    public Thread rightThread=null;

    // Name of the connected device
    private String mConnectedDeviceName = null;
    

    
    		// String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the chat services
    private BluetoothService mChatService = null;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(D) Log.e(TAG, "+++ ON CREATE +++");

        // Set up the window layout
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.main);
        loadInterfaces(currentContentView);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);

        // Set up the custom title
        mTitle = (TextView) findViewById(R.id.title_left_text);
        mTitle.setText(R.string.app_name);
        mTitle = (TextView) findViewById(R.id.title_right_text);
        
                // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
    }
    
    
    public void loadInterfaces(int current) {
    	switch(current) {
    	case 0:
    		// Initialize the compose field with a listener for the return key
            mOutEditText = (EditText) findViewById(R.id.edit_text_out);
            mOutEditText.setOnEditorActionListener(mWriteListener);

            // Initialize the send button with a listener that for click events
            mSendButton = (Button) findViewById(R.id.button_send);
            mSendButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    // Send a message using content of the edit text widget
                    TextView view = (TextView) findViewById(R.id.edit_text_out);
                    String message = view.getText().toString();
                    sendMessage(message);
                }
            });
    		im=(ImageView) findViewById(R.id.imageView1);
    		tv=(TextView) findViewById(R.id.editText1);
    		tv.setFocusable(false);
    		tv.setBackgroundColor(Color.BLACK);
    		tv.setTextColor(Color.CYAN);
    		break;
    		
    	case 1:

       	 head = (Button) findViewById(R.id.button1);
            head.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    sendMessage("1");
                }
            });
            
            back = (Button) findViewById(R.id.button2);
            back.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    sendMessage("2");
                }
            });

            left = (Button) findViewById(R.id.button3);
            left.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
               	 if(inCycle[0]) {
               		 inCycle[0]=false;
               	 }
               	 else {
               		 inCycle[1]=false;
               		 inCycle[0]=true;
               		 leftThread=new Thread(new Runnable() {
             		        public void run(){
             		        	while(inCycle[0]) {
             		        		try {
             		        			sendMessage("4");
             		        			Thread.sleep(400);
             		        		}catch(Exception e) {  
             		        			inCycle[0]=false;
             		        			leftThread=null;
             		        			if(D) Log.i(TAG,e.getMessage(),e);
             		        		}
             		        	}
             		        	leftThread=null;
                            } 
             		});
                	leftThread.start();
               	 }   
                }
            });
            
            right = (Button) findViewById(R.id.button4);
            right.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
               	 if(inCycle[1]) {
               		 inCycle[1]=false;
               	 }
               	 else {
               		 inCycle[0]=false;
               		 inCycle[1]=true;
               		 rightThread=new Thread(new Runnable() {
              		        public void run(){
              		        	while(inCycle[1]) {
              		        		try {
              		        			sendMessage("8");
              		        			Thread.sleep(400);
              		        		}catch(Exception e) {  
             		        			inCycle[1]=false;
             		        			rightThread=null;
             		        			if(D) Log.e(TAG,e.getMessage(),e);
             		        		}
              		        	}
              		        	rightThread=null;
                             } 
              		       });
                 		 rightThread.start();
               	 }     
                }
            });
    		break;
    	}
    }
    

    @Override
    public void onStart() {
        super.onStart();
        if(D) Log.e(TAG, "++ ON START ++");

        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        // Otherwise, setup the chat session
        } else {
            if (mChatService == null) setupChat();
        }
        
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        if(D) Log.e(TAG, "+ ON RESUME +");

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothService.STATE_NONE) {
              // Start the Bluetooth chat services
              mChatService.start();
            }
        }
    }

    private void setupChat() {
        Log.d(TAG, "setupChat()");
        
        // Initialize the BlueInterfaceService to perform bluetooth connections
        mChatService = new BluetoothService(this, mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        if(D) Log.e(TAG, "- ON PAUSE -");
    }

    @Override
    public void onStop() {
        super.onStop();
        if(D) Log.e(TAG, "-- ON STOP --");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        currentContentView=0;
        // Stop the Bluetooth chat services
        if (mChatService != null) mChatService.stop();
        if(D) Log.e(TAG, "--- ON DESTROY ---");
    }

    private void ensureDiscoverable() {
        if(D) Log.d(TAG, "ensure discoverable");
        if (mBluetoothAdapter.getScanMode() !=
            BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    /**
     * Sends a message.
     * @param message  A string of text to send.
     */
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BlueInterfaceService to write
            byte[] send = message.getBytes();
            mChatService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
            mOutEditText.setText(mOutStringBuffer);
        }
    }

    // The action listener for the EditText widget, to listen for the return key
    private TextView.OnEditorActionListener mWriteListener =
        new TextView.OnEditorActionListener() {
        public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
            // If the action is a key-up event on the return key, send the message
            if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                String message = view.getText().toString();
                sendMessage(message);
            }
            if(D) Log.i(TAG, "END onEditorAction");
            return true;
        }
    };

    // The Handler that gets information back from the BlueInterfaceService
    private final Handler mHandler = new Handler() {
    
    	
    @Override
    public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_STATE_CHANGE:
                if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                switch (msg.arg1) {
                case BluetoothService.STATE_CONNECTED:
                    mTitle.setText(R.string.title_connected_to);
                    mTitle.append(mConnectedDeviceName);
                    break;
                case BluetoothService.STATE_CONNECTING:
                    mTitle.setText(R.string.title_connecting);
                    break;
                case BluetoothService.STATE_LISTEN:
                case BluetoothService.STATE_NONE:
                    mTitle.setText(R.string.title_not_connected);
                    break;
                }
                break;
            case MESSAGE_WRITE:
                byte[] writeBuf = (byte[]) msg.obj;
                // construct a string from the buffer
                String writeMessage = new String(writeBuf);
                try {
            		UpdateP(Integer.parseInt(writeMessage),currentContentView);
            	}catch(NumberFormatException e) {}
                break;
            case MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
                if(msg.arg1>0) {
                	String readMessage = new String(readBuf, 0, msg.arg1);
                	try {
                		UpdateP(Integer.parseInt(readMessage),currentContentView);
                	}catch(NumberFormatException e) {tv.setText(readMessage);}
                }
                break;
            case MESSAGE_DEVICE_NAME:
                // save the connected device's name
                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                Toast.makeText(getApplicationContext(), "Connected to "
                               + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                break;
            case MESSAGE_TOAST:
                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                               Toast.LENGTH_SHORT).show();
                break;
            }
        }
    };
    /*
     * Change imageResource to ImageView if currentContentView = 0 (Cara)
     * */
    public void UpdateP(int msg,int current) {
    	if(current==0)
    	switch(msg) {
		case 0:im.setImageResource(R.drawable.i0);break;
		case 1:im.setImageResource(R.drawable.i1);break;
		case 2:im.setImageResource(R.drawable.i2);break;
		case 3:im.setImageResource(R.drawable.i3);break;
		case 4:im.setImageResource(R.drawable.i4);break;
		case 5:im.setImageResource(R.drawable.i5);break;
		case 6:im.setImageResource(R.drawable.i6);break;
		case 7:im.setImageResource(R.drawable.i7);break;
		default: Toast.makeText(getApplicationContext(),Integer.toString(msg),Toast.LENGTH_LONG).show();break;
	}
    }

    
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
        case REQUEST_CONNECT_DEVICE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                // Get the device MAC address
                String address = data.getExtras()
                                     .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                // Get the BLuetoothDevice object
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                // Attempt to connect to the device
                mChatService.connect(device);
            }
            break;
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                // Bluetooth is now enabled, so set up a chat session
                setupChat();
            } else {
                // User did not enable Bluetooth or an error occured
                Log.d(TAG, "BT not enabled");
                Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.scan:
            // Launch the DeviceListActivity to see devices and do scan
            Intent serverIntent = new Intent(this, DeviceListActivity.class);
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
            return true;
        case R.id.discoverable:
            // Ensure this device is discoverable by others
            ensureDiscoverable();
            return true;
        case R.id.item1:
        	if(currentContentView==0) {
        		setContentView(R.layout.carro);
        		currentContentView=1;
        	}else {
        		setContentView(R.layout.main);
        		currentContentView=0;}
        	loadInterfaces(currentContentView);
        	return true;
        }
        return false;
    }

}