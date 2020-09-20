package cn.edu.nju.zc_gesture_app;


/**
 * Created by sunke on 17/10/12.
 */
import java.io.*;
import java.math.BigInteger;

public class WavReader {
    public static int chunkSize, subChunk1Size,
            audioFormat, numChannels,
            sampleRate, byteRate,
            blockAlign, bitPerSample,
            subChunk2Size;
    public double actualData[];

    public WavReader(String fileName){
        byte data[];
        try {
            data = readWave(fileName);
            fileDescript(data); //set wave file header
        } catch (FileNotFoundException e) {
            System.out.println("File Not Found!!");
            e.printStackTrace();
        }
    }

    public double[] getData(){
        return actualData;
    }

    private void fileDescript(byte[] data){
        //System.out.println("******File header initialization******");
        byte[] temp = {data[4], data[5], data[6], data[7]};
        chunkSize = bigToLittleInt(temp);
        //System.out.println("Chunk Size: "+chunkSize);
        temp = new byte [] {data[16], data[17], data[18], data[19]};
        subChunk1Size = bigToLittleInt(temp);
        //System.out.println("subChunk1Size: "+subChunk1Size);
        temp = new byte [] {data[20], data[21]};
        audioFormat = bigToLittleInt(temp);
        //System.out.println("Audio Format: "+audioFormat);
        temp = new byte [] {data[22], data[23]};
        numChannels = bigToLittleInt(temp);
        //System.out.println("Num Channels: "+numChannels);
        temp = new byte [] {data[24], data[25], data[26], data[27]};
        sampleRate = bigToLittleInt(temp);
        //System.out.println("Sample Rate: "+sampleRate);
        temp = new byte [] {data[28], data[29], data[30], data[31]};
        byteRate = bigToLittleInt(temp);
        //System.out.println("Byte Rate: "+byteRate);
        temp = new byte [] {data[32], data[33]};
        blockAlign = bigToLittleInt(temp);
        //System.out.println("Block Align: "+blockAlign);
        temp = new byte [] {data[34], data[35]};
        bitPerSample = bigToLittleInt(temp);
        //System.out.println("Bits Per Sample: "+bitPerSample);
        temp = new byte [] {data[40], data[41], data[42], data[43]};
        subChunk2Size = bigToLittleInt(temp);
        //System.out.println("SubChunk2Size: "+subChunk2Size);

        //Read sample part
        int sIndex=44, index=0;
        actualData = new double[subChunk2Size];
        while(sIndex<subChunk2Size){
            index++;
            temp = new byte [] {data[sIndex++], data[sIndex++]};
            actualData[index] = bigToLittleDouble(temp);
        }

		/*for(int i=0;i<index+1;i++){
				System.out.print(result[i]+" ");
		}*/
        writeFiles(actualData, index);
    }

    private static int bigToLittleInt(byte[] raw){ //reverse array proved..
        for(int i = 0; i < raw.length / 2; i++)
        {
            byte temp = raw[i];
            raw[i] = raw[raw.length - i - 1];
            raw[raw.length - i - 1] = temp;
        }
        return byteArrayToInt(raw);
    }

    private static double bigToLittleDouble(byte[] raw){
        for(int i = 0; i < raw.length / 2; i++)
        {
            byte temp = raw[i];
            raw[i] = raw[raw.length - i - 1];
            raw[raw.length - i - 1] = temp;
        }
        return (double) new BigInteger(raw).intValue();
    }

    private static int byteArrayToInt(byte[] b) {
		/*for(byte r:b)
			System.out.printf("%02X ",r);*/
        //System.out.println();
        if (b.length == 4)
            return b[0] << 24 | (b[1] & 0xff) << 16 | (b[2] & 0xff) << 8
                    | (b[3] & 0xff);
        else if (b.length == 2)
            return 0x00 << 24 | 0x00 << 16 | (b[0] & 0xff) << 8 | (b[1] & 0xff);

        return 0;
    }


    private static byte[] readWave(String path) throws FileNotFoundException{
        FileInputStream fin;
        int len;
        byte data[] = new byte[1000000];
        try {
            fin = new FileInputStream(path);
            do {
                len = fin.read(data);
	      /*for (int j = 0; j < len; j++)
	        System.out.printf("%02X ", data[j]);*/
            } while (len != -1);
            //System.out.println("\nReading finish...");
            fin.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return data;
    }

    private static void writeFiles(double[] audi, int length){
        try {
            //System.out.println("Writing Files...");
            PrintWriter writer = new PrintWriter("test_wave.txt", "UTF-8");
            for(int i=0;i<length+1;i++){
                writer.write((double)i/sampleRate+" ");
                writer.println(audi[i]);
            }
            writer.close();
            //System.out.println("Writing done");
        } catch (FileNotFoundException e) {
            //System.out.println("File not found");
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            //System.out.println("Unsupported file format");
            e.printStackTrace();
        }
    }
}
