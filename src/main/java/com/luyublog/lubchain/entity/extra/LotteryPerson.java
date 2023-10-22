package com.luyublog.lubchain.entity.extra;

import lombok.Data;

/**
 * description:
 *
 * @author luyublog
 * @date 2023/8/14 11:33.
 */
@Data
public class LotteryPerson {
    private String name;
    private String award;

    public LotteryPerson(String name) {
        this.name = name;
    }
}

