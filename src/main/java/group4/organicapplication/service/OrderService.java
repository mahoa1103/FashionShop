package group4.organicapplication.service;

import group4.organicapplication.dto.SearchOrderObject;
import group4.organicapplication.model.Orders;
import group4.organicapplication.model.User;
import org.attoparser.ParseException;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface OrderService {

//    Page<Orders> getAllOrderByFilter(SearchOrderObject object, int page) throws ParseException;
    List<Orders> findByOrderStatus(String orderStatus);
    int countByOrderStatus(String orderStatus);

    Orders updateOrder(Orders order);

    Orders findById(long id);

    Orders save(Orders order);
}
