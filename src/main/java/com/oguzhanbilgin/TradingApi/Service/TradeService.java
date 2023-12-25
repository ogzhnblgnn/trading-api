package com.oguzhanbilgin.TradingApi.Service;

import com.oguzhanbilgin.TradingApi.Entity.Share;
import com.oguzhanbilgin.TradingApi.Entity.Transaction;
import com.oguzhanbilgin.TradingApi.Entity.User;
import com.oguzhanbilgin.TradingApi.Repository.*;
import com.oguzhanbilgin.TradingApi.enums.TransactionStatus;
import com.oguzhanbilgin.TradingApi.enums.TransactionType;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class TradeService {

    private final UserRepository userRepository;
    private final ShareRepository shareRepository;
    private final PortfolioRepository portfolioRepository;
    private final TransactionRepository transactionRepository;

    public TradeService(UserRepository userRepository, ShareRepository shareRepository, PortfolioRepository portfolioRepository, TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.shareRepository = shareRepository;
        this.portfolioRepository = portfolioRepository;
        this.transactionRepository = transactionRepository;
    }

    //ALIM
    public void buyTransaction(Long userId, Long shareId, Double quantity, BigDecimal price) {
        User buyer = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("KULLANICI BULUNAMADI"));
        Share share = shareRepository.findById(shareId).orElseThrow(() -> new RuntimeException("HİSSE SENEDİ BULUNAMADI"));

        Double totalPrice = quantity * share.getPrice().doubleValue();

        // Kullanıcının bakiyesi yeterli mi?
        if (buyer.getBalance() < totalPrice) {
            throw new RuntimeException("Yetersiz bakiye. Alım işlemi gerçekleştirilemedi.");
        }
        // Kullanıcının portföyü var mı ?
        if (buyer.getPortfolio() == null) {
            throw new RuntimeException("Alım işlemi için bir portföy oluşturmanız gerekmektedir.");
        }

        // Satış işlemi için uygun olan alım işlemlerini getir
        List<Transaction> pendingSellTransactions = transactionRepository.findByShareAndTransactionStatusAndTransactionType(share, TransactionStatus.PENDING, TransactionType.SELL);

        // uygun satış var mı?
        if (pendingSellTransactions.isEmpty()) {
            throw new RuntimeException("Uygun satış işlemi bulunamadı.");
        }

        // Hissede o anki fiyatında bekleyen satış emri var mı?
        boolean hasPendingSellOrder = pendingSellTransactions.stream()
                .anyMatch(transaction -> price.equals(share.getPrice()));

        if (!hasPendingSellOrder) {
            Transaction transaction = new Transaction();
            transaction.setPortfolio(buyer.getPortfolio());
            transaction.setTransactionType(TransactionType.BUY);
            transaction.setTransactionStatus(TransactionStatus.PENDING);
            transaction.setQuantity(quantity);
            transaction.setPrice(price.doubleValue());
            transaction.setShare(share);
            transaction.setUser(buyer);
            transaction.setTransactionDate(LocalDate.now());
            transactionRepository.save(transaction);
            throw new RuntimeException("Hissede anlık fiyatta bekleyen satış emri bulunmamaktadır. Emriniz beklemeye alınmıştır.");
        }

        //Satış işlemleri
        for (Transaction sellTransaction : pendingSellTransactions) {

            //Kendi hissemizi satın alamayız
            if (!sellTransaction.getUser().getId().equals(buyer.getId())) {
                Double purchasedQuantity = Math.min(share.getQuantity(), quantity);

                if (purchasedQuantity > 0) {
                    // Alım işlemine uygun ve beklemede olan satış işlemi var mı
                    Transaction pendingSellTransaction = findPendingSellTransaction(share, purchasedQuantity);

                    if (pendingSellTransaction != null) {
                        pendingSellTransaction.setQuantity(pendingSellTransaction.getQuantity() - purchasedQuantity);
                        transactionRepository.save(pendingSellTransaction);

                        Transaction successSellTransaction = new Transaction();
                        successSellTransaction.setTransactionType(TransactionType.SELL);
                        successSellTransaction.setTransactionStatus(TransactionStatus.SUCCESS);
                        successSellTransaction.setQuantity(purchasedQuantity);
                        successSellTransaction.setPrice(sellTransaction.getPrice()); // Satışın gerçekleştiği fiyatı kullanabiliriz
                        successSellTransaction.setTransactionDate(LocalDate.now());
                        successSellTransaction.setShare(share);
                        successSellTransaction.setPortfolio(sellTransaction.getPortfolio());
                        successSellTransaction.setUser(sellTransaction.getUser());
                        transactionRepository.save(successSellTransaction);

                        // seller bakiye güncellemesi
                        User seller = sellTransaction.getUser();
                        seller.setBalance(seller.getBalance() + (purchasedQuantity * sellTransaction.getPrice()));
                        userRepository.save(seller);

                        // seller portföy
                        Share soldShare = shareRepository.findByPortfolioAndSymbol(sellTransaction.getPortfolio(), share.getSymbol());
                        soldShare.setQuantity(soldShare.getQuantity() - purchasedQuantity);
                        shareRepository.save(soldShare);

                        //  Buyer portföy
                        Share boughtShare = shareRepository.findByPortfolioAndSymbol(buyer.getPortfolio(), share.getSymbol());
                        if (boughtShare != null) {
                            // Eğer aynı hisse senedinden varsa miktarı artır
                            boughtShare.setQuantity(boughtShare.getQuantity() + purchasedQuantity);
                            shareRepository.save(boughtShare);
                        } else {
                            // Yoksa yeni bir hisse senedi oluştur
                            Share newBoughtShare = new Share();
                            newBoughtShare.setName(share.getName());
                            newBoughtShare.setSymbol(share.getSymbol());
                            newBoughtShare.setPrice(share.getPrice());
                            newBoughtShare.setQuantity(purchasedQuantity);
                            newBoughtShare.setPortfolio(buyer.getPortfolio());
                            buyer.getPortfolio().getShares().add(newBoughtShare);
                            shareRepository.save(newBoughtShare);
                            portfolioRepository.save(buyer.getPortfolio());
                        }


                        quantity -= purchasedQuantity;

                        // alım işlemini tamamla
                        performBuyTransaction(buyer, share, purchasedQuantity);
                    } else {
                        // Satın alınamayan miktar beklemede olarak kaydedilsin
                        sellTransaction.setTransactionStatus(TransactionStatus.PENDING);
                        transactionRepository.save(sellTransaction);
                    }
                }
            }else{
                throw new RuntimeException("Kendi Hisseni alamazsın!");
            }
        }

        // quantity 0 veya daha küçükse yeni bir Transaction oluşturmasın
        if (quantity > 0) {
            Transaction buyTransaction = new Transaction();
            buyTransaction.setTransactionType(TransactionType.BUY);
            buyTransaction.setTransactionStatus(TransactionStatus.SUCCESS);
            buyTransaction.setQuantity(quantity);
            buyTransaction.setPrice(totalPrice);
            buyTransaction.setTransactionDate(LocalDate.now());
            buyTransaction.setShare(share);
            buyTransaction.setPortfolio(buyer.getPortfolio());
            buyTransaction.setUser(buyer);

            transactionRepository.save(buyTransaction);

        }
        //Quantity hala pozitif mi
        if (quantity > 0) {
            throw new RuntimeException("Alım işlemi tamamlanamadı. Yeterli miktarda satış bulunamadı.");
        }
    }


    //SATIŞ
    private Transaction findPendingSellTransaction(Share share, Double quantity) {
        // Alım işlemi için uygun PENDING SELL işlemini bul
        List<Transaction> pendingSellTransactions = transactionRepository.findByShareAndTransactionStatusAndTransactionType(share, TransactionStatus.PENDING, TransactionType.SELL);

        for (Transaction sellTransaction : pendingSellTransactions) {
            Double pendingSellQuantity = Math.min(sellTransaction.getQuantity(), quantity);

            if (pendingSellQuantity > 0) {
                return sellTransaction;
            }
        }

        return null;
    }

    private void performBuyTransaction(User buyer, Share share, Double quantity) {
        // quantity değeri 0 veya daha küçükse işlemi gerçekleştirme
        if (quantity <= 0) {
            throw new RuntimeException("Geçersiz miktar. Alım işlemi gerçekleştirilemedi.");
        }

        Double totalPrice = quantity * share.getPrice().doubleValue();

        Transaction buyTransaction = new Transaction();
        buyTransaction.setTransactionType(TransactionType.BUY);
        buyTransaction.setTransactionStatus(TransactionStatus.SUCCESS);
        buyTransaction.setQuantity(quantity);
        buyTransaction.setPrice(totalPrice);
        buyTransaction.setTransactionDate(LocalDate.now());
        buyTransaction.setShare(share);
        buyTransaction.setPortfolio(buyer.getPortfolio());
        buyTransaction.setUser(buyer);

        transactionRepository.save(buyTransaction);

        portfolioRepository.save(buyer.getPortfolio());
    }

    public void sellTransaction(Long userId, Long shareId, Double quantity, BigDecimal price) {
        User seller = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("KULLANICI BULUNAMADI"));
        Share share = shareRepository.findById(shareId).orElseThrow(() -> new RuntimeException("HİSSE SENEDİ BULUNAMADI"));

        //Portföy var mı?
        if (seller.getPortfolio() == null) {
            throw new RuntimeException("Satış işlemi için bir portföy oluşturmanız gerekmektedir.");
        }


        // Kullanıcının satmaya çalıştıpı hisse, portföyünde var mı?
        Share userShare = shareRepository.findByPortfolioAndSymbol(seller.getPortfolio(), share.getSymbol());
        if (userShare == null || userShare.getQuantity() < quantity) {
            throw new RuntimeException("Satış yapmak istediğiniz hisseye sahip değilsiniz veya yeterli miktarda hisseye sahip değilsiniz.");
        }

        // Satış işlemi için uygun olan alım işlemlerini getir
        List<Transaction> pendingBuyTransactions = transactionRepository.findByShareAndTransactionStatusAndTransactionType(share, TransactionStatus.PENDING, TransactionType.BUY);

        // Eğer uygun bir alım işlemi bulunamadıysa beklemeDe olarak kaydet
        if (pendingBuyTransactions.isEmpty()) {
            Transaction transaction = new Transaction();
            transaction.setTransactionType(TransactionType.SELL);
            transaction.setTransactionDate(LocalDate.now());
            transaction.setTransactionStatus(TransactionStatus.PENDING);
            transaction.setQuantity(quantity);
            transaction.setUser(seller);
            transaction.setShare(share);
            transaction.setPrice(price.doubleValue());
            transaction.setPortfolio(seller.getPortfolio());
            transactionRepository.save(transaction);
            throw new RuntimeException("Hissede anlık fiyatta bekleyen alım emri bulunmamaktadır. Emriniz beklemeye alınmıştır.");
        }

        for (Transaction buyTransaction : pendingBuyTransactions) {
            Double soldQuantity = Math.min(share.getQuantity(), quantity);

            if (soldQuantity > 0) {
                // Alım işlemi için uygun bekleyen alım işlemini
                Transaction pendingBuyTransaction = findPendingBuyTransaction(share, soldQuantity);

                if (pendingBuyTransaction != null) {
                    pendingBuyTransaction.setQuantity(pendingBuyTransaction.getQuantity() - soldQuantity);
                    transactionRepository.save(pendingBuyTransaction);

                    // Satılan hisse miktarında bir başarılı alım işlemi işlemi oluştur
                    Transaction successSellTransaction = new Transaction();
                    successSellTransaction.setTransactionType(TransactionType.BUY);
                    successSellTransaction.setTransactionStatus(TransactionStatus.SUCCESS);
                    successSellTransaction.setQuantity(soldQuantity);
                    successSellTransaction.setPrice(buyTransaction.getPrice()); // Alımın gerçekleştiği fiyatı kullanabiliriz
                    successSellTransaction.setTransactionDate(LocalDate.now());
                    successSellTransaction.setShare(share);
                    successSellTransaction.setPortfolio(seller.getPortfolio());
                    successSellTransaction.setUser(seller);
                    transactionRepository.save(successSellTransaction);


                    seller.setBalance(seller.getBalance() + (soldQuantity * buyTransaction.getPrice()));
                    userRepository.save(seller);

                    // satıcı portföyünü güncelle
                    Share soldShare = shareRepository.findByPortfolioAndSymbol(seller.getPortfolio(), share.getSymbol());
                    soldShare.setQuantity(soldShare.getQuantity() - soldQuantity);
                    shareRepository.save(soldShare);

                    // Aalıcı portföyünü güncelle
                    Share boughtShare = shareRepository.findByPortfolioAndSymbol(buyTransaction.getPortfolio(), share.getSymbol());
                    if (boughtShare != null) {
                        // Eğer aynı hisse senedinden varsa miktarı artır
                        boughtShare.setQuantity(boughtShare.getQuantity() + soldQuantity);
                        shareRepository.save(boughtShare);
                    } else {
                        // Yoksa yeni bir hisse senedi oluştur
                        Share newBoughtShare = new Share();
                        newBoughtShare.setName(share.getName());
                        newBoughtShare.setSymbol(share.getSymbol());
                        newBoughtShare.setPrice(share.getPrice());
                        newBoughtShare.setQuantity(soldQuantity);
                        newBoughtShare.setPortfolio(buyTransaction.getPortfolio());
                        buyTransaction.getPortfolio().getShares().add(newBoughtShare);
                        shareRepository.save(newBoughtShare);
                        portfolioRepository.save(buyTransaction.getPortfolio());
                    }

                    quantity -= soldQuantity;
                    //
                    performSellTransaction(seller, share, soldQuantity);
                } else {
                    buyTransaction.setTransactionStatus(TransactionStatus.PENDING);
                    transactionRepository.save(buyTransaction);

                    throw new RuntimeException("Kendi alım emirlerinize satış yapamazsınız.");
                }
            }
        }

        if (quantity > 0) {
            Transaction sellTransaction = new Transaction();
            sellTransaction.setTransactionType(TransactionType.SELL);
            sellTransaction.setTransactionStatus(TransactionStatus.SUCCESS);
            sellTransaction.setQuantity(quantity);
            sellTransaction.setPrice(share.getPrice().doubleValue() * quantity);
            sellTransaction.setTransactionDate(LocalDate.now());
            sellTransaction.setShare(share);
            sellTransaction.setPortfolio(seller.getPortfolio());
            sellTransaction.setUser(seller);

            transactionRepository.save(sellTransaction);

            seller.setBalance(seller.getBalance() + sellTransaction.getPrice());
            userRepository.save(seller);

            Share soldShare = shareRepository.findByPortfolioAndSymbol(seller.getPortfolio(), share.getSymbol());
            soldShare.setQuantity(soldShare.getQuantity() - quantity);
            shareRepository.save(soldShare);
        }

        if (quantity > 0) {
            throw new RuntimeException("Satış işlemi tamamlanamadı. Yeterli miktarda alım bulunamadı.");
        }
    }

    private Transaction findPendingBuyTransaction(Share share, Double quantity) {
        // Satış işlemine göre bekleyen alım işlemlerini bul
        List<Transaction> pendingBuyTransactions = transactionRepository.findByShareAndTransactionStatusAndTransactionType(share, TransactionStatus.PENDING, TransactionType.BUY);

        for (Transaction buyTransaction : pendingBuyTransactions) {
            Double pendingBuyQuantity = Math.min(buyTransaction.getQuantity(), quantity);

            if (pendingBuyQuantity > 0) {
                return buyTransaction;
            }
        }

        return null;
    }

    private void performSellTransaction(User seller, Share share, Double quantity) {
        if (quantity <= 0) {
            throw new RuntimeException("Geçersiz miktar. Satış işlemi gerçekleştirilemedi.");
        }

        Double totalPrice = quantity * share.getPrice().doubleValue();

        // Satış işlemi
        Transaction sellTransaction = new Transaction();
        sellTransaction.setTransactionType(TransactionType.SELL);
        sellTransaction.setTransactionStatus(TransactionStatus.SUCCESS);
        sellTransaction.setQuantity(quantity);
        sellTransaction.setPrice(totalPrice);
        sellTransaction.setTransactionDate(LocalDate.now());
        sellTransaction.setShare(share);
        sellTransaction.setPortfolio(seller.getPortfolio());
        sellTransaction.setUser(seller);

        transactionRepository.save(sellTransaction);

    }
}