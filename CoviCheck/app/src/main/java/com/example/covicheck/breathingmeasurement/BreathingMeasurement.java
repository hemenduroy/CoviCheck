package com.example.covicheck.breathingmeasurement;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.covicheck.R;
import com.example.covicheck.heartratemeasurement.parsingthreads.PeakDetect;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class BreathingMeasurement extends AppCompatActivity {
    private static int RPM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_breathing_measurement);
        Button breatheButton = findViewById(R.id.button2);


        breatheButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Scanner scanner = null;
                try {
                    scanner = new Scanner(new File("/data/user/0/com.example.covicheck/CSVBreathe19.csv"));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                ArrayList<Double> values = new ArrayList<>();
                int k = 0;
                while (scanner.hasNextLine()) {
                    values.add(k, Double.parseDouble(scanner.nextLine().split("\n")[0]));
                    k++;
                }
                scanner.close();
                System.out.println("size of values " + values.size());
                //for (int i = 0; i < values.size(); i++)
                //    System.out.println("value " + values.get(i));

                //peak detection from heart rate program
                ArrayList<Double> peaks;
                //getting movDiff
                ArrayList<Double> movDiff = new ArrayList<>();
                for(int x=0;x<values.size()-1;x++)
                    movDiff.add(x,values.get(x)-values.get(x+1));
                System.out.println("size of movDiff " + movDiff.size());

                //getting movAvg of movDiff
                int averageWindow = 15;
                ArrayList<Double> movAvgmovDiff = new ArrayList<>();
                //Calculating moving average with group size of 5
                for (int i = 0; i < movDiff.size(); i++) {
                    Double temp = 0.0;
                    //System.out.println(index);
                    for (int j=i; (j < i + averageWindow && j < movDiff.size()); j++) {
                        temp += movDiff.get(j);
                    }
                    //averaging temp across averageWindow
                    temp/=averageWindow;
                    //System.out.println(temp);
                    movAvgmovDiff.add(i, temp);
                }
                System.out.println("size of movAvgmovDiff : " + movAvgmovDiff.size());
                //diff(movAvgmovDiff)
                int oldSize = movAvgmovDiff.size()-1;
                for(int x=0;x<movAvgmovDiff.size()-1;x++)
                    movAvgmovDiff.set(x,movAvgmovDiff.get(x)-movAvgmovDiff.get(x+1));
                movAvgmovDiff.remove(oldSize);
                System.out.println("size of diff(movAvgmovDiff) : " + movAvgmovDiff.size());
                //calc peaks
                /*PeakDetect breathePeakDetect = new PeakDetect(movAvgmovDiff);
                Thread t = new Thread(breathePeakDetect);
                t.start();
                try {
                    t.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                peaks = breathePeakDetect.RPeakDetection();
                System.out.println("size of peaks " + peaks.size());
                for (int x=0;x<peaks.size();x++)
                    System.out.println("peak " + peaks.get(x));*/
                //calc peaks end
                int fps=64,timeW=10,sampleSize=fps*timeW,index=0,count=0;
                ArrayList<Integer> Br = new ArrayList<>();
                while (index< movAvgmovDiff.size()-sampleSize) {
                    ArrayList<Double> sampleData = new ArrayList<>();
                    for (int z=index;z<index+sampleSize;z++)
                        sampleData.add(movAvgmovDiff.get(z));

                    PeakDetect peakDetect = new PeakDetect(sampleData);
                    //thread
                    Thread t3 = new Thread(peakDetect);
                    t3.start();
                    try {
                        t3.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("size of sampledata " + sampleData.size());
                    peaks=peakDetect.RPeakDetection();
                    //thread
                    System.out.println("size of peaks : " + peaks.size());
                    //Br.add(count,60*peaks.size()/timeW); //60 * peaks per second = peaks per minute = respiration rate
                    Br.add(count,60*peaks.size()/timeW);
                    index+=sampleSize;
                    count++;
                }
                System.out.println("Br size " + Br.size());
                //System.out.println("Plotted " + x + " points");
                //int RPM=0;
                for(int tempcount=0; tempcount<Br.size();tempcount++) {
                    System.out.println("Br : " + Br.get(tempcount));
                    RPM+= Br.get(tempcount);
                }
                RPM/= Br.size();
                System.out.println("RPM : " + RPM);
                //thread
                //System.out.println("size of peaks : " + peaks.size());

            }
        });

    }
    public static int getRPMvalue() {
        return RPM;
    }

}