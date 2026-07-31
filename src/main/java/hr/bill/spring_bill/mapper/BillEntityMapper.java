package hr.bill.spring_bill.mapper;

import hr.bill.spring_bill.dto.eposlovanje.eposlovanje.response.DocumentStatusResponse;
import hr.bill.spring_bill.dto.eposlovanje.f1_web.response.ReceiptDto;
import hr.bill.spring_bill.dto.web.bill.BillResponse;
import hr.bill.spring_bill.dto.web.bill.b2b.ReportBillRequest;
import hr.bill.spring_bill.model.BillEntity;
import hr.bill.spring_bill.model.enums.BillType;
import hr.bill.spring_bill.service.HrPaymentReferenceService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_DEFAULT,
        imports = HrPaymentReferenceService.class)
public interface BillEntityMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "dsr.id", target = "systemId")
    @Mapping(source = "dsr.documentId", target = "fullBillId")
    @Mapping(source = "dsr.customerPartyName", target = "clientName")
    @Mapping(source = "dsr.customerPartyVATId", target = "clientOib")
    @Mapping(target = "billDate", expression = "java(java.time.LocalDateTime.parse(dsr.issuedOn()))")
    @Mapping(target = "totalAmount", expression = "java(java.math.BigDecimal.valueOf(dsr.amount()))")
    @Mapping(source = "dsr.status", target = "documentStatus")
    @Mapping(source = "billType", target = "billType")
    @Mapping(target = "sentCount", ignore = true)
    @Mapping(target = "paymentReference", ignore = true)
    BillEntity toBillEntity(DocumentStatusResponse dsr, BillType billType);

    default List<BillEntity> toBillEntityList(List<DocumentStatusResponse> dsrList, BillType billType) {
        return dsrList.stream().map(dsr -> toBillEntity(dsr, billType)).toList();
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "dsr.id", target = "systemId")
    @Mapping(source = "dsr.documentId", target = "fullBillId")
    @Mapping(source = "dsr.supplierPartyName", target = "clientName")
    @Mapping(source = "dsr.supplierPartyVATId", target = "clientOib")
    @Mapping(target = "billDate", expression = "java(java.time.LocalDateTime.parse(dsr.issuedOn()))")
    @Mapping(target = "totalAmount", expression = "java(java.math.BigDecimal.valueOf(dsr.amount()))")
    @Mapping(source = "dsr.status", target = "documentStatus")
    @Mapping(source = "billType", target = "billType")
    @Mapping(target = "sentCount", ignore = true)
    @Mapping(target = "paymentReference", ignore = true)
    BillEntity toIngoingBillEntity(DocumentStatusResponse dsr, BillType billType);

    default List<BillEntity> toIngoingBillEntityList(List<DocumentStatusResponse> dsrList, BillType billType) {
        return dsrList.stream().map(dsr -> toIngoingBillEntity(dsr, billType)).toList();
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "r.id", target = "systemId")
    @Mapping(source = "r.formattedReceiptNumber", target = "fullBillId")
    @Mapping(source = "r.buyerName", target = "clientName")
    @Mapping(source = "r.buyerOib", target = "clientOib")
    @Mapping(target = "billDate", expression = "java(java.time.LocalDateTime.parse(r.issueDateTime()))")
    @Mapping(target = "totalAmount", expression = "java(java.math.BigDecimal.valueOf(r.grandTotal()))")
    @Mapping(target = "documentStatus", ignore = true)
    @Mapping(source = "billType", target = "billType")
    @Mapping(target = "sentCount", ignore = true)
    @Mapping(target = "paymentReference", expression = "java(HrPaymentReferenceService.buildReference(r.formattedReceiptNumber()))")
    BillEntity toBillEntity(ReceiptDto r, BillType billType);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "systemId", expression = "java(r.getBillId().longValue())")
    @Mapping(target = "fullBillId", expression = "java(r.getBillId() + \"/1/1\")")
    @Mapping(source = "r.buyerName", target = "clientName")
    @Mapping(source = "r.buyerOib", target = "clientOib")
    @Mapping(target = "billDate", expression = "java(java.time.LocalDateTime.of(r.getBillDate(), r.getBillTime()))")
    @Mapping(source = "totalAmount", target = "totalAmount")
    @Mapping(target = "documentStatus", ignore = true)
    @Mapping(source = "billType", target = "billType")
    @Mapping(target = "sentCount", ignore = true)
    @Mapping(target = "paymentReference", ignore = true)
    BillEntity toBillEntity(ReportBillRequest r, BigDecimal totalAmount, BillType billType);

    BillResponse toBillResponse(BillEntity entity);

    List<BillResponse> toBillResponseList(List<BillEntity> entities);
}