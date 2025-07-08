package com.codewithmosh.store.payments;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class PaymentRabbitConfig {
    // 各种队列和交换机的名字
    public static final String ORDER_DELAY_QUEUE = "order.delay.queue";         // 延迟队列
    public static final String ORDER_DEAD_LETTER_QUEUE = "order.dlx.queue";     // 死信队列
    public static final String ORDER_EXCHANGE = "order.exchange";               // 交换机
    public static final String ORDER_ROUTING_KEY = "order.create";              // 正常消息的路由Key
    public static final String ORDER_DLX_ROUTING_KEY = "order.dlx";             // 死信消息的路由Key

    // 创建延迟队列：2分钟内不处理消息，2分钟后消息会被投递到死信队列
    @Bean
    public Queue orderDelayQueue() {
        Map<String, Object> args = new HashMap<>();
        // 指定过期或被拒绝时的“死信”该去哪
        args.put("x-dead-letter-exchange", ORDER_EXCHANGE); // 指定死信要发往哪个交换机
        args.put("x-dead-letter-routing-key", ORDER_DLX_ROUTING_KEY); // 指定死信的routingKey
        args.put("x-message-ttl", 2 * 60 * 1000); // 2分钟超时 (毫秒)
        // args.put("x-message-ttl", 15 * 1000); // 测试用——15秒超时 (毫秒)，切换需要前往控制台删除原有队列再启动
        return new Queue(ORDER_DELAY_QUEUE, true, false, false, args);
    }

    // 死信队列：存放超时/被拒绝/出错的消息（实际“自动关单”逻辑监听这个队列）
    @Bean
    public Queue orderDeadLetterQueue() {
        return new Queue(ORDER_DEAD_LETTER_QUEUE, true);
    }

    // 交换机：用来分发消息（直连模式）
    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(ORDER_EXCHANGE);
    }

    // 绑定延迟队列到交换机，指定路由键
    @Bean
    public Binding delayQueueBinding() {
        return BindingBuilder.bind(orderDelayQueue())
                .to(orderExchange())
                .with(ORDER_ROUTING_KEY); // 新订单用 order.create 路由key
    }

    // 绑定死信队列到交换机，指定路由键
    @Bean
    public Binding deadLetterQueueBinding() {
        return BindingBuilder.bind(orderDeadLetterQueue())
                .to(orderExchange())
                .with(ORDER_DLX_ROUTING_KEY); // 死信用 order.dlx 路由key
    }
}
