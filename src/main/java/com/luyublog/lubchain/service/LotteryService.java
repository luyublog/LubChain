package com.luyublog.lubchain.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.luyublog.lubchain.entity.chain.Block;
import com.luyublog.lubchain.entity.chain.Lottery;
import com.luyublog.lubchain.entity.chain.LubChain;
import com.luyublog.lubchain.entity.extra.LotteryPerson;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.*;

/**
 * description:
 *
 * @author luyublog
 * @date 2023/8/14 16:10.
 */
@Service
public class LotteryService {
//    private static volatile List<String> nameList = Arrays.asList("张三", "李四");

    private final List<String> nameList = Arrays.asList("张三", "李四");

    public Map<String, String> nameMapAward = new HashMap<>(nameList.size());

    public String getLatestHash() {
        String resp = HttpUtil.createPost("https://api.shasta.trongrid.io/jsonrpc").
                body("{ \"jsonrpc\": \"2.0\", \"method\": \"eth_getBlockByNumber\", \"params\": [\"latest\", true], \"id\": 114514 }").
                setProxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("localhost", 27170))).
                execute().body();
        JSONObject jsonObject = JSONUtil.parseObj(resp);
        System.out.println(resp);

        return jsonObject.getJSONObject("result").getStr("hash", "");
    }

    @PostConstruct
    public void getNameMapAward() {
        Lottery lottery = new Lottery();
        String externalHash = lottery.getLatestHash();
        System.out.println("externalHash is " + externalHash);

        Block genesisBlock = new Block("0");
        LubChain.addBlock(genesisBlock);

        for (int i = 0; i < 10; i++) {
            String previousHash = LubChain.blockchain.get(LubChain.blockchain.size() - 1).getHash();
            Block block1 = new Block(previousHash);
            LubChain.addBlock(block1);
        }
        String innerHash = LubChain.blockchain.get(LubChain.blockchain.size() - 1).getHash();
        System.out.println("innerHash is " + innerHash);
        System.out.println(JSONUtil.toJsonPrettyStr(LubChain.blockchain));


        String luckyHash = DigestUtil.sha256Hex(innerHash + externalHash);
        System.out.println("luckyHash is " + luckyHash);


        Map<String, LotteryPerson> nameMapHash = new TreeMap<>();

        nameList.forEach(x -> {
            nameMapHash.put(DigestUtil.sha256Hex(x + luckyHash), new LotteryPerson(x));
        });


        int index = 0;
        Map<String, String> nameMapAward = new HashMap<>(23);
        for (Map.Entry<String, LotteryPerson> entry : nameMapHash.entrySet()) {
            index++;
            if (index <= 2) {
                entry.getValue().setAward("升降桌（白色）");
            } else if (index > 2 && index <= 4) {
                entry.getValue().setAward("升降桌（黑色）");
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

    public String getPrize(String userName) {
        String award = this.nameMapAward.get(userName);

        return StrUtil.isBlank(award) ? "" : award + "（附超级大奖）";
    }

//    public static void main(String[] args) {
//        Lottery lottery = new Lottery();
//        lottery.getNameMapAward();
//        System.out.println("hello");
//    }
}

