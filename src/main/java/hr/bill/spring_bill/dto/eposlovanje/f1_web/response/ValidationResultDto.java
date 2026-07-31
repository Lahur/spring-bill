package hr.bill.spring_bill.dto.eposlovanje.f1_web.response;

import java.util.List;

public record ValidationResultDto(Boolean isValid, List<ValidationErrorDto> errors) {}
