package com.luyublog.lubchain.entity.chain;

import cn.hutool.json.JSONUtil;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * description:
 *
 * @author luyublog
 * @date 2023/8/12 11:18.
 */
public class LubChain {
    public static ArrayList<Block> blockchain = new ArrayList<Block>();
    //list of all unspent blockTransactions.
    public static HashMap<String, TransactionOutput> UTXOs = new HashMap<String, TransactionOutput>();
    public static int difficulty = 3;
    public static float minimumTransaction = 0.1f;
    public static String INIT_TRANSACTION_ID = "0";
    public static Transaction genesisTransaction;

    public static void main(String[] args) {
        //Create wallets:
        Wallet walletA = new Wallet();
        Wallet walletB = new Wallet();
        Wallet genesisWallet = new Wallet();

        //create genesis transaction, which sends 100 NoobCoin to walletA:
        genesisTransaction = new Transaction(genesisWallet.getPublicKey(), walletA.getPublicKey(), 100f, new ArrayList<>(0));
        //manually sign the genesis transaction
        genesisTransaction.generateSignature(genesisWallet.getPrivateKey());
        //manually set the transaction id
        genesisTransaction.setTransactionId(INIT_TRANSACTION_ID);
        //manually add the Transactions Output
        genesisTransaction.getTransactionOutputs().add(new TransactionOutput(genesisTransaction.getRecipient(), genesisTransaction.getValue(), genesisTransaction.getTransactionId()));
//        genesisTransaction.getInputs().add(new TransactionInput(genesisTransaction.getOutputs().get(0).getId()));
        // 把初始交易加入到资金池
        UTXOs.put(genesisTransaction.getTransactionOutputs().get(0).getId(), genesisTransaction.getTransactionOutputs().get(0));

        System.out.println("Creating and Mining Genesis block... ");
        Block genesisBlock = new Block("0");
        genesisBlock.addTransaction(genesisTransaction);
        LubChain.addBlock(genesisBlock);
        System.out.println("WalletA's balance is: " + walletA.getBalance());

        //testing  todo 交易和块什么关系，为什么要在这个块上？
        Block block1 = new Block(genesisBlock.getHash());
        System.out.println("\nWalletA's balance is: " + walletA.getBalance());
        System.out.println("\nWalletA is Attempting to send funds (40) to WalletB...");
        block1.addTransaction(Transaction.sendFunds(walletA, walletB, 40f));
        LubChain.addBlock(block1);
        System.out.println("\nWalletA's balance is: " + walletA.getBalance());
        System.out.println("WalletB's balance is: " + walletB.getBalance());

        Block block2 = new Block(block1.getHash());
        System.out.println("\nWalletA Attempting to send more funds (1000) than it has...");
        block2.addTransaction(Transaction.sendFunds(walletA, walletB, 1000f));
        LubChain.addBlock(block2);
        System.out.println("\nWalletA's balance is: " + walletA.getBalance());
        System.out.println("WalletB's balance is: " + walletB.getBalance());

        Block block3 = new Block(block2.getHash());
        System.out.println("\nWalletB is Attempting to send funds (20) to WalletA...");
        block3.addTransaction(Transaction.sendFunds(walletB, walletA, 20f));
        System.out.println("\nWalletA's balance is: " + walletA.getBalance());
        System.out.println("WalletB's balance is: " + walletB.getBalance());

        isChainValid();

        System.out.println(JSONUtil.toJsonPrettyStr(blockchain));

    }

    public static Boolean isChainValid() {
        Block currentBlock;
        Block previousBlock;
        String hashTarget = new String(new char[difficulty]).replace('\0', '0');
        //a temporary working list of unspent blockTransactions at a given block state.
        HashMap<String, TransactionOutput> tempUTXOs = new HashMap<String, TransactionOutput>();
        tempUTXOs.put(genesisTransaction.getTransactionOutputs().get(0).getId(), genesisTransaction.getTransactionOutputs().get(0));

        //loop through blockchain to check hashes:
        for (int i = 1; i < blockchain.size(); i++) {

            currentBlock = blockchain.get(i);
            previousBlock = blockchain.get(i - 1);
            //compare registered hash and calculated hash:
            if (!currentBlock.getHash().equals(currentBlock.calculateHash())) {
                System.out.println("#Current Hashes not equal");
                return false;
            }
            //compare previous hash and registered previous hash
            if (!previousBlock.getHash().equals(currentBlock.getPreviousHash())) {
                System.out.println("#Previous Hashes not equal");
                return false;
            }
            //check if hash is solved
            if (!currentBlock.getHash().substring(0, difficulty).equals(hashTarget)) {
                System.out.println("#This block hasn't been mined");
                return false;
            }

            //loop thru blockchains blockTransactions:
            TransactionOutput tempOutput;
            for (int t = 0; t < currentBlock.getBlockTransactions().size(); t++) {
                Transaction currentTransaction = currentBlock.getBlockTransactions().get(t);

                if (!currentTransaction.verifySignature()) {
                    System.out.println("#Signature on Transaction(" + t + ") is Invalid");
                    return false;
                }
                if (currentTransaction.getInputsValue() != currentTransaction.getOutputsValue()) {
                    System.out.println("#Inputs are note equal to outputs on Transaction(" + t + ")");
                    return false;
                }

                for (TransactionInput input : currentTransaction.getTransactionInputs()) {
                    tempOutput = tempUTXOs.get(input.getTransactionOutputId());

                    if (tempOutput == null) {
                        System.out.println("#Referenced input on Transaction(" + t + ") is Missing");
                        return false;
                    }

                    if (!input.getTransactionOutput().getValue().equals(tempOutput.getValue())) {
                        System.out.println("#Referenced input Transaction(" + t + ") value is Invalid");
                        return false;
                    }

                    tempUTXOs.remove(input.getTransactionOutputId());
                }

                for (TransactionOutput output : currentTransaction.getTransactionOutputs()) {
                    tempUTXOs.put(output.getId(), output);
                }

                if (currentTransaction.getTransactionOutputs().get(0).getRecipient() != currentTransaction.getRecipient()) {
                    System.out.println("#Transaction(" + t + ") output reciepient is not who it should be");
                    return false;
                }
                if (currentTransaction.getTransactionOutputs().get(1).getRecipient() != currentTransaction.getSender()) {
                    System.out.println("#Transaction(" + t + ") output 'change' is not sender.");
                    return false;
                }

            }

        }
        System.out.println("Blockchain is valid");
        return true;
    }

    public static void addBlock(Block newBlock) {
        newBlock.mineBlock(difficulty);
        blockchain.add(newBlock);
    }
}

