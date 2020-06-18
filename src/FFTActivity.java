import com.opencsv.CSVWriter;
import com.opencsv.ICSVWriter;
import com.opencsv.bean.OpencsvUtils;
import net.sf.javaml.classification.Classifier;
import net.sf.javaml.classification.KNearestNeighbors;
import net.sf.javaml.classification.evaluation.EvaluateDataset;
import net.sf.javaml.classification.evaluation.PerformanceMeasure;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.Instance;
import net.sf.javaml.tools.Serial;
import net.sf.javaml.tools.data.FileHandler;
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
import java.util.Map;

public class FFTActivity extends JFrame{

    private JPanel mainPanel;
    private JButton button1;
    private JLabel Format1;
    private JLabel Format2;
    private JButton button2;


    private FFTActivity(String title){
        super(title);
        this.setContentPane(mainPanel);

        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    writeCSV();
                } catch (IOException e) {
                    e.printStackTrace();
                }





            }
        });


        button2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    trainModel("E:/ObjFreqReq/Data/data.csv");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    public double[] FFTQ(String fileLoc) {
        double[] arrayR = new double[2205];
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
            double[] freq = Binning.ComputeBins(s.array(), 2205, false);
            for(int ind=0;ind<freq.length;ind++)
            {
                arrayR[ind]=10*Math.log10(10*Math.abs(freq[ind]));
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
        //CSVWriter writer=new CSVWriter(new FileWriter("C:/Users/battl/PycharmProjects/SpeechRec/data.csv"),char "'");
        CSVWriter writer =new CSVWriter(new FileWriter("E:/ObjFreqReq/Data/data.csv"),',',CSVWriter.NO_QUOTE_CHARACTER,CSVWriter.NO_ESCAPE_CHARACTER,CSVWriter.RFC4180_LINE_END);
        File folder = new File("E:/ObjFreqReq/TrainingSounds");
        File[] listOfFiles=folder.listFiles();

        //String[] stringcsv=new String[2206];
        //stringcsv[2205]="label";
        //for(int i=0;i<stringcsv.length-1;i++)
        //       stringcsv[i]="value"+i;
        //writer.writeNext(stringcsv);
      try {
          Collection files = FileUtils.listFiles(folder, new String[]{"wav"}, true);

          for (Iterator iterator = files.iterator(); iterator.hasNext(); ) {
              File file = (File) iterator.next();
             System.out.println(file.getAbsolutePath());

              double[] showarray = FFTQ(file.getAbsolutePath());

              String[] strarr = new String[]{(Arrays.toString(showarray)).replaceAll("[^0-9.,]+",""),(file.getName()).replaceAll("[^A-Z]","")};
              writer.writeNext(strarr);
              //writer.writeNext(new String[]{file.getName()});
          }
      }catch(Exception e){
          e.printStackTrace();
      }




        //System.out.println(files);
        writer.close();
    }



    public void trainModel(String filepath) throws IOException {
        Dataset data = FileHandler.loadDataset(new File(filepath), 2205, ",");
        Dataset dataForClassification = FileHandler.loadDataset(new File(filepath),2205, ",");


        int correct = 0, wrong = 0;

        Classifier knn = new KNearestNeighbors(3);
        knn.buildClassifier(data);


        storeModel("E:/ObjFreqReq/Model/Model.dat",knn);

        Classifier model = loadModel("E:/ObjFreqReq/Model/Model.dat");

        for (Instance inst : dataForClassification) {
            Object predictedClassValue = model.classify(inst);
                 Object realClassValue = inst.classValue();
                if(predictedClassValue.equals(realClassValue))
                     correct++;
               else
                   wrong++;

            }
            System.out.println("Correct Predictions : "+correct+ "     Wrong Preddictions: " +wrong);
        Map<Object, PerformanceMeasure> pm = EvaluateDataset.testDataset(knn,dataForClassification);
        for(Object o : pm.keySet())
            System.out.println(o + ":  " + pm.get(o).getAccuracy());
            Format1.setText(String.valueOf(correct));
            Format2.setText(String.valueOf(wrong));

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

    public void storeModel (String filename,Classifier model){
        Serial.store(model,filename);

    }

    public Classifier loadModel (String fileName){
        Classifier model = (Classifier) Serial.load(fileName);
        return model;
    }

}

