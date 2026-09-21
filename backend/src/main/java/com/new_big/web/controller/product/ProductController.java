
package com.new_big.web.controller.product;

import com.new_big.web.controller.product.dto.ProductRequestDTO;
import com.new_big.web.controller.product.dto.ProductResponseDTO;
import com.new_big.web.entity.Product;
import com.new_big.web.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/products")
@CrossOrigin(origins = "http://localhost:4200")
public class ProductController {

	private final ProductService service;

	public ProductController(ProductService service) {
		this.service = service;
	}

	@GetMapping
	public List<ProductResponseDTO> getAllProducts() {
		return service.getAllProducts();
	}

	@PostMapping
	public ResponseEntity<Void> createProduct(@Valid @RequestBody ProductRequestDTO data) {
		service.createProduct(data);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@PutMapping("/{id}")
	public ResponseEntity<ProductResponseDTO> updateProduct(
			@PathVariable Long id,
			@Valid @RequestBody ProductRequestDTO data) {
		return service.updateProduct(id, data)
				.map(product -> ResponseEntity.ok(new ProductResponseDTO(product)))
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
		return service.deleteProduct(id)
				? ResponseEntity.noContent().build()
				: ResponseEntity.notFound().build();
	}
}

