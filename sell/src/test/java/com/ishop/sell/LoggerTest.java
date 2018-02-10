package com.ishop.sell;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class LoggerTest {

//    private static final Logger log = org.slf4j.LoggerFactory.getLogger(LoggerTest.class);
//
    @Test
    public void test1() {
        String name = "imooc";
        String password = "123456";
        log.debug("debug..");
        log.info("name {} ,password {}" ,name,password);
        log.error("error...");
        log.warn("warn...");
        log.error(LoggerTest.class.toString());
    }

    @Test
    public void testCurrentTime() {
        long timeMillis = System.currentTimeMillis();
        System.out.println(timeMillis);
    }
}
