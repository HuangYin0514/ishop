package com.ishop.sell.service;

import com.ishop.sell.dataobject.ProductInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {

    ProductInfo findone(String productId);

    /**
     * 查询所有在架商品
     * @return
     */
    List<ProductInfo> findUPAll();

    Page<ProductInfo> findAll(Pageable pageable);

    ProductInfo save(ProductInfo productInfo);
}
