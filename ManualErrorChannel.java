import java.util.Arrays;

/*
  Kanał testowy do Kroku 1: wprowadza deterministyczne przekłamania
  na dokładnie wskazanych pozycjach bitowych ramki.
 */
public class ManualErrorChannel implements Channel {
    private final int[] errorPositions;

    /**
      @param positions pozycje bitów do odwrócenia (liczone od 0, od najstarszego bitu);
                       brak argumentów oznacza kanał bezbłędny.
     */
    public ManualErrorChannel(int... positions) {
        this.errorPositions = (positions != null) ? positions.clone() : new int[0];
    }

    @Override
    public byte[] transmit(byte[] data) {
        if (data == null) {
            throw new IllegalArgumentException("Przesyłana tablica bajtów nie może być null.");
        }

        // Pracujemy na kopii, aby zachować oryginał do porównania liczby zmienionych bitów
        byte[] corrupted = data.clone();
        int totalBits = corrupted.length * 8;

        for (int pos : errorPositions) {
            if (pos < 0 || pos >= totalBits) {
                throw new IllegalArgumentException(
                        "Pozycja bitu " + pos + " wykracza poza rozmiar ramki (dozwolony zakres: 0.." + (totalBits - 1) + ")."
                );
            }
            Channel.flipBit(corrupted, pos);
        }

        return corrupted;
    }

    @Override
    public String toString() {
        return errorPositions.length == 0
                ? "brak błędu"
                : "bity " + Arrays.toString(errorPositions);
    }
}
