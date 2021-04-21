package cn.enaium.learn.satis.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Enaium
 */
@Data
@AllArgsConstructor
public class AccountEntity {
    private Long id;
    private String name;
    private Integer age;

    public AccountEntity() {

    }
}
