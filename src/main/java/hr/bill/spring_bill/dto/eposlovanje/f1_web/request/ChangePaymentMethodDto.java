package hr.bill.spring_bill.dto.eposlovanje.f1_web.request;

import hr.bill.spring_bill.dto.eposlovanje.f1_web.common.PaymentMethod;
import lombok.Builder;

@Builder
public record ChangePaymentMethodDto(PaymentMethod newPaymentMethod) {}
