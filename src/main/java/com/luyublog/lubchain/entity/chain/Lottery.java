package com.luyublog.lubchain.entity.chain;

import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.luyublog.lubchain.entity.extra.LotteryPerson;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * description:
 * 抽奖：算法为取第1k个区块的hash（获取抽奖时eth最新区块hash）作为随机幸运数
 * <p>
 * 将参与人姓名的hash与随机hash连一起再取一次hash得到最终hash
 * <p>
 * 最后将hash按ASCII码进行排序
 *
 * @author luyublog
 * @date 2023/8/14 9:27.
 */
public class Lottery {
    private final List<String> nameList = Arrays.asList("张三", "李四");

    public Map<String, String> nameMapAward = new HashMap<>(nameList.size());

    public String getLatestHash() {
        String resp = HttpUtil.post("https://api.shasta.trongrid.io/jsonrpc",
                "{ \"jsonrpc\": \"2.0\", \"method\": \"eth_getBlockByNumber\", \"params\": [\"latest\", true], \"id\": 114514 }");

        JSONObject jsonObject = JSONUtil.parseObj(resp);
//        System.out.println(resp);

        return jsonObject.getJSONObject("result").getStr("hash", "");
    }

    @PostConstruct
    public void getNameMapAward() {
        Lottery lottery = new Lottery();
        String luckyHash = lottery.getLatestHash();
        System.out.println(luckyHash);

        Map<String, LotteryPerson> nameMapHash = new TreeMap<>();

        nameList.forEach(x -> {
            nameMapHash.put(DigestUtil.sha256Hex(x + luckyHash), new LotteryPerson(x));
        });


        int index = 0;
        Map<String, String> nameMapAward = new HashMap<>(23);
        for (Map.Entry<String, LotteryPerson> entry : nameMapHash.entrySet()) {
            index++;
            if (index <= 4) {
                entry.getValue().setAward("升降桌");
            } else if (index > 4 && index <= 11) {
                entry.getValue().setAward("鼠标");
            } else if (index > 11 && index <= 18) {
                entry.getValue().setAward("键盘");
            } else if (index > 18 && index <= 20) {
                entry.getValue().setAward("手环");
            } else if (index > 20 && index <= 23) {
                entry.getValue().setAward("闹钟");
            }
            this.nameMapAward.put(entry.getValue().getName(), entry.getValue().getAward());
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }
    }

}

