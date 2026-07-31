package hr.bill.spring_bill.dto.eposlovanje.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Getter
public enum UnitOfMeasure {
    Bg("BG", "VR", "vreća"),
    Bx("BX", "kut", "kutija"),
    Ca("CA", "KAN", "konzerva"),
    Day("DAY", "DAN", "dan"),
    Grm("GRM", "g", "gram"),
    H87("H87", "kom", "komad"),
    Hur("HUR", "sat", "sat"),
    Kgm("KGM", "kg", "kilogram"),
    Kmt("KMT", "km", "kilometar"),
    Lm("LM", "TM", "tekući metar"),
    Ls("LS", "PAU", "paušal"),
    Ltr("LTR", "L", "litra"),
    Min("MIN", "min", "minuta"),
    Mlt("MLT", "ml", "mililitar"),
    Mtk("MTK", "M2", "metar kvadratni"),
    Mtq("MTQ", "M3", "metar kubični"),
    Mtr("MTR", "m", "metar"),
    Paket("PK", "PAK", "paket"),
    Komplet("PK", "kpl", "komplet"),
    SetUnit("PK", "set", "set"),
    Pr("PR", "PAR", "par"),
    Tne("TNE", "t", "tona"),
    E48("E48", "usl", "usluga"),
    Ea("EA", "jed", "jedinica");

    private final String internationalCode;
    private final String croatianCode;
    private final String displayName;

    public static UnitOfMeasure fromInternationalCode(String code) {
        return switch (code.trim()) {
            case "BG" -> Bg;
            case "BX" -> Bx;
            case "CA" -> Ca;
            case "DAY" -> Day;
            case "GRM" -> Grm;
            case "H87" -> H87;
            case "HUR" -> Hur;
            case "KGM" -> Kgm;
            case "KMT" -> Kmt;
            case "LM" -> Lm;
            case "LS" -> Ls;
            case "LTR" -> Ltr;
            case "MIN" -> Min;
            case "MLT" -> Mlt;
            case "MTK" -> Mtk;
            case "MTQ" -> Mtq;
            case "MTR" -> Mtr;
            case "PK" -> Paket;
            case "PR" -> Pr;
            case "TNE" -> Tne;
            case "E48" -> E48;
            case "EA" -> Ea;
            default -> null;
        };
    }

    public static UnitOfMeasure defaultH87() {
        return H87;
    }

    public static List<UnitOfMeasureEntry> allEntries() {
        return Stream.of(values())
                .map(u -> new UnitOfMeasureEntry(u.name(), u.getDisplayName()))
                .toList();
    }
}