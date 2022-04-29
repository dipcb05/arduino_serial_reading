package com.company;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Scanner;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import com.fazecast.jSerialComm.SerialPort;
public class Main {
    static SerialPort chosenPort;
    static int x = 0;
    public static void main(String[] args) {
        JFrame window = new JFrame();
        window.setTitle("Sensor Graph GUI");
        window.setSize(600, 400);
        window.setLayout(new BorderLayout());
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JComboBox<String> portList = new JComboBox<String>();
        JButton connectButton = new JButton("Connect");
        JPanel topPanel = new JPanel();
        topPanel.add(portList);
        topPanel.add(connectButton);
        window.add(topPanel, BorderLayout.NORTH);
        SerialPort[] portNames = SerialPort.getCommPorts();
        
        for(int i = 0; i < portNames.length; i++)
            portList.addItem(portNames[i].getSystemPortName());
        
        XYSeries series = new XYSeries("Light Sensor Readings");
        XYSeriesCollection dataset = new XYSeriesCollection(series);
        JFreeChart chart = ChartFactory.createXYLineChart("Sensor Readings", "Time (seconds)", "ADC Reading", dataset);
        window.add(new ChartPanel(chart), BorderLayout.CENTER);
        connectButton.addActionListener(new ActionListener(){
            @Override public void actionPerformed(ActionEvent arg0) {
                if(connectButton.getText().equals("Connect")) {
                    chosenPort = SerialPort.getCommPort(portList.getSelectedItem().toString());
                    chosenPort.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0, 0);
                    if(chosenPort.openPort()) {
                        connectButton.setText("Disconnect");
                        portList.setEnabled(false);
                    }
                    Thread thread = new Thread(){
                        @Override public void run() {
                            Scanner scanner = new Scanner(chosenPort.getInputStream());
                            while(scanner.hasNextLine()) {
                                try {
                                    String line = scanner.nextLine();
                                    int number = Integer.parseInt(line);
                                    series.add(x++, 1023 - number);
                                    window.repaint();
                                } catch(Exception e) {}
                            }
                            scanner.close();
                        }
                    };
                    thread.start();
                } else {
                    chosenPort.closePort();
                    portList.setEnabled(true);
                    connectButton.setText("Connect");
                    series.clear();
                    x = 0;
                }
            }
        });
        window.setVisible(true);
    }

}
