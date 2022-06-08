package rohlik.casares.casestudy.utils;

import java.util.List;
import java.util.stream.Collectors;
import rohlik.casares.casestudy.dto.ProductDto;
import rohlik.casares.casestudy.model.Product;

public class ProductMapper {

    private ProductMapper(){}

    public static Product mapToProduct(ProductDto productDto) {
        return Product.builder()
                      .id(productDto.getProductId())
                      .name(productDto.getName())
                      .price(productDto.getPrice())
                      .quantity(productDto.getQuantity())
                      .build();
    }

    public static ProductDto mapToProductDto(Product product) {
        return ProductDto.builder()
                         .productId(product.getId())
                         .name(product.getName())
                         .price(product.getPrice())
                         .quantity(product.getQuantity())
                         .build();

    }

    public static List<ProductDto> mapToProductDtoList(List<Product> source) {
        return source
                .stream()
                .map(ProductMapper::mapToProductDto)
                .collect(Collectors.toList());
    }

    public static List<Product> mapToProductList(List<ProductDto> source) {
        return source
                .stream()
                .map(ProductMapper::mapToProduct)
                .collect(Collectors.toList());
    }
}
