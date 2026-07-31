package hr.bill.spring_bill.dto.eposlovanje.eposlovanje_util.response;

import hr.bill.spring_bill.dto.eposlovanje.eposlovanje_util.common.BusinessType;

import java.util.List;

public record BusinessEntity(
        String name,
        String shortName,
        String oib,
        String mbs,
        String emailAddress,
        String headquatersAddress,
        String headquatersCity,
        String headquatersZip,
        String headquatersRegion,
        Double foundingCapitalAmount,
        String foundingCapitalCurrency,
        String legalForm,
        String legalFormShort,
        String sudregRawData,
        List<Object> companyFounders,
        List<Object> legalRepresentatives,
        List<BusinessType> businessTypes,
        String registrationCourtName,
        String competentCourtName
) {}
