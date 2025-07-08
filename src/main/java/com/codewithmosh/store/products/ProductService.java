package com.codewithmosh.store.products;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

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

}
