package com.pawel;

import javax.sound.midi.*;

public class MiniMidiPlayer {
    public void play(int instrument, int note) {
        try {

            Sequencer player = MidiSystem.getSequencer();
            Sequence seq = new Sequence(Sequence.PPQ, 4);
            Track track = seq.createTrack();

            ShortMessage first = new ShortMessage(192, 1, instrument, 0);
            track.add(new MidiEvent(first, 1));

            ShortMessage a = new ShortMessage(ShortMessage.NOTE_ON, 1, note, 100);
            track.add(new MidiEvent(a, 1));

            ShortMessage b = new ShortMessage(ShortMessage.NOTE_OFF, 1, note, 100);
            track.add(new MidiEvent(b, 16));

            player.open();
            player.setSequence(seq);
            player.start();

        } catch (MidiUnavailableException e) {
            System.out.println("MidiUnavailableException: " + e.getMessage());
        } catch (InvalidMidiDataException e) {
            System.out.println("InvalidMidiDataException: " + e.getMessage());
        }
    }

}
