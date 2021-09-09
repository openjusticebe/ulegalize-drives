package com.ulegalize;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureTestEntityManager
@ComponentScan({"com.ulegalize"})
@TestPropertySource(
    locations = "classpath:application-devtest.properties")
public class OneDriveServiceTests {

  @BeforeClass
  public static void setUp() {
  }

  @Test
  public void getToken() throws Exception {
    Assert.assertTrue(true);
  }

}
