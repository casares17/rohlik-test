package rohlik.casares.casestudy.controller;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.stream.Collectors;
import org.hibernate.mapping.Collection;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import rohlik.casares.casestudy.dto.OrderDto;
import rohlik.casares.casestudy.dto.OrderProductDto;
import rohlik.casares.casestudy.dto.ProductDto;
import rohlik.casares.casestudy.exception.OrderNotFoundException;
import rohlik.casares.casestudy.exception.OrderServiceException;
import rohlik.casares.casestudy.exception.OrderStatusOperationException;
import rohlik.casares.casestudy.model.OrderStatus;
import rohlik.casares.casestudy.repository.OrderRepository;
import rohlik.casares.casestudy.repository.ProductRepository;
import rohlik.casares.casestudy.service.OrderService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class OrderControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderService orderService;

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
        baseUrl = baseUrl + ":" + port + "/api/orders";
    }


    @Test
    void createOrder_shouldCreate() throws IOException {
        final long countBefore = orderRepository.count();

        final OrderDto orderDto = getOrderDto();

        final OrderDto orderCreated = restTemplate.postForObject(baseUrl, orderDto, OrderDto.class);

        assertNotNull(orderCreated);
        assertNotNull(orderCreated.getOrderId());
        assertEquals(OrderStatus.CREATED, orderCreated.getStatus());
        assertTrue(orderCreated.getProducts()
                               .stream()
                               .map(p -> p.getProduct().getProductId())
                               .collect(Collectors.toList())
                               .containsAll(
                                       orderDto.getProducts().stream().map(p -> p.getProduct().getProductId()).collect(
                                               Collectors.toList())));
        assertNotNull(orderCreated.getTotal());

        final long countAfter = orderRepository.count();
        assertEquals(countAfter, countBefore + 1);
    }

    @Test
    void createOrder_errorProductNotFound() throws IOException {

        final OrderDto orderDto = getOrderDtoProductNotFound();

        assertThrows(HttpServerErrorException.InternalServerError.class, () -> restTemplate.postForObject(baseUrl, orderDto, OrderDto.class));
    }


    @Test
    void cancelOrder_shouldCancel() throws IOException, OrderServiceException {

        final OrderDto orderDto = getOrderDto();
        final OrderDto order = orderService.createOrder(orderDto);

        final OrderDto orderCancel = restTemplate.postForObject(
                baseUrl + "/" + order.getOrderId() + "/cancel", null, OrderDto.class);

        assertNotNull(orderCancel);
        assertEquals(1L, orderCancel.getOrderId());
        assertEquals(OrderStatus.CANCELLED, orderCancel.getStatus());
    }

    @Test
    void cancelOrder_notFound() {

        assertThrows(HttpClientErrorException.NotFound.class, () -> restTemplate.postForObject(baseUrl + "/100/cancel", null, OrderDto.class));
    }

    @Test
    void cancelOrder_alreadyCancelled()
            throws IOException, OrderServiceException, OrderNotFoundException, OrderStatusOperationException {
        final OrderDto orderDto = getOrderDto();
        final OrderDto order = orderService.createOrder(orderDto);
        orderService.cancelOrder(order.getOrderId());

        assertThrows(HttpServerErrorException.InternalServerError.class, () -> restTemplate.postForObject(baseUrl + "/" + order.getOrderId() + "/cancel", null, OrderDto.class));

    }

    @Test
    void payOrder_shouldPay() throws IOException, OrderServiceException {

        final OrderDto orderDto = getOrderDto();
        final OrderDto order = orderService.createOrder(orderDto);

        final OrderDto orderPaid = restTemplate.postForObject(
                baseUrl + "/" + order.getOrderId() + "/pay", null, OrderDto.class);

        assertNotNull(orderPaid);
        assertEquals(1L, orderPaid.getOrderId());
        assertEquals(OrderStatus.PAID, orderPaid.getStatus());
    }

    @Test
    void payOrder_notFound() {

        assertThrows(HttpClientErrorException.NotFound.class, () -> restTemplate.postForObject(baseUrl + "/100/pay", null, OrderDto.class));
    }

    @Test
    void payOrder_alreadyPaid()
            throws IOException, OrderServiceException, OrderNotFoundException, OrderStatusOperationException {
        final OrderDto orderDto = getOrderDto();
        final OrderDto order = orderService.createOrder(orderDto);
        orderService.payOrder(order.getOrderId());

        assertThrows(HttpServerErrorException.InternalServerError.class, () -> restTemplate.postForObject(baseUrl + "/" + order.getOrderId() + "/pay", null, OrderDto.class));

    }

    private OrderDto getOrderDto() throws IOException {
        return objectMapper.readValue(
                getClass().getClassLoader().getResourceAsStream("createOrderInput.json"), OrderDto.class);
    }

    private OrderDto getOrderDtoProductNotFound() throws IOException {
        return OrderDto.builder()
                       .products(Collections.singletonList(
                               OrderProductDto.builder()
                                              .product(ProductDto.builder()
                                                                 .productId(999L)
                                                                 .build())
                                              .productQuantity(1)
                                              .build()))
                       .build();
    }

}