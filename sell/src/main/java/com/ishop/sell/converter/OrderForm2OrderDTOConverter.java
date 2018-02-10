package com.ishop.sell.converter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ishop.sell.dataobject.OrderDetail;
import com.ishop.sell.dto.OrderDTO;
import com.ishop.sell.enums.ResultEnum;
import com.ishop.sell.exception.SellException;
import com.ishop.sell.form.OrderForm;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class OrderForm2OrderDTOConverter {

    public static OrderDTO convert(OrderForm orderForm) {
        Gson gson = new Gson();
        OrderDTO orderDTO = new OrderDTO();

        orderDTO.setBuyerName(orderForm.getName());
        orderDTO.setBuyerPhone(orderForm.getPhone());
        orderDTO.setBuyerAddress(orderForm.getAddress());
        orderDTO.setBuyerOpenid(orderForm.getOpenid());

        List<OrderDetail> orderDetailList = new ArrayList<>();

        try {
            /**
             *  将orderFrom中的json 转换成list
             *  来自orderForm.getItems()的类型的json数据
             *  转换成
             *  new TypeToken<List<OrderDetail>>() { }.getType());
             *  类型的数据*/
            orderDetailList = gson.fromJson(orderForm.getItems(),
                    new TypeToken<List<OrderDetail>>() {
                    }.getType());
        } catch (Exception e) {
            log.error("【对象转换】错误，String={}", orderForm.getItems());
            throw new SellException(ResultEnum.PARAM_ERROR);
        }
        orderDTO.setOrderDetailList(orderDetailList);
        return orderDTO;
    }
}
