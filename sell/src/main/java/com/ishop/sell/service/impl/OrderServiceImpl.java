package com.ishop.sell.service.impl;

import com.ishop.sell.converter.OrderMaster2OrderDTOConverter;
import com.ishop.sell.dataobject.OrderDetail;
import com.ishop.sell.dataobject.OrderMaster;
import com.ishop.sell.dataobject.ProductInfo;
import com.ishop.sell.dto.CartDTO;
import com.ishop.sell.dto.OrderDTO;
import com.ishop.sell.enums.OrderStatusEnum;
import com.ishop.sell.enums.PayStatusEnum;
import com.ishop.sell.enums.ResultEnum;
import com.ishop.sell.exception.SellException;
import com.ishop.sell.repository.OrderDetailRepository;
import com.ishop.sell.repository.OrderMasterRepository;
import com.ishop.sell.service.OrderService;
import com.ishop.sell.service.ProductService;
import com.ishop.sell.utils.KeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private ProductService productService;
    @Autowired
    private OrderDetailRepository orderDetailRepository;
    @Autowired
    private OrderMasterRepository orderMasterRepository;

    /**
     * 一个order_master中有多个order_detail
     * 创建订单
     * @param orderDTO
     * @return
     */
    @Override
    @Transactional
    public OrderDTO create(OrderDTO orderDTO) {
        //生成order_master的id ，当前毫秒级时间+随机数
        String orderId = KeyUtil.getnUniqueKey();
        //设置初始总金额
        BigDecimal orderAmount = new BigDecimal(BigInteger.ZERO);
        //遍历所有购买的商品，商品在orderDTO.getOrderDetailList中
        //并把每个OrderDetail存入数据库中
        for (OrderDetail orderDetail : orderDTO.getOrderDetailList()) {
            //根据id从数据库中查询一件商品
            ProductInfo productInfo = productService.findOne(orderDetail.getProductId());
            //如果商品不存在，则抛异常
            if (productInfo == null) {
                throw new SellException(ResultEnum.PRODUCT_NOT_EXIST);
            }
            //计算总金额，用累加的办法，总金额 = 商品单价 * 个数
            orderAmount = productInfo.getProductPrice()
                    .multiply(new BigDecimal(orderDetail.getProductQuantity()))
                    .add(orderAmount);
            //设置订单详情
            orderDetail.setDetailId(KeyUtil.getnUniqueKey());
            orderDetail.setOrderId(orderId);
            //名字相同的属性直接复制。不用每一个set
            BeanUtils.copyProperties(productInfo, orderDetail);
            //将order_detail存入数据库中
            orderDetailRepository.save(orderDetail);
        }
        //将OrderMaster属性补全
        OrderMaster orderMaster = new OrderMaster();
        orderDTO.setOrderId(orderId);
        BeanUtils.copyProperties(orderDTO, orderMaster);
        orderMaster.setOrderAmount(orderAmount);
        //设置订单状态
        orderMaster.setOrderStatus(OrderStatusEnum.NEW.getCode());
        //设置支付状态
        orderMaster.setPayStatus(PayStatusEnum.WAIT.getCode());
        //将将OrderMaster存入数据库中
        orderMasterRepository.save(orderMaster);
        //获得购物车列表
        List<CartDTO> cartDTOList = orderDTO.getOrderDetailList().stream()
                .map(e -> new CartDTO(e.getProductId(),e.getProductQuantity()))
                .collect(Collectors.toList());
        //将购物车对应的库存减去
        productService.decreaseStock(cartDTOList);
        orderDTO.setOrderAmount(orderAmount);
        orderDTO.setOrderStatus(OrderStatusEnum.NEW.getCode());
        orderDTO.setPayStatus(PayStatusEnum.WAIT.getCode());
        return orderDTO;
    }

    @Override
    public OrderDTO findOne(String orderId) {

        OrderMaster orderMaster = orderMasterRepository.findOne(orderId);
        if (orderMaster == null) {
            throw new SellException(ResultEnum.ORDER_NOT_EXIST);
        }
        List<OrderDetail> orderDetailList = orderDetailRepository.findByOrderId(orderId);
        if (CollectionUtils.isEmpty(orderDetailList)) {
            throw new SellException(ResultEnum.ORDERDETAIL_NOT_EXIST);
        }
        OrderDTO orderDTO = new OrderDTO();
        BeanUtils.copyProperties(orderMaster, orderDTO);
        orderDTO.setOrderDetailList(orderDetailList);

        return orderDTO;
    }

    @Override
    public Page<OrderDTO> findList(String buyerOpenid, Pageable pageable) {
        Page<OrderMaster> orderMasterPage = orderMasterRepository.findByBuyerOpenid(buyerOpenid, pageable);
        List<OrderDTO> orderDTOList = OrderMaster2OrderDTOConverter.convert(orderMasterPage.getContent());
        Page<OrderDTO> orderDTOPage = new PageImpl<>(orderDTOList, pageable, orderMasterPage.getTotalElements());
        return orderDTOPage;
    }

    @Override
    @Transactional
    public OrderDTO cancel(OrderDTO orderDTO) {
        OrderMaster orderMaster = new OrderMaster();

        //判断订单状态
        if (!orderDTO.getOrderStatus().equals(OrderStatusEnum.NEW.getCode())) {
            log.error("【取消订单】订单状态不正确。orderId={} , orderStatus={}",orderDTO.getOrderId(),orderDTO.getOrderStatus());
            throw new SellException(ResultEnum.ORDER_STATUS_ERROR);
        }
        orderDTO.setOrderStatus(OrderStatusEnum.CANCEL.getCode());
        //更新orderMaster表
        BeanUtils.copyProperties(orderDTO, orderMaster);
        OrderMaster updateResult = orderMasterRepository.save(orderMaster);
        if (updateResult == null) {
            log.error("【取消订单】更新失败，orderMaster={}", orderMaster);
            throw new SellException(ResultEnum.ORDER_UPDATE_FAIL);
        }
        //返回库存
        if (CollectionUtils.isEmpty(orderDTO.getOrderDetailList())) {
            log.error("【取消订单】订单中无商品详情， orderDTO={}", orderDTO);
            throw new SellException(ResultEnum.ORDER_DETAIL_EMPTY);
        }
        List<CartDTO> cartDTOList = orderDTO.getOrderDetailList().stream().map(
                e -> new CartDTO(e.getProductId(), e.getProductQuantity())
        ).collect(Collectors.toList());
        productService.increaseStock(cartDTOList);

        //如果支付，需要退款
        if (orderDTO.getPayStatus().equals(PayStatusEnum.SUCCESS.getCode())) {
            //TODO
        }
        return orderDTO;
    }

    @Override
    public OrderDTO finish(OrderDTO orderDTO) {
        return null;
    }

    @Override
    public OrderDTO paid(OrderDTO orderDTO) {
        return null;
    }

    @Override
    public Page<OrderDTO> findList(Pageable pageable) {
        return null;
    }
}
