package cn.edu.nju.zc_gesture_app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;


import org.jtransforms.fft.DoubleFFT_1D;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;

import org.pytorch.IValue;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;


public class MainActivity extends AppCompatActivity {
    private Button btnPlayRecord;

    private Button btnStopRecord;

    private TextView status;

    private TextView totalCount;

    private TextView upCount;

    private TextView downCount;

    private TextView clickCount;

    private TextView grabCount;

    private AudioManager audioManager;

    private boolean sendDatatoPython = true;

    private boolean logenabled = true;

    private Socket dataSocket;

    private String systemName = "ZC_gesture";

    private String IP_address= "192.168.1.102";

    private boolean btnPlay = false;

    private int recBufSize = 1024;

    private int readBufSize = 1920;

    private int fftsize = 1024;

    private int frameSize = 1024;

    private int sampleRateInHz = 48000;

    private int channelConfig = AudioFormat.CHANNEL_IN_STEREO;

    private int encodingBitrate = AudioFormat.ENCODING_PCM_16BIT;

    private int playBufSize = 0;

    private boolean signalRecord = false;

    private boolean signalPlay = false;


    private OutputStream datastream;

    private AudioRecord audioRecord;

    private int startpoint = 18750*1024/48000;  // zc载波中心频率

    private int zc_length = 127;

    private int zc_half_length = (zc_length - 1)/2;

    private int svmStatus = 0;

    private svm_model svmModel;

    private svm_node feature_svm[] = new svm_node[360];

    private int total_count = 0;

    private int up_count = 0;

    private int down_count = 0;

    private int click_count = 0;

    private int grab_count = 0;

    private int seq_length = 48;

    private int numChannels = 2;

    private int path_length = 256;

    private int numHistory = 100;

    private boolean timestamp = true;

    private double zc_real[]={-0.278743,1.113266,-2.488267,4.344796,-6.533468,8.760913,-10.552230,11.268566,
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
    private double zc_imag[]={-11.265980,11.214305,-10.991293,10.398209,-9.182254,7.088469,-3.956064,-0.139382,
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

    private double seq[][][] = new double[numChannels][path_length][seq_length];
    float[] data = new float [numChannels*path_length*seq_length];

    private int history[] = new int[numHistory];

    private int seq_index = 0;

    private int history_index = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        btnPlayRecord = findViewById(R.id.btnplayrecord);
        btnStopRecord = findViewById(R.id.btnstoprecord);
        status = findViewById(R.id.state_text);
        totalCount = findViewById(R.id.count_text);
        upCount = findViewById(R.id.up_text);
        downCount = findViewById(R.id.down_text);
        clickCount = findViewById(R.id.click_text);
        grabCount = findViewById(R.id.grab_text);

        for (int i=0;i<360;i++) feature_svm[i] = new svm_node();

        AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
        mylog("maxVolume: " + maxVolume);

        mAudioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,maxVolume,0); //固定音量为10




        btnPlayRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnPlayRecord.setEnabled(false);

                btnStopRecord.setEnabled(true);

                recBufSize = AudioRecord.getMinBufferSize(sampleRateInHz,
                        channelConfig, encodingBitrate);
                mylog("recBufSize: " + recBufSize);
                //playBufSize = AudioTrack.getMinBufferSize(sampleRateInHz,
                        //channelConfig, encodingBitrate);
                //mylog("recBufSize: " + playBufSize);

                playBufSize = 10*fftsize;         //放音的zc长度，必须是zc序列的偶数倍周期
                mylog("playBufSize: "+ playBufSize);

                audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                        sampleRateInHz, channelConfig, encodingBitrate, recBufSize);



//                Log.i("zc_gesture","channels:" + audioRecord.getChannelConfiguration());
                new ThreadInstantPlay().start();

                new ThreadInstantRecord().start();

                new ThreadInference().start();

                //new ThreadSocket().start();

                //loadSVMModal();

            }
        });



        btnStopRecord.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                btnPlayRecord.setEnabled(true);

                btnStopRecord.setEnabled(false);

                btnPlay=false;
                try{
                    datastream.close();

                    dataSocket.close();
                }catch (Exception e) {
                    mylog(e+"");
                }
            }
        });
    }

    /* 输出结果展示 */
    private Handler uiHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case 0:
                    status.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
                    status.setText("0");
                    break;
                case 1:
                    status.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
                    status.setText("1");
                    break;
                case 2:
                    status.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
                    status.setText("2");
                    break;
                case 3:
                    status.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
                    status.setText("3");
                    break;
                case 4:
                    status.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
                    status.setText("4");
                    break;
                case 6:
                    status.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
                    status.setText("5");
                    total_count += 1;
                    up_count += 1;
                    totalCount.setText("手势识别总次数："+ total_count +" 次");
                    upCount.setText("向上翻页："+ up_count+" 次");
                    break;
                case 7:
                    status.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_dark));
                    status.setText("6");
                    total_count += 1;
                    down_count += 1;
                    totalCount.setText("手势识别总次数："+ total_count +" 次");
                    downCount.setText("向下翻页："+ down_count+" 次");
                    break;
                case 8:
                    status.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_light));
                    status.setText("7");
                    total_count += 1;
                    click_count += 1;
                    totalCount.setText("手势识别总次数："+ total_count +" 次");
                    clickCount.setText("双击："+ click_count+" 次");
                    break;
                case 9:
                    status.setBackgroundColor(getResources().getColor(android.R.color.holo_purple));
                    status.setText("8");
                    total_count += 1;
                    grab_count += 1;
                    totalCount.setText("手势识别总次数："+ total_count +" 次");
                    grabCount.setText("截屏："+ grab_count+" 次");
                    break;
            }
        }
    };

    /*放音线程*/
    class ThreadInstantPlay extends Thread{
        @Override
        public void run(){
            SoundPlayer Player = new SoundPlayer(sampleRateInHz,playBufSize);

            btnPlay = true;
            signalRecord = true;
            while(signalRecord & signalPlay == false){}

            Player.play();
            mylog("playing time is : "+System.nanoTime());
            while(btnPlay == true){}

            Player.stop();
        }
    }

    class ThreadInference extends Thread{
        @Override
        public void run(){
            Module module = null;
            String filepath_module = "/storage/emulated/0/storage/emulated/0/zc_gesture/saa_pred_lite.pt";

            module = Module.load(filepath_module);
            mylog("model has been loaded!");

            while(btnPlay){
                if(seq_index == seq_length-1){
                    mylog("Start inference at "+System.currentTimeMillis());
                    long[] shape = {1,numChannels,path_length,seq_length};

                    Tensor inputTensor = Tensor.fromBlob(data,shape);

                    mylog(inputTensor.toString());

                    Tensor outputTensor = module.forward(IValue.from(inputTensor)).toTensor();

                    mylog("Finish inference at "+System.currentTimeMillis());
                    float[] result = outputTensor.getDataAsFloatArray();

                    mylog(Arrays.toString(result));

                    float max = 0;
                    int id = 0;
                    for(int i = 0;i<result.length;i++){
                        if(result[i] >= max){
                            max = result[i];
                            id = i;
                        }
                    }

                    Message msg = new Message();
                    msg.what = id;
                    uiHandler.sendMessage(msg);

                }
            }

        }
    }

    /*录音线程 包括状态转移、特征提取、SVM分类*/
    class ThreadInstantRecord extends Thread{
        @Override
        public void run(){
            short[] bsRecord = new short[recBufSize * 2];   //录音缓存buffer

            byte[] networkbuf = new byte[recBufSize * 16];  //网络socket传输buffer

            double fftbuf[] = new double[2*fftsize];
            double fftbuf2[] = new double[2*fftsize];
            double cirChannel1[]= new double[2*fftsize];    //信道1 CIR
            double cirChannel2[]= new double[2*fftsize];    //信道2 CIR
            double cirDiff1[] = new double[2*fftsize];      //信道1 CIR diff
            double cirDiff2[] = new double[2*fftsize];      //信道2 CIR diff
            double cirReal1[] = new double[fftsize];
            double cirReal2[] = new double[fftsize];
            double cirDiffReal1[] = new double[fftsize];      //信道1 CIR real diff
            double cirDiffReal2[] = new double[fftsize];      //信道2 CIR real diff
            double lastChannel1[] = new double[2*fftsize];
            double lastChannel2[] = new double[2*fftsize];
            double lastChannelReal1[] = new double[fftsize];
            double lastChannelReal2[] = new double[fftsize];
            int hashlist1[] = new int[fftsize];             //信道1 主峰index投票buffer
            int hashlist2[] = new int[fftsize];             //信道2 主峰index投票buffer
            double last_var[] = new double[2];
            int max_index[] = new int[2];                   //当前帧主峰index
            int last_max_index[] = new int[2];
//            double max_var_list[] = new double[5];          //信道1 连续5帧方差缓存
//            double dec_value[] = new double[6];
            double curDisAxisFea[][] = new double[2][15];   //特征缓存 maxpooling 150*1 -> 15*1
//            double disAxisFeatureBuf[][][] = new double[2][5][15];  //动作开始前5帧特征缓存 15*count(count<=5)
//            double maxPoolingFea[][][] = new double[2][12][15];     //单个动作样本特征缓存  150*72 -> 15*12
//            double feaMax[] = new double[2];                //特征最大值，用于特征归一化
//            double feaMin[] = new double[2];                //特征最小值，用于特征归一化
//            double timeSum[][] = new double[2][12];         //单个样本不同时间的能量分布
//            double disSum[][] = new double[2][15];          //单个样本不同时延(距离)的能量分布
//            double newFea[][][] = new double[2][12][15];    //归一化之后的特征缓存

            int maxindex1= 0, maxindex2=0;
            int datacount = 0;
            int time_count = 0;
            int index1, index2;
            int status = 0;
            int count = 0;

            while (btnPlay == false) {}
            signalPlay = true;
            while(signalRecord & signalPlay == false){}
            long ts = System.currentTimeMillis();
            audioRecord.startRecording();


            mylog("record started" );

            DoubleFFT_1D fftcal= new DoubleFFT_1D(fftsize);

            String filepath = "/storage/emulated/0/storage/emulated/0/zc_gesture/sample.txt";  //特征保存路径，可以adb pull导出作为训练集

            String filepath2 = "/storage/emulated/0/storage/emulated/0/zc_gesture/sample_raw.txt"; //原始数据保存路径

            try {
                FileWriter writer=new FileWriter(filepath);
                FileWriter writerRaw=new FileWriter(filepath2);
            while(btnPlay){

                int line = audioRecord.read(bsRecord, 0, frameSize * 2);  //framesize = 1024
                datacount = datacount + line / 2;

                /*单帧（1024个点）处理循环*/
                if(line >= frameSize){

                    /*信道1 获取CIR*/
                    for(int i=0; i< fftsize;i++)
                    {
                        fftbuf[i*2]= bsRecord[i*2];
                        fftbuf[i*2+1] =0;
                    }
                    fftcal.complexForward(fftbuf);
                    for(int i=0;i<fftsize*2;i++)
                        fftbuf2[i]=0;

                    for(int i=0;i<zc_length;i++)
                    {
                        index1= i-zc_half_length;
                        if(index1<0)
                        {
                            index1=index1+fftsize;
                        }
                        index2= startpoint + i - zc_half_length;
                        fftbuf2[index1*2] = fftbuf[index2*2]*zc_real[i]-fftbuf[index2*2+1]*zc_imag[i];
                        fftbuf2[index1*2+1] = fftbuf[index2*2]*zc_imag[i]+fftbuf[index2*2+1]*zc_real[i];
                    }
                    fftcal.complexInverse(fftbuf2,true);
                    for(int i=0; i<fftsize*2;i++)
                        cirChannel1[i]=fftbuf2[i];

                    /*信道2 获取CIR*/
                    for(int i=0; i< fftsize;i++)
                    {
                        fftbuf[i*2]= bsRecord[i*2+1];   //两个channel是交叉存储，2i的位置是channel1，2i+1的位置是channel2
                        fftbuf[i*2+1] =0;
                    }
                    fftcal.complexForward(fftbuf);      //complexForward 其中2i的位置是实部，2i+1的位置是虚部
                    for(int i=0;i<fftsize*2;i++)
                        fftbuf2[i]=0;
                    for(int i=0;i<zc_length;i++)
                    {
                        index1= i-zc_half_length;
                        if(index1<0)
                        {
                            index1=index1+fftsize;
                        }
                        index2= startpoint + i - zc_half_length;
                        fftbuf2[index1*2] = fftbuf[index2*2]*zc_real[i]-fftbuf[index2*2+1]*zc_imag[i];
                        fftbuf2[index1*2+1] = fftbuf[index2*2]*zc_imag[i]+fftbuf[index2*2+1]*zc_real[i];
                    }
                    fftcal.complexInverse(fftbuf2,true);
                    for(int i=0; i<fftsize*2;i++)
                        cirChannel2[i]=fftbuf2[i];

                    //得到两个channel的cir，分别存在cirChannel1和cirChannel2中，长度为1024*2

                    /* 投票寻找主峰index */
                    //前一百帧进行投票，寻找直达路径，然后后面的就不用计算了
                    if (time_count < 100) {
                        double maxval, temp = 0;
                        int maxindex = 0;
                        for (int i = 0; i < fftsize; i++) {
                            maxval = cirChannel1[2 * i] * cirChannel1[2 * i] + cirChannel1[2 * i + 1] * cirChannel1[2 * i + 1]; //通过复数的平方求最大值及其index
                            if (maxval > temp) {
                                temp = maxval;
                                maxindex = i;
                            }
                        }
                        hashlist1[maxindex] += 1;
                        temp = 0;
                        for (int i = 0; i < fftsize; i++) {
                            maxval = cirChannel2[2 * i] * cirChannel2[2 * i] + cirChannel2[2 * i + 1] * cirChannel2[2 * i + 1];
                            if (maxval > temp) {
                                temp = maxval;
                                maxindex = i;
                            }
                        }
                        hashlist2[maxindex] += 1;

                        for (int i = 0 ; i< fftsize; i++){
                            if(hashlist1[i]> hashlist1[maxindex1])
                                maxindex1 = i;
                            if(hashlist2[i]>hashlist2[maxindex2])
                                maxindex2 = i;
                        }
                    }

                    /* 计算CIR的实值，通过平方根来求*/
                    for (int i = 0;i<fftsize;i++){
                        cirReal1[i] = Math.sqrt(cirChannel1[2*i] * cirChannel1[2*i] + cirChannel1[2*i+1] * cirChannel1[2*i+1]);
                        cirReal2[i] = Math.sqrt(cirChannel2[2*i] * cirChannel2[2*i] + cirChannel2[2*i+1] * cirChannel2[2*i+1]);
                    }

                    /*计算实数值cir diff*/
                    for(int i=0;i<fftsize;i++){
                        cirDiffReal1[i] = cirReal1[i] - lastChannelReal1[i];
                        lastChannelReal1[i] = cirReal1[i];
                        cirDiffReal2[i] = cirReal2[i] - lastChannelReal2[i];
                        lastChannelReal2[i] = cirReal2[i];
                    }

                    /* 计算CIR diff  */
                    for(int i=0;i<fftsize*2;i++){
                        cirDiff1[i] = cirChannel1[i] - lastChannel1[i];
                        lastChannel1[i] = cirChannel1[i];
                        cirDiff2[i] = cirChannel2[i] - lastChannel2[i];
                        lastChannel2[i] = cirChannel2[i];
                    }

                    /*存入滑动矩阵*/
                    if(seq_index < seq_length-1){
                        for(int i = 0;i<path_length;i++) {
                            seq[0][i][seq_index] = cirDiffReal1[(i+maxindex1)%fftsize];
                            seq[1][i][seq_index] = cirDiffReal2[(i+maxindex2)%fftsize];
                        }
                        seq_index ++;
                    }
                    else if (seq_index == seq_length-1){
                        for (int i = 0;i<seq_length-1;i++){
                            for(int j = 0;j<path_length;j++){
                                seq[0][j][i] = seq[0][j][i+1];
                                seq[1][j][i] = seq[1][j][i+1];
                            }
                        }
                        for(int i = 0;i<path_length;i++){
                            seq[0][i][seq_length-1] = cirDiffReal1[(i+maxindex1)%fftsize];
                            seq[1][i][seq_length-1] = cirDiffReal2[(i+maxindex2)%fftsize];
                        }

                        int data_count = 0;
                        for(int i =0;i<numChannels;i++){
                            for(int j=0;j<path_length;j++){
                                for (int k = 0;k<seq_length;k++){
                                    data[data_count++] = (float) seq[i][j][k];
                                }
                            }
                        }

                    }


                    /* 计算前200帧的方差（正则化后的） 同时求当前运动的绝对距离 */
                    double val1 = 0, val2 = 0, val11=0, val22= 0;
                    double maxDiffCh1 = 0, maxDiffCh2 = 0;
                    double buf[][] = new double[2][8];

                    for(int j=0; j<200;j++){
                        int i;
                        i = (maxindex1  + j + fftsize) % fftsize;   //直达路径后200个path_index的复数平方的大小
                        double temp1 = cirDiff1[2 * i] * cirDiff1[2 * i] + cirDiff1[2 * i + 1] * cirDiff1[2 * i + 1];
                        /*信道1 运动绝对距离 */
                        if (maxDiffCh1 < temp1){
                            maxDiffCh1 = temp1;
                            max_index[0] = j;
                        }
                        val1 += temp1;
                        val11 += Math.sqrt(temp1);
                        i = (maxindex2  + j + fftsize) % fftsize;
                        double temp2 = cirDiff2[2 * i] * cirDiff2[2 * i] + cirDiff2[2 * i + 1] * cirDiff2[2 * i + 1];

                        /*信道2 运动绝对距离*/
                        if (maxDiffCh2 < temp2){
                            maxDiffCh2 = temp2;
                            max_index[1] = j;
                        }
                        val2 += temp2;
                        val22 += Math.sqrt(temp2);

                        /* 计算当前1帧的特征 150 * 1 -> 15 * 1*/
                        if(j < 150 && j% 10 == 0) {
                            curDisAxisFea[0][j / 10] = Math.sqrt(temp1);
                            curDisAxisFea[1][j / 10] = Math.sqrt(temp2);
                        }else if(j< 150){
                            curDisAxisFea[0][j / 10] = Math.sqrt(temp1) > curDisAxisFea[0][j / 10] ? Math.sqrt(temp1): curDisAxisFea[0][j / 10];
                            curDisAxisFea[1][j / 10] = Math.sqrt(temp2) > curDisAxisFea[1][j / 10] ? Math.sqrt(temp2): curDisAxisFea[1][j/10];
                        }
                    }


                    double cur_var1 = val1 !=0 ? (val1 - val11 * val11 / 200) / 200 / val1: 0;
                    double cur_var2 = val2 !=0 ? (val2 - val22 * val22 / 200) / 200 / val2: 0;


                    /*方差做平滑*/
                    double var1 = last_var[0] * 0.9 + cur_var1 * 0.1;
                    double var2 = last_var[1] * 0.9 + cur_var2 * 0.1;
                    last_var[0] = var1;
                    last_var[1] = var2;

                    /*运动距离做优化 加约束*/
                    if(Math.sqrt(maxDiffCh1) > 500 && var1 > 0.0012){
                        last_max_index[0] = max_index[0];
                    }
                    else{
                        max_index[0] = last_max_index[0];
                    }
                    if(Math.sqrt(maxDiffCh2) > 500 && var2 > 0.0012){
                        last_max_index[1] = max_index[1];
                    }
                    else{
                        max_index[1] = last_max_index[1];
                    }

                    /*状态流转*/
//                    if(status == 0 && max_index[0] > 100 && var1 > 0.0012){
//                        status = 1;
//
//                    }
//                    else if(status <= 1 && max_index[0] <= 100 && var1 > 0.0012){
//                        status = 2;
//                        count = 0;
//                    }
//                    else if(status == 1 && max_index[0] > 100 && var1 <= 0.0012){
//                        status = 0;
//
//                    }
//                    else if(status == 2 && max_index[0] > 100 && var1 > 0.0012 ){
//                        status = 1;
//                    }
//                    /*状态2->3 加上最大时长约束(约1秒) 对用户友好*/
//                    else if(status == 2 && ((max_index[0] <= 100 && var1 < 0.0012))){
//                        status = 3;
//                        count = 0;
//                        for (int i= 0;i< 5;i++)
//                            max_var_list[i] = 9999;
//                    }
//                    else if(status == 2){
//                        count += 1;
//                    }
//                    else if(status == 3 && var1 >0.0014 && var2 > 0.0014)
//                    {
//                        status = 4;
//                        count ++;
//                        /* 缓存的前几帧的特征先进一步处理*/
//                        for(int j=0;j<count;j++){
//                            for(int i=0; i<15;i++){
//                                if(j==0){
//                                    maxPoolingFea[0][j/6][i] = disAxisFeatureBuf[0][j][i];
//                                    maxPoolingFea[1][j/6][i] = disAxisFeatureBuf[1][j][i];
//                                }
//                                else{
//                                    maxPoolingFea[0][j/6][i] = disAxisFeatureBuf[0][j][i] > maxPoolingFea[0][j/6][i] ? disAxisFeatureBuf[0][j][i]: maxPoolingFea[0][j/6][i];
//                                    maxPoolingFea[1][j/6][i] = disAxisFeatureBuf[1][j][i] > maxPoolingFea[1][j/6][i] ? disAxisFeatureBuf[1][j][i]: maxPoolingFea[1][j/6][i];
//                                }
//                            }
//                        }
//
//                        feaMax[0] = maxPoolingFea[0][0][0];
//                        feaMax[1] = maxPoolingFea[1][0][0];
//                        feaMin[0] = maxPoolingFea[0][0][0];
//                        feaMin[1] = maxPoolingFea[1][0][0];
//                        /* 对当前帧特征进一步处理*/
//                        if (count % 6 == 0){
//                            for(int i=0;i<15;i++){
//                                maxPoolingFea[0][count/6][i] = curDisAxisFea[0][i];
//                                maxPoolingFea[1][count/6][i] = curDisAxisFea[1][i];
//                                feaMax[0] = maxPoolingFea[0][count/6][i] > feaMax[0] ? maxPoolingFea[0][count/6][i]: feaMax[0];
//                                feaMax[1] = maxPoolingFea[1][count/6][i] > feaMax[1] ? maxPoolingFea[1][count/6][i]: feaMax[1];
//                                feaMin[0] = maxPoolingFea[0][count/6][i] < feaMin[0] ? maxPoolingFea[0][count/6][i]: feaMin[0];
//                                feaMin[1] = maxPoolingFea[1][count/6][i] < feaMin[1] ? maxPoolingFea[1][count/6][i]: feaMin[1];
//
//                            }
//                        }else{
//                            for(int i=0;i<15;i++){
//                                maxPoolingFea[0][count/6][i] = curDisAxisFea[0][i] > maxPoolingFea[0][count/6][i] ? curDisAxisFea[0][i] : maxPoolingFea[0][count/6][i];
//                                maxPoolingFea[1][count/6][i] = curDisAxisFea[1][i] > maxPoolingFea[1][count/6][i] ? curDisAxisFea[1][i] : maxPoolingFea[1][count/6][i];
//                                feaMax[0] = maxPoolingFea[0][count/6][i] > feaMax[0] ? maxPoolingFea[0][count/6][i]: feaMax[0];
//                                feaMax[1] = maxPoolingFea[1][count/6][i] > feaMax[1] ? maxPoolingFea[1][count/6][i]: feaMax[1];
//                                feaMin[0] = maxPoolingFea[0][count/6][i] < feaMin[0] ? maxPoolingFea[0][count/6][i]: feaMin[0];
//                                feaMin[1] = maxPoolingFea[1][count/6][i] < feaMin[1] ? maxPoolingFea[1][count/6][i]: feaMin[1];
//                            }
//                        }
//
//                        updateList(max_var_list,var1);    //更新 最近5帧的方差
//
//                        for(int i=maxindex2;i<maxindex2+150;i++){
//                            writerRaw.write(cirDiff2[(i%fftsize)*2]+" "+cirDiff2[(i%fftsize)*2+1]+" ");
//                        }
//                        writerRaw.write("\n");
//                    }
//                    else if (status == 4){
//                        updateList(max_var_list,var1);  //更新 最近5帧的方差
//
//                        for(int i=maxindex2;i<maxindex2+150;i++){
//                            writerRaw.write(cirDiff2[(i%fftsize)*2]+" "+cirDiff2[(i%fftsize)*2+1]+" ");
//                        }
//                        writerRaw.write("\n");
//
//                        double avgMaxVar = getMaxVarFromList(max_var_list); //选5帧中最大方差代表有无运动
//                        if(count > 20 && count < 71){
//                            count += 1;
//                            if (count % 6 == 0){
//                                for(int i=0;i<15;i++){
//                                    maxPoolingFea[0][count/6][i] = curDisAxisFea[0][i];
//                                    maxPoolingFea[1][count/6][i] = curDisAxisFea[1][i];
//                                    feaMax[0] = maxPoolingFea[0][count/6][i] > feaMax[0] ? maxPoolingFea[0][count/6][i]: feaMax[0];
//                                    feaMax[1] = maxPoolingFea[1][count/6][i] > feaMax[1] ? maxPoolingFea[1][count/6][i]: feaMax[1];
//                                    feaMin[0] = maxPoolingFea[0][count/6][i] < feaMin[0] ? maxPoolingFea[0][count/6][i]: feaMin[0];
//                                    feaMin[1] = maxPoolingFea[1][count/6][i] < feaMin[1] ? maxPoolingFea[1][count/6][i]: feaMin[1];
//                                }
//                            }else{
//                                for(int i=0;i<15;i++){
//                                    maxPoolingFea[0][count/6][i] = curDisAxisFea[0][i] > maxPoolingFea[0][count/6][i] ? curDisAxisFea[0][i] : maxPoolingFea[0][count/6][i];
//                                    maxPoolingFea[1][count/6][i] = curDisAxisFea[1][i] > maxPoolingFea[1][count/6][i] ? curDisAxisFea[1][i] : maxPoolingFea[1][count/6][i];
//                                    feaMax[0] = maxPoolingFea[0][count/6][i] > feaMax[0] ? maxPoolingFea[0][count/6][i]: feaMax[0];
//                                    feaMax[1] = maxPoolingFea[1][count/6][i] > feaMax[1] ? maxPoolingFea[1][count/6][i]: feaMax[1];
//                                    feaMin[0] = maxPoolingFea[0][count/6][i] < feaMin[0] ? maxPoolingFea[0][count/6][i]: feaMin[0];
//                                    feaMin[1] = maxPoolingFea[1][count/6][i] < feaMin[1] ? maxPoolingFea[1][count/6][i]: feaMin[1];
//                                }
//                            }
//
//                        }
//                        /*/ 72帧动作特征提取结束*/
//                        else if (count == 71){
//                            //
//                            count = 0;
//                            status = 5;
//
//                            /* 计算 timeSum and disSum*/
//                            for(int i=0;i<12;i++)
//                            {
//                                for(int j=0;j<15;j++){
//                                    if(j==0){
//                                        timeSum[0][i] =  maxPoolingFea[0][i][j];
//                                        timeSum[1][i] =  maxPoolingFea[1][i][j];
//                                    }else{
//                                        timeSum[0][i] += maxPoolingFea[0][i][j];
//                                        timeSum[1][i] += maxPoolingFea[1][i][j];
//                                    }
//                                    if(i==0){
//                                        disSum[0][j] = maxPoolingFea[0][i][j];
//                                        disSum[1][j] = maxPoolingFea[1][i][j];
//                                    }else{
//                                        disSum[0][j] += maxPoolingFea[0][i][j];
//                                        disSum[1][j] += maxPoolingFea[1][i][j];
//                                    }
//                                }
//                            }
//                            //mylog(maxPoolingFea[1][0][0]+", "+maxPoolingFea[1][0][1]+", "+maxPoolingFea[1][0][2]+", "+maxPoolingFea[1][0][3]);
//
//                            /* find highest index in disSum*/
//                            int disIndex[] = new int[2];
//                            disIndex[0] = 0;
//                            disIndex[1] = 0;
//                            for(int i=0;i<15;i++){
//                                disIndex[0] = disSum[0][i] > disSum[0][disIndex[0]] ? i: disIndex[0];
//                                disIndex[1] = disSum[1][i] > disSum[1][disIndex[1]] ? i: disIndex[1];
//                            }
//
//                            /* find starting index in timeSum*/
//                            int timeIndex[] = new int[2];
//                            timeIndex[0] = 0;
//                            timeIndex[1] = 0;
//                            double tempMax[] = new double[2];
//                            double tempMin[] = new double[2];
//
//                            for(int i=0;i<2;i++){
//                                tempMax[i] = timeSum[i][0];
//                                tempMin[i] = timeSum[i][1];
//                            }
//                            for(int i =0;i< 12;i++){
//                                tempMax[0] = timeSum[0][i] > tempMax[0] ? timeSum[0][i]: tempMax[0];
//                                tempMax[1] = timeSum[1][i] > tempMax[1] ? timeSum[1][i]: tempMax[1];
//                                tempMin[0] = timeSum[0][i] < tempMin[0] ? timeSum[0][i]: tempMin[0];
//                                tempMin[1] = timeSum[1][i] < tempMin[1] ? timeSum[1][i]: tempMin[1];
//                            }
//
//                            for(int i=0;i<12;i++){
//                                if((timeSum[0][i]-tempMin[0])/(tempMax[0]-tempMin[0])>0.1){
//                                    timeIndex[0] = i;
//                                    break;
//                                }
//                            }
//                            for(int i=0;i<12;i++){
//                                if((timeSum[1][i]-tempMin[1])/(tempMax[1]-tempMin[1])>0.1){
//                                    timeIndex[1] = i;
//                                    break;
//                                }
//                            }
//                            /* 按照前面找到的点将特征图平移*/
//                            for(int i=0;i<12;i++){
//                                for(int j=0;j<15;j++){
//                                    newFea[0][i][j] = (maxPoolingFea[0][(i+timeIndex[0])% 12][(j+disIndex[0]-5+15)%15] - feaMin[0]) /(feaMax[0]-feaMin[0]);
//                                    newFea[1][i][j] = (maxPoolingFea[1][(i+timeIndex[1])% 12][(j+disIndex[1]-5+15)%15] - feaMin[1])/ (feaMax[1]- feaMin[1]);
//                                }
//                            }
//
//                            /*存入svm*/
//                            for(int i=0;i<180;i++){
//                                feature_svm[i].index = i+1;
//                                feature_svm[i].value = newFea[0][i/15][i%15];
//                                feature_svm[i+180].index = i+181;
//                                feature_svm[i+180].value = newFea[1][i/15][i%15];
//                            }
//
//                            for (int i=0;i<360;i++){
//                               writer.write(feature_svm[i].value+" ");
//                            }
//                            writer.write("\n");
//                        }
//                        else if(count <= 20 && avgMaxVar > 0.0014) {
//                            count += 1;
//                            if (count % 6 == 0){
//                                for(int i=0;i<15;i++){
//                                    maxPoolingFea[0][count/6][i] = curDisAxisFea[0][i];
//                                    maxPoolingFea[1][count/6][i] = curDisAxisFea[1][i];
//                                    feaMax[0] = maxPoolingFea[0][count/6][i] > feaMax[0] ? maxPoolingFea[0][count/6][i]: feaMax[0];
//                                    feaMax[1] = maxPoolingFea[1][count/6][i] > feaMax[1] ? maxPoolingFea[1][count/6][i]: feaMax[1];
//                                    feaMin[0] = maxPoolingFea[0][count/6][i] < feaMin[0] ? maxPoolingFea[0][count/6][i]: feaMin[0];
//                                    feaMin[1] = maxPoolingFea[1][count/6][i] < feaMin[1] ? maxPoolingFea[1][count/6][i]: feaMin[1];
//                                }
//                            }else{
//                                for(int i=0;i<15;i++){
//                                    maxPoolingFea[0][count/6][i] = curDisAxisFea[0][i] > maxPoolingFea[0][count/6][i] ? curDisAxisFea[0][i] : maxPoolingFea[0][count/6][i];
//                                    maxPoolingFea[1][count/6][i] = curDisAxisFea[1][i] > maxPoolingFea[1][count/6][i] ? curDisAxisFea[1][i] : maxPoolingFea[1][count/6][i];
//                                    feaMax[0] = maxPoolingFea[0][count/6][i] > feaMax[0] ? maxPoolingFea[0][count/6][i]: feaMax[0];
//                                    feaMax[1] = maxPoolingFea[1][count/6][i] > feaMax[1] ? maxPoolingFea[1][count/6][i]: feaMax[1];
//                                    feaMin[0] = maxPoolingFea[0][count/6][i] < feaMin[0] ? maxPoolingFea[0][count/6][i]: feaMin[0];
//                                    feaMin[1] = maxPoolingFea[1][count/6][i] < feaMin[1] ? maxPoolingFea[1][count/6][i]: feaMin[1];
//                                }
//                            }
//                        }else{
//                            status = 3;
//                            count = 0;
//                            for (int i= 0;i< 5;i++)
//                                max_var_list[i] = 9999;
//                        }
//                        //mylog(""+avgMaxVar+" "+count);
//                    }
//                    else if (status == 5 && count == 0){
//                        updateList(max_var_list,var1);
//                        count += 1;
//                        double result = svm.svm_predict_values(svmModel, feature_svm, dec_value);   //svm 出结果
//
//                        switch ((int)(result)){
//                            case 0:
//                                svmStatus = 6;
//                                break;
//                            case 1:
//                                svmStatus = 7;
//                                break;
//                            case 2:
//                                svmStatus = 8;
//                                break;
//                            case 3:
//                                svmStatus = 9;
//                                break;
//                        }
// //                       mylog("label is: " + (int)result+ " "+ dec_value[0] +" " + dec_value[1] +" " +dec_value[2]+ " " +dec_value[3]);
//                        //mylog(""+count);
//
//                    }else if(status == 5){
//                        updateList(max_var_list,var1);
//                        count += 1;
//                        if (count > 100){   //状态5 持续2秒
//                            count = 0;
//                            if (max_index[0] > 100 && var1 > 0.0012)
//                                status = 1;
//                            else if(max_index[0] <= 100){
//                                status = 2;
//                                count = 0;
//                            }
//                            else if(max_index[0] > 100 && var1 <= 0.0012)
//                                status = 0;
//                        }
//                    }
//
//                    /* 状态3 维护最大长度为5的特征缓存 15*5*/
//                    if(status == 3){
//                        if (var1 < max_var_list[0]) {
//                            count = 0;
//                            for(int i=0;i<15;i++){
//                                disAxisFeatureBuf[0][0][i] = curDisAxisFea[0][i];
//                                disAxisFeatureBuf[1][0][i] = curDisAxisFea[1][i];
//                            }
//                        }else if(count < 4){
//                            count++;
//                            for(int i=0;i<15;i++){
//                                disAxisFeatureBuf[0][count][i] = curDisAxisFea[0][i];
//                                disAxisFeatureBuf[1][count][i] = curDisAxisFea[1][i];
//                            }
//                        } else {
//                            count = 4;
//                            for(int j=0;j<=3;j++){
//
//                                for(int i=0;i<15;i++){
//                                    disAxisFeatureBuf[0][j][i] = disAxisFeatureBuf[0][j+1][i];
//                                    disAxisFeatureBuf[1][j][i] = disAxisFeatureBuf[1][j+1][i];
//                                }
//                            }
//                            for(int i=0;i<15;i++){
//                                disAxisFeatureBuf[0][count][i] = curDisAxisFea[0][i];
//                                disAxisFeatureBuf[1][count][i] = curDisAxisFea[1][i];
//                            }
//                        }
//                        updateList(max_var_list,var1);
//                        for(int i=maxindex2;i<maxindex2+150;i++){
//                            writerRaw.write(cirDiff2[(i%fftsize)*2]+" "+cirDiff2[(i%fftsize)*2+1]+" ");
//                        }
//                        writerRaw.write("\n");
//                    }
//
//
//                    /*显示当前状态 或 输出svm结果*/
//                    Message msg = new Message();
//                    if (status <= 4) {
//                        msg.what = status;
//                        uiHandler.sendMessage(msg);
//                    }
//                    else if (status == 5 && count == 1){
//                        //mylog(""+svmStatus);
//                        msg.what = svmStatus;
//                        uiHandler.sendMessage(msg);
//                    }

                    time_count+=1;      //帧数+1

                    /*socket传输部分*/
                    if (sendDatatoPython && datastream != null) {
                        int j = 0;
                        double data = 0;
                        short sdata = 0;
                        for (int i = 0; i < line; i++) {
                            //sum = sum + bsRecord[i];
                            if (i % 2 == 0) {
                                data = Math.sqrt(cirChannel1[i] * cirChannel1[i] + cirChannel1[i + 1] * cirChannel1[i + 1]);
                            } else {
                                data = Math.sqrt(cirChannel2[i] * cirChannel2[i] + cirChannel2[i - 1] * cirChannel2[i - 1]);
                            }

                            sdata = (short) data;

                            networkbuf[j++] = (byte) (sdata & 0xFF);
                            networkbuf[j++] = (byte) (sdata >> 8);
                        }
                        for (int i = 0; i < line; i++) {
                            networkbuf[j++] = (byte) (bsRecord[i] & 0xFF);
                            networkbuf[j++] = (byte) (bsRecord[i] >> 8);

                        }
                        //mylog(bsRecord[0]+", "+bsRecord[1]);
                        //Log.i("wavedemo", "data sum:" + sum);

                        if (time_count == 1) {

                            if (timestamp) {
                                byte[] netbuf = new byte[8];
                                long a = ts;
                                netbuf[0] = (byte) (a & 0xFF);
                                netbuf[1] = (byte) ((a >> 8) & 0xFF);
                                netbuf[2] = (byte) ((a >> 16) & 0xFF);
                                netbuf[3] = (byte) ((a >> 24) & 0xFF);
                                netbuf[4] = (byte) ((a >> 32) & 0xFF);
                                netbuf[5] = (byte) ((a >> 40) & 0xFF);
                                netbuf[6] = (byte) ((a >> 48) & 0xFF);
                                netbuf[7] = (byte) ((a >> 56) & 0xFF);
                                try {
                                    //mylog(time_count+"time count");
                                    datastream.write(netbuf, 0, 8);
                                    //mylog( "socket write" + j);
                                } catch (Exception e) {
                                    // TODO: handle this
                                    mylog("socket error" + e);
                                }
                                //mylog(""+a);
                            }
                        }

                        if (datastream != null) {
                            try {
                                //mylog(time_count+"time count");
                                datastream.write(networkbuf, 0, j);
                                //mylog( "socket write" + j);
                            } catch (Exception e) {
                                // TODO: handle this
                                mylog("socket error" + e);
                            }
                        }
                    }
                }
            }
            time_count = 0;
            writer.close();
            writerRaw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
    class ThreadSocket extends Thread{
        public void run()
        {
            byte []networkbuf = new byte[8];
            try{
                dataSocket = new Socket(IP_address,12345);
                mylog("socket connected"+ dataSocket);
                datastream = dataSocket.getOutputStream();
                mylog("socketstream:" + datastream);
            } catch(Exception e) {
            // TODO: handle this
            mylog("socket error"+e);
            }
        }
    }


    /*  更新方差缓存   */
    private void updateList(double []list, double var){
        for(int i =0;i < list.length - 1; i++)
        {
            list[i+1] = list[i];
        }
        list[0] = var;
    }

    /*  获得最大方差  */
    private double getMaxVarFromList(double []list){
        double max = 0;
        for (int i = 0;i<list.length;i++){
            if(list[i] > max && list[i] != 9999){
                max = list[i];
            }
        }
        return max;
    }


    /* 加载svm模型  */
    private void loadSVMModal(){
        svmModel = new svm_model();
        try{
            svmModel = svm.svm_load_model("/storage/emulated/0/storage/emulated/0/zc_gesture/svm_model_new.txt");  //svm模型地址
            //svmModel = svm.svm_load_model("svm_model_new.txt");
            mylog("model has been loaded!");

        } catch (IOException e){
            e.printStackTrace();
        }
    }

    /* 控制台打印输出 */
    private void mylog(String information)
    {
        if(logenabled)
        {

            Log.i(systemName,information);
        }
    }

}
