package com.codewithmosh.store.products;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/products")
@Tag(name = "Products")
public class ProductController {
    private final ProductService productService;
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final CategoryRepository categoryRepository;

    @GetMapping
    @Operation(summary = "checkout (login required)")
    public List<ProductDto> getAllProducts(
            @RequestParam(name = "categoryId", required = false) Long categoryId
    ) {
        return productService.getAllProducts(categoryId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "get a product by id")
    public ResponseEntity<ProductDto> getProduct(@PathVariable Long id) {
        var dto = productService.getProductById(id);
        return dto == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(dto);
    }


    @PostMapping
    @Operation(summary = "create product, don‘t add id filed (only admin)")
    public ResponseEntity<ProductDto> createProduct(
            @RequestBody ProductDto productDto,
            UriComponentsBuilder uriBuilder) {
        var dto = productService.createProduct(productDto);
        if (dto == null) {
            return ResponseEntity.badRequest().build();
        }
        var uri = uriBuilder.path("/products/{id}").buildAndExpand(dto.getId()).toUri();
        return ResponseEntity.created(uri).body(dto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "update product (only admin)")
    public ResponseEntity<?> updateProduct(
            @PathVariable Long id,
            @RequestBody ProductDto productDto) {
        var dto = productService.updateProduct(id, productDto);
        if (dto == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "delete product (only admin)")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        return productService.deleteProduct(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
