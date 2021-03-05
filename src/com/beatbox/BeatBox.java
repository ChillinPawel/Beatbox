package com.beatbox;

import javax.sound.midi.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;

public class BeatBox {

    private JFrame theFrame;
    private JPanel mainPanel;
    private ArrayList<JCheckBox> checkboxList;

    private Sequencer sequencer;
    private Sequence sequence;
    private Track track;

    private ArrayList<Instrument> instrumentList;
//    int[] instruments = {35, 42, 46, 38, 49, 39, 50, 60, 70, 72, 64, 56, 58, 47, 67, 63};

    public static void main(String[] args) {
        new BeatBox().buildGUI();
    }

    public void buildGUI(){
        theFrame = new JFrame("Cyber Beatbox");
        theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        BorderLayout layout = new BorderLayout();
        JPanel background = new JPanel(layout);
        background.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        checkboxList = new ArrayList<>();
        Box buttonBox = new Box(BoxLayout.Y_AXIS);

        JButton start = new JButton("Start");
        start.addActionListener(new MyStartListener());
        buttonBox.add(start);

        JButton stop = new JButton("Stop");
        stop.addActionListener(new MyStopListener());
        buttonBox.add(stop);

        JButton upTempo = new JButton("Tempo Up");
        upTempo.addActionListener(new MyUpTempoListener());
        buttonBox.add(upTempo);

        JButton downTempo = new JButton("Tempo Down");
        downTempo.addActionListener(new MyDownTempoListener());
        buttonBox.add(downTempo);

        JButton save = new JButton("Send It");
        save.addActionListener(new MySendListener());
        buttonBox.add(save);

        JButton load = new JButton("Load Pattern");
        load.addActionListener(new MyReadInListener());
        buttonBox.add(load);

        instrumentList = new ArrayList<Instrument>(loadInstruments("Instruments.txt"));
        Box nameBox =  new Box(BoxLayout.Y_AXIS);
        for(Instrument instrument : instrumentList){
            nameBox.add(new Label(instrument.getName()));
        }

        background.add(BorderLayout.EAST, buttonBox);
        background.add(BorderLayout.WEST, nameBox);

        theFrame.getContentPane().add(background);

        GridLayout grid = new GridLayout(16, 16);
        grid.setVgap(1);
        grid.setHgap(2);
        mainPanel = new JPanel(grid);
        background.add(BorderLayout.CENTER, mainPanel);

        for(int i=0; i<256; i++){
            JCheckBox c = new JCheckBox();
            c.setSelected(false); // raczej niepotrzebna linijka
            checkboxList.add(c);
            mainPanel.add(c);
        }

        setUpMidi();

        theFrame.setBounds(50, 50, 300, 300);
        theFrame.pack();
        theFrame.setVisible(true);
    }

    private ArrayList<Instrument> loadInstruments (String fileName){

        File file = new File(fileName);
        ArrayList<Instrument> instruments = new ArrayList<Instrument>();

        try(BufferedReader reader = new BufferedReader(new FileReader(file))){

            String line;

            while ((line = reader.readLine()) != null){
                String[] lineParts = line.split("\\.");

                Instrument instrument = new Instrument(lineParts[1].trim(),Integer.parseInt(lineParts[0]));
                instruments.add(instrument);
            }

        } catch(FileNotFoundException e){
            System.out.println("FileNotFoundException: " + e.getMessage());
        } catch(IOException e){
            System.out.println("IOException: " + e.getMessage());
        }

        return instruments;
    }

    private void setUpMidi(){
        try{
            sequencer = MidiSystem.getSequencer();
            sequencer.open();
            sequence = new Sequence(Sequence.PPQ, 4);
            track = sequence.createTrack();
            sequencer.setTempoInBPM(120);
        } catch(MidiUnavailableException | InvalidMidiDataException e){
            e.printStackTrace();
        }
    }

    private void buildTrackAndStart(){
        ArrayList<Integer> trackList;

        sequence.deleteTrack(track);
        track = sequence.createTrack();
        int row = 0;

        for(Instrument instrument : instrumentList){
            trackList = new ArrayList<>();

            int key = instrument.getId();

            for(int i = 0; i < 16; i++){

                JCheckBox jc = (JCheckBox) checkboxList.get(i + (16*row));
                if(jc.isSelected()){
                    trackList.add(key);
                } else {
                    trackList.add(0);
                }

            }

            makeTracks(trackList);
            track.add(makeEvent(176, 1, 127, 0, 16));

            row++;

        }

        track.add(makeEvent(192, 9, 1, 0, 15));

        try{
            sequencer.setSequence(sequence);
            sequencer.setLoopCount(sequencer.LOOP_CONTINUOUSLY);
            sequencer.start();
            sequencer.setTempoInBPM(120);
        } catch(InvalidMidiDataException e){
            e.printStackTrace();
        }
    }

    private class MyStartListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            buildTrackAndStart();
        }
    }

    private class MyStopListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            sequencer.stop();
        }
    }

    private class MySendListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            boolean[] checkboxState = new boolean[256];

            for(int i=0; i<256; i++){
                JCheckBox check = (JCheckBox) checkboxList.get(i);
                if(check.isSelected()){
                    checkboxState[i] = true;
                }
            }

            JFileChooser saveFile = new JFileChooser();
            saveFile.showSaveDialog(theFrame);

            try(ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(saveFile.getSelectedFile()))){
                os.writeObject(checkboxState);
            } catch(IOException ez){
                ez.printStackTrace();
            }
        }
    }

    private class MyReadInListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
            boolean[] checkboxState = null;

            JFileChooser loadFile = new JFileChooser();
            loadFile.showSaveDialog(theFrame);

            try(ObjectInputStream is = new ObjectInputStream(new FileInputStream(loadFile.getSelectedFile()))){
                checkboxState = (boolean[]) is.readObject();

            } catch(IOException | ClassNotFoundException ex){
                ex.printStackTrace();
            }

            for(int i=0; i<256; i++){
                JCheckBox check = (JCheckBox) checkboxList.get(i);
                check.setSelected(checkboxState[i]);
            }

            sequencer.stop();
            buildTrackAndStart();
        }
    }

    private class MyUpTempoListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            float tempoFactor = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float)(tempoFactor * 1.03));
        }
    }

    private class MyDownTempoListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            float tempoFactor = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float)(tempoFactor * .97));
        }
    }

    private void makeTracks(ArrayList<Integer> list){
        for(Integer key : list){
            if(key != 0){
                track.add(makeEvent(144, 9, key, 100, list.indexOf(key)));
                track.add(makeEvent(128, 9, key, 100, (list.indexOf(key) + 1)));
            }
        }
    }

    private MidiEvent makeEvent(int cmd, int chan, int one, int two, int tick){
        MidiEvent event = null;
        try{
            event = new MidiEvent(new ShortMessage(cmd,chan,one,two),tick);
        } catch(InvalidMidiDataException e){
            e.printStackTrace();
        }
        return event;
    }
}

