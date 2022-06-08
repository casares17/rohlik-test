package rohlik.casares.casestudy.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import rohlik.casares.casestudy.dto.OrderDto;
import rohlik.casares.casestudy.dto.ProductDto;
import rohlik.casares.casestudy.repository.ProductRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ProductControllerTest {


    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @LocalServerPort
    private int port;

    private String baseUrl = "http://localhost";

    private static RestTemplate restTemplate;

    @BeforeAll
    public static void init() {
        restTemplate = new RestTemplate();
    }

    @BeforeEach
    public void setUp() {
        baseUrl = baseUrl + ":" + port + "/api/products";
    }


    @Test
    void createProduct_shouldCreate() throws IOException {
        final long countBefore = productRepository.count();
        ProductDto productDto = getProductDto();

        final ProductDto productCreated = restTemplate.postForObject(baseUrl, productDto, ProductDto.class);

        assertNotNull(productCreated);
        assertNotNull(productCreated.getProductId());
        assertEquals(productDto.getName(), productCreated.getName());
        assertEquals(productDto.getPrice(), productCreated.getPrice());
        assertEquals(productDto.getQuantity(), productCreated.getQuantity());

        final long countAfter = productRepository.count();
        assertEquals(countAfter, countBefore + 1);
    }

    @Test
    void updateProduct_shouldUpdate() throws IOException {
        final long countBefore = productRepository.count();
        ProductDto productDto = getProductDtoUpdate();

        final ResponseEntity<ProductDto> response = restTemplate.exchange(
                baseUrl + "/1", HttpMethod.PUT, new HttpEntity<>(productDto), ProductDto.class);

        assertNotNull(response);
        final ProductDto productUpdated = response.getBody();
        assertNotNull(productUpdated);
        assertEquals(1L, productUpdated.getProductId());
        assertEquals(productDto.getName(), productUpdated.getName());
        assertEquals(productDto.getPrice(), productUpdated.getPrice());
        assertEquals(productDto.getQuantity(), productUpdated.getQuantity());

        final long countAfter = productRepository.count();
        assertEquals(countAfter, countBefore);
    }

    @Test
    void updateProduct_notFound() throws IOException {
        ProductDto productDto = getProductDto();

        assertThrows(HttpClientErrorException.NotFound.class, () -> restTemplate.put(baseUrl + "/100", productDto, ProductDto.class));
    }

    @Test
    void deleteProduct_shouldDelete() {
        final long countBefore = productRepository.count();

        restTemplate.delete(baseUrl + "/1");

        assertFalse(productRepository.findById(1L).isPresent());

        final long countAfter = productRepository.count();
        assertEquals(countAfter, countBefore - 1);
    }

    @Test
    void deleteProduct_notFound() {

        assertThrows(HttpClientErrorException.NotFound.class, () -> restTemplate.delete(baseUrl + "/100"));
    }

    private ProductDto getProductDto() throws IOException {
        return objectMapper.readValue(
                getClass().getClassLoader().getResourceAsStream("createProductInput.json"), ProductDto.class);
    }

    private ProductDto getProductDtoUpdate() throws IOException {
        return objectMapper.readValue(
                getClass().getClassLoader().getResourceAsStream("updateProductInput.json"), ProductDto.class);
    }

}