package com.example.simplebluetooth;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    // GUI Components
    private TextView mBluetoothStatus;
    private TextView mReadBuffer;
    private Button mScanBtn;
    private Button mOffBtn;
    private Button mListPairedDevicesBtn;
    private Button mDiscoverBtn;
    private BluetoothAdapter mBTAdapter;
    private Set<BluetoothDevice> mPairedDevices;
    private ArrayAdapter<String> mBTArrayAdapter;
    private ListView mDevicesListView;
    //private CheckBox mLED1;
    private Button sendDevice;
    private Button graph;

    private Handler mHandler;
    // Our main handler that will receive callback notifications
    private ConnectedThread mConnectedThread;
    // bluetooth background worker thread to send and receive data
    private BluetoothSocket mBTSocket = null;
    // bi-directional client-to-client data path

    private static final UUID BTMODULEUUID = UUID.fromString
            ("00001101-0000-1000-8000-00805F9B34FB"); // "random" unique identifier

    // #defines for identifying shared types between calling functions
    private final static int REQUEST_ENABLE_BT = 1;
    // used to identify adding bluetooth names
    private final static int MESSAGE_READ = 2;
    // used in bluetooth handler to identify message update
    private final static int CONNECTING_STATUS = 3;
    // used in bluetooth handler to identify message status
    private String _recieveData = "";
    public int[] angle = new int[37];
    public int[] distance = new int[37];
    public String[] split = new String[2];
    public int angleint = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //初始化元件
        mBluetoothStatus = (TextView) findViewById(R.id.bluetoothStatus);
        mReadBuffer = (TextView) findViewById(R.id.readBuffer);
        mScanBtn = (Button) findViewById(R.id.scan);
        mOffBtn = (Button) findViewById(R.id.off);
        mDiscoverBtn = (Button) findViewById(R.id.discover);
        mListPairedDevicesBtn = (Button) findViewById(R.id.PairedBtn);
        //mLED1 = (CheckBox)findViewById(R.id.checkboxLED1);
        sendDevice = (Button) findViewById(R.id.send);
        graph = (Button) findViewById(R.id.graph);

        mBTArrayAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_list_item_1);
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
//        mBluetoothAdapter = bluetoothManager.getAdapter();

        mBTAdapter = bluetoothManager.getAdapter();
//        mBTAdapter = BluetoothAdapter.getDefaultAdapter();
        // get a handle on the bluetooth radio

        mDevicesListView = (ListView) findViewById(R.id.devicesListView);
        mDevicesListView.setAdapter(mBTArrayAdapter); // assign model to view
        mDevicesListView.setOnItemClickListener(mDeviceClickListener);

        // 詢問藍芽裝置權限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]
                    {Manifest.permission.ACCESS_COARSE_LOCATION}, 1);

        //定義執行緒 當收到不同的指令做對應的內容
        mHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == MESSAGE_READ) { //收到MESSAGE_READ 開始接收資料
                    String readMessage = null;
                    try {
                        readMessage = new String((byte[]) msg.obj, "UTF-8");
                        readMessage = readMessage.trim();
                        arrange(readMessage);
//                        readMessage =  readMessage.substring(0,1);
                        //取得傳過來字串的第一個字元，其餘為雜訊
                        readMessage += "\n";
                        _recieveData += readMessage; //拼湊每次收到的字元成字串
//                      //Test
//                        byte[] data = (byte[])msg.obj;
//                        readMessage = new String(data,"UTF-8");
//                        readMessage=readMessage.trim();
//                        System.out.println(readMessage);
////                        for(int i = 0 ; i< data.length ; i++){
////                            System.out.print(data[i] + ",");
////                        }
////                        System.out.println();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mReadBuffer.setText(_recieveData); //將收到的字串呈現在畫面上

                }

                if (msg.what == CONNECTING_STATUS) {
                    //收到CONNECTING_STATUS 顯示以下訊息
                    if (msg.arg1 == 1)
                        mBluetoothStatus.setText("連接上 "
                                + (String) (msg.obj));
                    else
                        mBluetoothStatus.setText("連線失敗");
                }
            }
        };

        if (mBTArrayAdapter == null) {
            // Device does not support Bluetooth
            mBluetoothStatus.setText("未找到藍芽裝置");
            Toast.makeText(getApplicationContext(), "未找到藍芽裝置", Toast.LENGTH_SHORT).show();
        } else {

           /* mLED1.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    if(mConnectedThread != null) //First check to make sure thread created
                        mConnectedThread.write(inputdata.getText().toString());
                }
            });*/

            sendDevice.setOnClickListener(new View.OnClickListener() {
                //當按下send開始傳輸資料
                @Override
                public void onClick(View v) {
                    _recieveData = ""; //清除上次收到的資料
                    mReadBuffer.setText("");
                    if (mConnectedThread != null) //First check to make sure thread created
                        mConnectedThread.write("1");
                    //傳送將輸入的資料出去
                }
            });

            //定義每個按鍵按下後要做的事情
            mScanBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bluetoothOn(v);
                }
            });

            mOffBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bluetoothOff(v);
                }
            });

            mListPairedDevicesBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listPairedDevices(v);
                }
            });

            mDiscoverBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    discover(v);
                }
            });

            graph.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    newgraph(v);
                }
            });
        }
    }

    //將收到的數值儲存到陣列裡
    private void arrange(String string) {
        try {
            split = string.split(",");
            angleint = Integer.parseInt(split[0]);
            int num=(int)(angleint / 5);
            angle[num] = (int)Integer.parseInt(split[0]);
            distance[num] = (int)Integer.parseInt(split[1]);
            Factory.x[num]=(float)(distance[num]*Math.cos(Math.toRadians(angle[num])));
            Factory.y[num]=(float)(distance[num]*Math.sin(Math.toRadians(angle[num])));
        } catch (Exception e) {
            System.out.print(e.getMessage());
        }
    }


    private void bluetoothOn(View view) {
        if (mBTAdapter == null || !mBTAdapter.isEnabled()) {//如果藍芽沒開啟
            Intent enableBtIntent = new
                    Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);//跳出視窗
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            //開啟設定藍芽畫面
            mBluetoothStatus.setText("已開啟藍芽");
            Toast.makeText(getApplicationContext(), "開啟藍芽", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "已開啟藍芽",
                    Toast.LENGTH_SHORT).show();
        }
    }

    // Enter here after user selects "yes" or "no" to enabling radio
    //定義當按下跳出是否開啟藍芽視窗後要做的內容
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent Data) {
        // Check which request we're responding to
        if (requestCode == REQUEST_ENABLE_BT) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // The user picked a contact.
                // The Intent's data Uri identifies which contact was selected.
                mBluetoothStatus.setText("已開啟藍芽");
            } else
                mBluetoothStatus.setText("藍芽已關閉");
        }
    }

    private void bluetoothOff(View view) {
        mBTAdapter.disable(); // turn off bluetooth
        mBluetoothStatus.setText("藍芽已關閉");
        Toast.makeText(getApplicationContext(), "關閉藍芽",
                Toast.LENGTH_SHORT).show();
    }

    private void discover(View view) {
        // Check if the device is already discovering
        if (mBTAdapter.isDiscovering()) { //如果已經找到裝置
            mBTAdapter.cancelDiscovery(); //取消尋找
            Toast.makeText(getApplicationContext(), "取消尋找", Toast.LENGTH_SHORT).show();
        } else {
            if (mBTAdapter.isEnabled()) { //如果沒找到裝置且已按下尋找
                mBTArrayAdapter.clear(); // clear items
                mBTAdapter.startDiscovery(); //開始尋找
                Toast.makeText(getApplicationContext(), "開始尋找",
                        Toast.LENGTH_SHORT).show();
                registerReceiver(blReceiver, new
                        IntentFilter(BluetoothDevice.ACTION_FOUND));
            } else {
                Toast.makeText(getApplicationContext(), "藍芽未開啟",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    final BroadcastReceiver blReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // add the name to the list
                mBTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                mBTArrayAdapter.notifyDataSetChanged();
            }
        }
    };

    private void listPairedDevices(View view) {
        mPairedDevices = mBTAdapter.getBondedDevices();
        if (mBTAdapter.isEnabled()) {
            // put it's one to the adapter
            for (BluetoothDevice device : mPairedDevices)
                mBTArrayAdapter.add(device.getName() + "\n" + device.getAddress());

            Toast.makeText(getApplicationContext(), "顯示配對裝置",
                    Toast.LENGTH_SHORT).show();
        } else
            Toast.makeText(getApplicationContext(), "藍芽未開啟",
                    Toast.LENGTH_SHORT).show();
    }

    private void newgraph(View view) {
        startActivity(new Intent(MainActivity.this,Graph.class));
    }

    private AdapterView.OnItemClickListener mDeviceClickListener = new
            AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {

                    if (!mBTAdapter.isEnabled()) {
                        Toast.makeText(getBaseContext(), "藍芽未開啟",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    mBluetoothStatus.setText("連線中...");
                    // Get the device MAC address, which is the last 17 chars in the View
                    String info = ((TextView) v).getText().toString();
                    final String address = info.substring(info.length() - 17);
                    final String name = info.substring(0, info.length() - 17);

                    // Spawn a new thread to avoid blocking the GUI one
                    new Thread() {
                        public void run() {
                            boolean fail = false;
                            //取得裝置MAC找到連接的藍芽裝置
                            BluetoothDevice device = mBTAdapter.getRemoteDevice(address);

                            try {
                                mBTSocket = createBluetoothSocket(device);
                                //建立藍芽socket
                            } catch (IOException e) {
                                fail = true;
                                Toast.makeText(getBaseContext(), "連線建立失敗",
                                        Toast.LENGTH_SHORT).show();
                            }
                            // Establish the Bluetooth socket connection.
                            try {
                                mBTSocket.connect(); //建立藍芽連線
                            } catch (IOException e) {
                                try {
                                    fail = true;
                                    mBTSocket.close(); //關閉socket
                                    //開啟執行緒 顯示訊息
                                    mHandler.obtainMessage(CONNECTING_STATUS, -1, -1)
                                            .sendToTarget();
                                } catch (IOException e2) {
                                    //insert code to deal with this
                                    Toast.makeText(getBaseContext(), "連線建立失敗",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                            if (fail == false) {
                                //開啟執行緒用於傳輸及接收資料
                                mConnectedThread = new ConnectedThread(mBTSocket);
                                mConnectedThread.start();
                                //開啟新執行緒顯示連接裝置名稱
                                mHandler.obtainMessage(CONNECTING_STATUS, 1, -1, name)
                                        .sendToTarget();
                            }
                        }
                    }.start();
                }
            };

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws
            IOException {
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connection with BT device using UUID
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        //        public void run() {
//            int bytes;
//            byte[] buffer = new byte[1024];
//            String end = "\n";
//            StringBuilder curMsg = new StringBuilder();
//
//            while (true){
//                try {
//                    while (-1 != (bytes = mmInStream.read(buffer))) {
//                        curMsg.append(new String(buffer, 0, bytes, Charset.forName("UTF-8")));
//                        int endIdx = curMsg.indexOf(end);
//                        if (endIdx != -1) {
//                            String fullMessage = curMsg.substring(0, endIdx + end.length());
//                            curMsg.delete(0, endIdx + end.length());
//
//                            // Now send fullMessage
//                            // Send the obtained bytes to the UI Activity
//                            mHandler.obtainMessage(MESSAGE_READ, bytes, -1, fullMessage)
//                                    .sendToTarget();
//                        }
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//
//        }
        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.available();
                    if (bytes != 0) {
                        SystemClock.sleep(100);
                        //pause and wait for rest of data
                        bytes = mmInStream.available();
                        // how many bytes are ready to be read?
                        bytes = mmInStream.read(buffer, 0, bytes);
                        // record how many bytes we actually read
                        mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                                .sendToTarget(); // Send the obtained bytes to the UI activity
                    }
                } catch (IOException e) {
                    e.printStackTrace();

                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String input) {
            byte[] bytes = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }
}
