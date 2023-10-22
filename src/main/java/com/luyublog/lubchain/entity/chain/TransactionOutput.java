package com.luyublog.lubchain.entity.chain;

import cn.hutool.crypto.digest.DigestUtil;
import lombok.Data;

import java.security.PublicKey;
import java.util.Objects;

/**
 * description: 交易结果
 *
 * @author luyublog
 * @date 2023/8/12 15:35.
 */
@Data
public class TransactionOutput {
    private String id;
    /**
     * 接收者
     */
    private PublicKey recipient;

    /**
     * 金额
     */
    private Float value;

    /**
     * 父交易
     */
    private String parentTransactionId;

    public TransactionOutput(PublicKey recipient, float value, String parentTransactionId) {
        this.recipient = recipient;
        this.value = value;
        this.parentTransactionId = parentTransactionId;
        this.id = DigestUtil.sha256Hex(Tools.getBase64FromKey(recipient) + value + parentTransactionId);
    }

    /**
     * 判断该笔记录是否属于指定地址
     *
     * @param publicKey 公钥(收款地址)
     * @return result
     */
    public boolean isMine(PublicKey publicKey) {
        return Objects.equals(publicKey, recipient);
    }
}

