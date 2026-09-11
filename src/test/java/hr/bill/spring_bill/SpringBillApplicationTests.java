package hr.bill.spring_bill;

import org.junit.jupiter.api.Test;

// Extends AbstractIntegrationTest (rather than a bare @SpringBootTest) because the app needs a
// real datasource and every bill.* property (hub-url, eposlovanje/pondi/f1-web api-key, ...) to
// even start: the default profile alone never resolves them (that's normally Vault's job, see
// application-dev/prod.yaml), so a plain @SpringBootTest fails context startup before this test
// gets to run anything.
class SpringBillApplicationTests extends AbstractIntegrationTest {

	@Test
	void contextLoads() {
	}

}
