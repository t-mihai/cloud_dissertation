package com.ism.dissertation.repository;

import com.ism.dissertation.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepo extends JpaRepository<User, Integer> {

    @Query("select id from User where email = :email")
    Integer findIdUserByEmail(@Param("email") String email);

    @Query("select id from User where username = :username")
    Integer findIdUserByUsername(@Param("username") String username);

    @Query("select username from User where id = :id")
    String findUsernameById(@Param("id") Integer id);

    @Query("select id from User order by id desc")
    List<Integer> findLastId();

    @Query("select u from User u where email = :email")
    List<User> findByCredentials(@Param("email") String email);
}
