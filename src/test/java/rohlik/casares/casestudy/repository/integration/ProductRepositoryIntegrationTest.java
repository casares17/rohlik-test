package rohlik.casares.casestudy.repository.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.util.List;
import org.aspectj.lang.annotation.After;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import rohlik.casares.casestudy.model.Product;
import rohlik.casares.casestudy.repository.ProductRepository;

@ExtendWith(SpringExtension.class)
@DataJpaTest
class ProductRepositoryIntegrationTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    void testSave_success() {
        final long sizeBefore = productRepository.count();
        final Product cherry = Product.builder()
                                      .name("Cherry")
                                      .quantity(4)
                                      .price(new BigDecimal("12"))
                                      .build();
        final Product product = productRepository.save(cherry);
        assertNotNull(product);
        assertNotNull(product.getId());
        assertEquals("Cherry", product.getName());

        final long sizeAfter = productRepository.count();

        assertEquals(sizeBefore + 1, sizeAfter);
    }

    @Test
    void testFindById_success() {
        final Product product = productRepository.findById(1L).orElse(null);
        assertNotNull(product);
        assertEquals("Apple", product.getName());
    }

}