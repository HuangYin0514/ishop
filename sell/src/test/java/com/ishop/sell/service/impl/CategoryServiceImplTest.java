package com.ishop.sell.service.impl;

import com.ishop.sell.dataobject.ProductCategory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

@SpringBootTest
@RunWith(SpringRunner.class)
public class CategoryServiceImplTest {

    @Autowired
    private CategoryServiceImpl categoryService;

    @Test
    public void findOne() {
        ProductCategory result = categoryService.findOne(1);
        Assert.assertEquals(new Integer(1),result.getCategoryId());
    }

    @Test
    public void findAll() {
        List<ProductCategory> result = categoryService.findAll();
        Assert.assertNotNull(result);
    }

    @Test
    public void findByCategoryTypeIn() {
        List<ProductCategory> result = categoryService.findByCategoryTypeIn(Arrays.asList(1, 2, 3, 4, 5, 6));
        Assert.assertNotEquals(0,result.size());
    }

    @Test
    public void save() {
        ProductCategory result = categoryService.save(new ProductCategory("男生专项", 6));
        Assert.assertNotNull(result);
    }
}