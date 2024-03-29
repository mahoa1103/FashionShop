package group4.organicapplication.repository;

import group4.organicapplication.model.Product;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    @Query("SELECT p FROM Product p JOIN ImportBill ib ON p.productID = ib.product.productID JOIN ImportProduct ip ON ib.importProduct.importID = ip.importID WHERE ip.supplier.supplierID = :supplierId and ib.product.deleted = false")
    List<Product> findProductsBySupplierId(int supplierId);

    @Query("SELECT p FROM Product p JOIN ImportBill ib ON p.productID = ib.product.productID JOIN ImportProduct ip ON ib.importProduct.importID = ip.importID WHERE ip.supplier.supplierID = :supplierId and ib.product.deleted = true")
    List<Product> findProductsDeleteBySupplierId(int supplierId);
    @Query("SELECT p FROM Product p WHERE p.productName like %:searchName% and p.deleted = false ")
    List<Product> findByProductName(String searchName);

    @Query("SELECT p FROM Product p WHERE p.productName like %:searchName% and p.deleted = true ")
    List<Product> findByProductNameDeleted(String searchName);
    @Query("SELECT p FROM Product p WHERE p.category.categoryID = :categoryID and p.deleted = false")
    List<Product> findByCategoryId(Integer categoryID);

    List<Product> findByProductNameContainingIgnoreCase(String searchProductName);

    @Query("SELECT p FROM Product p WHERE p.productID = :productID")
    Product findByProductID(@Param("productID") int productID);

    @Query("SELECT p FROM Product p WHERE p.deleted = false")
    List<Product> findProduct();

    @Query("SELECT p FROM Product p WHERE p.deleted = true")
    List<Product> findProductDeleted();

}
