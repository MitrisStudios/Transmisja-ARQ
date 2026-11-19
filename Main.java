import Detection.*;
public class Main {
    public static void main(String[] args) {
        // Mapa bitów ramki DATA
        System.out.println("MAPA BITÓW RAMKI DATA:");
        System.out.println("TYP: 0-7 | NR SEKW.: 8-23 | DŁUGOŚĆ: 24-31 | DANE: 32-455 | SUMA: 456-463\n");

        String tekst = "Ala ma kota";
        ErrorDetector[] detektory = { new ParityDetector(), new Crc8Detector() };

        String[] nazwy = {
                "brak błędu",
                "1 bit (dane)",
                "2 bity",
                "3 bity",
                "1 bit w polu TYP",
                "1 bit w dopełnieniu",
                "1 bit w sumie kontrolnej",
                "seria 8 bitów",
                "2 bity odległe o 127"
        };

        int[][] pozycje = {
                {},
                {100},
                {100, 200},
                {100, 200, 300},
                {7},
                {300},
                {460},
                {200, 201, 202, 203, 204, 205, 206, 207},
                {40, 167}
        };

        System.out.printf("%-12s | %-25s | %-14s | %s%n", "Kod", "Scenariusz", "Zmienione bity", "Wynik");
        System.out.println("-".repeat(72));

        for (ErrorDetector detektor : detektory) {
            for (int i = 0; i < nazwy.length; i++) {
                testuj(tekst, detektor, nazwy[i], pozycje[i]);
            }
            System.out.println("-".repeat(72));
        }
    }

    private static void testuj(String tekst, ErrorDetector detektor, String scenariusz, int... bity) {
        // 1. Nadawca buduje ramkę i dokleja sumę kontrolną
        Frame ramka = new Frame(Frame.DATA, 0, tekst.getBytes());
        byte[] wyslane = ramka.toBytes(detektor);

        // 2. Kanał psuje wskazane bity na kopii ramki
        Channel kanal = new ManualErrorChannel(bity);
        byte[] odebrane = kanal.transmit(wyslane);

        // 3. Odbiornik sprawdza sumę kontrolną (null = wykryto błąd)
        Frame wynik = Frame.fromBytes(odebrane, detektor);
        int roznice = Channel.countDifferingBits(wyslane, odebrane);

        // 4. Ocena wyniku
        String werdykt;
        if (roznice == 0 && wynik != null) {
            werdykt = "OK";
        } else if (roznice > 0 && wynik == null) {
            werdykt = "WYKRYTO";
        } else if (roznice > 0) {
            werdykt = "NIEWYKRYTY";
        } else {
            werdykt = "BŁĄD IMPLEMENTACJI";
        }

        System.out.printf("%-12s | %-25s | %-14d | %s%n", detektor.getName(), scenariusz, roznice, werdykt);
    }
}