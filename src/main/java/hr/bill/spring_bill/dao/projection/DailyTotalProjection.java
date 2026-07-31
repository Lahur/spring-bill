package hr.bill.spring_bill.dao.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface DailyTotalProjection {

    LocalDate getDay();

    BigDecimal getTotal();
}