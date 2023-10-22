package com.luyublog.lubchain.entity.chain;

import cn.hutool.core.codec.Base64;
import cn.hutool.crypto.digest.DigestUtil;

import java.security.Key;
import java.util.ArrayList;

/**
 * description: tools
 *
 * @author luyublog
 * @date 2023/8/12 16:08.
 */
public class Tools {
    public static String getBase64FromKey(Key key) {
        return Base64.encode(key.getEncoded());
    }

    //Tacks in array of blockTransactions and returns a merkle root.
    public static String getMerkleRoot(ArrayList<Transaction> transactions) {
        int count = transactions.size();
        ArrayList<String> previousTreeLayer = new ArrayList<String>();
        for (Transaction transaction : transactions) {
            previousTreeLayer.add(transaction.getTransactionId());
        }
        ArrayList<String> treeLayer = previousTreeLayer;
        while (count > 1) {
            treeLayer = new ArrayList<String>();
            for (int i = 1; i < previousTreeLayer.size(); i++) {
                treeLayer.add(DigestUtil.sha256Hex(previousTreeLayer.get(i - 1) + previousTreeLayer.get(i)));
            }
            count = treeLayer.size();
            previousTreeLayer = treeLayer;
        }
        String merkleRoot = (treeLayer.size() == 1) ? treeLayer.get(0) : "";
        return merkleRoot;
    }
}

