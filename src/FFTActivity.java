import com.opencsv.CSVWriter;
import com.opencsv.bean.OpencsvUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.FastFourierTransformer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

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
                //String[] text=FFTQ();
                try {
                    writeCSV();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //System.out.println("De aici intra ala");
                //double[] arrayMofo=(FFTQ());
                //for(int m=0;m<arrayMofo.length;m++)
                //{
                //    System.out.println(Math.abs(arrayMofo[m]));
                //}
                //System.out.println(text[0]);
                //System.out.println(text[1]);
                //System.out.println(text[2]);
                //System.out.println(text[3]);
                //System.out.println(text[4]);
                //System.out.println(text[5]);
                //Format1.setText("FORMAT1 "+text[0]);
                //Format2.setText("FORMAT2 "+text[1]);
                //for(int ind=0;ind<text.length;ind++)
                //System.out.println(text[ind]);


            }
        });


    }


    public double[] FFTQ(String fileLoc) {
        double[] arrayR = new double[4096];
        try {


            ApacheFFT apacheFFT = new ApacheFFT();
            WindowFunction w = new RectangularWindow();


            WavIO wave = readWavBytes(fileLoc);
            short[] b = new short[wave.myData.length / 2];
            for (int i = 0; i < wave.myData.length - 1; i += 2)
                b[i / 2] = twoBytesToShort(wave.myData[i + 1], wave.myData[i]);


            double[] dataNew = new double[b.length];

            for (int i = 0; i < dataNew.length; i++) {
                dataNew[i] = b[i];
            }

            apacheFFT.forward(dataNew, (float) 44100, w);

            Spectrum s = apacheFFT.getMagnitudeSpectrum();
            double[] freq = s.array();

            for (int i = 0; i < freq.length; i++) {
                freq[i] = 10 * Math.log10(10 * Math.abs(freq[i]));
            }

            for(int ind=0;ind<arrayR.length;ind++)
            {
                arrayR[ind]=freq[ind];
            }
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

    public void writeCSV() throws IOException {
        CSVWriter writer=new CSVWriter(new FileWriter("C:/Users/battl/PycharmProjects/SpeechRec/data.csv"));
        File folder = new File("E:/FisiereLicenta/SuneteAplicatie");
        File[] listOfFiles=folder.listFiles();
        //String directory="E:/FisiereLicenta/SuneteAplicatie";
        //String fileName="WOOD1.wav";

        double[] showArray=new double[2048];
        writer.writeNext(new String[]{"label","values"});
      try {
          Collection files = FileUtils.listFiles(folder, new String[]{"wav"}, true);

          for (Iterator iterator = files.iterator(); iterator.hasNext(); ) {
              File file = (File) iterator.next();
             System.out.println(file.getAbsolutePath());

              double[] showarray = FFTQ(file.getAbsolutePath());

              String[] strarr = new String[]{(file.getName()).replaceAll("[^A-Z]",""),(Arrays.toString(showarray)).replaceAll("[^0-9.,]+","")};

              writer.writeNext(strarr);
              //writer.writeNext(new String[]{file.getName()});
          }
      }catch(Exception e){
          e.printStackTrace();
      }
        //System.out.println(files);
        writer.close();
    }

    public static WavIO readWavBytes(String path){
        WavIO wave = new WavIO(path);
        wave.read();
        return wave;
    }

    static short twoBytesToShort(byte b1, byte b2) {
        return (short) ((b1 << 8) | (b2 & 0xFF));
    }


    public static int byteArrayToInt(byte[] b)
    {
        int start = 0;
        int low = b[start] & 0xff;
        int high = b[start+1] & 0xff;
        return (int)( high << 8 | low );
    }


}

