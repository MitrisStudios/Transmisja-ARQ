import java.util.Arrays;

public class Frame {
    public static final byte DATA = 0;
    public static final byte ACK  = 1;
    public static final byte NACK = 2;
    public static final int PAYLOAD_SIZE = 63;

    public byte type;
    public int seqNum;
    public int dataLen;
    public byte[] payload;
    public byte[] checksum;

    public Frame(byte type, int seqNum, byte[] rawData) {
        this.type = type;
        this.seqNum = seqNum;
        this.checksum = new byte[0];

        if (type == DATA && rawData != null) {
            this.dataLen = rawData.length;
            this.payload = Arrays.copyOf(rawData, PAYLOAD_SIZE); // dopełnia zerami do 63 bajtów
        } else {
            this.dataLen = 0;
            this.payload = new byte[0];
        }
    }

    // Część chroniona sumą kontrolną (4 bajty nagłówka + dane)
    public byte[] getBody() {
        byte[] body = new byte[4 + payload.length];
        body[0] = type;
        body[1] = (byte) (seqNum >> 8);
        body[2] = (byte) seqNum;
        body[3] = (byte) dataLen;
        System.arraycopy(payload, 0, body, 4, payload.length);
        return body;
    }

    // Zamiana całej ramki na bajty do wysłania w kanał
    public byte[] toBytes() {
        byte[] body = getBody();
        byte[] full = Arrays.copyOf(body, body.length + checksum.length);
        System.arraycopy(checksum, 0, full, body.length, checksum.length);
        return full;
    }

    // Odtworzenie ramki z bajtów odebranych z kanału
    public static Frame fromBytes(byte[] bytes) {
        byte type = bytes[0];
        int seqNum = ((bytes[1] & 0xFF) << 8) | (bytes[2] & 0xFF);
        int dataLen = bytes[3] & 0xFF;
        int payloadSize = (type == DATA) ? PAYLOAD_SIZE : 0;

        if (bytes.length < 4 + payloadSize) return null; // ramka ACK przekłamana w kanał na DATA

        Frame frame = new Frame(type, seqNum, null);
        frame.dataLen = dataLen;
        frame.payload = Arrays.copyOfRange(bytes, 4, 4 + payloadSize);
        frame.checksum = Arrays.copyOfRange(bytes, 4 + payloadSize, bytes.length);
        return frame;
    }

    // Zwraca właściwe bajty pliku (odcina zera dopisane na końcu)
    public byte[] getUsefulData() {
        return Arrays.copyOf(payload, Math.min(dataLen, payload.length));
    }
}