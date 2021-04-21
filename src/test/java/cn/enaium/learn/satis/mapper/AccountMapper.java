package cn.enaium.learn.satis.mapper;

import cn.enaium.learn.satis.entity.AccountEntity;
import cn.eniaum.learn.satis.annotation.*;

import java.io.Serializable;
import java.util.List;

/**
 * @author Enaium
 */
public interface AccountMapper {
    @Select
    @SQL("select * from account")
    List<AccountEntity> getAll();

    @Select
    @SQL("select * from account where id = #{id}")
    AccountEntity getById(@Param("id") Serializable id);

    @Delete
    @SQL("delete from account where id = #{id}")
    void deleteById(@Param("id") Serializable id);

    @Insert
    @SQL("insert into account(id, name, age) values (#{id}, #{name}, #{age})")
    void insert(AccountEntity accountEntity);

    @Update
    @SQL("update account set name=#{name}, age=#{age} where id=#{id}")
    void update(AccountEntity accountEntity);
}
