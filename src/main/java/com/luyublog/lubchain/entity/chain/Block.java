package com.luyublog.lubchain.entity.chain;

import cn.hutool.crypto.digest.DigestUtil;
import lombok.Data;

import java.util.ArrayList;

/**
 * description: Block
 *
 * @author luyublog
 * @date 2023/8/11 21:21
 */
@Data
public class Block {
    private final static String INIT_HASH = "0";

    private String hash;
    private String previousHash;
    //our data will be a simple message.
    private ArrayList<Transaction> blockTransactions = new ArrayList<Transaction>();
    private String merkleRoot;
    //as number of milliseconds since 1/1/1970.
    private Long timeStamp;
    // nonce 初始化时随机挑一个，反正后面都会随着hash次数增加
    private Integer nonce = 10;


    // Block Constructor.
    public Block(String previousHash) {
//        this.data = data;
        this.previousHash = previousHash;
        this.timeStamp = System.currentTimeMillis();
        this.hash = calculateHash();
    }

    public String calculateHash() {
        return DigestUtil.sha256Hex(previousHash + Long.toString(timeStamp) + Integer.toString(nonce) + merkleRoot);
    }

    public void mineBlock(int difficulty) {
        merkleRoot = Tools.getMerkleRoot(blockTransactions);
        //Create a string with difficulty * "0"
        getHashBySolvingDifficulty(difficulty);
        System.out.println("Block Mined!!! : " + hash);
    }

    /**
     * 完成设置的目标从而获取正确的hash（计算hash这种完全没有意义）
     *
     * @param difficulty 难度目标
     */
    private void getHashBySolvingDifficulty(int difficulty) {
        String target = new String(new char[difficulty]).replace('\0', '0');
        while (!hash.substring(0, difficulty).equals(target)) {
            nonce++;
            hash = calculateHash();
        }
    }

    /**
     * 将交易加到区块上
     *
     * @param newTransaction tx
     * @return result
     */
    public boolean addTransaction(Transaction newTransaction) {
        //process newTransaction and check if valid, unless block is genesis block then ignore.
        if (newTransaction == null) {
            return false;
        }

        // 判断区块合法性
        if (!Block.INIT_HASH.equals(this.previousHash)) {
            if ((!newTransaction.judgeTransactionValidation())) {
                System.out.println("Transaction failed to process. Discarded.");
                return false;
            }
            newTransaction.generateUTXOs();
        }
        this.blockTransactions.add(newTransaction);
        System.out.println("Transaction Successfully added to Block");
        return true;
    }
}

