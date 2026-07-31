package hr.bill.spring_bill.dto.web.bill.info;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Schema(description = "Unit of measure for a bill item, merged across B2B and B2C bills")
@RequiredArgsConstructor
@Getter
public enum ItemUnitOfMeasure {
    Bg("vreća"),
    Bx("kutija"),
    Ca("konzerva"),
    Day("dan"),
    Grm("gram"),
    H87("komad"),
    Hur("sat"),
    Kgm("kilogram"),
    Kmt("kilometar"),
    Lm("tekući metar"),
    Ls("paušal"),
    Ltr("litra"),
    Min("minuta"),
    Mlt("mililitar"),
    Mtk("metar kvadratni"),
    Mtq("metar kubični"),
    Mtr("metar"),
    Paket("paket"),
    Komplet("komplet"),
    SetUnit("set"),
    Pr("par"),
    Tne("tona"),
    E48("usluga"),
    Ea("jedinica");

    private final String displayName;

    public static ItemUnitOfMeasure fromB2b(hr.bill.spring_bill.dto.eposlovanje.enums.UnitOfMeasure unitOfMeasure) {
        if (unitOfMeasure == null) {
            return null;
        }
        return ItemUnitOfMeasure.valueOf(unitOfMeasure.name());
    }

    public static ItemUnitOfMeasure fromB2c(hr.bill.spring_bill.dto.eposlovanje.f1_web.common.UnitOfMeasure unitOfMeasure) {
        if (unitOfMeasure == null) {
            return null;
        }
        return switch (unitOfMeasure) {
            case Kom -> H87;
            case Kg -> Kgm;
            case L -> Ltr;
            case M -> Mtr;
            case H -> Hur;
        };
    }
}