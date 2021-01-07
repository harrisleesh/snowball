package org.kiworkshop.snowball.portfolio.service;

import lombok.RequiredArgsConstructor;
import org.kiworkshop.snowball.auth.IAuthenticationFacade;
import org.kiworkshop.snowball.common.type.TransactionType;
import org.kiworkshop.snowball.portfolio.controller.dto.PortfolioStockResponseDto;
import org.kiworkshop.snowball.portfolio.util.ProfitCalculator;
import org.kiworkshop.snowball.stockdetail.entity.StockDetail;
import org.kiworkshop.snowball.stocktransaction.entity.StockTransaction;
import org.kiworkshop.snowball.stocktransaction.entity.StockTransactionRepository;
import org.kiworkshop.snowball.user.entity.User;
import org.kiworkshop.snowball.user.entity.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class PortfolioSummaryService {

    private final StockTransactionRepository stockTransactionRepository;
    private final IAuthenticationFacade iAuthenticationFacade;
    private final ProfitCalculator profitCalculator;

    public List<PortfolioStockResponseDto> getPortfolioSummary() {

        User user = iAuthenticationFacade.getUser();
        List<StockTransaction> stockTransactions = stockTransactionRepository.findByUserId(user.getId());
        Set<Map.Entry<StockDetail, List<StockTransaction>>> stockTransactionGroups = createStockTransactionGroups(stockTransactions);
        List<PortfolioStockResponseDto> portfolioStockResponseDtos = new ArrayList<>();

        // 연산을 위한 일급컬렉션, 서비스에서는 이것을 사용
        for (Map.Entry<StockDetail, List<StockTransaction>> stockTransactionGroup : stockTransactionGroups) {
            int[] buyingPrices = getBuyingPrices(stockTransactionGroup);
            double averageBuyingPrice = getAverageBuyingPrice(buyingPrices);
            Long currentPrice = getCurrentPrice(stockTransactions);
            double earningRate = getEarningRate(averageBuyingPrice, currentPrice);

            PortfolioStockResponseDto portfolioStockResponseDto = getPortfolioStockResponseDto(stockTransactionGroup, (long) averageBuyingPrice, earningRate);

            portfolioStockResponseDtos.add(portfolioStockResponseDto);
        }

        return portfolioStockResponseDtos;
    }

    private double getEarningRate(double averageBuyingPrice, Long currentPrice) {
        return (currentPrice - averageBuyingPrice) / averageBuyingPrice;
    }

    private Long getCurrentPrice(List<StockTransaction> stockTransactions) {
        return stockTransactions.get(stockTransactions.size() - 1).getTradedPrice();
    }

    private Set<Map.Entry<StockDetail, List<StockTransaction>>> createStockTransactionGroups(List<StockTransaction> stockTransactions) {
        Map<StockDetail, List<StockTransaction>> stockTransactionGroups = stockTransactions
                .stream().collect(Collectors.groupingBy(StockTransaction::getStockDetail));
        return stockTransactionGroups.entrySet();
    }

    private int[] getBuyingPrices(Map.Entry<StockDetail, List<StockTransaction>> stockTransactionGroup) {
        return stockTransactionGroup.getValue().stream()
                .filter(stockTransaction -> stockTransaction.getTransactionType().equals(TransactionType.BUY))
                .mapToInt(stockTransaction -> Math.toIntExact(stockTransaction.getTradedPrice())).toArray();
    }

    private double getAverageBuyingPrice(int[] buyingPrices) {
        return Arrays.stream(buyingPrices).average().orElseThrow();
    }

    private PortfolioStockResponseDto getPortfolioStockResponseDto(Map.Entry<StockDetail, List<StockTransaction>> stockTransactionGroup, long averageBuyingPrice, double earningRate) {
        return PortfolioStockResponseDto.builder()
                .companyName(stockTransactionGroup.getKey().getCompanyName())
                .averageBuyingPrice(averageBuyingPrice)
                .earningsRate(earningRate)
                .targetPrice(0L)
                .targetEarningsRate(0L)
                .build();
    }
}
