package rohlik.casares.casestudy.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rohlik.casares.casestudy.dto.ProductDto;
import rohlik.casares.casestudy.exception.ProductNotFoundException;
import rohlik.casares.casestudy.model.Product;
import rohlik.casares.casestudy.repository.ProductRepository;
import rohlik.casares.casestudy.service.ProductService;
import rohlik.casares.casestudy.utils.ProductMapper;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Override
    public ProductDto createProduct(ProductDto productDto) {
        Product product = ProductMapper.mapToProduct(productDto);
        final Product savedProduct = productRepository.save(product);
        return ProductMapper.mapToProductDto(savedProduct);
    }

    @Override
    public ProductDto updateProduct(Long id, ProductDto productDto) throws ProductNotFoundException {
        final Product productInDatabase = productRepository.findById(id)
                                                           .orElseThrow(() -> new ProductNotFoundException(
                                                                   String.format(
                                                                           "Product with id [%d] does not exist",
                                                                           productDto.getProductId()
                                                                   )));
        Product product = ProductMapper.mapToProduct(productDto)
                                       .toBuilder()
                                       .id(productInDatabase.getId())
                                       .build();
        final Product savedProduct = productRepository.save(product);
        return ProductMapper.mapToProductDto(savedProduct);
    }

    @Override
    public void deleteProduct(Long productId) throws ProductNotFoundException {
        Product product = productRepository.findById(productId)
                                           .orElseThrow(() -> new ProductNotFoundException(
                                                   String.format("Product with id [%d] does not exist", productId)));
        productRepository.delete(product);
    }

    @Override
    public List<ProductDto> findAll() {
        final List<Product> productList = productRepository.findAll();
        return ProductMapper.mapToProductDtoList(productList);
    }


}
