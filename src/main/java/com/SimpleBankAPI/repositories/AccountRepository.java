package com.SimpleBankAPI.repositories;

import com.SimpleBankAPI.models.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account,Long> {

}
