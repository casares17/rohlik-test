package rohlik.casares.casestudy.service;

import java.util.List;
import rohlik.casares.casestudy.dto.ProductDto;
import rohlik.casares.casestudy.exception.ProductNotFoundException;

public interface ProductService {

    ProductDto createProduct(ProductDto productDto);

    ProductDto updateProduct(Long id, ProductDto productDto) throws ProductNotFoundException;

    void deleteProduct(Long productId) throws ProductNotFoundException;

    List<ProductDto> findAll();


}
