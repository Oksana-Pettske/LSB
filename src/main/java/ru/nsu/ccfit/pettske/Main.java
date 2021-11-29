package ru.nsu.ccfit.pettske;

import ru.nsu.ccfit.pettske.Exceptions.DecodeException;
import ru.nsu.ccfit.pettske.Exceptions.EncodeException;

import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        if (args[0].equals("-emb")) {
            if (args.length == 4) {
                File imageFile = new File(args[1]);
                File textFile = new File(args[2]);
                File resultImageFile = new File(args[3]);
                LSB lsb = new LSB(imageFile, textFile, resultImageFile);
                try {
                    lsb.embed();
                } catch (IOException | EncodeException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println(
                        "To embed text from file enter: \n" +
                                "* GIF-image file. \n" +
                                "* Text file. \n" +
                                "* Result GIF-image file. \n" +
                                "To embed text from console enter: \n" +
                                "* GIF-image file. \n" +
                                "* Result GIF-image file. \n");
            }
        } else if (args[0].equals("-ext")) {
            if (args.length == 2) {
                File imageFile = new File(args[1]);
                LSB lsb = new LSB(imageFile);
                try {
                    System.out.println("Extracted text: \n" + lsb.extract());
                } catch (IOException | DecodeException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("To extract text from file enter: \n" +
                        "* GIF-image file. \n");
            }
        } else {
            System.out.println("To embed text enter key [-emb]. \n" +
                    "To extract text enter key [-ext]. \n");
        }
    }
}
