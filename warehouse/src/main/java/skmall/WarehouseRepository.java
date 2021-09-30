package skmall;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel="warehouses", path="warehouses")
public interface WarehouseRepository extends PagingAndSortingRepository<Warehouse, Long>{


}
