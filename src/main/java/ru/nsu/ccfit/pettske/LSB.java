package ru.nsu.ccfit.pettske;

import ru.nsu.ccfit.pettske.Exceptions.DecodeException;
import ru.nsu.ccfit.pettske.Exceptions.EncodeException;

import java.io.*;

public class LSB {
    private final File imageFile;
    private File resultImageFile;
    private File textFile;

    public LSB(File imageFile, File textFile, File resultImageFile) {
        this.imageFile = imageFile;
        this.textFile = textFile;
        this.resultImageFile = resultImageFile;
    }

    public LSB(File imageFile) {
        this.imageFile = imageFile;
    }

    protected byte[] checkBytes = new byte[] {0, 0, 0, 1, 1, 0, 1, 1};
    protected int firstLSBit = 0;
    protected int secondLSBit = 1;

    public void embed() throws IOException, EncodeException {
        System.out.println("Start embedding.");

        byte[] imageBytes = new byte[(int) imageFile.length()];
        InputStream inputStream = new FileInputStream(imageFile);
        inputStream.read(imageBytes);
        inputStream.close();

        if (!(new String(imageBytes, 0, 6)).equals("GIF89a")) {
            throw new EncodeException("Input image has wrong format.");
        }

        byte paletteSize = getPaletteSize(imageBytes);

        int possibleTextLength = getPossibleMessageLength(paletteSize);
        if (possibleTextLength < textFile.length()) {
            throw new EncodeException("Text is too big.");
        }

        int n = 13;
        n = writeBytes(imageBytes, n, checkBytes);

        byte[] textLengthBytes = toBitArray((byte) textFile.length());
        n = writeBytes(imageBytes, n, textLengthBytes);

        byte[] textBytes = new byte[(int) textFile.length()];
        InputStream inputStream1 = new FileInputStream(textFile);
        inputStream1.read(textBytes);
        inputStream1.close();

        for (int i = 0; i < textBytes.length; i++) {
            byte[] textBits = toBitArray(textBytes[i]);
            n = writeBytes(imageBytes, n, textBits);
        }

        OutputStream outputStream = new FileOutputStream(resultImageFile);
        outputStream.write(imageBytes);
        outputStream.close();

        System.out.println("Embedding success.");
    }

    public String extract() throws IOException, DecodeException {
        System.out.println("Start extracting.");

        byte[] imageBytes = new byte[(int) imageFile.length()];
        InputStream is = new FileInputStream(imageFile);
        is.read(imageBytes);
        is.close();

        if (!(new String(imageBytes, 0, 6)).equals("GIF89a")) {
            throw new DecodeException("Input image has wrong format.");
        }

        byte paletteSize = getPaletteSize(imageBytes);
        int possibleTextLength = getPossibleMessageLength(paletteSize);

        int n = 13;

        byte[] checkBits = new byte[checkBytes.length];
        n = readByte(imageBytes, n, 4, checkBits);
        byte checkByte = toByte(checkBits);

        if (checkByte != toByte(checkBytes)) {
            throw new DecodeException("Check sequence is incorrect.");
        }

        byte[] textLengthBits = new byte[8];
        n = readByte(imageBytes, n, 4, textLengthBits);
        byte bTextLength = toByte(textLengthBits);
        int textLength = Byte.toUnsignedInt(bTextLength);

        if (textLength < 0) {
            throw new DecodeException("Decoded text length is less than 0");
        }
        if (possibleTextLength < textLength) {
            throw new DecodeException("Decoded message length is less than possible message length.");
        }

        byte[] extractedText = new byte[textLength];
        for (int i = 0; i < extractedText.length; i++) {
            byte[] textBits = new byte[8];
            n = readByte(imageBytes, n, textBits.length / 2, textBits);
            extractedText[i] = toByte(textBits);
        }

        System.out.println("Extract success.");
        return new String(extractedText);
    }

    private byte getPaletteSize(byte[] imageBytes) {
        byte[] tenByte = toBitArray(imageBytes[10]);
        return toByte(new byte[]{tenByte[0], tenByte[1], tenByte[2]});
    }

    private int getPossibleMessageLength(byte paletteSize) {
        int colorCount = (int) Math.pow(2, paletteSize + 1);
        int possibleMessageLength = colorCount * 3 / 4;
        return possibleMessageLength - 2;
    }

    private int writeBytes(byte[] writeTo, int n, byte[] bytes) {
        for (int i = 0; i < bytes.length / 2; i++) {
            byte[] bitArray = toBitArray(writeTo[n]);
            bitArray[firstLSBit] = bytes[2 * i];
            bitArray[secondLSBit] = bytes[2 * i + 1];
            writeTo[n] = toByte(bitArray);
            n++;
        }
        return n;
    }

    private int readByte(byte[] readFrom, int n, int boarder, byte[] bits) {
        for (int i = 0; i < boarder; i++) {
            byte[] bitArray = toBitArray(readFrom[n]);
            bits[2 * i] = bitArray[firstLSBit];
            bits[2 * i + 1] = bitArray[secondLSBit];
            n++;
        }
        return n;
    }

    public static byte[] toBitArray(byte value) {
        byte[] bits = new byte[] {0,0,0,0,0,0,0,0};
        if (value < 0) {
            value = (byte)(value + Byte.MAX_VALUE + 1);
            bits[7] = 1;
        }
        int i = 0;
        while (true) {
            bits[i] = (byte)(value % 2);
            if ( (value != 1) && (value != 0) ) {
                value /= 2;
                i++;
            } else {
                break;
            }
        }
        bits[i] = value;
        return bits;
    }

    public static byte toByte(byte[] bitArray) {
        byte res = 0;
        if (bitArray.length > 8) {
            return res;
        }
        int r = 0;
        for (int i = 0; i < bitArray.length; i++) {
            r += bitArray[i]*Math.pow(2, i);
        }
        res = (byte)r;
        return res;
    }
}
