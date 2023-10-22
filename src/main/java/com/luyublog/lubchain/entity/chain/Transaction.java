package com.luyublog.lubchain.entity.chain;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.codec.Base64;
import cn.hutool.crypto.SignUtil;
import cn.hutool.crypto.asymmetric.Sign;
import cn.hutool.crypto.digest.DigestUtil;
import lombok.Data;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Map;

import static cn.hutool.crypto.asymmetric.SignAlgorithm.SHA1withECDSA;

/**
 * description: Transaction
 *
 * @author luyublog
 * @date 2023/8/12 15:33.
 */
@Data
public class Transaction {
    /**
     * 交易Id
     */
    private String transactionId;

    /**
     * 发送人地址
     */
    private PublicKey sender;

    /**
     * 接收人地址
     */
    private PublicKey recipient;

    /**
     * 交易金额
     */
    private Float value;

    /**
     * 签名；this is to prevent anybody else from spending funds in our wallet.
     */
    private byte[] signature;

    /**
     * 转入（使用）的UTXOS
     */
    private ArrayList<TransactionInput> transactionInputs = new ArrayList<TransactionInput>();

    /**
     * 生成的新的UTXOS
     */
    private ArrayList<TransactionOutput> transactionOutputs = new ArrayList<TransactionOutput>();

    /**
     * 交易系数
     */
    private static Integer sequence = 0;

    // Constructor:
    public Transaction(PublicKey from, PublicKey to, Float value, ArrayList<TransactionInput> transactionInputs) {
        this.sender = from;
        this.recipient = to;
        this.value = value;
        this.transactionInputs = transactionInputs;
    }

    /**
     * 计算交易Id的hash
     *
     * @return IdHash
     */
    private String calculateIdHash() {
        //increase the sequence to avoid 2 identical blockTransactions having the same hash
        sequence++;
        return DigestUtil.sha256Hex(Base64.encode(sender.getEncoded()) + Base64.encode(recipient.getEncoded()) + Float.toString(value) + sequence);
    }


    /**
     * 生成签名
     *
     * @param privateKey 发送者私钥
     */
    public void generateSignature(PrivateKey privateKey) {
        String data = getBase64FromKey(sender) + getBase64FromKey(recipient) + Float.toString(value);
        Sign sign = SignUtil.sign(SHA1withECDSA, privateKey.getEncoded(), null);
        signature = sign.sign(data.getBytes(StandardCharsets.UTF_8));
    }


    /**
     * 校验签名合法性
     *
     * @return result
     */
    public boolean verifySignature() {
        String data = getBase64FromKey(sender) + getBase64FromKey(recipient) + value;
        Sign sign = SignUtil.sign(SHA1withECDSA, null, sender.getEncoded());
        return sign.verify(data.getBytes(), signature);
    }

    /**
     * 校验交易的合法性
     *
     * @return result
     */
    public boolean judgeTransactionValidation() {
        if (!verifySignature()) {
            System.out.println("#Transaction Signature failed to verify");
            return false;
        }

        // 再次确认全局UTXOs内不包含此处作为输入的交易tx
        for (TransactionInput transactionInput : this.getTransactionInputs()) {
            LubChain.UTXOs.remove(transactionInput.getTransactionOutputId());
        }

        // 校验交易金额合法性
        if (this.getInputsValue() < LubChain.minimumTransaction) {
            System.out.println("#Transaction Inputs to small: " + this.getInputsValue());
            return false;
        }

        return true;
    }

    /**
     * 生成可用交易记录
     */
    public void generateUTXOs() {
        // 生成两笔输出交易，一笔是给接收方，一笔是给支付方
        float leftOver = this.getInputsValue() - this.getValue();
        this.transactionId = calculateIdHash();

        // 交易金额发送到收方地址的记录
        this.transactionOutputs.add(new TransactionOutput(this.recipient, value, transactionId));
        // 剩余金额发送给付方地址的记录
        this.transactionOutputs.add(new TransactionOutput(this.sender, leftOver, transactionId));

        // 然后将可用交易记录全部放入全局UTXOs中。(其实并没有什么币，只有UTXO)
        for (TransactionOutput o : this.transactionOutputs) {
            LubChain.UTXOs.put(o.getId(), o);
        }
    }

    /**
     * 计算输入总金额
     *
     * @return 金额
     */
    public float getInputsValue() {
        float total = 0;
        for (TransactionInput transactionInput : this.transactionInputs) {
            if (BeanUtil.isEmpty(transactionInput.getTransactionOutput())) {
                //if Transaction can't be found skip it
                continue;
            }
            total += transactionInput.getTransactionOutput().getValue();
        }
        return total;
    }

    //returns sum of outputs:
    public float getOutputsValue() {
        float total = 0;
        for (TransactionOutput o : transactionOutputs) {
            total += o.getValue();
        }
        return total;
    }


    public static String getBase64FromKey(Key key) {
        return Base64.encode(key.getEncoded());
    }

    /**
     * Generates and returns a new transaction from this wallet.
     *
     * @param recipient recipient
     * @param value     coin
     * @return result
     */
    public static Transaction sendFunds(Wallet sender, Wallet recipient, Float value) {
        //gather balance and check funds.
        if (sender.getBalance() < value) {
            System.out.println("#Not Enough funds to send transaction. Transaction Discarded.");
            return null;
        }
        //create array list of inputs
        ArrayList<TransactionInput> walletInputs = new ArrayList<TransactionInput>();

        // generate input list by all output
        float total = 0;
        for (Map.Entry<String, TransactionOutput> item : sender.getWalletUTXOs().entrySet()) {
            TransactionOutput UTXO = item.getValue();
            total += UTXO.getValue();
            walletInputs.add(new TransactionInput(UTXO.getId(), UTXO));
            if (total > value) {
                break;
            }
        }

        // create a new transaction
        Transaction newTransaction = new Transaction(sender.getPublicKey(), recipient.getPublicKey(), value, walletInputs);
        newTransaction.generateSignature(sender.getPrivateKey());

        // 从全链路的可用币上删除
        for (TransactionInput input : walletInputs) {
            LubChain.UTXOs.remove(input.getTransactionOutputId());
        }
        return newTransaction;
    }
}

