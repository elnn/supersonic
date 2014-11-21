package com.younha.supersonic;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;


public class AudioReader
{
    public static abstract class Listener
    {
        public static final int ERR_OK = 0;
        public static final int ERR_INIT_FAILED = 1;
        public static final int ERR_READ_FAILED = 2;
        public abstract void onReadComplete(int decibel);
        public abstract void onReadError(int error);
    }
     
    public int calculatePowerDb(short[] sdata, int off, int samples)
    {
        double sum = 0;
        double sqsum = 0;
        for (int i = 0; i < samples; i++)
        {
            final long v = sdata[off + i];
            sum += v;
            sqsum += v * v;
        }
        double power = (sqsum - sum * sum / samples) / samples;
         
        power /= MAX_16_BIT * MAX_16_BIT;
         
        double result = Math.log10(power) * 10f + FUDGE;
        return (int)result;
    }
     
    public AudioReader()
    {
    }
     
    public void startReader(int rate, int block, Listener listener)
    {
        Log.i(TAG, "Reader: Start Thread");
        synchronized (this)
        {
            int audioBuf = AudioRecord.getMinBufferSize(rate, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT) * 2;
             
            audioInput = new AudioRecord(MediaRecorder.AudioSource.MIC, rate, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, audioBuf);
            inputBlockSize = block;
            sleepTime = (long) (1000f / ((float) rate / (float) block));
            inputBuffer = new short[2][inputBlockSize];
            inputBufferWhich = 0;
            inputBufferIndex = 0;
            inputListener = listener;
            running = true;
            readerThread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    readerRun();
                }
            }, "Audio Reader");
            readerThread.start();
        }
    }
     
    public void stopReader()
    {
        Log.i(TAG, "Reader: Signal Stop");
        synchronized (this)
        {
            running = false;
        }
        try
        {
            if (readerThread != null)
                readerThread.join();
        } catch (InterruptedException e)
        {
            ;
        }
        readerThread = null;
         
        // Kill the audio input.
        synchronized (this)
        {
            if (audioInput != null)
            {
                audioInput.release();
                audioInput = null;
            }
        }
         
        Log.i(TAG, "Reader: Thread Stopped");
    }
     
    private void readerRun()
    {
        short[] buffer;
        int index, readSize;
         
        int timeout = 200;
        try
        {
            while (timeout > 0 && audioInput.getState() != AudioRecord.STATE_INITIALIZED)
            {
                Thread.sleep(50);
                timeout -= 50;
            }
        } catch (InterruptedException e)
        {
        }
         
        if (audioInput.getState() != AudioRecord.STATE_INITIALIZED)
        {
            Log.e(TAG, "Audio reader failed to initialize");
            readError(Listener.ERR_INIT_FAILED);
            running = false;
            return;
        }
         
        try
        {
            Log.i(TAG, "Reader: Start Recording");
            audioInput.startRecording();
            while (running)
            {
                long stime = System.currentTimeMillis();
                 
                if (!running)
                    break;
                 
                readSize = inputBlockSize;
                int space = inputBlockSize - inputBufferIndex;
                if (readSize > space)
                    readSize = space;
                buffer = inputBuffer[inputBufferWhich];
                index = inputBufferIndex;
                 
                synchronized (buffer)
                {
                    int nread = audioInput.read(buffer, index, readSize);
                     
                    boolean done = false;
                    if (!running)
                        break;
                     
                    if (nread < 0)
                    {
                        Log.e(TAG, "Audio read failed: error " + nread);
                        readError(Listener.ERR_READ_FAILED);
                        running = false;
                        break;
                    }
                    int end = inputBufferIndex + nread;
                    if (end >= inputBlockSize)
                    {
                        inputBufferWhich = (inputBufferWhich + 1) % 2;
                        inputBufferIndex = 0;
                        done = true;
                    }
                    else
                        inputBufferIndex = end;
                     
                    if (done)
                    {
                        readDone(buffer);
                         
                        long etime = System.currentTimeMillis();
                        long sleep = sleepTime - (etime - stime);
                        if (sleep < 5)
                            sleep = 5;
                        try
                        {
                            buffer.wait(sleep);
                        } catch (InterruptedException e)
                        {
                        }
                    }
                }
            }
        } finally
        {
            Log.i(TAG, "Reader: Stop Recording");
            if (audioInput.getState() == AudioRecord.RECORDSTATE_RECORDING)
                audioInput.stop();
        }
    }
     
    private void readDone(short[] buffer)
    {
        synchronized (this)
        {
            audioData = buffer;
            ++audioSequence;
             
            short[] buffer2 = null;
            if (audioData != null && audioSequence > audioProcessed)
            {
                audioProcessed = audioSequence;
                buffer2 = audioData;
            }
             
            if (buffer2 != null)
            {
                final int len = buffer2.length;
                
                inputListener.onReadComplete(calculatePowerDb(buffer2, 0, len));
                buffer2.notify();
            }
        }
    }
     
    private void readError(int code)
    {
        inputListener.onReadError(code);
    }
     
    private static final String TAG = "WindMeter";
    private AudioRecord audioInput;
    private short[][] inputBuffer = null;
    private int inputBufferWhich = 0;
    private int inputBufferIndex = 0;
    private int inputBlockSize = 0;
    private long sleepTime = 0;
    private Listener inputListener = null;
    private boolean running = false;
    private Thread readerThread = null;
    private short[] audioData;
    private long audioSequence = 0;
    private long audioProcessed = 0;
    private static final float MAX_16_BIT = 32768;
    private static final float FUDGE = 0.6f;
}
