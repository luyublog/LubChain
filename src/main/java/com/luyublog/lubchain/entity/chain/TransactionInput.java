package com.luyublog.lubchain.entity.chain;

import lombok.Data;

/**
 * description:
 *
 * @author luyublog
 * @date 2023/8/12 15:35.
 */
@Data
public class TransactionInput {
    /**
     * Reference to TransactionOutputs -> transactionId
     * 使用的UTXO的id
     */
    private String transactionOutputId;
    /**
     * Contains the Unspent transaction output
     * id对应的UTXO信息
     */
    private TransactionOutput transactionOutput;

    public TransactionInput(String transactionOutputId, TransactionOutput transactionOutput) {
        this.transactionOutputId = transactionOutputId;
        this.transactionOutput = transactionOutput;
    }
}

