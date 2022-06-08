package rohlik.casares.casestudy.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import rohlik.casares.casestudy.model.OrderStatus;

@Builder(toBuilder = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderDto {

    private Long orderId;
    private List<OrderProductDto> products;
    private OrderStatus status;
    private BigDecimal total;

}
