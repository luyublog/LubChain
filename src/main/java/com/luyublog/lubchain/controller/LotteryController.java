package com.luyublog.lubchain.controller;

import com.luyublog.lubchain.service.LotteryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * description: 抽奖工具
 *
 * @author luyublog
 * @date 2023/8/14 15:59.
 */
@RestController
@Slf4j
@Api(tags = {"抽奖工具"})
@RequestMapping("/tools")
public class LotteryController {
    @Autowired
    LotteryService lotteryService;

    @ApiOperation(value = "获取随机奖品")
    @GetMapping("/prize")
    public String getPrize(@RequestParam String 姓名) {
        return lotteryService.getPrize(姓名);
    }
}

