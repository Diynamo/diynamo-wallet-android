package com.wallet.crypto.alphawallet.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;

import com.wallet.crypto.alphawallet.entity.GasSettings;
import com.wallet.crypto.alphawallet.entity.NetworkInfo;
import com.wallet.crypto.alphawallet.entity.Ticket;
import com.wallet.crypto.alphawallet.entity.TokenInfo;
import com.wallet.crypto.alphawallet.entity.Wallet;
import com.wallet.crypto.alphawallet.interact.CreateTransactionInteract;
import com.wallet.crypto.alphawallet.interact.FindDefaultNetworkInteract;
import com.wallet.crypto.alphawallet.interact.FindDefaultWalletInteract;
import com.wallet.crypto.alphawallet.repository.TokenRepository;
import com.wallet.crypto.alphawallet.service.MarketQueueService;
import com.wallet.crypto.alphawallet.ui.TransferTicketDetailActivity;

import org.web3j.tx.Contract;

import java.math.BigInteger;

/**
 * Created by James on 21/02/2018.
 */

public class TransferTicketDetailViewModel extends BaseViewModel {
    private final MutableLiveData<Wallet> defaultWallet = new MutableLiveData<>();
    private final MutableLiveData<GasSettings> gasSettings = new MutableLiveData<>();
    private final MutableLiveData<NetworkInfo> defaultNetwork = new MutableLiveData<>();
    private final MutableLiveData<String> newTransaction = new MutableLiveData<>();

    private final FindDefaultNetworkInteract findDefaultNetworkInteract;
    private final FindDefaultWalletInteract findDefaultWalletInteract;
    private final MarketQueueService marketQueueService;
    private final CreateTransactionInteract createTransactionInteract;

    TransferTicketDetailViewModel(FindDefaultNetworkInteract findDefaultNetworkInteract,
                                  FindDefaultWalletInteract findDefaultWalletInteract,
                                  MarketQueueService marketQueueService,
                                  CreateTransactionInteract createTransactionInteract) {
        this.findDefaultNetworkInteract = findDefaultNetworkInteract;
        this.findDefaultWalletInteract = findDefaultWalletInteract;
        this.marketQueueService = marketQueueService;
        this.createTransactionInteract = createTransactionInteract;
    }

    public LiveData<Wallet> defaultWallet() {
        return defaultWallet;
    }
    public LiveData<String> newTransaction() { return newTransaction; }

    public void prepare(Ticket ticket) {
        disposable = findDefaultNetworkInteract
                .find()
                .subscribe(this::onDefaultNetwork, this::onError);
    }

    private void onDefaultNetwork(NetworkInfo networkInfo) {
        defaultNetwork.postValue(networkInfo);
        disposable = findDefaultWalletInteract
                .find()
                .subscribe(this::onDefaultWallet, this::onError);
    }

    private void onDefaultWallet(Wallet wallet) {
        defaultWallet.setValue(wallet);
    }

    public void generateSalesOrders(String contractAddr, BigInteger price, int[] ticketIndicies, int firstTicketId)
    {
        marketQueueService.createSalesOrders(defaultWallet.getValue(), price, ticketIndicies, contractAddr, firstTicketId, processMessages);
    }

    public void setWallet(Wallet wallet)
    {
        defaultWallet.setValue(wallet);
    }

    public void createTicketTransfer(String to, String contractAddress, String indexList, BigInteger gasPrice, BigInteger gasLimit)
    {
        final byte[] data = TokenRepository.createTicketTransferData(to, indexList);
        disposable = createTransactionInteract
                .create(defaultWallet.getValue(), contractAddress, BigInteger.valueOf(0), gasPrice, gasLimit, data)
                .subscribe(this::onCreateTransaction, this::onError);
    }

    private void onCreateTransaction(String transaction)
    {
        newTransaction.postValue(transaction);
    }
}
