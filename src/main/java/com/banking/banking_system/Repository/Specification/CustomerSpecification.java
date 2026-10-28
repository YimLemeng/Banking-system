package com.banking.banking_system.Repository.Specification;

import com.banking.banking_system.Entity.Customer;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class CustomerSpecification {
    public static Specification<Customer> filterCustomer(String searchKeyword){
        return (root, query, cb) -> {
            if (searchKeyword == null || searchKeyword.trim().isEmpty()) {
                return cb.conjunction();
            }

            String pattern = "%" + searchKeyword.trim().toLowerCase() + "%";
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.like(cb.lower(root.get("firstName")), pattern));
            predicates.add(cb.like(cb.lower(root.get("lastName")), pattern));
            predicates.add(cb.like(cb.lower(root.get("email")), pattern));
            predicates.add(cb.like(cb.lower(root.get("phoneNumber")), pattern));
            predicates.add(cb.like(cb.lower(root.get("nationalIdOrPassport")), pattern));

            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }
}
