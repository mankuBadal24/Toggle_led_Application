package com.example.connect;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.os.Bundle;
//import android.os.Handler;
//import android.os.Message;
//import android.support.v7.appAppCompatActivity;
//import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket socket;
    private ConnectedThread connectedThread;
    private boolean isConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button connectButton = findViewById(R.id.connectButton);
        Button sendOnButton = findViewById(R.id.sendOnButton);
        Button sendOffButton = findViewById(R.id.sendOffButton);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        connectButton.setOnClickListener(v -> {
            if (!isConnected) {
                connectToArduino();
            } else {
                disconnectFromArduino();
            }
        });

        sendOnButton.setOnClickListener(v -> {
            if (isConnected) {
                sendMessage("1"); // Send "1" to turn on the LED
            }
        });

        sendOffButton.setOnClickListener(v -> {
            if (isConnected) {
                sendMessage("0"); // Send "0" to turn off the LED
            }
        });
    }

    private void connectToArduino() {
        String arduinoAddress = "58:56:00:00:61:81"; // Replace with your Arduino's Bluetooth address 
        BluetoothDevice arduino = bluetoothAdapter.getRemoteDevice(arduinoAddress);

        try {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            socket = arduino.createRfcommSocketToServiceRecord(MY_UUID);
            socket.connect();
            isConnected = true;
            connectedThread = new ConnectedThread(socket);
            connectedThread.start();
            showToast("Connected to Arduino");
        } catch (IOException e) {
            showToast("Connection failed");
        }
    }

    private void disconnectFromArduino() {
        if (isConnected) {
            try {
                socket.close();
                isConnected = false;
                showToast("Disconnected from Arduino");
            } catch (IOException e) {
                showToast("Failed to disconnect");
            }
        }
    }

    private void sendMessage(String message) {
        if (isConnected) {
            connectedThread.write(message.getBytes());
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                showToast("Error creating stream");
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] mmBuffer = new byte[1024];
            int numBytes;

            while (true) {
                try {
                    numBytes = mmInStream.read(mmBuffer);
                    String receivedMessage = new String(mmBuffer, 0, numBytes);
                    // Handle received data from Arduino
                    // In this example, you can add code to respond to Arduino's data
                } catch (IOException e) {
                    break;
                }
            }
        }

        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                showToast("Error writing to Arduino");
            }
        }
    }
}
