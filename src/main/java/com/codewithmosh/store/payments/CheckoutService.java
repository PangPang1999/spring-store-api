package com.codewithmosh.store.payments;

import com.codewithmosh.store.orders.Order;
import com.codewithmosh.store.carts.CartEmptyException;
import com.codewithmosh.store.carts.CartNotFoundException;
import com.codewithmosh.store.carts.CartRepository;
import com.codewithmosh.store.orders.OrderRepository;
import com.codewithmosh.store.auth.AuthService;
import com.codewithmosh.store.carts.CartService;
import com.codewithmosh.store.products.OutOfStockException;
import com.codewithmosh.store.products.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CheckoutService {
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final AuthService authService;
    private final CartService cartService;
    private final PaymentGateway paymentGateway;
    private final RabbitTemplate rabbitTemplate;

    @Value("${websiteUrl}")
    private String websiteUrl;

    @Transactional
    public CheckoutResponse checkout(CheckoutRequest request) {
        var cart = cartRepository.getCartWithItems(request.getCartId()).orElse(null);
        if (cart == null) {
            throw new CartNotFoundException();
        }

        if (cart.isEmpty()) {
            throw new CartEmptyException();
        }

        // 是否所有商品都有货
        boolean allInStock = cart.getItems().stream()
                .allMatch(item ->
                        item.getProduct() != null &&
                                item.getQuantity() <= item.getProduct().getQuantity()
                );
        if (!allInStock) {
            throw new OutOfStockException();
        }

        // 解耦
        var order = Order.fromCart(cart, authService.getCurrentUser());
        // 预扣库存
        preDeductStock(order);

        orderRepository.save(order);

        try {
            //  1. 尝试创建 Stripe Checkout Session
            var session = paymentGateway.createCheckoutSession(order);

            // // 2. 如果 Stripe Session 成功创建，才发送延迟队列消息
            // 将订单id记录，将其放进延迟队列，2分钟后死，监听死
            // 若超时，将ORDER设置为失败，即便支付成功，也给他退款！
            rabbitTemplate.convertAndSend(
                    PaymentRabbitConfig.ORDER_EXCHANGE,      // 目标交换机的名字
                    PaymentRabbitConfig.ORDER_ROUTING_KEY,   // 发送到交换机的路由key
                    order.getId()                           // 要发送的消息内容，这里是订单ID
            );

            // 3. 清理购物车
            cartService.clearCart(cart.getId());

            // 4. 返回支付响应
            return new CheckoutResponse(order.getId(), session.getCheckoutUrl());
        } catch (PaymentException ex) {
            orderRepository.delete(order);
            throw ex;
        }
    }

    @Transactional
    public void preDeductStock(Order order) {
        order.getItems().stream().forEach(item -> {
            productService.preDeductStock(item.getProduct().getId(), item.getQuantity());
        });
    }

    public void handleWebhookEvent(WebhookRequest request) {
        paymentGateway
                .parseWebhookRequest(request)
                .ifPresent(paymentResult -> {
                    var order = orderRepository.findById(paymentResult.getOrderId()).orElseThrow();
                    order.setStatus(paymentResult.getPaymentStatus());
                    orderRepository.save(order);
                });
    }
}