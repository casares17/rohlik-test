package rohlik.casares.casestudy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rohlik.casares.casestudy.model.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

}
