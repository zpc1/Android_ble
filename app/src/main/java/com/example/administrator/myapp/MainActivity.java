package com.example.administrator.myapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mbluetoothGatt;
    BluetoothGattCharacteristic characteristic;
    private Handler mHandler;
    private boolean mScanning;
    private String mDeviceName = "";
    private static final int REQUEST_ENABLE_BT = 1;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHandler = new Handler();

        // 初始化 Bluetooth adapter, 通过蓝牙管理器得到一个参考蓝牙适配器(API必须在以上android4.3或以上和版本)
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        System.out.println("oncreat");

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
        System.out.println("onResume");


        scanLeDevice(true);

    }

    // stop scan after 10 second
    private static final long SCAN_PERIOD = 10000;
    private void scanLeDevice(final boolean enable) {
        if (enable) {

            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;

                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        invalidateOptionsMenu();
    }

    private String TAG = MainActivity.class.getSimpleName();
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (bluetoothDevice.getName() == null || bluetoothDevice == null)
                        return;
                    String name = bluetoothDevice.getName();
                    Log.d(TAG, "run:sring " + name);
                    if ((bluetoothDevice.getName()).equals("chexiaona")){

                        Log.d("haha", "run:soushuochenggong");
                        scanLeDevice(false);

                        mbluetoothGatt = bluetoothDevice.connectGatt(MainActivity.this, false, mGattCallback);

                    }
                }
            });

        }
    };

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                            int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {

                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:"
                        + mbluetoothGatt.discoverServices());

            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.e(TAG, "成功发现服务");

                List<BluetoothGattService> supportedGattServices =mbluetoothGatt.getServices();

                for(int i=0;i<supportedGattServices.size();i++){

                    Log.e("AAAAA","1:BluetoothGattService UUID=:"+
                            supportedGattServices.get(i).getUuid());

                    BluetoothGattService service = mbluetoothGatt.getService(UUID.fromString(supportedGattServices.get(i).getUuid().toString()));

                    List<BluetoothGattCharacteristic> listGattCharacteristic=
                            supportedGattServices.get(i).getCharacteristics();
                    for(int j=0;j<listGattCharacteristic.size();j++){
                        Log.e("a","2:   BluetoothGattCharacteristic UUID=:"
                                +listGattCharacteristic.get(j).getUuid());

                        if (service != null) {
                            characteristic = service.getCharacteristic(UUID.fromString(listGattCharacteristic.get(j).getUuid().toString()));
                            if (characteristic != null) {
                                gatt.setCharacteristicNotification(characteristic, true);

                            }
                        }
                    }
                }

            }else{
                Log.e(TAG, "服务发现失败，错误码为:" + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {

//                byte[] data = characteristic.getValue();
//                for (int i=0; i<data.length;i++){
//                    System.out.println(data[i]);
//
//                }


//                String s = byte2HexStr(data);


//                Log.e(TAG, "读取成功" + characteristic.getValue());
//                Log.e(TAG, "读取成功" + str2HexStr( characteristic.getValue().toString()));
            }else {
                Log.e(TAG, "读出: " + status);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.e(TAG, "写入成功" + characteristic.getValue());
            }else {
                Log.e(TAG, "写入: " + status);
            }
        };

        /*
         * when connected successfully will callback this method
         * this method can dealwith send password or data analyze
         *
         * */

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
//           mbluetoothGatt.readCharacteristic(characteristic);
            System.out.println(characteristic.getValue());
        }


        @Override
        public void onDescriptorWrite(BluetoothGatt gatt,
                                      BluetoothGattDescriptor descriptor, int status) {
            UUID uuid = descriptor.getCharacteristic().getUuid();
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            System.out.println("rssi = " + rssi);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        if (!mScanning) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);

        } else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);

        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:
                scanLeDevice(true);
                break;
            case R.id.menu_stop:
                scanLeDevice(false);
                break;
        }
        return true;
    }

    /**
     * byte转16进制
     *
     * @param b
     * @return
     */
    public static String byte2HexStr(byte[] b) {

        String stmp = "";
        StringBuilder sb = new StringBuilder("");
        for (int n = 0; n < b.length; n++) {
            stmp = Integer.toHexString(b[n] & 0xFF);
            sb.append((stmp.length() == 1) ? "0" + stmp : stmp);
            sb.append(" ");
        }
        return sb.toString().toUpperCase().trim();
    }

    public static String str2HexStr(String str) {
        char[] chars = "0123456789ABCDEF".toCharArray();
        StringBuilder sb = new StringBuilder("");
        byte[] bs = str.getBytes();
        int bit;
        for (int i = 0; i < bs.length; i++) {
            bit = (bs[i] & 0x0f0) >> 4;
            sb.append(chars[bit]);
            bit = bs[i] & 0x0f;
            sb.append(chars[bit]);
        }
        return sb.toString();

    }
}
