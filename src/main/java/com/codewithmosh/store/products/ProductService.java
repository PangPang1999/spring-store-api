package com.codewithmosh.store.products;

import com.codewithmosh.store.payments.StockRollbackException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final CategoryRepository categoryRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public List<ProductListDto> getAllProducts(Long categoryId) {
        // 缓存Key根据分类动态生成
        String cacheKey = categoryId != null
                ? "product:list:category:" + categoryId
                : "product:list:all";

        // 1. 先查缓存
        Object cache = redisTemplate.opsForValue().get(cacheKey);
        if (cache != null) {
            // 使用 ObjectMapper 和 TypeReference 来反序列化成 List<ProductListDto>
            try {
                List<ProductListDto> cacheList = new ObjectMapper().readValue((String) cache, new TypeReference<List<ProductListDto>>() {
                });
                return cacheList;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 2. 缓存没有，查数据库
        List<Product> products;
        if (categoryId != null) {
            products = productRepository.findByCategoryId(categoryId);
        } else {
            products = productRepository.findAllWithCategory();
        }
        List<ProductListDto> result = products.stream()
                .map(productMapper::toListDto)
                .toList();

        // 3. 写入缓存，设置过期时间（如10分钟）
        String serializedData = null;
        try {
            serializedData = new ObjectMapper().writeValueAsString(result);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize products list", e);
        }
        redisTemplate.opsForValue().set(cacheKey, serializedData, 10, TimeUnit.MINUTES);

        return result;
    }

    public ProductDto getProductById(Long id) {
        String cacheKey = "product:detail:" + id;

        // 1. 先查缓存
        ProductDto cached = (ProductDto) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 2. 没有缓存，查数据库
        var product = productRepository.findById(id).orElse(null);
        if (product == null) return null;

        ProductDto dto = productMapper.toDto(product);

        // 3. 写入缓存（如10分钟过期）
        redisTemplate.opsForValue().set(cacheKey, dto, 10, TimeUnit.MINUTES);

        return dto;
    }

    public ProductDto createProduct(ProductDto productDto) {
        var category = categoryRepository.findById(productDto.getCategoryId()).orElse(null);
        var quantity = productDto.getQuantity();

        if (quantity == null || quantity < 0 || category == null) {
            return null;
        }

        var product = productMapper.toEntity(productDto);
        product.setCategory(category);
        product.setQuantity(quantity);
        productRepository.save(product);

        productDto.setId(product.getId());

        // === 缓存失效 ===
        redisTemplate.delete("product:list:all");
        redisTemplate.delete("product:list:category:" + productDto.getCategoryId());

        return productDto;
    }

    public ProductDto updateProduct(Long id, ProductDto productDto) {
        var category = categoryRepository.findById(productDto.getCategoryId()).orElse(null);
        var quantity = productDto.getQuantity();

        if (quantity == null || quantity < 0 || category == null) {
            return null;
        }

        var product = productRepository.findById(id).orElse(null);
        if (product == null) return null;

        productMapper.update(productDto, product);
        product.setCategory(category);
        product.setQuantity(quantity);
        productRepository.save(product);

        productDto.setId(product.getId());

        // === 缓存失效处理 ===
        redisTemplate.delete("product:detail:" + id);                        // 商品详情缓存
        redisTemplate.delete("product:list:all");                            // 全部商品列表
        redisTemplate.delete("product:list:category:" + productDto.getCategoryId()); // 分类商品列表

        return productDto;
    }

    public boolean deleteProduct(Long id) {
        var product = productRepository.findById(id).orElse(null);
        if (product == null) return false;

        productRepository.delete(product);
        return true;
    }

    /**
     * 单个商品预扣库存（乐观锁，超卖防护）
     */
    public void preDeductStock(Long id, int quantity) {
        Product product = productRepository.findById(id)
                .orElseThrow(ProductNotFoundException::new);

        if (product.getQuantity() < quantity) {
            throw new OutOfStockException();
        }

        product.setQuantity(product.getQuantity() - quantity);

        try {
            productRepository.saveAndFlush(product); // JPA 乐观锁防止并发超卖

            // === 缓存失效处理 ===
            redisTemplate.delete("product:detail:" + id);
            redisTemplate.delete("product:list:all");
            if (product.getCategory() != null) {
                redisTemplate.delete("product:list:category:" + product.getCategory().getId());
            }

        } catch (ObjectOptimisticLockingFailureException e) {
            throw new OutOfStockException();
        }
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW) // 【关键】确保在一个新的事务中运行
    public void rollBackStock(Long productId, int quantityToRollback) {
        final int MAX_RETRIES = 5; // 定义最大重试次数
        for (int retryCount = 0; retryCount < MAX_RETRIES; retryCount++) {
            try {
                // 1. 查找商品：确保获取最新的数据，包括版本号
                // 如果找不到商品，说明可能是数据问题，直接抛出异常
                Product product = productRepository.findById(productId)
                        .orElseThrow(() -> new ProductNotFoundException());

                // 2. 增加库存数量
                product.setQuantity(product.getQuantity() + quantityToRollback);

                // 3. 保存更新：JPA 会在这里检查版本号并处理乐观锁
                productRepository.saveAndFlush(product);

                // 4. 如果保存成功，则库存回滚完成，并处理缓存失效（如果有）
                System.out.println("INFO: 商品ID " + productId + " 库存已成功回滚 " + quantityToRollback + "。");

                // === 缓存失效处理 ===
                redisTemplate.delete("product:detail:" + productId);
                redisTemplate.delete("product:list:all");
                if (product.getCategory() != null) {
                    redisTemplate.delete("product:list:category:" + product.getCategory().getId());
                }

                return; // 成功回滚，退出方法
            } catch (ObjectOptimisticLockingFailureException e) {
                // 5. 处理乐观锁冲突：
                // 如果捕获到乐观锁异常，说明有其他事务同时更新了该商品。
                // 打印日志，增加重试计数，并等待一小段时间后重试。
                System.out.println("WARN: 商品ID " + productId + " 库存回滚时发生乐观锁冲突。尝试重试 " + (retryCount + 1) + "/" + MAX_RETRIES + " 次。");
                try {
                    Thread.sleep(50); // 短暂等待，避免紧密循环占用 CPU
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt(); // 恢复中断状态
                    // 如果线程被中断，认为无法继续安全重试，直接抛出异常
                    throw new StockRollbackException("回滚库存中断，商品ID: " + productId, ie);
                }
            } catch (ProductNotFoundException e) {
                // 商品未找到的异常，通常不重试，直接抛出
                System.err.println("ERROR: 回滚库存失败：商品ID " + productId + " 不存在。");
                throw e; // 重新抛出此异常
            } catch (Exception e) {
                // 捕获其他所有意外异常
                System.err.println("ERROR: 回滚商品ID " + productId + " 库存时发生未知错误: " + e.getMessage());
                // 此时不进行重试，直接抛出包装后的异常，让调用方处理
                throw new StockRollbackException("回滚库存时发生未知错误，商品ID: " + productId, e);
            }
        }
        // 6. 如果循环结束仍未成功，说明重试次数已用尽
        System.err.println("CRITICAL: 商品ID " + productId + " 库存回滚失败，已达到最大重试次数 " + MAX_RETRIES + "。需要人工介入！");
        throw new StockRollbackException("回滚库存失败，已达到最大重试次数，商品ID: " + productId);
    }
}
