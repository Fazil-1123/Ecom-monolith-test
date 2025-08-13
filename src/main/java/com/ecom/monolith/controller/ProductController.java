package com.ecom.monolith.controller;

import com.ecom.monolith.Dto.ProductDto;
import com.ecom.monolith.service.ProductService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<ProductDto>> getAllProducts() {
        logger.info("GET /api/products - Fetching all products");
        List<ProductDto> productDtos = productService.getAllProducts();
        return ResponseEntity.ok(productDtos);
    }

    @GetMapping("{id}")
    public ResponseEntity<ProductDto> findById(@PathVariable("id") Long id) {
        logger.info("GET /api/products/{} - Fetching product by ID", id);
        ProductDto productDto = productService.findById(id);
        return ResponseEntity.ok(productDto);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductDto>> findByKeyword(@RequestParam("keyword") String keyword) {
        logger.info("GET /api/products/search?keyword={} - Searching products", keyword);
        List<ProductDto> productDtos = productService.findByKeyword(keyword);
        return ResponseEntity.ok(productDtos);
    }

    @PostMapping
    public ResponseEntity<ProductDto> addProduct(@Valid @RequestBody ProductDto productDto) {
        logger.info("POST /api/products - Adding new product with name={}", productDto.getName());
        ProductDto savedProduct = productService.addProduct(productDto);
        return ResponseEntity.ok(savedProduct);
    }

    @PutMapping("{id}")
    public ResponseEntity<ProductDto> updateProduct(@PathVariable("id") Long id, @Valid @RequestBody ProductDto productDto) {
        logger.info("PUT /api/products/{} - Updating product with name={}", id, productDto.getName());
        ProductDto savedProduct = productService.updateProduct(id, productDto);
        return ResponseEntity.ok(savedProduct);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable("id") Long id) {
        logger.info("DELETE /api/products/{} - Deleting product", id);
        String response = productService.deleteProduct(id);
        logger.info("Product deleted. Response: {}", response);
        return ResponseEntity.ok(response);
    }
}
