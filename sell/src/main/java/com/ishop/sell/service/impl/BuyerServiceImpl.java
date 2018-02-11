package com.ishop.sell.service.impl;

import com.ishop.sell.dto.OrderDTO;
import com.ishop.sell.enums.ResultEnum;
import com.ishop.sell.exception.SellException;
import com.ishop.sell.service.BuyerService;
import com.ishop.sell.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BuyerServiceImpl implements BuyerService {

    @Autowired
    private OrderService orderService;

    @Override
    public OrderDTO findOrderOne(String openid, String orderId) {
        OrderDTO result = checkOrderOwner(openid, orderId);
        return result;
    }

    @Override
    public OrderDTO cancelOrder(String openid, String orderId) {
        OrderDTO result = checkOrderOwner(openid, orderId);
        if (result == null) {
            log.error("【取消订单】查询不到该订单， orderId={}", orderId);
            throw new SellException(ResultEnum.ORDER_NOT_EXIST);
        }
        return result;
    }

    private OrderDTO checkOrderOwner(String openid, String orderId) {
        OrderDTO orderDTO = orderService.findOne(orderId);
        if (orderDTO == null) {
            return null;
        }
        if (!orderDTO.getBuyerOpenid().equalsIgnoreCase(openid)) {
            log.error("【查询订单】订单的openid不一致. openid ={}, orderDTO={}", openid, orderDTO);
            throw new SellException(ResultEnum.ORDER_OWNER_ERROR);
        }
        return orderDTO;

    }
}
