package rohlik.casares.casestudy.service;


import rohlik.casares.casestudy.dto.OrderDto;
import rohlik.casares.casestudy.exception.OrderNotFoundException;
import rohlik.casares.casestudy.exception.OrderServiceException;
import rohlik.casares.casestudy.exception.OrderStatusOperationException;

public interface OrderService {

    OrderDto createOrder(OrderDto orderDto) throws OrderServiceException;

    OrderDto cancelOrder(Long orderId) throws OrderNotFoundException, OrderStatusOperationException;

    OrderDto payOrder(Long orderId) throws OrderNotFoundException, OrderStatusOperationException;

}
