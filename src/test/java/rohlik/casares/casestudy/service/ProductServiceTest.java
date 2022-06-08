package rohlik.casares.casestudy.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import rohlik.casares.casestudy.dto.ProductDto;
import rohlik.casares.casestudy.model.Product;
import rohlik.casares.casestudy.repository.ProductRepository;
import rohlik.casares.casestudy.service.impl.ProductServiceImpl;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    ProductRepository productRepository;

    @InjectMocks
    ProductServiceImpl service;

    @Test
    void testCreateProduct_success() {
        Product product = Product.builder()
                                 .id(1L)
                                 .name("productName")
                                 .quantity(2)
                                 .price(BigDecimal.valueOf(10))
                                 .build();
        when(productRepository.save(any())).thenReturn(product);

        ProductDto productDto = ProductDto.builder()
                                          .name("productName")
                                          .quantity(2)
                                          .price(BigDecimal.valueOf(10))
                                          .build();


        ProductDto productSaved = service.createProduct(productDto);
        assertNotNull(productSaved);
        assertEquals(product.getId(), productSaved.getProductId());
        assertEquals(product.getName(), productSaved.getName());
        assertEquals(product.getQuantity(), productSaved.getQuantity());
        assertEquals(product.getPrice(), productSaved.getPrice());

    }



}