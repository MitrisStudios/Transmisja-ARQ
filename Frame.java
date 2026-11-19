import java.util.Arrays;
import Detection.ErrorDetector;


public class Frame {
    public static final byte DATA = 0;
    public static final byte ACK  = 1;
    public static final byte NACK = 2;
    public static final int HEADER_SIZE = 4; // typ (1 B) + numer sekwencyjny (2 B) + długość użyteczna (1 B)

    // Ciało ramki = 4 B nagłówka + 53 B danych = 57 B = 456 bitów
    // = dokładnie 8 bloków po 57 bitów dla cyklicznego kodu Hamminga (63,57).
    public static final int PAYLOAD_SIZE = 53;

    public byte type;
    public int seqNum;
    public int dataLen;
    public byte[] payload;
    public byte[] checksum;

    public Frame(byte type, int seqNum, byte[] rawData) {
        this.type = type;
        this.seqNum = seqNum;
        this.checksum = new byte[0];

        if (rawData != null && rawData.length > PAYLOAD_SIZE) {
            // Bez tego dane byłyby po cichu obcięte, a ramka i tak przeszłaby weryfikację
            throw new IllegalArgumentException(
                    "Za dużo danych w ramce: " + rawData.length + " B (maks. " + PAYLOAD_SIZE + " B)");
        }

        if (type == DATA && rawData != null) {
            this.dataLen = rawData.length;
            this.payload = Arrays.copyOf(rawData, PAYLOAD_SIZE); // dopełnia zerami do PAYLOAD_SIZE bajtów
        } else {
            this.dataLen = 0;
            this.payload = new byte[0];
        }
    }

    // Część chroniona sumą kontrolną (4 bajty nagłówka + dane)
    public byte[] getBody() {
        byte[] body = new byte[HEADER_SIZE + payload.length];
        body[0] = type;
        body[1] = (byte) (seqNum >> 8);
        body[2] = (byte) seqNum;
        body[3] = (byte) dataLen;
        System.arraycopy(payload, 0, body, HEADER_SIZE, payload.length);
        return body;
    }

    // Zamiana całej ramki na bajty do wysłania w kanał.
    // Suma kontrolna jest liczona tutaj, z całego ciała (nagłówek + dane z dopełnieniem),
    // więc nadawca i odbiorca zawsze używają tej samej procedury.
    public byte[] toBytes(ErrorDetector detector) {
        byte[] body = getBody();
        int checksumLen = checksumBytes(detector);
        this.checksum = longToBytes(detector.calculate(body), checksumLen);

        byte[] full = Arrays.copyOf(body, body.length + checksumLen);
        System.arraycopy(checksum, 0, full, body.length, checksumLen);
        return full;
    }

    // Odtworzenie ramki z bajtów odebranych z kanału.
    // Zwraca null, jeśli wykryto błąd (symulacja liczy to jako "błąd wykryty").
    //
    // Kolejność jest ważna: najpierw sprawdzamy sumę kontrolną, dopiero potem czytamy pola.
    // Dopóki suma nie jest sprawdzona, żadne pole (także typ) nie jest wiarygodne.
    public static Frame fromBytes(byte[] bytes, ErrorDetector detector) {
        int checksumLen = checksumBytes(detector);
        if (bytes == null || bytes.length < HEADER_SIZE + checksumLen) return null;

        // 1. Podział: długość sumy kontrolnej wynika z detektora, a nie z (niesprawdzonego) nagłówka
        int bodyLen = bytes.length - checksumLen;
        byte[] body = Arrays.copyOf(bytes, bodyLen);
        byte[] receivedChecksum = Arrays.copyOfRange(bytes, bodyLen, bytes.length);

        // 2. Weryfikacja
        if (!detector.verify(body, bytesToLong(receivedChecksum))) return null;

        // 3. Odczyt pól i sprawdzenie, czy mają sens
        byte type = body[0];
        if (type != DATA && type != ACK && type != NACK) return null;

        int expectedPayload = (type == DATA) ? PAYLOAD_SIZE : 0;
        if (bodyLen != HEADER_SIZE + expectedPayload) return null; // typ nie pasuje do długości ramki

        int dataLen = body[3] & 0xFF;
        if (dataLen > expectedPayload) return null;

        int seqNum = ((body[1] & 0xFF) << 8) | (body[2] & 0xFF);
        Frame frame = new Frame(type, seqNum, null);
        frame.dataLen = dataLen;
        frame.payload = Arrays.copyOfRange(body, HEADER_SIZE, bodyLen);
        frame.checksum = receivedChecksum;
        return frame;
    }

    // Ile pełnych bajtów zajmuje suma kontrolna (parzystość 1 bit -> 1 B, Hamming 48 bitów -> 6 B)
    private static int checksumBytes(ErrorDetector detector) {
        return (detector.getChecksumBits() + 7) / 8;
    }

    // long -> n bajtów, najstarszy bajt pierwszy (big-endian)
    private static byte[] longToBytes(long value, int n) {
        byte[] result = new byte[n];
        for (int i = n - 1; i >= 0; i--) {
            result[i] = (byte) value;
            value >>>= 8;
        }
        return result;
    }

    // n bajtów -> long, odwrotność longToBytes
    private static long bytesToLong(byte[] bytes) {
        long value = 0;
        for (byte b : bytes) {
            value = (value << 8) | (b & 0xFF);
        }
        return value;
    }

    // Zwraca właściwe bajty pliku (odcina zera dopisane na końcu)
    public byte[] getUsefulData() {
        return Arrays.copyOf(payload, Math.min(dataLen, payload.length));
    }
}