package com.ishop.sell.service.impl;

import com.ishop.sell.dataobject.ProductInfo;
import com.ishop.sell.dto.CartDTO;
import com.ishop.sell.enums.ProductStatusEnum;
import com.ishop.sell.enums.ResultEnum;
import com.ishop.sell.exception.SellException;
import com.ishop.sell.repository.ProductInfoRepository;
import com.ishop.sell.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductInfoRepository respository;

    @Override
    public ProductInfo findOne(String productId) {
        return respository.findOne(productId);
    }

    @Override
    public List<ProductInfo> findUPAll() {
        return respository.findByProductStatus(ProductStatusEnum.UP.getCode());
    }

    @Override
    public Page<ProductInfo> findAll(Pageable pageable) {
        return respository.findAll(pageable);
    }

    @Override
    public ProductInfo save(ProductInfo productInfo) {
        return respository.save(productInfo);
    }

    @Override
    @Transactional
    public void increaseStock(List<CartDTO> cartDTOList) {
        for (CartDTO cartDTO : cartDTOList) {
            ProductInfo productInfo = respository.findOne(cartDTO.getProductId());
            if (productInfo == null) {
                throw new SellException(ResultEnum.ORDER_NOT_EXIST);
            }
            Integer result = productInfo.getProductStock() + cartDTO.getProductQuantity();
            if (result < 0) {
                throw new SellException(ResultEnum.PRODUCT_STOCK_ERROR);
            }
            productInfo.setProductStock(result);
            respository.save(productInfo);
        }
    }

    @Override
    @Transactional
    public void decreaseStock(List<CartDTO> cartDTOList) {
        for (CartDTO cartDTO : cartDTOList) {
            ProductInfo productInfo = respository.findOne(cartDTO.getProductId());
            if (productInfo == null) {
                throw new SellException(ResultEnum.ORDER_NOT_EXIST);
            }
            Integer result = productInfo.getProductStock() - cartDTO.getProductQuantity();
            if (result < 0) {
                throw new SellException(ResultEnum.PRODUCT_STOCK_ERROR);
            }
            productInfo.setProductStock(result);
            respository.save(productInfo);
        }
    }
}
