package com.tannuo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class BootBroadReceiver extends BroadcastReceiver {
    public BootBroadReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            Log.e("BootBroadReceiver", "test");
            context.startService(new Intent(context, MyService.class));
            //context.startActivity(new Intent(context, MainActivity.class));
            Toast.makeText(context, "Reboot test receiver", Toast.LENGTH_LONG).show();
        } else if (action.equals("android.hardware.usb.action.USB_DEVICE_ATTACHED")) {
//            UsbDevice hidDevice = new UsbDevice(context);
//            hidDevice.start();
        }
    }
}
