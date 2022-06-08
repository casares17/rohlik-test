package rohlik.casares.casestudy.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import rohlik.casares.casestudy.dto.OrderDto;
import rohlik.casares.casestudy.dto.OrderProductDto;
import rohlik.casares.casestudy.exception.OrderNotFoundException;
import rohlik.casares.casestudy.exception.OrderServiceException;
import rohlik.casares.casestudy.exception.OrderStatusOperationException;
import rohlik.casares.casestudy.exception.ProductNotFoundException;
import rohlik.casares.casestudy.exception.StockExceededException;
import rohlik.casares.casestudy.model.Order;
import rohlik.casares.casestudy.model.OrderProduct;
import rohlik.casares.casestudy.model.OrderStatus;
import rohlik.casares.casestudy.model.Product;
import rohlik.casares.casestudy.repository.OrderProductRepository;
import rohlik.casares.casestudy.repository.OrderRepository;
import rohlik.casares.casestudy.repository.ProductRepository;
import rohlik.casares.casestudy.service.OrderService;
import rohlik.casares.casestudy.utils.OrderMapper;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderProductRepository orderProductRepository;

    @Autowired
    private ProductRepository productRepository;


    @Override
    public OrderDto createOrder(OrderDto orderDto) throws OrderServiceException {
        try {
            final OrderDto newOrder = createNewOrder(orderDto);
            if (Objects.isNull(newOrder)) {
                return null;
            }
            cancelAbandonedOrderAfter30Minutes(newOrder.getOrderId());

            return newOrder;
        }
        catch (RuntimeException e) {
            throw new OrderServiceException(e);
        }
    }

    @Override
    public OrderDto cancelOrder(Long orderId) throws OrderNotFoundException, OrderStatusOperationException {

        final Order order = orderRepository.findById(orderId)
                                           .orElseThrow(() -> new OrderNotFoundException(
                                                   String.format("Order with id [%d] does not exist", orderId)));

        checkOrderStatus(order);

        final List<Product> productsById = productRepository.findAllById(
                order.getOrderProducts().stream().map(op -> op.getProduct().getId()).collect(Collectors.toList()));
        final Map<Long, Product> stockMap = productsById.stream()
                                                        .collect(Collectors.toMap(Product::getId, Function.identity()));
        List<Product> productsToUpdate = order.getOrderProducts().stream().map(op -> {
            final Product product = stockMap.get(op.getProduct().getId());
            product.setQuantity(product.getQuantity() + op.getProductQuantity());
            return product;
        }).collect(Collectors.toList());
        productRepository.saveAll(productsToUpdate);

        order.setStatus(OrderStatus.CANCELLED);
        final Order orderCancelled = orderRepository.save(order);
        return OrderMapper.mapToOrderDto(orderCancelled);
    }

    @Override
    public OrderDto payOrder(Long orderId) throws OrderNotFoundException, OrderStatusOperationException {
        final Order order = orderRepository.findById(orderId)
                                           .orElseThrow(() -> new OrderNotFoundException(
                                                   String.format("Order with id [%d] does not exist", orderId)));

        checkOrderStatus(order);

        order.setStatus(OrderStatus.PAID);
        final Order orderPaid = orderRepository.save(order);
        return OrderMapper.mapToOrderDto(orderPaid);
    }

    @Async
    private void cancelAbandonedOrderAfter30Minutes(Long orderId) {
        new Thread(() -> {
            try {
                Thread.sleep(30L * 60L * 1000);
                cancelOrder(orderId);
            }
            catch (OrderNotFoundException | OrderStatusOperationException e) {
                throw new RuntimeException(e);
            }
            catch (InterruptedException e){
                Thread.currentThread().interrupt();
            }

        }).start();
    }


    private OrderDto createNewOrder(OrderDto orderDto) throws OrderServiceException {
        try {
            final List<OrderProductDto> orderProductDtoList = orderDto.getProducts();
            Map<Long, Product> stockMap = getStockMap(orderProductDtoList);

            final Order order = Order.builder()
                                     .total(getTotal(orderProductDtoList, stockMap))
                                     .status(OrderStatus.CREATED)
                                     .build();

            final Order savedOrder = orderRepository.save(order);

            final List<OrderProduct> orderProducts = buildOrderProducts(savedOrder, orderProductDtoList, stockMap);
            checkAndUpdateProductQuantities(orderProducts, stockMap);
            orderProductRepository.saveAll(orderProducts);
            savedOrder.setOrderProducts(orderProducts);
            return OrderMapper.mapToOrderDto(savedOrder);
        }
        catch (StockExceededException | ProductNotFoundException e) {
            throw new OrderServiceException(e);
        }
    }


    private List<OrderProduct> buildOrderProducts(
            Order order, List<OrderProductDto> orderProductDtoList, Map<Long, Product> stockMap
    ) {

        return orderProductDtoList.stream()
                                  .map(op -> OrderProduct.builder()
                                                         .order(order)
                                                         .product(stockMap.get(
                                                                 op.getProduct().getProductId()))
                                                         .productQuantity(op.getProductQuantity())
                                                         .build())
                                  .collect(Collectors.toList());
    }

    private void checkOrderStatus(Order order) throws OrderStatusOperationException {
        if (OrderStatus.CANCELLED.equals(order.getStatus())) {
            throw new OrderStatusOperationException(
                    String.format(
                            "Operation cannot be performed. Order with id [%d] is already cancelled", order.getId()));
        }
        if (OrderStatus.PAID.equals(order.getStatus())) {
            throw new OrderStatusOperationException(
                    String.format("Operation cannot be performed. Order with id [%d] is already paid", order.getId()));
        }
    }

    private BigDecimal getTotal(List<OrderProductDto> products, Map<Long, Product> stockMap) {
        return products.stream()
                       .map(op -> stockMap.get(op.getProduct().getProductId())
                                          .getPrice()
                                          .multiply(BigDecimal.valueOf(op.getProductQuantity())))
                       .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void checkAndUpdateProductQuantities(
            List<OrderProduct> orderProductDtoList, Map<Long, Product> stockMap
    ) throws StockExceededException {

        final List<OrderProduct> exceededProducts = orderProductDtoList.stream()
                                                                       .filter(op -> op.getProductQuantity() > stockMap.get(
                                                                                                                               op.getProduct().getId())
                                                                                                                       .getQuantity())
                                                                       .collect(
                                                                               Collectors.toList());
        if (!CollectionUtils.isEmpty(exceededProducts)) {
            throw new StockExceededException(String.format(
                    "Order quantity exceeded stock for the following products: \n" +
                            "* %s", exceededProducts.stream()
                                                    .map(ep -> String.format(
                                                            "%s - Missing items: %d",
                                                            ep.getProduct().getName(),
                                                            ep.getProductQuantity() - ep.getProduct()
                                                                                        .getQuantity()
                                                    ))
                                                    .collect(
                                                            Collectors.joining("\n* "))));
        }

        List<Product> productsToUpdate = orderProductDtoList.stream().map(op -> {
            final Product product = stockMap.get(op.getProduct().getId());
            product.setQuantity(product.getQuantity() - op.getProductQuantity());
            return product;
        }).collect(Collectors.toList());
        productRepository.saveAll(productsToUpdate);
    }

    private Map<Long, Product> getStockMap(List<OrderProductDto> orderProductDtoList)
            throws ProductNotFoundException {

        final List<Long> productIdList = orderProductDtoList.stream()
                                                            .map(op -> op.getProduct().getProductId())
                                                            .collect(Collectors.toList());
        final List<Product> productsById = productRepository.findAllById(productIdList);

        final Map<Long, Product> stockMap = productsById.stream()
                                                        .collect(Collectors.toMap(Product::getId, Function.identity()));
        if (!stockMap.keySet().containsAll(productIdList)) {
            throw new ProductNotFoundException("One or more products were not found");
        }
        return stockMap;
    }


}
