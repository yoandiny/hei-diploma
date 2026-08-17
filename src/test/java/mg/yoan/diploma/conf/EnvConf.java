package mg.yoan.diploma.conf;

import org.springframework.test.context.DynamicPropertyRegistry;

public class EnvConf {

  void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("app.jwt.secret", () -> "integration-test-jwt-secret-key-32b!");
  }
}
