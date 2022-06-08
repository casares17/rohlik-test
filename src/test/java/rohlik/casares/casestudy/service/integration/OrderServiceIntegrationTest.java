package rohlik.casares.casestudy.service.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.client.HttpClientErrorException;
import rohlik.casares.casestudy.dto.OrderDto;
import rohlik.casares.casestudy.dto.OrderProductDto;
import rohlik.casares.casestudy.dto.ProductDto;
import rohlik.casares.casestudy.exception.OrderNotFoundException;
import rohlik.casares.casestudy.exception.OrderServiceException;
import rohlik.casares.casestudy.exception.OrderStatusOperationException;
import rohlik.casares.casestudy.exception.ProductNotFoundException;
import rohlik.casares.casestudy.exception.StockExceededException;
import rohlik.casares.casestudy.model.Order;
import rohlik.casares.casestudy.model.OrderProduct;
import rohlik.casares.casestudy.model.OrderStatus;
import rohlik.casares.casestudy.model.Product;
import rohlik.casares.casestudy.repository.ProductRepository;
import rohlik.casares.casestudy.service.OrderService;
import rohlik.casares.casestudy.service.impl.OrderServiceImpl;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class OrderServiceIntegrationTest {

    @Autowired
    OrderService orderService;

    @Autowired
    ProductRepository productRepository;


    @ParameterizedTest
    @MethodSource("createOrderSuccessCases")
    void testCreateOrder_success(
            List<OrderProductDto> orderProductDtoList, OrderStatus expectedStatus, BigDecimal expectedTotal
    ) throws OrderServiceException {
        OrderDto orderDtoInput = OrderDto.builder()
                                         .products(orderProductDtoList)
                                         .build();

        OrderDto response = orderService.createOrder(orderDtoInput);
        assertNotNull(response);
        assertNotNull(response.getOrderId());
        assertEquals(expectedStatus, response.getStatus());
        assertEquals(expectedTotal, response.getTotal());

        for (OrderProductDto orderProductDto : orderProductDtoList) {
            final Product productInRepo = productRepository.findById(orderProductDto.getProduct().getProductId())
                                                           .orElse(null);
            assertNotNull(productInRepo);

            assertNotNull(orderProductDto);
            assertEquals(
                    orderProductDto.getProduct().getQuantity() - orderProductDto.getProductQuantity(),
                    productInRepo.getQuantity()
            );
        }
    }

    @ParameterizedTest
    @MethodSource("createOrderErrorCases")
    void testCreateOrder_error(
            List<OrderProductDto> orderProductDtoList, OrderStatus expectedStatus, BigDecimal expectedTotal,
            Class<Exception> exceptionClass
    ) {
        OrderDto orderDtoInput = OrderDto.builder()
                                         .products(orderProductDtoList)
                                         .build();

        try {
            orderService.createOrder(orderDtoInput);
        }
        catch (OrderServiceException e) {
            assertEquals(exceptionClass, e.getCause().getClass());
        }

    }


    @Test
    void testCancelOrder_success()
            throws OrderServiceException, IOException, OrderNotFoundException, OrderStatusOperationException {
        final Long orderId = createNewOrder();

        OrderDto response = orderService.cancelOrder(orderId);
        assertNotNull(response);
        assertNotNull(response.getOrderId());
        assertEquals(OrderStatus.CANCELLED, response.getStatus());
    }

    @Test
    void testCancelOrder_notFound() {

        assertThrows(OrderNotFoundException.class, () -> orderService.cancelOrder(10000L));
    }

    @Test
    void testCancelOrder_alreadyCancelled()
            throws IOException, OrderServiceException, OrderNotFoundException, OrderStatusOperationException {

        final Long orderId = createNewOrder();

        orderService.cancelOrder(orderId);

        assertThrows(OrderStatusOperationException.class, () -> orderService.cancelOrder(orderId));

    }

    @Test
    void testPayOrder_success()
            throws OrderServiceException, IOException, OrderNotFoundException, OrderStatusOperationException {
        final Long orderId = createNewOrder();

        OrderDto response = orderService.payOrder(orderId);
        assertNotNull(response);
        assertNotNull(response.getOrderId());
        assertEquals(OrderStatus.PAID, response.getStatus());
    }

    @Test
    void testPayOrder_notFound() {

        assertThrows(OrderNotFoundException.class, () -> orderService.payOrder(10000L));
    }

    @Test
    void testPayOrder_alreadyPaid()
            throws IOException, OrderServiceException, OrderNotFoundException, OrderStatusOperationException {

        final Long orderId = createNewOrder();

        orderService.payOrder(orderId);

        assertThrows(OrderStatusOperationException.class, () -> orderService.payOrder(orderId));

    }


    public static Stream<Arguments> createOrderSuccessCases() {
        return Stream.of(
                oneProduct(),
                twoProducts()
        );
    }

    public static Stream<Arguments> createOrderErrorCases() {
        return Stream.of(
                productNotFound(),
                productExceededStock()
        );
    }

    private static Arguments oneProduct() {
        return Arguments.of(
                List.of(OrderProductDto.builder()
                                       .product(ProductDto.builder()
                                                          .productId(
                                                                  1L)
                                                          .name("Apple")
                                                          .quantity(
                                                                  10)
                                                          .price(new BigDecimal(
                                                                  "2"))
                                                          .build())
                                       .productQuantity(3)
                                       .build()),
                OrderStatus.CREATED,
                new BigDecimal("6.00")

        );
    }

    private static Arguments twoProducts() {
        return Arguments.of(
                Arrays.asList(
                        OrderProductDto.builder()
                                       .product(ProductDto.builder()
                                                          .productId(
                                                                  1L)
                                                          .name("Apple")
                                                          .quantity(
                                                                  10)
                                                          .price(new BigDecimal(
                                                                  "2.00"))
                                                          .build())
                                       .productQuantity(3)
                                       .build(),
                        OrderProductDto.builder()
                                       .product(ProductDto.builder()
                                                          .productId(
                                                                  2L)
                                                          .name("Orange")
                                                          .quantity(
                                                                  8)
                                                          .price(new BigDecimal(
                                                                  "3.00"))
                                                          .build())
                                       .productQuantity(1)
                                       .build()
                ),
                OrderStatus.CREATED,
                new BigDecimal("9.00")

        );
    }

    private static Arguments productNotFound() {
        return Arguments.of(
                List.of(
                        OrderProductDto.builder()
                                       .product(ProductDto.builder()
                                                          .productId(
                                                                  999L)
                                                          .name("Apple")
                                                          .quantity(
                                                                  10)
                                                          .price(new BigDecimal(
                                                                  "2.00"))
                                                          .build())
                                       .productQuantity(3)
                                       .build()
                ),
                OrderStatus.CREATED,
                new BigDecimal("6.00"),
                ProductNotFoundException.class

        );
    }

    private static Arguments productExceededStock() {
        return Arguments.of(
                List.of(
                        OrderProductDto.builder()
                                       .product(ProductDto.builder()
                                                          .productId(
                                                                  1L)
                                                          .name("Apple")
                                                          .quantity(
                                                                  1)
                                                          .price(new BigDecimal(
                                                                  "2.00"))
                                                          .build())
                                       .productQuantity(100)
                                       .build(),
                        OrderProductDto.builder()
                                       .product(ProductDto.builder()
                                                          .productId(
                                                                  2L)
                                                          .name("Orange")
                                                          .quantity(
                                                                  0)
                                                          .price(new BigDecimal(
                                                                  "3.00"))
                                                          .build())
                                       .productQuantity(100)
                                       .build()
                ),
                OrderStatus.CREATED,
                new BigDecimal("6.00"),
                StockExceededException.class

        );
    }

    private Long createNewOrder() throws IOException, OrderServiceException {
        final OrderDto order = orderService.createOrder(getOrderDto());
        return order.getOrderId();
    }

    private OrderDto getOrderDto() throws IOException {
        return new ObjectMapper().readValue(
                getClass().getClassLoader().getResourceAsStream("createOrderInput.json"), OrderDto.class);
    }
}
