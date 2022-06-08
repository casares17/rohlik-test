package rohlik.casares.casestudy.utils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import rohlik.casares.casestudy.dto.OrderDto;
import rohlik.casares.casestudy.dto.OrderProductDto;
import rohlik.casares.casestudy.model.Order;
import rohlik.casares.casestudy.model.OrderProduct;
import rohlik.casares.casestudy.model.Product;

public class OrderMapper {

    private OrderMapper(){}

    public static OrderDto mapToOrderDto(Order order) {
        return OrderDto.builder()
                       .orderId(order.getId())
                       .products(mapToOrderProductDtoList(order.getOrderProducts()))
                       .total(order.getTotal())
                       .status(order.getStatus())
                       .build();
    }

    public static List<OrderProductDto> mapToOrderProductDtoList(List<OrderProduct> orderProducts) {
        return Objects.isNull(orderProducts) ? null : orderProducts.stream()
                                                                   .map(OrderMapper::mapToOrderProductDto)
                                                                   .collect(Collectors.toList());
    }

    public static OrderProductDto mapToOrderProductDto(OrderProduct orderProduct) {
        return OrderProductDto.builder()
                              .product(ProductMapper.mapToProductDto(orderProduct.getProduct()))
                              .productQuantity(orderProduct.getProductQuantity())
                              .build();
    }

    public static List<OrderProduct> mapToOrderProductList(List<OrderProductDto> orderProducts) {
        return Objects.isNull(orderProducts) ? null : orderProducts.stream()
                                                                   .map(OrderMapper::mapToOrderProduct)
                                                                   .collect(Collectors.toList());
    }

    public static List<OrderProduct> mapToOrderProductList(
            List<OrderProductDto> orderProducts, Map<Long, Product> stockMap
    ) {
        return Objects.isNull(orderProducts) ? null : orderProducts.stream()
                                                                   .map(op -> mapToOrderProduct(
                                                                           op,
                                                                           stockMap.get(op.getProduct().getProductId())
                                                                   ))
                                                                   .collect(Collectors.toList());
    }

    public static OrderProduct mapToOrderProduct(OrderProductDto orderProductDto) {
        return OrderProduct.builder()
                           .product(ProductMapper.mapToProduct(orderProductDto.getProduct()))
                           .productQuantity(orderProductDto.getProductQuantity())
                           .build();
    }

    public static OrderProduct mapToOrderProduct(OrderProductDto orderProductDto, Product product) {
        return OrderProduct.builder()
                           .product(product)
                           .productQuantity(orderProductDto.getProductQuantity())
                           .build();
    }
}
