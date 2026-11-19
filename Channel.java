/**
 * Wspólny interfejs dla wszystkich modeli kanałów komunikacyjnych
 * (ręczne błędy, kanał BSC, kanał Gilberta-Elliotta).
 */
public interface Channel {

    /**
     * Przesyła ramkę przez kanał, potencjalnie wprowadzając przekłamania bitów.
     * Implementacja nie może modyfikować tablicy wejściowej (musi pracować na kopii).
     */
    byte[] transmit(byte[] data);

    /**
     * Odwraca pojedynczy bit na pozycji i (numeracja od 0, od najstarszego bitu w bajcie).
     */
    static void flipBit(byte[] data, int i) {
        data[i / 8] ^= (byte) (1 << (7 - i % 8));
    }

    /**
     * Zlicza liczbę bitów, na których różnią się dwie tablice bajtów tej samej długości.
     */
    static int countDifferingBits(byte[] a, byte[] b) {
        if (a == null || b == null || a.length != b.length) {
            throw new IllegalArgumentException("Tablice muszą istnieć i mieć tę samą długość.");
        }
        int diff = 0;
        for (int j = 0; j < a.length; j++) {
            diff += Integer.bitCount((a[j] ^ b[j]) & 0xFF);
        }
        return diff;
    }
}