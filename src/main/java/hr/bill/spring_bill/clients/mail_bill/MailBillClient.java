package hr.bill.spring_bill.clients.mail_bill;

import hr.bill.spring_bill.dto.mail_bill.request.SendMailRequest;
import hr.bill.spring_bill.dto.mail_bill.response.SendMailResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "mail-bill",
        url = "${bill.mail-bill.base-url}"
)
public interface MailBillClient {

    @PostMapping("/send-mail")
    SendMailResponse sendMail(@RequestBody SendMailRequest req);
}