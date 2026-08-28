package hr.bill.spring_bill.dto.eposlovanje.eposlovanje_util.request;

import hr.bill.spring_bill.dto.eposlovanje.eposlovanje_util.common.*;
import lombok.Builder;

import java.util.List;

@Builder
public record FiscalizationRequest(
        String oib,
        Boolean uSustavuPdv,
        String datumVrijemeIzdavanja,
        OznakaSlijednostiType oznakaSlijednosti,
        BrojRacuna brojRacuna,
        List<PDV> pdv,
        List<PNP> pnp,
        List<OstaliPorez> ostaliPorezi,
        Double iznosOslobodenja,
        Double iznosNaKojiSeOdnosiPosebanPostupakOporezivanjaMarze,
        Double iznosKojiNePodlijezeOporezivanju,
        List<Naknada> naknade,
        Double ukupanIznos,
        NacinPlacanjaType nacinPlacanja,
        String oibOperatera,
        Boolean oznakaNaknadneDostaverRacuna,
        String oznakaParagonRacuna,
        String specificnaNamjena,
        String oibPrimateljaRacuna
) {}
