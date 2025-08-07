package com.kavindu.ecommerce.product;

import com.kavindu.ecommerce.exception.ProductPurchaseException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductService(ProductRepository productRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    public Integer createProduct(ProductRequest request) {
        var product= productMapper.toproduct(request);
        return productRepository.save(product).getId();
    }

    public ProductResponse findById(Integer productId) {
        return productRepository.findById(productId)
                .map(productMapper::toProductResponse)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with ID:: " + productId));
    }


    public List<ProductResponse> findAll() {
        return productRepository.findAll()
                .stream()
                .map(productMapper::toProductResponse)
                .collect(Collectors.toList());
    }

    @Transactional(rollbackFor = ProductPurchaseException.class)
    public List<ProductPurchaseResponse> purchaseProducts(List<ProductPurchaseRequest> requests) {

        var productIds=requests
                .stream()
                .map(ProductPurchaseRequest :: productId)
                .toList();

        var storedProducts=productRepository.findAllByIdInOrderById(productIds);

        if (productIds.size() != storedProducts.size()) {
            throw new ProductPurchaseException("One or more products does not exist");
        }

        var sortedRequest = requests
                .stream()
                .sorted(Comparator.comparing(ProductPurchaseRequest::productId))
                .toList();

        var purchasedProducts = new ArrayList<ProductPurchaseResponse>();

        for(int i=0;i<storedProducts.size();i++){
            var product=storedProducts.get(i);
            var productRequest=sortedRequest.get(i);

            if(product.getAvailableQuantity()<productRequest.quantity()){
                throw new ProductPurchaseException("Insufficient stock quantity for product with ID:: " + productRequest.productId());
            }

            var newAwilbleQuantitiy=product.getAvailableQuantity()-productRequest.quantity();
            product.setAvailableQuantity(newAwilbleQuantitiy);
            productRepository.save(product);
            purchasedProducts.add(productMapper.toproductPurchaseResponse(product,productRequest.quantity()));
        }
        return purchasedProducts;




    }
}
