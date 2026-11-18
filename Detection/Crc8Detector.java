/**
 * CRC-8 (wariant CRC-8/SMBUS).
 *
 * Parametry:
 *   wielomian generujący: x^8 + x^2 + x + 1  ->  0x07
 *                         (bit x^8 jest pomijany, bo zawsze = 1)
 *   wartość początkowa:   0x00
 *   XOR na końcu:         0x00
 *   wartość kontrolna:    CRC("123456789") = 0xF4
 *
 * Idea: dane traktujemy jako jeden długi wielomian binarny, dopisujemy 8 zer
 * i dzielimy (modulo 2, czyli XOR-em) przez wielomian generujący.
 * Reszta z dzielenia (8 bitów) to suma kontrolna.
 *
 * Co wykrywa:
 *   - wszystkie błędy 1-bitowe,
 *   - wszystkie błędy z nieparzystą liczbą bitów (bo wielomian ma czynnik x+1),
 *   - wszystkie serie (burst) błędów o długości <= 8 bitów,
 *   - dłuższe serie / losowe błędy z prawdopodobieństwem ok. 1 - 1/256 (99,6%).
 */
public class Crc8Detector implements ErrorDetector {

    private static final int POLYNOMIAL = 0x07;
    private static final int INITIAL_VALUE = 0x00;

    @Override
    public String getName() {
        return "CRC-8";
    }

    @Override
    public int getChecksumBits() {
        return 8;
    }

    @Override
    public long calculate(byte[] data) {
        int crc = INITIAL_VALUE;

        for (byte b : data) {
            // Wsuwamy kolejny bajt danych do rejestru
            crc ^= (b & 0xFF);

            // Przetwarzamy jego 8 bitów, od najstarszego
            for (int i = 0; i < 8; i++) {
                if ((crc & 0x80) != 0) {
                    // Najstarszy bit = 1 -> "odejmujemy" (XOR) wielomian
                    crc = (crc << 1) ^ POLYNOMIAL;
                } else {
                    crc = crc << 1;
                }
                crc &= 0xFF; // trzymamy rejestr na 8 bitach
            }
        }

        return crc;
    }
}
