package cn.edu.nju.zc_gesture_app;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import android.provider.MediaStore;
import android.renderscript.Sampler;
import android.util.Log;
import java.io.OutputStreamWriter;
import org.jtransforms.fft.DoubleFFT_1D;


public class SoundPlayer {
    private AudioTrack audioTrack;

    private int sampleRate = 48000;
    private int numSamples= 1920;
    private int fftsize = 1024;
    private int numFrames = 2;
    private int maxNumSamples=19200;
    private double sample[] = new double[maxNumSamples];
    private double fftbuf[] = new double[2*fftsize];
    private byte generatedSound[] = new byte[2 * maxNumSamples];

    private int startpoint = 18750*1024/48000;
    private int zc_l =127;
    private int zc_half = (zc_l-1)/2;

    private double zc_r[]={-0.278743,1.113266,-2.488267,4.344796,-6.533468,8.760913,-10.552230,11.268566,
            -10.229873,6.979571,-1.666493,-4.600661,9.712818,-11.165287,7.408593,0.557315,
            -8.582904,11.124064,-5.350746,-5.103790,11.227216,-6.304352,-5.594429,11.199678,
            -2.759369,-9.568489,8.211242,5.834688,-10.451150,-3.028782,11.021228,2.215642,
            -10.959677,-3.561886,10.109810,6.758587,-7.196283,-10.343676,0.835546,10.734960,
            7.616369,-3.825250,-11.076035,-7.819485,1.941662,9.851203,10.646852,4.853710,
            -3.296343,-9.418305,-11.247885,-8.933562,-4.086273,1.390305,6.071377,9.262359,
            10.891421,11.261671,10.816500,9.983561,9.100745,8.399643,8.017816,8.017816,
            8.399643,9.100745,9.983561,10.816500,11.261671,10.891421,9.262359,6.071377,
            1.390305,-4.086273,-8.933562,-11.247885,-9.418305,-3.296343,4.853710,10.646852,
            9.851203,1.941662,-7.819485,-11.076035,-3.825250,7.616369,10.734960,0.835546,
            -10.343676,-7.196283,6.758587,10.109810,-3.561886,-10.959677,2.215642,11.021228,
            -3.028782,-10.451150,5.834688,8.211242,-9.568489,-2.759369,11.199678,-5.594429,
            -6.304352,11.227216,-5.103790,-5.350746,11.124064,-8.582904,0.557315,7.408593,
            -11.165287,9.712818,-4.600661,-1.666493,6.979571,-10.229873,11.268566,-10.552230,
            8.760913,-6.533468,4.344796,-2.488267,1.113266,-0.278743,0.000000};
    private double zc_i_n[]={-11.265980,11.214305,-10.991293,10.398209,-9.182254,7.088469,-3.956064,-0.139382,
            4.727547,-8.847915,11.145528,-10.287561,5.714996,1.528516,-8.491923,11.255639,
            -7.302997,-1.804216,9.918141,-10.047454,0.974481,9.341047,-9.782759,-1.251882,
            10.926385,-5.953488,-7.718517,9.641391,4.215857,-10.854791,-2.352134,11.049476,
            2.624018,-10.691724,-4.979131,9.017843,8.672572,-4.473071,-11.238410,-3.429377,
            8.306077,10.600352,2.078811,-8.115150,-11.100899,-5.473006,3.693851,10.170619,
            10.776554,6.188338,-0.696484,-6.869604,-10.502493,-11.183338,-9.494123,-6.419401,
            -2.894297,0.418061,3.162805,5.227668,6.646536,7.513056,7.919256,7.919256,
            7.513056,6.646536,5.227668,3.162805,0.418061,-2.894297,-6.419401,-9.494123,
            -11.183338,-10.502493,-6.869604,-0.696484,6.188338,10.776554,10.170619,3.693851,
            -5.473006,-11.100899,-8.115150,2.078811,10.600352,8.306077,-3.429377,-11.238410,
            -4.473071,8.672572,9.017843,-4.979131,-10.691724,2.624018,11.049476,-2.352134,
            -10.854791,4.215857,9.641391,-7.718517,-5.953488,10.926385,-1.251882,-9.782759,
            9.341047,0.974481,-10.047454,9.918141,-1.804216,-7.302997,11.255639,-8.491923,
            1.528516,5.714996,-10.287561,11.145528,-8.847915,4.727547,-0.139382,-3.956064,
            7.088469,-9.182254,10.398209,-10.991293,11.214305,-11.265980,11.269428};


    SoundPlayer(int setsamplerate, int playbufsize) {


        sampleRate= setsamplerate;
        numSamples = playbufsize;

        numFrames = numSamples / fftsize;
        //STREAM_MUSIC  STREAM_VOICE_CALL
        audioTrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL, sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, numSamples, AudioTrack.MODE_STATIC);

        PrepareSound();
    }

    public void PrepareSound() {

        DoubleFFT_1D fftcal= new DoubleFFT_1D(fftsize);


        for (int i=0;i<zc_l;i++)
        {
            fftbuf[ (startpoint-zc_half+i)*2] = zc_r[i];  //set real and imag for zc
            fftbuf[ (startpoint-zc_half+i)*2+1] = -zc_i_n[i];
        }

        for(int i=1;i<fftsize/2;i++)
        {
            fftbuf[ (fftsize-i)*2] = fftbuf[i*2];
            fftbuf[ (fftsize-i)*2+1] = -fftbuf[i*2+1];  //imag reflection
        }

        fftcal.complexInverse(fftbuf,true);

        double maxval=0;
        for(int i=0; i< fftsize;i++)
        {
            sample[i]=fftbuf[i*2];
            if(Math.abs(sample[i])>maxval) {
                maxval = Math.abs(sample[i]);
            }
        }

        for(int i=0; i< fftsize;i++)
        {
            sample[i]=sample[i]/maxval;
        }

        for(int j = 1; j<numFrames;j++)
        {
            for(int i=0;i<fftsize;i++)
            {
                sample[j*fftsize+i]=sample[i];
            }
        }

        //for (int i = 0; i < numSamples; ++i) {
        //    sample[i] =Math.cos(2 * Math.PI * i / (sampleRate /1000));
        //}
        // convert to 16 bit pcm sound array
        // assumes the sample buffer is normalised.
        int idx = 0;
        for (final double dVal : sample) {
            // scale to maximum amplitude
            final short val = (short) ((dVal * 30000));
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSound[idx++] = (byte) (val & 0x00ff);
            generatedSound[idx++] = (byte) ((val & 0xff00) >>> 8);

        }


        Log.d("gesture", "" +audioTrack.write(generatedSound, 0, numSamples));
        audioTrack.setLoopPoints(0, numSamples/2,-1);
    }

    public void play() {
        //16 bit because it's supported by all phones
        audioTrack.play();
    }
    public void pause() {
        audioTrack.pause();
    }

    public void stop() {
        audioTrack.stop();
        audioTrack.release();
    }
}
