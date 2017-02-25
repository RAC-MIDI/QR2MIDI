package com.rijesh.qr2midi;

/**
 * Created by Rijesh on 2/24/2017.
 */

import android.util.Log;

import org.billthefarmer.mididriver.MidiDriver;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MidiPlayer  implements MidiDriver.OnMidiStartListener{
    protected MidiDriver midi;
    ExecutorService executor = Executors.newFixedThreadPool(1);

    public MidiPlayer(){
        midi = new MidiDriver();
        midi.setOnMidiStartListener(this);
        midi.start();
        //midi.write(new byte[]{(byte)0xc1, (byte) 111});
    }

    public void addToQueue(final byte[] b) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                final Byte QR_num = b[0];
                for(int ind=9;ind < b.length ; ind+=9){ // Each line is 9 bytes and each message occupies the first 7 bytes
                    final int i = ind;

                    executor.execute(new Runnable() {
                        public void run() {
                            try {
                                Thread.sleep(((0xFF & b[i])  | ((0xFF & b[i+1]) << 8) |
                                        ((0xFF & b[i+2]) << 16) | (0xFF & b[i+3] << 24))); // First four bytes are delta time between messages
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            };
                            midi.write(new byte[] {b[i+4], b[i+5], b[i+6] }); // Next three bytes are the actual message.
                            if (i == 9) {
                                Log.i("myTag", "Started Playing QR " + QR_num.toString());
                            }
                            else if (i >= b.length - 10){
                                Log.i("myTag", "finished playing QR " + QR_num.toString());
                            }
                        }
                    });
                }
                Log.i("myTag", "finished adding QR " + QR_num.toString() + " to Queue");
            }
        };
        thread.start();
    }

    public void pause() {
        executor.shutdownNow();
        midi.stop();
    }

    public void resume() {
        midi.start();
    }

    public void playCNote(){
        sendMidi(0x90, 48, 63);
        sendMidi(0x90, 52, 63);
        sendMidi(0x90, 55, 63);

        sendMidi((int)0x90 +1, 48, 63);
        sendMidi((int)0x90 +1, 52, 63);
        sendMidi((int)0x90 +1, 55, 63);
    }

    public void onMidiStart()
    {
        // Program change - harpsicord
        //sendMidi(0xc0, 1);
    }

    protected void sendMidi(int m, int n, int v)
    {
        midi.write(new byte[]{(byte) m, (byte) n, (byte) v});
    }
}
