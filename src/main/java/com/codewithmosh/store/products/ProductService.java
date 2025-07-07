package com.codewithmosh.store.products;

import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final CategoryRepository categoryRepository;

    public List<ProductDto> getAllProducts(Long categoryId) {
        List<Product> products;
        if (categoryId != null) {
            products = productRepository.findByCategoryId(categoryId);
        } else {
            products = productRepository.findAllWithCategory();
        }
        return products.stream()
                .map(productMapper::toDto)
                .toList();
    }

    public ProductDto getProductById(Long id) {
        var product = productRepository.findById(id).orElse(null);
        if (product == null) return null;
        return productMapper.toDto(product);
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
                .orElseThrow(() -> new ProductNotFoundException());

        if (product.getQuantity() < quantity) {
            throw new OutOfStockException();
        }

        product.setQuantity(product.getQuantity() - quantity);

        try {
            productRepository.saveAndFlush(product); // JPA 乐观锁防止并发超卖
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new OutOfStockException();
        }
    }
}
