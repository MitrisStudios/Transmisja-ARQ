package Detection;

/*
  Bit parzystości (parzysta parzystość, "even parity").
 
  Suma kontrolna to jeden bit dobrany tak, żeby łączna liczba jedynek
  w danych + bicie parzystości była parzysta:
    - nieparzysta liczba jedynek w danych -> bit parzystości = 1
    - parzysta liczba jedynek w danych    -> bit parzystości = 0
 
  Wykrywa każdą NIEPARZYSTĄ liczbę przekłamanych bitów (1, 3, 5, ...).
  NIE wykrywa parzystej liczby przekłamań (2, 4, ...), bo wtedy
  liczba jedynek zmienia się o liczbę parzystą i bit się zgadza.
 */
public class ParityDetector implements ErrorDetector {

    @Override
    public String getName() {
        return "Parzystość";
    }

    @Override
    public int getChecksumBits() {
        return 1;
    }

    @Override
    public long calculate(byte[] data) {
        int ones = 0;
        for (byte b : data) {
            // & 0xFF: byte w Javie jest ze znakiem, maskujemy do 8 bitów bez znaku
            ones += Integer.bitCount(b & 0xFF);
        }
        return ones % 2;
    }
}
