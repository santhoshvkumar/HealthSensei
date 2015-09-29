package com.example.droid_brsp_term;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;






import java.util.Random;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;





//import com.blueradios.Brsp;
//import com.blueradios.BrspCallback;
//import com.blueradios.exception.UnstableException;
import com.example.droid_brsp_sample.R;

import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.text.Editable;
import android.text.method.TextKeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class MainActivity extends Activity {
    private final String TAG = "BRSPTERM." + this.getClass().getSimpleName();
    private static final int MAX_OUTPUT_LINES = 100 + 1; // max lines in the
							 // outputView
    static List<String> _outputLines = new ArrayList<String>();

    private Brsp _brsp;
    private BluetoothDevice _selectedDevice;

    private AlertDialog confirm;
    private EditText _txtCommand;
    private TextView _textViewOutput;
    private View _view;
    private ScrollView _scrollView;	
    
    private Button btnSetting;
    private int countSetting= 0;
    private int mInterval = 7000; // 5 seconds by default, can be changed later
    private Handler mHandler;
    
    private BrspCallback _brspCallback = new BrspCallback() {

	@Override
	public void onSendingStateChanged(Brsp obj) {
	    Log.d(TAG, "onSendingStateChanged thread id:" + Process.myTid());
	    runOnUiThread(new Runnable() {
		    @Override
		    public void run() {
		    	
		    	if (_brsp != null ) {
		    		
		    		_brsp.getBrspMode();
		    		
		    		Toast.makeText(MainActivity.this,"Ack"+Brsp.BRSP_MODE_DATA, Toast.LENGTH_LONG).show();
		    	}
		    }
		});
	}

	@Override
	public void onConnectionStateChanged(Brsp obj) {
	    Log.d(TAG, "onConnectionStateChanged state:" + obj.getConnectionState() + " thread id:" + Process.myTid());
	    final Brsp brspObj = obj;
	    runOnUiThread(new Runnable() {
		@Override
		public void run() {
		    invalidateOptionsMenu();
		    BluetoothDevice currentDevice = brspObj.getDevice();
		    if (currentDevice != null && brspObj.getConnectionState() == BluetoothProfile.STATE_CONNECTED) {
			brspObj.readRssi();
			// Log.d(TAG, "Creating bond for device:" +
			// currentDevice.getAddress());
			// currentDevice.createBond();
		    }
		}
	    });
	}

	@Override
	public void onDataReceived(final Brsp obj) {
	    Log.d(TAG, "onDataReceived thread id:" + Process.myTid());
	    runOnUiThread(new Runnable() {
		@Override
		public void run() {
		    byte[] bytes = obj.readBytes();
		    try{
			    String input = new String(bytes);
				addLineToTextView(input);
		    }
		    catch(Exception e){
				Toast.makeText(getApplicationContext(), "Something Bad Happened",  Toast.LENGTH_SHORT).show();
				 //recreate();
			}
		    if (bytes != null) {
		    	try{
						String input = new String(bytes);
						addLineToTextView(input);
		    	}
		    	catch(Exception e){
					Toast.makeText(getApplicationContext(), "Something Bad Happened",  Toast.LENGTH_SHORT).show();
					// recreate();
				}
		    } else {
			// This occasionally happens but no data should be lost
		    }
		}

		private void addLineToTextView(final String lineText) {
		    _textViewOutput.append(lineText);
		    removeLinesFromTextView();
		    
		
		    _scrollView.post(new Runnable() {
			@Override
				public void run() {
				    _scrollView.fullScroll(ScrollView.FOCUS_DOWN);
				    List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(1);
				    nameValuePair.add(new BasicNameValuePair("name", lineText));
					final TextView heartViewTxt = (TextView) findViewById(R.id.heartRate);
					final TextView spo2Txt = (TextView) findViewById(R.id.lblSPO2);
					final TextView tempTxt = (TextView) findViewById(R.id.txtTemp);
					final TextView btrTxt = (TextView) findViewById(R.id.lblBat);
					
					
					String firstLetter = String.valueOf(lineText.charAt(0));
					if(19 < lineText.length() ){
						
						
						String[] getSperateValues = lineText.split("qq");
						
					
						for(String getAllValue:getSperateValues){
							final String temp = getAllValue.substring(1, 4);
							Toast.makeText(getApplicationContext(), "Device Value :"+getAllValue,  Toast.LENGTH_SHORT).show();
							firstLetter =  String.valueOf(getAllValue.charAt(0));
							
							
							if(firstLetter.equals("H")){
								
								
								try {
										int HeartRate = Integer.parseInt(temp);
										String uri="";
										
										if(HeartRate < 45)
											 uri = "@drawable/critical";
										else if (HeartRate > 45 && HeartRate < 59)
											 uri = "@drawable/inter";
										else if (HeartRate > 60 && HeartRate < 99)
											 uri = "@drawable/ok";
										else if (HeartRate > 100 && HeartRate < 109)
											 uri = "@drawable/inter";
										else if (HeartRate > 109)
											 uri = "@drawable/critical";
										
										String[] getEachValue = getAllValue.split(",");
										
										String getStartDate = getEachValue[1];
										
										
										String getStartTime = getEachValue[2];
										
										String startNotes = getStartDate.substring(0, 2)+"-"+getStartDate.charAt(2)+getStartDate.charAt(3)+"-"+getStartDate.charAt(4)+getStartDate.charAt(5);
										
										String startTime = getStartTime.substring(0, 2)+":"+getStartTime.charAt(2)+getStartTime.charAt(3)+":"+getStartTime.charAt(4)+getStartTime.charAt(5);
										
										runOnUiThread(new Runnable() {
										    @Override
										    public void run() {
										    		heartViewTxt.setText(temp);
										    		
										    }
										    
										});
								}
								catch(Exception e){
									Toast.makeText(getApplicationContext(), "Somthing Heart"+e+"----- "+getAllValue,  Toast.LENGTH_LONG).show();
								}
							}
							else if(firstLetter.equals("S")){
								try{
									int SPO2Rate = Integer.parseInt(temp);
									
									String uri ="";
									if(SPO2Rate < 92)
										 uri = "@drawable/critical";
									else if (SPO2Rate >= 92 && SPO2Rate < 95)
										 uri = "@drawable/inter";
									else if (SPO2Rate >= 95 && SPO2Rate < 100)
										 uri = "@drawable/ok";
								
								
									String[] getEachValue = getAllValue.split(",");
									
									String getStartDate = getEachValue[1];
									
									String getStartTime = getEachValue[2];
									
									String startNotes = getStartDate.substring(0, 2)+"-"+getStartDate.charAt(2)+getStartDate.charAt(3)+"-"+getStartDate.charAt(4)+getStartDate.charAt(5);
									String startTime = getStartTime.substring(0, 2)+":"+getStartTime.charAt(2)+getStartTime.charAt(3)+":"+getStartTime.charAt(4)+getStartTime.charAt(5);
									
									
									runOnUiThread(new Runnable() {
									    @Override
									    public void run() {
									    			spo2Txt.setText(temp);
									    			
									    }
									    
									});
								}
								catch(Exception e){
									Toast.makeText(getApplicationContext(), "Somthing SPO2"+e+"----- "+getAllValue,  Toast.LENGTH_LONG).show();
								}
							}
							else if(firstLetter.equals("T")){
								try {
									Float celcious =(float) 0.0;
									try{
										 celcious=(float) (Float.parseFloat(lineText.substring(1, 3)+"."+lineText.charAt(3))*1.8) + 32;
									}
									catch(Exception e){
										Toast.makeText(getApplicationContext(), "start"+e+"----- "+lineText.substring(1, 3),  Toast.LENGTH_LONG).show();
										Log.e(TAG, "Ayooo he he he Exception :",e);
										
									}
									String uri="";
									if(celcious < 96)
										 uri = "@drawable/critical";
									else if (celcious > 96 && celcious < 97 )
										 uri = "@drawable/inter";
									else if (celcious > 97 && celcious < 100)
										 uri = "@drawable/ok";
									else if (celcious > 100)
										 uri = "@drawable/critical";
									
									tempTxt.setText(String.format("%.1f", celcious) );
									String[] getEachValue = getAllValue.split(",");
									String reading ="";
									try{
										 reading = celcious.toString();
										 
											
											    	
											 
									}
									catch(Exception e){
										Toast.makeText(getApplicationContext(), "Ayoooo"+e+"----- "+celcious.toString(),  Toast.LENGTH_LONG).show();
										Log.e(TAG, "Ayooo Exception :",e);
										
									}
									String getStartDate = getEachValue[1];
									
									String getStartTime = getEachValue[2];
									
									String startNotes = getStartDate.substring(0, 2) +"-"+getStartDate.charAt(2)+getStartDate.charAt(3)+"-"+getStartDate.charAt(4)+getStartDate.charAt(5);
									String startTime = getStartTime.substring(0, 2) +":"+getStartTime.charAt(2)+getStartTime.charAt(3)+":"+getStartTime.charAt(4)+getStartTime.charAt(5);
									try {
										//mydatabase.execSQL("insert into tbl_temp(tempRange, startDate, endTime, userID) values ('"+reading+"','"+startNotes+"','"+startTime+"','1')");
									}
									catch(Exception e){
										Toast.makeText(getApplicationContext(), "failed during Insert"+e+"----- "+reading,  Toast.LENGTH_LONG).show();
										Log.e(TAG, "Ayooo reading Exception :",e);
										
									}
								}
								catch(Exception e){
									Toast.makeText(getApplicationContext(), "Somthing Temp"+e+"----- "+getAllValue,  Toast.LENGTH_LONG).show();
									Log.e(TAG, "Temp Exception :",e);
									
								}
								
								
							}
							else if(firstLetter.equals("A")){
								
							//	accTxt.setText(temp);
								
							}
							
						}
						
						
						// Encoding data
					
					    
					}
					else if(firstLetter.equals("P")){
						 Toast.makeText(getApplicationContext(), "PanAlert",  Toast.LENGTH_LONG).show();
						 try {
						//	   SmsManager smsManager = SmsManager.getDefault();
					       //  smsManager.sendTextMessage(StoredValue, null, "THis is emergergency from health Sense", null, null);
					         Toast.makeText(getApplicationContext(), "SMS sent.",
					         Toast.LENGTH_LONG).show();
					      } catch (Exception e) {
					         Toast.makeText(getApplicationContext(),
					         "SMS faild, please try again.",
					         Toast.LENGTH_LONG).show();
					         e.printStackTrace();
					      }
					}
					else if(firstLetter.equals("B")){
						try{
							String text = lineText.substring(1, 4);
							final Float temp = Float.parseFloat(text);
							double perctBt = 4.2 - temp;
							
							int perctBtInt = (int) (perctBt *100);
							perctBt = 100 - perctBtInt;
							runOnUiThread(new Runnable() {
							    @Override
							    public void run() {
							    	btrTxt.setText(String.format("%.1f",temp));
							    }
							});
							Toast.makeText(MainActivity.this,"Battery %"+ perctBt, Toast.LENGTH_SHORT).show();
								//	checkBattery(perctBt);
						}
						catch(Exception e){
							Toast.makeText(getApplicationContext(), "Somthing Battery"+e,  Toast.LENGTH_LONG).show();
						}
						
					}
					
					else if(firstLetter.equals("A")){
						runOnUiThread(new Runnable() {
						    @Override
						    public void run() {
						    		Toast.makeText(MainActivity.this,"Ack", Toast.LENGTH_LONG).show();
						    }
						});
					}
				}
		    });
		}

		private void removeLinesFromTextView() {
		    int linesToRemove = _textViewOutput.getLineCount() - MAX_OUTPUT_LINES;
		    if (linesToRemove > 0) {
			for (int i = 0; i < linesToRemove; i++) {
			    Editable text = _textViewOutput.getEditableText();
			    int lineStart = _textViewOutput.getLayout().getLineStart(0);
			    int lineEnd = _textViewOutput.getLayout().getLineEnd(0);
			    text.delete(lineStart, lineEnd);
			}
		    }
		}

	    });
	    super.onDataReceived(obj);
	}

	@Override
	public void onError(Brsp obj, Exception e) {
	    Log.e(TAG, "onError:" + e.getMessage() + " thread id:" + Process.myTid());
	    super.onError(obj, e);
	    if (e instanceof UnstableException) {
		Log.d(TAG, "Unstable Caught");
		runOnUiThread(new Runnable() {
		    @Override
		    public void run() {
			Toast.makeText(MainActivity.this, "Bluetooth has become unstable!  Restarting Adapter...", Toast.LENGTH_LONG).show();
			// Cycle the bluetooth here to try and recover from an unstable adapter
		    recreate();
		    }
		});
	    }
	}

	@Override
	public void onBrspModeChanged(Brsp obj) {
	    super.onBrspModeChanged(obj);
	    runOnUiThread(new Runnable() {
		@Override
		public void run() {
		    invalidateOptionsMenu();
		}
	    });
	}

	@Override
	public void onRssiUpdate(Brsp obj) {
	    Log.d(TAG, "onRssiUpdate thread id:" + Process.myTid());
	    super.onRssiUpdate(obj);
	    Log.d(TAG, "Remote device RSSI:" + obj.getLastRssi()); // Log RSSI
	}

	@Override
	public void onBrspStateChanged(Brsp obj) {
	    super.onBrspStateChanged(obj);
	    int currentState = obj.getBrspState();
	    Log.d(TAG, "onBrspStateChanged thread id:" + Process.myTid() + " State:" + currentState);
	    obj.readRssi(); // read the RSSI once
	    if (obj.getBrspState() == Brsp.BRSP_STATE_READY) {
		Log.d(TAG, "BRSP READY");
		// Ready to write
		// _brsp.writeBytes("Test".getBytes());
	    } else {
		Log.d(TAG, "BRSP NOT READY");
	    }
	}

    };

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
	// Hack to prevent onCreate being called on orientation change
	// This probably should be done in a better way in a real app
	// http://stackoverflow.com/questions/456211/activity-restart-on-rotation-android
	super.onConfigurationChanged(newConfig);
    }

    //Function can be used to disable or enable the bluetooth
    private boolean setBluetooth(boolean enable) {
	BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	boolean isEnabled = bluetoothAdapter.isEnabled();
	// Intent enableBtIntent = new
	// Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
	if (enable) {
	    // startActivityForResult(enableBtIntent, 1);
	    return bluetoothAdapter.enable();
	} else {
	    // startActivityForResult(enableBtIntent, 0);
	    return bluetoothAdapter.disable();
	}
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
	@Override
	public void onReceive(Context context, Intent intent) {
	    final String action = intent.getAction();
	    
	    if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
		final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
		 Log.e(TAG, "STATE:" + state);
		// addLineToTextView("State"+state);
		switch (state) {
		case BluetoothAdapter.STATE_OFF:
		    Log.d(TAG, "Enabling Bluetooth.  Result:" + setBluetooth(true));
		    break;
		case BluetoothAdapter.STATE_TURNING_OFF:
		    // TODO: Disable user input and show restarting msg
		    break;
		case BluetoothAdapter.STATE_ON:
		     //_brsp = new Brsp(_brspCallback, 10000, 10000);
		    // doScan();
			
		    break;
		case BluetoothAdapter.STATE_TURNING_ON:
		    break;
		}
	    }
	}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	Log.d(TAG, "onCreate");
	super.onCreate(savedInstanceState);

	IntentFilter adapterStateFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
	this.registerReceiver(mReceiver, adapterStateFilter);

	setContentView(R.layout.activity_main);
	_txtCommand = (EditText) findViewById(R.id.editTextCommand);
	_textViewOutput = (TextView) findViewById(R.id.textViewOutput);
	_scrollView = (ScrollView) findViewById(R.id.scrollView);
	_textViewOutput.setOnClickListener(new View.OnClickListener() {
	    @Override
	    public void onClick(View v) {
		// hideSoftKeyboard(); //Uncomment to hide keyboard after every
		// entry
	    }
	});
	_txtCommand.setImeOptions(EditorInfo.IME_ACTION_SEND);
	_txtCommand.setOnEditorActionListener(new OnEditorActionListener() {
	    @Override
	    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if (actionId == EditorInfo.IME_ACTION_SEND) {
		    return onEnterClicked(_txtCommand.getText());
		}
		Toast.makeText(MainActivity.this, "Not Connected", Toast.LENGTH_SHORT).show();
		return false;
	    }
	});

	_brsp = new Brsp(_brspCallback, 10000, 10000);
	 mHandler = new Handler();
     startRepeatingTask();
	
	doScan();
	
	
	btnSetting = (Button) findViewById(R.id.btnSetting);
	
	
	btnSetting.setOnClickListener(new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			
			//Toast.makeText(Dashboard.this, "Setting Clicked..."+countSetting, Toast.LENGTH_LONG).show();
			Calendar c = Calendar.getInstance();
        	SimpleDateFormat df = new SimpleDateFormat("hh:mm:ss");
        	
        	SimpleDateFormat dateFormat = new SimpleDateFormat("dd:MM:yy");
    		String dateSetting= dateFormat.format(c.getTime()).toString();
    		String sendTime = df.format(c.getTime()).toString();
        	sendTime = "St"+sendTime+"qq";
    		String emptyValue =" ";
    		//_brsp.writeBytes(emptyValue.getBytes());
        
    	   if(countSetting == 0){
				_brsp.writeBytes(emptyValue.getBytes());
				countSetting++;
			}
    	   else if(countSetting == 1){
				
		    		String commandString = "SYNC SETTGqq";
		    		_brsp.writeBytes(commandString.getBytes());
		    	//	Toast.makeText(MainActivity.this, "Setting Clicked..." + commandString+" "+ countSetting, Toast.LENGTH_LONG).show();
		    		countSetting++;
		    		_brsp.writeBytes(emptyValue.getBytes());
		    		countSetting++;
		    		 btnSetting.setText("Setting Click to Continue");
			}
			else if(countSetting == 2){
				
				
			}
			
        	else if(countSetting == 3){

	    		
	    		dateSetting ="Sd"+dateSetting+"qq";
	    		final String ds = dateSetting;
	    		_brsp.writeBytes(ds.getBytes());
	    	
	    		countSetting++;
	    		//Toast.makeText(MainActivity.this,"Date"+  ds+ " "+countSetting, Toast.LENGTH_LONG).show();
	    		_brsp.writeBytes(emptyValue.getBytes());
				countSetting++;
				 btnSetting.setText("Setting Click to Continue");
			}
        	else if(countSetting == 4){
				_brsp.writeBytes(emptyValue.getBytes());
				countSetting++;
			}
			else if(countSetting == 5){

	    		final String commandHeart = "DH003+++++qq";
	    		_brsp.writeBytes(commandHeart.getBytes());
	    		countSetting++;
	    		//Toast.makeText(MainActivity.this,"commandHeart :"+  commandHeart+ " "+countSetting, Toast.LENGTH_LONG).show();
	    		_brsp.writeBytes(emptyValue.getBytes());
				countSetting++;
				 btnSetting.setText("Setting Click to Continue");
			}
			else if(countSetting == 6){
				_brsp.writeBytes(emptyValue.getBytes());
				countSetting++;
			}
			else if(countSetting == 7){
	    		final String commandSPO2 = "DS005+++++qq";
	    		_brsp.writeBytes(commandSPO2.getBytes());
	    	
	    		countSetting++;
	    		//Toast.makeText(MainActivity.this,"commandSPO2 :"+  commandSPO2+ " "+countSetting, Toast.LENGTH_LONG).show();
	    		_brsp.writeBytes(emptyValue.getBytes());
				countSetting++;
				 btnSetting.setText("Setting Click to Continue");
			}
			else if(countSetting == 8){
				_brsp.writeBytes(emptyValue.getBytes());
				countSetting++;
			}
			else if(countSetting == 9){
				final String commandTemp = "DT001+++++qq";
				_brsp.writeBytes(commandTemp.getBytes());
	    		countSetting++;
	    		//Toast.makeText(MainActivity.this,"commandTemp :"+  commandTemp+ " "+countSetting, Toast.LENGTH_LONG).show();
	    		_brsp.writeBytes(emptyValue.getBytes());
				countSetting++;
				 btnSetting.setText("Setting Click to Continue");
			}
			else if(countSetting == 10){
				_brsp.writeBytes(emptyValue.getBytes());
				countSetting++;
			}
			else if(countSetting == 11){
					final String commandAcc = "DD002+++++qq"; // Sync freq. in min
					_brsp.writeBytes(commandAcc.getBytes());
		    		
		    		
		    		countSetting++;
		    		//Toast.makeText(MainActivity.this,"commandAcc :"+  commandAcc+ " "+countSetting, Toast.LENGTH_LONG).show();
		    		_brsp.writeBytes(emptyValue.getBytes());
					countSetting++;
					 btnSetting.setText("Setting Click to Continue");
			}
			else if(countSetting == 12){
				_brsp.writeBytes(emptyValue.getBytes());
				countSetting++;
			}
			else if(countSetting == 13){

	    		String sendTimes = df.format(c.getTime()).toString();
	    		sendTimes = "St"+sendTimes+"qq";
	        	final String st = sendTimes;
	        	_brsp.writeBytes(st.getBytes());
	        	
	        	
	        
	        	countSetting++;
	        	//Toast.makeText(MainActivity.this,"Time :"+  st+ " "+countSetting, Toast.LENGTH_LONG).show();
	        	_brsp.writeBytes(emptyValue.getBytes());
				countSetting++;
				 btnSetting.setText("Setting Completed");
				 btnSetting.setClickable(false);
				 btnSetting.setVisibility(View.INVISIBLE);
			}
			else if(countSetting == 14){
				_brsp.writeBytes(emptyValue.getBytes());
				countSetting++;
			}
		
			
		}
	});

    }
    
    /***** THis is stupid lines*************/
    Runnable mStatusChecker = new Runnable() {
        @Override 
        public void run() {
        	 final Random random = new Random();
    	        int i = random.nextInt(2 - 0 + 1) + 0;
    	        Log.d(TAG, "Selected Device id:" +_selectedDevice+":"+_brsp.getConnectionState()+":_brsp"+_brsp);
    	        
    	        if (_selectedDevice != null) {
    	        	
    	        	  if (_brsp != null){
    	        		 // doScan();
    	        		  Log.d(TAG,"");
    	        		  Toast.makeText(MainActivity.this,"Auto Connect called  Do COnnect is Called", Toast.LENGTH_SHORT).show();
    	        		  doConnect();
    	        		 // recreate();
    	        	  }
    	        			
    	        	
    	        	
    	        }
    	        Log.d(TAG, "Auto Connect"+i);
          mHandler.postDelayed(mStatusChecker, mInterval);
        }
      };


    private void startRepeatingTask() {
    	 mStatusChecker.run(); 
    }

    private boolean onEnterClicked(Editable s) {
	if (_brsp != null && _brsp.getBrspState() == Brsp.BRSP_STATE_READY) {
	    if (s.length() > 0) {
		String commandString = s.append("\r").toString();
		_brsp.writeBytes(commandString.getBytes());
		TextKeyListener.clear(_txtCommand.getText());
		return true; // Keep keyboard visible
	    }
	} else {
	    Toast.makeText(MainActivity.this, "BRSP not ready", Toast.LENGTH_SHORT).show();
	}
	return false; // Hide keyboard
    }

    @Override
    protected void onDestroy() {
	Log.d(TAG, "onDestroy");
	doDisconnect();
	this.unregisterReceiver(mReceiver);
	super.onDestroy();
    }

    @Override
    protected void onStart() {
	super.onStart();
    }

    @Override
    protected void onStop() {
	super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	Log.d(TAG, "onCreateOptionsMenu");
	getMenuInflater().inflate(R.menu.main, menu);
	return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
	Log.d(TAG, "onPrepareOptionsMenu");
	if (_selectedDevice != null) {
	    MenuItem item;
	    MenuItem connectStatusItem;
	    String menuText;

	    item = menu.findItem(R.id.menu_action_connect);
	    connectStatusItem = menu.findItem(R.id.menu_action_connect_status);
	    if (_selectedDevice != null) {
		item.setVisible(true);
		
		if (_brsp != null && _brsp.getConnectionState() == BluetoothGatt.STATE_CONNECTED) {
		    item.setIcon(R.drawable.connect);
		    connectStatusItem.setIcon(R.drawable.connect);
		    item.setTitle("Disconnect");
		} else {
		    item.setIcon(R.drawable.disconnect);
		    connectStatusItem.setIcon(R.drawable.disconnect);
		    item.setTitle("Connect");
		}

	    } else {
	    	item.setVisible(false);
	    }

	    // Add item for changing brsp mode
	    item = menu.findItem(R.id.menu_action_brspmode);
	    if (_brsp != null && _brsp.getConnectionState() == BluetoothGatt.STATE_CONNECTED) {
		item.setVisible(true);
		
		
		switch (_brsp.getBrspMode()) {
		case Brsp.BRSP_MODE_DATA:
		    menuText = "Data Mode";
		    
		    break;
		case Brsp.BRSP_MODE_COMMAND:
		    menuText = "Command Mode";
		    break;
		default:
		    menuText = "";
		    // Not supported in this sample
		}
		item.setTitle(menuText);
		Toast.makeText(MainActivity.this, menuText, Toast.LENGTH_LONG).show();
	    } else {
		item.setVisible(false);
	    }
	}
	return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	if (_brsp == null)
	    return false;
	Log.d(TAG, "onOptionsItemSelected");
	Log.d(TAG, "Title selected = " + item.getTitle());
	switch (item.getItemId()) {
	case R.id.menu_action_connect:
	case R.id.menu_action_connect_status:
	    if (_brsp != null && _brsp.getConnectionState() == BluetoothGatt.STATE_DISCONNECTED){
	    	Toast.makeText(MainActivity.this, "Do Connect is called", Toast.LENGTH_LONG).show();
	    	doConnect();
	    }
		
	    else if (_brsp != null && _brsp.getConnectionState() == BluetoothGatt.STATE_CONNECTED)
		doDisconnect();
	    break;
	case R.id.menu_action_scan:
	    doScan();
	    break;
	case R.id.menu_action_brspmode:
	    if (_brsp != null)
		if (_brsp.getBrspMode() == Brsp.BRSP_MODE_DATA)
		    _brsp.setBrspMode(Brsp.BRSP_MODE_COMMAND);
		else
		    _brsp.setBrspMode(Brsp.BRSP_MODE_DATA);
	    break;
	case R.id.menu_action_exit:
	    doQuit();
	    break;
	case R.id.menu_action_clear_output:
	    _textViewOutput.setText("");
	    break;
	case R.id.menu_action_version:
	    PackageInfo pInfo;
	    try {
		pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
		String versionMsg = "Version " + pInfo.versionName;
		Toast.makeText(MainActivity.this, versionMsg, Toast.LENGTH_LONG).show();
	    } catch (NameNotFoundException e) {
		e.printStackTrace();
	    }
	    break;

	}
	return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
	Log.d(TAG, "onKeyDown keyCode:" + keyCode + " event:" + event.toString());
	if (keyCode == KeyEvent.KEYCODE_BACK && isTaskRoot()) {
	    doQuit();
	    return true;
	} else {
	    return super.onKeyDown(keyCode, event);
	}
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	Log.d(TAG, "onActivityResult requestCode:" + requestCode + " resultCode:" + resultCode);
	if (resultCode == RESULT_OK) {
	    switch (requestCode) {
	    case ScanActivity.REQUEST_SELECT_DEVICE:
		if (resultCode == RESULT_OK) {
		    _selectedDevice = data.getParcelableExtra("device");
		    setTitle(data.getStringExtra("title"));
		    invalidateOptionsMenu();
		    doDisconnect();
		    doConnect();
		    showSoftKeyboard(_txtCommand);
		}
		break;
	    default:
		Log.w(TAG, "Unknown requestCode encountered in onActivityResult.  Ignoring code:" + requestCode);
		break;
	    }

	}
	super.onActivityResult(requestCode, resultCode, data);
    }

    private void hideSoftKeyboard() {
	InputMethodManager inputMethodManager = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
	inputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
    }

    private void showSoftKeyboard(EditText field) {
	field.setFocusableInTouchMode(true);
	field.requestFocus();
	hideSoftKeyboard();
	InputMethodManager inputMethodManager = (InputMethodManager) this.getSystemService(Service.INPUT_METHOD_SERVICE);
	// Not working for some reason
	// inputMethodManager.showSoftInput(field,
	// InputMethodManager.SHOW_IMPLICIT);
	inputMethodManager.toggleSoftInput(0, 0);
    }

    private void doScan() {
			Intent i = new Intent(this, ScanActivity.class);
			startActivityForResult(i, ScanActivity.REQUEST_SELECT_DEVICE);
    }

    private void doConnect() {
	if (_selectedDevice != null && _brsp.getConnectionState() == BluetoothGatt.STATE_DISCONNECTED) {
	    boolean result = false;

	    String bondStateText = "";
	    switch (_selectedDevice.getBondState()) {
	    case BluetoothDevice.BOND_BONDED:
		bondStateText = "BOND_BONDED";
		break;
	    case BluetoothDevice.BOND_BONDING:
		bondStateText = "BOND_BONDING";
		break;
	    case BluetoothDevice.BOND_NONE:
		bondStateText = "BOND_NONE";
		break;
	    }
	    Log.d(TAG, "Bond State:" + bondStateText);

	    result = _brsp.connect(this.getApplicationContext(), _selectedDevice);
	    Log.d(TAG, "Connect result:" + result);
	}
    }

    private void doDisconnect() {
	if (_brsp != null && _brsp.getConnectionState() != BluetoothGatt.STATE_DISCONNECTED) {
	    Log.d(TAG, "Atempting to disconnect");
	    _brsp.disconnect();
	}
    }

    private void doQuit() {
	new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle(R.string.msg_quit_title)
		.setMessage(R.string.msg_quit_detail).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {
			finish();
		    }
		}).setNegativeButton(R.string.no, null).show();
    }
}
