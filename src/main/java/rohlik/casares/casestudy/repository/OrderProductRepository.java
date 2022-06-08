package rohlik.casares.casestudy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rohlik.casares.casestudy.model.OrderProduct;

public interface OrderProductRepository  extends JpaRepository<OrderProduct, Long> {
}
