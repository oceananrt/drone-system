package com.drone.dronesystem;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "spring.main.allow-bean-definition-overriding=true"
})
class DroneSystemApplicationTests {

	@Test
	void contextLoads() {
	}

}
