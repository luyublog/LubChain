package com.luyublog.lubchain.entity.chain;

import cn.hutool.crypto.KeyUtil;
import lombok.Data;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

/**
 * description: wallet
 *
 * @author luyublog
 * @date 2023/8/12 15:17.
 */
@Data
public class Wallet {
    private PrivateKey privateKey;
    private PublicKey publicKey;

    private HashMap<String, TransactionOutput> walletUTXOs = new HashMap<String, TransactionOutput>();

    public Wallet() {
        generateKeyPair();
    }

    public void generateKeyPair() {
        try {
            KeyPair keyPair = KeyUtil.generateKeyPair("ECDSA");
            privateKey = keyPair.getPrivate();
            publicKey = keyPair.getPublic();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Float getBalance() {
        float total = 0;
        for (Map.Entry<String, TransactionOutput> item : LubChain.UTXOs.entrySet()) {
            TransactionOutput UTXO = item.getValue();
            //if output belongs to me ( if coins belong to me )
            if (UTXO.isMine(publicKey)) {
                //add it to our list of unspent blockTransactions.
                this.walletUTXOs.put(UTXO.getId(), UTXO);
                total += UTXO.getValue();
            }
        }
        return total;
    }


//    /**
//     * Generates and returns a new transaction from this wallet.
//     *
//     * @param recipient recipient
//     * @param value     coin
//     * @return result
//     */
//    public Transaction sendFunds(PublicKey recipient, Float value) {
//        //gather balance and check funds.
//        if (getBalance() < value) {
//            System.out.println("#Not Enough funds to send transaction. Transaction Discarded.");
//            return null;
//        }
//        //create array list of inputs
//        ArrayList<TransactionInput> walletInputs = new ArrayList<TransactionInput>();
//
//        float total = 0;
//        for (Map.Entry<String, TransactionOutput> item : this.walletUTXOs.entrySet()) {
//            TransactionOutput UTXO = item.getValue();
//            total += UTXO.getValue();
//            walletInputs.add(new TransactionInput(UTXO.getId()));
//            if (total > value) {
//                break;
//            }
//        }
//
//        Transaction newTransaction = new Transaction(publicKey, recipient, value, walletInputs);
//        newTransaction.generateSignature(privateKey);
//
//        for (TransactionInput input : walletInputs) {
//            this.walletUTXOs.remove(input.getTransactionOutputId());
//        }
//        return newTransaction;
//    }

}

