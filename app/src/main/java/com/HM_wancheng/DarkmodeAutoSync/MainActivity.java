package com.HM_wancheng.DarkmodeAutoSync;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket socket;
    private BluetoothDevice device;
    private OutputStream outputStream;
    private static final UUID RFCOMM_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "设备不支持蓝牙", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
        }

        registerReceiver(bluetoothReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));

        // 启动查找蓝牙设备
        bluetoothAdapter.startDiscovery();

        // 检测当前夜间模式
        int nightMode = getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
        String mode = (nightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES) ? "dark" : "light";
        sendBluetoothCommand(mode);
    }

    private void sendBluetoothCommand(String mode) {
        if (device != null) {
            new Thread(() -> {
                try {
                    socket = device.createRfcommSocketToServiceRecord(RFCOMM_UUID);
                    socket.connect();
                    outputStream = socket.getOutputStream();
                    outputStream.write(mode.getBytes());
                    outputStream.flush();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice newDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (newDevice.getName().equals("YOUR_COMPUTER_NAME")) {
                    device = newDevice;
                    Toast.makeText(MainActivity.this, "设备已连接", Toast.LENGTH_SHORT).show();
                    bluetoothAdapter.cancelDiscovery();
                    sendBluetoothCommand("light"); // 默认发送 light 模式
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(bluetoothReceiver);
    }
}
