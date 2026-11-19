package Detection;

/*
  Wspólny interfejs dla wszystkich kodów detekcyjnych (parzystość, CRC8/32, Hamming).
  Nadawca liczy sumę kontrolną przez calculate() i dopisuje ją do ramki,
  odbiorca sprawdza ją przez verify().
 */
public interface ErrorDetector {

    /** Nazwa kodu, np. do wypisywania wyników. */
    String getName();

    /** Ile bitów zajmuje suma kontrolna (parzystość: 1, CRC8: 8, ...). */
    int getChecksumBits();

    /** Liczy sumę kontrolną dla danych. */
    long calculate(byte[] data);

    /** Zwraca true, jeśli suma kontrolna pasuje do danych (nie wykryto błędu). */
    default boolean verify(byte[] data, long checksum) {
        return calculate(data) == checksum;
    }
}
