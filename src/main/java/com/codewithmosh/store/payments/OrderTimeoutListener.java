package com.codewithmosh.store.payments;

import com.codewithmosh.store.orders.*;
import com.codewithmosh.store.products.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OrderTimeoutListener {
    private final OrderService orderService; // 要实现自动取消订单和回滚库存
    private final OrderRepository orderRepository;
    private final ProductService productService;

    @RabbitListener(queues = PaymentRabbitConfig.ORDER_DEAD_LETTER_QUEUE)
    public void onOrderTimeout(Long orderId) {
        // --- 1. 查找订单 ---
        Optional<Order> orderOptional = orderRepository.findById(orderId);
        if (orderOptional.isEmpty()) {
            return;
        }
        Order order = orderOptional.get();
        // --- 2. 核心校验：判断订单当前状态 ---
        // 只有当订单状态为 PENDING (待支付) 时，才进行超时处理和库存回滚。
        // 如果订单已经是 PAID (已支付)、CANCELLED (已取消) 或 REFUNDED (已退款) 等状态，
        // 则说明此超时消息是多余的，无需处理，直接返回。
        if (order.getStatus() != PaymentStatus.PENDING) {
            return; // 订单状态不为待支付，直接结束
        }

        // --- 3. 更新订单状态为“超时取消” ---
        try {
            order.setStatus(PaymentStatus.CANCELLED_BY_TIMEOUT); // 假设你的 PaymentStatus 枚举中有这个值
            orderRepository.save(order); // 保存更新后的订单状态
            System.out.println("订单ID " + orderId + " 状态已更新为 " + PaymentStatus.CANCELLED_BY_TIMEOUT);

            // --- 4. 回滚库存 ---
            // 为了确保订单项是加载的，如果你使用了懒加载，可能需要重新通过一个能加载订单项的方法获取订单
            // 或者确保在你的 Order 实体上 @OneToMany(mappedBy = "order", fetch = FetchType.EAGER)
            // 但更好的实践是，在 OrderRepository 中提供一个专门的方法，如 getOrderWithItems(orderId)，
            // 确保在这个事务中能够访问到订单项。
            // 这里我们假设你原有的 getOrderWithItems 也能在事务内工作。
            orderRepository.getOrderWithItems(orderId) // 重新获取订单，确保items被加载
                    .ifPresent(o -> {
                        o.getItems().forEach(item -> {
                            try {
                                // 调用 ProductService 中的回滚库存方法
                                // 它会处理乐观锁和重试逻辑
                                productService.rollBackStock(item.getProduct().getId(), item.getQuantity());
                                // System.out.println("成功回滚商品 " + item.getProduct().getId() + " 数量 " + item.getQuantity() + "。");
                            } catch (StockRollbackException e) {
                                // 库存回滚失败的异常捕获。
                                // 打印错误日志，可能需要触发告警，或者记录到待人工处理的队列/表中。
                                // System.err.println("ERROR: 订单ID " + orderId + " 的商品 " + item.getProduct().getId() + " 库存回滚失败！原因: " + e.getMessage());
                                // 此时不应该吞掉异常，应该 re-throw 一个运行时异常，
                                // 这样 RabbitMQ 会认为消息处理失败，可能会根据配置重试或进入死信队列。
                                throw new RuntimeException("库存回滚失败，订单ID: " + orderId + ", 商品ID: " + item.getProduct().getId(), e);
                            } catch (Exception e) {
                                // System.err.println("ERROR: 订单ID " + orderId + " 的商品 " + item.getProduct().getId() + " 回滚库存时发生未知错误！原因: " + e.getMessage());
                                throw new RuntimeException("库存回滚发生未知错误，订单ID: " + orderId + ", 商品ID: " + item.getProduct().getId(), e);
                            }
                        });
                    });

        } catch (Exception e) {
            // 捕获订单状态更新或回滚过程中发生的任何其他意外异常
            System.err.println("CRITICAL ERROR: 处理订单ID " + orderId + " 超时时发生关键错误：" + e.getMessage());
            // 再次抛出异常，让 RabbitMQ 知道此消息处理失败，以便根据配置进行重试或放入死信队列。
            throw new RuntimeException("订单超时处理失败，订单ID: " + orderId, e);
        }
    }
}
