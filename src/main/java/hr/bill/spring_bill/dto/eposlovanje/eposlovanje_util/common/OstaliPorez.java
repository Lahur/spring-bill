package hr.bill.spring_bill.dto.eposlovanje.eposlovanje_util.common;

public record OstaliPorez(
        String nazivPoreza,
        Double poreznaStopa,
        Double osnovica,
        Double iznosPoreza
) {}
