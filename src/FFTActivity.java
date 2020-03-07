import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.FastFourierTransformer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;

public class FFTActivity extends JFrame{

    private JPanel mainPanel;
    private JButton button1;
    private JLabel Format1;
    private JLabel Format2;


    private FFTActivity(String title){
        super(title);
        this.setContentPane(mainPanel);

        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                //JFrame frame=new FFTActivity("Hallo");
                //frame.setVisible(true);
                FFTQ();
                String[] text=FFTQ();
                System.out.println("De aici intra ala");
                //double[] arrayMofo=(FFTQ());
                //for(int m=0;m<arrayMofo.length;m++)
                //{
                //    System.out.println(Math.abs(arrayMofo[m]));
                //}
                System.out.println(text[0]);
                System.out.println(text[1]);
                System.out.println(text[2]);
                System.out.println(text[3]);
                System.out.println(text[4]);
                System.out.println(text[5]);
                Format1.setText("FORMAT1 "+text[0]);
                Format2.setText("FORMAT2 "+text[1]);
            }
        });


    }


    public String[] FFTQ()
    {

    final String TAG ="";
    FastFourierTransformer fft;
    long myDataSize;
     Complex fftArray;
     String myPath;
     long myChunkSize;
     long mySubChunk1Size;
     int myFormat;
     long myChannels;
     long mySampleRate;
     long myByteRate;
     int myBlockAlign;
     int myBitsPerSample;
     double[] imag;
     String[] arrayR = new String[7];
    //private long myDataSize;

    //AudioInputStream audioInputStream;
    ApacheFFT apacheFFT=new ApacheFFT();
    int totalFramesRead=0;
    File fileIn = new File("E:"+"/"+"FisiereLicenta"+"/"+"test.wav");
    DataInputStream inFile=null;
     byte[] myData;
    // private long myChunkSize;

    WindowFunction w = new HanningWindow();

        //wavIo wave= readWavBytes();
        byte[] tmpLong=new byte[4];
        byte[] tmpInt = new byte[2];
    try {
        inFile = new DataInputStream(new FileInputStream(fileIn));


        String chunkID = "" + (char) inFile.readByte() + (char) inFile.readByte() + (char) inFile.readByte() + (char) inFile.readByte();


        inFile.read(tmpLong); // read the ChunkSize
        myChunkSize = byteArrayToLong(tmpLong);

        String format = "" + (char) inFile.readByte() + (char) inFile.readByte() + (char) inFile.readByte() + (char) inFile.readByte();

        // print what we've read so far
        //System.out.println("chunkID:" + chunkID + " chunk1Size:" + myChunkSize + " format:" + format); // for debugging only
        System.out.println("==========================================");

        System.out.println("chunkID:" + chunkID + " chunk1Size:" + myChunkSize + " format:" + format);


        String subChunk1ID = "" + (char) inFile.readByte() + (char) inFile.readByte() + (char) inFile.readByte() + (char) inFile.readByte();

        inFile.read(tmpLong); // read the SubChunk1Size
        mySubChunk1Size = byteArrayToLong(tmpLong);

        System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAASTA este SUBCHUNKSIZE");
        inFile.read(tmpInt); // read the audio format.  This should be 1 for PCM
        myFormat = byteArrayToInt(tmpInt);

        inFile.read(tmpInt); // read the # of channels (1 or 2)
        myChannels = byteArrayToInt(tmpInt);

        inFile.read(tmpLong); // read the samplerate
        mySampleRate = byteArrayToLong(tmpLong);

        inFile.read(tmpLong); // read the byterate
        myByteRate = byteArrayToLong(tmpLong);

        inFile.read(tmpInt); // read the blockalign
        myBlockAlign = byteArrayToInt(tmpInt);

        inFile.read(tmpInt); // read the bitspersample
        myBitsPerSample = byteArrayToInt(tmpInt);


        // print what we've read so far
        System.out.println("SubChunk1ID:" + subChunk1ID + " SubChunk1Size:" + mySubChunk1Size + " AudioFormat:" + myFormat + " Channels:" + myChannels + " SampleRate:" + mySampleRate);


        // read the data chunk header - reading this IS necessary, because not all wav files will have the data chunk here - for now, we're just assuming that the data chunk is here
        String dataChunkID = "" + (char) inFile.readByte() + (char) inFile.readByte() + (char) inFile.readByte() + (char) inFile.readByte();

        inFile.read(tmpLong); // read the size of the data
        myDataSize = byteArrayToLong(tmpLong);


        System.out.println("MYYYYYY DAAAAAAAAAAATAAAAAA SIIIIIIZE ISSSSSS   " + myDataSize);
        // read the data chunk
        myData = new byte[(int)myDataSize];
        //inFile.read(myData);


        double[] dataNew = new double[(int) myDataSize / 2];
        myData = new byte[(int) myDataSize];
        for (int i = 0; i < myDataSize / 2; i++) {
            short val = (short) ((inFile.readByte() & 0xFF) | (inFile.readByte() & 0xFF) << 8);
            dataNew[i] = (double) val;
        }
        /*for (int j = 0; j < dataNew.length; j++) {
            System.out.println("MY DATA IS HERE" + dataNew[j]);
        }*/

        //System.out.println("ASTA E DIMENSIUNEA DATELOR " + myDataSize);
        //System.out.println("ASTA E DIMENSIUNEA LA DATA PRELEVATA" + dataNew.length);

        double valMax = 0;
        int indexMax = 0;

        int indexInterest = 0;

        for (int q = 0; q < dataNew.length; q++) {
            if (dataNew[q] > valMax) {
                valMax = dataNew[q];
                indexMax = q;
            }

        }

        double[] arrayInterest = new double[dataNew.length - indexMax];

        while (dataNew[indexMax] > valMax * 0.1) {
            arrayInterest[indexInterest] = dataNew[indexMax];
            indexMax++;
            indexInterest++;
        }


        apacheFFT.forward(arrayInterest, (float) 44100, w);
        Spectrum s = apacheFFT.getMagnitudeSpectrum();
        double[] freq = s.array();
        double[] array = new double[freq.length];
        for (int i = 0; i < freq.length; i++) {
            array[i] = (10 * Math.log10(Math.abs(freq[i])));
        }

        for (int i = 0; i < array.length; i++)
            System.out.println("ASTEA SUNT FRECVENTELE" + Math.abs(array[i]));

        System.out.println("MARIMEA LA FRECVENTE" + freq.length);

        //getSampleRate(mySampleRate);


        arrayR[0]=chunkID;
        arrayR[1]=format;
        arrayR[2]=subChunk1ID;
        arrayR[3]=Long.toString(mySubChunk1Size);
        arrayR[4]=Long.toString(myChannels);
        arrayR[5]=Long.toString(mySampleRate);
        //System.out.println("AIIIIIIIIIIIIIIIIIIICIIIIIIIIIIII SE AFLA ============================== CHUNK ID");
    } catch (Exception e) {
        System.out.println(e.getMessage());
    }
        return arrayR;
    }
    public static void main(String[] args)
    {
        JFrame frame= new FFTActivity("HALO");
        frame.setPreferredSize(new Dimension(400,300));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

    }






    public static int byteArrayToInt(byte[] b)
    {
        int start = 0;
        int low = b[start] & 0xff;
        int high = b[start+1] & 0xff;
        return (int)( high << 8 | low );
    }

    public static long byteArrayToLong(byte[] b)
    {
        int start = 0;
        int i = 0;
        int len = 4;
        int cnt = 0;
        byte[] tmp = new byte[len];
        for (i = start; i < (start + len); i++)
        {
            tmp[cnt] = b[i];
            cnt++;
        }
        long accum = 0;
        i = 0;
        for ( int shiftBy = 0; shiftBy < 32; shiftBy += 8 )
        {
            accum |= ( (long)( tmp[i] & 0xff ) ) << shiftBy;
            i++;
        }
        return accum;
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
}

