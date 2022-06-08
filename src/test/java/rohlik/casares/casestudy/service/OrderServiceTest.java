package rohlik.casares.casestudy.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import rohlik.casares.casestudy.dto.OrderDto;
import rohlik.casares.casestudy.dto.OrderProductDto;
import rohlik.casares.casestudy.dto.ProductDto;
import rohlik.casares.casestudy.exception.OrderServiceException;
import rohlik.casares.casestudy.model.Order;
import rohlik.casares.casestudy.model.OrderStatus;
import rohlik.casares.casestudy.model.Product;
import rohlik.casares.casestudy.repository.OrderProductRepository;
import rohlik.casares.casestudy.repository.OrderRepository;
import rohlik.casares.casestudy.repository.ProductRepository;
import rohlik.casares.casestudy.service.impl.OrderServiceImpl;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    private static final Product PRODUCT_1 = Product.builder()
                                                    .id(1L)
                                                    .name("productName1")
                                                    .quantity(2)
                                                    .price(BigDecimal.valueOf(2))
                                                    .build();
    private static final ProductDto PRODUCT_DTO_1 = ProductDto.builder()
                                                              .productId(PRODUCT_1.getId())
                                                              .name(PRODUCT_1.getName())
                                                              .quantity(PRODUCT_1.getQuantity())
                                                              .price(PRODUCT_1.getPrice())
                                                              .build();
    private static final Product PRODUCT_2 = Product.builder()
                                                    .id(2L)
                                                    .name("productName2")
                                                    .quantity(3)
                                                    .price(BigDecimal.valueOf(3))
                                                    .build();
    private static final ProductDto PRODUCT_DTO_2 = ProductDto.builder()
                                                              .productId(PRODUCT_2.getId())
                                                              .name(PRODUCT_2.getName())
                                                              .quantity(PRODUCT_2.getQuantity())
                                                              .price(PRODUCT_2.getPrice())
                                                              .build();
    @Mock
    OrderRepository orderRepository;

    @Mock
    ProductRepository productRepository;

    @Mock
    OrderProductRepository orderProductRepository;

    @InjectMocks
    OrderServiceImpl service;

    @ParameterizedTest
    @MethodSource("createOrderSuccessCases")
    void testCreateOrder_success(List<Product> productList, List<OrderProductDto> orderProductDtoList, Order savedOrder)
            throws OrderServiceException {

        when(productRepository.findAllById(any())).thenReturn(productList);

        when(orderRepository.save(any())).thenReturn(savedOrder);

        OrderDto orderDtoInput = OrderDto.builder()
                                         .products(
                                                 orderProductDtoList
                                         )
                                         .build();

        OrderDto response = service.createOrder(orderDtoInput);
        assertNotNull(response);
        assertEquals(savedOrder.getId(), response.getOrderId());
        assertEquals(savedOrder.getStatus(), response.getStatus());
        assertEquals(savedOrder.getTotal(), response.getTotal());


    }

    public static Stream<Arguments> createOrderSuccessCases() {
        return Stream.of(
                twoProducts()
        );
    }

    private static Arguments twoProducts() {
        return Arguments.of(
                Arrays.asList(PRODUCT_1, PRODUCT_2),
                Arrays.asList(
                        OrderProductDto.builder()
                                       .product(PRODUCT_DTO_1)
                                       .productQuantity(2)
                                       .build(),
                        OrderProductDto.builder()
                                       .product(PRODUCT_DTO_2)
                                       .productQuantity(1)
                                       .build()
                ),
                Order.builder()
                     .id(1L)
                     .total(new BigDecimal("7"))
                     .status(OrderStatus.CREATED)
                     .build()
        );
    }

}