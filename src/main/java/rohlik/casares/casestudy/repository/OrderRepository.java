package rohlik.casares.casestudy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rohlik.casares.casestudy.model.Order;

public interface OrderRepository  extends JpaRepository<Order, Long> {

}
