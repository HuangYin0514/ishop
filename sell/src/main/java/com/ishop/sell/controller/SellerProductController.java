package com.ishop.sell.controller;

import com.ishop.sell.VO.ProductInfoVO;
import com.ishop.sell.VO.ProductVO;
import com.ishop.sell.VO.ResultVO;
import com.ishop.sell.dataobject.ProductCategory;
import com.ishop.sell.dataobject.ProductInfo;
import com.ishop.sell.service.CategoryService;
import com.ishop.sell.service.ProductService;
import com.ishop.sell.utils.ResultVOUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 买家商品
 */
@RestController
@RequestMapping("/buyer/product")
public class SellerProductController {

    @Autowired
    private ProductService productService;
    @Autowired
    private CategoryService categoryService;

    /**
     * 列表
     * @return
     */
    @GetMapping("/list")
    public ResultVO list() {
        //查询上架商品
        List<ProductInfo> productInfoList = productService.findUPAll();
        //查询类目
        List<Integer> categoryTypeList = productInfoList.stream().map(e -> e.getCategoryType()).collect(Collectors.toList());
        List<ProductCategory> productCategoryList = categoryService.findByCategoryTypeIn(categoryTypeList);
        //3数据拼装
        //设置第二层信息
        List<ProductVO> productVOList = new ArrayList<>();
        for (ProductCategory productCategory : productCategoryList) {
            ProductVO productVO = new ProductVO();
            productVO.setCategoryType(productCategory.getCategoryType());
            productVO.setCategoryName(productCategory.getCategoryName());

            //设置第三层信息
            List<ProductInfoVO> productInfoVOList = new ArrayList<>();
            for (ProductInfo productInfo : productInfoList) {
                //如果商品分类等于商品信息表中分类
                if (productInfo.getCategoryType().equals(productCategory.getCategoryType())) {
                    //设置商品详细信息
                    ProductInfoVO productInfoVO = new ProductInfoVO();
                    //将productInfo中信息复制到 productInfoVO中
                    BeanUtils.copyProperties(productInfo, productInfoVO);
                    productInfoVOList.add(productInfoVO);
                }
            }
            productVO.setProductInfoVOList(productInfoVOList);
            productVOList.add(productVO);
        }
        return ResultVOUtil.success(productVOList);
    }

}
