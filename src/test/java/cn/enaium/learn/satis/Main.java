package cn.enaium.learn.satis;

import cn.enaium.learn.satis.entity.AccountEntity;
import cn.enaium.learn.satis.mapper.AccountMapper;
import cn.eniaum.learn.satis.Satis;
import com.google.common.collect.ImmutableMap;

/**
 * @author Enaium
 */
public class Main {
    public static void main(String[] args) throws Exception {
        Satis satis = new Satis(ImmutableMap.of(
                "url", "jdbc:mariadb://localhost:3306/enaium?useUnicode=true&characterEncoding=UTF-8",
                "driver", "org.mariadb.jdbc.Driver",
                "username", "root",
                "password", "root"));
        AccountMapper mapper = satis.getMapper(AccountMapper.class);
        System.out.println(mapper.getById(1));
        mapper.getAll().forEach(System.out::println);
        mapper.insert(new AccountEntity(0L, "Enaium", 1));
        mapper.getAll().forEach((it) -> {
            it.setAge(0);
            mapper.update(it);
        });
    }
}
